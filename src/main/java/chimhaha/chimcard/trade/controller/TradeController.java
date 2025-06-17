package chimhaha.chimcard.trade.controller;

import chimhaha.chimcard.common.ApiResponse;
import chimhaha.chimcard.trade.dto.TradePostCreateDto;
import chimhaha.chimcard.trade.dto.TradeRequestCreateDto;
import chimhaha.chimcard.trade.dto.TradeStatusRequestDto;
import chimhaha.chimcard.trade.service.TradeService;
import chimhaha.chimcard.utils.AccountUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/trade")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;

    @PostMapping("/post")
    public ApiResponse<String> tradePost(@RequestBody TradePostCreateDto dto) {
        Long accountId = AccountUtils.getAccountId();
        cardCheck(dto.cardIds());

        tradeService.tradePost(accountId, dto);

        return ApiResponse.success("교환글 등록이 완료되었습니다.");
    }

    @PostMapping("/request/{id}")
    public ApiResponse<String> tradeRequest(@PathVariable("id") Long postId, @RequestBody TradeRequestCreateDto dto) {
        Long requesterId = AccountUtils.getAccountId();
        cardCheck(dto.cardIds());

        tradeService.tradeRequest(postId, requesterId, dto);

        return ApiResponse.success("교환 신청이 완료되었습니다.");
    }

    @PostMapping("/complete/{id}")
    public ApiResponse<String> tradeComplete(@PathVariable("id") Long postId, @RequestBody TradeStatusRequestDto dto) {
        Long ownerId = AccountUtils.getAccountId();

        tradeService.tradeComplete(postId, ownerId, dto);

        return ApiResponse.success("해당 사용자와 교환이 완료되었습니다.");
    }

    private static void cardCheck(List<Long> cards) {
        if (cards == null || cards.isEmpty()) {
            throw new IllegalArgumentException("교환을 원하는 카드가 없습니다. 교환할 카드를 선택해주세요.");
        }
    }
}
