package chimhaha.chimcard.trade.dto;

import chimhaha.chimcard.entity.Grade;
import com.querydsl.core.annotations.QueryProjection;

public record TradeCardDetailDto(Long cardId, String title, Grade grade, Long count, String imageUrl) {

    @QueryProjection
    public TradeCardDetailDto {
    }
}
