package chimhaha.chimcard.card.controller;

import chimhaha.chimcard.card.dto.CardCreateDto;
import chimhaha.chimcard.card.dto.SeasonCreateDto;
import chimhaha.chimcard.card.service.AdminCardService;
import chimhaha.chimcard.card.dto.CardUpdateDto;
import chimhaha.chimcard.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * 관리자) 카드 및 시즌 등록용 Controller
 */

@RestController
@RequestMapping("/api/admin/card")
@RequiredArgsConstructor
public class CardAdminController {

    private final AdminCardService adminCardService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> addCard(CardCreateDto dto) {
        adminCardService.saveCard(dto);

        return ApiResponse.success(dto.title());
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> updateCard(CardUpdateDto dto) {
        adminCardService.updateCard(dto);

        return ApiResponse.success(dto.title());
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteCard(@PathVariable("id") Long cardId) {
        adminCardService.deleteCard(cardId);

        return ApiResponse.success("card delete success!");
    }

    @PostMapping(path = "/season",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> addSeason(SeasonCreateDto dto) {
        adminCardService.saveSeason(dto);

        return ApiResponse.success(dto.seasonName() + " 등록 완료!");
    }
}
