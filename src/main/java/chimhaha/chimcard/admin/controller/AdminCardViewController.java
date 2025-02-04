package chimhaha.chimcard.admin.controller;

import chimhaha.chimcard.card.service.CardService;
import chimhaha.chimcard.entity.Card;
import chimhaha.chimcard.entity.CardSeason;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin/card")
@RequiredArgsConstructor
public class AdminCardViewController {

    private final CardService cardService;

    @GetMapping("/seasons")
    public void getCardSeasons(Model model) {
        List<CardSeason> seasons = cardService.getCardSeasons();
        model.addAttribute("seasons", seasons);
    }

    @GetMapping("/season/{seasonId}")
    public void getCardsBySeason(@PathVariable("seasonId") Long seasonId, Model model) {
        List<Card> cards = cardService.getCardsBySeason(seasonId);

        model.addAttribute("seasonId", seasonId);
        model.addAttribute("cards", cards);
    }
}
