package transfer;

import java.net.InetAddress;

/**
 * Created by Jonas on 2014-10-03.
 */
public class Host {

    private InetAddress address;
    private int port;

    public Host(InetAddress address, int port) {
        this.address = address;
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public InetAddress getAddress() {
        return address;
    }
}
