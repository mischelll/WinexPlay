package org.winex.betting.service;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.topic.ITopic;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.winex.hazelcast.common.event.Bet;

import java.math.BigDecimal;

@Service
public class BetProcessor {
    private final ITopic<Bet> topic;
    private final IMap<String, BigDecimal> stats;
    private final HazelcastInstance hazelcastInstance;

    public BetProcessor(ITopic<Bet> topic, HazelcastInstance hz, HazelcastInstance hazelcastInstance) {
        this.topic = topic;
        this.stats = hz.getMap("liveStats");
        this.hazelcastInstance = hazelcastInstance;
    }

    @PostConstruct
    public void listen() {
        if (hazelcastInstance.getCluster().getMembers().iterator().next().equals(hazelcastInstance.getCluster().getLocalMember())) {

            topic.addMessageListener(message -> {
                if (hazelcastInstance.getCluster().getLocalMember().isLiteMember()) return;


                Bet bet = message.getMessageObject();
            });

        }
    }
}
