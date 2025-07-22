package chimhaha.chimcard.game.dto;

import chimhaha.chimcard.entity.GameCard;

public record FieldCardDto(Long gameCardId, String imageUrl,
                           boolean isFront, // true: 앞면, false: 뒷면
                           boolean isMine // true: 내 카드, false: 상대 카드
) {
    public FieldCardDto(GameCard gameCard, boolean isMine) {
        this(gameCard.getId(), gameCard.getCard().getImageUrl(), gameCard.isFront(), isMine);
    }
}
