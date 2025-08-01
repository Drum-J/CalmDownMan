package chimhaha.chimcard.game.intercepter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class StompHeaderInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        // STOMP 연결 요청일 때 세션에 userId 저장
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // 프론트에서 보낸 헤더 값 추출
            String playerId = accessor.getFirstNativeHeader("playerId");
            if (playerId != null && !playerId.isBlank()) {
                Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
                log.info("sessionAttributes: {}", sessionAttributes);

                if (sessionAttributes != null) {
                    sessionAttributes.put("playerId", Long.parseLong(playerId));
                    log.info("게임 매칭 연결 sessionId: {}, playerId: {}", accessor.getSessionId(), playerId);
                }
            }
        }
        return message;
    }
}
