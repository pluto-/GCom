package gcom.groupmanager;

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

    Map<String, Group> groups;
    Host nameServiceHost;
    Host self;

    public GroupManager (Host nameServiceHost, Host self) {
        nameServiceHost = nameServiceHost;
        groups = new HashMap<>();
        this.self = self;
    }

    public void joinGroup(String groupName) throws RemoteException, MalformedURLException, NotBoundException {
        NameServiceGroupManagement nameService = (NameServiceGroupManagement) Naming.lookup(nameServiceHost + "/" + NameServiceGroupManagement.class.getName());
        Host leader = nameService.joinGroup(self, groupName);
        groups.put(groupName, new Group(groupName, leader));
    }

    public void leaveGroup(String groupName) throws RemoteException, NotBoundException, MalformedURLException {
        Host leader;
        if ((leader = groups.get(groupName).getLeader()) != null) {
            PeerCommunication leaderService = (PeerCommunication) Naming.lookup(leader + "/" + PeerCommunication.class.getName());
        }
    }

    public void addMember(String groupName, Host member) {
        groups.get(groupName).addMember(member);
    }

    public void processViewChange(String groupName, ArrayList<Host> members) {
        groups.get(groupName).setMembers(members);
    }
}
