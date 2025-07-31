package chimhaha.chimcard.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import static chimhaha.chimcard.entity.GameStatus.*;
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

    private String p1SessionId; // Player1 WebSocket Session ID
    private String p2SessionId; // Player2 WebSocket Session ID

    @Version
    private Long version; // 최초 session id 저장 시 동시성 문제 해결

    private int turnCount = 0; // 게임턴이 2일때 카드를 앞면으로 변경, 즉 각 플레이어가 한차례씩 카드를 제출하고 나면 카드를 앞면으로 바꿔야 함.

    public GameRoom(Account player1, Account player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.status = PLAYING;
        this.currentTurnPlayerId = whoIsFirst();
    }

    @Builder
    public GameRoom(Long id, Account player1, Account player2, GameStatus status, Long currentTurnPlayerId, Long winnerId, String p1SessionId, String p2SessionId) {
        this.id = id;
        this.player1 = player1;
        this.player2 = player2;
        this.status = status;
        this.currentTurnPlayerId = currentTurnPlayerId;
        this.winnerId = winnerId;
        this.p1SessionId = p1SessionId;
        this.p2SessionId = p2SessionId;
    }

    private Long whoIsFirst() {
        return ThreadLocalRandom.current().nextBoolean() ? player1.getId() : player2.getId();
    }

    public boolean canJoin() {
        return status == WAITING && player2 == null;
    }

    public void finishGame() {
        this.status = FINISHED;
    }

    public boolean isFinished() {
        return status == FINISHED;
    }

    public void gameWinner(Long winnerId) {
        this.winnerId = winnerId;
        finishGame();
    }

    public void changeTurn() {
        currentTurnPlayerId = currentTurnPlayerId.equals(player1.getId()) ?
                player2.getId() : player1.getId();
    }

    public void increaseTurnCount() {
        turnCount++;
    }

    public void updatePlayer1SessionId(String sessionId) {
        p1SessionId = sessionId;
    }

    public void updatePlayer2SessionId(String sessionId) {
        p2SessionId = sessionId;
    }

    public void disconnected() {
        status = DISCONNECTED;
    }

    public void reconnect() {
        status = PLAYING;
    }

    public boolean isDisconnected() {
        return status == DISCONNECTED;
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
