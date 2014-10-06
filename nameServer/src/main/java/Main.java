import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import gcom.nameserver.NameService;

/**
 * Created by Jonas on 2014-10-03.
 */
public class Main {
    public static void main(String args[]) {
        try {
            new NameService(9999);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
