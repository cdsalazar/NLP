/**
 * The OpenDMAP for Protege Project
 * July 2005
 */
package edu.uchsc.ccp.opendmap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;
import java.util.Iterator;

import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.ValueType;

/**
 * This class represents a Protege Slot.  DMAP is never looking
 * for this slot directly, it looks up the frame 
 * constraints on this slot and then looks for compatible fillers.
 * 
 * @author Will Fitzgerald
 * @author R. James Firby
 */

/* Changes (most recent first)
 * 
 * 02-01-06 rjf - Changed slot path handling to be property annotations.
 * 02-01-06 rjf - Fixed getTargets() so it does the right thing with Protege Slots.
 * 12-13-05 rjf - Made more general using protegeGroup
 * 09-28-05 rjf - Added support for slot paths
 * 09-27-05 rjf - Fixed all methods to work with SimpleInstances
 *                 as the defining frame.
 * 09-14-05 rjf - If a slot should be filled with a fixed type,
 *                 (ie. string, integer, float) then
 *                 generate a regex to match the allowed filler.
 */
public class ProtegeSlotItem extends DMAPItem {

	/* The Protege project group this slot is part of */
	private ProtegeProjectGroup protegeGroup = null;
	/* The Protege Slot */
	private Slot mySlot;
	/* The Protege Frame holding this slot */
	private Frame myFrame;
	/* The Protege Cls defining the possible fillers of this slot */
	private Frame myFiller;
	/* The additional path constraints on fillers of this slot */
	private ArrayList<String> myPath;
	/* Am I a 'denotation' holder? */
	private boolean amDenotation = false;
	/* The Protege Knowledge base holding this frame and slot */
	private KnowledgeBase kb;

	/**
	 * Create a new slot item for the given slot and frame.
	 * 
	 * @param slot The Protege Slot represented by this item
	 * @param frame The Protege Frame holding the slot
	 * @param group The Protege Project Group being used
	 * @param isDenotation True if this slot holds the 'denotation' for this pattern
	 */
	public ProtegeSlotItem(Slot slot, Frame frame, ProtegeProjectGroup group, boolean isDenotation) {
		super();
		protegeGroup = group;
		mySlot = slot;
		myFrame = frame;
		myFiller = null;
		myPath = null;
		amDenotation = isDenotation;
		if (slot == null) {
			kb = null;
		} else {
			kb = slot.getKnowledgeBase();
		}
	}

	/**
	 * Create a new slot item for the given slot, frame,
	 * constraint, and path
	 * 
	 * @param slot The Protege Slot represented by this item
	 * @param frame The Protege Frame holding the slot
	 * @param group The Protege Project Group being used
	 * @param isDenotation True if this slot holds the 'denotation' for this pattern
	 * @param path Any additional constraints on the filler of this slot
	 */
	ProtegeSlotItem(Slot slot, Frame frame, ProtegeProjectGroup group, boolean isDenotation, ArrayList<String> path) {
		super();
		protegeGroup = group;
		mySlot = slot;
		myFrame = frame;
		//myFiller = constraint;
		//myPath = path;
		myFiller = null; // No longer used?
		myPath = path;
		amDenotation = isDenotation;
		if (slot == null) {
			kb = null;
		} else {
			kb = slot.getKnowledgeBase();
		}
	}

	/**
	 * Get the Protege Frame that holds this slot.
	 * 
	 * @return The frame holding this slot.
	 */
	public Frame getFrame() {
		return myFrame;
	}
	
	/**
	 * Return whether or not this slot has been labelled as holding
	 * a concept 'denotation'.
	 * 
	 * @return True if this slot holds a 'denotation'
	 */
	public boolean isDenotation() {
		return amDenotation;
	}

	/**
	 * Get the Protege Slot represented by this slot item.
	 * 
	 * @return The slot represented by this item.
	 */
	public Slot getSlot() {
		return this.mySlot;
	}

	/*
	 * @see DMAPItem#toDescriptiveString
	 */
	public String toDescriptiveString() {
		return "slot " + this;
	}
	
	/**
	 * Return a string suitable for debugging output.
	 */
	public String toString() {
		return "ps<"+mySlot.toString()+">";
	}

	/**
	 * A <code>ProtegeSlotItem</code> is equal to another if they represent
	 * the same slot.
	 */
	public boolean equals(Object thing) {
		if (this == thing) {
			return true;
		} else if (thing == null) {
			return false;
		} else if (thing instanceof ProtegeSlotItem) {
			// Thing is a Protege Slot
			ProtegeSlotItem item = (ProtegeSlotItem) thing;
			if (mySlot == null) {
				return (item.mySlot == null);
			} else {
				return mySlot.equals(item.mySlot);
			}
		} else {
			// Not a ProtegeSlotItem
			return false;
		}
	}

	/* -------------------------------------------------------------- */
	/* Required public methods                                        */
	
	/**
	 * Is the Protege Frame holding this slot a class?
	 */
	public boolean isClass() {
		return (myFrame instanceof Cls);
	}

	/**
	 * Is the Protege Frame holding this slot an instance?
	 */
	public boolean isInstance() {
		return (myFrame instanceof SimpleInstance);
	}
	
	/**
	 * Is a possible slot filler for this item a subclass (or instance of)
	 * the given ancestor class.
	 */
	public boolean isa(DMAPItem ancestor) {
		/* TODO: Confirm this is doing the right thing */
		// Check for boundary cases
		if (myFrame == null) return false;
		if (ancestor == null) return false;
		// If we're both slots then compare possible fillers
		if (ancestor instanceof ProtegeSlotItem) {
			Frame ancestorClass = ((ProtegeSlotItem)ancestor).myFrame;
			if (ancestorClass == null) {
				// No ancestor given
				return false;
			} else {
				// Ancestor is a class
				return protegeGroup.isa(myFrame, ancestorClass);
			}
		} else {
			// ancestor not a ProtegeSlotItem
			return false;
		}
	}
	
	/*
	 * Check whether this item is a slot with the given name
	 * (non-Javadoc)
	 * @see edu.uchsc.ccp.dmap.DMAPItem#isa(java.lang.String)
	 */
	public boolean isa(String ancestor)	{
		/* TODO: This is completely different from the above */
		if (ancestor == null) return false;
		return ancestor.equals(mySlot.getName());
	}

	/**
	 * Get all abstractions of things that could fill this slot.
	 */
	public Collection<DMAPItem> allAbstractions() {
		assert (myFrame != null) : "null class for slot";
		assert (mySlot != null):  "null slot in ProtegeSlotItem";
		Vector<DMAPItem> returns = new Vector<DMAPItem>();
		// Do different things for different types of slots
		ValueType kind = mySlot.getValueType();
		if ((kind == ValueType.CLS) || (kind == ValueType.INSTANCE)) {
			// Get all of the classes that could fill this slot
			Cls parent = null;
			if (myFrame instanceof Cls) {
				parent = (Cls) myFrame;
			} else if (myFrame instanceof SimpleInstance) {
				parent = ((SimpleInstance) myFrame).getDirectType();
			}
			if (parent != null) {
				Iterator i = kb.getTemplateSlotAllowedClses((Cls) myFrame, mySlot).iterator();
				while (i.hasNext()) {
					// For each one, get all of its abstractions
					Collection<Frame> frames = protegeGroup.allAbstractions((Frame)i.next());
					for (Frame f: frames) {
						returns.addElement(new ProtegeFrameItem(f, protegeGroup));
					}
				}
			}
		}
		//System.out.println("ProtegeSlotItem/All abstractions for "+slot+" on "+cls+" are:"+ returns);
		return returns;
	}

	/**
	 * Get all of the instances in the Protege Knowledge base that
	 * could fill this slot.
	 */
	public Collection<DMAPItem> allInstances() {
		return dmapItems(allInstancesOfFrame());
	}

	@SuppressWarnings("unchecked")
	private Vector<Frame> allInstancesOfFrame() {
		/* TODO: Make sure this is doing the right thing. */
		/* Instances of the frame holding the slot aren't the same as instances of slot fillers */
		if ((myFrame == null) || !(myFrame instanceof Cls))
			return null;
		Cls c = (Cls) myFrame;
		Vector<Frame> returns = new Vector<Frame>();
		Iterator<Instance> i = c.getDirectInstances().iterator();
		while (i.hasNext()) {
			returns.add(i.next());
		}
		return returns;
	}

	private Vector<DMAPItem> dmapItems(Vector<Frame> frames) {
		Vector<DMAPItem> returns = new Vector<DMAPItem>();
		for (Frame frame : frames) {
			returns.add(new ProtegeFrameItem(frame, protegeGroup));
		}
		return returns;
	}

	public Vector<DMAPItem> findInstances (Collection<InfoPacket> information) {
		Vector<DMAPItem> instances = new Vector<DMAPItem>();
		instances.add(this);
		return instances;
	}
	
	public String getText() {
		if (mySlot == null) {
			return "";
		} else {
			return mySlot.getBrowserText();
		}
	}
	
	public String getReferenceString(Collection<InfoPacket> information, Collection<String> properties) {
		if (mySlot == null) {
			return "[NULL]";
		} else {
			return "[" + mySlot.getName() + "]";
		}
	}
		
	/* -------------------------------------------------------------- */
	/* Methods required to use this item in a pattern                 */
	
	/**
	 * The predicted targets for a Protege Slot are all of the possible 
	 * slot filler classes.
	 */
	public Vector<DMAPItem> getTargets() {
		// Gather up things to find fillers for this slot
		Vector<DMAPItem> v = new Vector<DMAPItem>();
		// These things depend on the type of value that can fill the slot
		ValueType kind = mySlot.getValueType();
		if ((kind == ValueType.CLS) || (kind == ValueType.INSTANCE)) {
			// Get all of the classes that could fill this slot
			if (myFiller != null) {
				// We have an overriding filler constraint
				v.add(new ProtegeFrameItem(myFiller, protegeGroup));
			} else  {
				// Use our Protege constraints
				Collection<Frame> frames = slotClasses(kb, myFrame, mySlot);
				if (frames != null) {
					for (Frame frame: frames) {
						v.add(new ProtegeFrameItem(frame, protegeGroup));
					}
				}
			}
		} else if (kind == ValueType.STRING) {
			// Look for any string to fill this slot
			v.addElement(new RegexItem(".+", "STRING"));
		} else if (kind == ValueType.INTEGER) {
			// Look for a valid integer string to fill this slot
			v.addElement(new RegexItem("[+-]?\\d+", "INTEGER"));
		} else if (kind == ValueType.FLOAT) {
			// Look for a valid float string to fill this slot
			v.addElement(new RegexItem("[+-]?(?:\\d*\\.\\d+)|(?:\\d+(:?\\.\\d*)?)", "FLOAT"));
		}
		// Done
		return v;
	}
	
	/**
	 * Get the Protege Frame types that could fill this slot.
	 * 
	 * @param kb The Protege Knowledge Base holding this frame and slot
	 * @param frame The frame holding the slot
	 * @param slot The slot
	 * @return The Protege Frames that might fill this slot
	 */
	@SuppressWarnings("unchecked")
	private Collection<Frame> slotClasses(KnowledgeBase kb, Frame frame, Slot slot) {
		Collection<Frame> frames = null;
		ValueType kind = slot.getValueType();
		if (kind == ValueType.CLS) {
			frames = slot.getAllowedParents();
		} else if (frame instanceof Cls) {
			frames = kb.getTemplateSlotAllowedClses((Cls) frame, slot);
		} else if (frame instanceof SimpleInstance) {
			Cls parent = ((SimpleInstance) frame).getDirectType();
			if (parent != null) {
				frames = kb.getTemplateSlotAllowedClses((Cls) parent, mySlot);
			}
		}
		return frames;
	}

	/*
	 * Return a pattern string representing this item.
	 * 
	 * @see DMAPItem#toPatternString
	 */
	public String toPatternString() {
		if (mySlot == null) {
			return "[NULL]";
		} else if (myPath != null) {
			StringBuffer sb = new StringBuffer();
			sb.append("["); sb.append(mySlot.getName());
			if (myFiller != null) {sb.append(" "); sb.append(myFiller.getName());}
			for (String thing: myPath) { 
				sb.append(" "); 
				sb.append(thing);
			}
			sb.append("]");
			return sb.toString();
		} else if ((myFiller != null) && (myFiller != myFrame)) {
			return "[" + mySlot.getName() + " " + myFiller.getName() + "]";
		} else {
			return "[" + mySlot.getName() + "]";
		}
	}
	
	/*
	 * Check that this reference is allowed in this slot according to this slot's path constraints.
	 * The prediction is considering this reference as a filler for this slot. (non-Javadoc)
	 * @see edu.uchsc.ccp.dmap.DMAPItem#pathSatisfiedIn(edu.uchsc.ccp.dmap.Reference, edu.uchsc.ccp.dmap.Prediction)
	 */
	boolean pathSatisfiedIn(Reference reference, Prediction prediction) {
		if ((myPath == null) || myPath.isEmpty()) 
			return true; //!reference.hasProperties();
		// Check whether the path is satisfied in the reference
		boolean okay = true;
		for (String pathStep: myPath) {
			boolean sign = true;
			String property = pathStep;
			// Extract sign information, if any
			if (pathStep.startsWith("+")) {
				sign = true;
				property = pathStep.substring(1);
			} else if (pathStep.startsWith("-")) {
				sign = false;
				property = pathStep.substring(1);
			}
			// See exactly what type of property it is
			if (property.indexOf(":") >= 0) {
				// Some sort of phrase graph function
				String parts[] = property.split(":");
				if (parts.length != 2) {
					// A simple property after all
					okay = checkProperty(reference, property, sign);
				} else {
					// Do the phrase graph thing
					String func = parts[0].trim();
					String arg = parts[1].trim();
					okay = prediction.applyPropertyFunction(func, arg, reference);
					if (!sign) okay = !okay;
				}
			} else {
				// A simple symbol property
				okay = checkProperty(reference, property, sign);
			}
			if (!okay) break;
		}
		return okay;
	}
	
	/*
	 * Add any path bindings that this slot needs to make to the prediction recognizing this
	 * reference to fill this slot. (non-Javadoc)
	 * @see edu.uchsc.ccp.dmap.DMAPItem#addPathBindingsFor(edu.uchsc.ccp.dmap.Reference, edu.uchsc.ccp.dmap.Prediction)
	 */
	void addPathBindingsFor(Reference reference, Prediction prediction) {
		if ((myPath != null) && !myPath.isEmpty()) {
			for (String pathStep: myPath) {
				// See exactly what type of property it is
				if (pathStep.indexOf(":") >= 0) {
					// Some sort of phrase graph function
					String parts[] = pathStep.split(":");
					if (parts.length == 2) {
						// Do the phrase graph thing
						String func = parts[0].trim();
						String arg = parts[1].trim();
						if (func.startsWith("+") || func.startsWith("-")) func = func.substring(1);
						prediction.addBinding(func, arg, reference);
					}
				}
			}
		}
	}
	
	/**
	 * Check that the reference is tagged with the given property with the
	 * appropriate sign.
	 * 
	 * @param reference The reference holding the property
	 * @param property The property to check
	 * @param sign True if the property should be found, false if it should not be
	 * @return True if the reference has the property with the right sign
	 */
	private boolean checkProperty(Reference reference, String property, boolean sign) {
		if (sign) {
			return reference.hasProperty(property);
		} else {
			return !reference.hasProperty(property);
		}
	}
	
	/**
	 * Check whether the given item is a valid filler for this slot.
	 * 
	 * @param item The potential filler.
	 * @return True if the item could fill this slot.
	 */
	public boolean validFiller(DMAPItem item) {
		boolean okay = false;
		// These things depend on the type of value that can fill the slot
		ValueType kind = mySlot.getValueType();
		if ((kind == ValueType.CLS) || (kind == ValueType.INSTANCE)) {
			if (!(item instanceof ProtegeFrameItem)) return false;
			Frame iFrame = ((ProtegeFrameItem) item).getFrame();
			Collection<Frame> frames = slotClasses(kb, myFrame, mySlot);
			if (frames != null) {
				for (Frame frame: frames) {
					if (protegeGroup.isa(iFrame, frame)) {
						okay = true;
						break;
					}
				}
			} 
		} else if (kind == ValueType.STRING) {
			// Look for any string to fill this slot
			okay = (item instanceof TextItem);
		} else if (kind == ValueType.INTEGER) {
			// Look for a valid integer string to fill this slot
			if (!(item instanceof TextItem)) return false;
			try {
				Integer.parseInt(((TextItem) item).getText());
				okay = true;
			} catch (Exception e) {}
		} else if (kind == ValueType.FLOAT) {
			// Look for a valid float string to fill this slot
			if (!(item instanceof TextItem)) return false;
			try {
				Double.parseDouble(((TextItem) item).getText());
				okay = true;
			} catch (Exception e) {}
		}
		return okay;
	}

	/* -------------------------------------------------------------- */
	/* Methods required to use this item in a prediction              */
	
	/**
	 * Get a new prediction map appropriate for this type
	 * of predicted item.
	 */
	PredictionMap getNewPredictionMap() {
		return new FramePredictionMap();
	}
	
	/**
	 * Get a key that identifies which prediction map should
	 * be used for items of this class.
	 */
	public PredictionTable.Key getTableKey() {
		return PredictionTable.Key.FRAME;
	}
	
}
