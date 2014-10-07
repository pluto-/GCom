import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import gcom.nameserver.NameService;

/**
 * Created by Jonas on 2014-10-03.
 */
public class Main {
    public static void main(String args[]) {
        if(args.length < 1) {
            System.out.println("Specify RMI port as parameter.");
            System.exit(1);
        }
        /*System.out.print("Setting policy...");
        System.setProperty("java.security.policy", "server.policy");
        System.out.println("Done!");*/

        try {
            new NameService(Integer.valueOf(args[0]));
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
