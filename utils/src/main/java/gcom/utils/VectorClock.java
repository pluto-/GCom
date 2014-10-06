package gcom.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jonas on 2014-10-06.
 */
public class VectorClock {
    Map<Host, Integer> clock;

    public void VectorClock() {
        clock = new HashMap<Host, Integer>();
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
