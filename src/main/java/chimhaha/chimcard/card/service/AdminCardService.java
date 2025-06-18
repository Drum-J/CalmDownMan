package chimhaha.chimcard.card.service;

import chimhaha.chimcard.card.dto.CardCreateDto;
import chimhaha.chimcard.card.dto.SeasonCreateDto;
import chimhaha.chimcard.card.dto.CardUpdateDto;
import chimhaha.chimcard.card.repository.CardRepository;
import chimhaha.chimcard.card.repository.CardSeasonRepository;
import chimhaha.chimcard.entity.Card;
import chimhaha.chimcard.entity.CardSeason;
import chimhaha.chimcard.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static chimhaha.chimcard.common.MessageConstants.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminCardService {

    private final CardRepository cardRepository;
    private final CardSeasonRepository cardSeasonRepository;

    @Transactional
    public void saveCard(CardCreateDto dto) {
        CardSeason cardSeason = cardSeasonRepository.findById(dto.cardSeasonId())
                .orElseThrow(() -> new ResourceNotFoundException(CARD_SEASON_NOT_FOUND));

        Card card = Card.builder()
                .title(dto.title())
                .attackType(dto.attackType())
                .grade(dto.grade())
                .power(dto.power())
                .cardSeason(cardSeason)
                .build();

        cardRepository.save(card);
    }

    @Transactional
    public void updateCard(CardUpdateDto dto) {
        Card card = cardRepository.findById(dto.id())
                .orElseThrow(() -> new ResourceNotFoundException(CARD_NOT_FOUND));

        card.update(dto.title(), dto.power(), dto.attackType(), dto.grade());
    }

    @Transactional
    public void saveSeason(SeasonCreateDto dto) {
        cardSeasonRepository.save(dto.toEntity());
    }
}
