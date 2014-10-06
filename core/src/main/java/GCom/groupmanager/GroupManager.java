package gcom.groupmanager;

import gcom.GCom;
import gcom.utils.Group;
import gcom.utils.NameServiceGroupManagement;
import gcom.utils.Host;
import gcom.utils.PeerCommunication;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jonas on 2014-10-03.
 */
public class GroupManager {

    private Map<String, Group> groups;
    private NameServiceGroupManagement nameService;
    private Host self;
    private GCom gCom;

    public GroupManager (Host nameServiceHost, Host self, GCom gCom) throws RemoteException, NotBoundException, MalformedURLException {
        nameService = (NameServiceGroupManagement) Naming.lookup(nameServiceHost + "/" + NameServiceGroupManagement.class.getName());
        groups = new HashMap<>();
        this.self = self;
        this.gCom = gCom;
    }

    public void joinGroup(String groupName) throws RemoteException {
        Host leader = nameService.joinGroup(groupName, self);
        groups.put(groupName, new Group(groupName, leader));
    }

    public void leaveGroup(String groupName) throws RemoteException, NotBoundException, MalformedURLException {
        Host leader;
        if ((leader = groups.get(groupName).getLeader()) != null) {
            PeerCommunication leaderService = (PeerCommunication) Naming.lookup(leader + "/" + PeerCommunication.class.getName());
        }
    }

    public void addMember(String groupName, Host member) throws RemoteException, NotBoundException {
        groups.get(groupName).addMember(member);
        gCom.sendViewChange(groups.get(groupName));
    }

    public void processViewChange(String groupName, ArrayList<Host> members) {
        if (!members.contains(groups.get(groupName).getLeader())) {
            gCom.sendLeaderElection(groups.get(groupName));
        }
        groups.get(groupName).setMembers(members);
    }

    public ArrayList<Host> getMembers(String groupName) {
        return groups.get(groupName).getMembers();
    }
}
