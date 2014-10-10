package gcom.utils;

import java.io.IOException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.security.Permission;

/**
 * Created by Patrik on 2014-10-03.
 */
public class RmiServer {

    private Registry registry;
    private InetAddress externalIp;
    private int port;
    private Logger logger = LogManager.getLogger(this.getClass());

    public RmiServer(int portNumber) throws IOException {
        this.port = portNumber;
        logger.error("before security manager");
        if(System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager() {
                public void checkConnect (String host, int port) {/*logger.error("connect");*/}
                public void checkConnect (String host, int port, Object context) {/*logger.error("connect");*/}
                public void checkPermission(Permission permission) {/*logger.error("connect");*/}
                public void checkPermission(Permission permission, Object context) {/*logger.error("connect");*/}
                public void checkAccept(String host, int port) {/*logger.error("connect");*/}
            });
        }
        logger.error("after security manager");
        //System.setProperty("java.rmi.server.hostname", Inet4Address.getLocalHost().getHostAddress());
        System.setProperty("java.rmi.server.hostname", IpChecker.getIp());
        registry = java.rmi.registry.LocateRegistry.createRegistry(portNumber);
        externalIp = InetAddress.getByName(IpChecker.getIp());
        logger.error("constructor done");
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
