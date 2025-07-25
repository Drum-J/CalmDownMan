package chimhaha.chimcard.game.dto.message;

import chimhaha.chimcard.game.dto.BattleCardDto;
import chimhaha.chimcard.game.dto.FieldCardDto;
import chimhaha.chimcard.game.dto.MyGameCardDto;
import chimhaha.chimcard.game.event.CardSubmitEvent;

import java.util.List;
import java.util.Map;

public record SubmitMessageDto(Long currentTurnPlayerId,
                               Map<Integer, FieldCardDto> fieldCards,
                               BattleCardDto battleCardDto,
                               List<MyGameCardDto> myHandCards,
                               Long gameWinnerId) {
    // 현재 플레이어 전송
    public SubmitMessageDto(CardSubmitEvent event) {
        this(event.currentPlayerId(), event.currentPlayerFieldCards(), event.battleCardDto(), event.myHandCards(), event.gameWinnerId());
    }

    // 다음 플레이어 전송
    public SubmitMessageDto(CardSubmitEvent event, List<MyGameCardDto> myHandCards) {
        this(event.currentPlayerId(), event.nextPlayerFieldCards(), event.battleCardDto(), myHandCards, event.gameWinnerId());
    }
}
