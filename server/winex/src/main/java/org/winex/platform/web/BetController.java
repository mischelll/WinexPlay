package org.winex.platform.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.winex.platform.event.Bet;
import org.winex.platform.service.BetPublisher;

@RestController
@RequestMapping("/bet")
public class BetController {

    private final BetPublisher publisher;

    public BetController(BetPublisher publisher) {
        this.publisher = publisher;
    }

    @PostMapping
    public String placeBet(@RequestBody Bet bet) {
        if (bet.getGame() == null || bet.getGame().isBlank()) {
            return "Missing 'game' field";
        }
        publisher.publish(bet);
        return "Bet accepted for game: " + bet.getGame() +
                " (Stake: " + bet.getStake() + ", Odds: " + bet.getOdds() + ")";
    }
}