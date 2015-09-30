import java.io.IOException;

/**
 *
 * @author pengfei.zhu
 *
 */
public class Scanner {

	static final char TAB = '\t';

	static final char CR = '\n';

	static final char LF = '\r';

	static char token;

	static char look;

	static String value;

	static int lCount;

	static final String[] KWList = { "IF", "ELSE", "ENDIF", "END" };

	static final String KWCode = "xilee";

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

	static boolean isMulOp(char c) {
		if (c == '*' || c == '/') {
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

	static void getName() throws IOException {
		while (look == CR) {
			fin();
		}
		if (!isAlpha(look)) {
			expected("Name");
		}
		value = "";
		while (isAlNum(look)) {
			value += Character.toUpperCase(look);
			getChar();
		}
		skipWhite();
	}

	static void getNum() throws IOException {
		if (!isDigit(look)) {
			expected("Integer");
		}
		value = "";
		while (isDigit(look)) {
			value += look;
			getChar();
		}
		token = '#';
		skipWhite();
	}

	static void scan() throws IOException {
		getName();
		token = KWCode.charAt(lookUp(value));
	}

	static void matchString(String x) {
		if (!value.equals(x)) {
			expected(x);
		}
	}

	static String newLabel() {
		String s = String.valueOf(lCount);
		lCount++;
		return "L" + s;
	}

	static void postLabel(String l) {
		System.out.println(l + ":");
	}

	static void emit(String s) {
		System.out.print(TAB + s);
	}

	static void emitLn(String s) {
		emit(s);
		System.out.println();
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

	static void signedFactor() throws IOException {
		boolean s = (look == '-');
		if (isAddOp(look)) {
			getChar();
			skipWhite();
		}
		factor();
		if (s) {
			emitLn("NEG D0");
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

	static void term1() throws IOException {
		while (isMulOp(look)) {
			emitLn("MOVE D0, -(SP)");
			switch (look) {
			case '*':
				multiply();
				break;
			case '/':
				divide();
				break;
			}
		}
	}

	static void term() throws IOException {
		factor();
		term1();
	}

	static void firstTerm() throws IOException {
		signedFactor();
		term1();
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
		firstTerm();
		while (isAddOp(look)) {
			emitLn("MOVE D0, -(SP)");
			switch (look) {
			case '+':
				add();
				break;
			case '-':
				substract();
				break;
			}
		}
	}

	static void condition() {
		emitLn("Condition");
	}

	static void doIf() throws IOException {
		String l1, l2;
		condition();
		l1 = newLabel();
		l2 = l1;
		emitLn("BEQ " + l1);
		block();
		if (token == 'l') {
			match('l');
			l2 = newLabel();
			emitLn("BRA " + l2);
			postLabel(l1);
			block();
		}
		postLabel(l2);
		matchString("ENDIF");
	}

	static void assignment() throws IOException {
		String name;
		name = value;
		match('=');
		expression();
		emitLn("LEA " + name + "(PC), A0");
		emitLn("MOVE D0, (A0)");
	}

	static void block() throws IOException {
		scan();
		while (token != 'e' && token != 'l') {
			switch (token) {
			case 'i':
				doIf();
				break;
			default:
				assignment();
			}
			scan();
		}
	}

	static void doProgram() throws IOException {
		block();
		matchString("END");
		emitLn("END");
	}

	static void init() throws IOException {
		lCount = 0;
		getChar();
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		init();
		doProgram();
	}

}
