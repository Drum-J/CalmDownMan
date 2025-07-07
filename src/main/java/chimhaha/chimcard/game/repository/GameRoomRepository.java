package chimhaha.chimcard.game.repository;

import chimhaha.chimcard.entity.GameRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRoomRepository extends JpaRepository<GameRoom, Long> {
}
