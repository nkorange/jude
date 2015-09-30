import java.io.IOException;

/**
 * 
 * @author pengfei.zhu
 * 
 */
public class Topdown {

	static final String TAB = "\t";

	static final char CR = '\r';

	static char look;

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

	static char getName() throws IOException {

		char token;
		if (!isAlpha(look)) {
			expected("Name");
		}
		token = Character.toUpperCase(look);
		getChar();
		skipWhite();
		return token;
	}

	static String getNum() throws IOException {

		String value = "";
		if (!isDigit(look)) {
			expected("Integer");
		}
		while (isDigit(look)) {
			value = value + look;
			getChar();
		}
		skipWhite();
		return value;
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
			emitLn("MOVE #" + getNum() + ", D0");
		}
	}

	static void ident() throws IOException {
		char name = getName();
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
		char name = getName();
		match('=');
		expression();
		emitLn("LEA " + name + "(PC), A0");
		emitLn("MOVE D0, (A0)");
	}

	static void init() throws IOException {
		getChar();
		skipWhite();
	}

	static void postLabel(char c) {
		System.out.println(c + ":");
	}

	static void prog() throws IOException {
		char name;
		match('p');
		name = getName();
		prolog();
		doBlock(name);
		match('.');
		epilog(name);
	}

	static void prolog() {
		emitLn("WARMST EQU $A01E");
	}

	static void epilog(char name) {
		emitLn("DC WARMST");
		emitLn("END " + name);
	}

	static void doBlock(char name) throws IOException {
		declarations();
		postLabel(name);
		statements();
	}

	static void declarations() throws IOException {

		while (look == 'l' || look == 'c' || look == 't' || look == 'v'
				|| look == 'p' || look == 'f') {
			switch (look) {
			case 'l':
				labels();
				break;
			case 'c':
				constants();
				break;
			case 't':
				types();
				break;
			case 'v':
				variables();
				break;
			case 'p':
				doProcedure();
				break;
			case 'f':
				doFunction();
				break;
			}
		}
	}

	static void statements() throws IOException {
		match('b');
		while (look != 'e') {
			getChar();
		}
		match('e');
	}
	
	static void labels() throws IOException {
		match('l');
	}
	
	static void constants() throws IOException {
		match('c');
	}
	
	static void types() throws IOException {
		match('t');
	}
	
	static void variables() throws IOException {
		match('v');
	}
	
	static void doProcedure() throws IOException {
		match('p');
	}
	
	static void doFunction() throws IOException {
		match('f');
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		init();
		prog();
	}

}
