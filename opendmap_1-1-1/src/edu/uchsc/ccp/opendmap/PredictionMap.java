/**
 * The OpenDMAP for Protege Project
 * July 2005 
 */
package edu.uchsc.ccp.opendmap;

import java.util.Vector;
import java.util.logging.Level;

/**
 * The interface expected by the <code>PredictionTable</code> class.
 * 
 * @author R. James Firby
 * @author Will Fitzgerald
 */

/* Changes (most recent first)
 * 
 * 09-14-05 rjf - Fixed some Javadoc warnings.
 */
interface PredictionMap  {

	/**
	 * Get the predictions associated with items matching
	 * the supplied key.
	 * 
	 * @param key The key to match against pending predictions
	 * @return The predictions waiting to see this key
	 */
	Vector<Prediction> get(DMAPItem key);

	/**
	 * Add a prediction with the supplied key.
	 * 
	 * @param key The key this prediction is waiting for
	 * @param prediction The prediction that is pending
	 * @return True if the prediction was added
	 */
	boolean add(DMAPItem key, Prediction prediction);

	/**
	 * Clear all of the predictions from this map.
	 */
	void clear();
	
	/**
	 * Write all of the predictions from this map to the
	 * supplied logger.
	 * 
	 * @param logger The destination for the prediction map
	 * @param level The level at which they should be written
	 */
	void log(DMAPLogger logger, Level level);

}
