package chimhaha.chimcard.card;

import chimhaha.chimcard.card.controller.CardViewController;
import chimhaha.chimcard.card.service.CardService;
import chimhaha.chimcard.entity.AttackType;
import chimhaha.chimcard.entity.Card;
import chimhaha.chimcard.entity.CardSeason;
import chimhaha.chimcard.entity.Grade;
import org.hibernate.annotations.NotFound;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
                .andExpect(status().isNotFound())
                //ApiResponse 공통
                .andExpect(jsonPath("status").value(NOT_FOUND.value()))
                .andExpect(jsonPath("message").value(NOT_FOUND.getReasonPhrase()))
                .andExpect(jsonPath("time").exists())
                //e.message()
                .andExpect(jsonPath("data").value(e.getMessage()));

        verify(cardService).getCardById(2L);
    }
}
