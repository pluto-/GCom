package gcom.groupmanager;

import gcom.GCom;
import gcom.utils.*;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jonas on 2014-10-03.
 */
public class GroupManager implements NameServiceClient {

    private Map<String, Group> groups;
    private NameServiceGroupManagement nameService;
    private Host self;
    private GCom gCom;

    public GroupManager (Host nameServiceHost, Host self, GCom gCom) throws RemoteException, NotBoundException, MalformedURLException {
        nameService = (NameServiceGroupManagement) Naming.lookup("rmi://" + nameServiceHost + "/" + NameServiceGroupManagement.class.getSimpleName());
        System.out.println("Name Service: " + nameService);

        groups = new HashMap<>();
        this.self = self;
        this.gCom = gCom;
    }
    public void sendJoinGroup(String groupName) throws RemoteException, MalformedURLException, NotBoundException {
        nameService.joinGroup(groupName, self);
    }

    public Group getGroup(String groupName) {
        return groups.get(groupName);
    }

    public void leaveGroup(String groupName) throws RemoteException, NotBoundException, MalformedURLException {
        Group group = groups.get(groupName);
        group.removeMember(self);
        gCom.sendViewChange(group);
    }

    public void addMember(String groupName, Host member) throws RemoteException, NotBoundException, MalformedURLException {
        if(!groups.get(groupName).getMembers().contains(member)) {
            groups.get(groupName).addMember(member);
        }
        gCom.sendViewChange(groups.get(groupName));

    }

    @Override
    public void setLeader(String groupName, Host leader) throws RemoteException {
        Group group = groups.get(groupName);
        if(group == null ) {
            group = new Group(groupName, leader);
        } else {
            group.setLeader(leader);
        }
        groups.put(groupName, group);

    }

    public void processViewChange(ViewChange viewChange) throws RemoteException, NotBoundException, MalformedURLException {
        System.out.println("Members:");
        ArrayList<Host> members = viewChange.getMembers();
        ArrayList<Host> newMembers = new ArrayList<>();
        ArrayList<Host> currentMembers = getMembers(viewChange.getGroupName());
        for(Host member : members) {
            if(!currentMembers.contains(member)) {
                groups.get(viewChange.getGroupName()).addVectorValue(member, viewChange.getVectorClock().getValue(member));
            }
            System.out.println(member.getAddress().getHostAddress());
        }
        String groupName = viewChange.getGroupName();
        if (!members.contains(groups.get(groupName).getLeader())) {
            sendJoinGroup(groupName);
        }
        groups.get(groupName).setMembers(members);
    }

    public ArrayList<Host> getMembers(String groupName) {
        return groups.get(groupName).getMembers();
    }

    public VectorClock getVectorClock(String groupName) {
        return groups.get(groupName).getVectorClock();
    }
}
