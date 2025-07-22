package chimhaha.chimcard.game.dto.message;

import chimhaha.chimcard.game.dto.FieldCardDto;
import chimhaha.chimcard.game.dto.MyGameCardDto;

import java.util.List;
import java.util.Map;

public record SubmitMessageDto(Long currentTurnPlayerId,
                               Map<Integer, FieldCardDto> fieldCards,
                               boolean isBattleReady,
                               List<MyGameCardDto> myHandCardIds) {
}
