package gcom.utils;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

/** Contains a hosts address and port number.
 * Created by Jonas on 2014-10-03.
 */
public class Host implements Serializable, Comparable {

    private final InetAddress address;
    private final int port;

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

    public static Host fromString(String addressAndPort) throws UnknownHostException {
        String address = addressAndPort.substring(0, addressAndPort.indexOf(':'));
        String port = addressAndPort.substring(addressAndPort.indexOf(':') + 1, addressAndPort.length());

        return new Host(InetAddress.getByName(address), Integer.valueOf(port));
    }

    @Override
    public int hashCode() {
        int result = address != null ? address.hashCode() : 0;
        result = 31 * result + port;
        return result;
    }

    @Override
    public int compareTo(Object o) {
        Host other = (Host)o;
        return (other.toString().compareTo(this.toString())) ;
    }
}
