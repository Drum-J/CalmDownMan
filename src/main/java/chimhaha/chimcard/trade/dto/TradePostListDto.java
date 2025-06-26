package chimhaha.chimcard.trade.dto;

import chimhaha.chimcard.entity.Grade;
import chimhaha.chimcard.entity.TradeStatus;
import com.querydsl.core.annotations.QueryProjection;

public record TradePostListDto(
        Long tradeId, String title, String content, TradeStatus tradeStatus, Grade grade,
        Long accountId, String username, String nickname, String profileImage,
        Long cardCount) {

    @QueryProjection
    public TradePostListDto {
    }
}
