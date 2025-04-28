package chimhaha.chimcard.card.controller;

import chimhaha.chimcard.card.dto.CardCreateDto;
import chimhaha.chimcard.card.dto.SeasonCreateDto;
import chimhaha.chimcard.card.service.AdminCardService;
import chimhaha.chimcard.card.dto.CardUpdateDto;
import chimhaha.chimcard.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 관리자) 카드 및 시즌 등록용 Controller
 */

@RestController
@RequestMapping("/api/admin/card")
@RequiredArgsConstructor
public class CardApiController {

    private final AdminCardService adminCardService;

    @PostMapping
    public ApiResponse<CardCreateDto> addCard(@RequestBody CardCreateDto dto) {
        adminCardService.saveCard(dto);

        return ApiResponse.success(dto);
    }

    @PutMapping("/{id}")
    public ApiResponse<CardUpdateDto> updateCard(@RequestBody CardUpdateDto dto) {
        adminCardService.updateCard(dto);

        return ApiResponse.success(dto);
    }

    @PostMapping("/season")
    public ApiResponse<String> addSeason(@RequestBody SeasonCreateDto dto) {
        adminCardService.saveSeason(dto);

        return ApiResponse.success(dto.seasonName() + " 등록 완료!");
    }
}
