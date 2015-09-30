import java.io.IOException;

/**
 * compiler that supports PROCEDURE
 * 
 * @author zpf.073@gmail.com
 * 
 */
public class Procedure {

	static final char TAB = '\t';

	static final char CR = '\r';

	static final char LF = '\n';

	static char look;

	static char[] ST = new char['Z' + 1];

	static int[] params = new int['Z' + 1];

	static int numParams;
	
	static int base;

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

	static void undefined(String name) {
		abort("Undefined Identifier " + name);
	}

	static void duplicate(String name) {
		abort("Duplicate Identifier " + name);
	}

	static char typeOf(char n) {

		if (isParam(n)) {
			return 'f';
		} else {
			return ST[n];
		}
	}

	static boolean inTable(char n) {
		return (ST[n] != ' ');
	}

	static void addEntry(char name, char t) {
		if (inTable(name)) {
			duplicate(String.valueOf(name));
		}
		ST[name] = t;
	}

	static void checkVar(char name) {
		if (!inTable(name)) {
			undefined(String.valueOf(name));
		}
		if (typeOf(name) != 'v') {
			abort(name + " is not a variable");
		}
	}

	static boolean isAlpha(char c) {
		return (Character.toUpperCase(c) >= 'A' && Character.toUpperCase(c) <= 'Z');
	}

	static boolean isDigit(char c) {
		return (c >= '0' && c <= '9');
	}

	static boolean isAlNum(char c) {
		return (isAlpha(c) || isDigit(c));
	}

	static boolean isAddOp(char c) {
		return (c == '+' || c == '-');
	}

	static boolean isMulOp(char c) {
		return (c == '*' || c == '/');
	}

	static boolean isOrOp(char c) {
		return (c == '|' || c == '~');
	}

	static boolean isRelOp(char c) {
		return (c == '=' || c == '#' || c == '>' || c == '<');
	}

	static boolean isWhite(char c) {
		return (c == ' ' || c == TAB);
	}

	static void skipWhite() throws IOException {
		while (isWhite(look)) {
			getChar();
		}
	}

	static void fin() throws IOException {
		if (look == CR) {
			getChar();
			if (look == LF) {
				getChar();
			}
		}
	}

	static void match(char x) throws IOException {
		if (look == x) {
			getChar();
		} else {
			expected(String.valueOf(x));
		}
		skipWhite();
	}

	static char getName() throws IOException {
		if (!isAlpha(look)) {
			expected("Name");
		}
		char name = Character.toUpperCase(look);
		getChar();
		skipWhite();
		return name;
	}

	static char getNum() throws IOException {
		if (!isDigit(look)) {
			expected("Integer");
		}
		char c = look;
		getChar();
		skipWhite();
		return c;
	}

	static void emit(String s) {
		System.out.print(TAB + s);
	}

	static void emitLn(String s) {
		emit(s);
		System.out.println();
	}

	static void postLabel(String l) {
		System.out.println(l + ":");
	}

	static void loadVar(char name) {
		checkVar(name);
		emitLn("MOVE " + name + "(PC), D0");
	}

	static void storeVar(char name) {
		checkVar(name);
		emitLn("LEA " + name + "(PC), A0");
		emitLn("MOVE D0, (A0)");
	}

	static void init() throws IOException {
		char i;
		getChar();
		skipWhite();
		for (i = 'A'; i <= 'Z'; i++) {
			ST[i] = ' ';
		}
		clearParams();
	}

	static void expression() throws IOException {
		char name = getName();
		if (isParam(name)) {
			loadParam(paramNumber(name));
		} else {
			loadVar(name);
		}
	}

	static void assignment(char name) throws IOException {
		match('=');
		expression();
		if (isParam(name)) {
			storeParam(paramNumber(name));
		} else {
			storeVar(name);
		}
	}

	static void clearParams() {
		char i;
		for (i = 'A'; i <= 'Z'; i++) {
			params[i] = 0;
		}
		numParams = 0;
	}

	static void formalParam() throws IOException {
		addParam(getName());
	}

	static void param() throws IOException {
		expression();
		push();
	}

	static int paramNumber(char n) {
		return params[n];
	}

	static boolean isParam(char n) {
		return (params[n] != 0);
	}

	static void addParam(char name) {
		if (isParam(name)) {
			duplicate(String.valueOf(name));
		}
		numParams++;
		params[name] = numParams;
	}

	static void loadParam(int n) {
		int offset;
		offset = 8 + 2 * (base - n);
		emit("MOVE ");
		System.out.println(offset + " (A6), D0");
	}

	static void storeParam(int n) {
		int offset;
		offset = 8 + 2 * (base - n);
		emit("MOVE D0, ");
		System.out.println(offset + "(A6)");
	}

	static void push() {
		emitLn("MOVE D0, -(SP)");
	}

	static int paramList() throws IOException {
		int n = 0;
		match('(');
		if (look != ')') {
			param();
			n++;
			while (look == ',') {
				match(',');
				param();
				n++;
			}
		}
		match(')');
		return 2 * n;
	}

	static void formalList() throws IOException {
		match('(');
		if (look != ')') {
			formalParam();
			while (look == ',') {
				match(',');
				formalParam();
			}
		}
		match(')');
		fin();
		base = numParams;
		numParams += 4;
	}

	static void assignOrProc() throws IOException {
		char name = getName();
		switch (typeOf(name)) {
		case ' ':
			undefined(String.valueOf(name));
			break;
		case 'v':
		case 'f':
			assignment(name);
			break;
		case 'p':
			callProc(name);
			break;
		default:
			abort("Identifier " + name + " Cannot Be Used Here");
		}
	}

	static void cleanStack(int n) {
		if (n > 0) {
			emit("ADD #");
			System.out.println(n + " ,SP");
		}
	}

	static void callProc(char name) throws IOException {
		int n = paramList();
		call(name);
		cleanStack(n);
	}

	static void call(char name) {
		emitLn("BSR " + name);
	}

	static void procProlog(char n, int k) {
		postLabel(String.valueOf(n));
		emit("LINK A6, #");
		System.out.println(-2*k);
	}

	static void procEpilog() {
		emitLn("UNLK A6");
		emitLn("RTS");
	}

	static void doProcedure() throws IOException {
		char n;
		int k;
		match('p');
		n = getName();
		if (inTable(n)) {
			duplicate(String.valueOf(n));
		}
		ST[n] = 'p';
		formalList();
		k = locDecls();
		procProlog(n, k);
		beginBlock();
		procEpilog();
		clearParams();
	}

	static void Return() {
		emitLn("RTS");
	}

	static void doBlock() throws IOException {
		while (look != 'e') {

			assignOrProc();
			fin();
		}
	}

	static void beginBlock() throws IOException {
		match('b');
		fin();
		doBlock();
		match('e');
		fin();
	}

	static void alloc(char n) {
		if (inTable(n)) {
			duplicate(String.valueOf(n));
		}
		ST[n] = 'v';
		System.out.println(n + ":" + TAB + "DC 0");
	}

	static void decl() throws IOException {
		match('v');
		alloc(getName());
	}

	static void topDecls() throws IOException {
		while (look != '.') {
			switch (look) {
			case 'v':
				decl();
				break;
			case 'p':
				doProcedure();
				break;
			case 'P':
				doMain();
				break;
			default:
				abort("Unrecognized Keyword " + look);
				break;
			}
			fin();
		}
	}
	
	static void locDecl() throws IOException {
		match('v');
		addParam(getName());
		fin();
	}
	
	static int locDecls() throws IOException {
		int n = 0;
		while (look == 'v') {
			locDecl();
			n ++;
		}
		return n;
	}

	static void prolog() {
		postLabel("MAIN");
	}

	static void epilog() {
		emitLn("DC WARMST");
		emitLn("END MAIN");
	}

	static void doMain() throws IOException {
		char n;
		match('P');
		n = getName();
		fin();
		if (inTable(n)) {
			duplicate(String.valueOf(n));
		}
		prolog();
		beginBlock();
	}

	public static void main(String[] args) throws IOException {

		System.out.println((int) (' '));
		System.out.println((int) ('\t'));
		System.out.println((int) ('\r'));
		System.out.println((int) ('\n'));
		init();
		topDecls();
		epilog();

	}

}
