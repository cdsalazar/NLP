/**
 * The OpenDMAP for Protege Project
 * May 2006
 */
package edu.uchsc.ccp.opendmap;

/**
 * An exception to be thrown when an external object has a problem evaluating
 * a property function.
 * 
 * @author R. James Firby
 */
public class DMAPPropertyFunctionException extends Exception {

	private static final long	serialVersionUID	= -3940187020856140287L;
	
	public DMAPPropertyFunctionException(String message) {
		super(message);
	}
}
