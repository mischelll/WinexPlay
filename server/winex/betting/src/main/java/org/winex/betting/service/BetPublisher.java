package org.winex.betting.service;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.topic.ITopic;
import org.springframework.stereotype.Service;
import org.winex.hazelcast.common.events.Bet;

import static org.winex.hazelcast.common.constants.HazelcastConstants.TOPIC_BET_EVENTS;

@Service
public class BetPublisher {

    private final HazelcastInstance hazelcastInstance;
    private final ITopic<Bet> betsTopic;

    public BetPublisher(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
        this.betsTopic = hazelcastInstance.getTopic(TOPIC_BET_EVENTS);
    }

    public void publish(Bet bet) {
        betsTopic.publish(bet);
        System.out.println("Published bet: " + bet.getBetId() + " (" + bet.getGame() + ")");
    }
}
