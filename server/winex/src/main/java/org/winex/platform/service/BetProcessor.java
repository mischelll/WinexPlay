package org.winex.platform.service;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.topic.ITopic;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.winex.platform.event.Bet;

import java.math.BigDecimal;

@Service
public class BetProcessor {
    private final ITopic<Bet> topic;
    private final IMap<String, BigDecimal> stats;

    public BetProcessor(ITopic<Bet> topic, HazelcastInstance hz) {
        this.topic = topic;
        this.stats = hz.getMap("liveStats");
    }

    @PostConstruct
    public void listen() {
        topic.addMessageListener(message -> {
            Bet bet = message.getMessageObject();
            String prefix = bet.getGame() + ".";

            stats.lock(prefix + "totalBets");
            try {
                var bets = stats.getOrDefault(prefix + "totalBets", BigDecimal.ZERO);
                stats.put(prefix + "totalBets", bets.add(BigDecimal.ONE));
            } finally {
                stats.unlock(prefix + "totalBets");
            }

            stats.lock(prefix + "totalStake");
            try {
                var stake = stats.getOrDefault(prefix + "totalStake", BigDecimal.ZERO);
                stats.put(prefix + "totalStake", bet.getStake().add(stake));
            } finally {
                stats.unlock(prefix + "totalStake");
            }

            stats.lock(prefix + "expectedPayout");
            try {
                var payout = stats.getOrDefault(prefix + "expectedPayout", BigDecimal.ZERO);
                stats.put(prefix + "expectedPayout", bet.getStake().multiply(bet.getOdds()).add(payout));
            } finally {
                stats.unlock(prefix + "expectedPayout");
            }
            System.out.println("Processed bet: " + bet.getBetId() + " for game " + bet.getGame());
        });

        System.out.println("Bet listener registered on bets-topic!");
    }
}
