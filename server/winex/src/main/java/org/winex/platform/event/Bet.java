package org.winex.platform.event;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Bet implements Serializable {
    private String betId;
    private String playerId;
    private String game;
    private BigDecimal stake;
    private BigDecimal odds;
    private Instant timestamp;

    public Bet() {
        this.betId = UUID.randomUUID().toString();
        this.timestamp = Instant.now();
    }

    public Bet(String playerId, String game, BigDecimal stake, BigDecimal odds) {
        this();
        this.playerId = playerId;
        this.game = game;
        this.stake = stake;
        this.odds = odds;
    }

    public String getBetId() {
        return betId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getGame() {
        return game;
    }

    public void setGame(String game) {
        this.game = game;
    }

    public BigDecimal getStake() {
        return stake;
    }

    public void setStake(BigDecimal stake) {
        this.stake = stake;
    }

    public BigDecimal getOdds() {
        return odds;
    }

    public void setOdds(BigDecimal odds) {
        this.odds = odds;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
