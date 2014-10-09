package gcom.messagesorter;

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

        if(!holdBackQueues.containsKey(message.getSource())) {
            holdBackQueues.put(message.getSource(), new PriorityBlockingQueue<>(5, new MessageComparator()));
        }
        holdBackQueues.get(message.getSource()).put(message);

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
            for(Host holdBackQueueHost : holdBackQueues.keySet()) {

                PriorityBlockingQueue<Message> holdBackQueue = holdBackQueues.get(holdBackQueueHost);

                Message firstMessage = holdBackQueue.peek();

                if((firstMessage != null) && firstMessage.isCausallyConsistent(localVectorClock)){

                    // Deliver

                    try {
                        deliverQueue.put(firstMessage);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    System.err.println("Delivering to deliverQueue");
                   
                    incrementLocalVectorClock(holdBackQueueHost);
                    holdBackQueue.remove(firstMessage);
                    running = true;
                } /*else {

                    for(Host clockKey : firstMessage.getVectorClock().getClock().keySet()) {
                        System.err.println("First Message Vector Clock Key: " + clockKey + " Value: " + firstMessage.getVectorClock().getValue(clockKey));
                    }
                    for(Host clockKey : localVectorClock.getClock().keySet()) {
                        System.err.println("Local Vector Clock Key: " + clockKey + " Value: " + localVectorClock.getValue(clockKey));
                    }

                    System.err.println("First bool: " + (firstMessage.getVectorClock().getValue(holdBackQueueHost) == (localVectorClock.getValue(holdBackQueueHost) + 1)));
                    System.err.println("Second bool: " + firstMessage.getVectorClock().isBeforeOrEqualOnAllValuesExcept(localVectorClock, holdBackQueueHost));

                }*/

            }
        }
        System.err.println("Not Running");
    }

    private class MessageComparator implements Comparator<Message>
    {

        @Override
        public int compare(Message first, Message second)
        {
            if (first.getVectorClock().isBefore(second.getVectorClock(), first.getSource()))
            {
                return -1;
            }
            if (second.getVectorClock().isBefore(first.getVectorClock(), first.getSource()))
            {
                return 1;
            }
            return 0;
        }
    }
}
