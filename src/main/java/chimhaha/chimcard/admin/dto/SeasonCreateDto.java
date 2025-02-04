package chimhaha.chimcard.admin.dto;

import chimhaha.chimcard.entity.CardSeason;
import lombok.Getter;

@Getter
public class SeasonCreateDto {

    private String seasonName;
    private String imageUrl;

    public CardSeason toEntity() {
        return new CardSeason(seasonName, imageUrl);
    }
}
