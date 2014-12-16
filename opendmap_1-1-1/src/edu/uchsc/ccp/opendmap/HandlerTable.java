/**
 * The OpenDMAP for Protege Project
 * November 2005
 */
package edu.uchsc.ccp.opendmap;

import java.util.Hashtable;

import edu.stanford.smi.protege.model.Frame;

/**
 * This class implements a table of reference handlers for the
 * OpenDMAP parser.  A reference handler is an arbitrary piece of
 * Java code to be run whenever a specific type of reference is
 * recognized by the OpenDMAP parser.
 * 
 * @author R. James Firby
 */
public class HandlerTable {

	/* The table to hold the DMAP reference handlers */
	private Hashtable<Frame, IReferenceHandler> table = new Hashtable<Frame, IReferenceHandler>();
	
	/**
	 * Add a new frame reference handler to the handler table.
	 * 
	 * @param frame The frame this handler is waiting on
	 * @param handler The handler to process references to this frame
	 */
	public void add(Frame frame, IReferenceHandler handler) {
		table.put(frame, handler);
	}
	
	/**
	 * Get the handler for a DMAP Item, if there is one.
	 * 
	 * @param item The item beinging referenced
	 * @return The handler associated with items of this type
	 */
	public IReferenceHandler get(DMAPItem item) {
		if (item instanceof ProtegeFrameItem) {
			Frame frame = ((ProtegeFrameItem) item).getFrame();
			if (frame != null) {
				// Found an appropriate handler
				return table.get(frame);
			}
		}
		// Didn't find anything
		return null;
	}
	
}
