package gcom.utils;

import java.util.ArrayList;

/** Information about a group is stored in this class: The members addresses, the leaders address and the group
 * name etc. The local vector clock for the group is located here as well.
 * Created by Patrik on 2014-10-06.
 */
public class Group {

    private final String name;
    private Host leader;
    private volatile ArrayList<Host> members;
    private volatile VectorClock vectorClock;

    public Group(String name, VectorClockListener listener) {
        this.name = name;
        members = new ArrayList<>();
        vectorClock = new VectorClock();
        vectorClock.setVectorClockListener(listener);
    }

    public String getName() {
        return name;
    }

    public Host getLeader() {
        return leader;
    }

    public void setLeader(Host leader) {
        this.leader = leader;
    }

    public ArrayList<Host> getMembers() {
        return members;
    }

    public ArrayList<Host> removeMember(Host member) {
        members.remove(member);
        return members;
    }

    public ArrayList<Host> addMember(Host member) {
        if(!members.contains(member)) {
            members.add(member);
        }
        return members;
    }

    public boolean hasMember(Host member) {
        return members.contains(member);
    }

    public void setMembers(ArrayList<Host> members) {
        this.members = members;
    }

    public VectorClock getVectorClock() {
        return vectorClock;
    }

    public void setVectorClock(VectorClock vectorClock) {
        VectorClockListener listener = this.vectorClock.getVectorClockListener();
        this.vectorClock = vectorClock;
        this.vectorClock.setVectorClockListener(listener);
    }

    public void addVectorValue(Host member, int value) {
        vectorClock.addValue(member, value);
    }

    public void removeVectorEntry(Host member) {
        vectorClock.removeValue(member);
    }
}
