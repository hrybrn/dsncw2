import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

class MyClient {
    private int id;

    private String localrmi;
    private String username;

    public static void main(String[] args){
        new MyClient(args[0], args[1]);
    }

    public MyClient(String localrmi, String username){
        this.localrmi = localrmi;
        this.username = username;

        talkToServer();
    }

    private void talkToServer(){
        try{
            //find local rmi server and get the ciphertext
            Registry registry = LocateRegistry.getRegistry(localrmi);
            LocalInterface stub = (LocalInterface) registry.lookup("LocalInterface");
            this.getCipher(this.generateKey(stub), stub);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public int generateKey(LocalInterface stub) throws RemoteException{
        //get x, g, p values from server whilst also registering for an id
        int[] values = stub.register();
        this.id = values[0];
        int x = values[1];
        int g = values[2];
        int p = values[3];

        //generate a random b value
        int b = ThreadLocalRandom.current().nextInt(9);

        //compute y and send it to the server
        int y = (int) (Math.pow(g,b) % p);
        stub.postY(id,y);

        //compute key and return it
        int key = (int) (Math.pow(x,b) % p);
        return key;
    }

    public String decode(String input, int key){
        //convert input to char[] and produce and output array
        char[] chars = input.toCharArray();
        char[] message = new char[chars.length];

        //fix offset
        int offset = key % 8;
        for(int i=0; i<chars.length; i += 8){
            for(int j=0; j<8; j++){
                message[(8 + j - offset) % 8 + i] = chars[i + j];
            }
        }

        //undo caesar
        int caesar = key % 26;
        for(int i=0; i<message.length; i++){
            message[i] = (char) (((message[i] - 65) + 26 - caesar) % 26 + 65); 
        }

        //return output as string
        return new String(message);
    }

    //calls decode twice
    public String doubleDecode(String input, int key){
        return decode(decode(input, key), key);
    }

    public void getCipher(int key, LocalInterface stub){
        try {
            //if failure then try again
            String response = stub.getCipher(id, key, username);

            //print the decoded string
            System.out.println(doubleDecode(response, key));

        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}