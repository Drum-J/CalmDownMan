package chimhaha.chimcard.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class GameCard extends TimeStamped {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    private GameRoom gameRoom;

    private Long playerId;

    @ManyToOne(fetch = LAZY)
    private Card card;

    @Enumerated(EnumType.STRING)
    private CardLocation location = CardLocation.HAND;

    @Builder
    public GameCard(GameRoom gameRoom, Long playerId, Card card) {
        this.gameRoom = gameRoom;
        this.playerId = playerId;
        this.card = card;
    }

    public void toField() {
        location = CardLocation.FIELD;
    }

    public void toGrave() {
        location = CardLocation.GRAVE;
    }
}
