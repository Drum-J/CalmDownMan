package chimhaha.chimcard.game.service;

import chimhaha.chimcard.entity.Account;
import chimhaha.chimcard.entity.CardLocation;
import chimhaha.chimcard.entity.GameCard;
import chimhaha.chimcard.entity.GameRoom;
import chimhaha.chimcard.exception.ResourceNotFoundException;
import chimhaha.chimcard.game.dto.GameInfoDto;
import chimhaha.chimcard.game.dto.MyGameCardDto;
import chimhaha.chimcard.game.repository.GameCardRepository;
import chimhaha.chimcard.game.repository.GameRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameService {

    private final GameRoomRepository gameRoomRepository;
    private final GameCardRepository gameCardRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 게임 입장 시 상대 닉네임과 내 게임 카드 로딩
     */
    public GameInfoDto gameInfo(Long gameRoomId, Long playerId) {
        GameRoom gameRoom = gameRoomRepository.findWithPlayersById(gameRoomId) // fetchJoin 으로 조회
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 게임입니다."));

        Account player1 = gameRoom.getPlayer1();
        Account player2 = gameRoom.getPlayer2();

        // 상대 닉네임 가져오기
        String otherPlayer = player1.getId().equals(playerId) ? player2.getNickname() : player1.getNickname();

        // 내 카드 가져오기
        List<MyGameCardDto> myCards = gameCardRepository.findWithCardByGameRoomAndPlayerId(gameRoomId, playerId)
                .stream().map(card -> new MyGameCardDto(card.getId(),card.getCard())).toList();

        return new GameInfoDto(otherPlayer, myCards, gameRoom.getCurrentTurnPlayerId());
    }

    @Transactional
    public void playCard(Long gameRoomId, Long playerId, Long gameCardId) {
        GameRoom gameRoom = gameRoomRepository.findWithPlayersById(gameRoomId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 게임입니다."));

        // 1. 유효성 검사
        // 턴 확인
        if (!gameRoom.getCurrentTurnPlayerId().equals(playerId)) {
            throw new IllegalArgumentException("현재 턴이 아닙니다.");
        }

        GameCard gameCard = gameCardRepository.findById(gameCardId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 게임 카드입니다."));

        // 카드 소유자 확인
        if (!gameCard.getPlayerId().equals(playerId)) {
            throw new IllegalArgumentException("자신의 카드가 아닙니다.");
        }

        // 카드가 손패에 있는지 확인
        if (gameCard.getLocation() != CardLocation.HAND) {
            throw new IllegalArgumentException("손패에 있는 카드만 낼 수 있습니다.");
        }

        // 2. 카드 제출 처리
        // 필드에 있는 모든 카드 가져오기
        List<GameCard> fieldCards = gameCardRepository.findByGameRoomAndLocation(gameRoom.getId(), CardLocation.FIELD);

        // 기존 필드 카드 전진 player1은 [1] -> [6] / player2는 [1] <- [6] 로 움직임
        Long player1Id = gameRoom.getPlayer1().getId();
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

        // 3. 전투 발생 확인 및 처리 (TODO: battle 메서드 구현 필요)
        // battle(gameRoom, fieldCards);

        // 4. 턴 넘기기
        gameRoom.changeTurn();

        // 5. WebSocket으로 게임 상태 업데이트 브로드캐스트 (TODO: GameUpdateDto 구현 필요)
        // messagingTemplate.convertAndSend("/topic/game/" + gameRoomId, new GameUpdateDto(...));
    }

    // TODO: GameRoom에 updateCurrentTurnPlayerId 메서드 추가 필요
    // TODO: battle 메서드 구현 필요
}
