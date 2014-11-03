package gcom.nameserver;

import java.io.IOException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;

import gcom.nameserver.NameService;

/** Contains the main method which takes one argument (local RMI port number). Creates the NameService
 *  object and sends the local RMI port number as parameter.
 * Created by Jonas on 2014-10-03.
 */
public class Main {
    public static void main(String args[]) {
        if(args.length < 1) {
            System.out.println("Specify RMI port as parameter.");
            System.exit(1);
        }

        try {
            new NameService(Integer.valueOf(args[0]));
        } catch (RemoteException | UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
