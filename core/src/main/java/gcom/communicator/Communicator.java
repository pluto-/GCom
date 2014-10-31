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
import java.util.concurrent.ConcurrentHashMap;

/** This class is the communication module. It multicasts messages through Communication channels (see class
 * CommunicationChannel). The interface PeerCommunication is implemented and therefore this class receives messages
 * from other clients as well.
 * Created by Jonas on 2014-10-03.
 */
public class Communicator implements PeerCommunication {

    private final GCom gCom;
    private final Host self;
    private volatile Map<Host, CommunicationChannel> channelMap;
    private int sleepMillisBetweenClients;
    private final Logger logger = LogManager.getLogger(this.getClass());

    /**
     * Initializes the object.
     * @param gCom the GCom creating this object.
     * @param self the local host.
     */
    public Communicator(GCom gCom, Host self) {
        this.gCom = gCom;
        this.self = self;
        channelMap = new ConcurrentHashMap<>();
        sleepMillisBetweenClients = 0;
    }

    /**
     * this is called when a dead remote client has been detected.
     * @param deadHost the dead remote client.
     * @param groupName the group.
     */
    public void triggerViewChange(Host deadHost, String groupName) {
        removeChannel(deadHost);
        gCom.triggerViewChange(deadHost, groupName);
    }

    public void setSleepMillisBetweenClients(int millis) {
        sleepMillisBetweenClients = millis;
    }

    /**
     * Multicasts a message to the whole group (except localhost). Puts the messages in their corresponding
     * CommunicationChannels.
     * @param message the message.
     * @param groupMembers the group members.
     */
    public void multicast(Message message, ArrayList<Host> groupMembers) {
        for(Host member : groupMembers) {
            if(!member.equals(self)) {
                if (!channelMap.containsKey(member)) {
                    try {
                        channelMap.put(member, new CommunicationChannel(member, this));
                    } catch (RemoteException | NotBoundException | MalformedURLException e) {
                        logger.error("Triggering view change, removing " + member);
                        triggerViewChange(member, message.getGroupName());
                    }
                }
                try {
                    System.out.println("ChannelMap " + member.toString());
                    channelMap.get(member).send(message);
                } catch (InterruptedException e) {
                    logger.error("Triggering view change, removing " + member);
                    triggerViewChange(member, message.getGroupName());
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
        gCom.receive(message);
    }

    public void removeChannel(Host host) {
        CommunicationChannel communicationChannel = channelMap.get(host);
        if(communicationChannel != null) {
            communicationChannel.stop();
        }
        channelMap.remove(host);
    }
}