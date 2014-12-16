/**
 * The OpenDMAP for Protege Project
 * October 2005
 */
package edu.uchsc.ccp.opendmap;

import java.util.Vector;

import edu.stanford.smi.protege.model.Frame;

/**
 * A DMAPToken is an input concept for the Parser.  Simple
 * tokens are text strings corresponding to words.  However,
 * in more complex situations a token may correspong to a
 * Protege frame with some slots filled in.
 * <p>
 * For example, when DMAP is used in a UIMA environment it
 * might be paired with a entity tagger.  The tagged entity
 * annotations could be used as DMAP input tokens with a
 * ProtegeFrameItem corresponding to the type of entity
 * recognized and a [name] slot bound to the specific name
 * of the entity.
 * 
 * @author R. James Firby
 */

/* Changes (most recent first)
 * 
 * 02-01-06 rjf  - Added token positions as well as character positions.
 * 
 */
public class DMAPToken {

	/* The DMAPItem corresponding to the basic token */
	private DMAPItem item = null;
	/* Slot filler information for the basic token */
	private Vector<InfoPacket> information = null;
	
	/* The ordinal token start and end positions */
	private int start = -1;
	private int end = -1;
	
	/* These two values are specifically needed for UIMA */
	/* The start character position for this token in the input */
	private int cStart = -1;
	/* The end character position for this token in the input */
	private int cEnd = -1;
	
	/* This value is needed to help carry around info for the UIMA type system */
	private Object source = null;
	
	/**
	 * Creates a simple token.
	 * @param item The DMAPItem defining the token.
	 */
	public DMAPToken(DMAPItem item) {
		this.item = item;
	}
	
	/**
	 * Create a simple token with start and end character positions.
	 * @param item The DMAPItem defining this token.
	 * @param cStart The character position of the start of this token.
	 * @param cEnd The character position of the end of this token.
	 */
	public DMAPToken(DMAPItem item, int cStart, int cEnd) {
		this.item = item;
		this.cStart = cStart;
		this.cEnd = cEnd;
	}
	
	/**
	 * Create a token for the given item with additional 
	 * slot filler information.
	 * @param item The DMAPItem defining this token.
	 * @param information The slot filler information for this token.
	 */
	public DMAPToken(DMAPItem item, Vector<InfoPacket> information) {
		this.item = item;
		this.information = information;
	}
	
	/**
	 * Create a token for the given item with additional 
	 * slot filler information and character start and end
	 * positions.
	 * @param item The DMAPItem defining this token.
	 * @param information The slot filler information for this token.
	 * @param cStart The character position of the start of this token.
	 * @param cEnd The character position of the end of this token.
	 */
	public DMAPToken(DMAPItem item, Vector<InfoPacket> information, int cStart, int cEnd) {
		this.item = item;
		this.information = information;
		this.cStart = cStart;
		this.cEnd = cEnd;
	}

	/**
	 * Get the DMAPItem defining this token.
	 * @return The DMAPItem defining this token.
	 */
	public DMAPItem getItem() {
		return item;
	}
	
	/**
	 * Get the slot filler information for this token.
	 * @return The slot filler information.
	 */
	public Vector<InfoPacket> getInformation() {
		return information;
	}
	
	/**
	 * Get the character position of the start of this token.
	 * @return The start character position.
	 */
	public int getCharacterStart() {
		return cStart;
	}
	
	/**
	 * Get the character position of the end of this token.
	 * @return The end character position.
	 */
	public int getCharacterEnd() {
		return cEnd;
	}
	
	/**
	 * Get the token position of the start of this token.
	 * @return The ordinal position of the start this token.
	 */
	public int getStart() {
		return start;
	}
	
	/**
	 * Set the ordinal start position of this token in the token stream.
	 * @param start The ordinal position of the start of this token.
	 */
	public void setStart(int start) {
		this.start = start;
	}
	
	/**
	 * Get the token position of the end of this token.
	 * @return The ordinal position of the end this token.
	 */
	public int getEnd() {
		return end;
	}
	
	/**
	 * Set the ordinal end position of this token in the token stream.
	 * @param end The ordinal position of the end of this token.
	 */
	public void setEnd(int end) {
		this.end = end;
	}
	
	/**
	 * Get the 'source' of this token.  This value is not interpreted by the
	 * OpenDMAP system but it is available to post-processing systems via
	 * the tokens that were used to recognize a Reference.
	 * 
	 * @return The source object set for this token
	 */
	public Object getSource() {
		return source;
	}
	
	/**
	 * Get the 'source' of this token.  This value is not interpreted by the
	 * OpenDMAP system but it is available to post-processing systems via
	 * the tokens that were used to recognize a Reference.
	 * 
	 * @param source The 'source' object to save with this token
	 */
	public void setSource(Object source) {
		this.source = source;
	}

	/** 
	 * Create a new DMAPToken from a text string.
	 * 
	 * @param parser The parser that will be using this token
	 * @param text The text string for the token.
	 * @param cStart The character position of the start of the token
	 * @param cEnd The character position of the end of the token
	 * @return The token
	 */
	public static DMAPToken newToken(Parser parser, String text, int cStart, int cEnd) {
		return new DMAPToken(new TextItem(text), cStart, cEnd);
	}
	
	/**
	 * Create a new DMAPToken for a Protege Frame and it's slot fillers.
	 * <p>
	 * The 'uniquifier' object can be any Java object that is not equal to the
	 * 'uniqufuer' object of any other token.  It can also be null in most circumstances
	 * without causing a problem.
	 * 
	 * @param parser The parser that will be using this token
	 * @param name The name of the Protege Frame that this token represents
	 * @param slots The slot/filler pairs corresponding to this frame and token.
	 * @param cStart The character position of the start of the token
	 * @param cEnd The character position of the end of the token
	 * @param uniquifier An object that makes this token unique from any other tokens that might cover the same characters.
	 * @return The token
	 */
	public static DMAPToken newToken(Parser parser, String name, Vector<Object> slots, int cStart, int cEnd, Object uniquifier) {
		ProtegeProjectGroup group = parser.getProtegeProjectGroup();
		if (group != null) {
			Frame frame = group.getFrameByName(name);
			if (frame != null) {
				Vector<InfoPacket> information = null;
				if ((slots != null) && (slots.size() > 0)) {
					information = new Vector<InfoPacket>();
					for (Object slot: slots) {
						if (slot instanceof InfoPacket) information.add((InfoPacket) slot);
					}
					if (information.size() <= 0) information = null;
				}
				return new DMAPToken(new ProtegeFrameItem(frame, group, uniquifier), information, cStart, cEnd);
			}
		}
		return null;
	}

}
