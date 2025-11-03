package org.winex.hazelcast.common.event;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

public class MarketEvent implements Serializable {
    private String matchId;
    private String homeTeam;
    private String awayTeam;
    private Map<String, BigDecimal> odds;
    private String status;

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public String getHomeTeam() {
        return homeTeam;
    }

    public void setHomeTeam(String homeTeam) {
        this.homeTeam = homeTeam;
    }

    public String getAwayTeam() {
        return awayTeam;
    }

    public void setAwayTeam(String awayTeam) {
        this.awayTeam = awayTeam;
    }

    public Map<String, BigDecimal> getOdds() {
        return odds;
    }

    public void setOdds(Map<String, BigDecimal> odds) {
        this.odds = odds;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
