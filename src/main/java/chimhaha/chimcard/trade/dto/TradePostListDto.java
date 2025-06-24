package chimhaha.chimcard.trade.dto;

import com.querydsl.core.annotations.QueryProjection;

public record TradePostListDto(
        Long tradeId, String title, String content, String tradeStatus,
        String username, String nickname, String profileImage,
        Long cardCount) {

    @QueryProjection
    public TradePostListDto {
    }
}
