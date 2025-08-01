package chimhaha.chimcard.game.controller;

import chimhaha.chimcard.common.ApiResponse;
import chimhaha.chimcard.exception.ResourceNotFoundException;
import chimhaha.chimcard.game.dto.MatchingRequestDto;
import chimhaha.chimcard.game.service.GameMatchingService;
import chimhaha.chimcard.utils.AccountUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/game/matching")
public class GameMatchingController {

    private final GameMatchingService gameMatchingService;

    @PostMapping("/join")
    public ApiResponse<String> join(@RequestBody MatchingRequestDto dto) {
        Long accountId = AccountUtils.getAccountId();
        if (!dto.playerId().equals(accountId)) {
            throw new IllegalArgumentException("사용자 정보가 일치하지 않습니다.");
        }

        gameMatchingService.joinMatching(dto);
        return ApiResponse.success("매칭 대기열에 등록되었습니다.");
    }

    @DeleteMapping("/cancel")
    public ApiResponse<String> cancel() {
        Long accountId = AccountUtils.getAccountId();
        boolean result = gameMatchingService.cancelMatching(accountId);

        if (result) {
            return ApiResponse.success("매칭 취소가 완료되었습니다.");
        } else {
            throw new ResourceNotFoundException("매칭 취소 데이터를 찾을 수 없습니다.");
        }
    }
}
