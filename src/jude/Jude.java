package jude;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * The Jude language, support x86_64 platform only.
 * 
 * @author zpf.073@gmail.com
 *
 */
public class Jude {

	static final String TAB = "\t";
	static final char CR = '\r';
	static final char LF = '\n';

	static final int _CLASS = 1;
	static final int _VARIABLE = 2;
	static final int _METHOD = 3;

	static char look;

	static String value;

	static type keyType;

	static Map<String, String> globalVariables = new HashMap<String, String>();

	static Map<String, String> localVariables = new HashMap<String, String>();

	static Map<String, String> methods = new HashMap<String, String>();

	// static Map<String, Integer> names = new HashMap<String, Integer>();

	static enum type {
		VOID, INT, LONG, BYTE, SHORT, BOOL, CHAR, IF, ELSE, WHILE, FOR, IN, AS, SWITCH, CASE, CLASS,

		NONE, END
	};

	/**
	 * Keywords definition
	 */
	public static final String INT = "int";
	public static final String LONG = "long";
	public static final String BYTE = "byte";
	public static final String SHORT = "short";
	public static final String BOOL = "bool";
	public static final String CHAR = "char";

	public static final String IF = "if";
	public static final String ELSE = "else";
	public static final String WHILE = "while";
	public static final String FOR = "for";
	public static final String IN = "in";
	public static final String AS = "as";
	public static final String SWITCH = "switch";
	public static final String CASE = "case";

	public static final String CLASS = "class";

	private static Map<String, type> keywords;
	static {

		keywords = new HashMap<String, type>();
		keywords.put(INT, type.INT);
		keywords.put(LONG, type.LONG);
		keywords.put(BYTE, type.BYTE);
		keywords.put(SHORT, type.SHORT);
		keywords.put(BOOL, type.BOOL);
		keywords.put(CHAR, type.CHAR);
		keywords.put(IF, type.IF);
		keywords.put(ELSE, type.ELSE);
		keywords.put(WHILE, type.WHILE);
		keywords.put(FOR, type.FOR);
		keywords.put(IN, type.IN);
		keywords.put(AS, type.AS);
		keywords.put(SWITCH, type.SWITCH);
		keywords.put(CASE, type.CASE);
		keywords.put(CLASS, type.CLASS);
	}

	public static type findKeyword(String name) {

		if (keywords.containsKey(name)) {
			return keywords.get(name);
		}
		return type.NONE;
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

	static boolean isDefinedGlobalVar(String name) {
		return globalVariables.containsKey(name);
	}

	static boolean isDefinedLocalVar(String name) {
		return localVariables.containsKey(name);
	}

	static boolean isDefinedMethod(String name) {
		return methods.containsKey(name);
	}

	static void defineGlobalVar(String name, String type) {
		globalVariables.put(name, type);
	}

	static void defineLocalVar(String name, String type) {
		localVariables.put(name, type);
	}

	static void defineMethod(String name, String type) {
		methods.put(name, type);
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

	static String getStoreType(type varType) {
		switch (varType) {
		case BOOL:
		case CHAR:
		case BYTE:
			return "db";
		case SHORT:
			return "dw";
		case INT:
			return "dd";
		case LONG:
			return "dq";
		default:
			abort("illegal type:" + varType);
		}
		return null;
	}

	static String defaultValue(type varType) {
		return "0";
	}

	static String boolNumeric(String boolValue) {
		if (boolValue.equals("true")) {
			return "0";
		} else {
			return "1";
		}
	}

	static boolean isNum(String str) {
		try {
			Long.parseLong(str);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	static boolean isName(String str) {
		// TODO check if str is a legal variable name
		return true;
	}

	static boolean isBool(String str) {
		return str.equals("true") || str.equals("false");
	}

	static boolean isChar(String str) {
		return str.length() == 1;
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
		while (look == CR) {
			getChar();
			if (look == LF) {
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

	static String getStr() throws IOException {
		newLine();
		String str = "";
		while (!isWhite(look)) {
			str += look;
			getChar();
		}
		skipWhite();
		return str;

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
		keyType = findKeyword(value);
	}

	static void init() throws IOException {
		getChar();
		scan();
	}

	static boolean isKeyword(String name) {
		type keyType = findKeyword(name);
		return keyType != type.NONE;
	}

	static void matchKeyword(String name) throws IOException {
		if (!isKeyword(name)) {
			expected("Keyword");
		}
		scan();

	}

	static void program() throws IOException {

		while (isKeyword(value)) {

			switch (keyType) {
			case VOID:
			case INT:
			case BYTE:
			case BOOL:
			case LONG:
			case CHAR:
			case SHORT:
				doMethod();
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

	static void topDecls() throws IOException {

		emitLn("section .data");
		while (isKeyword(value)) {

			switch (keyType) {
			case INT:
			case BYTE:
			case BOOL:
			case LONG:
			case CHAR:
			case SHORT:
				DeclVar();
				break;
			default:
				abort("illegal type:" + keyType);
				break;
			}

			scan();
		}
	}

	static void doMethod(String name, String type) {

	}

	static void doDeclVar(String name, String type) throws IOException {
		allocaGlobalVar(name, type);
	}

	// This method allocate global variables
	static void allocaGlobalVar(String name, String type) throws IOException {
		// TODO add real allocation.
		// now we start a real allocation with nasm:
		String varValue = null;
		if (look == '=') {
			match('=');
			// get the initial value:
			varValue = getStr();
		} else {
			match(';');
		}

		switch (findKeyword(type)) {
		case BYTE:
		case SHORT:
		case INT:
		case LONG:
			if (!isNum(varValue)) {
				expected("numberic value");
			}
			break;
		case BOOL:
			if (!isBool(varValue)) {
				expected("bool value");
			}
			varValue = boolNumeric(varValue);
			break;
		case CHAR:
			if (!isChar(varValue)) {
				expected("character value");
			}
			break;
		default:
			abort("undefined type:" + type);
			break;
		}
		if (varValue != null) {
			emitLn(name + ":" + TAB + getStoreType(findKeyword(type)) + TAB
					+ varValue);
		} else {
			emitLn(name + ":" + TAB + getStoreType(findKeyword(type)) + TAB
					+ defaultValue(findKeyword(type)));
		}

	}

	static void doMethod() throws IOException {

		String type = value;
		String name = null;
		matchKeyword(value);
		name = value;
		if (isDefinedMethod(name)) {
			duplicate(name);
		}

		switch (look) {
		case '(':
			defineMethod(name, type);
			doMethod(name, type);
			break;
		default:
			expected("(");
			break;
		}

		getChar();
	}

	static void DeclVar() throws IOException {
		String type = value;
		String name = null;
		matchKeyword(value);
		name = value;
		if (isDefinedGlobalVar(name)) {
			duplicate(name);
		}

		switch (look) {
		case ';':
		case '=':
			defineGlobalVar(name, type);
			doDeclVar(name, type);
			break;
		default:
			expected("; or =");
			break;
		}

		getChar();
	}

	static void doClass() {

	}

	static void loadConst(int n) {
		emit("MOVE RO, #");
		System.out.println(n);
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
