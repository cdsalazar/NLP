/**
 * The OpenDMAP for Protege Project
 * December 2005
 */
package edu.uchsc.ccp.opendmap.configuration;

/** 
 * @author R. James Firby
 */
public class ConfigurationException extends Exception {

	private static final long	serialVersionUID	= -5535020729367331733L;

	public ConfigurationException(String message) {
		super(message);
	}
	
	public ConfigurationException(String message, Throwable e) {
		super(message, e);
	}

}
