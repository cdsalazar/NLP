/**
 * The OpenDMAP for Protege Project
 * October 2005
 */
package edu.uchsc.ccp.opendmap;

/**
 * The DefaultTokenizer is used to create a token sequence
 * when an input string is handed to the Parser.  It handles
 * some punctuation and numbers, but it isn't very smart or
 * complete.
 * 
 * @author R. James Firby
 */

/* Changes (most recent first)
 * 
 * 02-06-06 rjf - Changed tokenizer to assign ordinal start and end positions internally
 * 10-16-05 rjf - Changed tokenizer to keep track of character
 *                 positions.
 */
public class DefaultTokenizer extends DMAPTokenizer {
	
	/* Return all tokens in lowercase */
	private boolean asLowercase = true;
	
	/* The raw input string */
	private String inputText = null;
	
	/* The next token within text */
	private String tokenText = null;
	private int tokenBegin = -1;
	private int tokenEnd = 0;
	
	/* The ordinal toekn number of the next token */
	private int tokenNumber = 0;
	
	/**
	 * Create a new DefaultTokenizer for a particular string.
	 * 
	 * @param inputText The input string to tokenize
	 * @param retainCase True if case should be retained, False if case should be ignored.
	 */
	public DefaultTokenizer(String inputText, boolean retainCase) {
		// Save the raw input
		this.asLowercase = !retainCase;
		this.inputText = inputText;
		this.tokenNumber = 0;
		// Extract the first token
		tokenEnd = 0;
		if (!findToken()) {
			tokenText = null;
		}
	}

	/**
	 * See if there is another token to return.
	 */
	public boolean hasNext() {
		return (tokenText != null);
	}

	/**
	 * Return the next token.
	 */
	public DMAPToken next() {
		if (tokenText != null) {
			// Create the base DMAP Item
			DMAPItem value = null;
			if (asLowercase) {
				value = new TextItem(tokenText.toLowerCase());
			} else {
				value = new TextItem(tokenText);
			}
			// Create a complete DMAP Token
			DMAPToken token = new DMAPToken(value, tokenBegin, tokenEnd);
			token.setStart(tokenNumber);
			token.setEnd(tokenNumber);
			tokenNumber = tokenNumber + 1;
			// Find the next token
			if (!findToken()) tokenText= null;
			// Return the token
			return token;
		} else {
			return null;
		}
	}

	/**
	 * Look for the next token in the input text.  Side effect
	 * the internal representation of what and where that token is.
	 * @return True is a valid token was found.
	 */
	private boolean findToken() {
		char ch = ' ';
		boolean found = false;
		// Look for the beginning of the next token
		int i = tokenEnd;
		// Look for the beginning of a token
		while (!found && (i < inputText.length())) {
			ch = inputText.charAt(i);
			if (isTokenChar(ch) || isSingletonChar(ch)) {
				found = true;
			} else {
				i = i + 1;
			}
		}
		// If no new token found, then stop
		if (!found) return false;
		// Look for the end of the token
		tokenBegin = i;
		if (isSingletonChar(ch)) {
			found = true;
			i = i + 1;
		} else {
			found = false;
			i = tokenBegin + 1;
			while (!found && (i < inputText.length())) {
				ch = inputText.charAt(i);
				if (!isTokenChar(ch)) {
					found = true;
				} else {
					i = i + 1;
				}
			}
		}
		// Pull out the token
		tokenText = inputText.substring(tokenBegin, i);
		tokenEnd = i;
		// If a token holds "." then it might be a number
		// If it isn't then drop trailing "."
		if ((tokenText != null) && !tokenText.equals("")) {
			if (tokenText.contains(".")) {
				if (tokenText.length() > 1) {
					try {
						Double.valueOf(tokenText);
					} catch (Exception e) {
						if (tokenText.endsWith(".")) {
							tokenText = tokenText.substring(0,tokenText.length()-1);
							tokenEnd = tokenEnd - 1;
						}
					}
				} else {
					tokenText = null;
				}
			}
		}
		// Make sure we really have a token
		if ((tokenText == null) || tokenText.equals("")) {
			// Grab the next token
			if (i < inputText.length()) {
				return findToken();
			}
		}
		// Done
		return true;
	}
	
	/**
	 * Is a character a valid part of a larger token?
	 * @param ch The character to check
	 * @return True if the character is valid in a token
	 */
	private boolean isTokenChar(char ch) {
		if (Character.isLetterOrDigit(ch)) {
			return true;
		} else if (ch == '.') {
			return true;
		} else if (ch == '/') {
			return true;
		} else if (ch == '-') {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Is a character should be a singleton punctionation token?
	 * @param ch The character to check
	 * @return True if the character is valid as a token
	 */
	private boolean isSingletonChar(char ch) {
		if (ch == '(') {
			return true;
		} else if (ch == ')') {
			return true;
		} else {
			return false;
		}
	}

}
