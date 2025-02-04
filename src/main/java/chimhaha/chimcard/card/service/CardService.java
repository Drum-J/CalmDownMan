package chimhaha.chimcard.card.service;

import chimhaha.chimcard.card.dto.CardUpdateDto;
import chimhaha.chimcard.card.repository.CardRepository;
import chimhaha.chimcard.card.repository.CardSeasonRepository;
import chimhaha.chimcard.entity.Card;
import chimhaha.chimcard.entity.CardSeason;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
                .orElseThrow(() -> new IllegalArgumentException("해당 카드를 찾을 수 없습니다."));
    }

    @Transactional
    public String updateCard(CardUpdateDto dto) {
        Card card = cardRepository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 카드를 찾을 수 없습니다."));

        card.update(dto.getTitle(), dto.getPower(), dto.getAttackType(), dto.getGrade());

        return "카드 업데이트";
    }

    public List<CardSeason> getCardSeasons() {
        return cardSeasonRepository.findAll();
    }

    public List<Card> getCardsBySeason(Long seasonId) {
        CardSeason cardSeason = cardSeasonRepository.findById(seasonId)
                .orElseThrow(() -> new IllegalArgumentException("해당 시즌 카드팩은 존재하지 않습니다."));

        return cardRepository.findByCardSeason(cardSeason);
    }

    public void saveSeason() {

    }
}
