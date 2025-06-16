package chimhaha.chimcard.card.service;

import chimhaha.chimcard.card.dto.MyCardDetailDto;
import chimhaha.chimcard.card.repository.AccountCardRepository;
import chimhaha.chimcard.card.repository.CardCustomRepository;
import chimhaha.chimcard.card.repository.CardRepository;
import chimhaha.chimcard.card.repository.CardSeasonRepository;
import chimhaha.chimcard.entity.*;
import chimhaha.chimcard.exception.ResourceNotFoundException;
import chimhaha.chimcard.user.repository.AccountRepository;
import chimhaha.chimcard.utils.CardUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CardService {

    private final CardDrawService cardDrawService;
    private final CardRepository cardRepository;
    private final CardSeasonRepository cardSeasonRepository;
    private final CardCustomRepository cardCustomRepository;

    private final AccountRepository accountRepository;
    private final AccountCardRepository accountCardRepository;

    public List<Card> getAllCards() {
        return cardRepository.findAll();
    }

    public Card getCardById(Long cardId) {
        return cardRepository.findById(cardId)
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

    @Transactional
    public List<Card> cardPackOpen(Long accountId, Long seasonId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 회원을 찾을 수 없습니다."));

        Map<Grade, List<Card>> map = getCardsBySeason(seasonId)
                .stream().collect(Collectors.groupingBy(Card::getGrade));

        List<Card> drawnCards = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            drawnCards.add(cardDrawService.draw(map, new Random(), seasonId));
        }

        // 뽑은 카드 - 갯수 Map
        Map<Card, Long> drawnCardMap = CardUtils.cardCountMap(drawnCards);
        upsertList(account, drawnCardMap);

        return drawnCards;
    }

    public void upsertList(Account account, Map<Card, Long> cardMap) {
        List<AccountCard> accountCards = cardCustomRepository.getMyCardByCards(account, cardMap.keySet());
        Map<Card, AccountCard> accountCardMap = CardUtils.accountCardMapCard(accountCards);

        List<AccountCard> upsertCards = new ArrayList<>();
        for (Map.Entry<Card, Long> entry : cardMap.entrySet()) {
            Card card = entry.getKey();
            Long count = entry.getValue();

            AccountCard accountCard = accountCardMap.get(card);
            if (accountCard == null) {
                accountCard = new AccountCard(account, card, count);
            } else {
                accountCard.increaseCount(count);
            }

            upsertCards.add(accountCard);
        }

        accountCardRepository.saveAll(upsertCards);
    }

    public List<MyCardDetailDto> getMyCards(Long accountId, Long cardSeasonId) {
        return cardCustomRepository.getMyCards(accountId,cardSeasonId);
    }

    public List<Card> getNotMyCards(Long accountId, Long cardSeasonId) {
        return cardCustomRepository.getNotMyCards(accountId, cardSeasonId);
    }

    public List<Account> getCardOwner(Long id) {
        return cardCustomRepository.getCardOwner(id);
    }
}
