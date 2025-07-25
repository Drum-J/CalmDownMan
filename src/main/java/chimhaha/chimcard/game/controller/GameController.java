package chimhaha.chimcard.game.controller;

import chimhaha.chimcard.common.ApiResponse;
import chimhaha.chimcard.game.dto.*;
import chimhaha.chimcard.game.service.GameService;
import chimhaha.chimcard.utils.AccountUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/game")
public class GameController {

    private final GameService gameService;

    @GetMapping("/{gameRoomId}")
    public ApiResponse<GameInfoDto> gameInfo(@PathVariable("gameRoomId") Long gameRoomId) {
        Long playerId = AccountUtils.getAccountId();

        return ApiResponse.success(gameService.gameInfo(gameRoomId, playerId));
    }

    @PostMapping("/{gameRoomId}/cardSubmit")
    public ApiResponse<String> cardSubmit(@PathVariable("gameRoomId") Long gameRoomId, @RequestBody CardSubmitRequestDto dto) {
        Long accountId = AccountUtils.getAccountId();
        if (!dto.playerId().equals(accountId)) {
            throw new IllegalArgumentException("사용자 정보가 일치하지 않습니다.");
        }

        gameService.cardSubmit(gameRoomId, dto.playerId(), dto.gameCardId());

        return ApiResponse.success("Submit Success");
    }

    @PostMapping("/{gameRoomId}/battle")
    public void battleStart(@PathVariable("gameRoomId") Long gameRoomId, @RequestBody BattleCardDto dto) {
        gameService.battleStart(gameRoomId, dto);
    }

    @PostMapping("/{gameRoomId}/fieldBattle")
    public void fieldBattle(@PathVariable("gameRoomId") Long gameRoomId,@RequestBody FieldBattleRequestDto dto) {
        gameService.fieldBattle(gameRoomId, dto.playerId());
    }

    @PostMapping("/{gameRoomId}/surrender")
    public void surrender(@PathVariable("gameRoomId") Long gameRoomId,@RequestBody SurrenderDto dto) {
        gameService.surrender(gameRoomId, dto.playerId());
    }

}
