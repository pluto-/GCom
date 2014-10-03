import util.Host;
import util.MessageTransfer;
import util.NameServerGroupManagement;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jonas on 2014-10-03.
 */
public class NameService implements NameServerGroupManagement {

    Map<String, Host> groups;

    public NameService(/* port, address etc */) {
        groups = new HashMap<String, Host>();
    }

    public Host joinGroup(Host newMember, String groupName) throws RemoteException {
        if(!groups.containsKey(groupName)) {
            groups.put(groupName, newMember);
        }
        Host leader = groups.get(groupName);

        // Use leaders addMember method.
        try {
            sendAddMember(leader, newMember);
        } catch (NotBoundException e) {
            e.printStackTrace();
        }

        return leader;
    }

    private void sendAddMember(Host leader, Host newMember) throws RemoteException, NotBoundException {
        Registry leaderRegistry = LocateRegistry.getRegistry(leader.getAddress().getHostAddress(), leader.getPort());

        MessageTransfer stub = (MessageTransfer) leaderRegistry.lookup("MessageTransfer");
        stub.addMember(newMember);
    }
}
