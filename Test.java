public class Test {
    public static void main(String[] args){
        int g = 83;
        int p = 97;
        
        int a = 5;
        int b = 4;

        double y = (Math.pow(g,b)) % p;
        double x = (Math.pow(g,a)) % p;

        int inty = (int) y;
        int intx = (int) x;
        
        //compute key and return it
        int keyc = ((int) Math.pow(intx,b)) % p;
        System.out.println(b);
        System.out.println(keyc);

        //compute key and return it
        int keys = ((int) Math.pow(inty,a)) % p;
        System.out.println(b);
        System.out.println(keys);
    }
}