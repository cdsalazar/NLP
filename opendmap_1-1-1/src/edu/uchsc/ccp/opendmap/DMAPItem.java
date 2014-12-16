/**
 * The OpenDMAP for Protege Project
 * March 2005
 */
package edu.uchsc.ccp.opendmap;

import java.util.Collection;
import java.util.Vector;

/**
 * A <code>DMAPItem</code> is the basic unit of recognition in OpenDMAP.
 * <p>
 * This class is somewhat overloaded; it is used to represent both this
 * items that are waiting to be recognized (ie. pattern components) and
 * those items that have been recognized (ie. input tokens and frames).
 * While this overloading can occasionally  be confusing within the code, 
 * it does make the code quite a bit simpler to write.
 * <p>
 * The public methods are supplied to allow DMAP users to use gather
 * various sorts of information about this type of object once they
 * get a handle on it from inside a <code>Reference</code>.
 * 
 * @author Will Fitzgerald
 * @author R. James Firby
 */

/* Changes (most recent first)
 * 
 * 03-09-06 rjf - Added addPathBindingsFor
 * 03-08-06 rjf - Added prediction fo pathSatisfiedIn
 * 09-28-05 rjf - Added pathSatisfiedIn
 */
public abstract class DMAPItem {
	
	/**
	 * Check whether this object represents a class.
	 * 
	 * @return True if this object represents a class
	 */
	public abstract boolean isClass();

	/**
	 * Check whether this object represents an instance.
	 * 
	 * @return True if this object represents an instance
	 */
	public abstract boolean isInstance();

	/**
	 * Check whether this object is a subclass or instance of the
	 * given ancestor class.
	 * 
	 * @param ancestor The class this object might belong to
	 * @return True if this object is an instance or subclass of ancestor
	 */
	public abstract boolean isa(DMAPItem ancestor);
	
	/**
	 * Check whether this object is a subclass or instance of the
	 * ancestor class with the given name.
	 * 
	 * @param ancestorClass The name of the ancestor class to check.
	 * @return True if this object is a child of the ancestor class.
	 */
	public boolean isa(String ancestorClass) { return false; }

	/**
	 * Find all abstractions of this object.  That is, all classes that
	 * this object belongs to.
	 * 
	 * @return All superclasses of this object, including itself
	 */
	public abstract Collection<DMAPItem> allAbstractions();

	/**
	 * Find all instances of this object.  That is, all instances that
	 * have this object as an ancestor.
	 * 
	 * @return All instances of this class
	 */
	public abstract Collection<DMAPItem> allInstances();

	/**
	 * Return all instances of this class that have slots filled with
	 * values compatible with those in the suppled information.
	 * 
	 * @param information The slot fillers to match.
	 * @return All instances of this class that match the information.
	 */
	abstract Collection<DMAPItem> findInstances(Collection<InfoPacket> information);
	
	public DMAPItem getSlotValue(String slotName) { return null; }
	
	public abstract String getText();
	
	/* ---------------------------------------------------------------- */
	/* Methods needed to use this item as a reference base              */

	/**
	 * Get a string describing this object as a reference with its
	 * slots filled according to the supplied information.
	 * 
	 * @return A descriptive string that includes slot fillers.
	 */
	abstract String getReferenceString(Collection<InfoPacket> information, Collection<String> properties);
	
	/* ---------------------------------------------------------------- */
	/* Methods needed to use this item as pattern components            */

	/**
	 * Get all of the predictable items that might match this
	 * pattern item.  That is, all of the items that DMAP should
	 * watch for when this pattern item is predicted.
	 * 
	 * @return The items to predict for this pattern item.
	 */
	Vector<DMAPItem> getTargets(){
		Vector<DMAPItem> v = new Vector<DMAPItem>(1);
		v.add(this);
		return v;
	}
	
	/**
	 * Check whether this item's path constraints are satisfied
	 * in the supplied reference.
	 * 
	 * @param reference The reference to check
	 * @param prediction The prediction using the reference
	 * @return True if this item is consistent with the reference
	 */
	boolean pathSatisfiedIn(Reference reference, Prediction prediction) {
		return true;
	}
	
	/**
	 * Add any bindings this item wants to make to the prediction using
	 * this reference.  Bindings are used by path property functions to create
	 * constraints across separate pattern elements.
	 * 
	 * @param reference The reference recognized by the prediction
	 * @param prediction The prediction recognizing the reference
	 */
	void addPathBindingsFor(Reference reference, Prediction prediction) {
	}
	
	/**
	 * Get a string that could be read back in to produce this
	 * pattern item.
	 * 
	 * @return A readable pattern string.
	 */
	abstract String toPatternString();
	
	/* ---------------------------------------------------------------- */
	/* Methods needed to use this item as prediction key                */

	/**
	 * Get a prediction map for this type of item.  Different predictable
	 * items use different prediction maps because they must be matched
	 * (and hence stored) in different ways.
	 * 
	 * @return A new prediction map for this type of item.
	 */
	abstract PredictionMap getNewPredictionMap();
	
	/**
	 * Get an association key to use when looking up the prediction table
	 * to use for this item.  Some items share the same prediction table so
	 * they must be able to return the same key.
	 * 
	 * @return The prediction table key to use for this item.
	 */
	abstract PredictionTable.Key getTableKey();

	/* ---------------------------------------------------------------- */
	/* Methods used for debugging                                       */

	/**
	 * Return a descriptive string for this object that includes its
	 * type in human-reabable form.
	 * 
	 * @return A descriptive string to be used in trace output.
	 */
	abstract String toDescriptiveString();

}
