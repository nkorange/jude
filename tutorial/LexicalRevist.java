import java.io.IOException;

/**
 * Source code in chapter 11, establish TINY 1.1
 * 
 * @author pengfei.zhu
 * 
 */
public class LexicalRevist {

	static char look;

	static int lCount;

	static String symbol;

	static String[] symTab = new String[1000];

	static char token;

	static String value;

	static final char TAB = '\t';

	static final char CR = '\r';

	static final char LF = '\n';

	static final int NKW = 9;

	static final int NKW1 = 10;

	static final String[] KWList = { "IF", "ELSE", "ENDIF", "WHILE",
			"ENDWHILE", "READ", "WRITE", "VAR", "BEGIN", "END", "PROGRAM" };

	static final String KWCode = "xileweRWvbep";

	static final int MAXENTRY = 100;

	static String[] ST = new String[MAXENTRY + 1];

	static char[] SType = new char[MAXENTRY + 1];

	static int NEntry = 0;

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

	static void checkIdent() {
		if (token != 'x') {
			expected("Identifier");
		}
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

	static boolean isAlNum(char c) {
		return (isAlpha(c) || isDigit(c));
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

	static boolean isOrOp(char c) {
		return (c == '|' || c == '~');
	}

	static boolean isRelOp(char c) {
		return (c == '=' || c == '#' || c == '<' || c == '>');
	}

	static boolean isWhite(char c) {
		if (c == ' ' || c == TAB || c == CR || c == LF) {
			return true;
		}
		return false;
	}

	static void skipWhite() throws IOException {
		while (isWhite(look)) {
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

	static int locate(String s) {
		for (int i = 0; i < ST.length; i++) {
			if (ST[i].equals(s)) {
				return i + 1;
			}
		}
		return 0;
	}

	static boolean inTable(String s) {
		for (int i = 0; i < ST.length; i++) {
			if (ST[i].equals(s)) {
				return true;
			}
		}
		return false;
	}

	static void checkTable(String s) {
		if (!inTable(s)) {
			undefined(s);
		}
	}

	static void checkDup(String s) {
		if (inTable(s)) {
			duplicate(s);
		}
	}

	static void addEntry(String s, char t) {
		checkDup(s);
		if (NEntry == MAXENTRY) {
			abort("Symbol Table Full");
		}
		NEntry++;
		ST[NEntry] = s;
		SType[NEntry] = t;
	}

	static void getName() throws IOException {
		skipWhite();
		if (!isAlpha(look)) {
			expected("Name");
		}
		token = 'x';
		value = "";
		while (isAlNum(look)) {
			value += Character.toUpperCase(look);
			getChar();
		}
	}

	static void getNum() throws IOException {
		skipWhite();
		if (!isDigit(look)) {
			expected("Integer");
		}
		token = '#';
		value = "";
		while (isDigit(look)) {
			value += look;
			getChar();
		}
	}

	static void getOp() throws IOException {
		skipWhite();
		token = look;
		value = "" + look;
		getChar();
	}

	static void next() throws IOException {
		skipWhite();
		if (isAlpha(look)) {
			getName();
		} else if (isDigit(look)) {
			getNum();
		} else {
			getOp();
		}
	}

	static void scan() throws IOException {
		if (token == 'x') {
			token = KWCode.charAt(lookUp(value));
		}
	}

	static void matchString(String x) throws IOException {
		if (!value.equals(x)) {
			expected(x);
		}
		next();
	}

	static void emit(String s) {
		System.out.print(TAB + s);
	}

	static void emitLn(String s) {
		emit(s);
		System.out.println();
	}

	static String newLabel() {
		String s = String.valueOf(lCount);
		lCount++;
		return "L" + s;
	}

	static void postLabel(String l) {
		System.out.println(l + ":");
	}

	static void clear() {
		emitLn("CLR D0");
	}

	static void negate() {
		emitLn("NEG D0");
	}

	static void notIt() {
		emitLn("NOT D0");
	}

	static void loadConst(String n) {
		emit("MOVE #");
		System.out.println(n + " D0");
	}

	static void loadVar(String name) {
		if (!inTable(name)) {
			undefined(name);
		}
		emitLn("MOVE " + name + "(PC), D0");
	}

	static void push() {
		emitLn("MOVE D0, -(SP)");
	}

	static void popAdd() {
		emitLn("ADD (SP)+, D0");
	}

	static void popSub() {
		emitLn("SUB (SP)+, D0");
	}

	static void popMul() {
		emitLn("MULS (SP)+, D0");
	}

	static void popDiv() {
		emitLn("MOVE (SP)+, D7");
		emitLn("EXT.L D7");
		emitLn("DIVS D0, D7");
		emitLn("MOVE D7, D0");
	}

	static void popAnd() {
		emitLn("AND (SP)+, D0");
	}

	static void popOr() {
		emitLn("OR (SP)+, D0");
	}

	static void popXor() {
		emitLn("EOR (SP)+, D0");
	}

	static void popCompare() {
		emitLn("CMP (SP)+, D0");
	}

	static void setEqual() {
		emitLn("SEQ D0");
		emitLn("EXT D0");
	}

	static void setNEqual() {
		emitLn("SNE D0");
		emitLn("EXT D0");
	}

	static void setGreater() {
		emitLn("SLT D0");
		emitLn("EXT D0");
	}

	static void setLess() {
		emitLn("SGT D0");
		emitLn("EXT D0");
	}

	static void setLessOrEqual() {
		emitLn("SGE D0");
		emitLn("EXT D0");
	}

	static void setGreaterOrEqual() {
		emitLn("SLE D0");
		emitLn("EXT D0");
	}

	static void store(String name) {
		emitLn("LEA " + name + "(PC), A0");
		emitLn("MOVE D0, (A0)");
	}

	static void branch(String l) {
		emitLn("BRA " + l);
	}

	static void branchFalse(String l) {
		emitLn("TST D0");
		emitLn("BEQ " + l);
	}

	static void readIt(String name) {
		emitLn("BSR READ");
		store(name);
	}

	static void writeIt() {
		emitLn("BSR WRITE");
	}

	static void header() {
		System.out.println("WARMST" + TAB + "EQU $A01E");
		emitLn("LIB TINYLIB");
	}

	static void prolog() {
		postLabel("MAIN");
	}

	static void epilog() {
		emitLn("DC WARMST");
		emitLn("END MAIN");
	}

	static void allocate(String name, String val) {
		System.out.println(name + ":" + TAB + "DC " + val);
	}

	static void factor() throws IOException {
		if (token == '(') {
			next();
			boolExpression();
			matchString(")");
		} else if (token == 'x') {
			loadVar(value);
		} else if (token == '#') {
			loadConst(value);
		} else {
			expected("Math Factor");
		}
		next();
	}

	static void multiply() throws IOException {
		next();
		factor();
		popMul();
	}

	static void divide() throws IOException {
		next();
		factor();
		popDiv();
	}

	static void term() throws IOException {
		factor();
		while (isMulOp(token)) {
			push();
			switch (token) {
			case '*':
				multiply();
				break;
			case '/':
				divide();
				break;
			}
		}
	}

	static void add() throws IOException {
		next();
		term();
		popAdd();
	}

	static void substract() throws IOException {
		next();
		term();
		popSub();
	}

	static void expression() throws IOException {
		if (isAddOp(look)) {
			clear();
		} else {
			term();
		}
		while (isAddOp(look)) {
			push();
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

	static void compareExpression() throws IOException {
		expression();
		popCompare();
	}

	static void nextExpression() throws IOException {
		next();
		compareExpression();
	}

	static void equals() throws IOException {
		nextExpression();
		setEqual();
	}

	static void lessOrEqual() throws IOException {
		nextExpression();
		setLessOrEqual();
	}

	static void notEqual() throws IOException {
		nextExpression();
		setNEqual();
	}

	static void less() throws IOException {
		next();
		switch (token) {
		case '=':
			lessOrEqual();
			break;
		case '>':
			notEqual();
			break;
		default:
			compareExpression();
			setLess();
			break;
		}

	}

	static void greater() throws IOException {
		next();
		if (token == '=') {
			nextExpression();
			setGreaterOrEqual();
		} else {
			compareExpression();
			setGreater();
		}
	}

	static void relation() throws IOException {
		expression();
		if (isRelOp(token)) {
			push();
			switch (token) {
			case '=':
				equals();
				break;
			case '<':
				less();
				break;
			case '>':
				greater();
				break;
			}
		}
	}

	static void notFactor() throws IOException {
		if (token == '!') {
			next();
			relation();
			notIt();
		} else {
			relation();
		}
	}

	static void boolTerm() throws IOException {
		notFactor();
		while (token == '&') {
			push();
			next();
			notFactor();
			popAnd();
		}
	}

	static void boolOr() throws IOException {
		next();
		boolTerm();
		popOr();
	}

	static void boolXor() throws IOException {
		next();
		boolTerm();
		popXor();
	}

	static void boolExpression() throws IOException {
		boolTerm();
		while (isOrOp(token)) {
			push();
			switch (token) {
			case '|':
				boolOr();
				break;
			case '~':
				boolXor();
				break;
			}
		}
	}

	static void assignment() throws IOException {
		String name;
		checkTable(value);
		name = value;
		next();
		matchString("=");
		boolExpression();
		store(name);
	}

	static void doIf() throws IOException {
		String l1, l2;
		next();
		boolExpression();
		l1 = newLabel();
		l2 = l1;
		branchFalse(l1);
		block();
		if (token == 'l') {
			next();
			l2 = newLabel();
			branch(l2);
			postLabel(l1);
			block();
		}
		postLabel(l2);
		matchString("ENDIF");
	}

	static void doWhile() throws IOException {
		String l1, l2;
		l1 = newLabel();
		l2 = newLabel();
		postLabel(l1);
		boolExpression();
		branchFalse(l2);
		block();
		matchString("ENDWHILE");
		branch(l1);
		postLabel(l2);
	}

	static void readVar() throws IOException {
		checkIdent();
		checkTable(value);
		readIt(value);
		next();
	}

	static void doRead() throws IOException {
		next();
		matchString("(");
		readVar();
		while (token == ',') {
			next();
			readVar();
		}
		matchString(")");
	}

	static void doWrite() throws IOException {
		next();
		matchString("(");
		expression();
		writeIt();
		while (token == ',') {
			next();
			expression();
			writeIt();
		}
		matchString(")");
	}

	/**
	 * program block implementation
	 * 
	 * @throws IOException
	 */
	static void block() throws IOException {
		scan();
		while (token != 'e' && token != 'l') {
			switch (token) {
			case 'i':
				doIf();
				break;
			case 'w':
				doWhile();
				break;
			case 'R':
				doRead();
				break;
			case 'W':
				doWrite();
				break;
			default:
				assignment();
			}
			scan();
		}
	}

	/**
	 * add new variable and allocate default value
	 * 
	 * @throws IOException
	 */
	static void alloc() throws IOException {

		next();
		if (token != 'x') {
			expected("Variable Name");
		}
		checkDup(value);
		addEntry(value, 'v');
		allocate(value, "0");
		next();
	}

	/**
	 * variable declaration at top of program
	 * 
	 * @throws IOException
	 */
	static void topDecls() throws IOException {
		scan();
		while (token == 'v') {
			alloc();
			while (token == ',') {
				alloc();
			}
		}
	}

	static void init() throws IOException {

		for (int i = 0; i <= MAXENTRY; i++) {
			ST[i] = " ";
		}

		getChar();
		next();
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		init();
		matchString("PROGRAM");
		header();
		topDecls();
		matchString("BEGIN");
		prolog();
		block();
		matchString("END");
		epilog();
	}

}
