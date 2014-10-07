package gcom.communicator;

import gcom.GCom;
import gcom.utils.RmiServer;
import gcom.utils.Host;
import gcom.utils.Message;
import gcom.utils.PeerCommunication;

import java.io.IOException;
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

    private GCom gCom;

    public Communicator(GCom gCom) throws IOException, AlreadyBoundException {
        this.gCom = gCom;

    }

    public void multicast(Message message, ArrayList<Host> groupMembers) throws RemoteException, NotBoundException {
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

        gCom.deliverMessage(message.getText());
    }

    @Override
    public void addMember(String groupName, Host newMember) throws RemoteException, NotBoundException {
        gCom.addMember(groupName, newMember);
    }

    @Override
    public void viewChanged(String groupName, ArrayList<Host> members) throws RemoteException {
        gCom.viewChanged(groupName, members);
    }

    @Override
    public void electLeader(String groupName) {
        //TODO fix
    }

    public void sendViewChange(ArrayList<Host> members, String groupName) throws RemoteException, NotBoundException {
        for(Host member : members) {
            Registry memberRegistry = LocateRegistry.getRegistry(member.getAddress().getHostAddress(), member.getPort());

            PeerCommunication stub = (PeerCommunication) memberRegistry.lookup(PeerCommunication.class.getName());
            stub.viewChanged(groupName, members);
        }
    }
}
