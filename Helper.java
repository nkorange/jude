import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * Helper methods and variables.
 *
 * @author zpf.073@gmail.com
 */
public class Helper {

    public static BufferedWriter writer;
    public static BufferedReader reader;

    public static BufferedWriter pusher; // push some temporary codes to file
    public static BufferedReader poper; // pop the temporary codes back

    public static boolean pushFlag = false;

    public static void turnOnPusher() {
        pushFlag = true;
    }

    public static void turnOffPusher() {
        pushFlag = false;
    }

    public static boolean shouldPush() {
        return pushFlag;
    }

    public static void popAllCode() {

        String line = null;
        try {
            line = poper.readLine();
            while (line != null) {
                writer.write(line);
                writer.newLine();
                writer.flush();
                line = poper.readLine();
            }
        } catch (Exception e) {
            System.out.println("" + e);
        }
    }

    public static void pushCode(String code) {
        try {
            pusher.write(code);
            pusher.flush();
        } catch (Exception e) {
            System.out.println("" + e);
        }

    }

    public static void pushCodeLine(String code) {
        try {
            pusher.write(code);
            pusher.newLine();
            pusher.flush();
        } catch (Exception e) {
            System.out.println("" + e);
        }

    }

    public static void write(String code) {
        try {
            writer.write(code);
            writer.flush();
        } catch (Exception e) {
            System.out.println("" + e);
        }
    }

    public static void writeLine(String code) {
        try {
            writer.write(code);
            writer.newLine();
            writer.flush();
        } catch (Exception e) {
            System.out.println("" + e);
        }
    }

    static {
        try {
            writer = new BufferedWriter(new FileWriter("program.asm"));
            reader = new BufferedReader(new FileReader("program.asm"));
            pusher = new BufferedWriter(new FileWriter("tmp.asm"));
            poper = new BufferedReader(new FileReader("tmp.asm"));
        } catch (Exception e) {
            System.out.println("" + e);
        }
    }

    public static void end() {

        try {
            writer.close();
            reader.close();
            pusher.close();
            poper.close();
        } catch (Exception e) {

        }
    }

    public static void main(String[] args) throws Exception {
        writer.write("xxxxxxxx");
        writer.newLine();
        writer.close();
    }
}
