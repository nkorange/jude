import java.io.IOException;

/**
 * @author pengfei.zhu.
 */
public class Judec {

	public static void main(String[] args) throws IOException {

		if (args.length == 0) {
			System.out.println("no input file!");
			System.exit(1);
		}

		String fname = args[0];
		if (!fname.endsWith(".jude")) {
			System.out.println("input file should end with .jude");
			System.exit(1);
		}

		String[] paths = fname.split("/");
		String program = paths[paths.length - 1].substring(0,
				paths[paths.length - 1].length() - 5);

		Jude.main(args);
		Runtime.getRuntime().exec("nasm -f macho64 " + program + ".asm");
		Process gccProcess = Runtime.getRuntime().exec(
				"gcc -o " + program + " " + program + ".o");
		if (gccProcess.exitValue() == 0) {
			System.out
					.println("now executable program is available:" + program);
		}

	}
}
