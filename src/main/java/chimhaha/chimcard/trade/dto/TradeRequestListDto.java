package chimhaha.chimcard.trade.dto;

import chimhaha.chimcard.entity.TradeStatus;
import com.querydsl.core.annotations.QueryProjection;

public record TradeRequestListDto(
        Long id, Long requesterId,
        String username, String nickname, String profileImage,
        TradeStatus tradeStatus, Long cardCount) {

    @QueryProjection
    public TradeRequestListDto {
    }
}
