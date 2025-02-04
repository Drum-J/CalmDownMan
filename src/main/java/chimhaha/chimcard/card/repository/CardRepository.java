package chimhaha.chimcard.card.repository;

import chimhaha.chimcard.entity.Card;
import chimhaha.chimcard.entity.CardSeason;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long> {

    List<Card> findByCardSeason(CardSeason cardSeason);
}
