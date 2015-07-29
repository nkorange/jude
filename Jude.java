import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
 * <p>
 * http://cs.lmu.edu/~ray/notes/nasmtutorial/ OSX program sample
 * <p>
 * http://www.website.masmforum.com/tutorials/fptute/fpuchap5.htm
 * <p>
 * http://www.csee.umbc.edu/courses/undergraduate/CMSC313/fall04/burt_katz/
 * lectures/Lect12/floatingpoint.html
 * 
 * <pre>
 * global _main
 * extern _printf
 * default rel
 * section .text
 * 
 * _main:
 * ;       mov rax, __float64__(111.2222)
 * ;       fld qword [rax]
 *         mov rax, 20
 * 
 *         push rax
 *         lea rdi, [message]
 *         mov rsi, rax
 *         mov rax,0
 *         call _printf
 * ;       fld qword [a]
 * ;       mov qword [b], __float32__(1.12)
 * ;       fadd qword [b]
 * 
 * ;       mov rax, [a]
 * ;       fld qword [rax]
 * ;       fadd qword [rax]
 * ;       fstp qword [rax]
 *         mov rax, 0x2000004
 *         mov rdi, 1
 *         lea rsi, [rel msg]
 *         mov rdx, msg.len
 *         syscall
 * 
 *         mov rax, 0x2000001
 *         mov rdi, 0
 *         syscall
 * 
 * section .data
 * 
 *  msg:    db  "Hello, World!", 10
 *  .len:   equ $ - msg
 * message: db "Register = %08d", 10, 0
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
 * <h2>Floating type</h2>
 * 
 * About floating type there are many problems deserving careful consideration.
 * 
 * @author zpf.073@gmail.com
 *
 */
public class Jude {

	static final char TAB = '\t';
	static final char CR = '\r';
	static final char LF = '\n';
	static final char BLANK = ' ';
	static final char POINT = '.';

	static char look;

	static String token;

	static Type keyType;

	static String lastName = null;

	static String lastType = null;

	static Map<String, String> globalVariables = new HashMap<String, String>();

	static Map<String, ParamInfo> localVariables = new HashMap<String, ParamInfo>();

	static Map<String, MethodInfo> methods = new HashMap<String, MethodInfo>();

	static Map<String, ParamInfo> params = new HashMap<String, ParamInfo>();

	static int indexInt = 0;
	static int indexFloat = 0;

	static final int INITIAL_PARAM_OFFSET = 8;
	static int currentOffset = 0;

	// static Map<String, Integer> names = new HashMap<String, Integer>();

	static enum Type {
		VOID, BOOL, CHAR, BYTE, SHORT, INT, FLOAT, LONG, NON_FLOAT, IF, ELSE, WHILE, FOR, IN, AS, SWITCH, CASE, CLASS,

		NONE, END, VAR, PROC, RETURN
	};

	static class ParamInfo {
		int offset;
		int size;
		Type type;
		int index;
	}

	static class MethodInfo {
		String name;
		Type returnType;
		int paramCount;
		List<Type> params;
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
	public static final String FLOAT = "float";

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
		keywords.put(FLOAT, Type.FLOAT);
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
		types.put(FLOAT, Type.FLOAT);
	}

	static final long MAX_STACK_SIZE = 128;
	static final String TEMP_FLOAT_VAR_NAME = "__1__";

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
		System.out.println("[info] " + s);
	}

	static void compileWarn(Object s) {
		System.out.println("[warn] " + s);
	}

	static void postLabel(String l) {
		System.out.println(l + ":");
	}

	static void postMethodStartLabel(String name) {
		System.out.println(name + ":");
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
		case FLOAT:
			return "rax";
		default:
			abort("unknown type:" + type);
		}
		return null;
	}

	static String toAsmType(Type type) {
		switch (type) {
		case BOOL:
		case BYTE:
		case CHAR:
			return "byte";
		case SHORT:
			return "word";
		case INT:
			return "dword";
		case LONG:
		case FLOAT:
			return "qword";
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

		MethodInfo info = new MethodInfo();
		info.name = name;
		info.returnType = toType(type);
		info.paramCount = 0;
		info.params = new ArrayList<Type>();
		methods.put(name, info);
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

	static char getChar() throws IOException {
		look = (char) System.in.read();
		return look;
	}

	static char getAChar() throws IOException {
		match('\'');

		char c = look;
		getChar();
		match('\'');
		return c;
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
		case FLOAT:
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

	static int getParamIndex(String name) {
		return params.get(name).index;
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

	static String getValue(Type type) throws IOException {

		// Allow assign different form of values to different types:
		String tmp;
		switch (type) {
		case BOOL:
			tmp = getName();
			if (!isBool(tmp)) {
				abort("invalid bool value:" + tmp);
			}
			return boolNumeric(tmp);
		case CHAR:
			return String.valueOf(getAChar());
		case SHORT:
		case INT:
		case LONG:
			return String.valueOf(getIntegerNum());
		case FLOAT:
			return String.valueOf(getFloatNum());
		default:
			abort("unknown type:" + type);
		}
		return null;

	}

	static String getNum() throws IOException {
		String val = "";
		int pointCount = 0;
		newLine();
		if (!isDigit(look)) {
			expected("Integer");
		}
		while (isDigit(look) || (look == POINT && pointCount <= 1)) {
			if (look == POINT) {
				pointCount++;
			}
			val += look;
			getChar();
		}
		if (pointCount > 1) {
			abort("more than one '.' found");
		}
		skipWhite();
		return val;
	}

	static int getIntegerNum() throws IOException {
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

	static boolean isIntType(Type type) {
		return type == Type.BYTE || type == Type.SHORT || type == Type.INT
				|| type == Type.LONG || type == Type.BOOL || type == Type.CHAR;
	}

	/**
	 * Get a float number from a string.
	 * <p>
	 * Legal float number format:<br>
	 * <code>5, 5.0</code>
	 * <p>
	 * Illegal float numbers: <br>
	 * <code>.5, 5.</code>(they are legal in Java language)
	 * 
	 * @return
	 * @throws IOException
	 */
	static double getFloatNum() throws IOException {

		String val = "";
		int pointCount = 0;
		newLine();
		if (!isDigit(look)) {
			expected("Integer");
		}
		while (isDigit(look) || (look == POINT && pointCount <= 1)) {
			if (look == POINT) {
				pointCount++;
			}
			val += look;
			getChar();
		}
		if (pointCount > 1) {
			abort("more than one '.' found");
		}
		skipWhite();

		return Double.parseDouble(val);
	}

	static boolean isFloat(String num) {
		return num.contains("" + POINT);
	}

	static String getToken() throws IOException {
		newLine();
		String str = "";
		if (isNum(String.valueOf(look))) {
			return String.valueOf(getFloatNum());
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
		case FLOAT:
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

		// Predefine some procedures and constants:
		defineHandleOverFlow();

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
			case FLOAT:
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
		predefineGlobalVars();
		while (isKeyword(token)) {
			switch (keyType) {
			case INT:
			case BYTE:
			case BOOL:
			case LONG:
			case CHAR:
			case SHORT:
			case FLOAT:
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

	// Some predefined variables:
	static void predefineGlobalVars() {
		// Used for temporary floating number:
		emitLn(TEMP_FLOAT_VAR_NAME + ":	dq	1.0");
	}

	static void doDeclVar(String name, String type) throws IOException {
		allocaGlobalVar(name, type);
	}

	// This method allocates global variables
	static void allocaGlobalVar(String name, String type) throws IOException {
		// now we start a real allocation with nasm:
		String varValue = null;
		if (look == '=') {
			match('=');
			// get the initial value:
			// TODO let's limit that assignment is not permitted here for global
			// variables:
			varValue = getValue(toType(type));
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

	static boolean isAssignValid(Type type, Type expType) {
		switch (type) {
		case BYTE:
		case SHORT:
		case INT:
		case LONG:
			if (expType != Type.BYTE && expType != Type.SHORT
					&& expType != Type.INT && expType != Type.LONG) {
				return false;
			}
			if (expType != type) {
				compileWarn("Type " + expType + " is converted to " + type);
			}
			break;
		case BOOL:
		case CHAR:
			if (expType != type) {
				return false;
			}
			break;
		case FLOAT:
			if (expType != Type.BYTE && expType != Type.SHORT
					&& expType != Type.INT && expType != Type.LONG
					&& expType != Type.FLOAT) {
				return false;
			}
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
		doMethodParams(name);
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
		int n = paramList(name);
		call(name);
		
		cleanStack(n);
	}

	static void doReturn() {

	}

	static int paramList(String methodName) throws IOException {

		int n = 0;
		match('(');
		if (look != ')') {
			param(methodName, n);

			n++;
			while (look == ',') {
				match(',');
				param(methodName, n);
				n++;
			}
		}
		match(')');
		// check the count of parameters
		if (n != methods.get(methodName).paramCount) {
			abort("different arguments number from definition");
		}
		return 2 * n;
	}

	static Type param(String methodName, int index) throws IOException {
		Type expType = expression();
		// Check the validity of parameter:
		checkParam(methodName, expType, index);
		pushParam(methodName, index + 1);
		return expType;
	}

	static void pushParam(String methodName, int index) {
		Type type = methods.get(methodName).params.get(index-1);
		if (isIntType(type)) {
			pushIntParam(type, intParamIndex(methodName, index));
		} else if (type == Type.FLOAT) {
			pushFloatParam(type, floatParamIndex(methodName, index));
		}
	}

	//RDI, RSI, RDX, RCX, R8, and R9.
	static void pushIntParam(Type type, int index) {

		if (index > 6) {
			emitLn("push rax");
		} else {
			switch (index) {
			case 1:
				emitLn("mov rdi, rax");
				break;
			case 2:
				emitLn("mov rsi, rax");
				break;
			case 3:
				emitLn("mov rdx, rax");
				break;
			case 4:
				emitLn("mov rcx, rax");
				break;
			case 5:
				emitLn("mov r8, rax");
				break;
			case 6:
				emitLn("mov r9, rax");
				break;
			}
		}
	}

	static void pushFloatParam(Type type, int index) {

		if (index > 8) {
			emitLn("push qword [" + TEMP_FLOAT_VAR_NAME + "]");
		} else {
			emitLn("mov mm" + (index-1) + ", [" + TEMP_FLOAT_VAR_NAME + "]");
		}
	}

	static int intParamIndex(String methodName, int index) {
		int i = 0, n = 0;
		while (i < index) {
			if (isIntType(methods.get(methodName).params.get(i))) {
				n++;
			}
			i++;
		}
		return n;
	}

	static int floatParamIndex(String methodName, int index) {
		int i = 0, n = 0;
		while (i < index) {
			if (methods.get(methodName).params.get(i) == Type.FLOAT) {
				n++;
			}
			i++;
		}
		return n;
	}

	static void checkParam(String methodName, Type expType, int index) {
		MethodInfo info = methods.get(methodName);
		if (info.paramCount <= index) {
			abort("too many arguments");
		}
		if (!isAssignValid(info.params.get(index), expType)) {
			abort("Cannot assign " + expType + " to " + info.params.get(index));
		}
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

	static void doMethodParams(String name) throws IOException {

		clearParamIndex();
		match('(');
		if (look != ')') {
			formalParam(name);
			while (look == ',') {
				match(',');
				formalParam(name);
			}
		}
		match(')');
		newLine();
	}

	static void clearParamIndex() {
		indexInt = 0;
		indexFloat = 0;
	}

	static void formalParam(String methodName) throws IOException {
		String type = getName();
		String name = getName();

		if (toType(type) == Type.FLOAT) {
			indexFloat++;
		} else {
			indexInt++;
		}

		addParam(methodName, type, name);
	}

	// Note that the parameter storage follows C language convention. First six
	// integer arguments are passed in RDI, RSI, RDX, RCX, R8, and R9.
	// Additional arguments are pushed to the stack. First eight arguments are
	// passed in xmm0 to xmm7.
	static void addParam(String methodName, String type, String name) {
		if (isParam(name)) {
			duplicate("param:" + name);
		}
		if (!isType(type)) {
			abort("expected valid type, but found " + type);
		}
		int size = sizeOfType(toType(type));
		doAddParam(methodName, name, size, toType(type));

	}

	static boolean isParam(String name) {
		return params.containsKey(name);
	}

	static Type getParamType(String name) {
		return params.get(name).type;
	}

	static void doAddParam(String methodName, String name, int size, Type type) {

		methods.get(methodName).paramCount++;
		methods.get(methodName).params.add(type);

		if (type == Type.FLOAT) {
			doAddFloatParam(name, size, type);
		} else {
			doAddIntParam(name, size, type);
		}
	}

	// For integer parameter, only 7th+ parameters have offsets:
	static void doAddIntParam(String name, int size, Type type) {
		ParamInfo info = new ParamInfo();
		info.offset = currentOffset;
		info.size = size;
		info.type = type;
		info.index = indexInt;
		params.put(name, info);
		if (indexInt >= 7) {
			currentOffset += size;
		}
	}

	// For floating parameter, only 9th+ parameters have offsets:
	static void doAddFloatParam(String name, int size, Type type) {
		ParamInfo info = new ParamInfo();
		info.offset = currentOffset;
		info.size = size;
		info.type = type;
		info.index = indexFloat;
		params.put(name, info);
		if (indexFloat >= 9) {
			currentOffset += size;
		}
	}

	static int doStoreLocalVar(Type type, int offset) throws IOException {

		int offsetPerVar = sizeOfType(type);
		String name;
		do {
			name = token;
			if (isDefinedLocalVar(name)) {
				duplicate(name);
			}
			offset += offsetPerVar;
			defineLocalVar(name, type, offset);

			if (look == '=') {
				match('=');
				storeVar(type, name, doAssignment(type));
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

	static Type loadConst() throws IOException {
		String var = getNum();
		if (isFloat(var)) {
			return loadFloatConst(Double.parseDouble(var));
		} else {
			return loadIntegerConst(Integer.parseInt(var));
		}
	}

	static Type loadNegConst() throws IOException {
		double var = -getFloatNum();
		if (isFloat(String.valueOf(var))) {
			return loadFloatConst(var);
		} else {
			return loadIntegerConst((int) var);
		}
	}

	static Type loadIntegerConst(int n) {
		// By default rax accepts a 32-bit number:
		emitLn("mov rax, " + n);
		return Type.INT;
	}

	static Type loadFloatConst(double d) {
		emitLn("mov qword [" + TEMP_FLOAT_VAR_NAME + "], __float32__(" + d
				+ ")");
		return Type.FLOAT;
	}

	// Treat char as common constant number:
	static Type loadChar(char c) {
		emitLn("mov rax, 0");
		emitLn("mov al, " + (int) c);

		return Type.CHAR;
	}

	static Type loadBool(String boolValue) {
		emitLn("mov rax, 0");
		emitLn("mov al, " + boolNumeric(boolValue));

		return Type.BOOL;
	}

	static void assignment(Type type, String name) throws IOException {

		match('=');
		storeVar(type, name, doAssignment(type));
		match(';');
	}

	static Type doAssignment(Type type) throws IOException {
		Type expType = expression();
		// Determine if expType and type is compatible:
		if (!isAssignValid(type, expType)) {

			// This allows max flexibility in assignment, which also apparently
			// brings the risk of overflowing and confuse. But I believe if
			// something has its necessity to be used, it should be used in the
			// most straight and natural way.
			abort("invalid assignment from " + expType + " to " + type);
		}
		return expType;
	}

	static void storeVar(Type type, String name, Type expType) {

		if (type == Type.FLOAT) {

			if (expType == Type.FLOAT) {
				emitLn("fld qword [" + TEMP_FLOAT_VAR_NAME + "]");
			} else {
				// An integer-to-float assignment:
				emitLn("fild qword [rax]");
			}
			if (isDefinedGlobalVar(name)) {
				emitLn("fstp qword [" + name + "]");
			} else if (isDefinedLocalVar(name)) {
				emitLn("fstp qword [rbp-" + getLocalVarOffset(name) + "]");
			} else if (isParam(name)) {
				storeFloatParam(name);
			} else {
				abort("expected legal variable, but found " + name);
			}
		} else {
			// store with the correct register:
			String reg = regOfType(typeOf(name));
			if (isDefinedGlobalVar(name)) {
				emitLn("mov [" + name + "], " + reg);
			} else if (isDefinedLocalVar(name)) {
				emitLn("mov [rbp-" + getLocalVarOffset(name) + "], " + reg);
			} else if (isParam(name)) {
				storeIntParam(name);
			} else {
				abort("expected legal variable, but found " + name);
			}
		}
	}

	static void storeParam(String name) {
		if (typeOf(name) == Type.FLOAT) {
			storeFloatParam(name);
		} else {
			storeIntParam(name);
		}
	}

	static void storeIntParam(String name) {
		String reg = regOfType(typeOf(name));
		if (getParamIndex(name) > 6) {
			emitLn("mov [rbp+" + getParamOffset(name) + "], " + reg);
		} else {
			switch (getParamIndex(name)) {
			case 1:
				emitLn("mov rdi, " + reg);
				break;
			case 2:
				emitLn("mov rsi, " + reg);
				break;
			case 3:
				emitLn("mov rdx, " + reg);
				break;
			case 4:
				emitLn("mov rcx, " + reg);
				break;
			case 5:
				emitLn("mov r8, " + reg);
				break;
			case 6:
				emitLn("mov r9, " + reg);
				break;

			}
		}
	}

	static void storeFloatParam(String name) {

		if (getParamIndex(name) > 8) {
			emitLn("fstp qword [rbp+" + getParamOffset(name) + "]");
		} else {
			emitLn("fstp qword [mm" + (getParamIndex(name) - 1) + "]");
		}
	}

	static Type loadVar(String name) {

		if (typeOf(name) == Type.FLOAT) {
			return loadFloatVar(name);
		}

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

	static Type loadFloatVar(String name) {

		if (isDefinedGlobalVar(name)) {

			emitLn("fld qword [" + name + "]");
			emitLn("fstp qword [" + TEMP_FLOAT_VAR_NAME + "]");
		} else if (isDefinedLocalVar(name)) {
			emitLn("fld qword [rbp-" + getLocalVarOffset(name) + "]");
			emitLn("fstp qword [" + TEMP_FLOAT_VAR_NAME + "]");
		} else if (isParam(name)) {
			emitLn("fld qword [rbp+" + getParamOffset(name) + "]");
			emitLn("fstp qword [" + TEMP_FLOAT_VAR_NAME + "]");
		} else {
			abort("expected legal variable, but found " + name);
		}
		return Type.FLOAT;
	}

	static Type factor() throws IOException {
		if (look == '(') {
			match('(');
			Type res = expression();
			match(')');
			return res;
		} else if (isAlpha(look)) {
			getName();
			// Don't forget bool value is also a non-number string. Usually a
			// bool assignment is invalid in an expression, but we will just
			// leave the judgment to subsequent program.
			if (isBool(token)) {
				return loadBool(token);
			}
			// TODO add procedure name
			return loadVar(token);
		} else if (isNum(String.valueOf(look))) {
			return loadConst();
		} else if (look == '\'') {
			return loadChar(getAChar());
		} else {
			return Type.NONE;
		}
	}

	static Type negFactor() throws IOException {
		Type resType;
		match('-');
		if (isDigit(look)) {
			resType = loadNegConst();
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
	// extended the value to 64-bit.
	// For floating value, we use a predefined global variable to act as [rax]
	// in integer mode.
	static void push(Type type) {

		if (type == Type.FLOAT) {
			emitLn("fld qword [" + TEMP_FLOAT_VAR_NAME + "]");
		} else {
			emitLn("push rax");
		}
	}

	// Not used currently:
	static void pop() {
		emitLn("pop rax");
	}

	static Type popAdd(Type firstType, Type secondType) {

		if (firstType == Type.FLOAT || secondType == Type.FLOAT) {
			return popAddFloat(firstType, secondType);
		}

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

	static Type popAddFloat(Type firstType, Type secondType) {

		if (firstType == Type.FLOAT && secondType == Type.FLOAT) {
			emitLn("fadd qword [" + TEMP_FLOAT_VAR_NAME + "]");
			emitLn("fstp qword [" + TEMP_FLOAT_VAR_NAME + "]");
		} else if (firstType == Type.FLOAT) {
			// second type is integer:
			emitLn("fstp qword [" + TEMP_FLOAT_VAR_NAME + "]");
			emitLn("fild qword [rax]");
			emitLn("fadd qword [" + TEMP_FLOAT_VAR_NAME + "]");
			emitLn("fstp qword [" + TEMP_FLOAT_VAR_NAME + "]");
		} else {
			// first type is integer:
			emitLn("pop rax");
			emitLn("fild qword [rax]");
			emitLn("fadd qword [" + TEMP_FLOAT_VAR_NAME + "]");
			emitLn("fstp qword [" + TEMP_FLOAT_VAR_NAME + "]");
		}

		emitLn("jo handle_overflow");
		return Type.FLOAT;
	}

	static Type popSub(Type firstType, Type secondType) {
		// If the add operation is invalid:
		if (!isLegalOperation(firstType, secondType)) {
			abort("invalid operation between " + firstType + " and "
					+ secondType);
		}

		if (firstType == Type.FLOAT || secondType == Type.FLOAT) {
			return popSubFloat(firstType, secondType);
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

	static Type popSubFloat(Type firstType, Type secondType) {

		if (firstType == Type.FLOAT && secondType == Type.FLOAT) {
			emitLn("fsub qword [" + TEMP_FLOAT_VAR_NAME + "]");
			emitLn("fstp qword [" + TEMP_FLOAT_VAR_NAME + "]");
		} else if (firstType == Type.FLOAT) {
			// second type is integer:
			emitLn("fild qword [rax]");
			emitLn("fstp qword [" + TEMP_FLOAT_VAR_NAME + "]");
			emitLn("fsub qword [" + TEMP_FLOAT_VAR_NAME + "]");
			emitLn("fstp qword [" + TEMP_FLOAT_VAR_NAME + "]");
		} else {
			// first type is integer:
			emitLn("pop rax");
			emitLn("fild qword [rax]");
			emitLn("fsub qword [" + TEMP_FLOAT_VAR_NAME + "]");
			emitLn("fstp qword [" + TEMP_FLOAT_VAR_NAME + "]");
		}

		emitLn("jo handle_overflow");
		return Type.FLOAT;
	}

	static Type popMul(Type firstType, Type secondType) {

		// If the add operation is invalid:
		if (!isLegalOperation(firstType, secondType)) {
			abort("invalid operation between " + firstType + " and "
					+ secondType);
		}

		if (firstType == Type.FLOAT || secondType == Type.FLOAT) {
			return popMulFloat(firstType, secondType);
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

	static Type popMulFloat(Type firstType, Type secondType) {

		if (firstType == Type.FLOAT && secondType == Type.FLOAT) {
			emitLn("fmul qword [" + TEMP_FLOAT_VAR_NAME + "]");
			emitLn("fstp qword [" + TEMP_FLOAT_VAR_NAME + "]");
		} else if (firstType == Type.FLOAT) {
			// second type is integer:
			emitLn("fild qword [rax]");
			emitLn("fstp qword [" + TEMP_FLOAT_VAR_NAME + "]");
			emitLn("fmul qword [" + TEMP_FLOAT_VAR_NAME + "]");
			emitLn("fstp qword [" + TEMP_FLOAT_VAR_NAME + "]");
		} else {
			// first type is integer:
			emitLn("pop rax");
			emitLn("fild qword [rax]");
			emitLn("fmul qword [" + TEMP_FLOAT_VAR_NAME + "]");
			emitLn("fstp qword [" + TEMP_FLOAT_VAR_NAME + "]");
		}

		emitLn("jo handle_overflow");
		return Type.FLOAT;
	}

	static Type popDiv(Type firstType, Type secondType) {

		// If the add operation is invalid:
		if (!isLegalOperation(firstType, secondType)) {
			abort("invalid operation between " + firstType + " and "
					+ secondType);
		}

		if (firstType == Type.FLOAT || secondType == Type.FLOAT) {
			return popDivFloat(firstType, secondType);
		}

		// For integer numbers, if the dividend is smaller than divisor, the
		// result is zero:
		emitLn("xor rdx, rdx");
		emitLn("pop rbx");
		emitLn("idiv rbx");
		emitLn("jo handle_overflow");
		return firstType;
	}

	static Type popDivFloat(Type firstType, Type secondType) {
		if (firstType == Type.FLOAT && secondType == Type.FLOAT) {
			emitLn("fdiv qword [" + TEMP_FLOAT_VAR_NAME + "]");
			emitLn("fstp qword [" + TEMP_FLOAT_VAR_NAME + "]");
		} else if (firstType == Type.FLOAT) {
			// second type is integer:
			emitLn("fild qword [rax]");
			emitLn("fstp qword [" + TEMP_FLOAT_VAR_NAME + "]");
			emitLn("fdiv qword [" + TEMP_FLOAT_VAR_NAME + "]");
			emitLn("fstp qword [" + TEMP_FLOAT_VAR_NAME + "]");
		} else {
			// first type is integer:
			emitLn("pop rax");
			emitLn("fild qword [rax]");
			emitLn("fdiv qword [" + TEMP_FLOAT_VAR_NAME + "]");
			emitLn("fstp qword [" + TEMP_FLOAT_VAR_NAME + "]");
		}

		emitLn("jo handle_overflow");
		return Type.FLOAT;
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
