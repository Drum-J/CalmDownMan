package chimhaha.chimcard.card.service;

import chimhaha.chimcard.card.dto.MyCardDetailDto;
import chimhaha.chimcard.card.repository.AccountCardRepository;
import chimhaha.chimcard.card.repository.CardCustomRepository;
import chimhaha.chimcard.card.repository.CardRepository;
import chimhaha.chimcard.card.repository.CardSeasonRepository;
import chimhaha.chimcard.entity.*;
import chimhaha.chimcard.exception.ResourceNotFoundException;
import chimhaha.chimcard.user.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
        List<AccountCard> insertList = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < 3; i++) {
            Card drawnCard = cardDrawService.draw(map, random, seasonId);
            drawnCards.add(drawnCard);

            Optional<AccountCard> findCard = accountCardRepository.findByAccountAndCard(account, drawnCard);
            if (findCard.isPresent()) {
                findCard.get().increaseCount(); // 중복 카드일 경우 갯수 업데이트
            } else {
                insertList.add(new AccountCard(account, drawnCard));
            }
        }

        accountCardRepository.saveAll(insertList); // 뽑은 카드 일괄 저장

        return drawnCards;
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
