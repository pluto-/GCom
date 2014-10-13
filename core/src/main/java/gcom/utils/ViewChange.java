package gcom.utils;

import java.util.ArrayList;

/** Extends the Message class. If a view change is sent, this class is used instead of the Message class. It contains
 * the list of members in the group.
 * Created by Patrik on 2014-10-09.
 */
public class ViewChange extends Message {

    private final ArrayList<Host> members;

    public ViewChange(boolean isReliable, boolean deliverCausally, String text, Host source, VectorClock vectorClock, String groupName, ArrayList<Host> members) {
        super(isReliable, deliverCausally, text, source, vectorClock, groupName);
        this.members = members;

    }

    public ArrayList<Host> getMembers() {
        return members;
    }

}
