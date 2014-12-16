/**
 * The OpenDMAP for Protege Project
 * February 2005
 */
package edu.uchsc.ccp.opendmap;

import java.util.Collection;
import java.util.Vector;

/**
 * This class represents a text string within DMAP.
 * 
 * @author Will Fitzgerald
 * @author R. James Firby
 */
public class TextItem extends DMAPItem {
	private String text;

	/**
	 * Create a new DMAP Item for the given text string.
	 * 
	 * @param text The string this item represents.
	 */
	public TextItem(String text) {
		super();
		this.text = text;
	}

	/**
	 * Get the text string this item represents.
	 * 
	 * @return The text string this item represents.
	 */
	public String getText() {
		return this.text;
	}

	/*
	 * @see DMAPItem#toDescriptiveString
	 */
	String toDescriptiveString() {
		return "string \"" + text + "\"";
	}
	
	/**
	 * Return a string suitable for debugging output.
	 */
	public String toString() {
		return "tf\"" + this.text + "\"";
	}

	/**
	 * A <code>TextItem</code> is equal to another if their internal strings
	 * are equal.
	 */
	public boolean equals(Object thing) {
		if (thing == null) {
			return false;
		} else if (thing instanceof TextItem) {
			TextItem item = (TextItem) thing;
			if (text == null) {
				return (item.getText() == null) ;
			} else {
				return text.equals(item.getText());
			}
		} else {
			return false;
		}
	}

	/* -------------------------------------------------------------- */
	/* Required public methods                                        */
	
	/**
	 * A <code>TextItem</code> is not a class.
	 */
	public boolean isClass() {
		return false;
	}

	/**
	 * A <code>TextItem</code> is an instance.
	 */
	public boolean isInstance() {
		return true;
	}

	/**
	 * A <code>TextItem</code> is an ancestor only of itself.
	 */
	public boolean isa(DMAPItem ancestor) {
		if (this.equals(ancestor)) 
			return true;
		return false;
	}

	/**
	 * A <code>TextItem</code> is the only abstraction of itself.
	 */
	public Vector<DMAPItem> allAbstractions() {
		Vector<DMAPItem> v = new Vector<DMAPItem>(1);
		v.add(this);
		return v;
	}

	/**
	 * A <code>TextItem</code> is the only instance of itself.
	 */
	public Vector<DMAPItem> allInstances() {
		Vector<DMAPItem> v = new Vector<DMAPItem>(1);
		v.add(this);
		return v;
	}
	
	/**
	 * A <code>TextItem</code> is always consistent with slot information.
	 */
	Vector<DMAPItem> findInstances (Collection<InfoPacket> information) {
		Vector<DMAPItem> instances = new Vector<DMAPItem>();
		instances.add(this);
		return instances;
	}

  String getReferenceString(Collection<InfoPacket> information, Collection<String> properties) {
		return toPatternString();
	}

	/* -------------------------------------------------------------- */
	/* Methods required to use this item in a pattern                 */
	
	/**
	 * The only target for a <code>TextItem</code> is itself.
	 */
	Vector<DMAPItem> getTargets() {
		Vector<DMAPItem> v = new Vector<DMAPItem>(1);
		v.add(this);
		return v;
	}

	/*
	 * Return a pattern string representing this item.
	 * 
	 * @see DMAPItem#toPatternString
	 */
	public String toPatternString() {
		if (text == null) {
			return "<NULL>";
		} else {
			return "\"" + escapeString(text) + "\"";
		}
	}
	
//	public boolean matches(DMAPItem target) {
//		return matches((TextItem) target);
//	}
//
//	public boolean matches(TextItem target) {
//		return this.text.equals(target.text);
//	}

	/* -------------------------------------------------------------- */
	/* Methods required to use this item in a prediction              */
	
	/**
	 * Get a new prediction map appropriate for this type
	 * of predicted item.
	 */
	PredictionMap getNewPredictionMap() {
		return new StringPredictionMap();
	}
	
	/**
	 * Get a key that identifies which prediction map should
	 * be used for items of this class.
	 */
	PredictionTable.Key getTableKey() {
		return PredictionTable.Key.TEXT;
	}

	/* -------------------------------------------------------------- */
	/* Utility methods                                                */

	/**
	 * Escape the characters in a string.
	 * 
	 * @param input The characters that may need escaping
	 * @return The string with escaped characters in it
	 */
	static String escapeString(String input) {
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<input.length(); i++) {
			char c = input.charAt(i);
			if (c == '"') sb.append("\\\"");
			else if (c == '\n') sb.append("\\n");
			else if (c == '\t') sb.append("\\t");
			else if (c == '\b') sb.append("\\b");
			else if (c == '\f') sb.append("\\f");
			else if (c == '\r') sb.append("\\r");
			else if (c == '\n') sb.append("\\n");
			else sb.append(c);
		}
		return sb.toString();
	}
	
	/**
	 * Convert escaped characters into normal characters.
	 * 
	 * @param input The characters that might be escaped
	 * @return The unescaped characters
	 */
	static String unescapeString(String input) {
		StringBuilder sb = new StringBuilder();
		boolean inEscape = false;
		boolean inNumber = false;
		int num = 0;
		for (int i=0; i<input.length(); i++) {
			// Get the next character
			char c = input.charAt(i);
			boolean consumed = false;
			// See if its part of an escape sequence
			if (inEscape && inNumber) {
				// A code point reference
				if (Character.isDigit(c)) {
					num = (num * 8) + Character.digit(c, 8);
					consumed = true;
				} else {
					sb.append(Character.toChars(num));
					inEscape = false;
				}
			} else if (inEscape) {
				// The next character after an escape
				consumed = true;
				inEscape = false;
				if (c == 't') sb.append("\t");
				else if (c == 'r') sb.append("\r");
				else if (c == 'n') sb.append("\n");
				else if (c == 'b') sb.append("\b");
				else if (c == 'f') sb.append("\f");
				else if (Character.isDigit(c)) {
					num = Character.digit(c, 8);
					inNumber = true;
					inEscape = true;
				} else
					sb.append(c);
			}
			// If the character hasn't been used, use it now
			if (!consumed) {
				if (c == '\\') {
					inEscape = true;
					inNumber = false;
				} else {
					sb.append(c);
				}
			}
		}
		// There may be a pending escape
		if (inEscape && inNumber) {
			sb.append(Character.toChars(num));
		} else if (inEscape) {
			sb.append("\\");
		}
		// Done
		return sb.toString();
	}


//	public static void main(String[] args) {
//		TextItem stf = new TextItem("Will");
//		System.out.println("isInstance 'Will'? " + stf.isInstance());
//		System.out.println("isClass 'Will'? " + stf.isClass());
//		System.out.println("isa 'Will', 'Will'? " + stf.isa(stf));
//		System.out.println("isa 'Will', 'Dog'? "
//				+ stf.isa(new TextItem("Dog")));
//		System.out.println("all Abstractions of 'Will'? "
//				+ stf.allAbstractions());
//		System.out.println("all instances of 'Will'? " + stf.allInstances());
//		System.out.println("Matches self? " + stf.matches(stf));
//		System.out.println("Matches copy of self? "
//				+ stf.matches(new TextItem("Will")));
//
//	}
}
