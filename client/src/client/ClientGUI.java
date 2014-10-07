package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * Created by Jonas on 2014-10-06.
 */
public class ClientGUI extends JFrame implements ActionListener {

    JTextArea chat;
    JTextField message;
    JButton send;
    Client client;

    /**
     * GUI look taken from: http://codereview.stackexchange.com/questions/25461/simple-chat-room-swing-gui
     */
    public ClientGUI(Client client) {

        this.client = client;

        client.setGroupName(JOptionPane.showInputDialog("Group name: "));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        JPanel southPanel = new JPanel();
        southPanel.setBackground(Color.BLUE);
        southPanel.setLayout(new GridBagLayout());

        message = new JTextField(30);
        message.requestFocusInWindow();

        send = new JButton("Send");
        send.addActionListener(this);

        chat = new JTextArea();
        chat.setEditable(false);
        chat.setFont(new Font("Times New Roman", Font.PLAIN, 15));
        chat.setLineWrap(true);

        mainPanel.add(new JScrollPane(chat), BorderLayout.CENTER);

        GridBagConstraints left = new GridBagConstraints();
        left.anchor = GridBagConstraints.LINE_START;
        left.fill = GridBagConstraints.HORIZONTAL;
        left.weightx = 512.0D;
        left.weighty = 1.0D;

        GridBagConstraints right = new GridBagConstraints();
        right.insets = new Insets(0, 10, 0, 0);
        right.anchor = GridBagConstraints.LINE_END;
        right.fill = GridBagConstraints.NONE;
        right.weightx = 1.0D;
        right.weighty = 1.0D;

        southPanel.add(message, left);
        southPanel.add(send, right);

        mainPanel.add(BorderLayout.SOUTH, southPanel);

        add(mainPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(470, 300);
        setVisible(true);


    }

    public void incomingMessage(String message) {
        chat.append("< Someone >:  " + message
                + "\n");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource().equals(send)) {
            if (message.getText().length() < 1) {
                // do nothing
            } else if (message.getText().equals(".clear")) {
                chat.setText("Cleared all messages\n");
                message.setText("");
            } else {
                try {
                    client.sendMessage(message.getText());
                } catch (NotBoundException e1) {
                    e1.printStackTrace();
                } catch (RemoteException e1) {
                    e1.printStackTrace();
                } catch (UnknownHostException e1) {
                    e1.printStackTrace();
                }
                message.setText("");
            }
            message.requestFocusInWindow();

        }
    }
}