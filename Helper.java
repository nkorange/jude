import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.LinkedList;
import java.util.List;

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

    public static BufferedWriter initCodePusher;
    public static BufferedReader initCodePoper;

    public static boolean pushFlag = false;

    public static List<String> codes = new LinkedList<String>();

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

    public static void pushInitCode(String code) {
        try {
            initCodePusher.write(code);
            initCodePusher.flush();
        } catch (Exception e) {
            System.out.println("" + e);
        }
    }

    public static void pushInitCodeLine(String code) {
        try {
            initCodePusher.write(code);
            initCodePusher.newLine();
            initCodePusher.flush();
        } catch (Exception e) {
            System.out.println("" + e);
        }
    }

    public static void popInitCode() {
        String line = null;
        try {
            line = initCodePoper.readLine();
            while (line != null) {
                writer.write(line);
                writer.newLine();
                writer.flush();
                line = initCodePoper.readLine();
            }

            // At the end of the init procedure:
            writer.write("\tleave");
            writer.newLine();
            writer.flush();
            writer.write("\tret");
            writer.newLine();
            writer.flush();
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
            initCodePusher = new BufferedWriter(new FileWriter("init.asm"));
            initCodePoper = new BufferedReader(new FileReader("init.asm"));
            initCodePusher.write("_init:");
            initCodePusher.newLine();
            initCodePusher.flush();
            initCodePusher.write("\tpush rbp");
            initCodePusher.newLine();
            initCodePusher.flush();
            initCodePusher.write("\tmov rbp, rsp");
            initCodePusher.newLine();
            initCodePusher.flush();
            codes.add("section .data");
        } catch (Exception e) {
            System.out.println("" + e);
        }
    }

    public static void end() {
        fetchCode();
        try {
            writer.close();
            reader.close();
            pusher.close();
            poper.close();
        } catch (Exception e) {

        }
    }

    public static void storeCode(String code) {
        codes.add(code);
    }

    public static void fetchCode() {
        try {
            for (String code : codes) {
                writer.write(code);
                writer.newLine();
                writer.flush();
            }
        } catch (Exception e) {
            System.out.println("" + e);
        }
    }

    public static void main(String[] args) throws Exception {
        writer.write("xxxxxxxx");
        writer.newLine();
        writer.close();
    }
}
