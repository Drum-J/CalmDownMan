package chimhaha.chimcard.card.service;

import chimhaha.chimcard.card.repository.CardRepository;
import chimhaha.chimcard.card.repository.CardSeasonRepository;
import chimhaha.chimcard.entity.Card;
import chimhaha.chimcard.entity.CardSeason;
import chimhaha.chimcard.entity.Grade;
import chimhaha.chimcard.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CardService {

    private final CardRepository cardRepository;
    private final CardSeasonRepository cardSeasonRepository;

    public List<Card> getAllCards() {
        return cardRepository.findAll();
    }

    public Card getCardById(Long id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("해당 카드를 찾을 수 없습니다."));
    }

    public List<CardSeason> getCardSeasons() {
        return cardSeasonRepository.findAll();
    }

    public List<Card> getCardsBySeason(Long seasonId) {
        CardSeason cardSeason = cardSeasonRepository.findById(seasonId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 시즌 카드팩은 존재하지 않습니다."));

        return cardRepository.findByCardSeason(cardSeason);
    }

    public List<Card> cardPackOpen(Long seasonId) {
        Map<Grade, List<Card>> map = getCardsBySeason(seasonId)
                .stream().collect(Collectors.groupingBy(Card::getGrade));

        List<Card> drawnCards = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < 3; i++) {
            Card drawnCard = drawCardBySeason(map, random, seasonId);
            drawnCards.add(drawnCard);
        }

        // TODO: 뽑은 카드(drawnCards) -> 회원이 가진 카드로 등록

        return drawnCards;
    }

    private Card drawCardBySeason(Map<Grade, List<Card>> map, Random random, Long seasonId) {
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
