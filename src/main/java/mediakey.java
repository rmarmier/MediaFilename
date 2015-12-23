/**
 * Created by raphael on 29.11.15.
 */
public class mediakey {

    public static void main (String[] args){
        if (args.length < 1) {
            System.out.println("You must provide a filename.");
        } else {
            final String filename = args[0];
            System.out.println(filename);
        }
    }
}
