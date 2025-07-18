package chimhaha.chimcard.game.dto;

public record GameResultDto(String message, Long finalWinnerId) {
    public GameResultDto(Long finalWinnerId) {
        this("GAME RESULT", finalWinnerId);
    }

    public static GameResultDto drawGame() {
        return new GameResultDto("DRAW GAME", null);
    }
}
