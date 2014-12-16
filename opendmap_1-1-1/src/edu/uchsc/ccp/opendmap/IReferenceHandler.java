/**
 * The OpenDMAP for Protege Project
 * November 2005
 */
package edu.uchsc.ccp.opendmap;

/**
 * This is the interface that a Java class needs to implement in
 * order to be a valid Reference handler withing OpenDMAP.
 * 
 * @author R. James Firby
 */
public interface IReferenceHandler {

	/**
	 * Handle a reference.  Any processing of the reference within
	 * this method is appropriate, including the calls to the parser.
	 * 
	 * @param parser The parser that generated the reference
	 * @param reference The reference that was recognized
	 */
	public void handleReference(Parser parser, Reference reference);
	
}
