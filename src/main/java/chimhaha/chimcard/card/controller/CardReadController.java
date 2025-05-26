package chimhaha.chimcard.card.controller;

import chimhaha.chimcard.card.dto.CardResponseDto;
import chimhaha.chimcard.card.dto.CardSeasonResponseDto;
import chimhaha.chimcard.card.dto.MyCardDetailDto;
import chimhaha.chimcard.card.service.CardService;
import chimhaha.chimcard.common.ApiResponse;
import chimhaha.chimcard.entity.Card;
import chimhaha.chimcard.user.dto.UserDetailDto;
import chimhaha.chimcard.utils.AccountUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 공용) 카드 및 시즌 조회용 Controller
 */

@Slf4j
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

    @GetMapping("/mine")
    public ApiResponse<List<MyCardDetailDto>> getMyCards(@RequestParam(required = false) Long cardSeasonId) {
        Long id = AccountUtils.getAccountId();

        return ApiResponse.success(cardService.getMyCards(id, cardSeasonId));
    }

    @GetMapping("/notMine")
    public ApiResponse<List<CardResponseDto>> getNotMyCards(@RequestParam(required = false) Long cardSeasonId) {
        Long id = AccountUtils.getAccountId();
        List<CardResponseDto> list = cardService.getNotMyCards(id, cardSeasonId).stream().map(CardResponseDto::new).toList();

        return ApiResponse.success(list);
    }

    @GetMapping("/owner/{id}")
    public ApiResponse<List<UserDetailDto>> getCardOwner(@PathVariable("id") Long id) {
        List<UserDetailDto> list = cardService.getCardOwner(id).stream().map(UserDetailDto::new).toList();

        return ApiResponse.success(list);
    }
}
