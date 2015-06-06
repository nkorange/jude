package jude.app;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author zpf.073@gmail.com
 *
 */
public class Entry {

	static char look;

	static String value;

	static KeywordType keywordType;

	static Map<String, Integer> names = new HashMap<String, Integer>();
	
	static void emit(String s) {
		System.out.print(Constants.TAB + s);
	}

	static void emitLn(String s) {
		emit(s);
		System.out.println();
	}

	static void postLabel(String l) {
		System.out.println(l + ":");
	}

	static boolean isDefined(String name) {
		return names.containsKey(name);
	}

	static void define(String name, int type) {
		names.put(name, type);
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

	static void getChar() throws IOException {
		look = (char) System.in.read();
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

	static void newLine() throws IOException {
		while (look == Constants.CR) {
			getChar();
			if (look == Constants.LF) {
				getChar();
			}
			skipWhite();
		}
	}

	static void getName() throws IOException {
		newLine();
		if (!isAlpha(look)) {
			expected("Name");
		}
		value = "";
		while (isAlNum(look)) {
			value += look;
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

	static void match(char c) throws IOException {
		newLine();
		if (look == c) {
			getChar();
			skipWhite();
		} else {
			expected(String.valueOf(c));
		}
	}

	static void scan() throws IOException {
		getName();
		keywordType = KeywordUtil.findKeyword(value);
	}

	static void init() throws IOException {
		getChar();
		scan();
	}

	static boolean isKeyword(String name) {
		KeywordType type = KeywordUtil.findKeyword(name);
		return type != KeywordType.NONE;
	}

	static void matchKeyword(String name) throws IOException {
		if (!isKeyword(name)) {
			expected("Keyword");
		}
		scan();

	}

	static void program() throws IOException {

		while (isKeyword(value)) {

			switch (keywordType) {

			case INT:
			case BYTE:
			case BOOL:
			case LONG:
			case CHAR:
			case SHORT:
				methodOrDecl();
				break;
			case CLASS:
				doClass();
				break;
			default:
				expected("type or class");
				break;
			}

			scan();
		}
	}

	static void doMethod(String name, String type) {

	}

	static void doDecl(String name, String type) throws IOException {

		allocaVar(name, type);
		if (look == '=') {
			match('=');
			
		}
	}
	
	static void allocaVar(String name, String type) {
		emitLn("allocate " + type + " " + name);
	}

	static void methodOrDecl() throws IOException {

		
		String type = value;
		String name = null;
		matchKeyword(value);
		name = value;
		if (isDefined(name)) {
			duplicate(name);
		}

		switch (look) {
		case '(':
			define(name, Constants.METHOD);
			doMethod(name, type);
			break;
		case ';':
		case '=':
			define(name, Constants.VARIABLE);
			doDecl(name, type);
			break;
		default:
			expected("(, ; or =");
			break;
		}
		
		getChar();
	}

	static void doClass() {

	}
	
	static void loadConst(int n) {
		emit("MOVE #");
		System.out.println(n + " D0");
	}
	
	static void assignment() throws IOException {
		match('=');
		expression();
		store(value);
	}
	
	static void loadVar(String name) {
		if (!inTable(name)) {
			undefined(name);
		}
		emitLn("MOVE " + name + "(PC), D0");
	}
	
	static void factor() throws IOException {
		if (look == '(') {
			match(')');
			expression();
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
	
	static void negate() {
		emitLn("NEG D0");
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
	
	static void term() throws IOException {
		factor();
		term1();
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
	
	static void firstTerm() throws IOException {
		firstFactor();
		term1();
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

	public static void main(String[] args) throws IOException {

		init();
		program();
	}

}
