package gcom.utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Jonas on 2014-10-06.
 */
public class VectorClock implements Serializable {
    private final Map<Host, Integer> clock;

    public VectorClock() {
        clock = new HashMap<>();
    }

    public VectorClock(VectorClock objectToClone) {
        clock = new HashMap<>(objectToClone.getClock());
    }

    public boolean hasReceived(Message message) {
        return !(message.getVectorClock().getValue(message.getSource()) < getValue(message.getSource()));
    }


    public Integer getValue(Host host) {
        if(!clock.containsKey(host)) {
            clock.put(host,0);
        }
        return clock.get(host);
    }

    public void increment(Host host) {
        clock.put(host, getValue(host) + 1);
    }

    public boolean isBefore(VectorClock other, Host host) {
        if(clock.get(host) < other.getValue(host)) {
            return true;
        }
        return false;
    }

    public boolean isBeforeOrEqualOnAllValuesExcept(VectorClock other, Host exception) {

        for(Host host : clock.keySet()) {
            if(!host.equals(exception) && (clock.get(host) > other.getValue(host))) {
                return false;
            }
        }
        return true;
    }

    public Map<Host, Integer> getClock() {
        return clock;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VectorClock that = (VectorClock) o;

        if (clock != null ? !clock.equals(that.clock) : that.clock != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return clock != null ? clock.hashCode() : 0;
    }
}
