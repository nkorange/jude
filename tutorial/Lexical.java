import java.io.IOException;

/**
 * 
 * @author zpf.073@gmail.com
 * 
 */
public class Lexical {

	static final String TAB = "\t";

	static final char CR = '\r';

	static final char LF = '\n';

	static char look;

	static String[] KWList = { "IF", "ELSE", "ENDIF", "END" };
	
	static String KWCode = "xilee";
	
	static char token;

	static String value;

	static void getChar() throws IOException {
		look = (char) System.in.read();
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

	static boolean isOp(char c) {
		return (c == '+' || c == '-' || c == '*' || c == '/' || c == '>'
				|| c == '<' || c == ':' || c == '=');
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

	static boolean isAlNum(char c) {
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

	static void skipComma() throws IOException {
		skipWhite();
		if (look == ',') {
			getChar();
			skipWhite();
		}
	}

	static void fin() throws IOException {
		if (look == CR) {
			getChar();
		}
		if (look == LF) {
			getChar();
		}
	}

	static void getName() throws IOException {

		int k;
		value = "";
		if (!isAlpha(look)) {
			expected("Name");
		}
		while (isAlNum(look)) {
			value += Character.toUpperCase(look);
			getChar();
		}
		k = lookUp(value);

			token = KWCode.charAt(k);

	}

	static void getNum() throws IOException {

		value = "";
		if (!isDigit(look)) {
			expected("Integer");
		}
		while (isDigit(look)) {
			value += look;
			getChar();
		}
		token = '#';
	}

	static void getOp() throws IOException {
		value = "";
		if (!isOp(look)) {
			expected("Operator");
		}
		while (isOp(look)) {
			value += look;
			getChar();
		}
		if (value.length() == 1) {
			token = value.charAt(0);
		} else {
			token = '?';
		}
	}

	/**
	 * search s in KWList, return the occurrence index of s in KWList.
	 * 
	 * @param s
	 * @return
	 */
	static int lookUp(String s) {
		for (int i = 0; i < KWList.length; i++) {
			if (KWList[i].equals(s)) {
				return i + 1;
			}
		}
		return 0;
	}

	static void scan() throws IOException {
		if (look == CR) {
			fin();
		}
		if (look == LF) {
			fin();
		}

		if (isAlpha(look)) {
			getName();
		} else if (isDigit(look)) {
			getNum();
		} else if (isOp(look)) {
			getOp();
		} else {
			value = String.valueOf(look);
			token = '?';
			getChar();
		}
		skipComma();
	}

	static void emit(String s) {
		System.out.print(TAB + s);
	}

	static void emitLn(String s) {
		emit(s);
		System.out.println();
	}

	static void term() throws IOException {
		factor();
		while (look == '*' || look == '/') {
			emitLn("MOVE D0, -(SP)");
			switch (look) {
			case '*':
				multiply();
				break;
			case '/':
				divide();
				break;
			default:
				expected("Mul or Div op");
			}
		}
	}

	static void factor() throws IOException {

		if (look == '(') {
			match('(');
			expression();
			match(')');
		} else if (isAlpha(look)) {
			ident();
		} else {
			getNum();
			emitLn("MOVE #" + value + ", D0");
		}
	}

	static void ident() throws IOException {
		getName();
		String name = value;
		if (look == '(') {
			match('(');
			match(')');
			emitLn("BSR " + name);
		} else {
			emitLn("MOVE " + name + "(PC), D0");
		}
	}

	static void multiply() throws IOException {
		match('*');
		factor();
		emitLn("MULS (SP)+, D0");
	}

	static void divide() throws IOException {
		match('/');
		factor();
		emitLn("DIVS (SP)+, D0");
	}

	static void add() throws IOException {
		match('+');
		term();
		emitLn("ADD (SP)+, D0");
	}

	static void substract() throws IOException {
		match('-');
		term();
		emitLn("SUB (SP)+, D0");
		emitLn("NEG D0");
	}

	static void expression() throws IOException {

		if (isAddOp(look)) {
			emitLn("CLR D0"); // same as emitLn("MOV #0, D0")
		} else {
			term();
		}
		while (look == '+' || look == '-') {
			emitLn("MOVE D0, -(SP)");
			switch (look) {
			case '+':
				add();
				break;
			case '-':
				substract();
				break;
			default:
				expected("Add or Sub op");
			}
		}
	}

	static void assignment() throws IOException {
		getName();
		String name = value;
		match('=');
		expression();
		emitLn("LEA " + name + "(PC), A0");
		emitLn("MOVE D0, (A0)");
	}

	static void init() throws IOException {
		getChar();
		skipWhite();
	}

	public static void main(String[] args) throws IOException {

		init();
		do {
			scan();
			switch (token) {
			case 'x':
				System.out.print("Ident ");
				break;
			case '#':
				System.out.print("Number ");
				break;
			case 'i':
			case 'l':
			case 'e':
				System.out.print("Keyword");
				break;
			default:
				System.out.print("Operator ");
				break;
			}
			System.out.println(value);
		} while (!value.equals("END"));

	}

}
