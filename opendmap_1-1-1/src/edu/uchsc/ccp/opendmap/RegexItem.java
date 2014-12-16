/**
 * The OpenDMAP for Protege Project
 * July 2005
 */
package edu.uchsc.ccp.opendmap;

import java.util.Collection;
import java.util.Vector;

/**
 * This class represents a regular expression to be
 * matched against input text strings.
 * 
 * @author R. James Firby
 */

/* Changes (most recent first)
 * 
 * 09-13-05 rjf - Made sure that null regex pattern would not cause errors.
 */
public class RegexItem extends DMAPItem {
	
	/* The regex string compiled into a regex Pattern */
	private java.util.regex.Pattern regex = null;
	/* Message for printing the regex */
	private String message = null;

	/**
	 * Create a new regex item to represent the pattern
	 * string supplied.
	 * 
	 * @param text The regex pattern as a string.
	 */
	RegexItem(String text) {
		super();
		if (text == null) {
			this.regex = null;
		} else {
			this.regex = java.util.regex.Pattern.compile(text);
		}
	}

	/**
	 * Create a new regex item to represent the pattern
	 * string supplied.
	 * 
	 * @param text The regex pattern as a string.
	 * @param message A nice way to print this regex.
	 */
	RegexItem(String text, String message) {
		super();
		if (text == null) {
			this.regex = null;
		} else {
			this.regex = java.util.regex.Pattern.compile(text);
		}
		this.message = message;
	}

	/**
	 * Get the compiled regex pattern being used by this item.
	 * 
	 * @return The compiled regex pattern being used by this item.
	 */
	public java.util.regex.Pattern getRegexPattern() {
		return regex;
	}

	/*
	 * @see DMAPItem#toDescriptiveString
	 */
	public String toDescriptiveString() {
		if (regex == null) {
			return "regex r<NULL>";
		} else {
			return "regex r'" + regex.pattern() + "'";
		}
	}

	/**
	 * A string suitable for debugging output.
	 */
	public String toString() {
		if (regex == null) {
			return "xf<NULL>";
		} else if (message != null) {
			return "xf<" + message + ">";
		} else {
			return "xf\"" + regex.pattern() + "\"";
		}
	}

	/**
	 * A <code>RegexItem</code> is equal to another if their pattern strings
	 * are equal.
	 */
	public boolean equals(Object thing) {
		if (thing == null) {
			return false;
		} else if (thing instanceof RegexItem) {
			RegexItem item = (RegexItem) thing;
			if (regex == null) {
				return (item.getRegexPattern() == null) ;
			} else {
				return regex.pattern().equals(item.getRegexPattern().pattern());
			}
		} else {
			return false;
		}
	}

	/* -------------------------------------------------------------- */
	/* Required public methods                                        */

	/**
	 * A <code>RegexItem</code> is not a class.
	 */
	public boolean isClass() {
		return false;
	}

	/**
	 * A <code>RegexItem</code> is an instance.
	 */
	public boolean isInstance() {
		return true;
	}

	/**
	 * A <code>RegexItem</code> is an ancestor only of itself.
	 */
	public boolean isa(DMAPItem ancestor) {
		if (this == ancestor)
			return true;
		return false;
	}

	/**
	 * A <code>RegexItem</code> is its only abstraction.
	 */
	public Vector<DMAPItem> allAbstractions() {
		Vector<DMAPItem> v = new Vector<DMAPItem>(1);
		v.add(this);
		return v;
	}

	/**
	 * A <code>RegexItem</code> is its only instance.
	 */
	public Vector<DMAPItem> allInstances() {
		Vector<DMAPItem> v = new Vector<DMAPItem>(1);
		v.add(this);
		return v;
	}

	/**
	 * A <code>RegexItem</code> is always consistent with slot information
	 */
	Vector<DMAPItem> findInstances (Collection<InfoPacket> information) {
		Vector<DMAPItem> instances = new Vector<DMAPItem>();
		instances.add(this);
		return instances;
	}
	
	public String getText() {
		return toPatternString();
	}
	
	String getReferenceString(Collection<InfoPacket> information, Collection<String> properties) {
		if (regex == null) {
			return "r<NULL>";
		} else if (message != null) {
			return "r<" + message + ">";
		} else {
			return "r'" + regex.pattern() + "'";
		}
	}

	/* -------------------------------------------------------------- */
	/* Methods required to use this item in a pattern                 */

	/**
	 * The only target for a <code>RegexItem</code> is itself.
	 */
	Vector<DMAPItem> getTargets() {
		Vector<DMAPItem> v = new Vector<DMAPItem>(1);
		v.add(this);
		return v;
	}

	public String toPatternString() {
		if (regex == null) {
			return "<NULL>";
		} else {
			return "r\"" + regex.pattern() + "\"";
		}
	}
	
//	public boolean matches(DMAPItem target) {
//		return matches((RegexItem) target);
//	}
//
//	public boolean matches(RegexItem target) {
//		return false; //this.text.equals(target.text);
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
	
}
