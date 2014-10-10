package gcom.nameserver;

import gcom.utils.*;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jonas on 2014-10-03.
 */
public class NameService implements NameServiceGroupManagement  {

    private Map<String, Host> groups;
    private RmiServer rmiServer;
    private Logger logger = LogManager.getLogger(this.getClass());

    public NameService(int rmiPort) throws IOException, AlreadyBoundException {
        groups = new HashMap<>();
        rmiServer = new RmiServer(rmiPort);

        NameServiceGroupManagement stub = (NameServiceGroupManagement) UnicastRemoteObject.exportObject(this, rmiPort);
        rmiServer.bind(NameServiceGroupManagement.class.getSimpleName(), stub);

    }

    @Override
    public Host joinGroup(String groupName, Host newMember) throws RemoteException, MalformedURLException, NotBoundException {
        logger.error("joinGroup from " +  newMember + " " + groupName);
        Host leader;
        if(!groups.containsKey(groupName)) {
            setLeader(groupName, newMember);
        }
        leader = groups.get(groupName);


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
            sendAddMember(groupName, leader,  newMember);
        } catch(ConnectException e) {
            if (nameServiceClient != null) {
                nameServiceClient.setLeader(groupName, newMember);
                logger.error("Leader not reachable, new leader - " + newMember);
            }
        }

        logger.error("after addMember");

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

    private void sendAddMember(String groupName, Host leader, Host newMember) throws RemoteException, NotBoundException, MalformedURLException {

        NameServiceClient stub = (NameServiceClient) Naming.lookup("rmi://" + leader + "/" + NameServiceClient.class.getSimpleName());
        stub.addMember(groupName, newMember);
    }
}
