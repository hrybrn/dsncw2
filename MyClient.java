import java.math.BigInteger;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

class MyClient {
    private int id;

    private BigInteger x;
    private BigInteger y;

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

    private int[] generateKey(LocalInterface stub) throws RemoteException{
        //get x, g, p values from server whilst also registering for an id
        int[] values = stub.register();
        this.id = values[0];
        Integer g = values[1];
        Integer p = values[2];

        x = stub.getX(id);

        //generate a random b value
        int b = ThreadLocalRandom.current().nextInt(9);

        //compute y
        y = new BigInteger(g.toString()).pow(b).mod(new BigInteger(p.toString()));
        
        int[] output = {b, p};
        return output;
    }

    private String decode(String input, int key){
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
    private String fullDecode(String input, int key){
        return clean(decode(decode(input, key), key));
    }

    private String clean(String input){
        ArrayDeque<Character> chars = new ArrayDeque<Character>();

        for(Character c : input.toCharArray()){
            chars.add(c);
        }

        while(chars.peekLast().equals('Z')){
            chars.pollLast();
        }

        char[] out = new char[chars.size()];
        
        for(int i = 0; i < out.length; i++){
            out[i] = (char) chars.pollFirst();
        }

        return new String(out);
    }

    private void getCipher(int[] vals, LocalInterface stub){
        int b = vals[0];
        Integer p = vals[1];
        //compute key and return it
        int key = x.pow(b).mod(new BigInteger(p.toString())).intValue();
        try {
            String response = stub.getCipher(id, y, username);

            //print the decoded string
            System.out.println(fullDecode(response, key));

        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}