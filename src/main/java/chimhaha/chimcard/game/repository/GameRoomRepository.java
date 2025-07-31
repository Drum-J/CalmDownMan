package chimhaha.chimcard.game.repository;

import chimhaha.chimcard.entity.GameRoom;
import chimhaha.chimcard.entity.GameStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GameRoomRepository extends JpaRepository<GameRoom, Long> {

    @Query("SELECT g FROM GameRoom g JOIN FETCH g.player1 JOIN FETCH g.player2 WHERE g.id = :gameRoomId")
    Optional<GameRoom> findWithPlayersById(@Param("gameRoomId") Long gameRoomId);

    @Query("SELECT g FROM GameRoom g JOIN FETCH g.player1 JOIN FETCH g.player2 WHERE (g.player1.id = :playerId OR g.player2.id = :playerId) AND g.status in (:status)")
    Optional<GameRoom> findByPlayerIdAndStatus(@Param("playerId") Long playerId, @Param("status") List<GameStatus> status);
}
