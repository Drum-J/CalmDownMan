package chimhaha.chimcard.card.dto;

import chimhaha.chimcard.entity.CardSeason;

public record CardSeasonResponseDto(Long id, String title, String imageUrl) {
    public CardSeasonResponseDto(CardSeason cardSeason) {
        this(cardSeason.getId(), cardSeason.getSeasonName(), cardSeason.getImageUrl());
    }
}
