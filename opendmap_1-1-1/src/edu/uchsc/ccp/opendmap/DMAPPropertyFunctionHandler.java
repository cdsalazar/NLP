/**
 * The OpenDMAP for Protege Project
 * May 2006
 */
package edu.uchsc.ccp.opendmap;

/**
 * This interface allows external objects to handle DMAP slot property
 * functions.  DMAP does not implement any standard property functions.
 * <p>
 * As a filler for a reference slot is considered, the functions attached to
 * that slot are evaluated to see if the filler is 'approved' by that function.
 * <p>
 * When a slot is filled, all the functions evaluated to check that slot filler
 * are 'recorded' in the property handler so that it can reevaluate them all as
 * fillers for other slots are considered.  So, if functions represent constraints
 * between slots, these constraints will continue to be evaluated as each slot
 * is filled.
 * 
 * @author R. James Firby
 */
public interface DMAPPropertyFunctionHandler {
	
	/**
	 *  Create a copy of this handler (including all saved functions).  Cloning is
	 *  needed because each time a slot is filled, it spins off alternate ways that
	 *  the rest of the pattern might proceed.  Each of these alternatives needs a
	 *  copy of the recorded functional state sot far.
	 *  <p>
	 *  If a function handler does not record any internal state, it may return the
	 *  same object without copying it.
	 */
	public DMAPPropertyFunctionHandler clone();
	
	/**
	 * Check whether this handler knows what to do with this function.  If many function
	 * handlers are available, DMAP uses only the first one that says it handles the given
	 * function.
	 * 
	 * @param func The name of the function of interest.
	 * @return 'True' if this handler wants to handle this function.
	 */
	public boolean handles(String func);
	
	/**
	 * Evaluate a function to see if it says the reference is an acceptable slot filler.
	 * Internally, the handler may check past recorded functions as well.
	 * 
	 * @param func The function to evaluate.
	 * @param arg The name of the argument to this function.
	 * @param reference The potential slot filler to check.
	 * @return 'True' if this function accepts this reference as a slot filler.
	 * @throws DMAPPropertyFunctionException When there is a problem evaluating the function.
	 */
	public boolean evalFunction(String func, String arg, Reference reference)throws DMAPPropertyFunctionException;
	
	/**
	 * Record the function associated with a slot filler so that it can be reevaluated
	 * later to make sure it still holds.
	 * 
	 * @param func The function that was evaluated and should be recorded.
	 * @param arg The name of the argument to this function.
	 * @param reference The slot filler accepted by this function.
	 */
	public void recordFunction(String func, String arg, Reference reference);

}
