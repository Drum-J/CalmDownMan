package chimhaha.chimcard.game.event;

import chimhaha.chimcard.game.dto.message.SubmitMessageDto;
import chimhaha.chimcard.game.service.GameMatchingService;
import chimhaha.chimcard.game.service.GameResultAsyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameEventListener {

    private final GameMatchingService gameMatchingService;
    private final SimpMessageSendingOperations messagingTemplate;
    private final GameResultAsyncService gameResultAsyncService;

    @EventListener(PlayerMatchingJoinEvent.class)
    public void matchingSchedule(PlayerMatchingJoinEvent event) {
        gameMatchingService.successMatching();
    }

    @TransactionalEventListener
    public void sendMatchingSuccessMessage(MatchingSuccessEvent event) {
        sendMatchSuccessMessage(event.gameRoomId(), event.player1Id());
        sendMatchSuccessMessage(event.gameRoomId(), event.player2Id());
    }

    @Async
    @TransactionalEventListener
    public void gameResult(GameEndEvent event) {
        gameResultAsyncService.gameResult(event.gameRoomId());
    }

    @TransactionalEventListener
    public void sendCardSubmittedMessage(CardSubmitEvent event) {
        // 현재 플레이어에게 메세지 전송 (게임룸 ID, 수신자, DTO[다음 턴, 필드 카드, battle 진행할 카드, 내 핸드])
        sendSubmitOrBattleMessage(event.gameRoomId(), event.currentPlayerId(),
                new SubmitMessageDto(event.nextPlayerId(), event.player1FieldCards(), event.battleCardDto(), event.myHandCards()));
        // 다음 플레이어에게 메세지 전송 (게임룸 ID, 수신자, DTO[다음 턴, 필드 카드, battle 진행할 카드, 내 핸드(null)])
        sendSubmitOrBattleMessage(event.gameRoomId(), event.nextPlayerId(),
                new SubmitMessageDto(event.nextPlayerId(), event.player2FieldCards(), event.battleCardDto(), null));
    }

    private void sendMatchSuccessMessage(Long gameRoomId, Long playerId) {
        log.info("매칭 완료 메세지 전송!! gameRoomId: {}, playerId: {}", gameRoomId, playerId);
        messagingTemplate.convertAndSend("/queue/game/matching/success/" + playerId, gameRoomId);
    }

    private <T> void sendSubmitOrBattleMessage(Long gameRoomId, Long playerId, T data) {
        log.info("카드 제출 || 배틀 결과 메세지 전송! gameRoomId: {}, playerId: {}", gameRoomId, playerId);
        String destination = String.format("/queue/game/%s/%s", gameRoomId, playerId);
        messagingTemplate.convertAndSend(destination, data);
    }
}