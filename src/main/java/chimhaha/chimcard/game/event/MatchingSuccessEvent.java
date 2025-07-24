package chimhaha.chimcard.game.event;

public record MatchingSuccessEvent(Long gameRoomId, Long player1Id, Long player2Id) {
}
