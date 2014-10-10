package gcom.utils;

import java.util.ArrayList;

/**
 * Created by Patrik on 2014-10-09.
 */
public class ViewChange extends Message {

    private ArrayList<Host> members;

    public ViewChange(boolean isReliable, boolean deliverCausally, String text, Host source, VectorClock vectorClock, String groupName, ArrayList<Host> members) {
        super(isReliable, deliverCausally, text, source, vectorClock, groupName);
        this.members = members;

    }

    public ArrayList<Host> getMembers() {
        return members;
    }

    public void setMembers(ArrayList<Host> members) {
        this.members = members;
    }
}
