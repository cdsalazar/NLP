/**
 * The OpenDMAP for Protege Project
 * July 2005
 */
package edu.uchsc.ccp.opendmap;

import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;

/**
 * Implements a mapping from string-based pattern items 
 * to predictions.
 * <p>
 * The internals of this class are a bit complex because
 * looking up the predictions on a text string requires
 * looking at exact matches to the string and also at any
 * regex that might match the string.  The exact matches
 * can easily be put into a hashtable but the regex matches
 * must be searched explicitly.
 * 
 * @author R. James Firby
 */

/* Changes (most recent first)
 * 
 * 09-13-05 rjf - Added check for null regex patterns.
 */

public class StringPredictionMap implements PredictionMap {
	
	/* The hashtable holding simple text keys and their predictions */
	private Hashtable<String, Vector<Prediction>> textTable = new Hashtable<String, Vector<Prediction>>();
	
	/* A table to hold regex pattern matches and their associated predictions */
	private Vector<java.util.regex.Pattern> regexKeys = new Vector<java.util.regex.Pattern>();
	private Vector<Prediction> regexTable = new Vector<Prediction>();

	/**
	 * Add a prediction on the supplied key.  Both text items
	 * and regex items can be used as keys.
	 */
	public boolean add(DMAPItem key, Prediction prediction) {
		// See what kind of item we have
		if  (key instanceof TextItem) {
			// Save in the text table so we can look it up fast
			String text = ((TextItem)key).getText();
			Vector<Prediction> value = textTable.get(text);
			if (value != null) {
				value.add(prediction);
			} else {
				value = new Vector<Prediction>();
				value.add(prediction);
				textTable.put(text, value);
			}
			return true;
		} else if (key instanceof RegexItem) {
			java.util.regex.Pattern pat = ((RegexItem)key).getRegexPattern();
			regexKeys.add(pat);
			regexTable.add(prediction);
			return true;
		} else {
			// Not an appropriate type, this should never happen
			throw new Error("Invalid DMAPItem type used in StringPredictionMap add.");			
		}
	}

	/**
	 * Get all predictions that matche the supplied text string key.
	 * Note that regex items are not valid arguments to this method.
	 */
	public Vector<Prediction> get(DMAPItem key) {
		if (key instanceof TextItem) {
			TextItem item = (TextItem) key;
			String text = item.getText();
			// First look in the table of matching texts
			Vector<Prediction> found = textTable.get(text);
			// Next look in the regex table
			Vector<Prediction> result = null;
			for (int i=0; i<regexTable.size(); i++) {
				java.util.regex.Pattern pat = regexKeys.get(i);
				if ((pat != null) && (pat.matcher(text).matches())) {
					// The text matches the regex pattern
					if (result == null) result = initializeGetResult(found);
					result.add(regexTable.get(i));
				}
			}
			// Done, return what we have
			if (result == null) {
				return found;
			} else {
				return result;
			}
		} else {
			// Not an appropriate type, this should never happen
			throw new Error("Invalid DMAPItem type used in StringPredictionMap get.");
		}
	}
	
	@SuppressWarnings("unchecked")
	private Vector<Prediction> initializeGetResult(Vector<Prediction> found) {
		if (found == null) {
			return new Vector<Prediction>();
		} else {
			return (Vector<Prediction>)found.clone();
		}
	}
	
	/**
	 * Clear all the predictions from this table.
	 */
	public void clear() {
		textTable.clear();
		regexKeys.clear();
		regexTable.clear();
	}
	
	/**
	 * Dump this map to a logger.
	 */
	public void log(DMAPLogger logger, Level level) {
		// Write a summary
		logger.log(level, "Number of keys: " + (textTable.size()+regexKeys.size()));
		// Now all the predictions on text items
		for (String key: textTable.keySet()) {
			logger.log(level, "Predictions on: \"" + key + "\"");
			logger.addIndent();
			Vector<Prediction> predictions = textTable.get(key);
			if (predictions != null) {
				for (Prediction p: predictions) {
					logger.log(level, p.toString());
				}
			}
			logger.removeIndent();
		}
		// And now all the predictions on regex items
		for (int i=0; i<regexKeys.size(); i++) {
			java.util.regex.Pattern key = regexKeys.get(i);
			logger.log(level, "Predictions on: r\"" + key.pattern() + "\"");
			logger.addIndent();
			Prediction p = regexTable.get(i);
			logger.log(level, p.toString());
			logger.removeIndent();
		}
	}
	
	/**
	 * Return an simple informative string describing this map
	 */
	public String toString() {
		return "<StringPredictionMap: " + hashCode() + ">";
	}
}
