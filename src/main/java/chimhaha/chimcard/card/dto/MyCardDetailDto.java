package chimhaha.chimcard.card.dto;

import chimhaha.chimcard.entity.Grade;
import com.querydsl.core.annotations.QueryProjection;

public record MyCardDetailDto(Long id, String title, String attackType,
                              Grade grade, int power, String cardSeason, long count) {

    @QueryProjection
    public MyCardDetailDto {
    }
}
