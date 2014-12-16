/**
 * The DMAP for Protege Project
 * July 2005
 */
package edu.uchsc.ccp.opendmap;

import java.util.Vector;
import java.util.logging.Level;

/**
 * This class holds a set of DMAP predictions.  Each prediction is indexed by the first 
 * item that it is waiting to see.  DMAP typically uses two of these tables, one for
 * the fixed set of anytime predictions and one for the dynamic predictions generated
 * each time a new item is recognized.
 * <p>
 * A prediction table is really a collection of prediction maps.  Each map is indexed
 * by the type of idex item it is using.  So, all TextItems go in one table, all FrameItems
 * go in another, and so on.  The separation is used to speed indexing and allow different
 * types of items to be indexed efficiently in different ways.
 * <p>
 * This class hides the existence of these subtables from the rest of the system.
 * 
 * @author R. James Firby
 */

/* Changes (most recent first)
 * 
 * 09-15-05 rjf - Enabled recursive predictions to support :head-like patterns
 */
public class PredictionTable  {
	
	enum Key {
		TEXT,   // A subtable index for TextItems
		FRAME   // A subtable index for FrameItems
	}
	
	/* A debugging name for this table */
	private String label = null;
	/* Subtables; an array is used for speed */
	private PredictionMap[] table = new PredictionMap[Key.values().length];
	
	/**
	 * Creates a new Prediction Table.
	 * 
	 * @param name A user friendly name for this prediction table.  Used for logging and debugging.
	 */
	public PredictionTable(String name) {
		label = name;
	}
	
	/**
	 * Gets the prediction map associated with this type if index item.
	 * 
	 * @param index The index item
	 * @return The map for items of that type
	 */
	private PredictionMap getMap(DMAPItem index) {
		PredictionMap predMap = table[index.getTableKey().ordinal()];
		if (predMap == null) {
			predMap = index.getNewPredictionMap();
			table[index.getTableKey().ordinal()] = predMap;
		}
		return predMap;
	}
	
	/**
	 * Gets the predictions currently waiting to see the supplied item.
	 * 
	 * @param target The supplied item
	 * @return The predictions waiting to see that item next
	 */
	public Vector<Prediction> getPredictions(DMAPItem target) {
		PredictionMap predMap = getMap(target);
		if (predMap == null) {
			return null;
		} else {
			return predMap.get(target);
		}
	}

	/**
	 * Adds a new prediction to the prediction table.  It is indexed under the
	 * first item that it expects to see.  The pattern in the prediction must
	 * be normalized.
	 * 
	 * @param p The prediction to add
	 * @return Whether this prediction is okay
	 */
	public boolean addPrediction(Prediction p) {
		// Enforce only normalized patterns
		assert (p.getKey() != null) : "Prediction pattern must be normalized.";
		// Add the prediction to the right table
		DMAPItem target = p.getKey();
		//if (!target.equals(p.getBase())) {
			// Find the appropriate map and add it
			PredictionMap predMap = getMap(target);
			return predMap.add(target, p);
		//} else {
			// This isn't a legal prediction
		//	return false;
		//}
	}
	
	/**
	 * Clears all predictions from this table.
	 *
	 */
	public void clearPredictions() {
		for (int i=0; i<table.length; i++) {
			table[i] = null;
		}
	}

	/**
	 * Dump the contents of this table to the supplied logger.
	 * 
	 * @param logger The destination for the information
	 * @param level The level at which it should be written
	 */
	public void log(DMAPLogger logger, Level level) {
		logger.log(level, label);
		logger.addIndent();
		int count = 0;
		for (int i=0; i<table.length; i++) {
			if (table[i] != null) count = count + 1;
		}
		logger.log(level, "Number of tables: " + count);
		for (int i=0; i<table.length; i++) {
			PredictionMap predMap = table[i];
			if (predMap != null) {
				logger.log(level, "Table with key type: "+ Key.values()[i]);
				logger.addIndent();
				predMap.log(logger, level);
				logger.removeIndent();
			}
		}
		logger.removeIndent();
	}
	
}
