package gcom;

import gcom.utils.*;

import java.net.ConnectException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/** All group management methods are located in this class. The interface NameServiceClient is implemented
 * in this class so that members may be added to groups and leaders can be set.
 * Created by Jonas on 2014-10-03.
 */
public class GroupManager implements NameServiceClient {

    private Map<String, Group> groups;
    private Host self;
    private GCom gCom;

    /**
     * Connects to the remote name service.
     * @param self the local host.
     * @param gCom the GCom which has this object.
     * @throws RemoteException
     * @throws NotBoundException
     * @throws MalformedURLException
     */
    public GroupManager (Host self, GCom gCom) throws RemoteException, NotBoundException, MalformedURLException {

        groups = new HashMap<>();
        this.self = self;
        this.gCom = gCom;
    }

    /**
     * Joins a group. Calls the remote method joinGroup of the name service.
     * @param group the group name.
     * @throws RemoteException
     * @throws MalformedURLException
     * @throws NotBoundException
     */
    public void sendJoinGroup(Group group, Host leader, DatabaseHandler databaseHandler) {
        groups.put(group.getName(), group);

        group.setLeader(leader);

        NameServiceClient stub = null;
        try {
            stub = (NameServiceClient) Naming.lookup("rmi://" + leader + "/" + NameServiceClient.class.getSimpleName());
            stub.addMember(group.getName(), self);
        } catch (RemoteException e) {
            group.setLeader(self);
            databaseHandler.setLeader(group.getName(), self);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }

    public Group getGroup(String groupName) {
        return groups.get(groupName);
    }

    public void leaveGroup(String groupName) throws RemoteException {
        Group group = groups.get(groupName);
        group.removeMember(self);
        gCom.sendViewChange(group);
    }

    /**
     * Adds a member to the specified group and sends a viewChange.
     * @param groupName the group.
     * @param member the new member.
     * @throws RemoteException
     */
    public void addMember(String groupName, Host member) throws RemoteException {
        Group group = groups.get(groupName);
        if(!group.getMembers().contains(member)) {
            group.addMember(member);
        } else {
            group.removeVectorEntry(member);
        }
        gCom.sendViewChange(group);
    }

    /**
     * Sets the leader of the group.
     * @param groupName the group.
     * @param leader the leader.
     * @throws RemoteException
     */
    @Override
    public void setLeader(String groupName, Host leader) throws RemoteException {
        Group group = groups.get(groupName);
        group.setLeader(leader);

    }

    /**
     * Processes a view change. If no leader is found in a group, tries to be the leader and send a joinGroup to
     * the name service.
     * @param viewChange the viewChange containing the members.
     * @throws RemoteException
     * @throws NotBoundException
     * @throws MalformedURLException
     */
    public void processViewChange(ViewChange viewChange) throws RemoteException, NotBoundException, MalformedURLException {
        System.out.println("View change vector clock:");
        ArrayList<Host> members = viewChange.getMembers();
        Group group = groups.get(viewChange.getGroupName());
        for(Host member : members) {
            System.out.println(member + " clock value: " + viewChange.getVectorClock().getValue(member));
            groups.get(viewChange.getGroupName()).addVectorValue(member, viewChange.getVectorClock().getValue(member));

        }
        if (!members.contains(group.getLeader())) {
            sendJoinGroup(group);
        }
        groups.get(group.getName()).setMembers(members);
    }

    public ArrayList<Host> getMembers(String groupName) {
        return groups.get(groupName).getMembers();
    }

    public VectorClock getVectorClock(String groupName) {
        return groups.get(groupName).getVectorClock();
    }
}
