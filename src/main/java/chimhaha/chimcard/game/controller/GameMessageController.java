package chimhaha.chimcard.game.controller;

import chimhaha.chimcard.game.dto.GameInfoDto;
import chimhaha.chimcard.game.dto.GameInfoRequestDto;
import chimhaha.chimcard.game.service.GameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class GameMessageController {

    private final GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/game/{gameRoomId}/info")
    public void gameInfo(@DestinationVariable("gameRoomId") Long gameRoomId, GameInfoRequestDto dto, SimpMessageHeaderAccessor headerAccessor) {
        log.info("gameInfo request: {}", dto);
        String sessionId = headerAccessor.getSessionId();

        GameInfoDto gameInfoDto = gameService.gameInfo(gameRoomId, dto.playerId(), sessionId);
        String destination = String.format("/queue/game/%s/%s", gameRoomId, dto.playerId());
        Map<String, GameInfoDto> map = new HashMap<>();
        map.put("GAME INFO", gameInfoDto);
        messagingTemplate.convertAndSend(destination, map);
    }

    @MessageExceptionHandler
    public void handleException(Exception e) {
        String destination = "/topic/exception";
        messagingTemplate.convertAndSend(destination, e.getMessage());
    }
}