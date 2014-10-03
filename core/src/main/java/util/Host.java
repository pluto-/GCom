package util;

import java.net.InetAddress;

/**
 * Created by Jonas on 2014-10-03.
 */
public class Host {

    InetAddress address;
    int port;

    public Host(InetAddress address, int port) {
        this.address = address;
        this.port = port;
    }
}
