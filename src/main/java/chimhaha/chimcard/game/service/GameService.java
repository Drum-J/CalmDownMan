package chimhaha.chimcard.game.service;

import chimhaha.chimcard.entity.*;
import chimhaha.chimcard.exception.ResourceNotFoundException;
import chimhaha.chimcard.game.dto.BattleCardDto;
import chimhaha.chimcard.game.dto.FieldCardDto;
import chimhaha.chimcard.game.dto.GameInfoDto;
import chimhaha.chimcard.game.dto.MyGameCardDto;
import chimhaha.chimcard.game.event.*;
import chimhaha.chimcard.game.repository.GameCardRepository;
import chimhaha.chimcard.game.repository.GameRoomRepository;
import io.micrometer.core.annotation.Counted;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static chimhaha.chimcard.common.MessageConstants.*;
import static chimhaha.chimcard.entity.GameStatus.*;
import static java.util.Comparator.comparingInt;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameService {

    private final GameRoomRepository gameRoomRepository;
    private final GameCardRepository gameCardRepository;
    private final ApplicationEventPublisher eventPublisher;

    public Long checkGameRoom(Long playerId) {
        return gameRoomRepository.findByPlayerIdAndStatus(playerId, List.of(DISCONNECTED, PLAYING)).map(GameRoom::getId).orElse(null);
    }

    /**
     * 게임 입장 시 상대 닉네임과 내 게임 카드 로딩
     */
    @Retryable(
            retryFor = {OptimisticLockingFailureException.class},
            backoff = @Backoff(delay = 100)
    )
    @Transactional
    public GameInfoDto gameInfo(Long gameRoomId, Long playerId, String sessionId) {
        GameRoom gameRoom = gameRoomRepository.findWithPlayersById(gameRoomId)
                .orElseThrow(() -> new ResourceNotFoundException(GAME_ROOM_NOT_FOUND));

        if (gameRoom.isFinished()) {
            throw new IllegalArgumentException("종료된 게임입니다.");
        }

        Account player1 = gameRoom.getPlayer1();
        Account player2 = gameRoom.getPlayer2();

        if (!(player1.getId().equals(playerId) || player2.getId().equals(playerId))) {
            throw new IllegalArgumentException(NOT_MY_GAME);
        }

        // 세션 ID 저장 로직 추가
        if (player1.getId().equals(playerId)) {
            //player1의 재접속, 기존 Session ID 값과 다른 경우
            if (gameRoom.isDisconnected() && !gameRoom.getP1SessionId().equals(sessionId)) {
                gameRoom.reconnect();
                eventPublisher.publishEvent(new ReconnectEvent(gameRoomId, player2.getId()));
            }
            gameRoom.updatePlayer1SessionId(sessionId);
        } else {
            //player2의 재접속
            if (gameRoom.isDisconnected() && !gameRoom.getP2SessionId().equals(sessionId)) {
                gameRoom.reconnect();
                eventPublisher.publishEvent(new ReconnectEvent(gameRoomId, player1.getId()));
            }
            gameRoom.updatePlayer2SessionId(sessionId);
        }

        String otherPlayer = player1.getId().equals(playerId) ? player2.getNickname() : player1.getNickname();

        // 내 손패 카드
        List<MyGameCardDto> myCards = gameCardRepository.findWithCardByGameRoomAndPlayerId(gameRoomId, playerId)
                .stream().filter(gameCard -> gameCard.getLocation().equals(CardLocation.HAND))
                .map(gameCard -> new MyGameCardDto(gameCard.getId(),gameCard.getCard())).toList();

        // 전체 필드 카드
        List<GameCard> fieldCards = getGameCardsInLocation(gameRoomId, CardLocation.FIELD);
        Map<Integer, FieldCardDto> cardMap =
                updateFieldCardMapForPlayer(fieldCards, playerId, gameRoom.getTurnCount(), null);

        return new GameInfoDto(otherPlayer, myCards, gameRoom.getCurrentTurnPlayerId(), player1.getId(), player2.getId(), cardMap);
    }

    @Counted("game.playing")
    @Transactional
    public void cardSubmit(Long gameRoomId, Long playerId, Long gameCardId) {
        GameRoom gameRoom = getGameRoomAndValidateTurn(gameRoomId, playerId);
        GameCard gameCard = getGameCardAndValidate(playerId, gameCardId);

        processCardMove(gameRoom, gameCard);
        gameRoom.increaseTurnCount();
        if (checkFieldCondition(gameRoom)) {
            eventPublisher.publishEvent(new GameEndEvent(gameRoomId));
        }

        gameRoom.changeTurn();
        createSubmitEvent(gameRoom, playerId);
    }

    @Counted("game.playing")
    @Transactional
    public void battleStart(Long gameRoomId, BattleCardDto dto) {
        GameCard card1 = gameCardRepository.findWithCardAndRoomById(dto.gameCardId1())
                .orElseThrow(() -> new ResourceNotFoundException(GAME_CARD_NOT_FOUND));
        GameCard card2 = gameCardRepository.findWithCardAndRoomById(dto.gameCardId2())
                .orElseThrow(() -> new ResourceNotFoundException(GAME_CARD_NOT_FOUND));
        GameRoom gameRoom = card1.getGameRoom();

        if (!gameRoom.getId().equals(gameRoomId)) {
            throw new IllegalArgumentException(GAME_ROOM_NOT_FOUND);
        }

        Long winnerId = battle(card1, card2);

        // 현재 필드 상태 업데이트
        List<GameCard> fieldCards = getGameCardsInLocation(gameRoom.getId(), CardLocation.FIELD);
        Map<Integer, FieldCardDto> cardMap1 =
                updateFieldCardMapForPlayer(fieldCards, card1.getPlayerId(), gameRoom.getTurnCount(), null);
        Map<Integer, FieldCardDto> cardMap2 =
                updateFieldCardMapForPlayer(fieldCards, card2.getPlayerId(), gameRoom.getTurnCount(), null);

        if (checkGraveCard(gameRoom)) { // 무덤 카드 체크
            eventPublisher.publishEvent(new GameEndEvent(gameRoom.getId()));
        }

        eventPublisher.publishEvent(
                new BattleEvent(
                        gameRoomId, card1.getPlayerId(), card2.getPlayerId(), gameRoom.getCurrentTurnPlayerId(),
                        winnerId , cardMap1, cardMap2 ,null ,null,
                        gameRoom.getWinnerId()
                )
        );
    }

    /**
     * 필드 카드로 직접 배틀 실행
     * 카드의 fieldPosition과 상관없이 가장 앞에 있는 카드로 승부
     */
    @Counted("game.playing")
    @Transactional
    public void fieldBattle(Long gameRoomId, Long currentPlayerId) {
        GameRoom gameRoom = getGameRoomAndValidateTurn(gameRoomId, currentPlayerId);
        gameRoom.increaseTurnCount();
        Long player1Id = gameRoom.getPlayer1().getId();
        Long player2Id = gameRoom.getPlayer2().getId();

        List<GameCard> fieldCards = gameCardRepository.findWithCardByGameRoomAndLocation(gameRoom.getId(), CardLocation.FIELD);
        GameCard player1Card = getPlayer1Card(fieldCards, player1Id);
        GameCard player2Card = getPlayer2Card(fieldCards, player1Id);
        if (player1Card == null || player2Card == null) {
            throw new IllegalArgumentException("필드 배틀을 진행할 수 없습니다.");
        }
        player1Card.turnFront();
        player2Card.turnFront();

        Long winnerId = battle(player1Card, player2Card);

        gameRoom.changeTurn();
        // 현재 필드 상태 업데이트 조회된 fieldCards를 stream.filter 로 데이터 판별
        List<GameCard> fieldCardsResult = fieldCards.stream()
                .filter(gameCard -> gameCard.getLocation().equals(CardLocation.FIELD)).toList();
        Map<Integer, FieldCardDto> cardMap1 =
                updateFieldCardMapForPlayer(fieldCardsResult, player1Id, gameRoom.getTurnCount(), null);
        Map<Integer, FieldCardDto> cardMap2 =
                updateFieldCardMapForPlayer(fieldCardsResult, player2Id, gameRoom.getTurnCount(), null);

        if (checkGraveCard(gameRoom)) {
            eventPublisher.publishEvent(new GameEndEvent(gameRoomId));
        }

        eventPublisher.publishEvent(
                new BattleEvent(
                        gameRoomId, player1Id, player2Id, gameRoom.getCurrentTurnPlayerId(),
                        winnerId , cardMap1, cardMap2, player1Card.getCard().getImageUrl(), player2Card.getCard().getImageUrl(),
                        gameRoom.getWinnerId()
                )
        );
    }

    @Counted("game.playing")
    @Transactional
    public void surrender(Long gameRoomId, Long PlayerId) {
        log.info("surrender playerId: {}", PlayerId);
        GameRoom gameRoom = gameRoomRepository.findWithPlayersById(gameRoomId)
                .orElseThrow(() -> new ResourceNotFoundException(GAME_ROOM_NOT_FOUND));

        if (gameRoom.isFinished()) {
            throw new IllegalArgumentException("이미 종료된 게임입니다.");
        }

        Long player1Id = gameRoom.getPlayer1().getId();
        Long player2Id = gameRoom.getPlayer2().getId();

        if (player1Id.equals(PlayerId)) {
            gameRoom.gameWinner(player2Id);
        } else if (player2Id.equals(PlayerId)) {
            gameRoom.gameWinner(player1Id);
        } else {
            throw new IllegalArgumentException("해당 게임의 플레이어가 아닙니다.");
        }

        eventPublisher.publishEvent(new SurrenderEvent(gameRoomId, player1Id, player2Id, gameRoom.getWinnerId()));
        eventPublisher.publishEvent(new GameEndEvent(gameRoomId));
    }

    @Counted("game.playing")
    @Transactional
    public void timeout(Long gameRoomId, Long playerId) {
        GameRoom gameRoom = gameRoomRepository.findWithPlayersById(gameRoomId)
                .orElseThrow(() -> new ResourceNotFoundException(GAME_ROOM_NOT_FOUND));

        // 60초 제한이 지난 후 여전히 연결이 끊겨있으면?
        if (gameRoom.isDisconnected()) {
            gameRoom.gameWinner(playerId); // 게임 방에 남아있는 사람이 승리
            eventPublisher.publishEvent(new TimeoutEvent(gameRoomId, playerId, gameRoom.getWinnerId()));
            eventPublisher.publishEvent(new GameEndEvent(gameRoomId));
        } else {
            throw new IllegalArgumentException("재연결 되었거나 끝난 게임입니다. 새로고침을 시도해주세요.");
        }
    }

    /**
     * 카드 제출 및 필드 카드 이동
     */
    private void processCardMove(GameRoom gameRoom, GameCard gameCard) {
        Long playerId = gameCard.getPlayerId();
        Long player1Id = gameRoom.getPlayer1().getId();

        // 해당 플레이어의 필드 카드만 가져오기
        List<GameCard> playersCards = getGameCardsInLocation(gameRoom.getId(), CardLocation.FIELD)
                .stream().filter(gc -> gc.getPlayerId().equals(playerId)).toList();

        if (playersCards.size() >= 6) {
            throw new IllegalArgumentException("필드가 가득 차서 카드를 제출할 수 없습니다.");
        }

        // 기존 필드 카드 전진 player1은 [1] -> [6] / player2는 [1] <- [6] 로 움직임
        for (GameCard fieldCard : playersCards) {
            if (playerId.equals(player1Id)) { // Player1의 카드
                fieldCard.moveRight();
            } else { // Player2의 카드
                fieldCard.moveLeft();
            }
        }

        // 새 카드 필드에 배치
        int initialPosition = (player1Id.equals(playerId)) ? 1 : 6;
        gameCard.handToField(initialPosition);
    }

    /**
     * 각 플레이어에게 보낼 매세지 생성
     */
    private void createSubmitEvent(GameRoom gameRoom, Long currentPlayerId) {
        List<GameCard> fieldCards = getGameCardsInLocation(gameRoom.getId(), CardLocation.FIELD);
        BattleCardDto battleCardDto = checkForBattle(fieldCards, gameRoom.getPlayer1().getId());

        Long nextPlayerId = gameRoom.getCurrentTurnPlayerId();
        Map<Integer, FieldCardDto> cardMap1 = updateFieldCardMapForPlayer(fieldCards, currentPlayerId, gameRoom.getTurnCount(), battleCardDto);
        Map<Integer, FieldCardDto> cardMap2 = updateFieldCardMapForPlayer(fieldCards, nextPlayerId, gameRoom.getTurnCount(), battleCardDto);

        // 현재 플레이어의 핸드 조회
        List<MyGameCardDto> myHandCards = getCurrentHandCards(gameRoom, currentPlayerId);

        eventPublisher.publishEvent(
                new CardSubmitEvent(gameRoom.getId(), currentPlayerId, nextPlayerId,
                        cardMap1, cardMap2, battleCardDto, myHandCards, gameRoom.getWinnerId())
        );
    }

    /**
     * 각 플레이어 별 필드 카드 데이터
     */
    private Map<Integer, FieldCardDto> updateFieldCardMapForPlayer(List<GameCard> fieldCards, Long playerId, int turnCount, BattleCardDto battleCardDto) {
        Map<Integer, FieldCardDto> maps = new HashMap<>();

        for (GameCard gameCard : fieldCards) {
            // 필드 카드가 내 것인지 확인
            boolean isMine = gameCard.getPlayerId().equals(playerId);

            // 앞면 표시 여부 확인
            // 1. 각 플레이어가 처음으로 제출한 카드
            if (turnCount == 2 && (gameCard.getFieldPosition() == 1 || gameCard.getFieldPosition() == 6)) {
                gameCard.turnFront();
            }
            // 2. 카드 전투가 가능한 카드
            if (battleCardDto != null && (gameCard.getId().equals(battleCardDto.gameCardId1()) || gameCard.getId().equals(battleCardDto.gameCardId2()))) {
                gameCard.turnFront();
            }

            FieldCardDto dto = new FieldCardDto(gameCard, isMine);
            maps.put(gameCard.getFieldPosition(), dto);
        }

        return maps;
    }

    /**
     * 플레이어의 현재 핸드 조회
     */
    private List<MyGameCardDto> getCurrentHandCards(GameRoom gameRoom, Long playerId) {
        return getGameCardsInLocation(gameRoom.getId(), CardLocation.HAND)
                .stream().filter(card -> card.getPlayerId().equals(playerId))
                .map(card -> new MyGameCardDto(card.getId(), card.getCard()))
                .toList();
    }

    /**
     * 카드 승부 발생 체크
     */
    private BattleCardDto checkForBattle(List<GameCard> fieldCards, Long player1Id) {
        if (fieldCards.size() == 6) {
            GameCard player1Card = getPlayer1Card(fieldCards, player1Id);
            GameCard player2Card = getPlayer2Card(fieldCards, player1Id);

            if (player1Card != null && player2Card != null) {
                player1Card.turnFront();
                player2Card.turnFront();
                return new BattleCardDto(player1Card, player2Card);
            }
        }

        return null;
    }

    /**
     * 두 카드의 승패를 결정
     */
    private Long battle(GameCard player1Card, GameCard player2Card) {
        Long winnerId = 0L;
        int result = player1Card.getCard().match(player2Card.getCard());
        switch (result) {
            case 1 -> { // player1 승리
                player2Card.toGrave();
                log.info("Player1 승리! Player2 카드 무덤으로 이동: {}", player2Card.getCard().getTitle());
                winnerId = player1Card.getPlayerId();
            }
            case 0 -> { // 무승부
                player1Card.toGrave();
                player2Card.toGrave();
                log.info("무승부! 양쪽 카드 무덤으로 이동: {} vs {}", player1Card.getCard().getTitle(), player2Card.getCard().getTitle());
            }
            case -1 -> { // player1 패배 (player2 승리)
                player1Card.toGrave();
                log.info("Player1 패배! Player1 카드 무덤으로 이동: {}", player1Card.getCard().getTitle());
                winnerId = player2Card.getPlayerId();
            }
            default -> throw new IllegalStateException("Unexpected battle result: " + result);
        }

        return winnerId;
    }

    /**
     * 게임 승패 조건 1. 한 플레이어가 모든 필드를 차지했는가?
     */
    private boolean checkFieldCondition(GameRoom gameRoom) {
        List<GameCard> fieldCards = getGameCardsInLocation(gameRoom.getId(), CardLocation.FIELD);

        if (fieldCards.size() == 6) {
            Long player1Id = gameRoom.getPlayer1().getId();
            Long player2Id = gameRoom.getPlayer2().getId();

            Map<Long, Long> fieldCardMap = fieldCards.stream()
                    .collect(Collectors.groupingBy(GameCard::getPlayerId, Collectors.counting()));

            if (fieldCardMap.get(player1Id) == 6L) {
                gameRoom.gameWinner(player1Id);
                return true;
            } else if (fieldCardMap.get(player2Id) == 6L) {
                gameRoom.gameWinner(player2Id);
                return true;
            }
        }

        return false;
    }

    /**
     * 게임 승패 조건 2. 한 플레이어의 카드가 모두 무덤에 갔는가?
     */
    private boolean checkGraveCard(GameRoom gameRoom) {
        List<GameCard> graveCards = getGameCardsInLocation(gameRoom.getId(), CardLocation.GRAVE);
        Map<Long, Long> graveCardCountMap = graveCards.stream()
                .collect(Collectors.groupingBy(GameCard::getPlayerId, Collectors.counting()));

        log.info("graveCardCountMap: {}", graveCardCountMap);

        List<Long> loserIds = new ArrayList<>();
        for (Map.Entry<Long, Long> entry : graveCardCountMap.entrySet()) {
            Long playerId = entry.getKey();
            Long graveCardCount = entry.getValue();

            if (graveCardCount == 7L) {
                loserIds.add(playerId);
            }
        }

        if (loserIds.size() == 2) { // 무승부, 마지막으로 승부한 카드가 최종 무승부로 동시에 무덤으로 이동할 경우
            gameRoom.gameWinner(0L); // 무승부는 게임 상태만 변경 winnerId 는 0으로 둔다.
            return true;
        } else if (loserIds.size() == 1) { // 승패 결정
            Long loserId = loserIds.getFirst();
            Long winnerId = gameRoom.getPlayer1().getId().equals(loserId)
                    ? gameRoom.getPlayer2().getId()
                    : gameRoom.getPlayer1().getId();

            gameRoom.gameWinner(winnerId);
            return true;
        }

        return false;
    }

    private GameRoom getGameRoomAndValidateTurn(Long gameRoomId, Long playerId) {
        GameRoom gameRoom = gameRoomRepository.findWithPlayersById(gameRoomId)
                .orElseThrow(() -> new ResourceNotFoundException(GAME_ROOM_NOT_FOUND));

        if (!gameRoom.getCurrentTurnPlayerId().equals(playerId)) {
            throw new IllegalArgumentException(NOT_MY_TURN);
        }

        return gameRoom;
    }

    private GameCard getGameCardAndValidate(Long playerId, Long gameCardId) {
        GameCard gameCard = gameCardRepository.findById(gameCardId)
                .orElseThrow(() -> new ResourceNotFoundException(GAME_CARD_NOT_FOUND));

        if (!gameCard.getPlayerId().equals(playerId)) {
            throw new IllegalArgumentException(NOT_MY_CARD);
        }

        if (gameCard.getLocation() != CardLocation.HAND) {
            throw new IllegalArgumentException(NOT_IN_HAND);
        }

        return gameCard;
    }

    private GameCard getPlayer1Card(List<GameCard> cardList, Long player1Id) {
        return cardList.stream()
                .filter(card -> card.getPlayerId().equals(player1Id))
                .max(comparingInt(GameCard::getFieldPosition))
                .orElse(null);
    }

    private GameCard getPlayer2Card(List<GameCard> cardList, Long player1Id) {
        return cardList.stream()
                .filter(card -> !card.getPlayerId().equals(player1Id))
                .min(comparingInt(GameCard::getFieldPosition))
                .orElse(null);
    }

    private List<GameCard> getGameCardsInLocation(Long gameRoomId, CardLocation location) {
        return gameCardRepository.findWithCardByGameRoomAndLocation(gameRoomId, location);
    }
}
