package chimhaha.chimcard.trade.service;

import chimhaha.chimcard.trade.dto.TradeCardDetailDto;
import chimhaha.chimcard.trade.dto.TradePostListDto;
import chimhaha.chimcard.trade.dto.TradeRequestListDto;
import chimhaha.chimcard.trade.dto.TradeSearchDto;
import chimhaha.chimcard.trade.repository.TradeCustomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TradeViewService {

    private final TradeCustomRepository tradeCustomRepository;

    public Page<TradePostListDto> getPostList(TradeSearchDto dto) {
        return tradeCustomRepository.getPostList(dto.toPageRequest(), dto.toStatus(), dto.toGrade());
    }

    public List<TradeCardDetailDto> getPostDetail(Long tradePostId) {
        return tradeCustomRepository.getPostDetail(tradePostId);
    }

    public List<TradeRequestListDto> getRequestList(Long tradePostId) {
        return tradeCustomRepository.getRequestList(tradePostId);
    }

    public List<TradeCardDetailDto> getRequestDetail(Long tradeRequestId) {
        return tradeCustomRepository.getRequestDetail(tradeRequestId);
    }
}
