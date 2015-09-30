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

    static final char TAB = '\t';
    static final char CR = '\r';
    static final char LF = '\n';
    static final char BLANK = ' ';
    static final char POINT = '.';

    static final String TEMP_FLOAT_VAR_NAME = "__1__";
    static final String NEW_LINE_CONST_STR = "__2__";
    static final String OUTPUT_BUFFER_ADDR = "__output_buffer__";

    public static BufferedWriter writer;
    public static BufferedReader reader;

    public static BufferedWriter pusher; // push some temporary codes to file
    public static BufferedReader poper; // pop the temporary codes back

    public static BufferedWriter initCodePusher;
    public static BufferedReader initCodePoper;

    public static BufferedReader codeReader = null;

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
            initCodePusher.write("\t" + code);
            initCodePusher.newLine();
            initCodePusher.flush();
        } catch (Exception e) {
            System.out.println("" + e);
        }
    }

    public static void pushInitLabelLine(String label) {
        try {
            initCodePusher.write(label + ":");
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

            pushInitLabelLine("_push_digit");
            pushInitCodeLine("cmp qword [__tmp_buf], 0");
            pushInitCodeLine("jl __set_neg_flag");
            pushInitLabelLine("__push_digit");
            pushInitCodeLine("mov qword rax, [__tmp_buf]");
            pushInitCodeLine("call __divide_by_ten");
            pushInitCodeLine("push rax");
            pushInitCodeLine("inc rbx");
            pushInitCodeLine("cmp qword [__tmp_buf], 0");
            pushInitCodeLine("jg _push_digit");
            pushInitCodeLine("mov rax, [__neg_flag]");
            pushInitCodeLine("mov [__out_buf], rax");
            pushInitCodeLine("push rax");
            pushInitCodeLine("mov rax, 0");
            pushInitCodeLine("mov [__neg_flag], rax");
            pushInitCodeLine("pop rax");
            pushInitCodeLine("cmp rax, 0");
            pushInitCodeLine("je __pop_digit");
            pushInitCodeLine("call __print_digit");

            pushInitLabelLine("__pop_digit");
            pushInitCodeLine("pop rax");
            pushInitCodeLine("mov [__out_buf], rax");
            pushInitCodeLine("call __print_digit");
            pushInitCodeLine("dec rbx");
            pushInitCodeLine("cmp rbx, 0");
            pushInitCodeLine("jg __pop_digit");
            pushInitCodeLine("ret");

            pushInitLabelLine("__set_neg_flag");
            pushInitCodeLine("mov rax, 45");
            pushInitCodeLine("mov [__neg_flag], rax");
            pushInitCodeLine("mov rax, [__tmp_buf]");
            pushInitCodeLine("neg rax");
            pushInitCodeLine("mov [__tmp_buf], rax");
            pushInitCodeLine("jmp __push_digit");

            pushInitLabelLine("__print_digit");
            pushInitCodeLine("push rdi");
            pushInitCodeLine("push rsi");
            pushInitCodeLine("mov rax, 0x2000004");
            pushInitCodeLine("mov rdi, 1");
            pushInitCodeLine("mov rsi, __out_buf");
            pushInitCodeLine("mov rdx, 1");
            pushInitCodeLine("syscall");
            pushInitCodeLine("pop rsi");
            pushInitCodeLine("pop rdi");
            pushInitCodeLine("ret");

            pushInitLabelLine("__divide_by_ten");
            pushInitCodeLine("push rdx");
            pushInitCodeLine("push rcx");
            pushInitCodeLine("mov rdx, 0");
            pushInitCodeLine("mov rcx, 10");
            pushInitCodeLine("idiv qword rcx");
            pushInitCodeLine("mov [__tmp_buf], rax");
            pushInitCodeLine("mov rax, rdx");
            pushInitCodeLine("add rax, 0x30");
            pushInitCodeLine("pop rcx");
            pushInitCodeLine("pop rdx");
            pushInitCodeLine("ret");

            pushInitLabelLine("_print_bool");
            pushInitCodeLine("cmp rax, 0");
            pushInitCodeLine("jne _print_true");
            pushInitCodeLine("push rdi");
            pushInitCodeLine("push rsi");
            pushInitCodeLine("mov rax, 0x2000004");
            pushInitCodeLine("mov rdi, 1");
            pushInitCodeLine("mov rsi, __false");
            pushInitCodeLine("mov rdx, __false.len");
            pushInitCodeLine("syscall");
            pushInitCodeLine("pop rsi");
            pushInitCodeLine("pop rdi");
            pushInitLabelLine("__print_bool");
            pushInitCodeLine("ret");

            pushInitLabelLine("_print_true");
            pushInitCodeLine("push rdi");
            pushInitCodeLine("push rsi");
            pushInitCodeLine("mov rax, 0x2000004");
            pushInitCodeLine("mov rdi, 1");
            pushInitCodeLine("mov rsi, __true");
            pushInitCodeLine("mov rdx, __true.len");
            pushInitCodeLine("syscall");
            pushInitCodeLine("pop rsi");
            pushInitCodeLine("pop rdi");
            pushInitCodeLine("jmp __print_bool");

            pushInitLabelLine("_init");
            pushInitCodeLine("push rbp");
            pushInitCodeLine("mov rbp, rsp");

            storeCode("section .bss");
            storeCode("__x  resb 8");
            storeCode("__out_buf    resb 1");
            storeCode("section .data");
            storeCode("__something:    db \"useless string\"");
            storeCode(NEW_LINE_CONST_STR + ":\t" + "db `\\n`");
            storeCode(".len equ $ - " + NEW_LINE_CONST_STR);
            storeCode("__true:\tdb \"true\"");
            storeCode(".len equ $ - __true");
            storeCode("__false:\tdb \"false\"");
            storeCode(".len equ $ - __false");
            storeCode("__y: db  \"yyy\"");
            storeCode(".len equ $ - __y");
            storeCode("__ten: dq  10");
            storeCode("__tmp_buf: dq 0");
            storeCode("__neg_flag: dq 0");

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
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        char[] c = new char[256];
        for (int i = 0; i < 256; i++) {
            c[i] = (char) i;
            System.out.print(c[i]);
        }
    }
}
