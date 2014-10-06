package gcom.messagesorter;

import GCom.GCom;
import sun.plugin2.message.Message;
import transfer.Host;

/**
 * Created by Jonas on 2014-10-03.
 */
public class MessageSorter {

    GCom gCom;


    public MessageSorter(GCom gCom) {
        this.gCom = gCom;
    }


    public void deliverCausally(Host sender, Message message) {

    }
}
