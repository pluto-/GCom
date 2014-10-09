package client;

import gcom.utils.Host;
import gcom.utils.VectorClock;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Jonas on 2014-10-09.
 */
public class VectorClockGUI extends JFrame {

    JTextArea vectorClockText;

    public VectorClockGUI() {
        super("Local Vector Clock");
        setLayout(new BorderLayout());

        JLabel label = new JLabel("Local Vector Clock");
        vectorClockText = new JTextArea("");
        vectorClockText.setEditable(false);

        add(label, BorderLayout.PAGE_START);
        add(new JScrollPane(vectorClockText), BorderLayout.CENTER);

        setSize(300, 300);
    }

    public void setVectorClockText(VectorClock vectorClock) {
        System.out.println("New Vector Clock");
        if(vectorClock == null) {
            vectorClockText.setText("");
        } else {
            String text = "";
            for(Host key : vectorClock.getClock().keySet()) {
                text = text.concat("\nHost: " + key + " Clock: " + vectorClock.getValue(key));
            }
            vectorClockText.setText(text);
        }
    }
}
