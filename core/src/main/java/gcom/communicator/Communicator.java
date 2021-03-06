package gcom.communicator;

import gcom.GCom;
import gcom.utils.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    public synchronized void triggerViewChange(Host deadHost, String groupName) throws UnknownHostException {
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
    public synchronized void multicast(Message message, ArrayList<Host> groupMembers) throws UnknownHostException {

        ArrayList<Host> deadHosts = new ArrayList<>();

        for (Host member : groupMembers) {
            if (!member.equals(self)) {
                if (!channelMap.containsKey(member)) {
                    try {
                        channelMap.put(member, new CommunicationChannel(member, this));
                    } catch (RemoteException | NotBoundException | MalformedURLException e) {
                        logger.error("Triggering view change, removing " + member);
                        //triggerViewChange(member, message.getGroupName());
                        deadHosts.add(member);
                    }
                }
                try {
                    System.out.println("ChannelMap " + member.toString());
                    CommunicationChannel communicationChannel;
                    if ((communicationChannel = channelMap.get(member)) != null) {
                        communicationChannel.send(message);
                    }
                } catch (InterruptedException e) {
                    logger.error("Triggering view change, removing " + member);
                    //triggerViewChange(member, message.getGroupName());
                    deadHosts.add(member);
                }
            }
            if (sleepMillisBetweenClients > 0) {
                try {
                    Thread.sleep(sleepMillisBetweenClients);
                } catch (InterruptedException e) {
                    logger.error("InterruptedException when trying to sleep between clients.");
                }
            }
        }

        for(Host deadHost : deadHosts) {
            triggerViewChange(deadHost, message.getGroupName());
        }

    }

    @Override
    public synchronized void receiveMessage(Message message) throws RemoteException, UnknownHostException {
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