package gcom.utils;

import java.util.Map;

/**
 * Created by Jonas on 2014-10-06.
 */
public class Message {

    private boolean isReliable;
    private String text;
    private Host sender;
    private VectorClock vectorClock;
    private String groupName;

    public Message(boolean isReliable, String text, Host sender, VectorClock vectorClock, String groupName) {
        this.isReliable = isReliable;
        this.text = text;
        this.sender = sender;
        this.vectorClock = vectorClock;
        this.groupName = groupName;
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

}
