package chimhaha.chimcard.game.service;

import chimhaha.chimcard.entity.Account;
import chimhaha.chimcard.entity.CardLocation;
import chimhaha.chimcard.entity.GameCard;
import chimhaha.chimcard.entity.GameRoom;
import chimhaha.chimcard.exception.ResourceNotFoundException;
import chimhaha.chimcard.game.dto.MessageDto;
import chimhaha.chimcard.game.dto.GameInfoDto;
import chimhaha.chimcard.game.dto.GameResultDto;
import chimhaha.chimcard.game.dto.MyGameCardDto;
import chimhaha.chimcard.game.repository.GameCardRepository;
import chimhaha.chimcard.game.repository.GameRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static chimhaha.chimcard.common.MessageConstants.*;
import static java.util.Comparator.comparingInt;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameService {

    private final GameRoomRepository gameRoomRepository;
    private final GameCardRepository gameCardRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final GameResultAsyncService gameResultAsyncService;

    /**
     * 게임 입장 시 상대 닉네임과 내 게임 카드 로딩
     */
    public GameInfoDto gameInfo(Long gameRoomId, Long playerId) {
        GameRoom gameRoom = gameRoomRepository.findWithPlayersById(gameRoomId)
                .orElseThrow(() -> new ResourceNotFoundException(GAME_ROOM_NOT_FOUND));

        Account player1 = gameRoom.getPlayer1();
        Account player2 = gameRoom.getPlayer2();

        if (!(player1.getId().equals(playerId) || player2.getId().equals(playerId))) {
            throw new IllegalArgumentException(NOT_MY_GAME);
        }

        String otherPlayer = player1.getId().equals(playerId) ? player2.getNickname() : player1.getNickname();

        List<MyGameCardDto> myCards = gameCardRepository.findWithCardByGameRoomAndPlayerId(gameRoomId, playerId)
                .stream().map(card -> new MyGameCardDto(card.getId(),card.getCard())).toList();

        return new GameInfoDto(otherPlayer, myCards, gameRoom.getCurrentTurnPlayerId(), player1.getId(), player2.getId());
    }

    @Transactional
    public void cardSubmit(Long gameRoomId, Long playerId, Long gameCardId) {
        GameRoom gameRoom = getGameRoomAndValidateTurn(gameRoomId, playerId);
        GameCard gameCard = getGameCardAndValidate(playerId, gameCardId);

        processCardMove(gameRoom, gameCard);
        Long winnerId = checkForBattle(gameRoom);

        if (checkGameEnd(gameRoom)) {
            gameResultAsyncService.gameResult(gameRoomId);
            return;
        }

        Long nextTurnPlayerId = gameRoom.changeTurn();
        sendMessage(gameRoomId, MessageDto.cardSubmitSuccess(nextTurnPlayerId, winnerId));
    }

    /**
     * 필드 카드로 직접 배틀 실행
     * 카드의 fieldPosition과 상관없이 가장 앞에 있는 카드로 승부
     */
    @Transactional
    public void fieldBattle(Long gameRoomId, Long playerId) {
        GameRoom gameRoom = getGameRoomAndValidateTurn(gameRoomId, playerId);
        Long player1Id = gameRoom.getPlayer1().getId();

        List<GameCard> fieldCards = gameCardRepository.findWithCardByGameRoomAndLocation(gameRoom.getId(), CardLocation.FIELD);
        GameCard player1Card = getPlayer1Card(fieldCards, player1Id);
        GameCard player2Card = getPlayer2Card(fieldCards, player1Id);

        Long winnerId = null;
        if (player1Card != null && player2Card != null) {
            winnerId = battle(player1Card, player2Card);
        }

        if (winnerId != null && checkGraveCard(gameRoom)) {
            gameResultAsyncService.gameResult(gameRoomId);
            return;
        }

        Long nextTurnPlayerId = gameRoom.changeTurn();

        sendMessage(gameRoomId, MessageDto.fieldBattleResult(nextTurnPlayerId, winnerId));
    }

    /**
     * 카드 제출 및 필드 카드 이동
     */
    private void processCardMove(GameRoom gameRoom, GameCard gameCard) {
        Long playerId = gameCard.getPlayerId();
        Long player1Id = gameRoom.getPlayer1().getId();

        // 필드에 있는 모든 카드 가져오기
        List<GameCard> fieldCards = getGameCardsInLocation(gameRoom.getId(), CardLocation.FIELD);
        // 기존 필드 카드 전진 player1은 [1] -> [6] / player2는 [1] <- [6] 로 움직임
        for (GameCard fieldCard : fieldCards) {
            if (player1Id.equals(playerId)) { // Player1의 카드
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
     * 카드 승부 발생 체크
     */
    private Long checkForBattle(GameRoom gameRoom) {
        List<GameCard> fieldCards = getGameCardsInLocation(gameRoom.getId(), CardLocation.FIELD);

        if (fieldCards.size() == 6) {
            Long player1Id = gameRoom.getPlayer1().getId();
            GameCard player1Card = getPlayer1Card(fieldCards, player1Id);
            GameCard player2Card = getPlayer2Card(fieldCards, player1Id);

            if (player1Card != null && player2Card != null) {
                return battle(player1Card, player2Card);
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
     * 게임 승패 체크
     */
    private boolean checkGameEnd(GameRoom gameRoom) {
        if (checkFieldCondition(gameRoom)) {
            return true;
        }

        return checkGraveCard(gameRoom);
    }

    /**
     * 게임 승패 조건 1. 한 플레이어가 모든 필드를 차지했는가?
     */
    private boolean checkFieldCondition(GameRoom gameRoom) {
        List<GameCard> fieldCards = getGameCardsInLocation(gameRoom.getId(), CardLocation.FIELD);

        if (fieldCards.size() == 6) {
            Long player1Id = gameRoom.getPlayer1().getId();
            long p1count = fieldCards.stream().filter(card -> card.getPlayerId().equals(player1Id)).count();
            if (p1count == 6L) {
                gameRoom.gameWinner(player1Id);
                sendMessage(gameRoom.getId(), new GameResultDto(player1Id));
                return true;
            }

            Long player2Id = gameRoom.getPlayer2().getId();
            long p2count = fieldCards.stream().filter(card -> card.getPlayerId().equals(player2Id)).count();
            if (p2count == 6L) {
                gameRoom.gameWinner(player2Id);
                sendMessage(gameRoom.getId(), new GameResultDto(player2Id));
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
            gameRoom.finishGame(); // 무승부는 게임 상태만 변경 winnerId 는 null로 둔다.
            sendMessage(gameRoom.getId(), GameResultDto.drawGame());
            return true;
        } else if (loserIds.size() == 1) { // 승패 결정
            Long loserId = loserIds.getFirst();
            Long winnerId = gameRoom.getPlayer1().getId().equals(loserId)
                    ? gameRoom.getPlayer2().getId()
                    : gameRoom.getPlayer1().getId();

            gameRoom.gameWinner(winnerId);
            sendMessage(gameRoom.getId(), new GameResultDto(winnerId));
            return true;
        }

        return false;
    }

    private <T> void sendMessage(Long gameRoomId, T message) {
        messagingTemplate.convertAndSend("/topic/game" + gameRoomId, message);
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
