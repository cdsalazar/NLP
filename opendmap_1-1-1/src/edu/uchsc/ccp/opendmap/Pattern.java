package edu.uchsc.ccp.opendmap;

import java.util.Vector;

/**
 * A pattern is what DMAP is looking to find in the input text.
 * <p>
 * Here it is represented as a tree of subpatterns that organize
 * base elements.  The base elements are what can actually be recognized.
 * The patterns place various sorts of ordering constraints on the base
 * elements within the pattern.
 * <p>
 * Patterns can be normalized or not.  A normalized pattern has been
 * massaged until it's first element is a single base element that
 * can be recognized by the parser.  Normalizing a pattern may require
 * generating an arbitrary number of alternate patterns with different
 * initial base elements.
 * 
 * @author R. James Firby
 * @author Will Fitzgerald
 */

/* Changes (most recent first)
 * 
 * 01-05-06 rjf - Added clause sets.
 * 09-14-05 rjf - Minor changes to Javadoc comments.
 */
public class Pattern {
	
	public enum Operator {
		SINGLE,        // A sequence of one subpattern
		STAR,          // The subpatterns might appear any number of times
		PLUS,          // The subpatterns must appear at least once
		OPTIONAL,      // The subpatterns may appear only once
		ALTERNATION,   // Any one of the subpatterns must appear
		SEQUENCE,      // The subpatterns must appear in order
		BASE,          // This pattern is a single base element to match
		CLAUSESET      // This pattern holds a set clause patterns that can come in any order
	}

	private Operator operator = Operator.BASE;

	private DMAPItem baseItem = null;
	private Vector<Pattern> subPatterns = null;
	
	private DMAPItem key = null;
	
	private boolean isMainClause = false;

	/**
	 * Create a new pattern of the given type with the given constituents.
	 * Do not use this method to create a BASE pattern.
	 * 
	 * @param kind The type of pattern being created
	 * @param constituents The sub pattern pieces for this pattern
	 */
	public Pattern(Operator kind, Vector<Pattern> constituents) {
		super();
		assert (kind != Operator.BASE) : "A base pattern cannot have subpatterns";
		assert (constituents != null) : "Patterns must not be null";
		assert (constituents.size() > 0): "Patterns must not be empty";
		this.operator = kind;
		this.subPatterns = constituents;
	}

	/**
	 * Create a new pattern of the given type with a single constituent.
	 * Do not use this method to create a BASE pattern.
	 * 
	 * @param kind The type of pattern being created
	 * @param constituent The single sub pattern for this pattern
	 */
	public Pattern(Operator kind, Pattern constituent) {
		super();
		assert (kind != Operator.BASE) : "A base pattern cannot have subpatterns";
		assert (constituent != null) : "Patterns must not be null";
		this.operator = kind;
		this.subPatterns = new Vector<Pattern>(1);
		this.subPatterns.addElement(constituent);
	}

	/**
	 * Create a BASE pattern item for the given content item.
	 * 
	 * @param content A base element that can be recognized by the parser
	 */
	public Pattern(DMAPItem content) {
		super();
		assert (content != null) : "Content must not be null";
		this.operator = Operator.BASE;
		this.baseItem = content;
	}
	
	/**
	 * A private method for creating new patterns with specific key elements.
	 * Used only while normalizing patterns.
	 * 
	 * @param kind The type of pattern being created
	 * @param constituents The sub pattern pieces for this pattern
	 * @param key The key item associated with this normalized pattern
	 */
	private Pattern(Operator kind, Vector<Pattern> constituents, DMAPItem key) {
		super();
		this.operator = kind;
		this.subPatterns = constituents;
		this.key = key;
		if ((kind == Operator.SINGLE) || (kind == Operator.SEQUENCE)) {
			this.baseItem = constituents.elementAt(0).getContent();
		}
	}

	/**
	 * A private method for creating new BASE patterns with specific key elements.
	 * Used only while normalizing patterns.
	 * 
	 * @param content The base item content for this pattern
	 * @param key The key item associated with this normalized pattern
	 */
	private Pattern(DMAPItem content, DMAPItem key) {
		super();
		this.operator = Operator.BASE;;
		this.baseItem = content;
		this.key = key;
	}

	/**
	 * @return Returns the content.
	 */
	public DMAPItem getContent() {
		return baseItem;
	}
	
	public void setAsMainClause(boolean value) {
		isMainClause = value;
	}
	
	/**
	 * Predicate to tell if this pattern is normalized or not.
	 * <p>
	 * A normalized pattern has been massaged to be expecting a
	 * single recognizable parser item.
	 * 
	 * @return Whether this pattern has been normalized
	 */
	public boolean isNormalized() {
		return (key != null);
	}
	
	/**
	 * Get the base element that this normalized pattern starts
	 * with.
	 * 
	 * @return The normalized base element
	 */
	public DMAPItem getKey() {
		return key;
	}

	/**
	 * Process a pattern to find the first base element that it is waiting for.
	 * There may, in fact, be many possible base elements so this method
	 * may return a collection of new patterns, each normalized to a different
	 * element.
	 * 
	 * This method may copy as much or as little of the pattern as required.
	 * 
	 * Normalizing a pattern should always result in a sequence.
	 * 
	 * @return The normalized versions of this pattern.
	 */
	public Vector<Pattern> normalize() {
		// This method must not side effect any of the constituents of this
		// pattern.  They may be in use elsewhere.  Everything must be copied
		// if it needs to be changed.
		
		// If no work necessary then escape
		if (isNormalized()) {
			Vector<Pattern> result = new Vector<Pattern>(1);
			result.addElement(this);
			return result;
		}
		
		// Process each type of pattern as required to create a set of
		// normalized patterns.
		switch (operator) {
		case BASE:
			// A base pattern may still have specific instances to see
			return normalizeBase(baseItem);
		case SINGLE:
			// Normalize a single by normalizing its child
			return subPatterns.elementAt(0).normalize();
		case SEQUENCE:
			// A sequence needs to have its first component normalized
			return normalizeSequence(subPatterns);
		case STAR:
			// Normalizing means pulling one copy out in front of the star
			return normalizeStar(subPatterns);
		case PLUS:
			// Normalizing means pulling one copy out in front of the plus
			return normalizePlus(subPatterns);
		case OPTIONAL:
			// Normalizing means normalizing the content and making it optional
			return normalizeOptional(subPatterns);
		case ALTERNATION:
			// Normalizing means creating a pattern for each part
			return normalizeAlternation(subPatterns);
		case CLAUSESET:
			// Normalizing means creating a sequence starting with each clause
			return normalizeClauseSet(subPatterns);
		}
		// We should NEVER get here
		assert false : "Invalid operatorType";
		return null;
	}
	
	/**
	 * Normalizing a base element means finding all of the
	 * possible base DMAP items that it might match.
	 * <p>
	 * It may be more efficient to do this work when checking
	 * a reference match rather than generating all possibilities
	 * in advance to make the reference match easy.
	 * 
	 * @param base The base item to normalize
	 * @return The normalized patterns for the base element
	 */
	private Vector<Pattern> normalizeBase(DMAPItem base) {
		// To normalize a base element, find all of its targets
		Vector<DMAPItem> targets = base.getTargets();
		// Create a pattern for each one
		Vector<Pattern> results = new Vector<Pattern>();
		for (DMAPItem target: targets) {
			results.addElement(new Pattern(base, target));
		}
		// Return the results
		if (results.isEmpty()) throw new Error("The pattern item " + toPatternString() + " on " + base + " can never be matched.");
		return results;
	}
	
	/**
	 * Normalizing a sequence requires normalizing the first element
	 * and then generating a new sequence pattern that includes each
	 * normalized result.
	 * 
	 * @param sequence The sequence of patterns to be normalized
	 * @return The resulting normalized patterns
	 */
	private Vector<Pattern> normalizeSequence(Vector<Pattern> sequence) {
		// To normalize a sequence, normalize the first element
		Pattern first = sequence.elementAt(0);
		Vector<Pattern> firsts = first.normalize();
		// Fold the results back in
		return sequenceReplaceFirst(sequence, firsts);
	}
	
	@SuppressWarnings("unchecked")
	private Vector<Pattern> sequenceReplaceFirst(Vector<Pattern> sequence, Vector<Pattern> newFirsts) {
		// Fold the results back into the first part of the sequence
		Vector<Pattern> results = new Vector<Pattern>();
		for (Pattern newFirst: newFirsts) {
			if (newFirst == null) {
				// The first item was optional, normalize the rest of the sequence
				Vector<Pattern> rest = normalizeSequenceRest(sequence);
				if (rest == null) {
					// There is no more sequence, include an optional marker
					addInItem(results, null);
				} else {
					// Add the normalizations with no first element
					for (Pattern p: rest) addInItem(results, p);
				}
			} else {
				// The new first item is substituted into a copy of the pattern
				Vector<Pattern> newSubPatterns = (Vector<Pattern>)sequence.clone();
				newSubPatterns.setElementAt(newFirst, 0);
				results.addElement(new Pattern(Operator.SEQUENCE, newSubPatterns, newFirst.key));
			}
		}
		// Return the results
		return results;
	}
	
	private Vector<Pattern> normalizeSequenceRest(Vector<Pattern> sequence) {
		if (sequence.size() <= 1) {
			// There is no rest of the sequence
			return null;
		} else if (sequence.size() == 2) {
			// One piece remains, raise it up and normalize it
			Pattern piece = sequence.elementAt(1);
			return piece.normalize();
		} else {
			// There are more pieces to deal with, drop the first element
			Vector<Pattern> newSubPatterns = new Vector<Pattern>(sequence.size() - 1);
			for (int i=1; i<sequence.size(); i++) {
				newSubPatterns.addElement(sequence.elementAt(i));
			}
			// Normalize the remainder
			return normalizeSequence(newSubPatterns);
		}
	}
	
	/**
	 * Normalizing an alternation means creating a new pattern starting
	 * with each of the possible alternations.
	 * 
	 * @param elements The possible alternations
	 * @return A normalized list of patterns for each alternation
	 */
	private Vector<Pattern> normalizeAlternation(Vector<Pattern> elements) {
		// To normalize an alternation, split out each piece separately
		Vector<Pattern> results = new Vector<Pattern>();
		for (Pattern element: elements) {
			// Normalize the alternation and add to the results
			Vector<Pattern> pieces = element.normalize();
			for (Pattern piece: pieces) addInItem(results, piece);
		}
		return results;
	}
	
	/**
	 * Normalizing a CLAUSESET means creating a new pattern that starts with each
	 * clause and is followed by the others as a CLAUSESET.
	 * 
	 * @param elements The elements in the clause set
	 * @return The normalized patterns
	 */
	private Vector<Pattern> normalizeClauseSet(Vector<Pattern> elements) {
		// To normalize a clause set, create a new clause set
		Vector<Pattern> results = new Vector<Pattern>();
		if (elements.size() == 1) {
			// Only one clause, just normalize it
			Vector<Pattern> pieces =  elements.elementAt(0).normalize();
			for (Pattern piece: pieces) addInItem(results, piece);
			// If the "main clause" has been recognized, then add an optional marker
			if (!elements.elementAt(0).isMainClause) addInItem(results, null);
		} else if (elements.size() > 1) {
			// For each clause, create a new sequence starting with the clause and ending with a clause set of the rest
			boolean sawMainClause = false;
			for (Pattern clause: elements) {
				if (clause.isMainClause) sawMainClause = true;
				Vector<Pattern> sequence = new Vector<Pattern>(2);
				// First the clause
				sequence.add(clause);
				// Second, a new clause set holding the other elements
				Vector<Pattern> rest = new Vector<Pattern>(elements.size() - 1);
				for (Pattern element: elements) if (element != clause) rest.add(element);
				sequence.add(new Pattern(Operator.CLAUSESET, rest));
				// Normalize the sequence and add to the results
				Vector<Pattern> pieces = normalizeSequence(sequence);
				for (Pattern piece: pieces) addInItem(results, piece);
			}
			// Now, if the "main clause" has been recognized, then add an optional marker
			if (!sawMainClause) addInItem(results, null);
		}
		return results;
	}

	/**
	 * Normalizing an OPTIONAL pattern means creating one pattern
	 * definitely holding the element, and one pattern without it.
	 * 
	 * @param elements An implicit sequence of optional elements
	 * @return A normalized set of patterns with no optional first element
	 */
	private Vector<Pattern> normalizeOptional(Vector<Pattern> elements) {
		// To normalize an optional, normalize with it
		Vector<Pattern> results = normalizeSequence(elements);
		// And add a marker that says this pattern is optional
		addInItem(results, null);
		return results;
	}

	/**
	 * Normalizing a STAR pattern means creating a pattern holding
	 * one instance of the pattern and no more, a pattern holding
	 * one instance of the pattern and then a PLUS (to prevent more
	 * optional markers), and an optional marker saying that no copy of 
	 * the pattern is also okay.
	 * 
	 * @param elements An implicit sequence that is STARed
	 * @return A normalized set of patterns with no STAR at the beginning
	 */
	private Vector<Pattern> normalizeStar(Vector<Pattern> elements) {
		// To normalize a star, normalize with one copy only
		Vector<Pattern> results = normalizeSequence(elements);
		// Add in one copy followed by a PLUS
		Vector<Pattern> withStar = new Vector<Pattern>(results.size());
		for (Pattern p: results) {
			if (p != null) withStar.addElement(p.append(new Pattern(Operator.PLUS, subPatterns, key)));
		}
		for (Pattern p: withStar) addInItem(results, p);
		// Add a marker that says this pattern is optional
		addInItem(results, null);
		return results;
	}

	/**
	 * Normalizing a PLUS pattern means creating a pattern holding
	 * one instance of the pattern and no more, a pattern holding
	 * one instance of the pattern and then another PLUS.
	 * 
	 * @param elementsAn implicit sequence that is PLUSed
	 * @return A normalized set of patterns with no PLUS at the beginning
	 */
	private Vector<Pattern> normalizePlus(Vector<Pattern> elements) {
		// To normalize a PLUS, normalize with one copy
		Vector<Pattern> results = normalizeSequence(elements);
		// Add in a pattern with one copy followed by the PLUS
		Vector<Pattern> withPlus = new Vector<Pattern>(results.size());
		for (Pattern p: results) {
			if (p != null) withPlus.addElement(p.append(this));
		}
		for (Pattern p: withPlus) addInItem(results, p);
		// No optional marker here
		return results;
	}

	@SuppressWarnings("unchecked")
	private Pattern append(Pattern pattern) {
		// Append a new element on to a pattern
		if ((operator == Operator.SEQUENCE) || (operator == Operator.SINGLE)) {
			// If its already a sequence, just append
			Vector<Pattern> newSubPatterns = (Vector<Pattern>)subPatterns.clone();
			addInItem(newSubPatterns, pattern);
			return new Pattern(Operator.SEQUENCE, newSubPatterns, key);
		} else {
			// If its something else, make it into a sequence
			Vector<Pattern> newSubPatterns = new Vector<Pattern>(2);
			addInItem(newSubPatterns, this);
			addInItem(newSubPatterns, pattern);
			return new Pattern(Operator.SEQUENCE, newSubPatterns, key);
		}
	}
	
	/**
	 * Add a pattern to a list of patterns making sure that only one
	 * copy of the optional marker (ie. null) is added to the list.
	 * 
	 * @param elements The list being added to
	 * @param element The new item
	 */
	private void addInItem(Vector<Pattern> elements, Pattern element) {
		if (element != null) {
			elements.addElement(element);
		} else if (!elements.contains(null)) {
			elements.addElement(null);
		}
	}

	/**
	 * Advancing a pattern moves it forward to the next base element that
	 * the parser should be looking for.  Advancing a pattern generates only
	 * normalized results.  Thus, advancing a pattern may also generate an
	 * arbitrary number of new patterns.
	 * 
	 * @return The new, advanced, normalized versions of this pattern
	 */
	public Vector<Pattern> advance() {
		// This method must not side effect any of the constituents of this
		// pattern.  They may be in use elsewhere.  Everything must be copied
		// if it needs to be changed.
		
		assert(isNormalized()) : "Only normalized patterns can be advanced.";
		
		// Process each type of pattern as required to create a set of
		// advanced, normalized patterns.
		switch (operator) {
		case BASE:
			// A base pattern cannot be advanced
			return null;
		case SINGLE:
			// A single pattern cannot be advanced
			return null;
		case SEQUENCE:
			// A sequence needs to have its first component removed
			return advanceSequence();
		}
		// A normalized pattern should be one of the above.
		assert false : "Cannot advance pattern of type " + operator + ".";
		return null;		
	}
	
	/**
	 * Test whether this pattern can be advanced.  If not, return 0.
	 * If so, return the number of subpatterns in the pattern.
	 * @return
	 */
	public int testAdvance() {
		switch (operator) {
		case BASE:
			// A base pattern cannot be advanced
			return 0;
		case SINGLE:
			// A single pattern cannot be advanced
			return 0;
		case SEQUENCE:
			return subPatterns.size();
		default:
			if ( subPatterns == null ) {
				return 0;
			} else {
				return subPatterns.size();
			}
		}
	}
	
	/**
	 * To advance a sequence, advance the first component.
	 * 
	 * @return A normalized set of next patterns
	 */
	@SuppressWarnings("unchecked")
	private Vector<Pattern> advanceSequence() {
		// To advance a sequence, advance the first element
		Pattern first = subPatterns.elementAt(0);
		Vector<Pattern> firsts = first.advance();
		if (firsts == null) {
			// The first element is exhausted, drop it and move on
			return normalizeSequenceRest(subPatterns);
		} else {
			// There is more to do, fold back into the sequence
			return sequenceReplaceFirst(subPatterns, firsts);
		}
	}

	/**
	 * Return the pattern as a string that could be read back in.
	 * 
	 * @return A readable pattern string
	 */
	public String toPatternString() {
		if (operator == Operator.BASE) {
			if (baseItem == null) {
				return "<null>";
			} else {
				return baseItem.toPatternString();
			}
		} else if ((operator == Operator.SINGLE) || (operator == Operator.SEQUENCE)) {
			return subPatternsToString(" ", false);
		} else if (operator == Operator.ALTERNATION) {
			return subPatternsToString(" | ", false);
		} else if (operator == Operator.CLAUSESET) {
			return subPatternsToString(" @ ", false);
		} else if (operator == Operator.STAR) {
			return subPatternsToString(" ", true) + "*";
		} else if (operator == Operator.PLUS) {
			return subPatternsToString(" ", true) + "+";
		} else if (operator == Operator.OPTIONAL) {
			return subPatternsToString(" ", true) + "?";
		} else {
			return "%Pattern " + operator + "%";
		}
	}
		
	private String subPatternsToString(String sep, boolean group) {
		if (subPatterns.size() == 1) {
			String piece = subPatterns.elementAt(0).toPatternString();
			if (group) {
				if (piece.length() > 0) {
					char lastch = piece.charAt(piece.length()-1);
					if ((lastch == '+') || (lastch == '*') || (lastch == '?')) {
						return "(" + piece + ")";
					}
				}
			}
			return piece;
		} else {
		  StringBuffer sb = new StringBuffer();
		  sb.append('(');
		  int main = -1;
		  for (int i=0; i<subPatterns.size(); i++) {
		  	if (subPatterns.elementAt(i).isMainClause) {
		  		main = i;
		  		break;
		  	}
		  }
		  if (main < 0) {
			  for (int i=0; i<subPatterns.size(); i++) {
			  	if (i != 0) sb.append(sep);
			  	sb.append(subPatterns.elementAt(i).toPatternString());
			  }
		  } else {
		  	sb.append(subPatterns.elementAt(main).toPatternString());
			  for (int i=0; i<subPatterns.size(); i++) {
			  	if (i != main) {
			  		sb.append(sep);
			  		sb.append(subPatterns.elementAt(i).toPatternString());
			  	}
			  }
		  }
		  sb.append(')');
		  return sb.toString();
		}
	}
	
	/**
	 * Return that pattern as a normalized 'prediction'.
	 * This will start with the first item the pattern is
	 * expecting, a dot, and then the pattern.
	 * 
	 * @return A 'prediction' view of a normalized pattern
	 */
	public String toNormalizedString() {
		if (baseItem == null) {
			return "<null> . " + toPatternString();
		} else {
			return baseItem.toPatternString() + " . " + toPatternString();
		}
	}
	
	/**
	 * Return the pattern as a string
	 */
	public String toString() {
		return toPatternString();
	}
	
}
