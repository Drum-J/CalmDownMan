package chimhaha.chimcard.card.service;

import chimhaha.chimcard.card.repository.CardRepository;
import chimhaha.chimcard.card.repository.CardSeasonRepository;
import chimhaha.chimcard.entity.AttackType;
import chimhaha.chimcard.entity.Card;
import chimhaha.chimcard.entity.CardSeason;
import chimhaha.chimcard.entity.Grade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static chimhaha.chimcard.card.CardTestUtil.createCards;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CardServiceUnitTest {

    @InjectMocks CardService cardService;
    @Mock CardRepository cardRepository;
    @Mock CardSeasonRepository cardSeasonRepository;

    @Test
    @DisplayName("전체 카드 조회")
    void getAllCards() throws Exception {
        //given
        List<Card> cards = createCards(new CardSeason("season1", "imageUrl"));
        given(cardRepository.findAll()).willReturn(cards);

        //when
        List<Card> result = cardService.getAllCards();

        //then
        // 테스트 코드 내에서는 card1, card2 가 계속 사용되기 때문에 assertEquals() 를 사용해도 문제는 없다.
        assertAll(
                () -> assertEquals(cards.size(), result.size()),
                () -> assertEquals(cards.getFirst(), result.getFirst()),
                () -> assertEquals(cards.getLast(), result.getLast()),
                () -> verify(cardRepository).findAll()
        );
    }

    @Test
    @DisplayName("전체 카드 조회 실패 - 등록된 카드가 하나도 없는 경우")
    void getAllCards_empty() throws Exception {
        //given
        given(cardRepository.findAll()).willReturn(List.of());

        //when
        List<Card> result = cardService.getAllCards();

        //then
        // JPA Repository가 제공하는 findAll() 메서드는 데이터가 없을 때 빈 리스트를 반환
        assertAll(
                () -> assertTrue(result.isEmpty()),
                () -> verify(cardRepository).findAll()
        );
    }

    @Test
    @DisplayName("ID 값으로 카드 조회 성공")
    void getCardById() throws Exception {
        //given
        Card card = Card.builder()
                .title("card1")
                .attackType(AttackType.ROCK)
                .grade(Grade.SR)
                .power(13)
                .cardSeason(null)
                .build();

        given(cardRepository.findById(anyLong())).willReturn(Optional.of(card));

        //when
        Card find = cardService.getCardById(1L);

        //then
        assertAll(
                () -> assertEquals(card, find),
                () -> verify(cardRepository).findById(1L)
        );
    }

    @Test
    @DisplayName("ID 값으로 카드 조회 실패")
    void getCardById_NotFound() throws Exception {
        //given
        given(cardRepository.findById(anyLong())).willThrow(new IllegalArgumentException("해당 카드를 찾을 수 없습니다."));

        //when

        //then
        assertThrows(IllegalArgumentException.class, () -> cardService.getCardById(1L));
    }

    @Test
    @DisplayName("카드 시즌 전체 조회 성공")
    void getCardSeason() throws Exception {
        //given
        List<CardSeason> list = List.of(
                new CardSeason("season1", "imageUrl"),
                new CardSeason("season2", "imageUrl")
        );

        given(cardSeasonRepository.findAll()).willReturn(list);

        //when
        List<CardSeason> result = cardService.getCardSeasons();

        //then
        assertAll(
                () -> assertEquals(list.size(), result.size()),
                () -> assertEquals(list.getFirst(), result.getFirst()),
                () -> assertEquals(list.getLast(), result.getLast()),
                () -> verify(cardSeasonRepository).findAll()
        );
    }

    @Test
    @DisplayName("카드 시진 전체 조회 실패 - 등록된 시즌이 하나도 없는 경우")
    void getCardSeason_empty() throws Exception {
        //given
        given(cardSeasonRepository.findAll()).willReturn(List.of());

        //when
        List<CardSeason> result = cardService.getCardSeasons();

        //then
        assertAll(
                () -> assertTrue(result.isEmpty()),
                () -> verify(cardSeasonRepository).findAll()
        );
    }

    @Test
    @DisplayName("시즌 별 카드 조회 성공")
    void getCardsBySeason() throws Exception {
        //given
        CardSeason cardSeason = new CardSeason("season1", "imageUrl");
        List<Card> filter = createCards(cardSeason).stream().filter(
                c -> c.getCardSeason().equals(cardSeason)).toList();

        given(cardSeasonRepository.findById(anyLong())).willReturn(Optional.of(cardSeason));
        given(cardRepository.findByCardSeason(cardSeason)).willReturn(filter);

        //when
        List<Card> result = cardService.getCardsBySeason(anyLong());

        //then
        assertAll(
                () -> assertEquals(filter.size(), result.size()),
                () -> verify(cardSeasonRepository).findById(anyLong()),
                () -> verify(cardRepository).findByCardSeason(cardSeason)
        );
    }

    @Test
    @DisplayName("시즌 별 카드 조회 실패 - 시즌 조회 실패")
    void getCardsBySeason_NotFound() throws Exception {
        //given
        CardSeason cardSeason = new CardSeason("season1", "imageUrl");
        List<Card> filter = createCards(cardSeason).stream().filter(
                c -> c.getCardSeason().equals(cardSeason)).toList();

        given(cardSeasonRepository.findById(anyLong()))
                .willThrow(new IllegalArgumentException("해당 시즌 카드팩은 존재하지 않습니다.")); // cardSeason 조회 실패!

        //when

        //then
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> cardService.getCardsBySeason(anyLong())),
                () -> verify(cardSeasonRepository).findById(anyLong())
        );
    }
}
