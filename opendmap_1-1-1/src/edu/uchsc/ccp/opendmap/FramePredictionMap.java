/**
 * The OpenDMAP for Protege Project
 * July 2005
 */
package edu.uchsc.ccp.opendmap;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;

import edu.stanford.smi.protege.model.Frame;

/**
 * Implements a mapping from Protege frame pattern items 
 * to predictions.
 * 
 * @author Will Fitzgerald
 * @author R. James Firby
 */
public class FramePredictionMap implements PredictionMap {
	
	/* The predictions are stored in a hash table */
	private Hashtable<Frame, Vector<Prediction>> table = new Hashtable<Frame, Vector<Prediction>>();

	/**
	 * Add prediction on a frame item.  Save these in a hash
	 * table for fast access.
	 */
	public boolean add(DMAPItem key, Prediction prediction) {
		// Only valid for frame items
		if (key instanceof ProtegeFrameItem) {
			// Save in the hash table
			boolean newvalue = false;
			Frame frame = ((ProtegeFrameItem)key).getFrame();
			if (frame != null) {
				newvalue = true;
				Vector<Prediction> value = table.get(frame);
				if (value != null) {
					value.add(prediction);
				} else {
					value = new Vector<Prediction>();
					value.add(prediction);
					table.put(frame, value);
				}
			}
			return newvalue;
		} else {
			// Not an appropriate type, this should never happen
			throw new Error("Invalid DMAPItem type used in FramePredictionMap add.");
		}
	}

	/**
	 * Get the predictions associated with this key.
	 */
	public Vector<Prediction> get(DMAPItem key) {
		// Only valid for frame items
		if (key instanceof ProtegeFrameItem) {
			ProtegeFrameItem item = (ProtegeFrameItem) key;
			/* TODO: Make prediction lookup on frames more efficient */
			Vector<Prediction> results = new Vector<Prediction>();
			Collection<DMAPItem> absts = item.allAbstractions();
			if (absts == null)
				return null;
			for (DMAPItem abst : absts) {
				if (abst instanceof ProtegeFrameItem) {
					Frame frame = ((ProtegeFrameItem)abst).getFrame();
					Vector<Prediction> predictions = table.get(frame);
					if (predictions != null) {
						for (Prediction p : predictions) {
							results.add(p);
						}
					}
				}
			}
			return results;
		} else {
			// Not an appropriate type, this should never happen
			throw new Error("Invalid DMAPItem type used in FramePredictionMap get.");			
		}
	}

	/**
	 * Clear all the predictions from this map.
	 */
	public void clear() {
		table.clear();
	}

	/**
	 * Dump this map to a logger.
	 */
	public void log(DMAPLogger logger, Level level) {
		// Write a summary
		logger.log(level, "Number of keys: " + table.size());
		// Now all the predictions on text items
		for (Frame key: table.keySet()) {
			logger.log(level, "Predictions on: \"" + key + "\"");
			logger.addIndent();
			Vector<Prediction> predictions = table.get(key);
			if (predictions != null) {
				for (Prediction p: predictions) {
					logger.log(level, p.toString());
				}
			}
			logger.removeIndent();
		}
	}
	
	/**
	 * Return an simple informative string describing this map
	 */
	public String toString() {
		return "<FramePredictionMap: " + hashCode() + ">";
	}

}
