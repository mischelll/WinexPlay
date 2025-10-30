package org.winex.platform.web;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/stats")
public class StatsController {

    private final HazelcastInstance hazelcastInstance;

    public StatsController(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    @GetMapping("/live")
    public Map<String, Map<String, BigDecimal>> getLiveStats() {
        IMap<String, BigDecimal> statsMap = hazelcastInstance.getMap("liveStats");
        Map<String, Map<String, BigDecimal>> response = new HashMap<>();

        statsMap.forEach((key, value) -> {
            String[] parts = key.split("\\.");
            if (parts.length == 2) {
                String game = parts[0];
                String metric = parts[1];
                response.computeIfAbsent(game, k -> new HashMap<>())
                        .put(metric, value);
            }
        });

        return response;
    }
}
