import java.io.IOException;

/**
 * 
 * @author zpf.073@gmail.com
 * 
 */
public class Control {

	static final String TAB = "\t";

	static final char CR = '\r';
	
	static final char LF = '\n';

	static char look;
	static int lCount;

	static void other() throws IOException {
		emitLn(String.valueOf(getName()));
	}
	
	static void fin() throws IOException {
		if (look == CR) {
			getChar();
		}
		if (look == LF) {
			getChar();
		}
	}

	static void block(String l) throws IOException {
		while (look != 'e' && look != 'u') {
			fin();
			switch (look) {
			case 'w':
				doWhile();
				break;
			case 'i':
				doIf(l);
				break;
			case 'l':
				// should check if previous 'i' exists and then return:
				return;
			case 'p':
				doLoop();
				break;
			case 'r':
				doRepeat();
				break;
			case 'f':
				doFor();
				break;
			case 'd':
				doDo();
				break;
			case 'b':
				doBreak(l);
				break;
			default:
				assignment();
				break;
			}
			fin();
		}
	}
	
	static void assignment() throws IOException {
		char name = getName();
		match('=');
		boolExpression();
		emitLn("LEA " + name + "(PC), A0");
		emitLn("MOVE D0, (A0)");
	}

	static String newLabel() {
		String s = String.valueOf(lCount);
		lCount++;
		return "L" + s;
	}

	static void postLabel(String l) {
		System.out.println(l + ":");
	}

	static void doIf(String l) throws IOException {
		String l1, l2;
		match('i');
		boolExpression();
		l1 = newLabel();
		l2 = l1;
		emitLn("BEQ " + l1);
		block(l);
		if (look == 'l') {
			match('l');
			l2 = newLabel();
			emitLn("BRA " + l2);
			postLabel(l1);
			block(l);
		}
		match('e');
		postLabel(l2);
	}

	static void doWhile() throws IOException {
		String l1, l2;
		match('w');

		l1 = newLabel();
		l2 = newLabel();
		postLabel(l1);
		boolExpression();
		emitLn("BEQ " + l2);
		block(l2);
		emitLn("BRA " + l1);
		match('e');
		postLabel(l2);
	}

	static void doLoop() throws IOException {
		match('p');
		String l1 = newLabel();
		String l2 = newLabel();
		postLabel(l1);
		block(l2);
		match('e');
		emitLn("BRA " + l1);
		postLabel(l2);
	}

	static void doRepeat() throws IOException {
		match('r');
		String l1 = newLabel();
		String l2 = newLabel();
		postLabel(l1);
		block(l2);
		match('u');
		boolExpression();
		emitLn("BEQ " + l1);
		postLabel(l2);
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

	static void signedFactor() throws IOException {
		if (look == '+') {
			getChar();
			factor();

		} else if (look == '-') {
			getChar();
			if (isDigit(look)) {
				emitLn("MOVE #-" + getNum() + ", D0");
			} else {
				factor();
				emitLn("NEG D0");
			}
		} else {
			factor();
		}
	}

	static void term() throws IOException {
		signedFactor();
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

	static void doFor() throws IOException {
		String l1, l2;
		char name;
		match('f');
		l1 = newLabel();
		l2 = newLabel();
		name = getName();
		match('=');
		expression();
		emitLn("SUBQ #1, D0");
		emitLn("LEA " + name + "(PC), A0");
		emitLn("MOVE D0, (A0)");
		expression();
		emitLn("MOVE D0, -(SP)");
		postLabel(l1);
		emitLn("LEA " + name + "(PC), A0");
		emitLn("MOVE (A0 ) ,D0");
		emitLn("ADDQ #1 ,D0");
		emitLn("MOVE D0 , ( A0)");
		emitLn("CMP (SP) ,D0");
		emitLn("BGT " + l2);
		block(l2);
		match('e');
		emitLn("BRA " + l1);
		postLabel(l2);
		emitLn("ADDQ #2, SP");
	}

	static void doDo() throws IOException {
		String l1, l2;
		match('d');
		l1 = newLabel();
		l2 = newLabel();
		expression();
		emitLn("SUBQ #1, D0");
		postLabel(l1);
		emitLn("MOVE D0, -(SP)");
		block(l2);
		emitLn("MOVE (SP)+, D0");
		emitLn("DBRA D0, " + l1);
		emitLn("SUBQ #2, SP");
		postLabel(l2);
		emitLn("ADDQ #2, SP");
	}

	static void doBreak(String l) throws IOException {
		match('b');
		if (l != "") {
			emitLn("BRA " + l);
		} else {
			abort("No loop to break from");
		}
	}

	static boolean isBoolean(char c) {
		return Character.toUpperCase(c) == 'T'
				|| Character.toUpperCase(c) == 'F';
	}

	static boolean getBoolean() throws IOException {
		char c;
		if (!isBoolean(look)) {
			expected("Boolean Literal");
		}
		c = Character.toUpperCase(look);
		getChar();
		return c == 'T';
	}

	static boolean isOrOp(char c) {
		if (c == '|' || c == '~') {
			return true;
		}
		return false;
	}

	static void boolTerm() throws IOException {
		notFactor();
		while (look == '&') {
			emitLn("MOVE D0, -(SP)");
			match('&');
			notFactor();
			emitLn("AND (SP)+, D0");
		}
	}

	static void notFactor() throws IOException {
		if (look == '!') {
			match('!');
			boolFactor();
			emitLn("EOR #-1, D0");
		} else {
			boolFactor();
		}
	}

	static void boolFactor() throws IOException {
		if (isBoolean(look)) {
			if (getBoolean()) {
				emitLn("MOVE # -1, D0");
			} else {
				emitLn("CLR D0");
			}
		} else {
			relation();
		}
	}

	static void boolExpression() throws IOException {

		boolTerm();
		while (isOrOp(look)) {
			emitLn("MOVE D0, -(SP)");
			switch (look) {
			case '|':
				boolOr();
				break;
			case '~':
				boolXor();
				break;
			}
		}
	}

	static boolean isRelop(char c) {
		return (c == '=' || c == '#' || c == '<' || c == '>');
	}

	static void equals() throws IOException {
		match('=');
		expression();
		emitLn("CMP (SP)+, D0");
		emitLn("SEQ D0");
	}

	static void notEquals() throws IOException {
		match('#');
		expression();
		emitLn("CMP (SP)+, D0");
		emitLn("SNE D0");
	}

	static void less() throws IOException {
		match('<');
		expression();
		emitLn("CMP (SP)+, D0");
		emitLn("SGE D0");
	}

	static void greater() throws IOException {
		match('>');
		expression();
		emitLn("CMP (SP)+, D0");
		emitLn("SLE D0");
	}

	static void relation() throws IOException {
		expression();
		if (isRelop(look)) {
			emitLn("MOVE D0, -(SP)");
			switch (look) {
			case '=':
				equals();
				break;
			case '#':
				notEquals();
				break;
			case '<':
				less();
				break;
			case '>':
				greater();
				break;
			}
			emitLn("TST D0");
		}
	}

	static void boolOr() throws IOException {
		match('|');
		boolTerm();
		emitLn("OR (SP)+, D0");
	}

	static void boolXor() throws IOException {
		match('~');
		boolTerm();
		emitLn("EOR (SP)+, D0");
	}

	/*
	 * static String getName() throws IOException {
	 * 
	 * String token = ""; if (!isAlpha(look)) { expected("Name"); } while
	 * (isAlphaNum(look)) { token = token + Character.toUpperCase(look);
	 * getChar(); } skipWhite(); return token; }
	 */

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

	static void doProgram() throws IOException {
		block("");
		if (look != 'e') {
			expected("End");
		}
		emitLn("END");
	}

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
		if (!isAlpha(look)) {
			expected("Name");
		}

		char tmp = Character.toUpperCase(look);
		getChar();
		skipWhite();
		return tmp;
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

	static void init() throws IOException {

		lCount = 0;
		getChar();
		skipWhite();
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		init();

		while (look != 'e') {
			doProgram();
		}
	}

}
