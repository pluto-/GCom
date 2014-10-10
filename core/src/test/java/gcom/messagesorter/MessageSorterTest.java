package gcom.messagesorter;

import gcom.MessageSorter;
import gcom.utils.Host;
import gcom.utils.Message;
import gcom.utils.VectorClock;
import junit.framework.TestCase;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MessageSorterTest extends TestCase {

    public void testReceive_ordered() throws Exception {
        System.err.println("\n\nALREADY ORDERED TEST");
        System.err.println("------------------------------------------------");
        BlockingQueue<Message> deliverQueue = new LinkedBlockingQueue<>();
        VectorClock localVectorClock = new VectorClock();

        Host localHost = new Host(InetAddress.getByName("localhost"), 2000);
        MessageSorter messageSorter = new MessageSorter(deliverQueue,  localVectorClock);

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
        BlockingQueue<Message> deliverQueue = new LinkedBlockingQueue<>();
        VectorClock localVectorClock = new VectorClock();

        Host localHost = new Host(InetAddress.getByName("localhost"), 2000);
        MessageSorter messageSorter = new MessageSorter(deliverQueue,  localVectorClock);

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
        BlockingQueue<Message> deliverQueue = new LinkedBlockingQueue<>();
        VectorClock localVectorClock = new VectorClock();

        Host localHost = new Host(InetAddress.getByName("localhost"), 2000);
        MessageSorter messageSorter = new MessageSorter(deliverQueue,  localVectorClock);

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

    public void testSpamReceive() throws Exception {
        BlockingQueue<Message> deliverQueue = new LinkedBlockingQueue<>();
        VectorClock localVectorClock = new VectorClock();
        MessageSorter messageSorter = new MessageSorter(deliverQueue,  localVectorClock);

        for(int i = 1; i < 10; i++) {
            new Run(100, messageSorter, i);
        }

        int finishedProcesses = 0;
        while(finishedProcesses < 9) {
            String text = deliverQueue.take().getText();
            if (text.endsWith("99")) {
                System.out.println(text);
                finishedProcesses++;
            }
        }
    }

    private class Run implements Runnable {

        int numberOfMessages;
        Thread thread;
        MessageSorter messageSorter;
        int id;

        public Run(int numberOfMessages, MessageSorter messageSorter, int id) {
            this.messageSorter = messageSorter;
            this.numberOfMessages = numberOfMessages;
            this.id = id;
            thread = new Thread(this);
            thread.start();
        }

        @Override
        public void run() {
            Host someone = null;
            try {
                someone = new Host(InetAddress.getByName("1.1.1." + id), 3000);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            VectorClock someoneVectorClock = new VectorClock();
            for(int i = 0; i < numberOfMessages; i++) {
                someoneVectorClock.increment(someone);
                Message message = new Message(false, true, "id: " + id + " message: " + i, someone, someoneVectorClock, null);
                messageSorter.receive(message);
            }
        }
    }

}