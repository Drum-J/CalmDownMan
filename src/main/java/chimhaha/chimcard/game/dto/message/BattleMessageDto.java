package chimhaha.chimcard.game.dto.message;

import chimhaha.chimcard.game.dto.FieldCardDto;
import chimhaha.chimcard.game.event.BattleEvent;

import java.util.Map;

public record BattleMessageDto(Long currentTurnPlayerId,
                               Map<Integer, FieldCardDto> fieldCards ,
                               Long winnerId,
                               String card1Image,
                               String card2Image,
                               Long gameWinnerId
) {
    public BattleMessageDto(BattleEvent event, Map<Integer, FieldCardDto> fieldCards) {
        this(event.currentTurnPlayerId(), fieldCards, event.winnerId(), event.card1Image(), event.card2Image(), event.gameWinnerId());
    }
}
