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

    public Group(String name, Host leader) {
        members = new ArrayList<>();
        this.leader = leader;
        this.name = name;
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

    public void setMembers(ArrayList<Host> members) {
        this.members = members;
    }
}
