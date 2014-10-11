package gcom.communicator;

import gcom.GCom;
import gcom.utils.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jonas on 2014-10-03.
 */
public class Communicator implements PeerCommunication {

    private final GCom gCom;
    private final Host self;
    private final Map<Host, CommunicationChannel> channelMap;
    private int sleepMillisBetweenClients;
    private final Logger logger = LogManager.getLogger(this.getClass());

    public Communicator(GCom gCom, Host self) {
        this.gCom = gCom;
        this.self = self;
        channelMap = new HashMap<>();
        sleepMillisBetweenClients = 0;
    }

    public void triggerViewChange(Host deadHost, String groupName) {
        removeChannel(deadHost);
        gCom.triggerViewChange(deadHost, groupName);
    }

    public void setSleepMillisBetweenClients(int millis) {
        sleepMillisBetweenClients = millis;
    }

    public void multicast(Message message, ArrayList<Host> groupMembers) {
        for(Host member : groupMembers) {
            if(!member.equals(self)) {
                if (!channelMap.containsKey(member)) {
                    try {
                        channelMap.put(member, new CommunicationChannel(member, this));
                    } catch (RemoteException | NotBoundException | MalformedURLException e) {
                        triggerViewChange(member, message.getGroupName());
                    }
                }
                try {
                    channelMap.get(member).send(message);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if(sleepMillisBetweenClients > 0) {
                try {
                    Thread.sleep(sleepMillisBetweenClients);
                } catch (InterruptedException e) {
                    logger.error("InterruptedException when trying to sleep between clients.");
                }
            }
        }
    }

    @Override
    public synchronized void receiveMessage(Message message) throws RemoteException {
        if(!gCom.hasReceived(message)) {
            gCom.receive(message);
        }
    }

    public void removeChannel(Host host) {
        channelMap.get(host).stop();
        channelMap.remove(host);
    }
}