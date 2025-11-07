package org.winex.collector.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.topic.ITopic;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.winex.hazelcast.common.events.MatchUpdateEvent;
import org.winex.hazelcast.common.events.MatchesWrapper;

import java.util.List;

import static org.winex.hazelcast.common.constants.HazelcastConstants.TOPIC_LIVE_MATCHES_EVENTS;

@Service
public class MatchCollector {
    private static final Logger logger = LoggerFactory.getLogger(MatchCollector.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ITopic<MatchUpdateEvent> matchesTopic;

    public MatchCollector(HazelcastInstance hazelcastInstance) {
        this.matchesTopic = hazelcastInstance.getTopic(TOPIC_LIVE_MATCHES_EVENTS);
    }

    @Scheduled(fixedRate = 5000)
    public void fetchMatches() {
        HttpGet httpGet = new HttpGet("http://localhost:8010/matches");
        httpGet.setHeader("Content-Type", "application/json");

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse httpResponse = httpClient.execute(httpGet)) {

            String jsonBody = EntityUtils.toString(httpResponse.getEntity());
            MatchesWrapper wrapper = objectMapper.readValue(jsonBody, MatchesWrapper.class);
            List<MatchUpdateEvent> matches = wrapper.getMatches();

            for (MatchUpdateEvent event : matches) {
                matchesTopic.publish(event);
                logger.info("Published: {} vs {} ({})",
                        event.getHomeTeam(), event.getAwayTeam(), event.getMinute());
            }
        } catch (Exception e) {
            logger.error("Failed to fetch matches", e);
        }
    }
}
