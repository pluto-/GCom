package gcom.utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jonas on 2014-10-06.
 */
public class VectorClock implements Serializable {
    Map<Host, Integer> clock;

    public VectorClock() {
        clock = new HashMap<>();
    }

    public boolean hasReceived(Host host, Integer value) {
        if(value < clock.get(host)) {
            return false;
        } else {
            return true;
        }
    }

    public Integer getValue(Host host) {
        return clock.get(host);
    }

    public void increment(Host host) {
        clock.put(host, clock.get(host) + 1);
    }
}
