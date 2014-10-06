package gcom.nameserver;

import gcom.utils.NameServiceGroupManagement;

import gcom.utils.RmiServer;
import gcom.utils.Host;
import gcom.utils.PeerCommunication;

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
public class NameService implements NameServiceGroupManagement {

    private Map<String, Host> groups;
    private RmiServer rmiServer;

    public NameService(int rmiPort) throws RemoteException, UnknownHostException, AlreadyBoundException {
        groups = new HashMap<>();
        rmiServer = new RmiServer(rmiPort);

        NameServiceGroupManagement stub = (NameServiceGroupManagement) UnicastRemoteObject.exportObject(this, 0);
        rmiServer.bind(NameServiceGroupManagement.class.getName(), stub);
    }

    @Override
    public Host joinGroup(String groupName, Host newMember) throws RemoteException {
        if(!groups.containsKey(groupName)) {
            setLeader(groupName, newMember);
        }
        Host leader = groups.get(groupName);

        // Use leaders addMember method.
        try {
            sendAddMember(groupName, leader, newMember);
        } catch (NotBoundException e) {
            e.printStackTrace();
        }

        return leader;
    }

    @Override
    public void removeGroup(String groupName) throws RemoteException {
        groups.remove(groupName);
    }

    @Override
    public void setLeader(String groupName, Host leader) throws RemoteException {
        groups.put(groupName, leader);
    }

    private void sendAddMember(String groupName, Host leader, Host newMember) throws RemoteException, NotBoundException {
        Registry leaderRegistry = LocateRegistry.getRegistry(leader.getAddress().getHostAddress(), leader.getPort());

        PeerCommunication stub = (PeerCommunication) leaderRegistry.lookup(PeerCommunication.class.getName());
        stub.addMember(groupName, newMember);
    }
}
