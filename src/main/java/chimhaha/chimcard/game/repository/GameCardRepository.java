package chimhaha.chimcard.game.repository;

import chimhaha.chimcard.entity.CardLocation;
import chimhaha.chimcard.entity.GameCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GameCardRepository extends JpaRepository<GameCard, Long> {

    @Query("SELECT gc FROM GameCard gc JOIN FETCH gc.card WHERE gc.gameRoom.id = :gameRoomId AND gc.playerId =:playerId")
    List<GameCard> findWithCardByGameRoomAndPlayerId(@Param("gameRoomId") Long gameRoomId, @Param("playerId") Long playerId);

    @Query("SELECT gc FROM GameCard gc JOIN FETCH gc.card WHERE gc.gameRoom.id = :gameRoomId AND gc.location =:cardLocation")
    List<GameCard> findWithCardByGameRoomAndLocation(@Param("gameRoomId") Long gameRoomId, @Param("cardLocation") CardLocation cardLocation);

    @Query("SELECT gc FROM GameCard gc JOIN FETCH gc.card JOIN FETCH gc.gameRoom WHERE gc.id = :id")
    Optional<GameCard> findWithCardAndRoomById(@Param("id") Long id);
}
