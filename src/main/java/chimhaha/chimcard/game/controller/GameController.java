package chimhaha.chimcard.game.controller;

import chimhaha.chimcard.common.ApiResponse;
import chimhaha.chimcard.game.dto.CardSubmitRequestDto;
import chimhaha.chimcard.game.dto.GameInfoDto;
import chimhaha.chimcard.game.service.GameService;
import chimhaha.chimcard.utils.AccountUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @GetMapping("/api/game/{gameRoomId}")
    public ApiResponse<GameInfoDto> gameInfo(@PathVariable("gameRoomId") Long gameRoomId) {
        Long playerId = AccountUtils.getAccountId();

        return ApiResponse.success(gameService.gameInfo(gameRoomId, playerId));
    }

    /**
     * 카드 제출 요청을 처리합니다.
     * 클라이언트는 /api/game/{gameRoomId}/play 주소로 메시지를 보냅니다.
     */
    @MessageMapping("/game/{gameRoomId}/play")
    public void cardSubmit(@DestinationVariable Long gameRoomId, CardSubmitRequestDto dto) {
        Long accountId = AccountUtils.getAccountId();
        if (!dto.playerId().equals(accountId)) {
            throw new IllegalArgumentException("사용자 정보가 일치하지 않습니다.");
        }

        gameService.cardSubmit(gameRoomId, dto.playerId(), dto.gameCardId());
    }
}
