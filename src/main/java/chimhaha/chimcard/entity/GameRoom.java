package chimhaha.chimcard.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class GameRoom extends TimeStamped {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    private Account player1;

    @ManyToOne(fetch = LAZY)
    private Account player2;

    private Long currentTurnPlayerId; // 현재 플레이어 턴

    @Enumerated(EnumType.STRING)
    private GameStatus status; // 게임 상태

    private Long winnerId; // 게임 승자

    public GameRoom(Account player1, Account player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.status = GameStatus.PLAYING;
        this.currentTurnPlayerId = whoIsFirst();
    }

    @Builder
    public GameRoom(Long id, Account player1, Account player2, GameStatus status, Long currentTurnPlayerId, Long winnerId) {
        this.id = id;
        this.player1 = player1;
        this.player2 = player2;
        this.status = status;
        this.currentTurnPlayerId = currentTurnPlayerId;
        this.winnerId = winnerId;
    }

    private Long whoIsFirst() {
        return ThreadLocalRandom.current().nextBoolean() ? player1.getId() : player2.getId();
    }

    public boolean canJoin() {
        return status == GameStatus.WAITING && player2 == null;
    }

    public void finishGame() {
        this.status = GameStatus.FINISHED;
    }

    public boolean isFinished() {
        return status == GameStatus.FINISHED;
    }

    public void gameWinner(Long winnerId) {
        this.winnerId = winnerId;
        finishGame();
    }

    public void changeTurn() {
        currentTurnPlayerId = currentTurnPlayerId.equals(player1.getId()) ?
                player2.getId() : player1.getId();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof GameRoom gameRoom)) return false;
        return Objects.equals(id, gameRoom.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
