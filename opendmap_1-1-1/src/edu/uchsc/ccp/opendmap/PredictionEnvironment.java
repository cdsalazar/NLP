/**
 * The OpenDMAP for Protege Project
 * March 2006
 */
package edu.uchsc.ccp.opendmap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;

/**
 * This class defines a binding environment for the pattern property
 * constraints used by a prediction.
 * <p>
 * Pattern property contraints are "functions" on a variable that are
 * attached to slots.  Functions are all managed by objects external
 * to the DMAP system that implement the DMAPPropertyFunctionHandler
 * interface.
 * 
 * @author R. James Firby
 */
public class PredictionEnvironment {

	/* A list of property function handlers */
	private Collection<DMAPPropertyFunctionHandler> handlers = null;
	
	/* The logger, if there is one */
	private DMAPLogger logger = null;
	
	/**
	 * Private constructor used for cloning.
	 */
	private PredictionEnvironment() {}

	/**
	 * Contruct a new prediction binding environment.
	 * 
	 * @param parser The parser that is in use.
	 */
	PredictionEnvironment(Parser parser) {
		Collection<DMAPPropertyFunctionHandler> parserHandlers = parser.getPropertyFunctionHandlers();
		if (parserHandlers != null) {
			handlers = new ArrayList<DMAPPropertyFunctionHandler>(parserHandlers.size());
			for (DMAPPropertyFunctionHandler handler: parserHandlers) {
				handlers.add(handler.clone());
			}
		}
		logger = parser.getLogger();
	}
	
	/**
	 * Clone this prediction environment.
	 * @return A new copy of this environment.
	 */
	public PredictionEnvironment clone() {
		PredictionEnvironment environment = new PredictionEnvironment();
		if (handlers != null) {
			environment.handlers = new ArrayList<DMAPPropertyFunctionHandler>(handlers.size());
			for (DMAPPropertyFunctionHandler handler: handlers) {
				environment.handlers.add(handler.clone());
			}
		}
		environment.logger = logger;
		return environment;
	}

	/**
	 * Apply a property function and see if it is satisfied.
	 * 
	 * @param func The function name
	 * @param arg The argument variable
	 * @param reference The reference that should satisfy this function
	 * @return True if this property constraint is satisfied
	 */
	boolean applyPropertyFunction(String func, String arg, Reference reference) {
		if (handlers == null) return true;
		for (DMAPPropertyFunctionHandler handler: handlers) {
			if (handler.handles(func)) {
				try {
					return handler.evalFunction(func, arg, reference);
				} catch (Exception e) {
					if (logger != null) {
						logger.log(Level.WARNING, "Warning: " + e.getMessage());
					}
				}
			}
		}
		return true;
	}
	
	/**
	 * Record a function as being attached to a slot in this reference.
	 * This is important so that handlers can evaluate all of the functions
	 * 'seen so far' when evaluating a later function.
	 * 
	 * @param func The property constraint function
	 * @param arg The argument variable
	 * @param reference The reference creating the binding
	 */
	void addBinding(String func, String arg, Reference reference) {
		if (handlers != null) {
			for (DMAPPropertyFunctionHandler handler: handlers) {
				if (handler.handles(func)) {
					try {
						handler.recordFunction(func, arg, reference);
					} catch (Exception e) {
						if (logger != null) {
							logger.log(Level.WARNING, "Warning: " + e.getMessage());
						}
					}
					break;
				}
			}
		}
	}
	
}
