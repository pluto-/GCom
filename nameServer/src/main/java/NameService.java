import nameserver.NameServerGroupManagement;
import rmi.RmiServer;
import transfer.Host;
import transfer.MessageTransfer;

import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jonas on 2014-10-03.
 */
public class NameService implements NameServerGroupManagement {

    private Map<String, Host> groups;
    private RmiServer rmiServer;

    public NameService(int rmiPort) throws RemoteException, UnknownHostException, AlreadyBoundException {
        groups = new HashMap<String, Host>();
        rmiServer = new RmiServer(rmiPort);

        NameServerGroupManagement stub = (NameServerGroupManagement) UnicastRemoteObject.exportObject(this, 0);
        rmiServer.bind("NameServerGroupManagement", stub);
    }

    @Override
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

    @Override
    public void removeGroup(String groupName) throws RemoteException {
        groups.remove(groupName);
    }

    private void sendAddMember(Host leader, Host newMember) throws RemoteException, NotBoundException {
        Registry leaderRegistry = LocateRegistry.getRegistry(leader.getAddress().getHostAddress(), leader.getPort());

        MessageTransfer stub = (MessageTransfer) leaderRegistry.lookup("MessageTransfer");
        stub.addMember(newMember);
    }
}
