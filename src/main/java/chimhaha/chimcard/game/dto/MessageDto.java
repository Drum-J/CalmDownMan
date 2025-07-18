package chimhaha.chimcard.game.dto;

public record MessageDto(String message, Long nextTurnPlayer , Long winnerId) {
    public static MessageDto cardSubmitSuccess(Long nextTurnPlayer, Long winnerId) {
        return new MessageDto("CARD SUBMIT SUCCESS", nextTurnPlayer, winnerId);
    }

    public static MessageDto fieldBattleResult(Long nextTurnPlayer, Long winnerId) {
        return new MessageDto("FIELD BATTLE RESULT", nextTurnPlayer, winnerId);
    }
}
