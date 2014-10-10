package gcom.communicator;

import gcom.GCom;
import gcom.utils.*;

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
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jonas on 2014-10-03.
 */
public class Communicator implements PeerCommunication {

    private GCom gCom;
    private Host self;
    private Map<Host, CommunicationChannel> channelMap;

    public Communicator(GCom gCom, Host self) throws IOException, AlreadyBoundException {
        this.gCom = gCom;
        this.self = self;
        channelMap = new HashMap<>();
    }

    public void triggerViewChange(Host deadHost, String groupName) {
        channelMap.remove(deadHost);
        gCom.triggerViewChange(deadHost, groupName);
    }

    public void multicast(Message message, ArrayList<Host> groupMembers) {
        for(Host member : groupMembers) {
            if(!member.equals(self)) {
                if (!channelMap.containsKey(member)) {
                    try {
                        channelMap.put(member, new CommunicationChannel(member, this));
                    } catch (RemoteException | NotBoundException e) {
                        triggerViewChange(member, message.getGroupName());
                    }
                }
                try {
                    channelMap.get(member).send(message);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void receiveMessage(Message message) throws RemoteException, NotBoundException {
        if(!gCom.hasReceived(message)) {
            gCom.receive(message);
        }
    }
}