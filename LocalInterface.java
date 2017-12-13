import java.math.BigInteger;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface LocalInterface extends Remote{
    String getCipher(int id, BigInteger y, String username) throws RemoteException;

    int[] register() throws RemoteException;

    BigInteger getX(int id) throws RemoteException;
}