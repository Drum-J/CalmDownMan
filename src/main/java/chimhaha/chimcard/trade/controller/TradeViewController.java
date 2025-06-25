package chimhaha.chimcard.trade.controller;

import chimhaha.chimcard.common.ApiResponse;
import chimhaha.chimcard.trade.dto.TradeCardDetailDto;
import chimhaha.chimcard.trade.dto.TradePostListDto;
import chimhaha.chimcard.trade.dto.TradeRequestListDto;
import chimhaha.chimcard.trade.dto.TradeSearchDto;
import chimhaha.chimcard.trade.service.TradeViewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trade")
public class TradeViewController {

    private final TradeViewService tradeViewService;

    @GetMapping("/list")
    public ApiResponse<PagedModel<TradePostListDto>> getPostList(TradeSearchDto dto) {
        Page<TradePostListDto> postList = tradeViewService.getPostList(dto);

        return ApiResponse.success(new PagedModel<>(postList));
    }

    @GetMapping("/{id}")
    public ApiResponse<List<TradeCardDetailDto>> getPostDetail(@PathVariable("id") Long tradePostId) {
        return ApiResponse.success(tradeViewService.getPostDetail(tradePostId));
    }

    @GetMapping("/request/{id}")
    public ApiResponse<List<TradeRequestListDto>> getRequestList(@PathVariable("id") Long tradePostId) {
        return ApiResponse.success(tradeViewService.getRequestList(tradePostId));
    }

    @GetMapping("/request/detail/{id}")
    public ApiResponse<List<TradeCardDetailDto>> getRequestDetail(@PathVariable("id") Long tradeRequestId) {
        return ApiResponse.success(tradeViewService.getRequestDetail(tradeRequestId));
    }
}
