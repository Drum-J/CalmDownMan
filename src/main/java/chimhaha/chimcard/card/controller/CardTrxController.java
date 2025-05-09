package chimhaha.chimcard.card.controller;

import chimhaha.chimcard.card.dto.CardResponseDto;
import chimhaha.chimcard.card.service.CardService;
import chimhaha.chimcard.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 공용) 카드 뽑기 및 CUD 관련 Controller
 * 뽑기, 교환 등에 사용 예정
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/card")
public class CardTrxController {

    private final CardService cardService;

    @PostMapping("/open/{seasonId}")
    public ApiResponse<List<CardResponseDto>> cardPackOpen(@PathVariable("seasonId") Long seasonId) {
        List<CardResponseDto> list = cardService.cardPackOpen(seasonId).stream().map(CardResponseDto::new).toList();

        return ApiResponse.success(list);
    }
}
