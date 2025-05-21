package chimhaha.chimcard.card.dto;

import chimhaha.chimcard.entity.Grade;
import com.querydsl.core.annotations.QueryProjection;

public record MyCardDetailDto(Long id, String title, String attackType,
                              Grade grade, int power, String cardSeason, int count) {

    @QueryProjection
    public MyCardDetailDto {
    }
}
