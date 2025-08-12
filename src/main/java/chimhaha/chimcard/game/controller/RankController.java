package chimhaha.chimcard.game.controller;

import chimhaha.chimcard.common.ApiResponse;
import chimhaha.chimcard.game.dto.RankResponseDto;
import chimhaha.chimcard.game.service.RankService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rank")
public class RankController {

    private final RankService rankService;

    @GetMapping
    public ApiResponse<List<RankResponseDto>> rank() {
        return ApiResponse.success(rankService.top10Rank());
    }
}
