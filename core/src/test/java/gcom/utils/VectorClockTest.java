package gcom.utils;

import junit.framework.TestCase;

import java.net.InetAddress;

public class VectorClockTest extends TestCase {

    public void testFromString() throws Exception {
        VectorClock vectorClock = new VectorClock();
        vectorClock.addValue(new Host(InetAddress.getByName("127.0.0.1"), 8000), 5);
        vectorClock.addValue(new Host(InetAddress.getByName("127.0.0.2"), 8200), 2);
        vectorClock.addValue(new Host(InetAddress.getByName("127.0.0.3"), 8500), 8);
        String vectorClockString = vectorClock.toString();
        VectorClock vectorClock2 = VectorClock.fromString(vectorClockString);
        assertEquals(vectorClock, vectorClock2);
    }

    public void testFromString2() throws Exception {
        VectorClock vectorClock = new VectorClock();
        String vectorClockString = vectorClock.toString();
        VectorClock vectorClock2 = VectorClock.fromString(vectorClockString);
        assertEquals(vectorClock, vectorClock2);
    }

    public void testFromString3() throws Exception {
        VectorClock vectorClock = new VectorClock();
        vectorClock.addValue(new Host(InetAddress.getByName("127.0.0.1"), 8000), 5);
        String vectorClockString = vectorClock.toString();
        VectorClock vectorClock2 = VectorClock.fromString(vectorClockString);
        assertEquals(vectorClock, vectorClock2);
    }
}