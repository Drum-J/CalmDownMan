package chimhaha.chimcard.game.scheduler;

import chimhaha.chimcard.game.service.GameMatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameScheduler {

    private final GameMatchingService gameMatchingService;
    private final SimpMessageSendingOperations messagingTemplate;

    @Scheduled(fixedRate = 1000)
    public void matchingSchedule() {
        gameMatchingService.successMatching().ifPresent(result -> {
            sendMatchSuccessMessage(result.gameRoomId(), result.player1Id());
            sendMatchSuccessMessage(result.gameRoomId(), result.player2Id());
        });
    }

    private void sendMatchSuccessMessage(Long gameRoomId, Long playerId) {
        messagingTemplate.convertAndSend("/queue/game/matching/success/" + playerId, gameRoomId);
    }
}
