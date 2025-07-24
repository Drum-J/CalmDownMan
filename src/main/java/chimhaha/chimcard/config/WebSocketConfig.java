package chimhaha.chimcard.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 메시지 브로커가 /topic, /queue 로 시작하는 주소를 구독하는 클라이언트에게 메시지를 전달하도록 설정합니다.
        // /topic 은 보통 1:N (pub/sub) 통신에, /queue 는 1:1 통신에 사용됩니다.
        registry.enableSimpleBroker("/topic", "/queue");

        // 클라이언트가 서버로 메시지를 보낼 때 사용할 접두사(prefix)를 설정합니다.
        registry.setApplicationDestinationPrefixes("/api");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 클라이언트가 WebSocket 연결을 시작할 엔드포인트를 설정합니다.
        registry
                .addEndpoint("/ws-connection")
                .setAllowedOrigins("http://localhost:5173") //cors
                .withSockJS();
    }
}
