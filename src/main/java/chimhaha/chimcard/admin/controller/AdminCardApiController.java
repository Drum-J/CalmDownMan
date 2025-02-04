package chimhaha.chimcard.admin.controller;

import chimhaha.chimcard.admin.dto.CardCreateDto;
import chimhaha.chimcard.admin.dto.SeasonCreateDto;
import chimhaha.chimcard.admin.service.AdminCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/card")
@RequiredArgsConstructor
public class AdminCardApiController {

    private final AdminCardService adminCardService;

    @PostMapping
    public void addCard(@RequestBody CardCreateDto dto) {
        adminCardService.saveCard(dto);
    }

    @PostMapping("/season")
    public void addSeason(@RequestBody SeasonCreateDto dto) {
        adminCardService.saveSeason(dto);
    }
}
