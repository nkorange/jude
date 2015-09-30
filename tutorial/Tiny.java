import java.io.IOException;

/**
 * 
 * @author zpf.073@gmail.com
 * 
 */
public class Tiny {

	static char look;

	static int lCount;

	static String symbol;

	static String[] symTab = new String[1000];

	static char token;

	static String value;

	static final String TAB = "\t";

	static final char CR = '\r';

	static final char LF = '\n';

	static final int NKW = 9;

	static final int NKW1 = 10;

	static final String[] KWList = { "IF", "ELSE", "ENDIF", "WHILE",
			"ENDWHILE", "READ", "WRITE", "VAR", "BEGIN", "END", "PROGRAM", "BREAK", "SWITCH", "ENDSWITCH", "CASE", "ENDCASE" };

	static final String KWCode = "xileweRWvbepksece";

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
		getName();
		token = KWCode.charAt(lookUp(value));
	}

	static void matchString(String x) {
		if (!value.equals(x)) {
			expected(x);
		}
	}

	static void match(char c) throws IOException {
		newLine();
		if (look == c) {
			getChar();
			skipWhite();
		} else {
			expected(String.valueOf(c));
		}
	}

	static String newLabel() {
		String s = String.valueOf(lCount);
		lCount++;
		return "L" + s;
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

	static boolean isAlNum(char c) {
		return (isAlpha(c) || isDigit(c));
	}

	static boolean inTable(String s) {
		for (int i = 0; i < ST.length; i++) {
			if (ST[i].equals(s)) {
				return true;
			}
		}
		return false;
	}

	static void addEntry(String s, char t) {
		if (inTable(s)) {
			abort("Duplicate Identifier " + s);
		}
		if (NEntry == MAXENTRY) {
			abort("Symbol Table Full");
		}
		NEntry++;
		ST[NEntry] = s;
		SType[NEntry] = t;
	}

	static void getName() throws IOException {
		newLine();
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

	static int getNum() throws IOException {
		int val = 0;
		newLine();
		if (!isDigit(look)) {
			expected("Integer");
		}
		while (isDigit(look)) {
			val = 10 * val + look - '0';
			getChar();
		}
		skipWhite();
		return val;
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

	static void init() throws IOException {

		for (int i = 0; i <= MAXENTRY; i++) {
			ST[i] = " ";
		}

		getChar();
		scan();
	}

	static void prog() throws IOException {
		matchString("PROGRAM");
		header();
		topDecls();
		__main();
		match('.');
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

	static void alloc(String name) throws IOException {

		if (inTable(name)) {
			abort("Duplicate Variable Name " + name);
		}
		addEntry(name, 'v');
		System.out.print(name + ":" + TAB + "DC ");
		if (look == '=') {
			match('=');
			if (look == '-') {
				System.out.print(look);
				match('-');
			}
			System.out.println(getNum());
		} else {
			System.out.println(0);
		}
	}

	static void decl() throws IOException {
		getName();
		alloc(value);
		while (look == ',') {
			match(',');
			getName();
			alloc(value);
		}
	}

	static void topDecls() throws IOException {
		scan();
		while (token != 'b') {
			switch (token) {
			case 'v':
				decl();
				break;
			default:
				abort("Unrecognized Keyword " + look);
			}
			scan();
		}
	}

	static void block() throws IOException {
		scan();
		while (token != 'e' && token != 'l' && token != 'k') {
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
			case 's':
				doSwitch();
				break;
			default:
				assignment();
			}
			scan();
		}
	}

	static void __main() throws IOException {
		matchString("BEGIN");
		prolog();
		block();
		matchString("END");
		epilog();
	}

	static void clear() {
		emitLn("CLR D0");
	}

	static void negate() {
		emitLn("NEG D0");
	}

	static void loadConst(int n) {
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
	
	static void pop() {
		emitLn("MOVE (SP)+, D0");
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

	static void store(String name) {
		if (!inTable(name)) {
			undefined(name);
		}
		emitLn("LEA " + name + "(PC), A0");
		emitLn("MOVE D0, (A0)");
	}

	static void factor() throws IOException {
		if (look == '(') {
			match(')');
			boolExpression();
			match(')');
		} else if (isAlpha(look)) {
			getName();
			loadVar(value);
		} else {
			loadConst(getNum());
		}
	}

	static void negFactor() throws IOException {
		match('-');
		if (isDigit(look)) {
			loadConst(-getNum());
		} else {
			factor();
			negate();
		}
	}

	static void firstFactor() throws IOException {
		switch (look) {
		case '+':
			match('+');
			factor();
			break;
		case '-':
			negFactor();
			break;
		default:
			factor();
		}
	}

	static void multiply() throws IOException {
		match('*');
		factor();
		popMul();
	}

	static void divide() throws IOException {
		match('/');
		factor();
		popDiv();
	}

	static void term1() throws IOException {
		while (isMulOp(look)) {
			push();
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
		firstFactor();
		term1();
	}

	static void add() throws IOException {
		match('+');
		term();
		popAdd();
	}

	static void substract() throws IOException {
		match('-');
		term();
		popSub();
	}

	static void expression() throws IOException {
		firstTerm();
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

	static void assignment() throws IOException {
		match('=');
		boolExpression();
		store(value);
	}

	static boolean isOrOp(char c) {
		return (c == '|' || c == '~');
	}

	static boolean isRelOp(char c) {
		return (c == '=' || c == '#' || c == '<' || c == '>');
	}

	static void notIt() {
		emitLn("NOT D0");
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

	static void equals() throws IOException {
		match('=');
		expression();
		popCompare();
		setEqual();
	}

	static void notEqual() throws IOException {
		match('>');
		expression();
		popCompare();
		setNEqual();
	}

	static void less() throws IOException {
		match('<');
		switch (look) {
		case '=':
			lessOrEqual();
			break;
		case '>':
			notEqual();
			break;
		default:
			expression();
			popCompare();
			setLess();
			break;
		}

	}

	static void greater() throws IOException {
		match('>');
		if (look == '=') {
			match('=');
			expression();
			popCompare();
			setGreaterOrEqual();
		} else {
			expression();
			popCompare();
			setGreater();
		}
	}

	static void lessOrEqual() throws IOException {
		match('=');
		expression();
		popCompare();
		setLessOrEqual();
	}

	static void relation() throws IOException {
		expression();
		if (isRelOp(look)) {
			push();
			switch (look) {
			case '=':
				equals();
				break;
			case '#':
				notEqual();
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
		if (look == '!') {
			match('!');
			relation();
			notIt();
		} else {
			relation();
		}
	}

	static void boolTerm() throws IOException {
		notFactor();
		while (look == '&') {
			push();
			match('&');
			notFactor();
			popAnd();
		}
	}

	static void boolOr() throws IOException {
		match('|');
		boolTerm();
		popOr();
	}

	static void boolXor() throws IOException {
		match('~');
		boolTerm();
		popXor();
	}

	static void boolExpression() throws IOException {
		boolTerm();
		while (isOrOp(look)) {
			push();
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

	static void branch(String l) {
		emitLn("BRA " + l);
	}

	static void branchFalse(String l) {
		emitLn("TST D0");
		emitLn("BEQ " + l);
	}

	static void doIf() throws IOException {
		String l1, l2;
		boolExpression();
		l1 = newLabel();
		l2 = l1;
		branchFalse(l1);
		block();
		if (token == '|') {
			l2 = newLabel();
			branch(l2);
			postLabel(l1);
			block();
		}
		postLabel(l2);
		matchString("ENDIF");
	}
	
	static void doSwitch() throws IOException {
		String l1 = newLabel();
		String valName;
		getName();
		loadVar(value);
		push();
		valName = value;
		getName();
		while (!"ENDSWITCH".equals(value)) {
			if ("DEFAULT".equals(value)) {
				doDefaultCase(l1);
			} else if ("CASE".equals(value)) {
				String l = newLabel();
				doCase(l, l1, valName);
				postLabel(l);
			} else {
				expected("CASE or DEFAULT");
			}
			getName();
		}
		matchString("ENDSWITCH");
		postLabel(l1);
	}
	
	static void doCase(String l, String l1, String valName) throws IOException {
		
		int valueOfCase = getNum();
		emitLn("MOVE #" + valueOfCase +", D0");
		match(':');
		popCompare();
		loadVar(valName);
		push();
		setEqual();
		branchFalse(l);
		block();
		
		if ("BREAK".equals(value)) {
			branch(l1);
			getName();
		}
		matchString("ENDCASE");
	}
	
	static void doDefaultCase(String l1) throws IOException {
		match(':');
		block();
		if ("BREAK".equals(value)) {
			branch(l1);
			getName();
		}
		matchString("ENDCASE");
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
	
	static void doRead() throws IOException {
		match('(');
		getName();
		readVar();
		while (look == ',') {
			match(',');
			getName();
			readVar();
		}
		match(')');
	}
	
	static void doWrite() throws IOException {
		match('(');
		expression();
		writeVar();
		while (look == ',') {
			match(',');
			expression();
			writeVar();
		}
		match(')');
	}

	static void newLine() throws IOException {
		while (look == CR) {
			getChar();
			if (look == LF) {
				getChar();
			}
			skipWhite();
		}
	}
	
	static void readVar() {
		emitLn("BSR READ");
		store(value);
	}
	
	static void writeVar() {
		emitLn("BSR WRITE");
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		init();
		prog();
		if (look != CR) {
			abort("Unexpectd data after '.'");
		}
	}

}
