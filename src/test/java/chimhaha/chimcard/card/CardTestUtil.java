package chimhaha.chimcard.card;

import chimhaha.chimcard.entity.AttackType;
import chimhaha.chimcard.entity.Card;
import chimhaha.chimcard.entity.CardSeason;
import chimhaha.chimcard.entity.Grade;

import java.util.List;

public class CardTestUtil {

    public static List<Card> createCards(CardSeason cardSeason) {
        Card card1 = Card.builder()
                .title("card1")
                .attackType(AttackType.ROCK)
                .grade(Grade.SR)
                .power(13)
                .cardSeason(cardSeason)
                .build();

        Card card2 = Card.builder()
                .title("card2")
                .attackType(AttackType.SCISSORS)
                .grade(Grade.R)
                .power(9)
                .cardSeason(cardSeason)
                .build();

        // 시즌이 다른 카드
        Card card3 = Card.builder()
                .title("card3")
                .attackType(AttackType.ALL)
                .grade(Grade.SSR)
                .power(15)
                .cardSeason(new CardSeason("season2", "imageUrl"))
                .build();

        return List.of(card1, card2, card3);
    }
}
