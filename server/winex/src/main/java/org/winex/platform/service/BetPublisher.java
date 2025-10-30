package org.winex.platform.service;

import com.hazelcast.topic.ITopic;
import org.springframework.stereotype.Service;
import org.winex.platform.event.Bet;

@Service
public class BetPublisher {

    private final ITopic<Bet> betsTopic;

    public BetPublisher(ITopic<Bet> betsTopic) {
        this.betsTopic = betsTopic;
    }

    public void publish(Bet bet) {
        betsTopic.publish(bet);
        System.out.println("Published bet: " + bet.getBetId() + " (" + bet.getGame() + ")");
    }
}
