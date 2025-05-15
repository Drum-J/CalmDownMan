package chimhaha.chimcard.card.service;

import chimhaha.chimcard.entity.Card;
import chimhaha.chimcard.entity.Grade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Random;

@Slf4j
@Service
public class CardDrawService {

    public Card draw(Map<Grade, List<Card>> map, Random random, Long seasonId) {
        if (seasonId == 1) {
            int randomValue = random.nextInt(100);
            return drawCardSeason1(map, randomValue);
        } else if (seasonId == 2) {
            int randomValue = random.nextInt(10000);
            return drawCardSeason2(map, randomValue);
        }

        return null;
    }

    private Card drawCardSeason1(Map<Grade, List<Card>> map, int randomValue) {
        if (randomValue < 1) { //1% (0)
            return getRandomCardByGrade(map, Grade.SSR);
        } else if (randomValue < 11) { //10% (1 ~ 10)
            return getRandomCardByGrade(map, Grade.SR);
        } else if (randomValue < 45) { // 34% (11 ~ 44)
            return getRandomCardByGrade(map, Grade.R);
        } else { // 55% (45 ~ 99)
            return getRandomCardByGrade(map, Grade.N);
        }
    }

    private Card drawCardSeason2(Map<Grade, List<Card>> map, int randomValue) {
        if (randomValue < 32) { // 0.32%
            return getRandomCardByGrade(map, Grade.SSR);
        } else if (randomValue < 432) { // 4%
            return getRandomCardByGrade(map, Grade.SR);
        } else if (randomValue < 2232) { // 18%
            return getRandomCardByGrade(map, Grade.R);
        } else if (randomValue < 6900) { // 46.68%
            return getRandomCardByGrade(map, Grade.N);
        } else if (randomValue < 9900) { // 30%
            return getRandomCardByGrade(map, Grade.C);
        } else { // 1%
            return getRandomCardByGrade(map, Grade.V);
        }
    }

    private Card getRandomCardByGrade(Map<Grade, List<Card>> map, Grade grade) {
        List<Card> cards = map.get(grade);

        return cards.get(new Random().nextInt(cards.size()));
    }
}
