package edu.uchsc.ccp.opendmap.tokenizer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import edu.uchsc.ccp.opendmap.DMAPItem;
import edu.uchsc.ccp.opendmap.DMAPToken;
import edu.uchsc.ccp.opendmap.DMAPTokenizer;
import edu.uchsc.ccp.opendmap.Parser;
import edu.uchsc.ccp.opendmap.Reference;
import edu.uchsc.ccp.opendmap.TextItem;

/**
 * This extension of the DMAPTokenizer mimics the function of the DefaultTokenizer (tokenizes on whitespace and
 * punctuation), but adds the ability to insert arbitrary entity annotations prior to OpenDMAP processing. Entity
 * annotations are added using the EntityAnnotation class when the EntityTokenizer is initialized. One thing to note --
 * when adding entity annotations, make sure that the entity type exists in the ontology being used by OpenDMAP. If it
 * does not, then the entity will not be recognized. <br>
 * <br>
 * Consider this code marginally fragile as it has not really been tested. It should work for inserting protein
 * annotations however.
 * 
 * @author Bill Baumgartner.
 * 
 */
public class EntityTokenizer extends DMAPTokenizer {
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

	/* The CAS iterator of the TokenAnnotation structures */
	private ArrayList<DMAPToken> tokens = new ArrayList<DMAPToken>();

	private Iterator<DMAPToken> tokenIterator = null;

	public EntityTokenizer(Parser parser, String inputText, boolean retainCase, List<EntityAnnotation> entityAnnotations) {

		/* Generate default tokens */
		// Save the raw input
		this.asLowercase = !retainCase;
		this.inputText = inputText;
		this.tokenNumber = 0;
		// Extract the first token
		tokenEnd = 0;
		if (!findToken()) {
			tokenText = null;
		}
		while (tokenText != null) {
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
			if (!findToken())
				tokenText = null;
			// Return the token
			sortInNewToken(token);
		}

		/* Generate entity annotations */
		ArrayList<DMAPToken> semanticTokens = new ArrayList<DMAPToken>();
		for (EntityAnnotation entityAnnot : entityAnnotations) {
			DMAPToken token = buildDMAPToken(parser, entityAnnot);
			if (token != null) {
				token.setSource(entityAnnot);
				if (token != null) {
					int start = 0;
					for (int i = 0; i < tokens.size(); i++) {
						if (token.getCharacterStart() < tokens.get(i).getCharacterEnd()) {
							start = i;
							break;
						}
					}
					int end = tokens.size() - 1;
					for (int i = 0; i < tokens.size(); i++) {
						if (token.getCharacterEnd() <= tokens.get(i).getCharacterStart()) {
							end = i - 1;
							break;
						}
					}
					if ((start <= end) && (start >= 0)) {
						token.setStart(start);
						token.setEnd(end);
						semanticTokens.add(token);
						// System.err.println("Created new entity token: [" + token.getStart() + ".." + token.getEnd() +
						// "]");
					}
				}
			} else {
				System.err.println("Token is null, check to make sure entity type '" + entityAnnot.getEntityType()
						+ "' is in the Protege ontology.");
			}
		}
		// Keep the list of tokens sorted by their end point
		for (DMAPToken token : semanticTokens) {
			sortInNewToken(token);
		}
		tokenIterator = tokens.iterator();
	}

	/**
	 * Build a DMAPToken representing a Protege Frame reference found in the JCas.
	 * 
	 * @param parser
	 *            The parser that will be using the token
	 * @param jcasToken
	 *            The JCas annotation that corresponds to this token
	 * @param annotationMap
	 *            The configuration map from JCas annotations to DMAP frames
	 * @return
	 */
	private DMAPToken buildDMAPToken(Parser parser, EntityAnnotation entityAnnot) {
		String type = entityAnnot.getEntityType();
		Vector<Object> slots = getMentionSlotValues(parser, entityAnnot);
		DMAPToken token = DMAPToken.newToken(parser, type, slots, entityAnnot.getSpanStart(), entityAnnot.getSpanEnd(), entityAnnot);
		return token;
	}

	/**
	 * Build a DMAP Reference for a JCas mention annotation. The type of the Reference will corrspond to a Protege Frame
	 * and any mention slots will be turned into Reference slot/filler pairs.
	 * 
	 * @param parser
	 *            The parser that will be using this reference
	 * @param mention
	 *            The mention in the JCas this reference represents
	 * @param annotationMap
	 *            The configuration map from JCas annotations to DMAP frames
	 * @return The reference
	 */
	private Reference buildMentionReference(Parser parser, EntityAnnotation entityAnnot) {
		String type = entityAnnot.getEntityType();
		Vector<Object> dmapSlots = getMentionSlotValues(parser, entityAnnot);
		return parser.newReference(type, dmapSlots);
	}

	/**
	 * Generate a list of DMAP slot/filler pairs for the slots in a JCas mention.
	 * 
	 * @param parser
	 *            The parser that will be using the reference holding these slots
	 * @param mention
	 *            The mention in the JCas that has the slots
	 * @param annotationMap
	 *            The configuration map from JCas annotations to DMAP frames
	 * @return The list of slot/filler pairs
	 */
	private Vector<Object> getMentionSlotValues(Parser parser, EntityAnnotation entityAnnot) {
		Vector<Object> dmapSlots = new Vector<Object>();

		String frameName = entityAnnot.getEntityType();
		Map<String, Set<Object>> slotNameToValueMap = entityAnnot.getSlotName2ValuesMap();
		for (String slotName : slotNameToValueMap.keySet()) {

			Set<Object> slotValues = slotNameToValueMap.get(slotName);
			for (Object slotValue : slotValues) {
				if (slotValue instanceof EntityAnnotation) {
					// Create a reference out of a slot filler class mention
					Reference reference = buildMentionReference(parser, (EntityAnnotation) slotValue);
					// If we have a slot value, save it away
					if (reference != null) {
						dmapSlots.add(parser.newReferenceSlot(frameName, slotName, reference));
					}
				} else if (slotValue instanceof String) {
					dmapSlots.add(parser.newReferenceSlot(frameName, slotName, (String) slotValue));
				} else {
					System.err.println("Unexpected class found as a slot value: " + slotValue.getClass().getName());
				}
			}
		}
		return dmapSlots;
	}

	/**
	 * Look for the next token in the input text. Side effect the internal representation of what and where that token
	 * is.
	 * 
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
		if (!found)
			return false;
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
							tokenText = tokenText.substring(0, tokenText.length() - 1);
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
	 * 
	 * @param ch
	 *            The character to check
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
	 * 
	 * @param ch
	 *            The character to check
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

	/**
	 * Check if there are any more tokens to process
	 */
	@Override
	public boolean hasNext() {
		// Are there any DefaultToken structures left
		if (tokenIterator == null) {
			return false;
		} else {
			return tokenIterator.hasNext();
		}
	}

	/**
	 * Grab the next token and return it. The tokens are in a list sorted by ending character (which roughly corresponds
	 * to input token order).
	 */
	@Override
	public DMAPToken next() {
		// Grab the next DefaultToken and turn it into a DMAP Token
		if ((tokenIterator != null) && (tokenIterator.hasNext())) {
			return tokenIterator.next();
		} else {
			return null;
		}
	}

	/**
	 * Sort a new token into the list of tokens this tokenizer will return.
	 * 
	 * @param token
	 *            The token to sort in
	 */
	private void sortInNewToken(DMAPToken token) {
		// System.err.println("Adding token..  token count before: " + tokens.size());
		boolean found = false;
		for (int i = 0; i < tokens.size(); i++) {
			if (token.getCharacterEnd() < tokens.get(i).getCharacterEnd()) {
				tokens.add(i, token);
				found = true;
				break;
			}
		}
		if (!found) {
			tokens.add(token);
		}

		// System.err.println("Adding token..  token count after: " + tokens.size());
	}

}
