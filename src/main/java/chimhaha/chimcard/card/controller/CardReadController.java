package chimhaha.chimcard.card.controller;

import chimhaha.chimcard.card.dto.CardResponseDto;
import chimhaha.chimcard.card.dto.CardSeasonResponseDto;
import chimhaha.chimcard.card.service.CardService;
import chimhaha.chimcard.common.ApiResponse;
import chimhaha.chimcard.entity.Card;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 공용) 카드 및 시즌 조회용 Controller
 */

@RestController
@RequestMapping("/api/card")
@RequiredArgsConstructor
public class CardReadController {

    private final CardService cardService;

    @GetMapping
    public ApiResponse<List<CardResponseDto>> cards() {
        List<CardResponseDto> list =
                cardService.getAllCards().stream().map(CardResponseDto::new).toList();

        return ApiResponse.success(list);
    }

    @GetMapping("/{id}")
    public ApiResponse<CardResponseDto> getCard(@PathVariable("id") Long id) {
        Card card = cardService.getCardById(id);
        return ApiResponse.success(new CardResponseDto(card));
    }

    @GetMapping("/seasons")
    public ApiResponse<List<CardSeasonResponseDto>> getCardSeasons() {
        List<CardSeasonResponseDto> list = cardService.getCardSeasons().stream().map(
                s -> new CardSeasonResponseDto(s.getSeasonName(), s.getImageUrl())).toList();

        return ApiResponse.success(list);
    }

    @GetMapping("/season/{seasonId}")
    public ApiResponse<List<CardResponseDto>> getCardsBySeason(@PathVariable("seasonId") Long seasonId) {
        List<CardResponseDto> list =
                cardService.getCardsBySeason(seasonId).stream().map(CardResponseDto::new).toList();

        return ApiResponse.success(list);
    }
}
