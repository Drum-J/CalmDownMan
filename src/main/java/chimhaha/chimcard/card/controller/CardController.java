package chimhaha.chimcard.card.controller;

import chimhaha.chimcard.card.dto.CardCreateDto;
import chimhaha.chimcard.card.dto.CardUpdateDto;
import chimhaha.chimcard.card.service.CardService;
import chimhaha.chimcard.entity.Card;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/card")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @GetMapping
    public List<Card> cards() {
        return cardService.getAllCards();
    }

    @GetMapping("/{id}")
    public Card getCard(@PathVariable("id") Long id) {
        return cardService.getCardById(id);
    }
    @PostMapping
    public String saveCard(@RequestBody CardCreateDto dto) {
        cardService.saveCard(dto);

        return "카드 저장";
    }

    @PutMapping("/{id}")
    public String updateCard(@RequestBody CardUpdateDto dto) {
        return cardService.updateCard(dto);
    }
}
