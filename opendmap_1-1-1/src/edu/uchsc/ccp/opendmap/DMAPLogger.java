/**
 * The OpenDMAP for Protege Project
 * July 2005
 */
package edu.uchsc.ccp.opendmap;

import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A facility for logging tracing messages in various ways.
 * 
 * The default logging behavior is to write to System.out.  This
 * default behavior does not use the standard Java logging
 * facility so that it interleaves correctly with other information
 * written to System.out.
 * 
 * This class also wraps the standard Java logging class so that
 * Java logging is incorporated.  A Java logger is sought under
 * the name passed to the constructor.  Use the standard Java
 * "logging.properties" file to configure additional logging for
 * that name.
 * 
 * Logging levels:
 *  FINEST: list recognition patterns
 *   FINER:
 *    FINE:
 *  CONFIG:
 *    INFO:
 * WARNING:
 *  SEVERE:
 *     ALL:
 *     OFF:
 * 
 * @author R. James Firby
 */
public class DMAPLogger {
	
	private Level logLevel = Level.OFF;
	
	private Logger javaLogger = null;
	
	private OutputStream outStream = null;
	private int indent = 0;
	private boolean errorReported = false;
	
	public DMAPLogger(String name) {
		// See if there is a Java logger defined under this name
		javaLogger = java.util.logging.LogManager.getLogManager().getLogger(name);
		// Set the default stream
		outStream = System.out;
	}
	
	public DMAPLogger(String name, Level level) {
		// See if there is a Java logger defined under this name
		javaLogger = java.util.logging.LogManager.getLogManager().getLogger(name);
		// Set the default stream
		outStream = System.out;
		// Set the desired level
		logLevel = level;
		//if (javaLogger != null) {
		//	javaLogger.setLevel(level);
		//}
	}
	
	public DMAPLogger(String name, Level level, OutputStream stream) {
		// See if there is a Java logger defined under this name
		javaLogger = java.util.logging.LogManager.getLogManager().getLogger(name);
		// Set the default stream
		outStream = stream;
		// Set the desired level
		logLevel = level;
		//if (javaLogger != null) {
		//	javaLogger.setLevel(level);
		//}
	}
	
	public boolean isLoggable(Level level) {
		if (isLogging(level)) {
			return true;
		} else if (javaLogger != null) {
			return javaLogger.isLoggable(level);
		} else {
			return false;
		}
	}
	
	private boolean isLogging(Level level) {
		return (logLevel.intValue() <= level.intValue());
	}
	
	public int addIndent() {
		return changeIndent(2);
	}
	
	public int removeIndent() {
		return changeIndent(-2);
	}
	
	public int changeIndent(int delta) {
		indent = indent + delta;
		if (indent < 0) indent = 0;
		return indent;
	}
	
	public void log(Level level, String message) {
		if (isLogging(level)) {
			printMessage(message);
		}
		if (javaLogger != null) {
			javaLogger.log(level, message);
		}
	}
			
	private void printMessage(String message) {
		try {
			for (int i=1; i<=indent; i++) outStream.write(" ".getBytes());
			outStream.write(message.getBytes());
			outStream.write("\n".getBytes());
			outStream.flush();
		} catch (java.io.IOException e) {
			if (!errorReported) {
				System.err.print("Warning: Log error - " + e.getMessage());
				errorReported = true;
			}
		}
	}
	
	

}
