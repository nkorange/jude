import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * The Jude language, supports x86_64 platform only and currently does not
 * support the following features:
 * <p>
 * 1. pointer type <br>
 * 2. array type <br>
 * 3. reference type <br>
 * 4. user defined type including List, Map, Structure, Class etc.<br>
 * 5. a statement starts with "(".<br>
 * 6. return in a block, even in a procedure.
 * 
 * 
 * @author zpf.073@gmail.com
 *
 */
public class Jude {

	static final String TAB = "\t";
	static final char CR = '\r';
	static final char LF = '\n';

	static char look;

	static String value;

	static Type keyType;

	static String lastName = null;

	static String lastType = null;

	static Map<String, String> globalVariables = new HashMap<String, String>();

	static Map<String, ParamInfo> localVariables = new HashMap<String, ParamInfo>();

	static Map<String, String> methods = new HashMap<String, String>();

	static Map<String, ParamInfo> params = new HashMap<String, ParamInfo>();

	static int currentOffset = 8;

	// static Map<String, Integer> names = new HashMap<String, Integer>();

	static enum Type {
		VOID, INT, LONG, BYTE, SHORT, BOOL, CHAR, IF, ELSE, WHILE, FOR, IN, AS, SWITCH, CASE, CLASS,

		NONE, END, VAR, PROC, RETURN
	};

	static class ParamInfo {
		int offset;
		int size;
		Type type;
	}

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

	private static Map<String, Type> keywords;
	static {

		keywords = new HashMap<String, Type>();
		keywords.put(INT, Type.INT);
		keywords.put(LONG, Type.LONG);
		keywords.put(BYTE, Type.BYTE);
		keywords.put(SHORT, Type.SHORT);
		keywords.put(BOOL, Type.BOOL);
		keywords.put(CHAR, Type.CHAR);
		keywords.put(IF, Type.IF);
		keywords.put(ELSE, Type.ELSE);
		keywords.put(WHILE, Type.WHILE);
		keywords.put(FOR, Type.FOR);
		keywords.put(IN, Type.IN);
		keywords.put(AS, Type.AS);
		keywords.put(SWITCH, Type.SWITCH);
		keywords.put(CASE, Type.CASE);
		keywords.put(CLASS, Type.CLASS);
	}

	static Map<String, Type> types;
	static {
		types = new HashMap<String, Type>();
		types.put(INT, Type.INT);
		types.put(LONG, Type.LONG);
		types.put(BYTE, Type.BYTE);
		types.put(SHORT, Type.SHORT);
		types.put(BOOL, Type.BOOL);
		types.put(CHAR, Type.CHAR);
	}

	static final long MAX_STACK_SIZE = 128;

	static Type findKeyword(String name) {

		if (keywords.containsKey(name)) {
			return keywords.get(name);
		}
		
		if (isLegalVar(name)) {
			return Type.VAR;
		}
		
		if (isReturn(name)) {
			return Type.RETURN;
		}
		
		if (isDefinedMethod(name)) {
			return Type.PROC;
		}

		return Type.NONE;
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

	static void postMethodStartLabel(String name) {
		System.out.println("start_" + name + ":");
	}

	static void postMethodEndLabel(String name) {
		System.out.println("end_" + name + ":");
	}
	
	static boolean isReturn(String name) {
		return "return".equals(name);
	}

	static boolean isLegalVar(String name) {
		return isDefinedGlobalVar(name) || isDefinedLocalVar(name)
				|| isParam(name);
	}

	static boolean isDefinedGlobalVar(String name) {
		return globalVariables.containsKey(name);
	}

	static boolean isDefinedLocalVar(String name) {
		return localVariables.containsKey(name) || params.containsKey(name);
	}

	static boolean isDefinedMethod(String name) {
		return methods.containsKey(name);
	}

	static void defineGlobalVar(String name, String type) {
		globalVariables.put(name, type);
	}

	static void defineLocalVar(String name, Type type, int offset) {
		ParamInfo info = new ParamInfo();
		info.offset = offset;
		info.type = type;
		info.size = sizeOfType(type);
		localVariables.put(name, info);
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

	static String getStoreType(Type varType) {
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

	static int getLocalVarOffset(String name) {
		return localVariables.get(name).offset;
	}

	static int getParamOffset(String name) {
		return params.get(name).offset;
	}

	static String defaultValue(Type varType) {
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
		while (look == CR || look == LF) {
			getChar();
			if (look == CR || look == LF) {
				getChar();
			}
			skipWhite();
		}
	}

	static String getName() throws IOException {
		newLine();
		if (!isAlpha(look)) {
			expected("Name " + look);
		}
		value = "";
		while (isAlNum(look)) {
			value += look;
			getChar();
		}
		skipWhite();
		return value;
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
		while (!isWhite(look) && look != ';') {
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
		Type keyType = findKeyword(name);
		return keyType != Type.NONE;
	}

	static boolean isType(String type) {
		return types.containsKey(type);
	}

	static Type toType(String type) {
		return types.get(type);
	}

	static int sizeOfType(Type type) {
		switch (type) {
		case BOOL:
		case BYTE:
		case CHAR:
			return 1;
		case SHORT:
			return 2;
		case INT:
			return 4;
		case LONG:
			return 8;
		default:
			abort("Invalid Type:" + type);
		}
		return 0;
	}

	static void matchKeyword(String name) throws IOException {
		if (!isKeyword(name)) {
			expected("Keyword");
		}
		scan();
	}

	static void matchType(String type) throws IOException {
		if (!isType(type)) {
			expected("type");
		}
		scan();
	}

	static void program() throws IOException {

		emitLn("section .text");
		emitLn("global main");

		if (lastName != null) {
			if (isDefinedMethod(lastName)) {
				duplicate(lastName);
			}
			defineMethod(lastName, lastType);
			doMethod(lastName, lastType);
			getChar();
			scan();
		}

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

		boolean endOfDecl = false;
		emitLn("section .data");
		while (isKeyword(value)) {

			switch (keyType) {
			case INT:
			case BYTE:
			case BOOL:
			case LONG:
			case CHAR:
			case SHORT:
				endOfDecl = DeclVar();
				if (!endOfDecl) {
					return;
				}
				break;
			default:
				abort("illegal type:" + keyType);
				break;
			}

			scan();
		}
	}

	static void doDeclVar(String name, String type) throws IOException {
		allocaGlobalVar(name, type);
	}

	// This method allocate global variables
	static void allocaGlobalVar(String name, String type) throws IOException {
		// now we start a real allocation with nasm:
		String varValue = null;
		if (look == '=') {
			match('=');
			// get the initial value:
			varValue = getStr();
		}

		if (!isAssignValid(toType(type), varValue)) {
			abort("invalid assignment");
		}

		if (varValue != null) {
			emitLn(name + ":" + TAB + getStoreType(findKeyword(type)) + TAB
					+ varValue);
		} else {
			emitLn(name + ":" + TAB + getStoreType(findKeyword(type)) + TAB
					+ defaultValue(findKeyword(type)));
		}
		match(';');

	}

	static boolean isAssignValid(Type type, String value) {
		switch (type) {
		case BYTE:
		case SHORT:
		case INT:
		case LONG:
			if (!isNum(value)) {
				return false;
			}
			break;
		case BOOL:
			if (!isBool(value)) {
				return false;
			}
			value = boolNumeric(value);
			break;
		case CHAR:
			if (!isChar(value)) {
				return false;
			}
			break;
		default:
			abort("undefined type:" + type);
			break;
		}
		return true;
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

	static void doMethod(String name, String type) throws IOException {

		postMethodStartLabel(name);
		storeMethodParams();
		match('{');
		int stackSize = storeLocalVars();
		int offset = 0;

		String varType = getName();
		while (isType(type)) {
			matchType(type);
			offset += doStoreLocalVar(toType(varType), offset);
			varType = getName();
		}

		if (stackSize > MAX_STACK_SIZE) {
			abort("stack overflow :(");
		}
		methodProlog(stackSize);
		firstBlock(varType);
		match('}');
		postMethodEndLabel(name);
		methodEpilog();
		clearParams();
	}

	static void doIf() {

	}

	static void doFor() {

	}

	static void doWhile() {

	}

	static void doSwitch() {

	}

	static void callMethod(String name) throws IOException {
		int n = paramList();
		call(name);
		cleanStack(n);
	}

	static void doReturn() {

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
	
	static void param() throws IOException {
		expression();
		push();
	}
	
	static void cleanStack(int n) {
		if (n > 0) {
			emit("ADD #");
			System.out.println(n + " ,SP");
		}
	}
	
	static void call(String name) {
		emitLn("call " + name);
	}


	static void storeMethodParams() throws IOException {
		match('(');
		if (look != ')') {
			formalParam();
			while (look == ',') {
				match(',');
				formalParam();
			}
		}
		match(')');
		newLine();
	}

	static void formalParam() throws IOException {
		String type = getName();
		String name = getName();
		addParam(type, name);
	}

	static void addParam(String type, String name) {
		if (isParam(name)) {
			duplicate("param:" + name);
		}
		if (!isType(type)) {
			abort("expected valid type, but found " + type);
		}
		int size = sizeOfType(toType(type));
		doAddParam(name, size);

	}

	static boolean isParam(String name) {
		return params.containsKey(name);
	}

	static void doAddParam(String name, int size) {
		ParamInfo info = new ParamInfo();
		info.offset = currentOffset;
		info.size = size;
		params.put(name, info);
		currentOffset += size;
	}

	static int storeLocalVars() throws IOException {
		int offset = 0;

		String type = getName();
		while (isType(type)) {
			matchType(type);
			offset += doStoreLocalVar(toType(type), offset);
			// TODO The last one of 'type' should be used in the next statement,
			// though it was scanned in this round.
			type = getName();
		}

		return offset;
	}

	static int doStoreLocalVar(Type type, int offset) throws IOException {

		int offsetPerVar = sizeOfType(type);
		String name;
		String value;
		do {
			name = getName();
			value = null;
			if (isDefinedLocalVar(name)) {
				duplicate(name);
			}
			defineLocalVar(name, type, offset);

			if (look == '=') {
				match('=');
				value = getName();
				if (!isAssignValid(type, value)) {
					abort("invalid assignment type:" + type + ", value:"
							+ value);
				}
				assignLocalVar(name, value);
			} else if (look == ',') {
				match(',');
				initLocalVar(name);
				continue;
			} else if (look == ';') {
				match(';');
				break;
			} else {
				expected(", or = or ;");
			}
			offset += offsetPerVar;
		} while (true);
		return offset;
	}

	static void assignLocalVar(String name, String value) {

		int offset = localVariables.get(name).offset;
		switch (localVariables.get(name).type) {
		case BOOL:
			emitLn("mov db [ebp-" + offset + "], " + boolNumeric(value));
			break;
		case CHAR:
			emitLn("mov db [ebp-" + offset + "], " + (int) (value.charAt(0)));
			break;
		case BYTE:
			emitLn("mov db [ebp-" + offset + "], " + value);
			break;
		case SHORT:
			emitLn("mov dw [ebp-" + offset + "], " + value);
			break;
		case INT:
			emitLn("mov dd [ebp-" + offset + "], " + value);
			break;
		case LONG:
			emitLn("mov dq [ebp-" + offset + "], " + value);
			break;
		default:
			abort("invalid type");
		}
	}

	static void initLocalVar(String name) {
		// TODO
	}

	static void methodProlog(int stackSize) {
		emitLn("push ebp");
		emitLn("mov ebp, esp");
		emitLn("sub esp, " + stackSize);
	}

	static void methodEpilog() {
		emitLn("mov esp, ebp");
		emitLn("pop ebp");
		emitLn("ret");
	}

	static void clearParams() {
		params.clear();
		currentOffset = 8;
	}

	static void block() throws IOException {
		scan();
		firstBlock(value);
	}

	static void firstBlock(String word) throws IOException {

		keyType = findKeyword(word);
		while (keyType != Type.NONE) {
			switch (keyType) {
			case IF:
				doIf();
				break;
			case WHILE:
				doWhile();
				break;
			case FOR:
				doFor();
				break;
			case SWITCH:
				doSwitch();
				break;
			case VAR:
				assignment(word);
				break;
			case PROC:
				callMethod(word);
				break;
			case RETURN:
				doReturn();
				break;
			default:
				abort("illegal identifier:" + word);
			}
			scan();
		}
	}

	static boolean DeclVar() throws IOException {
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
		case '(':
			lastName = name;
			lastType = type;
			return false;
		default:
			expected("; or =");
			break;
		}

		getChar();
		return true;
	}

	static void doClass() {

	}

	static void loadConst(int n) {
		emit("MOVE eax, ");
		System.out.println(n);
	}

	static void assignment(String name) throws IOException {

		match('=');
		expression();
		store(name);
	}

	static void store(String name) {

		if (isDefinedGlobalVar(name)) {
			emitLn("mov [" + name + "], eax");
		} else if (isDefinedLocalVar(name)) {
			emitLn("mov [ebp-" + getLocalVarOffset(name) + "], eax");
		} else if (isParam(name)) {
			emitLn("mov [ebp+" + getParamOffset(name) + "], eax");
		} else {
			abort("expected legal variable, but found " + name);
		}
	}

	static void loadVar(String name) {

		if (isDefinedGlobalVar(name)) {
			emitLn("mov eax, " + name);
		} else if (isDefinedLocalVar(name)) {
			emitLn("mov eax, [ebp-" + getLocalVarOffset(name) + "]");
		} else if (isParam(name)) {
			emitLn("mov eax, [ebp+" + getParamOffset(name) + "]");
		} else {
			abort("expected legal variable, but found " + name);
		}
	}

	static void factor() throws IOException {
		if (look == '(') {
			match('(');
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
		emitLn("neg eax");
	}

	static void push() {
		emitLn("push eax");
	}

	static void pop() {
		emitLn("pop eax");
	}

	static void popAdd() {
		emitLn("pop ebx");
		emitLn("add eax, ebx");
	}

	static void popSub() {
		emitLn("pop ebx");
		emitLn("sub eax, ebx");
	}

	static void popMul() {
		emitLn("pop ebx");
		emitLn("mul ebx");
	}

	static void popDiv() {
		emitLn("xor edx, edx");
		emitLn("pop ebx");
		emitLn("div ebx");
	}

	static void popMod() {

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
		topDecls();
		program();
	}

}