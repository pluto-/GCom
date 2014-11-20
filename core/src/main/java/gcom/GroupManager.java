package gcom;

import gcom.utils.*;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
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
    private DatabaseHandler databaseHandler;
    /**
     * Connects to the remote name service.
     * @param self the local host.
     * @param gCom the GCom which has this object.
     * @throws RemoteException
     * @throws NotBoundException
     * @throws MalformedURLException
     */
    public GroupManager (Host self, GCom gCom, DatabaseHandler databaseHandler) throws RemoteException, NotBoundException, MalformedURLException {

        groups = new HashMap<>();
        this.self = self;
        this.gCom = gCom;
        this.databaseHandler = databaseHandler;
    }

    /**
     * Joins a group. Calls the remote method joinGroup of the name service.
     * @param group the group name.
     * @throws RemoteException
     * @throws MalformedURLException
     * @throws NotBoundException
     */
    public void sendJoinGroup(Group group) throws RemoteException, MalformedURLException, NotBoundException, UnknownHostException {
        groups.put(group.getName(), group);
        //TODO kanske skapa vektorklocka från högsta befintliga värdet för varje process i databasen som baseline för att skapa causal consistency för nya processer i fallet då alla gamla processer är disconnectade
        VectorClock clock = databaseHandler.getVectorClock(group.getName(), self);
        group.setVectorClock(clock);
        if(!clock.isEmpty()) {
            gCom.processOfflineMessages(databaseHandler.getNewMessages(group.getName(), clock));
        } else {
            clock = databaseHandler.getCurrentVectorClock(group.getName());
            group.setVectorClock(clock);
        }

        //nameService.joinGroup(group.getName(), self);

        Host leader;
        if((leader = databaseHandler.getLeader(group.getName())) == null) {
            databaseHandler.setLeader(group.getName(), self);
            leader = self;
        }
        setLeader(group.getName(), leader);

        try {
            NameServiceClient stub = (NameServiceClient) Naming.lookup("rmi://" + leader + "/" + NameServiceClient.class.getSimpleName());
            stub.addMember(group.getName(), self);

        } catch(NotBoundException | RemoteException | MalformedURLException | UnknownHostException e) {
            setLeader(group.getName(), self);
            databaseHandler.updateMemberConnected(group.getName(), leader, false);
            databaseHandler.setLeader(group.getName(), self);
            addMember(group.getName(), self);
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
    public void addMember(String groupName, Host member) throws RemoteException, UnknownHostException {
        Group group = groups.get(groupName);
        if(!databaseHandler.hasMember(member, groupName)) {
            databaseHandler.addMember(groupName, member, new VectorClock(), true);
        } else {
            databaseHandler.updateMemberConnected(groupName,  member, true);
        }
        group.setMembers(databaseHandler.getMembers(groupName, true));
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
    public void processViewChange(ViewChange viewChange) throws RemoteException, NotBoundException, MalformedURLException, UnknownHostException {
        if(!(viewChange instanceof OfflineViewChange)) {
            System.out.println("View change vector clock:");
            ArrayList<Host> members = viewChange.getMembers();
            Group group = groups.get(viewChange.getGroupName());
            for (Host member : members) {
                System.out.println(member + " clock value: " + viewChange.getVectorClock().getValue(member));
                if (group.getVectorClock().getValue(member) < viewChange.getVectorClock().getValue(member)) {
                    group.addVectorValue(member, viewChange.getVectorClock().getValue(member));
                    System.err.println("Setting vector value for: " + member + " to: " + viewChange.getVectorClock().getValue(member));
                }
            }
            if (!members.contains(group.getLeader())) {
                sendJoinGroup(group);
            }
            groups.get(group.getName()).setMembers(members);
            Map<Host, Integer> clock = viewChange.getVectorClock().getClock();
            boolean update = false;
            for(Host host : clock.keySet()) {
                if(clock.get(host) < getVectorClock(viewChange.getGroupName()).getValue(host)) {
                    update = true;
                    break;
                }
            }
            if(update) {
                gCom.sendViewChange(group);
            }
        }
    }

    public ArrayList<Host> getMembers(String groupName) {
        return groups.get(groupName).getMembers();
    }

    public VectorClock getVectorClock(String groupName) {
        return groups.get(groupName).getVectorClock();
    }
}