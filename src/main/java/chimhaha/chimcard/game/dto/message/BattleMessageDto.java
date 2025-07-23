package chimhaha.chimcard.game.dto.message;

import chimhaha.chimcard.game.dto.FieldCardDto;

import java.util.Map;

public record BattleMessageDto(Long currentTurnPlayerId,
                               Map<Integer, FieldCardDto> fieldCards ,
                               Long winnerId
) {
}
