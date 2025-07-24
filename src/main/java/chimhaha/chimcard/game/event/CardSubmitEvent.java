package chimhaha.chimcard.game.event;

import chimhaha.chimcard.game.dto.BattleCardDto;
import chimhaha.chimcard.game.dto.FieldCardDto;
import chimhaha.chimcard.game.dto.MyGameCardDto;

import java.util.List;
import java.util.Map;


public record CardSubmitEvent(
        Long gameRoomId, Long currentPlayerId, Long nextPlayerId,
        Map<Integer, FieldCardDto> player1FieldCards,
        Map<Integer, FieldCardDto> player2FieldCards,
        BattleCardDto battleCardDto,
        List<MyGameCardDto> myHandCards) {
}
