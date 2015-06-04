package jude.app;

import java.io.IOException;

/**
 * 
 * @author zpf.073@gmail.com
 *
 */
public class Entry {
	
	static char look;
	
	static String value;
	
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
	
	static void getChar() throws IOException {
		look = (char) System.in.read();
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
			value += Character.toUpperCase(look);
			getChar();
		}
		skipWhite();
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

	}
	
	static void init() throws IOException {
		getChar();
		scan();
	}

	public static void main(String[] args) throws IOException {
		
		init();
		
	}

}
