package chimhaha.chimcard.game.event;

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

    private void sendMatchSuccessMessage(Long gameRoomId, Long playerId) {
        log.info("매칭 완료 메세지 전송!! gameRoomId: {}, playerId: {}", gameRoomId, playerId);
        messagingTemplate.convertAndSend("/queue/game/matching/success/" + playerId, gameRoomId);
    }
}
