package org.winex.hazelcast.common.event;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

public class ScoreEvent implements Serializable {
    private String playerId;
    private BigDecimal points;
    private String game;
    private Instant timestamp = Instant.now();

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public BigDecimal getPoints() {
        return points;
    }

    public void setPoints(BigDecimal points) {
        this.points = points;
    }

    public String getGame() {
        return game;
    }

    public void setGame(String game) {
        this.game = game;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
