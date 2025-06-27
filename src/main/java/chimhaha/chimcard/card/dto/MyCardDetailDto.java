package chimhaha.chimcard.card.dto;

import chimhaha.chimcard.entity.Card;
import chimhaha.chimcard.entity.Grade;
import com.querydsl.core.annotations.QueryProjection;

public record MyCardDetailDto(Long id, String title, String attackType,
                              Grade grade, int power,String imageUrl, String cardSeason, long count) {

    @QueryProjection
    public MyCardDetailDto {
    }

    // 테스트 코드 사용을 위해 추가
    public MyCardDetailDto(Card card, long count) {
        this(card.getId(), card.getTitle(), card.getAttackType().name(),
                card.getGrade(), card.getPower(), card.getImageUrl(),
                card.getCardSeason().getSeasonName(), count
        );
    }
}
