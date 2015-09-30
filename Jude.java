/**
 * Copyright (C) 2015  nkorange<zpf.073@gmail.com>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Jude language.
 * <p>
 * <p>
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
 * <p>
 * <pre>
 * int a = 10;
 *
 * void main() {
 * 	int c = 9;
 * 	a = c + 11 * 8;
 * }
 * </pre>
 * <p>
 * It will be improved step by step and currently does not support the following
 * features:
 * <li> pointer type <br>
 * <li> reference type <br>
 * <li> user defined type including Enum, Structure, Class etc.<br>
 * <li> a statement starts with "(".<br>
 * <li> overload of procedure.<br>
 * <li> string type.</li>
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
 * <p>
 * http://faydoc.tripod.com/cpu/setnz.htm
 * <p>
 * https://en.wikibooks.org/wiki/X86_Assembly/Control_Flow#Comparison_Instructions
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
 * <p>
 * <h2>Assignment</h2>
 * Valid assignments:<br>
 * <p>
 * <pre>
 * < byte | char | short | int | long > =
 * < byte | char | short | int | long >
 * < + | - | * | / >
 * < byte | char | short | int | long >
 * </pre>
 * <p>
 * <h2>Change log</h2>
 * 2015-08-08   Start to support array type.<br>
 * 2015-08-16   Start to test the assembly code.<br>
 *
 * @author zpf.073@gmail.com
 */
public class Jude extends Helper {


    static char look;

    static String token;

    static Type keyType;

    static String lastName = null;

    static String lastType = null;

    static Map<String, VarInfo> globalVariables = new HashMap<String, VarInfo>();

    static Map<String, ParamInfo> localVariables = new HashMap<String, ParamInfo>();

    static Map<String, MethodInfo> methods = new HashMap<String, MethodInfo>();

    static Map<String, ParamInfo> params = new HashMap<String, ParamInfo>();

    static int indexInt = 0;

    static int indexFloat = 0;

    static final int INITIAL_PARAM_OFFSET = 8;

    static int currentOffset = 0;

    static boolean endOfFile = false;

    enum Type {
        VOID, BOOL, CHAR, BYTE, SHORT, INT, FLOAT, LONG, IF, ELSE, ELIF, WHILE, FOR, IN, AS, CASE, CLASS,

        NONE, END, VAR, PROC, RETURN, COMMENT
    }

    enum Op {
        EQUAL, NEQUAL, GREATER, GREATERE, SMALLER, SMALLERE, AND, OR, XOR, NOT
    }

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

        @Override
        public String toString() {
            final StringBuffer sb = new StringBuffer("MethodInfo{");
            sb.append("name='").append(name).append('\'');
            sb.append(", returnType=").append(returnType);
            sb.append(", paramCount=").append(paramCount);
            sb.append(", params=").append(params);
            sb.append('}');
            return sb.toString();
        }
    }

    static class VarInfo {
        String name;
        Type type;
        int length;

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("VarInfo{");
            sb.append("name='").append(name).append('\'');
            sb.append(", type=").append(type);
            sb.append(", length=").append(length);
            sb.append('}');
            return sb.toString();
        }
    }

    static byte isLastVarArray = -1;

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
    public static final String ELIF = "elif";
    public static final String WHILE = "while";
    public static final String FOR = "for";
    public static final String IN = "in";
    public static final String AS = "as";
    public static final String CASE = "case";

    public static final String CLASS = "class";

    // Some system procedures:
    public static final String PRINT = "print";
    public static final String PRINTLN = "println";

    public static final String EQUAL = "==";
    public static final String NEQUAL = "!=";
    public static final String GREATER = ">";
    public static final String GREATERE = ">=";
    public static final String SMALLER = "<";
    public static final String SMALLERE = "<=";

    public static final String AND = "&&";
    public static final String OR = "||";
    public static final String XOR = "^";
    public static final String NOT = "!";

    public static final String COMMENT = "//";

    static {
        methods.put(PRINT, null);
        methods.put(PRINTLN, null);
    }

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
        keywords.put(ELIF, Type.ELIF);
        keywords.put(WHILE, Type.WHILE);
        keywords.put(FOR, Type.FOR);
        keywords.put(IN, Type.IN);
        keywords.put(AS, Type.AS);
        keywords.put(CASE, Type.CASE);
        keywords.put(CLASS, Type.CLASS);
        keywords.put(COMMENT, Type.COMMENT);
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
        types.put(VOID, Type.VOID);
    }

    static Map<String, Op> ops = new HashMap<String, Op>();

    static {
        ops.put(EQUAL, Op.EQUAL);
        ops.put(NEQUAL, Op.NEQUAL);
        ops.put(GREATER, Op.GREATER);
        ops.put(GREATERE, Op.GREATERE);
        ops.put(SMALLER, Op.SMALLER);
        ops.put(SMALLERE, Op.SMALLERE);
        ops.put(AND, Op.AND);
        ops.put(OR, Op.OR);
        ops.put(XOR, Op.XOR);
        ops.put(NOT, Op.NOT);
    }

    static final long MAX_STACK_SIZE = 12800;

    static Stack<String> methodStack = new Stack<String>();

    static int labelIndex = 0;
    static int constStrIndex = 0;
    static int preIfCount = 0;
    static String endIfLabel = null;

    /**/
    static Type findKeyword(String name) {

        if (keywords.containsKey(name)) {
            return keywords.get(name);
        }

        if (isDefinedVar(name)) {
            return Type.VAR;
        }

        if (isReturn(name)) {
            return Type.RETURN;
        }

        if (isDefinedMethod(name)) {
            return Type.PROC;
        }

        if (name.equals(COMMENT)) {
            return Type.COMMENT;
        }

        return Type.NONE;
    }

    static MethodInfo getMethodInfo(String name) {
        return methods.get(name);
    }

    static void emit(String s) {
        if (shouldPush()) {
            pushCode(TAB + s);
        } else {
            write(TAB + s);
        }
    }

    static void emitLn(String s) {
        if (shouldPush()) {
            pushCodeLine(TAB + s);
        } else {
            writeLine(TAB + s);
        }
    }

    static void compileInfo(Object s) {
        System.out.println("[info] " + s);
    }

    static void compileWarn(Object s) {
        System.out.println("[warn] " + s);
    }

    static String newLabel() {
        labelIndex++;
        return "label" + labelIndex;
    }

    static String newConstStr() {
        constStrIndex++;
        return "__constStr" + constStrIndex;
    }

    static void postLabel(String l) {
        if (shouldPush()) {
            pushCodeLine(l + ":");
        } else {
            writeLine(l + ":");
        }
    }

    static void postMethodStartLabel(String name) {

        if ("main".equals(name)) {
            name = "_main";
        }

        if (shouldPush()) {
            pushCodeLine(name + ":");
        } else {
            writeLine(name + ":");
        }
    }

    static void postMethodEndLabel(String name) {
        if (shouldPush()) {
            pushCodeLine("_end_" + name + ":");
        } else {
            writeLine("_end_" + name + ":");
        }
    }

    static boolean isReturn(String name) {
        return "return".equals(name);
    }

    static boolean isDefinedVar(String name) {
        return isDefinedGlobalVar(name) || isDefinedLocalVar(name)
                || isParam(name);
    }

    static boolean isDefinedArray(String name) {
        if (isDefinedGlobalVar(name)) {
            return globalVariables.get(name).length > sizeOfType(globalVariables.get(name).type);
        }
        if (isDefinedLocalVar(name)) {
            return localVariables.get(name).size > sizeOfType(localVariables.get(name).type);
        }
        if (isParam(name)) {
            return params.get(name).size > sizeOfType(params.get(name).type);
        }
        return false;
    }

    static int getArraySize(String name) {
        if (isDefinedGlobalVar(name)) {
            return globalVariables.get(name).length;
        }
        if (isDefinedLocalVar(name)) {
            return localVariables.get(name).size;
        }
        if (isParam(name)) {
            return params.get(name).size;
        }
        return 0;
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
        VarInfo info = new VarInfo();
        info.name = name;
        info.type = toType(type);
        info.length = sizeOfType(info.type);
        if (isLastVarArray == 1 || isLastVarArray == -1) {
            storeCode("section .data");
            isLastVarArray = 0;
        }
        globalVariables.put(name, info);
    }

    static void defineGlobalArray(String name, String type) throws IOException {
        match('[');
        int size = getIntegerNum();
        if (size <= 0) {
            abort("array size should be positive number");
        }
        match(']');
        VarInfo info = new VarInfo();
        info.name = name;
        info.type = toType(type);
        info.length = sizeOfType(info.type) * size;
        globalVariables.put(name, info);
        if (isLastVarArray == 0 || isLastVarArray == -1) {
            emitLn("section .bss");
            isLastVarArray = 1;
        }
        switch (info.type) {
            case BOOL:
            case CHAR:
            case BYTE:
                emitLn(name + ":" + TAB + "resb" + TAB + size);
                break;
            case SHORT:
                emitLn(name + ":" + TAB + "resw" + TAB + size);
                break;
            case INT:
            case FLOAT:
                emitLn(name + ":" + TAB + "resq" + TAB + size);
                break;
            case LONG:
                emitLn(name + ":" + TAB + "rest" + TAB + size); // rest?
                break;
            default:
                abort("unexpected type: " + info.type);
        }
        // New line is not permitted here:
        if (look == '=') {
            match('=');
            match('{');
            // The count of initial values should match exactly the size of array:
            int i = 0;
            while (i < size) {
                getToken();
                if (!isConst(info.type, token)) {
                    abort("illegal const value:" + token + " of type:" + info.type);
                }
                if (info.type == Type.BOOL) {
                    token = boolNumeric(token);
                }
                pushInitCodeLine(TAB + "mov [" + info.name + "+" + sizeOfType(info.type) + "*" + i + "], " + token);
                i++;
                if (i < size) {
                    match(',');
                }
            }
            if (i != size) {
                abort("incorrect count of initial values (found " + i + ", expect " + size + ")");
            }
            match('}');
        }
        match(';');
    }

    static Type getGlobalVarType(String name) {
        return globalVariables.get(name).type;
    }

    static Type getLocalVarType(String name) {
        return localVariables.get(name).type;
    }

    static Type typeOf(String name) {
        if (isDefinedGlobalVar(name)) {
            return getGlobalVarType(name);
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

    static void defineLocalVar(String name, Type type, int offset, int size) {
        ParamInfo info = new ParamInfo();
        info.offset = offset;
        info.type = type;
        info.size = sizeOfType(type) * size;
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

    static void duplicate(String name) {
        abort("Duplicate Identifier " + name);
    }

    static char getChar() throws IOException {

        int res = codeReader.read();
        if (res == -1) {
            endOfFile = true;
        }
        look = (char)res;
        //look = (char) System.in.read();
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

    static String getAssignType(Type varType) {
        switch (varType) {
            case BOOL:
            case CHAR:
            case BYTE:
                return "byte";
            case SHORT:
                return "word";
            case INT:
                return "dword";
            case LONG:
            case FLOAT:
                // TODO what type should float variable use?
                return "qword";
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

    static String getIntParamReg(int index) {
        switch (index) {
            case 1:
                return "rdi";
            case 2:
                return "rsi";
            case 3:
                return "rdx";
            case 4:
                return "rcx";
            case 5:
                return "r8";
            case 6:
                return "r9";
            default:
                return null;
        }
    }

    static String getFloatParamReg(int index) {
        return "mm" + (index - 1);
    }

    static String defaultValue(Type varType) {
        return "0";
    }

    static String boolNumeric(String boolValue) {
        if (boolValue.equals("true")) {
            return "1";
        } else {
            return "0";
        }
    }

    // Only integer is permitted here:
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

    static boolean isProcedure(String str) {
        return methods.containsKey(str);
    }

    static boolean isAddOp(char c) {
        return c == '+' || c == '-';

    }

    static boolean isMulOp(char c) {
        return c == '*' || c == '/';
    }

    static boolean isWhite(char c) {
        return c == BLANK || c == TAB;
    }

    static void skipWhite() throws IOException {
        while (isWhite(look)) {
            getChar();
        }
    }

    static boolean isAlpha(char c) {
        char upC = Character.toUpperCase(c);
        return upC <= 'Z' && upC >= 'A';
    }

    static boolean isDigit(char c) {
        return (c <= '9' && c >= '0');
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
                return String.valueOf((int) getAChar());
            case BYTE:
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
        boolean neg = false;
        newLine();
        if (look == '-') {
            neg = true;
            getChar();
        }
        if (!isDigit(look)) {
            expected("Integer");
        }
        while (isDigit(look)) {
            val = 10 * val + look - '0';
            getChar();
        }
        skipWhite();
        return neg ? -val : val;
    }

    static boolean isIntType(Type type) {
        return type == Type.BYTE || type == Type.SHORT || type == Type.INT
                || type == Type.LONG || type == Type.CHAR;
    }

    static boolean isFloatType(Type type) {
        return type == Type.FLOAT;
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
    static Number getFloatNum() throws IOException {

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
        if (pointCount == 0) {
            return Integer.parseInt(val);
        }
        return Double.parseDouble(val);
    }

    static boolean isFloat(String num) {
        return num.contains("" + POINT);
    }

    static String getToken() throws IOException {
        newLine();
        String str = "";
        if (isNum(String.valueOf(look))) {
            token = String.valueOf(getFloatNum());
            return token;
        }

        if (isName(String.valueOf(look))) {
            token = getName();
            return token;
        }

        if (look == '\'') {
            return String.valueOf((int) getAChar());
        }

        if (isBoolOp(look)) {
            token = getBoolOp(look);
            return token;
        }

        if (look == '/') {
            getChar();
            if (look != '/') {
                abort("// needed");
            }
            token = COMMENT;
            return COMMENT;
        }

        str += look;
        skipWhite();
        token = str;
        return str;

    }

    static boolean isConst(Type type, String value) {
        if (!isBool(token) && !isFloat(token) && !isNum(token)) {
            return false;
        }

        if (isBool(token) && type != Type.BOOL) {
            return false;
        }
        if (isFloat(token) && type != Type.FLOAT) {
            return false;
        }
        if (isNum(token) && !isIntType(type) && type != Type.FLOAT) {
            return false;
        }
        return true;
    }

    static String getRelOp() throws IOException {
        newLine();
        String op = "";
        switch (look) {
            case '=':
                getChar();
                if (look == '=') {
                    op = EQUAL;
                    getChar();
                    break;
                }
                abort("not a relation operator:=");
            case '>':
                getChar();
                if (look == '=') {
                    op = GREATERE;
                    getChar();
                } else {
                    op = GREATER;
                }
                break;
            case '<':
                getChar();
                if (look == '=') {
                    op = SMALLERE;
                    getChar();
                } else {
                    op = SMALLER;
                }
                break;
            case '!':
                getChar();
                if (look != '=') {
                    abort("not a relation operator:!");
                }
                op = NEQUAL;
                getChar();
                break;
            default:
                abort("not a relation operator:" + look);
                break;
        }

        // scan to the next non-empty character:
        if (look == BLANK || look == TAB) {
            skipWhite();
        }
        return op;
    }

    static String getBoolOp(char ch) throws IOException {
        String op = "" + ch;
        char c = getChar();
        if (ch == '!') {
            return NOT;
        }
        if (ch == '&') {
            match('&');
            return AND;
        }

        if (ch == '|') {
            match('|');
            return OR;
        }

        if (ch == '^') {
            return XOR;
        }

        return null;
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

    static void match(String s) throws IOException {
        newLine();
        int index = 0;
        while (index < s.length() && look == s.charAt(index)) {
            getChar();
            index++;
        }
        if (index != s.length()) {
            expected(s);
        }
        skipWhite();
    }

    static void scan() throws IOException {
        getToken();
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

        emitLn("default rel");
        emitLn("section .text");
        emitLn("global _main");

        popInitCode();

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

        if (endOfFile) {
            end();
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
            if (endOfFile) {
                end();
            }
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
            storeCode(name + ":" + TAB + getStoreType(findKeyword(type)) + TAB
                    + varValue);
            pushInitCodeLine("mov " + getAssignType(findKeyword(type)) + " [" + name + "], " + varValue);
        } else {
            storeCode(name + ":" + TAB + getStoreType(findKeyword(type)) + TAB
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

        methodStack.push(name);
        postMethodStartLabel(name);
        if ("main".equals(name)) {
            emitLn("call _init");
        }
        doMethodParams(name);
        match('{');
        int offset = 0;
        String varType = getToken();

        turnOnPusher();
        while (isType(varType)) {
            matchType(varType);
            offset += doStoreLocalVar(toType(varType), offset);
            varType = getToken();
        }

        if (offset > MAX_STACK_SIZE) {
            abort("stack overflow :(");
        }
        turnOffPusher();
        methodProlog(offset);

        popAllCode();

        firstBlock(varType);
        match('}');
        postMethodEndLabel(name);
        methodEpilog();
        clearParams();
    }

    /**
     * <code>
     * if (condition) {
     * <p>
     * } elif (condition) {
     * <p>
     * } else {
     * <p>
     * }
     * </code>
     *
     * @throws IOException
     */
    static void doIf() throws IOException {
        match('(');
        String label = newLabel();
        preIfCount = 0;
        String endLabel = endIfLabel = newLabel();
        boolExpression();
        emitLn("cmp al, 0");
        emitLn("je " + label);
        match(')');
        match('{');
        block();
        match('}');
        endIfLabel = endLabel;
        emitLn("jmp " + endIfLabel);
        postLabel(label);
        preIfCount = 1;

    }

    static void doElse() throws IOException {
        if (preIfCount == 0) {
            abort("require a 'if' before 'else'");
        }
        preIfCount = 0; // end the if block
        String endLabel = endIfLabel;
        match('{');
        block();
        match('}');
        endIfLabel = endLabel;
        postLabel(endIfLabel);
        endIfLabel = null;
    }

    static void doElif() throws IOException {
        if (preIfCount == 0) {
            abort("require an 'if' before 'elif'");
        }
        preIfCount = 0;
        // store the end label:
        String endLabel = endIfLabel;
        match('(');
        String label = newLabel();
        boolExpression();
        emitLn("cmp al, 0");
        emitLn("je " + label);
        match(')');
        match('{');
        block();
        match('}');
        // revert the previous count of 'if':
        endIfLabel = endLabel;
        preIfCount = 1;
        emitLn("jmp " + endIfLabel);
        postLabel(label);
    }

    /**
     * <code>
     * int i = 0;
     * for (i <- 0:10) {
     * // code block
     * }
     * int array[10];
     * for (i <- array) {
     * // code block
     * }
     * </code>
     *
     * @throws IOException
     */
    static void doFor() throws IOException {
        match('(');
        String var = getName();
        if (!isDefinedVar(var)) {
            abort("not a legal variable:" + var);
        }
        //loadVar(var);
        //emitLn("mov rbx, rax");
        match("<-");
        getToken();
        if (isNum(token)) {
            emitLn("mov rbx, " + token);
        } else if (isDefinedVar(token)) {
            if (isDefinedArray(token)) {
                // TODO process when token is an array

            } else {
                loadVar(token);
                emitLn("mov rbx, rax");
            }
        } else {
            expected("const integer number or variable");
        }

        match(':');
        getToken();
        if (isNum(token)) {
            emitLn("mov rax, " + token);
        } else if (isDefinedVar(token)) {
            if (isDefinedArray(token)) {
                abort("array is invalid here");
            } else {
                loadVar(token);
            }
        } else {
            expected("const integer number or variable");
        }

        String startFor = newLabel();
        String endFor = newLabel();
        postLabel(startFor);
        emitLn("push rax");
        emitLn("mov rax, rbx");
        storeVar(typeOf(var), var, typeOf(var));
        emitLn("pop rax");
        emitLn("cmp rbx, rax");
        emitLn("jg " + endFor);
        emitLn("push rax");
        emitLn("push rbx");
        match(')');
        match('{');
        block();
        match('}');
        emitLn("pop rbx");
        emitLn("pop rax");
        emitLn("add rbx, 1");
        emitLn("jmp " + startFor);
        postLabel(endFor);
    }

    /**
     * <code>
     * while(condition) {
     * <p>
     * }
     * </code>
     */
    static void doWhile() throws IOException {
        match('(');
        String beginWhile = newLabel();
        postLabel(beginWhile);
        boolExpression();
        match(')');
        match('{');
        String endWhile = newLabel();
        emitLn("cmp al, 0");
        emitLn("je " + endWhile);
        block();
        match('}');
        emitLn("jmp " + beginWhile);
        postLabel(endWhile);
    }

    /**
     * <pre>
     *  case(variable) {
     *      1 ->
     *          block
     *      2 =>
     *          block
     *      * ->
     *          block
     *  }
     * </pre>
     * -> means break the case after block.
     * => means continue the case after block.
     */
    static void doCase() throws IOException {

        String value = null;
        char c = 0;
        match('(');
        getName();
        if (!isDefinedVar(token)) {
            abort("need a variable here");
        }
        loadVar(token);
        Type varType = typeOf(token);
        if (varType == Type.FLOAT || varType == Type.BOOL) {
            abort(varType + " is not allowed to be used in a case module.");
        }
        match(')');
        match('{');
        String endCase = newLabel();
        String nextCase = newLabel();
        newLine();
        getToken();
        while (!"}".equals(token) && ("*".equals(token) || isConst(varType, token))) {
            newLine();
            // Default case:
            if ("*".equals(token)) {
                c = '*';
                match('*');
            }

            if (c != '*') {
                emitLn("mov rbx, " + token);
                emitLn("cmp rbx, rax");
                emitLn("mov rax, 0");
                emitLn("setne al");
                emitLn("jne " + nextCase);
            }
            // Save the variable value:
            emitLn("push rax");
            // new line is not permitted here:
            if (look == '=' || look == '-') {
                c = look;
                getChar();
                if (look != '>') {
                    abort("need >");
                }
                getChar();
            } else {
                abort("need => or ->, found: " + look);
            }

            block();
            if (c == '-') {
                emitLn("jmp " + endCase);
            }
            c = 0;
            //compileInfo(token);
            postLabel(nextCase);
            nextCase = newLabel();
            // pop the variable value again:
            emitLn("pop rax");
        }
        match('}');
        postLabel(endCase);
    }

    static void callMethod(String name) throws IOException {

        if (PRINT.equals(name)) {
            callPrint();
            return;
        }

        if (PRINTLN.equals(name)) {
            callPrintln();
            return;
        }

        pushProcReg(name);
        paramList(name);
        popProcReg(name);
        call(name);
        popProcReg(name);
    }

    /**
     * Print something to terminal:
     * sample code:
     * <pre>
     *     int a= 9, b = 10;
     *     float c = 9.5;
     *     print(a + ", " + b + ", " + c);
     *     println("Hello World!");
     * </pre>
     * Basic idea is to identify every parameter connected by '+' and use system calls to print the parameter solely
     * according to its type.<br>
     * For instance in the preceding example,
     * <pre>
     *     print(a + ", " + b + ", " + c);
     * </pre>
     * would be decomposed into:
     * <pre>
     *     print(a);
     *     print(", ");
     *     print(b);
     *     print(", ");
     *     print(c);
     * </pre>
     *
     * @throws IOException
     */
    static void callPrint() throws IOException {
        match('(');
        while (true) {
            if (look == '\"') {
                // we meet a const string:
                printString();
            } else if (isDigit(look)) {
                // we meet a const number:
                doPrintString(String.valueOf(getNum()));
            } else {
                // it's a variable:
                doPrintVar();
            }
            if (look == '+') {
                match('+');
                continue;
            } else if (look == ')') {
                match(')');
                break;
            } else {
                abort("unexpected char:" + look);
            }
        }
    }

    static void callPrintln() throws IOException {
        callPrint();
        printNewLine();
    }

    static void printString() throws IOException {
        getChar();
        String str = "";
        while (look != '\"') {
            // TODO not support a string containing \"
            if (look == LF || look == CR) {
                abort("not permitted char:" + (int) look);
            }
            str += look;
            getChar();
        }
        match('\"');
        doPrintString(str);
    }


    static void doPrintString(String s) {
        String name = newConstStr();
        storeCode(name + ":" + TAB + "db " + "\"" + s + "\"");
        storeCode(".len: equ $ - " + name);
        emitLn("push rdx");
        emitLn("push rdi");
        emitLn("push rsi");
        emitLn("mov rax, 0x2000004");
        emitLn("mov rdi, 1");
        emitLn("mov rsi, " + name);
        emitLn("mov rdx, " + name + ".len");
        emitLn("syscall");
        emitLn("pop rsi");
        emitLn("pop rdi");
        emitLn("pop rdx");

    }

    static void doPrintln(String s) {
        doPrintString(s);
        printNewLine();
    }

    static void printNewLine() {
        emitLn("push rdx");
        emitLn("push rdi");
        emitLn("push rsi");
        emitLn("mov rax, 0x2000004");
        emitLn("mov rdi, 1");
        emitLn("mov rsi, " + NEW_LINE_CONST_STR);
        emitLn("mov rdx, 1");
        emitLn("syscall");
        emitLn("pop rsi");
        emitLn("pop rdi");
        emitLn("pop rdx");
    }

    static void doPrintVar() throws IOException {
        String name = getName();
        if (isDefinedArray(name)) {
            loadArray(name);
        } else {
            loadVar(name);
        }
        if (typeOf(name) == Type.FLOAT) {
            doPrintFloatVar();
        } else if (typeOf(name) == Type.CHAR) {
            doPrintCharVar();
        } else if (typeOf(name) == Type.BOOL) {
            doPrintBoolVar();
        } else {
            doPrintIntVar();
        }
    }

    static void doPrintIntVar() {
        emitLn("mov [__tmp_buf], rax");
        emitLn("mov rbx, 0");
        emitLn("call _push_digit");
    }

    static void doPrintCharVar() {
        emitLn("mov [__out_buf], rax");
        emitLn("push rdx");
        emitLn("push rdi");
        emitLn("push rsi");
        emitLn("mov rax, 0x2000004");
        emitLn("mov rdi, 1");
        emitLn("mov rsi, __out_buf");
        emitLn("mov rdx, 1");
        emitLn("syscall");
        emitLn("pop rsi");
        emitLn("pop rdi");
        emitLn("pop rdx");
    }

    static void doPrintBoolVar() {
        emitLn("call _print_bool");
    }

    // TODO It's a little more complicated in printing float variable via syscall.
    // So we skip it as for now.
    static void doPrintFloatVar() {

    }

    static void doReturn() throws IOException {
        String methodName = methodStack.pop();
        MethodInfo info = methods.get(methodName);
        if (info.returnType != Type.VOID) {
            Type expType = expression();
            if (!isAssignValid(info.returnType, expType)) {
                abort("procedure returned the wrong type " + expType);
            }
            if (info.returnType == Type.FLOAT) {
                // As a convention, floating number is returned in xmm0:
                emitLn("mov xmm0, [" + TEMP_FLOAT_VAR_NAME + "]");
            }
        }
        match(';');
        emitLn("jmp _end_" + info.name);
    }

    static void doComment() throws IOException {
        getChar();
        while (look != LF && look != CR) {
            getChar();
        }
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
            abort("different argument count from definition");
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
        Type type = methods.get(methodName).params.get(index - 1);
        if (isIntType(type)) {
            pushIntParam(type, intParamIndex(methodName, index));
        } else if (type == Type.FLOAT) {
            pushFloatParam(type, floatParamIndex(methodName, index));
        }
    }

    // RDI, RSI, RDX, RCX, R8, and R9.
    static void pushIntParam(Type type, int index) {
        emitLn("push rax");
    }

    static void pushFloatParam(Type type, int index) {

        if (index > 8) {
            emitLn("push qword [" + TEMP_FLOAT_VAR_NAME + "]");
        } else {
            emitLn("mov mm" + (index - 1) + ", [" + TEMP_FLOAT_VAR_NAME + "]");
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

            int size = 1;
            if (look == '[') {
                // An array to define:
                match('[');
                size = Integer.parseInt(getNum());
                match(']');
                offset += size * offsetPerVar;
            } else {
                offset += offsetPerVar;
            }
            defineLocalVar(name, type, offset, size);

            if (look == '=') {
                match('=');
                // simply prohibit initialization of array:
                if (size > 1) {
                    abort("local array initialization is not allowed");
                }
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
        emitLn("leave");
        emitLn("ret");
    }

    static void clearParams() {
        params.clear();
        localVariables.clear();
        currentOffset = 8;
    }

    static void pushProcReg(String methodName) {
        MethodInfo info = getMethodInfo(methodName);
        int intInd = 0;
        int floatInd = 0;
        for (Type type : info.params) {
            if (isIntType(type)) {
                intInd++;
                if (intInd <= 6) {
                    emitLn("push " + getIntParamReg(intInd));
                }
            } else if (isFloatType(type)) {
                floatInd++;
                if (floatInd <= 8) {
                    // TODO push float parameter
                }
            }

        }
    }

    static void popProcReg(String methodName) {
        MethodInfo info = getMethodInfo(methodName);
        int intInd = 0;
        int floatInd = 0;
        List<String> intRegs = new ArrayList<String>();
        for (Type type : info.params) {
            if (isIntType(type)) {
                intInd++;
                if (intInd <= 6) {
                    // TODO pop should be in reversed order:
                    intRegs.add(getIntParamReg(intInd));
                    //emitLn("pop " + getIntParamReg(intInd));
                }
            } else if (isFloatType(type)) {
                floatInd++;
                if (floatInd <= 8) {
                    // TODO pop float parameter
                }
            }
        }

        for (int i = intRegs.size() - 1; i >= 0; i--) {
            emitLn("pop " + intRegs.get(i));
        }
    }

    static void block() throws IOException {
        scan();
        firstBlock(token);
    }

    static void firstBlock(String word) throws IOException {

        keyType = findKeyword(word);
        //compileInfo(keyType);
        token = word;
        while (keyType != Type.NONE) {
            //compileInfo(token);
            switch (keyType) {
                case IF:
                    doIf();
                    break;
                case ELIF:
                    doElif();
                    break;
                case ELSE:
                    doElse();
                    break;
                case WHILE:
                    doWhile();
                    break;
                case FOR:
                    doFor();
                    break;
                case CASE:
                    doCase();
                    break;
                case VAR:
                    assignment(typeOf(token), token);
                    break;
                case PROC:
                    callMethod(token);
                    match(';');
                    break;
                case RETURN:
                    doReturn();
                    break;
                case COMMENT:
                    doComment();
                    break;
                default:
                    abort("illegal identifier:" + word);
            }

            token = getToken();
            keyType = findKeyword(token);
            //compileInfo(token);
            // We have to remember whether there was a previous 'if' only when keyword is 'else' or 'elif':
            if (keyType != Type.ELIF && keyType != Type.ELSE && preIfCount != 0) {
                //compileInfo("inner " + token);
                postLabel(endIfLabel);
                preIfCount = 0;
            }
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
            case '[':
                // Start to support array type:
                defineGlobalArray(name, type);
                break;
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
        double var = -((Double) getFloatNum());
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

        int offset = 0;
        String index = null;
        if (isDefinedArray(name)) {
            int size = getArraySize(name);
            match('[');
            emitLn("push rcx");
            emitLn("push rax");
            expression();
            // TODO compare index and array size to optionally generate a runtime exception. Plan is that if out of
            // bound is detected, print an error and exit the program, so first we need a print function.
            emitLn("mov rcx, rax");
            index = "rcx";
            match(']');
        }
        match('=');
        if (isDefinedArray(name)) {
            storeArray(type, name, index, doAssignment(type));
            emitLn("pop rax");
            emitLn("pop rcx");
        } else {
            storeVar(type, name, doAssignment(type));
        }

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

    static void storeArray(Type type, String name, String index, Type expType) {

        if (type == Type.FLOAT) {
            if (expType == Type.FLOAT) {
                emitLn("fld qword [" + TEMP_FLOAT_VAR_NAME + "]");
            } else {
                // An integer-to-float assignment:
                emitLn("fild qword [rax]");
            }
            if (isDefinedGlobalVar(name)) {
                emitLn("fstp qword [" + name + " + " + index + " * " + sizeOfType(type) + "]");
            } else if (isDefinedLocalVar(name)) {
                emitLn("fstp qword [rbp-" + getLocalVarOffset(name) + " + " + index + " * " + sizeOfType(type) + "]");
            } else {
                abort("expected legal variable, but found " + name);
            }
        } else {
            // store with the correct register:
            String reg = regOfType(typeOf(name));
            if (isDefinedGlobalVar(name)) {
                emitLn("push rbx");
                emitLn("lea rbx, [" + name + "]");
                emitLn("mov [rbx + " + index + " * " + sizeOfType(type) + "], " + reg);
                emitLn("pop rbx");
            } else if (isDefinedLocalVar(name)) {
                emitLn("mov [rbp-" + getLocalVarOffset(name) + " + " + index + " * " + sizeOfType(type) + "], " + reg);
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

            emitLn("mov " + reg + ", [" + name + "]");
        } else if (isDefinedLocalVar(name)) {
            emitLn("mov " + reg + ", [rbp-" + getLocalVarOffset(name) + "]");
        } else if (isParam(name)) {
            if (getParamIndex(name) <= 6) {
                emitLn("mov rax, " + getIntParamReg(getParamIndex(name)));
            } else {
                emitLn("mov " + reg + ", [rbp+" + getParamOffset(name) + "]");
            }
        } else {
            abort("expected legal variable, but found " + name);
        }

        // Sign extension, we extend the value to rax:
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

    static Type loadArray(String name) throws IOException {

        String index = null;
        match('[');
        emitLn("push rcx");
        emitLn("push rax");
        expression();
        emitLn("mov rcx, rax");
        emitLn("pop rax");
        index = "rcx";
        match(']');
        if (typeOf(name) == Type.FLOAT) {
            return loadFloatArray(name, index);
        }
        emitLn("mov rax, 0"); // Clear all bits of rax

        String reg = regOfType(typeOf(name));

        if (isDefinedGlobalVar(name)) {
            emitLn("push rbx");
            emitLn("lea rbx, [" + name + "]");
            emitLn("mov " + reg + ", [rbx + " + index + " * " + sizeOfType(typeOf(name)) + "]");
            emitLn("pop rbx");
        } else if (isDefinedLocalVar(name)) {
            emitLn("mov " + reg + ", [rbp-" + getLocalVarOffset(name) + " + " + index + " * " + sizeOfType(typeOf(name)) + "]");
        } else if (isParam(name)) {
            emitLn("mov " + reg + ", [rbp+" + getParamOffset(name) + " + " + index + " * " + sizeOfType(typeOf(name)) + "]");
        } else {
            abort("expected legal variable, but found " + name);
        }

        emitLn("pop rcx");

        // Sign extension, we extend the value to rax:
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

    static Type loadFloatArray(String name, String index) {

        if (isDefinedGlobalVar(name)) {

            emitLn("fld qword [" + name + " + " + index + " * " + sizeOfType(typeOf(name)) + "]");
            emitLn("fstp qword [" + TEMP_FLOAT_VAR_NAME + "]");
        } else if (isDefinedLocalVar(name)) {
            emitLn("fld qword [rbp-" + getLocalVarOffset(name) + " + " + index + " * " + sizeOfType(typeOf(name)) + "]");
            emitLn("fstp qword [" + TEMP_FLOAT_VAR_NAME + "]");
        } else if (isParam(name)) {
            emitLn("fld qword [rbp+" + getParamOffset(name) + " + " + index + " * " + sizeOfType(typeOf(name)) + "]");
            emitLn("fstp qword [" + TEMP_FLOAT_VAR_NAME + "]");
        } else {
            abort("expected legal variable, but found " + name);
        }
        return Type.FLOAT;
    }

    static Type factor() throws IOException {

        newLine();
        if (look == '(') {
            match('(');
            Type res = expression();
            match(')');
            return res;
        } else if (isAlpha(look)) {
            getName();
            // Don't forget bool value is also a non-number string. Usually a
            // bool assignment is invalid in an expression, but we will just
            // leave the decision to subsequent program.
            if (isBool(token)) {
                return loadBool(token);
            }
            // add procedure name
            if (isProcedure(token)) {
                // Save token from being changed:
                String methodName = token;
                callMethod(token);
                return methods.get(methodName).returnType;
            }
            // load array element:
            if (isDefinedArray(token)) {
                return loadArray(token);
            }
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
            emitLn("jo _handle_overflow");
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

        emitLn("jo _handle_overflow");
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
        emitLn("push rax");
        emitLn("mov rax, rbx");
        emitLn("pop rbx");
        emitLn("sub rax, rbx");

        // Consider the possibility of overflowing:
        if (firstType == Type.LONG || secondType == Type.LONG) {
            emitLn("jo _handle_overflow");
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

        emitLn("jo _handle_overflow");
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
        emitLn("jo _handle_overflow");

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

        emitLn("jo _handle_overflow");
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
        emitLn("push rax");
        emitLn("mov rax, rbx");
        emitLn("pop rbx");
        emitLn("idiv rbx");
        emitLn("jo _handle_overflow");
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

        emitLn("jo _handle_overflow");
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

        Type firstType = firstFactor();
        if (firstType == Type.BOOL) {
            return Type.BOOL;
        }
        Type secondType = term1(firstType);
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
        if (firstType == Type.BOOL) {
            return Type.BOOL;
        }
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

    // ----------------------------------------------------------------------
    // Bool expression:
    // Remember that 0 represents true, 1 represents false.
    static void boolExpression() throws IOException {

        boolTerm();
        getToken();
        while (isOrOp(token)) {
            emitLn("push rax");
            switch (getOpType(token)) {
                case OR:
                    boolOr();
                    break;
                case XOR:
                    boolXor();
                    break;
            }
        }
    }

    static void boolTerm() throws IOException {
        notFactor();
        getToken();
        while (getOpType(token) == Op.AND) {
            emitLn("push rax");
            notFactor();
            emitLn("pop rbx");
            emitLn("and al, bl");
        }
    }

    static void boolOr() throws IOException {
        boolTerm();
        emitLn("pop rbx");
        emitLn("or al, bl");
    }

    static void boolXor() throws IOException {
        boolTerm();
        emitLn("pop rbx");
        emitLn("xor al, bl");
    }

    static boolean isOrOp(String token) {
        return "||".equals(token) || "^".equals(token);
    }

    static void notFactor() throws IOException {
        if (look == '!') {
            match('!');
            boolFactor();
            emitLn("xor rax, -1"); // Negate the value in rax
        } else {
            boolFactor();
        }
    }

    static void boolFactor() throws IOException {
        relation();
    }

    static boolean getBoolean() throws IOException {

        if (!isBool(token)) {
            expected("Boolean Literal");
        }
        getChar();
        return "true".equals(token);
    }

    static void relation() throws IOException {
        Type expType = expression();
        if (expType == Type.BOOL) {
            return;
        }
        token = getRelOp();
        if (isRelop(token)) {
            push(expType);
            switch (getOpType(token)) {
                case EQUAL:
                    equals(expType);
                    break;
                case NEQUAL:
                    notEquals(expType);
                    break;
                case SMALLER:
                    less(expType);
                    break;
                case GREATER:
                    greater(expType);
                    break;
                case GREATERE:
                    greaterEqual(expType);
                    break;
                case SMALLERE:
                    lessEqual(expType);
                    break;
            }
            //emitLn("cmp al, 0");
        }
    }

    static boolean isRelop(String op) {
        return EQUAL.equals(op) || NEQUAL.equals(op) || GREATER.equals(op) ||
                GREATERE.equals(op) || SMALLER.equals(op) || SMALLERE.equals(op);
    }

    static boolean isBoolOp(char c) {
        return (c == '&' || c == '|' || c == '^' || c == '!');
    }

    static Op getOpType(String op) {
        return ops.get(op);
    }

    static void equals(Type type) throws IOException {
        compare(type);
        emitLn("sete al");
        emitLn("and rax, 0xFF"); // clear other bits of rax
    }

    static void notEquals(Type type) throws IOException {
        compare(type);
        emitLn("setne al");
        emitLn("and rax, 0xFF");
    }

    static void less(Type type) throws IOException {
        compare(type);
        emitLn("setl al");
        emitLn("and rax, 0xFF");
    }

    static void greater(Type type) throws IOException {
        compare(type);
        emitLn("setg al");
        emitLn("and rax, 0xFF");
    }

    static void greaterEqual(Type type) throws IOException {
        compare(type);
        emitLn("setge al");
        emitLn("and rax, 0xFF");

    }

    static void lessEqual(Type type) throws IOException {
        compare(type);
        emitLn("setle al");
        emitLn("and rax, 0xFF");
    }

    static void compare(Type type) throws IOException {
        Type expType = expression();
        if (type == Type.FLOAT || expType == Type.FLOAT) {
            compareFloat(type, expType);
        } else {
            compareInt();
        }
    }

    static void compareFloat(Type type, Type expType) {
        if (type != Type.FLOAT) {
            emitLn("pop rax");
            emitLn("fild qword [rax]");
        }
        if (expType != Type.FLOAT) {
            emitLn("fild qword [rax]");
            emitLn("fstp qword [" + TEMP_FLOAT_VAR_NAME + "]");
        }

        emitLn("fstp st0");
        emitLn("fcom [" + TEMP_FLOAT_VAR_NAME + "]");
    }

    static void compareInt() {
        emitLn("pop rbx");
        emitLn("cmp rbx, rax");
        //emitLn("mov rax, 0");
    }

    // End of bool expression
    // --------------------------------------------------------------


    static void defineHandleOverFlow() {
        postLabel("_handle_overflow");
        // Simply exit the program:
        exit(1);
    }

    static void exit(int code) {
        emitLn("pop	rbp");
        emitLn("mov rax, " + code);
        emitLn("ret");
    }

    public static void main(String[] args) throws IOException {

        codeReader = new BufferedReader(new FileReader("qsort.jude"));
        init();
        topDecls();
        program();
    }

}