package gcom.communicator;

import GCom.GCom;
import rmi.RmiServer;
import transfer.Host;
import transfer.Message;
import transfer.PeerCommunication;

import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

/**
 * Created by Jonas on 2014-10-03.
 */
public class Communicator implements PeerCommunication {

    GCom gCom;
    RmiServer rmiServer;

    public Communicator(GCom gCom, int rmiPort) throws RemoteException, UnknownHostException, AlreadyBoundException {
        this.gCom = gCom;
        rmiServer = new RmiServer(rmiPort);

        PeerCommunication stub = (PeerCommunication) UnicastRemoteObject.exportObject(this, 0);
        rmiServer.bind(PeerCommunication.class.getName(), stub);
    }

    public void multicast(Message message) throws RemoteException, NotBoundException {
        ArrayList<Host> groupMembers = gCom.getGroupMembers(message.getGroupName());

        for(Host member : groupMembers) {
            Registry memberRegistry = LocateRegistry.getRegistry(member.getAddress().getHostAddress(), member.getPort());

            PeerCommunication stub = (PeerCommunication) memberRegistry.lookup(PeerCommunication.class.getName());
            stub.receiveMessage(message);
        }
    }

    @Override
    public void receiveMessage(Message message) throws RemoteException {
        if(message.isReliable()) {
            // TODO: Send to others if not received before.
        }

        //TODO: Send to message ordering.
    }

    @Override
    public void addMember(Host newMember, String groupName) throws RemoteException, NotBoundException {
        ArrayList<Host> groupMembers = gCom.getGroupMembers(groupName);

        for(Host member : groupMembers) {
            Registry memberRegistry = LocateRegistry.getRegistry(member.getAddress().getHostAddress(), member.getPort());

            PeerCommunication stub = (PeerCommunication) memberRegistry.lookup(PeerCommunication.class.getName());
            stub.viewChanged(newMember, groupName);
        }

        gCom.addMemberInGroup(newMember, groupName);
    }

    @Override
    public void viewChanged(Host newMember, String groupName) throws RemoteException {
        gCom.addMemberInGroup(newMember, groupName);
    }
}
