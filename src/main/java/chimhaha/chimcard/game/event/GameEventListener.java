package chimhaha.chimcard.game.event;

import chimhaha.chimcard.entity.GameStatus;
import chimhaha.chimcard.game.dto.message.BattleMessageDto;
import chimhaha.chimcard.game.dto.message.ConnectMessageDto;
import chimhaha.chimcard.game.dto.message.SubmitMessageDto;
import chimhaha.chimcard.game.dto.message.SurrenderMessageDto;
import chimhaha.chimcard.game.repository.GameRoomRepository;
import chimhaha.chimcard.game.service.GameMatchingService;
import chimhaha.chimcard.game.service.GameResultAsyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import static chimhaha.chimcard.common.MessageConstants.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameEventListener {

    private final GameMatchingService gameMatchingService;
    private final SimpMessageSendingOperations messagingTemplate;
    private final GameResultAsyncService gameResultAsyncService;
    private final GameRoomRepository gameRoomRepository;
    private final ApplicationEventPublisher eventPublisher;

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
        sendMessage(event.gameRoomId(), event.currentPlayerId(), new SubmitMessageDto(event));
        // 다음 플레이어에게 메세지 전송 (게임룸 ID, 수신자, DTO[다음 턴, 필드 카드, battle 진행할 카드, 내 핸드(null)])
        sendMessage(event.gameRoomId(), event.nextPlayerId(), new SubmitMessageDto(event, null));
    }

    @TransactionalEventListener
    public void sendBattleResultMessage(BattleEvent event) {
        // 배틀 결과 메세지 전송 (게임룸 ID, 수신자, DTO[현재 턴, 필드 카드, gameWinnerId])
        sendMessage(event.gameRoomId(), event.player1Id(), new BattleMessageDto(event, event.player1FieldCards()));
        sendMessage(event.gameRoomId(), event.player2Id(), new BattleMessageDto(event, event.player2FieldCards()));
    }

    @TransactionalEventListener
    public void sendSurenderMessage(SurrenderEvent event) {
        sendMessage(event.gameRoomId(), event.player1Id(), new SurrenderMessageDto(event.gameWinnerId()));
        sendMessage(event.gameRoomId(), event.player2Id(), new SurrenderMessageDto(event.gameWinnerId()));
    }

    @EventListener(SessionDisconnectEvent.class)
    @Transactional
    public void sessionDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();

        log.info("disconnect Session Id {}",sessionId);
        gameRoomRepository.findWithPlayersBySessionId(sessionId).ifPresent(gameRoom -> {
            if (gameRoom.getStatus().equals(GameStatus.FINISHED)) {
                log.info("gameRoom is finished. skip disconnect event.");
                return;
            }

            if (gameRoom.getStatus().equals(GameStatus.DISCONNECTED)) {
                log.info("all players disconnected. game end");
                gameRoom.gameWinner(0L); // 양측 모두 연결 해제 시 무승부 처리
                eventPublisher.publishEvent(new GameEndEvent(gameRoom.getId()));
                return;
            }

            gameRoom.disconnected();

            Long gameRoomId = gameRoom.getId();
            Long otherPlayerId = null;

            // player1의 연결 끊김
            if (gameRoom.getP1SessionId().equals(sessionId)) {
                otherPlayerId = gameRoom.getPlayer2().getId();
            } else if (gameRoom.getP2SessionId().equals(sessionId)) { // player2의 연결 끊김
                otherPlayerId = gameRoom.getPlayer1().getId();
            }

            if (otherPlayerId != null) {
                String destination = String.format("/queue/game/%s/%s", gameRoomId, otherPlayerId);
                messagingTemplate.convertAndSend(destination, new ConnectMessageDto(DISCONNECT));
            }
        });
    }

    @TransactionalEventListener
    public void reconnection(ReconnectEvent event) {
        String destination = String.format("/queue/game/%s/%s", event.gameRoomId(), event.playerId());
        messagingTemplate.convertAndSend(destination, new ConnectMessageDto(RECONNECT));
    }

    @TransactionalEventListener
    public void timeout(TimeoutEvent event) {
        sendMessage(event.gameRoomId(), event.playerId(), new SurrenderMessageDto(event.gameWinnerId()));
    }

    private void sendMatchSuccessMessage(Long gameRoomId, Long playerId) {
        messagingTemplate.convertAndSend("/queue/game/matching/success/" + playerId, gameRoomId);
    }

    private <T> void sendMessage(Long gameRoomId, Long playerId, T data) {
        String destination = String.format("/queue/game/%s/%s", gameRoomId, playerId);
        messagingTemplate.convertAndSend(destination, data);
    }
}