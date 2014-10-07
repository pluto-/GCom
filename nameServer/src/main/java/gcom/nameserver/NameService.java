package gcom.nameserver;

import gcom.utils.*;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
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
            leader = newMember;
        } else {
            leader = groups.get(groupName);
        }


        // Use leaders addMember method.
        logger.error("before addmember");
        try {
            NameServiceClient nameServiceClient = (NameServiceClient)Naming.lookup("rmi://" + newMember + "/" + NameServiceClient.class.getSimpleName());
            nameServiceClient.setLeader(groupName, leader);
            sendAddMember(groupName, leader,  newMember);
        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.error("after addmember");

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
