package chimhaha.chimcard.trade.dto;

import com.querydsl.core.annotations.QueryProjection;

public record TradeRequestListDto(
        Long id, Long requesterId,
        String username, String nickname, String profileImage,
        String tradeStatus, Long cardCount) {

    @QueryProjection
    public TradeRequestListDto {
    }
}
