import java.rmi.Remote;
import java.rmi.RemoteException;

public interface LocalInterface extends Remote{
    String getCipher(int id, int key, String username) throws RemoteException;

    int[] register() throws RemoteException;

    void postY(int id, int y) throws RemoteException;
}