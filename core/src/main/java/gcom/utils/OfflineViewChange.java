package gcom.utils;

import java.util.ArrayList;

/**
 * Subclass to distinguish offline view changes, for which only the vector clock needs to be incremented, from live
 * view changes, which need to be processed completely.
 */
public class OfflineViewChange extends ViewChange {
    public OfflineViewChange(boolean isReliable, boolean deliverCausally, String text, Host source, VectorClock vectorClock, String groupName, ArrayList<Host> members) {
        super(isReliable, deliverCausally, text, source, vectorClock, groupName, members);
    }
}
