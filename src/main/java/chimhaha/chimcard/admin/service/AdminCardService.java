package chimhaha.chimcard.admin.service;

import chimhaha.chimcard.admin.dto.CardCreateDto;
import chimhaha.chimcard.admin.dto.SeasonCreateDto;
import chimhaha.chimcard.card.repository.CardRepository;
import chimhaha.chimcard.card.repository.CardSeasonRepository;
import chimhaha.chimcard.entity.Card;
import chimhaha.chimcard.entity.CardSeason;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminCardService {

    private final CardRepository cardRepository;
    private final CardSeasonRepository cardSeasonRepository;

    @Transactional
    public void saveCard(CardCreateDto dto) {
        CardSeason cardSeason = cardSeasonRepository.findById(dto.getCardSeasonId())
                .orElseThrow(() -> new IllegalArgumentException("해당 시즌 카드팩은 존재하지 않습니다."));

        Card card = Card.builder()
                .title(dto.getTitle())
                .attackType(dto.getAttackType())
                .grade(dto.getGrade())
                .power(dto.getPower())
                .cardSeason(cardSeason)
                .build();

        cardRepository.save(card);
    }

    @Transactional
    public void saveSeason(SeasonCreateDto dto) {
        cardSeasonRepository.save(dto.toEntity());
    }
}
