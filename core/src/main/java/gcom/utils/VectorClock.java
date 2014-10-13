package gcom.utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/** Contains a map with clock values for added hosts. The clocks can be incremented, compared, added etc. If the
 * user tries to increment a clock for a host which isn't in the vector clock, the host is added to the vector clock.
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
        return (message.getVectorClock().getValue(message.getSource()) <= getValue(message.getSource()));
    }


    public Integer getValue(Host host) {
        return (clock.containsKey(host) ? clock.get(host) : 0);
    }

    public void increment(Host host) {
        clock.put(host, getValue(host) + 1);
    }

    public boolean isBefore(VectorClock other, Host host) {
        return clock.get(host) < other.getValue(host);
    }

    /**
     * Checks if the clock values are lower than or equal to the corresponding values of the other vector clock. If
     * one or more isn't false is returned, otherwise true.
     * @param other the other victor clock.
     * @param exception
     * @return true if this statement holds for all clock values, otherwise false.
     */
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

    public boolean isEmpty() {
        return clock.isEmpty();
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

    public void addValue(Host member, int value) {
        clock.put(member, value);
    }

    public void removeValue(Host member) {
        clock.remove(member);
    }
}
