package chimhaha.chimcard.entity;

import jakarta.persistence.*;
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

    public GameRoom(Account player1, Account player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.status = GameStatus.PLAYING;
        this.currentTurnPlayerId = whoIsFirst();
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
