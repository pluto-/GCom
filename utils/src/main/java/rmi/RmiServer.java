package rmi;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.Permission;

/**
 * Created by Patrik on 2014-10-03.
 */
public class RmiServer {

    Registry registry;

    public RmiServer(int portNumber) throws RemoteException, UnknownHostException {
        if(System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager() {
                public void checkConnect (String host, int port) {}
                public void checkConnect (String host, int port, Object context) {}
                public void checkPermission(Permission permission) {}
                public void checkPermission(Permission permission, Object context) {}
                public void checkAccept(String host, int port) {}
            });
        }
        System.out.println();
        System.setProperty("java.rmi.server.hostname", Inet4Address.getLocalHost().getHostAddress());
        System.out.println(System.getProperty("java.rmi.server.hostname"));
        java.rmi.registry.LocateRegistry.createRegistry(portNumber);
        registry = LocateRegistry.getRegistry();
    }

    public void bind(String name, Remote object) throws RemoteException, AlreadyBoundException {
        registry.rebind(name, object);
    }

}
