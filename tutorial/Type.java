import java.io.IOException;

/**
 * 
 * @author zpf.073@gmail.com
 * 
 */
public class Type {

	static final char TAB = '\t';

	static final char CR = '\r';

	static final char LF = '\n';

	static char look;

	static char ST[] = new char['Z' + 1];

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

	static void clear() {
		emitLn("CLR D0");
	}

	static void dumpTable() {
		for (char i = 'A'; i <= 'Z'; i++) {
			if (ST[i] != '?') {
				System.out.println(i + " " + ST[i]);
			}
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

	static void duplicate(String name) {
		abort("Duplicate Identifier " + name);
	}

	static boolean inTable(char n) {
		return (ST[n] != '?');
	}

	static void addEntry(char name, char t) {
		if (inTable(name)) {
			duplicate(String.valueOf(name));
		}
		ST[name] = t;
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

	static long getNum() throws IOException {
		long var = 0;
		if (!isDigit(look)) {
			expected("Integer");
		}
		while (isDigit(look)) {
			var = 10 * var + look - '0';
			getChar();
		}
		// getChar();
		skipWhite();
		return var;
	}

	static void emit(String s) {
		System.out.print(TAB + s);
	}

	static void emitLn(String s) {
		emit(s);
		System.out.println();
	}

	static void allocVar(char n, char t) {
		System.out.println(n + ":" + TAB + "DC. " + t + " 0");
	}

	static void alloc(char n, char t) {
		addEntry(n, t);
		allocVar(n, t);
	}

	static void convert(char source, char dest) {
		if (source != dest) {
			if (source == 'B') {
				emitLn("AND.W #$FF, D0");
			}
			if (dest == 'L') {
				emitLn("EXT.L D0");
			}
		}
	}

	static char load(char name) {
		char type = varType(name);
		loadVar(name, varType(name));
		return type;
	}

	static void loadVar(char name, char type) {
		move(type, name + "(PC)", "D0");
	}

	static char loadNum(long n) {
		char type;
		if (Math.abs(n) <= 127) {
			type = 'B';
		} else if (Math.abs(n) <= 32767) {
			type = 'W';
		} else {
			type = 'L';
		}
		loadConst(n, type);
		return type;
	}

	static void loadConst(long n, char type) {

		move(type, "#" + n, "D0");

	}

	static void storeVar(char name, char type) {
		emitLn("LEA " + name + "(PC), A0");
		move(type, "D0", "(A0)");
	}

	static void store(char name, char t1) {
		char t2 = varType(name);
		convert(t1, t2);
		storeVar(name, t2);
	}

	static void move(char size, String source, String dest) {
		emitLn("MOVE." + size + " " + source + "," + dest);
	}

	static boolean isVarType(char c) {
		return (c == 'B' || c == 'W' || c == 'L');
	}

	static char varType(char name) {
		// FIXME char type = TypeOf(name)
		if (!isVarType(ST[name])) {
			abort("Identifier " + name + " is not a variable");
		}
		return ST[name];
	}

	static void decl() throws IOException {
		char type = getName();
		alloc(getName(), type);
	}

	static void topDecls() throws IOException {
		while (look != 'B') {
			switch (look) {
			case 'b':
			case 'w':
			case 'l':
				decl();
				break;
			default:
				abort("Unrecognized Keyword " + look);
				break;
			}
			fin();
		}
	}

	static char term() throws IOException {

		char type = factor();
		while (isMulOp(look)) {
			push(type);
			switch (look) {
			case '*':
				type = multiply(type);
				break;
			case '/':
				type = divide(type);
				break;
			default:
				break;
			}
		}
		return type;
	}

	static char factor() throws IOException {
		char res;
		if (look == '(') {
			match('(');
			res = expression();
			match(')');
		} else if (isAlpha(look)) {
			res = load(getName());
		} else {
			res = loadNum(getNum());
		}
		return res;
	}

	static char expression() throws IOException {
		char type;
		if (isAddOp(look)) {
			type = unOp();
		} else {
			type = term();
		}
		while (isAddOp(look)) {
			push(type);
			switch (look) {
			case '+':
				type = add(type);
				break;
			case '-':
				type = subtract(type);
				break;
			}
		}
		return type;
	}

	static char unOp() {
		clear();
		return 'W';
	}

	static void pop(char size) {
		move(size, "(SP)+", "D7");
	}

	static void push(char size) {
		move(size, "D0", "-(SP)");
	}

	static char add(char t1) throws IOException {
		match('+');
		return popAdd(t1, term());
	}

	static char subtract(char t1) throws IOException {
		match('-');
		return popSub(t1, term());
	}

	static char multiply(char t1) throws IOException {
		match('*');
		return popMul(t1, factor());
	}

	static char divide(char t1) throws IOException {
		match('/');
		return popDiv(t1, factor());
	}

	static void convert(char source, char dest, String reg) {
		if (source != dest) {
			if (source == 'B') {
				emitLn("AND.W #$FF, " + reg);
			}
			if (dest == 'L') {
				emitLn("EXT.L " + reg);
			}
		}
	}

	static char promote(char t1, char t2, String reg) {
		char type = t1;
		if (t2 != t1) {
			if (t1 == 'B' || (t1 == 'W' && t2 == 'L')) {
				convert(t1, t2, reg);
				type = t2;
			}
		}
		return type;
	}

	static char sameType(char t1, char t2) {
		t1 = promote(t1, t2, "D7");
		return promote(t2, t1, "D0");
	}

	static char popAdd(char t1, char t2) {
		pop(t1);
		t2 = sameType(t1, t2);
		genAdd(t2);
		return t2;
	}

	static char popSub(char t1, char t2) {
		pop(t1);
		t2 = sameType(t1, t2);
		genSub(t2);
		return t2;
	}

	static char popMul(char t1, char t2) {
		char t;
		pop(t1);
		t = sameType(t1, t2);
		convert(t, 'W', "D7");
		convert(t, 'W', "D0");
		if (t == 'L') {
			genLongMult();
		} else {
			genMult();
		}

		if (t == 'B') {
			return 'W';
		} else {
			return 'L';
		}
	}

	static char popDiv(char t1, char t2) {
		pop(t1);
		convert(t1, 'L', "D7");
		if (t1 == 'L' || t2 == 'L') {
			convert(t2, 'L', "D0");
			genLongDiv();
			return 'L';
		} else {
			convert(t2, 'W', "D0");
			genDiv();
			return t1;
		}
	}

	static void genAdd(char size) {
		emitLn("ADD." + size + "D7, D0");
	}

	static void genSub(char size) {
		emitLn("SUB." + size + " D7, D0");
		emitLn("NEG." + size + " D0");
	}

	static void genMult() {
		emitLn("MULS D7, D0");
	}

	static void genLongMult() {
		emitLn("JSR MUL32");
	}

	static void genDiv() {
		emitLn("DIVS D0, D7");
		move('W', "D7", "D0");
	}

	static void genLongDiv() {
		emitLn("JSR DIV32");
	}

	static void init() throws IOException {
		char i;
		for (i = 'A'; i <= 'Z'; i++) {
			ST[i] = '?';
		}
		getChar();
	}

	static void assignment() throws IOException {
		char name = getName();
		match('=');
		store(name, expression());
	}

	static void block() throws IOException {
		while (look != '.') {
			assignment();
			fin();
		}
	}

	public static void main(String[] args) throws IOException {
		init();
		topDecls();
		match('B');
		fin();
		block();
		dumpTable();
	}
}
