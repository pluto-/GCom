package groupmanager;

import nameserver.NameServiceGroupManagement;
import transfer.Host;

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

    Map<String, Host> groupLeaders;
    Map<String, ArrayList<Host>> groupMembers;
    NameServiceGroupManagement nameService;
    Host self;

    public GroupManager (Host nameServiceHost, Host self) throws RemoteException, NotBoundException, MalformedURLException {
        groupLeaders = new HashMap<>();
        nameService = (NameServiceGroupManagement) Naming.lookup(nameServiceHost + "/" + NameServiceGroupManagement.class.getName());
        this.self = self;
    }

    public void joinGroup(String groupName) throws RemoteException {
        Host leader = nameService.joinGroup(self, groupName);
        groupLeaders.put(groupName, leader);
    }

    public void leaveGroup(String groupName) {
        Host leader;
        if ((leader = groupLeaders.get(groupName)) != null) {
            //TODO send view change
        }
    }

    public void processViewChange(String groupName, ArrayList<Host> members) {
        groupMembers.put(groupName, members);
    }
}
