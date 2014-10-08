package gcom.utils;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by Jonas on 2014-10-06.
 */
public class Message implements Serializable {

    private boolean isReliable;
    private boolean deliverCausally;
    private String text;
    private Host sender;
    private VectorClock vectorClock;
    private String groupName;

    public Message(boolean isReliable, boolean deliverCausally, String text, Host sender, VectorClock vectorClock, String groupName) {
        this.isReliable = isReliable;
        this.deliverCausally = deliverCausally;
        this.text = text;
        this.sender = sender;
        this.vectorClock = new VectorClock(vectorClock);
        this.groupName = groupName;
    }

    public boolean deliverCausally() {
        return deliverCausally;
    }

    public String getGroupName() {
        return groupName;
    }

    public VectorClock getVectorClock() {
        return vectorClock;
    }

    public boolean isReliable() {
        return isReliable;
    }

    public String getText() {
        return text;
    }

    public Host getSender() {
        return sender;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message message = (Message) o;

        if (deliverCausally != message.deliverCausally) return false;
        if (isReliable != message.isReliable) return false;
        if (groupName != null ? !groupName.equals(message.groupName) : message.groupName != null) return false;
        if (sender != null ? !sender.equals(message.sender) : message.sender != null) return false;
        if (text != null ? !text.equals(message.text) : message.text != null) return false;
        if (vectorClock != null ? !vectorClock.equals(message.vectorClock) : message.vectorClock != null) return false;

        return true;
    }

}
