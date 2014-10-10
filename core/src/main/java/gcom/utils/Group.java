package gcom.utils;

import gcom.utils.Host;

import java.util.ArrayList;

/**
 * Created by Patrik on 2014-10-06.
 */
public class Group {

    private final String name;
    private Host leader;
    private ArrayList<Host> members;
    private VectorClock vectorClock;

    public Group(String name) {
        this.name = name;
        members = new ArrayList<>();
        vectorClock = new VectorClock();
    }

    public Group(String name, Host leader) {
        this(name);
        this.leader = leader;
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
        members.add(member);
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
        this.vectorClock = vectorClock;
    }

    public void addVectorValue(Host member, int value) {
        vectorClock.addValue(member, value);

    }
}
