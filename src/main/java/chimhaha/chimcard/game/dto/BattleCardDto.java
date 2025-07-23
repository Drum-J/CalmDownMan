package chimhaha.chimcard.game.dto;

import chimhaha.chimcard.entity.GameCard;

public record BattleCardDto(Long gameCardId1, Long gameCardId2) {
    public BattleCardDto(GameCard gameCard1, GameCard gameCard2) {
        this(gameCard1.getId(), gameCard2.getId());
    }
}
