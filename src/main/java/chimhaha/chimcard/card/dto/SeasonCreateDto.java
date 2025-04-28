package chimhaha.chimcard.card.dto;

import chimhaha.chimcard.entity.CardSeason;

public record SeasonCreateDto(String seasonName, String imageUrl) {

    public CardSeason toEntity() {
        return new CardSeason(seasonName, imageUrl);
    }
}
