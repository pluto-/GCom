package gcom.utils;

import java.io.Serializable;
import java.net.InetAddress;

/**
 * Created by Jonas on 2014-10-03.
 */
public class Host implements Serializable {

    private InetAddress address;
    private int port;

    public Host(InetAddress address, int port) {
        this.address = address;
        this.port = port;
    }

    public Host(Host host) {
        this.address = host.getAddress();
        this.port = host.getPort();
    }

    public int getPort() {
        return port;
    }

    public InetAddress getAddress() {
        return address;
    }

    public String toString() {
        return getAddress().getHostAddress().replaceAll("/", "") + ":" + getPort();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Host host = (Host) o;

        if (port != host.port) return false;
        if (address != null ? !address.equals(host.address) : host.address != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = address != null ? address.hashCode() : 0;
        result = 31 * result + port;
        return result;
    }
}
