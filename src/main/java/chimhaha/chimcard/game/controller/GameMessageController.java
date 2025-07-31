package chimhaha.chimcard.game.controller;

import chimhaha.chimcard.game.dto.GameInfoDto;
import chimhaha.chimcard.game.dto.GameInfoRequestDto;
import chimhaha.chimcard.game.dto.message.GameInfoMessageDto;
import chimhaha.chimcard.game.service.GameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

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
        String destination = String.format("/queue/game/%s/%s", gameRoomId, dto.playerId());

        try {
            GameInfoDto gameInfoDto = gameService.gameInfo(gameRoomId, dto.playerId(), sessionId);
            GameInfoMessageDto<GameInfoDto> message = new GameInfoMessageDto<>("GAME INFO", gameInfoDto);
            messagingTemplate.convertAndSend(destination, message);
        } catch (Exception e) {
            log.error("error in gameInfo: {}", e.getMessage());
            GameInfoMessageDto<String> message = new GameInfoMessageDto<>("ERROR", e.getMessage());
            messagingTemplate.convertAndSend(destination, message);
        }
    }
}