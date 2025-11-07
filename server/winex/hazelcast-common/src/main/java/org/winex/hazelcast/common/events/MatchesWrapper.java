package org.winex.hazelcast.common.events;

import java.io.Serializable;
import java.util.List;

public class MatchesWrapper implements Serializable {
    private List<MatchUpdateEvent> matches;

    public List<MatchUpdateEvent> getMatches() {
        return matches;
    }

    public void setMatches(List<MatchUpdateEvent> matches) {
        this.matches = matches;
    }
}
