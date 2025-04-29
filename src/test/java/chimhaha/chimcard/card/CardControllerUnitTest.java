package chimhaha.chimcard.card;

import chimhaha.chimcard.card.controller.CardViewController;
import chimhaha.chimcard.card.service.CardService;
import chimhaha.chimcard.entity.AttackType;
import chimhaha.chimcard.entity.Card;
import chimhaha.chimcard.entity.CardSeason;
import chimhaha.chimcard.entity.Grade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CardViewController.class)
@AutoConfigureMockMvc
public class CardControllerUnitTest {

    @Autowired private MockMvc mvc;
    @MockitoBean private CardService cardService;
    @MockitoBean private JpaMetamodelMappingContext jpaMetamodelMappingContext; // JpaAuditing 사용 중이라 추가해야 됨

    @Test
    @DisplayName("ID: 1 카드 조회 성공")
    void getCardById() throws Exception {
        //given
        Card card = Card.builder()
                .title("test")
                .grade(Grade.SR)
                .attackType(AttackType.ROCK)
                .power(13)
                .cardSeason(new CardSeason("test","imageUrl"))
                .build();

        given(cardService.getCardById(1L)).willReturn(card);

        //when

        //then
        mvc.perform(get("/api/card/{id}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                //ApiResponse 공통
                .andExpect(jsonPath("status").value(OK.value()))
                .andExpect(jsonPath("message").value(OK.getReasonPhrase()))
                .andExpect(jsonPath("time").exists())
                //T data
                .andExpect(jsonPath("data.title").value(card.getTitle()))
                .andExpect(jsonPath("data.attackType").value(card.getAttackType().getType()))
                .andExpect(jsonPath("data.grade").value(card.getGrade().name()))
                .andExpect(jsonPath("data.power").value(card.getPower()))
                .andExpect(jsonPath("data.cardSeason").value(card.getCardSeason().getSeasonName()));

        verify(cardService).getCardById(1L);
    }

    @Test
    @DisplayName("ID: 2 카드 조회 실패")
    void getCardById_NotFound() throws Exception {
        //given
        IllegalArgumentException e = new IllegalArgumentException("해당 카드를 찾을 수 없습니다.");
        given(cardService.getCardById(2L)).willThrow(e);

        //when

        //then
        mvc.perform(get("/api/card/{id}", 2L))
                .andDo(print())
                .andExpect(status().isNotFound())
                //ApiResponse 공통
                .andExpect(jsonPath("status").value(NOT_FOUND.value()))
                .andExpect(jsonPath("message").value(NOT_FOUND.getReasonPhrase()))
                .andExpect(jsonPath("time").exists())
                //e.message()
                .andExpect(jsonPath("data").value(e.getMessage()));

        verify(cardService).getCardById(2L);
    }

    @Test
    @DisplayName("카드 시즌 전체 조회 성공")
    void getSeasons() throws Exception {
        //given
        List<CardSeason> cardSeasons = List.of(
                new CardSeason("season1", "imageUrl"),
                new CardSeason("season2", "imageUrl")
        );

        given(cardService.getCardSeasons()).willReturn(cardSeasons);
        //when

        //then
        mvc.perform(get("/api/card/seasons"))
                .andDo(print())
                .andExpect(status().isOk())
                //ApiResponse 공통
                .andExpect(jsonPath("status").value(OK.value()))
                .andExpect(jsonPath("message").value(OK.getReasonPhrase()))
                .andExpect(jsonPath("time").exists())
                //List<> data
                .andExpect(jsonPath("data[0].title").value(cardSeasons.getFirst().getSeasonName()))
                .andExpect(jsonPath("data[0].imageUrl").value(cardSeasons.getFirst().getImageUrl()))
                .andExpect(jsonPath("data[1].title").value(cardSeasons.get(1).getSeasonName()))
                .andExpect(jsonPath("data[1].imageUrl").value(cardSeasons.get(1).getImageUrl()));

        verify(cardService).getCardSeasons();
    }
    
    @Test
    @DisplayName("시즌 별 카드 조회")
    void getCardsBySeason() throws Exception {
        //given
        CardSeason cardSeason = new CardSeason("season1", "imageUrl");
        List<Card> cards = makeCardList(cardSeason);

        // Equals And HashCode 미구현으로 인한 seasonName 비교
        // ID값으로 Equals/HashCode 구현하면 되지만 ID값의 생성을 DB에 위임.
        List<Card> findBySeason = cards.stream().filter(
                        card -> card.getCardSeason().getSeasonName().equals(cardSeason.getSeasonName())
                ).toList();
        //이 코드도 테스트 코드 내에서는 통과되지만 실제 운영 상황에서는 실패할 수 있음.
        //card -> card.getCardSeason().equals(cardSeason)).toList();

        given(cardService.getCardsBySeason(1L)).willReturn(findBySeason);

        //when

        //then
        mvc.perform(get("/api/card/season/{id}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                //ApiResponse 공통
                .andExpect(jsonPath("status").value(OK.value()))
                .andExpect(jsonPath("message").value(OK.getReasonPhrase()))
                .andExpect(jsonPath("time").exists())
                //List<Card> data
                .andExpect(jsonPath("data.length()").value(findBySeason.size()))
                .andExpect(jsonPath("data[0].title").value(findBySeason.getFirst().getTitle()))
                .andExpect(jsonPath("data[0].attackType").value(findBySeason.getFirst().getAttackType().getType()))
                .andExpect(jsonPath("data[0].grade").value(findBySeason.getFirst().getGrade().name()))
                .andExpect(jsonPath("data[0].power").value(findBySeason.getFirst().getPower()))
                .andExpect(jsonPath("data[0].cardSeason").value(cardSeason.getSeasonName()));
    }

    @Test
    @DisplayName("시즌 별 카드 조회 실패")
    void getCardsBySeason_NotFound() throws Exception {
        //given
        IllegalArgumentException e = new IllegalArgumentException("해당 시즌 카드팩은 존재하지 않습니다.");
        given(cardService.getCardsBySeason(2L)).willThrow(e);
        //when

        //then
        mvc.perform(get("/api/card/season/{id}", 2L))
                .andDo(print())
                .andExpect(status().isNotFound())
                // ApiResponse 공통
                .andExpect(jsonPath("status").value(NOT_FOUND.value()))
                .andExpect(jsonPath("message").value(NOT_FOUND.getReasonPhrase()))
                .andExpect(jsonPath("time").exists())
                // T data
                .andExpect(jsonPath("data").value(e.getMessage()));
    }

    @Test
    @DisplayName("등록된 카드가 없어도 에러는 발생하지 않음")
    void cards_empty() throws Exception {
        //given
        given(cardService.getAllCards()).willReturn(List.of());

        //when
        mvc.perform(get("/api/card"))
                .andDo(print());
        // CardResponseDto::new 가 실행될 때 NPE가 발생할 거라 생각했지만
        // 여기서 stream().map(CardResponseDto::new) 코드는 실행되지 않는다. 빈 list 이기 때문
        // map()이 실행되지 않아서 그대로 빈 리스트가 반환된다 -> NPE가 발생하지 않음.

        //then
        verify(cardService).getAllCards();
    }

    private static List<Card> makeCardList(CardSeason cardSeason) {
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
