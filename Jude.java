import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * The Jude language.
 * <p>
 * 
 * <pre>
 *          ______                     __         
 *         /_   _ /                   / /
 *          /  /    __  __    ___    / /   ____
 *         /  /    / / / /   / _ \  / /   / / \ \
 *        /  /    / / / /   / / \ \/ /   / /__/_/
 *      _/  /    / /_/ /   / /___\  /    \ \_____
 *      \__/     \____/    \_______/      \_____/
 * </pre>
 * <p>
 * It can now handle some simple inputs, test it using programs like:
 * 
 * <pre>
 * int a = 10;
 * 
 * void main() {
 * 	int c = 9;
 * 	a = c + 11 * 8;
 * }
 * </pre>
 * 
 * It will be improved step by step and currently does not support the following
 * features:
 * <p>
 * 1. pointer type <br>
 * 2. array type <br>
 * 3. reference type <br>
 * 4. user defined type including List, Map, Structure, Class etc.<br>
 * 5. a statement starts with "(".<br>
 * 6. return in a block, even in a procedure.<br>
 * 7. overload of procedure.<br>
 * 8. floating type.<br>
 * 9. string type.
 * <p>
 * About the types:
 * <p>
 * Refer to: http://www.nasm.us/doc/nasmdo11.html for how to manipulate
 * registers in 64-bit system.
 * <p>
 * <a href="http://www.csee.umbc.edu/portal/help/nasm/sample_64.shtml">Helpful
 * programs</a>
 * <p>
 * http://www.posix.nl/linuxassembly/nasmdochtml/nasmdoca.html
 * <p>
 * https://www.tortall.net/projects/yasm/manual/html/nasm-immediate.html
 * <p>
 * http://stackoverflow.com/questions/4017424/how-to-check-if-a-signed-integer-
 * is-neg-or-pos
 * <p>
 * http://stackoverflow.com/questions/16917643/how-to-push-a-64bit-int-in-nasm
 * <p>
 * http://www.cwde.de/
 * <p>
 * http://stackoverflow.com/questions/9072922/nasm-idiv-a-negative-value
 * 
 * <pre>
 * global _main
 * 
 * section .text
 * 
 * _main:
 *     mov rax, 0x2000004
 *     mov rdi, 1
 *     lea rsi, [rel msg]
 *     mov rdx, msg.len
 *     syscall
 * 
 *     mov rax, 0x2000001
 *     mov rdi, 0
 *     syscall
 * 
 * section .data
 * 
 * msg:    db  "Hello, World!", 10
 * .len:   equ $ - msg
 * </pre>
 * 
 * <h2>Assignment</h2>
 * Valid assignments:<br>
 * 
 * <pre>
 * < byte | char | short | int | long > = 
 * < byte | char | short | int | long > 
 * < + | - | * | / > 
 * < byte | char | short | int | long >
 * </pre>
 * 
 * @author zpf.073@gmail.com
 *
 */
public class Jude {

	static final char TAB = '\t';
	static final char CR = '\r';
	static final char LF = '\n';
	static final char BLANK = ' ';

	static char look;

	static String token;

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
		VOID, BOOL, CHAR, BYTE, SHORT, INT, LONG, IF, ELSE, WHILE, FOR, IN, AS, SWITCH, CASE, CLASS,

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
	public static final String VOID = "void";
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
		keywords.put(VOID, Type.VOID);
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

	static void compileInfo(Object s) {
		System.out.println("[Jude] " + s);
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

	static boolean isLegalOperation(Type firstType, Type secondType) {

		if (firstType == Type.BOOL || secondType == Type.BOOL) {
			return false;
		}
		return true;
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

	static String getGlobalVarType(String name) {
		return globalVariables.get(name);
	}

	static Type getLocalVarType(String name) {
		return localVariables.get(name).type;
	}

	static Type typeOf(String name) {
		if (isDefinedGlobalVar(name)) {
			return types.get(getGlobalVarType(name));
		}
		if (isDefinedLocalVar(name)) {
			return getLocalVarType(name);
		}
		if (isParam(name)) {
			return getParamType(name);
		}
		expected("defined variable:" + name);
		return Type.NONE;
	}

	static String regOfType(Type type) {
		switch (type) {
		case BOOL:
		case BYTE:
		case CHAR:
			return "al";
		case SHORT:
			return "ax";
		case INT:
			return "eax";
		case LONG:
			return "rax";
		default:
			abort("unknown type:" + type);
		}
		return null;
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

		if (!isAlpha(str.charAt(0))) {
			return false;
		}

		int ind = 1;
		while (ind < str.length()) {
			if (!isAlNum(str.charAt(ind)))
				return false;
			ind++;
		}
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
		if (c == BLANK || c == TAB) {
			return true;
		}
		return false;
	}

	static boolean isEndOfLine(char c) {
		return c == CR || c == LF;
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
		token = "";
		while (isAlNum(look)) {
			token += look;
			getChar();
		}
		skipWhite();
		return token;
	}

	// TODO Let's assume value is just a number for now.
	static String getValue() throws IOException {
		return String.valueOf(getNum());
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

	static String getToken() throws IOException {
		newLine();
		String str = "";
		if (isNum(String.valueOf(look))) {
			return String.valueOf(getNum());
		}

		if (isName(String.valueOf(look))) {
			return getName();
		}

		str += look;
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
		keyType = findKeyword(token);
	}

	static void init() throws IOException {

		// Predefine some procedures and constants:
		defineHandleOverFlow();

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

	// number of byte in each type
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
		emitLn("global start_main");

		if (lastName != null) {
			if (isDefinedMethod(lastName)) {
				duplicate(lastName);
			}
			defineMethod(lastName, lastType);
			doMethod(lastName, lastType);
			getChar();
			scan();
		}

		while (isKeyword(token)) {

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
		while (isKeyword(token)) {
			switch (keyType) {
			case INT:
			case BYTE:
			case BOOL:
			case LONG:
			case CHAR:
			case SHORT:
			case VOID:
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
			varValue = getToken();
		}

		if (varValue != null && !isAssignValid(toType(type), varValue)) {
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

		String type = token;
		String name = null;
		matchKeyword(token);
		name = token;
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
		int offset = 0;
		String varType = getToken();

		while (isType(varType)) {
			matchType(varType);
			offset += doStoreLocalVar(toType(varType), offset);
			varType = getToken();
		}

		if (offset > MAX_STACK_SIZE) {
			abort("stack overflow :(");
		}

		methodProlog(offset);
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
		Type expType = expression();
		push(expType);
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

	static Type getParamType(String name) {
		return params.get(name).type;
	}

	static void doAddParam(String name, int size) {
		ParamInfo info = new ParamInfo();
		info.offset = currentOffset;
		info.size = size;
		params.put(name, info);
		currentOffset += size;
	}

	static int doStoreLocalVar(Type type, int offset) throws IOException {

		int offsetPerVar = sizeOfType(type);
		String name;
		String value;
		do {
			name = token;
			value = null;
			if (isDefinedLocalVar(name)) {
				duplicate(name);
			}
			offset += offsetPerVar;
			defineLocalVar(name, type, offset);

			if (look == '=') {
				match('=');
				value = getValue();
				if (!isAssignValid(type, value)) {
					abort("invalid assignment type:" + type + ", _value:"
							+ value);
				}
				assignLocalVar(name, value);
			}

			if (look == ',') {
				match(',');
				initLocalVar(type, name);
				scan();
				continue;
			}

			if (look == ';') {
				match(';');
				break;
			} else {
				expected(", or = or ;");
			}

			
		} while (true);

		return offset;
	}

	static void assignLocalVar(String name, String value) {

		int offset = localVariables.get(name).offset;
		switch (localVariables.get(name).type) {
		case BOOL:
			emitLn("mov db [rbp-" + offset + "], " + boolNumeric(value));
			break;
		case CHAR:
			emitLn("mov db [rbp-" + offset + "], " + (int) (value.charAt(0)));
			break;
		case BYTE:
			emitLn("mov db [rbp-" + offset + "], " + value);
			break;
		case SHORT:
			emitLn("mov dw [rbp-" + offset + "], " + value);
			break;
		case INT:
			emitLn("mov dd [rbp-" + offset + "], " + value);
			break;
		case LONG:
			emitLn("mov dq [rbp-" + offset + "], " + value);
			break;
		default:
			abort("invalid type");
		}
	}

	static void initLocalVar(Type type, String name) {
		// TODO
	}

	static void methodProlog(int stackSize) {
		emitLn("push rbp");
		emitLn("mov rbp, rsp");
		emitLn("sub rsp, " + stackSize);
	}

	static void methodEpilog() {
		emitLn("mov rsp, rbp");
		emitLn("pop rbp");
		emitLn("ret");
	}

	static void clearParams() {
		params.clear();
		currentOffset = 8;
	}

	static void block() throws IOException {
		scan();
		firstBlock(token);
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
				assignment(typeOf(word), word);
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

			token = getToken();
			keyType = findKeyword(token);
		}
	}

	static boolean DeclVar() throws IOException {
		String type = token;
		String name = null;
		matchKeyword(token);
		name = token;
		if (isDefinedGlobalVar(name)) {
			duplicate(name);
		}

		switch (look) {
		case ';':
		case '=':
			if (toType(type) == Type.VOID) {
				abort("cannot define a variable of void type!");
			}
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

	static Type loadConst(int n) {
		// By default rax accepts a 32-bit number:
		emit("mov rax, ");
		System.out.println(n);

		// Temporarily only int constants:
		return Type.INT;
	}

	static void assignment(Type type, String name) throws IOException {

		match('=');
		Type expType = expression();
		// Determine if expType and type is compatible:
		if ((type != expType) && (expType == Type.BOOL)) {

			// This allows max flexibility in assignment, which also apparently
			// brings the risk of overflowing and confuse. But I believe if
			// something has its necessity to be used, it should be used in the
			// most straight and natural way.
			abort("invalid assignment from " + expType + " to " + type);
		}
		store(type, name);
		match(';');
	}

	static void store(Type type, String name) {

		// store with the right register:
		String reg = regOfType(typeOf(name));
		if (isDefinedGlobalVar(name)) {
			emitLn("mov [" + name + "], " + reg);
		} else if (isDefinedLocalVar(name)) {
			emitLn("mov [rbp-" + getLocalVarOffset(name) + "], " + reg);
		} else if (isParam(name)) {
			emitLn("mov [rbp+" + getParamOffset(name) + "], " + reg);
		} else {
			abort("expected legal variable, but found " + name);
		}
	}

	static Type loadVar(String name) {
		// have to use the type info of the variable:
		emitLn("mov rax, 0"); // Clear all bits of rax

		String reg = regOfType(typeOf(name));

		if (isDefinedGlobalVar(name)) {

			emitLn("mov " + reg + ", " + name);
		} else if (isDefinedLocalVar(name)) {
			emitLn("mov " + reg + ", [rbp-" + getLocalVarOffset(name) + "]");
		} else if (isParam(name)) {
			emitLn("mov " + reg + ", [rbp+" + getParamOffset(name) + "]");
		} else {
			abort("expected legal variable, but found " + name);
		}

		// Sign extension, we extend the value to until rax:
		if ("al".equals(reg)) {
			emitLn("cbw");
			emitLn("cwde");
			emitLn("cdqe");
		} else if ("ax".equals(reg)) {
			emitLn("cwde");
			emitLn("cdqe");
		} else if ("eax".equals(reg)) {
			emitLn("cdqe");
		}
		return typeOf(name);
	}

	static Type factor() throws IOException {
		if (look == '(') {
			match('(');
			Type res = expression();
			match(')');
			return res;
		} else if (isAlpha(look)) {
			getName();
			return loadVar(token);
		} else {
			return loadConst(getNum());
		}
	}

	static Type negFactor() throws IOException {
		Type resType;
		match('-');
		if (isDigit(look)) {
			resType = loadConst(-getNum());
		} else {
			resType = factor();
			negate();
		}
		return resType;
	}

	// Not sure if this operation is valid
	static void negate() {
		emitLn("neg rax");
	}

	// push to the stack ignoring the type of the value because we have sign
	// extended the value to 64-bit
	static void push(Type type) {
		emitLn("push rax");
	}

	static void pop() {
		emitLn("pop rax");
	}

	static Type popAdd(Type firstType, Type secondType) {

		// If the add operation is invalid:
		if (!isLegalOperation(firstType, secondType)) {
			abort("invalid operation between " + firstType + " and "
					+ secondType);
		}
		emitLn("pop rbx");
		emitLn("add rax, rbx");

		// Consider the possibility of overflowing:
		if (firstType == Type.LONG || secondType == Type.LONG) {
			emitLn("jo handle_overflow");
		}

		// Which type should be returned:
		if (firstType.ordinal() < secondType.ordinal()) {
			return secondType;
		} else {
			return firstType;
		}
	}

	static Type popSub(Type firstType, Type secondType) {
		// If the add operation is invalid:
		if (!isLegalOperation(firstType, secondType)) {
			abort("invalid operation between " + firstType + " and "
					+ secondType);
		}

		emitLn("pop rbx");
		emitLn("sub rax, rbx");

		// Consider the possibility of overflowing:
		if (firstType == Type.LONG || secondType == Type.LONG) {
			emitLn("jo handle_overflow");
		}

		// Which type should be returned:
		if (firstType.ordinal() < secondType.ordinal()) {
			return secondType;
		} else {
			return firstType;
		}
	}

	static Type popMul(Type firstType, Type secondType) {

		// If the add operation is invalid:
		if (!isLegalOperation(firstType, secondType)) {
			abort("invalid operation between " + firstType + " and "
					+ secondType);
		}

		emitLn("pop rbx");
		emitLn("imul rbx");
		emitLn("jo handle_overflow");

		// Which type should be returned:
		if (firstType.ordinal() < secondType.ordinal()) {
			return secondType;
		} else {
			return firstType;
		}
	}

	static Type popDiv(Type firstType, Type secondType) {

		// If the add operation is invalid:
		if (!isLegalOperation(firstType, secondType)) {
			abort("invalid operation between " + firstType + " and "
					+ secondType);
		}

		// For integer numbers, if the dividend is smaller than divisor, the
		// result is zero:
		emitLn("xor rdx, rdx");
		emitLn("pop rbx");
		emitLn("idiv rbx");
		emitLn("jo handle_overflow");
		return firstType;
	}

	static void popMod() {

	}

	static Type add(Type type) throws IOException {
		match('+');
		Type termType = term();

		// Check if type and termType is compatible:

		popAdd(type, termType);

		return type;
	}

	static Type substract(Type type) throws IOException {
		match('-');
		Type termType = term();
		popSub(type, termType);
		return type;
	}

	static Type multiply(Type type) throws IOException {
		match('*');
		Type factorType = factor();
		return popMul(type, factorType);
	}

	static Type divide(Type type) throws IOException {
		match('/');
		Type factorType = factor();
		return popDiv(type, factorType);
	}

	static Type term() throws IOException {
		Type factorType = factor();
		Type term1Type = term1(factorType);
		return term1Type;
	}

	static Type term1(Type type) throws IOException {

		Type resType = type;
		while (isMulOp(look)) {
			push(type);
			switch (look) {
			case '*':
				resType = multiply(type);
				break;
			case '/':
				resType = divide(type);
				break;
			}
		}
		return resType;
	}

	static Type firstTerm() throws IOException {

		Type fristType = firstFactor();
		Type secondType = term1(fristType);
		return secondType;
	}

	static Type firstFactor() throws IOException {
		switch (look) {
		case '+':
			match('+');
			return factor();
		case '-':
			return negFactor();
		default:
			return factor();
		}
	}

	static Type expression() throws IOException {
		Type firstType = firstTerm();
		Type resType = firstType;
		while (isAddOp(look)) {
			push(firstType);
			switch (look) {
			case '+':
				resType = add(firstType);
				break;
			case '-':
				resType = substract(firstType);
				break;
			}
		}
		return resType;
	}

	static void defineHandleOverFlow() {
		postLabel("handle_overflow:");
		// Simply exit the program:
		exit(1);
	}

	static void exit(int code) {
		emitLn("pop	rbp");
		emitLn("mov rax, " + code);
		emitLn("ret");
	}

	public static void main(String[] args) throws IOException {

		init();
		topDecls();
		program();
	}

}