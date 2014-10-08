package gcom.messagesorter;

import gcom.utils.Host;
import gcom.utils.Message;
import gcom.utils.VectorClock;
import junit.framework.TestCase;

import java.net.InetAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MessageSorterTest extends TestCase {

    public void testReceive_ordered() throws Exception {
        System.err.println("\n\nALREADY ORDERED TEST");
        System.err.println("------------------------------------------------");
        BlockingQueue<Message> deliverQueue = new LinkedBlockingQueue<Message>();
        VectorClock localVectorClock = new VectorClock();

        Host localHost = new Host(InetAddress.getByName("localhost"), 2000);
        MessageSorter messageSorter = new MessageSorter(deliverQueue,  localVectorClock, localHost);

        Host someone = new Host(InetAddress.getByName("1.1.1.1"), 3000);
        VectorClock someoneVectorClock = new VectorClock();
        someoneVectorClock.increment(someone);
        Message message1 = new Message(false, true, "Hej1", someone, someoneVectorClock, null);
        someoneVectorClock.increment(someone);
        Message message2 = new Message(false, true, "Hej2", someone, someoneVectorClock, null);
        someoneVectorClock.increment(someone);
        Message message3 = new Message(false, true, "Hej3", someone, someoneVectorClock, null);

        System.err.println("Sending message1 to MessageSorter.");
        messageSorter.receive(message1);
        System.err.println("Sending message2 to MessageSorter.");
        messageSorter.receive(message2);
        System.err.println("Sending message3 to MessageSorter.");
        messageSorter.receive(message3);

        System.err.println("Taking...");
        System.err.println("First message delivered from MessageSorter: " + deliverQueue.take().getText());
        System.err.println("Taking...");
        System.err.println("Second message delivered from MessageSorter: " + deliverQueue.take().getText());
        System.err.println("Taking...");
        System.err.println("Third message delivered from MessageSorter: " + deliverQueue.take().getText());
        System.err.println("------------------------------------------------");
    }

    public void testReceive_inverseOrdered() throws Exception {
        System.err.println("\n\nINVERSE ORDER TEST");
        System.err.println("------------------------------------------------");
        BlockingQueue<Message> deliverQueue = new LinkedBlockingQueue<Message>();
        VectorClock localVectorClock = new VectorClock();

        Host localHost = new Host(InetAddress.getByName("localhost"), 2000);
        MessageSorter messageSorter = new MessageSorter(deliverQueue,  localVectorClock, localHost);

        Host someone = new Host(InetAddress.getByName("1.1.1.1"), 3000);
        VectorClock someoneVectorClock = new VectorClock();
        someoneVectorClock.increment(someone);
        Message message1 = new Message(false, true, "Hej1", someone, someoneVectorClock, null);
        someoneVectorClock.increment(someone);
        Message message2 = new Message(false, true, "Hej2", someone, someoneVectorClock, null);
        someoneVectorClock.increment(someone);
        Message message3 = new Message(false, true, "Hej3", someone, someoneVectorClock, null);

        System.err.println("Sending message3 to MessageSorter.");
        messageSorter.receive(message3);
        System.err.println("Sending message2 to MessageSorter.");
        messageSorter.receive(message2);
        System.err.println("Sending message1 to MessageSorter.");
        messageSorter.receive(message1);

        System.err.println("Taking...");
        System.err.println("First message delivered from MessageSorter: " + deliverQueue.take().getText());
        System.err.println("Taking...");
        System.err.println("Second message delivered from MessageSorter: " + deliverQueue.take().getText());
        System.err.println("Taking...");
        System.err.println("Third message delivered from MessageSorter: " + deliverQueue.take().getText());
        System.err.println("------------------------------------------------");
    }

    public void testReceive_delayed() throws Exception {
        System.err.println("\n\n3RD MESSAGE DELAYED TEST");
        System.err.println("------------------------------------------------");
        BlockingQueue<Message> deliverQueue = new LinkedBlockingQueue<Message>();
        VectorClock localVectorClock = new VectorClock();

        Host localHost = new Host(InetAddress.getByName("localhost"), 2000);
        MessageSorter messageSorter = new MessageSorter(deliverQueue,  localVectorClock, localHost);

        Host someone = new Host(InetAddress.getByName("1.1.1.1"), 3000);
        VectorClock someoneVectorClock = new VectorClock();
        someoneVectorClock.increment(someone);
        Message message1 = new Message(false, true, "Hej1", someone, someoneVectorClock, null);
        someoneVectorClock.increment(someone);
        Message message2 = new Message(false, true, "Hej2", someone, someoneVectorClock, null);
        someoneVectorClock.increment(someone);
        Message message3 = new Message(false, true, "Hej3", someone, someoneVectorClock, null);

        System.err.println("Sending message3 to MessageSorter.");
        messageSorter.receive(message3);
        System.err.println("Sending message2 to MessageSorter.");
        messageSorter.receive(message2);

        System.err.println("Delay 2 seconds.");
        Thread.sleep(2000);
        System.err.println("Sending message1 to MessageSorter.");
        messageSorter.receive(message1);

        System.err.println("Taking...");
        System.err.println("First message delivered from MessageSorter: " + deliverQueue.take().getText());
        System.err.println("Taking...");
        System.err.println("Second message delivered from MessageSorter: " + deliverQueue.take().getText());
        System.err.println("Taking...");
        System.err.println("Third message delivered from MessageSorter: " + deliverQueue.take().getText());

        System.err.println("------------------------------------------------");
    }
}