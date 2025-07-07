package chimhaha.chimcard.game.repository;

import chimhaha.chimcard.entity.GameCard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameCardRepository extends JpaRepository<GameCard, Long> {
}
