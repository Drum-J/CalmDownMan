package chimhaha.chimcard.message;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

// 랜덤 포트 사용
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebSocketTest {

    @LocalServerPort
    private int port;

    @Autowired SimpMessagingTemplate messagingTemplate;

    // 비동기로 도착하는 메시지를 기다리기 위한 장치
    private CompletableFuture<SimpleMessageDto> messageFuture;
    private WebSocketStompClient stompClient;

    @BeforeEach
    void setUp() {
        //WebSocketConfig.registerStompEndpoints 에 withSockJS 추가로인해 변경
        List<Transport> transports = new ArrayList<>();
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        WebSocketClient webSocketClient = new SockJsClient(transports);

        stompClient = new WebSocketStompClient(webSocketClient);
        // 서버와 동일하게 Jackson을 사용하여 JSON 메시지를 객체로 변환하도록 설정
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        messageFuture = new CompletableFuture<>();
    }

    @Test
    @DisplayName("메세지 통신 확인")
    void messageTest() throws Exception {
        //given
        //JwtProviderTest 에서 생성해서 가져옴.
        String topic = "/topic/messageTest";
        String connectUrl = String.format("ws://localhost:%d/ws-connection", port); // 접속 주소 ws 프로토콜 사용
        SimpleMessageDto messageDto = new SimpleMessageDto("Tester", "Test Message");

        //when
        // 1. client 와 server를 연결
        StompSession stompSession = stompClient.connectAsync(
                connectUrl, new StompSessionHandlerAdapter() {}
        ).get(5, TimeUnit.SECONDS);

        // 2. client는 "/topic/messageTest" 을 구독하고 전달받은 메세지를 SimpleMessageDto 객체로 역직렬화.
        stompSession.subscribe(topic, new StompFrameHandler() {

            @Override
            public Type getPayloadType(StompHeaders headers) {
                return SimpleMessageDto.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                messageFuture.complete((SimpleMessageDto) payload);
            }
        });

        Thread.sleep(1000); // 구독 완료 후 메세지를 보내기 위해 잠시 대기

        // 3. server에서 메세지를 보냄. 해당 topic 을 구독중인 client는 메세지를 받을 수 있음.
        messagingTemplate.convertAndSend(topic, messageDto);

        //then
        // 4. client가 받은 메세지
        SimpleMessageDto receivedMessage = messageFuture.get(5, TimeUnit.SECONDS);

        assertNotNull(receivedMessage);
        assertEquals(receivedMessage.sender(), messageDto.sender());
        assertEquals(receivedMessage.message(), messageDto.message());
    }
}
