package chimhaha.chimcard.trade.controller;

import chimhaha.chimcard.trade.dto.TradePostCreateDto;
import chimhaha.chimcard.trade.dto.TradeRequestCreateDto;
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
    public void tradePost(@RequestBody TradePostCreateDto dto) {
        Long accountId = AccountUtils.getAccountId();
        cardCheck(dto.cardIds());

        tradeService.tradePost(accountId, dto);
    }

    @PostMapping("/request/{id}")
    public void tradeRequest(@PathVariable("id") Long postId, @RequestBody TradeRequestCreateDto dto) {
        Long requesterId = AccountUtils.getAccountId();
        cardCheck(dto.cardIds());

        tradeService.tradeRequest(postId, requesterId, dto);
    }

    private static void cardCheck(List<Long> cards) {
        if (cards == null || cards.isEmpty()) {
            throw new IllegalArgumentException("교환을 원하는 카드가 없습니다. 교환할 카드를 선택해주세요.");
        }
    }
}
