package gcom.utils;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
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

    private Registry registry;
    private InetAddress externalIp;
    private int port;

    public RmiServer(int portNumber) throws IOException {
        this.port = portNumber;
        if(System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager() {
                public void checkConnect (String host, int port) {System.out.println("connect");}
                public void checkConnect (String host, int port, Object context) {}
                public void checkPermission(Permission permission) {}
                public void checkPermission(Permission permission, Object context) {}
                public void checkAccept(String host, int port) {}
            });
        }
        System.out.println();
        System.setProperty("java.rmi.server.hostname", Inet4Address.getLocalHost().getHostAddress());
        registry = java.rmi.registry.LocateRegistry.createRegistry(portNumber);
        System.out.println("constructor done");
        externalIp = InetAddress.getByName(IpChecker.getIp());
    }

    public void bind(String name, Remote object) throws RemoteException, AlreadyBoundException {
        registry.rebind(name, object);
    }

    public Host getHost() throws UnknownHostException {
        return new Host(externalIp, port);
    }

    public Registry getRegistry() {
        return registry;
    }
}
