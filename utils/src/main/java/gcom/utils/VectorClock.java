package gcom.utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Jonas on 2014-10-06.
 */
public class VectorClock implements Serializable {
    private Map<Host, Integer> clock = new HashMap<>();

    public boolean hasReceived(Host host, Integer value) {
        if(value < clock.get(host)) {
            return false;
        } else {
            return true;
        }
    }

    public boolean hasReceived(Message message) {
        return !(message.getVectorClock().getValue(message.getSender()) < clock.get(message.getSender()));
    }


    public Integer getValue(Host host) {
        return clock.get(host);
    }

    public void increment(Host host) {
        if(!clock.containsKey(host)) {
            clock.put(host, 0);
        }
        clock.put(host, clock.get(host) + 1);
    }

    public boolean isBefore(VectorClock other, Host host) {
        if(clock.get(host) < other.getValue(host)) {
            return true;
        }
        return false;
    }

    public boolean isBeforeOrEqualOnAllValuesExcept(VectorClock other, Host exception) {

        Iterator<Host> keys = clock.keySet().iterator();
        Host key;
        while(keys.hasNext()) {
            key = keys.next();
            if(key != exception) {
                if(clock.get(key) > other.getValue(key)) {
                    return false;
                }
            }
        }
        return true;
    }
}
