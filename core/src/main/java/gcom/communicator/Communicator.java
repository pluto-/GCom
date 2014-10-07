package gcom.communicator;

import gcom.GCom;
import gcom.utils.RmiServer;
import gcom.utils.Host;
import gcom.utils.Message;
import gcom.utils.PeerCommunication;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
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
    private RmiServer rmiServer;

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
    public void viewChanged(String groupName, ArrayList<Host> members) throws RemoteException {
        gCom.viewChanged(groupName, members);
    }

    @Override
    public void electLeader(String groupName) throws RemoteException {
        //TODO fix
    }

    public void sendViewChange(ArrayList<Host> members, String groupName) throws RemoteException, NotBoundException, MalformedURLException {
        for(Host member : members) {
            System.out.println("Send view changed to " + member.getAddress().getHostAddress());
            PeerCommunication stub = (PeerCommunication) Naming.lookup("rmi://" + member + "/" + PeerCommunication.class.getSimpleName());
            stub.viewChanged(groupName, members);
        }
    }
}
