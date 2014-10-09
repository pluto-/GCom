package gcom.messagesorter;

import gcom.GCom;
import gcom.utils.Host;
import gcom.utils.Message;
import gcom.utils.VectorClock;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by Jonas on 2014-10-03.
 */
public class MessageSorter implements Runnable {

    private BlockingQueue<Message> deliverQueue;
    private VectorClock localVectorClock;
    private volatile Map<Host,PriorityBlockingQueue<Message>> holdBackQueues;
    private Thread thread;
    private volatile boolean running;

    public MessageSorter(BlockingQueue<Message> deliverQueue, VectorClock localVectorClock) {
        this.deliverQueue = deliverQueue;
        this.localVectorClock = localVectorClock;
        holdBackQueues = new ConcurrentHashMap<>();
    }

    public synchronized void receive(Message message) {

        if(!holdBackQueues.containsKey(message.getSender())) {
            holdBackQueues.put(message.getSender(), new PriorityBlockingQueue<>(5, new MessageComparator()));
        }
        holdBackQueues.get(message.getSender()).put(message);

        startThread();
    }

    private void startThread() {
        running = true;
        if(thread == null || !thread.isAlive()) {
            thread = new Thread(this);
            thread.start();
        }
    }

    private void incrementLocalVectorClock(Host host) {
        localVectorClock.increment(host);
    }

    @Override
    public void run() {
        while(running) {
            System.err.println("Running");
            running = false;
            Iterator<Host> keys = holdBackQueues.keySet().iterator();
            Host key;
            while(keys.hasNext()) {
                key = keys.next();

                PriorityBlockingQueue<Message> holdBackQueue = holdBackQueues.get(key);

                Message firstMessage = holdBackQueue.peek();

                if((firstMessage == null) || (firstMessage.getVectorClock().getValue(key) == null)) {}
                else if((firstMessage.getVectorClock().getValue(key) == (localVectorClock.getValue(key) + 1)) &&
                        firstMessage.getVectorClock().isBeforeOrEqualOnAllValuesExcept(localVectorClock, key)){

                    // Deliver

                    try {
                        deliverQueue.put(firstMessage);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    System.err.println("Delivering to deliverQueue");
                   
                    incrementLocalVectorClock(key);
                    holdBackQueue.remove();
                    running = true;
                } else {

                    for(Host key1 : firstMessage.getVectorClock().getClock().keySet()) {
                        System.err.println("First Message Vector Clock Key: " + key1 + " Value: " + firstMessage.getVectorClock().getValue(key1));
                    }
                    for(Host key1 : localVectorClock.getClock().keySet()) {
                        System.err.println("Local Vector Clock Key: " + key1 + " Value: " + localVectorClock.getValue(key1));
                    }

                    System.err.println("First bool: " + (firstMessage.getVectorClock().getValue(key) == (localVectorClock.getValue(key) + 1)));
                    System.err.println("Second bool: " + firstMessage.getVectorClock().isBeforeOrEqualOnAllValuesExcept(localVectorClock, key));

                }

            }
        }
        System.err.println("Not Running");
    }

    private class MessageComparator implements Comparator<Message>
    {

        @Override
        public int compare(Message first, Message second)
        {
            if (first.getVectorClock().isBefore(second.getVectorClock(), first.getSender()))
            {
                return -1;
            }
            if (second.getVectorClock().isBefore(first.getVectorClock(), first.getSender()))
            {
                return 1;
            }
            return 0;
        }
    }
}
