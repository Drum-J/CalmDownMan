package chimhaha.chimcard.card.repository;

import chimhaha.chimcard.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardRepository extends JpaRepository<Card, Long> {
}
