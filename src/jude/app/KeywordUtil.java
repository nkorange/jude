package jude.app;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author zpf.073@gmail.com
 *
 */
public class KeywordUtil {

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
	
	private static Map<String, KeywordType> keywords;
	static {
		
		keywords = new HashMap<String, KeywordType>();
		keywords.put(INT, KeywordType.INT);
		keywords.put(LONG, KeywordType.LONG);
		keywords.put(BYTE, KeywordType.BYTE);
		keywords.put(SHORT, KeywordType.SHORT);
		keywords.put(BOOL, KeywordType.BOOL);
		keywords.put(CHAR, KeywordType.CHAR);
		keywords.put(IF, KeywordType.IF);
		keywords.put(ELSE, KeywordType.ELSE);
		keywords.put(WHILE, KeywordType.WHILE);
		keywords.put(FOR, KeywordType.FOR);
		keywords.put(IN, KeywordType.IN);
		keywords.put(AS, KeywordType.AS);
		keywords.put(SWITCH, KeywordType.SWITCH);
		keywords.put(CASE, KeywordType.CASE);
		keywords.put(CLASS, KeywordType.CLASS);
	}
	
	public static KeywordType findKeyword(String name) {
		
		if (keywords.containsKey(name)) {
			return keywords.get(name);
		}
		return KeywordType.NONE;
	}
	
	
}
