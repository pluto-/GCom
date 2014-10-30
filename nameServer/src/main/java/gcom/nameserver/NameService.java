package gcom.nameserver;

import gcom.utils.*;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.*;
import java.net.UnknownHostException;
import java.rmi.*;
import java.rmi.ConnectException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;

/**When creating an object of this class, it needs an RMI port number to use when creating the local registry.
 * This local registry will contain the remote method joinGroup
 * which is implemented from the interface NameServiceGroupManagement
 * Created by Jonas on 2014-10-03.
 */
public class NameService implements NameServiceGroupManagement  {

    private final Logger logger = LogManager.getLogger(this.getClass());
    DatabaseHandler databaseHandler;

    /**
     * Creates a local RMI registry on specified port and puts it's own joinGroup in the registry.
     * @param rmiPort the port to use.
     * @throws IOException
     */
    public NameService(int rmiPort) throws IOException {
        RmiServer rmiServer = new RmiServer(rmiPort);

        NameServiceGroupManagement stub = (NameServiceGroupManagement) UnicastRemoteObject.exportObject(this, rmiPort);
        rmiServer.bind(NameServiceGroupManagement.class.getSimpleName(), stub);
        databaseHandler = new DatabaseHandler(rmiServer.getHost().getAddress().getHostAddress(), rmiServer.getHost());
    }

    /**
     * The method which will be called by remote hosts to join a group and get the leader.
     * @param groupName the group to join.
     * @param newMember the remote host which calls the method.
     * @return the leader of the group.
     * @throws RemoteException
     * @throws MalformedURLException
     * @throws NotBoundException
     */
    @Override
    public synchronized Host joinGroup(String groupName, Host newMember) throws RemoteException, MalformedURLException, NotBoundException, java.net.UnknownHostException {
        logger.error("joinGroup from " +  newMember + " for group:" + groupName);
        Host leader;
        if((leader = databaseHandler.getLeader(groupName)) == null) {
            setLeader(groupName, newMember);
            leader = newMember;
        }

        // Use leaders addMember method.
        logger.error("before addmember");
        NameServiceClient nameServiceClient = null;
        try {
            nameServiceClient = (NameServiceClient)Naming.lookup("rmi://" + newMember + "/" + NameServiceClient.class.getSimpleName());
            nameServiceClient.setLeader(groupName, leader);
        } catch (NotBoundException | MalformedURLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            logger.error("sending addMember for: " + newMember + " to: " + leader + " for group: " + groupName);
            sendAddMember(groupName, leader, newMember);
        } catch(NotBoundException | RemoteException | MalformedURLException | UnknownHostException e) {
            if (nameServiceClient != null) {
                nameServiceClient.setLeader(groupName, newMember);
                setLeader(groupName, newMember);
                logger.error("Leader not reachable, new leader - " + newMember);
            }
        }

        logger.error("after addMember");

        return leader;
    }

    private void setLeader(String groupName, Host leader) throws RemoteException {
        databaseHandler.setLeader(groupName, leader);
    }

    /**
     * Sends an add member to the leader.
     * @param groupName the group which has the new member.
     * @param leader the leader.
     * @param newMember the new member.
     * @throws RemoteException
     * @throws NotBoundException
     * @throws MalformedURLException
     */
    private void sendAddMember(String groupName, Host leader, Host newMember) throws RemoteException, NotBoundException, MalformedURLException, UnknownHostException {
        NameServiceClient stub = (NameServiceClient) Naming.lookup("rmi://" + leader + "/" + NameServiceClient.class.getSimpleName());
        stub.addMember(groupName, newMember);
    }
}
