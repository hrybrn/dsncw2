import java.rmi.RMISecurityManager; 
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URLClassLoader;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class MyServer implements LocalInterface{
    private ConcurrentHashMap<Integer, Integer> aValues;
    private ConcurrentHashMap<Integer, BigInteger> xValues;

    private static final Integer g = 1299061;
    private static final Integer p = 373587043;

    public static void main(String[] args){
        new MyServer();
    }

    public MyServer() {
        try {
            LocalInterface stub = (LocalInterface) UnicastRemoteObject.exportObject(this, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            registry.bind("LocalInterface", stub);

            aValues = new ConcurrentHashMap<Integer, Integer>();
            xValues = new ConcurrentHashMap<Integer, BigInteger>();

            System.setProperty( "java.security.policy", "mypolicy" );
            if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
            }

        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }

    public synchronized int[] register(){
        int a = ThreadLocalRandom.current().nextInt(20);
        
        //find a unique id
        int id = ThreadLocalRandom.current().nextInt();
        while(aValues.get(id) != null){
            id = ThreadLocalRandom.current().nextInt();
        }

        //store a
        aValues.put(id, a);

        BigInteger x = new BigInteger(g.toString()).pow(a).mod(new BigInteger(p.toString()));

        xValues.put(id, x);
        int[] result = {id,g,p};
        return result;
    }

    public BigInteger getX(int id){
        return xValues.get(id);
    }

    public String getCipher(int id, BigInteger y, String username){
        int a = aValues.get(id);
        int key = y.pow(a).mod(new BigInteger(p.toString())).intValue();

        try {
            Registry registry = LocateRegistry.getRegistry("svm-tjn1f15-comp2207.ecs.soton.ac.uk", 12345);

            CiphertextInterface stub = (CiphertextInterface) registry.lookup("CiphertextProvider");
            return stub.get(username, key);
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
            return "";
        }
    }
}