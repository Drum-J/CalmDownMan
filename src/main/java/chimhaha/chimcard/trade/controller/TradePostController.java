package chimhaha.chimcard.trade.controller;

import chimhaha.chimcard.trade.dto.TradePostCardRequestDto;
import chimhaha.chimcard.trade.service.TradeService;
import chimhaha.chimcard.utils.AccountUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/trade")
@RequiredArgsConstructor
public class TradePostController {

    private final TradeService tradeService;

    @PostMapping("/post")
    public void tradePost(@RequestBody TradePostCardRequestDto dto) {
        Long accountId = AccountUtils.getAccountId();
        log.info("Trade post card request: {}", dto);
        if (dto.cardIds() == null || dto.cardIds().isEmpty()) {
            throw new IllegalArgumentException("교환을 원하는 카드가 없습니다. 교환할 카드를 선택해주세요.");
        }

        tradeService.tradePost(accountId, dto);
    }
}
