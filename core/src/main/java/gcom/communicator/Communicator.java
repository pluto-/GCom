package gcom.communicator;

import gcom.GCom;
import gcom.utils.*;

import java.io.IOException;
import java.net.ConnectException;
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
    private Host self;

    public Communicator(GCom gCom, Host self) throws IOException, AlreadyBoundException {
        this.gCom = gCom;
        this.self = self;
    }

    public void multicast(Message message, ArrayList<Host> groupMembers) throws RemoteClientException {
        for(Host member : groupMembers) {
            if(!member.equals(self)) {

                try {
                    Registry memberRegistry = LocateRegistry.getRegistry(member.getAddress().getHostAddress(), member.getPort());
                    PeerCommunication stub = (PeerCommunication) memberRegistry.lookup(PeerCommunication.class.getSimpleName());
                    stub.receiveMessage(message);
                } catch(RemoteException e) {
                    throw new RemoteClientException(groupMembers.indexOf(member));
                } catch(NotBoundException e) {
                    throw new RemoteClientException(groupMembers.indexOf(member));
                }
            }
        }
    }

    @Override
    public void receiveMessage(Message message) throws RemoteException, NotBoundException {
        boolean hasReceived = gCom.hasReceived(message);
        if(!gCom.hasReceived(message)) {
            gCom.receive(message);
        } else {
            gCom.alreadyReceived(message);
        }
    }
}
