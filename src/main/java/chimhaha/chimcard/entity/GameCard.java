package chimhaha.chimcard.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static jakarta.persistence.FetchType.*;
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

    @ManyToOne(fetch = LAZY)
    private Account player;

    @ManyToOne(fetch = LAZY)
    private Card card;

    @Enumerated(EnumType.STRING)
    private CardLocation location = CardLocation.DECK;

    @Setter
    private Integer cardOrder; // 덱에서의 순서

    @Builder
    public GameCard(GameRoom gameRoom, Account player, Card card) {
        this.gameRoom = gameRoom;
        this.player = player;
        this.card = card;
    }

    public void toHand() {
        location = CardLocation.HAND;
    }

    public void toField() {
        location = CardLocation.FIELD;
    }

    public void toGrave() {
        location = CardLocation.GRAVE;
    }
}
