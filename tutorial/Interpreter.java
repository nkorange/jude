import java.io.IOException;

/**
 * 
 * @author pengfei.zhu
 * 
 */
public class Interpreter {

	static final String TAB = "\t";

	static final char CR = '\r';

	static final char LF = '\n';

	static char look;

	static int[] table = new int[128];

	static void initTable() {
		for (int i = 'A'; i <= 'Z'; i++) {
			table[i] = 0;
		}
	}

	static char getChar() throws IOException {
		look = (char) System.in.read();
		return look;
	}

	static void error(String s) {
		System.out.println();
		System.out.print("Error: " + s + ".");
	}

	static void abort(String s) {
		error(s);
		System.exit(1);
	}

	static void expected(String s) {
		abort(s + " Expected");
	}

	static void match(char c) throws IOException {
		if (look == c) {
			getChar();
			skipWhite();
		} else {
			expected(String.valueOf(c));
		}
	}

	static boolean isAddOp(char c) {
		if (c == '+' || c == '-') {
			return true;
		}
		return false;
	}

	static boolean isAlpha(char c) {
		char upC = Character.toUpperCase(c);
		if (upC <= 'Z' && upC >= 'A') {
			return true;
		}
		return false;
	}

	static boolean isDigit(char c) {
		if (c <= '9' && c >= '0') {
			return true;
		}
		return false;
	}

	static boolean isAlphaNum(char c) {
		return (isAlpha(c) || isDigit(c));
	}

	static boolean isWhite(char c) {
		if (c == ' ' || c == '\t') {
			return true;
		}
		return false;
	}

	static void skipWhite() throws IOException {
		while (isWhite(look)) {
			getChar();
		}
	}

	static void newLine() throws IOException {
		if (look == CR) {
			getChar();
			if (look == LF) {
				getChar();
			}
		}
	}

	static char getName() throws IOException {

		char token;
		if (!isAlpha(look)) {
			expected("Name");
		}
		token = look;
		getChar();
		skipWhite();
		return token;
	}

	static int getNum() throws IOException {

		int value = 0;
		if (!isDigit(look)) {
			expected("Integer");
		}
		while (isDigit(look)) {
			value = 10 * value + (look - '0');
			getChar();
		}
		return value;
	}

	static void emit(String s) {
		System.out.print(TAB + s);
	}

	static void emitLn(String s) {
		emit(s);
		System.out.println();
	}

	static int term() throws IOException {

		int value = factor();
		while (look == '*' || look == '/') {
			switch (look) {
			case '*':
				match('*');
				value *= factor();
				break;
			case '/':
				match('/');
				value /= factor();
				break;
			default:
				expected("Mul or Div op");
			}
		}
		return value;
	}

	static int factor() throws IOException {

		int value = 0;
		if (look == '(') {
			match('(');
			value = expression();
			match(')');
		} else if (isAlpha(look)) {
			value = table[getName()];
		} else {
			value = getNum();
		}
		return value;
	}

	static int expression() throws IOException {
		int value;
		if (isAddOp(look)) {
			value = 0;
		} else {
			value = term();
		}
		while (isAddOp(look)) {
			switch (look) {
			case '+':
				match('+');
				value += term();
				break;
			case '-':
				match('-');
				value -= term();
				break;
			default:
				expected("AddOp");
			}
		}
		return value;
	}

	static void assignment() throws IOException {
		char name = getName();
		match('=');
		table[name] = expression();
	}

	static void init() throws IOException {
		getChar();
		skipWhite();
	}

	static void input() throws IOException {
		match('?');
		table[getName()] = getNum();
	}

	static void output() throws IOException {
		match('!');
		System.out.println(table[getName()]);
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		init();
		do {
			switch (look) {
			case '?':
				input();
				break;
			case '!':
				output();
				break;
			default:
				assignment();
				break;
			}
			newLine();
		} while (look != '.');
	}

}
