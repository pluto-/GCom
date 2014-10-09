package client;

import gcom.utils.Host;
import gcom.utils.Message;
import gcom.utils.VectorClock;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Created by Jonas on 2014-10-09.
 */
public class HoldBackQueueGUI extends JFrame {

    JTextArea holdBackQueueText;
    ArrayList<Message> messages;

    public HoldBackQueueGUI() {
        super("Hold-Back Queue");
        setLayout(new BorderLayout());
        messages = new ArrayList<>();

        JLabel label = new JLabel("Hold-Back Queue");
        holdBackQueueText = new JTextArea("");
        holdBackQueueText.setEditable(false);

        add(label, BorderLayout.PAGE_START);
        add(new JScrollPane(holdBackQueueText), BorderLayout.CENTER);

        setSize(300, 300);
    }

    public void addMessage(Message message) {
        messages.add(message);
        updateText();
    }

    public void removeMessage(Message message) {
        messages.remove(message);
        updateText();
    }

    private void updateText() {
        String text = "";
        for(Message message : messages) {
            text = text.concat("\nHost: " + message.getSource() + " Message: " + message.getText());
        }
        holdBackQueueText.setText(text);
    }
}
