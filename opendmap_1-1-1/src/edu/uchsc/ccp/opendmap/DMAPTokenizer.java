/**
 * The OpenDMAP for Protege Project
 * October 2005
 */
package edu.uchsc.ccp.opendmap;

import java.util.Iterator;

/**
 * This class is the root of all DMAPTokenizer objects.  A
 * tokenizer returns a sequence of DMAP tokens for input to
 * the Parser.
 * 
 * @author R. James Firby
 */
public abstract class DMAPTokenizer implements Iterator<DMAPToken>, Iterable<DMAPToken>{
	
	/**
	 * Required by Iterator.
	 */
	public abstract boolean hasNext();
	
	/**
	 * Required by Iterator.
	 */
	public abstract DMAPToken next();
	
	/**
	 * Required by Iterable.  This default implementation
	 * need never be redefined.  It is included to support
	 * "for (token: tokens) {}" like constructs.
	 */
	public Iterator<DMAPToken> iterator() {
		return this;
	}
	
	/**
	 * Optional in Iterator.  This default implementation
	 * need never be redefined because the Parser never
	 * calls it.
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
