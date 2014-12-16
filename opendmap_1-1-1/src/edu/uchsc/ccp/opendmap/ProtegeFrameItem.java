/**
 * The OpenDMAP for Protege Project
 * July 2005
 */
package edu.uchsc.ccp.opendmap;

import java.util.Collection;
import java.util.Vector;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.model.Slot;

/**
 * This class represents a Protege Frame in DMAP.
 * 
 * @author Will Fitzgerald
 * @author R. James Firby
 */

/* Changes (most recent first)
 * 
 * 12-13-05 rjf - Moved isa(frame, frame) into protege group for generality
 * 09-28-05 rjf - Made isa(frame, frame) visible for use in ProtegeSlotItem
 * 09-26-05 rjf - Fixed isa(frame, frame) to do the right thing.
 */
public class ProtegeFrameItem extends DMAPItem {

	/* The Protege Project Group this frame is part of */
	private ProtegeProjectGroup protegeGroup;
	/* The Protege Frame being represented */
	private Frame myFrame;
	/* A human friendly name for this frame */
	private String friendlyName = null;
	
	/* Uniqueness token */
	private Object uniquenessToken = null;

	/**
	 * Create a new DMAP Item encapsulating the supplied
	 * Protege Frame.
	 * 
	 * @param frame The Protege Frame to encapsulate.
	 */
	ProtegeFrameItem(Frame frame, ProtegeProjectGroup group, Object uniquifier) {
		super();
		protegeGroup = group;
		myFrame = frame;
		friendlyName = null;
		uniquenessToken = uniquifier;
	}
	
	ProtegeFrameItem(Frame frame, ProtegeProjectGroup group) {
		this(frame, group, null);
	}
	
	/**
	 * Get the Protege Frame this item encapsulates.
	 * 
	 * @return The P=rotege Frame this item encapsulates.
	 */
	public Frame getFrame() {
		return myFrame;
	}

	/*
	 * @see DMAPItem#toDescriptiveString
	 */
	public String toDescriptiveString() {
		return "frame " + this;
	}
	
	/**
	 * Return a string suitable for debugging output.
	 */
	public String toString() {
		if (myFrame != null)
		return  "pf<" + myFrame.toString() + ">";
		return "pf<NULL>";
	}

	/**
	 * A <code>ProtegeFrameItem</code> is equal to another if their internal frames
	 * are equal.
	 */
	public boolean equals (Object o) {
		if (this == o) {
			return true;
		} else if (!(o instanceof ProtegeFrameItem)) {
			return false;
		} else if (myFrame == null) {
			return (((ProtegeFrameItem)o).myFrame == null);
		} else {
			ProtegeFrameItem other = (ProtegeFrameItem) o;
			if (!myFrame.equals(other.myFrame)) {
				return false;
			} else if ((uniquenessToken == null) && (other.uniquenessToken == null)) {
				return true;
			} else if ((uniquenessToken == null) || (other.uniquenessToken == null)) {
				return false;
			} else {
				return (uniquenessToken.equals(other.uniquenessToken));
			}
		}
	}
		
	/* -------------------------------------------------------------- */
	/* Required public methods                                        */
	
	/**
	 * A <code>ProtegeFrameItem</code> is a class if the encapsulated
	 * frame is a class.
	 */
	public boolean isClass() {
		return (myFrame instanceof Cls);
	}

	/**
	 * A <code>ProtegeFrameItem</code> is an instance if the encapsulated
	 * frame is a simple instance.
	 */
	public boolean isInstance() {
		return (myFrame instanceof SimpleInstance);
	}

	/**
	 * A <code>ProtegeFrameItem</code> is a member of an ancestor class if its
	 * encapsulated frame is a subclass of the frame encapsulated by
	 * the ancestor class.
	 */
	public boolean isa(DMAPItem ancestor) {
		if (ancestor == null) return false;
		if (ancestor instanceof ProtegeFrameItem) {
			Frame ancestorFrame = ((ProtegeFrameItem)ancestor).getFrame();
			return protegeGroup.isa(myFrame, ancestorFrame);
		} else {
			// ancestor not a ProtegeFrameItem
			return false;
		}
	}
	
	/**
	 * See if this item encapsulates a Protege frame that has
	 * an ancestor with the given class name.
	 */
	public boolean isa(String ancestorClass) {
		if (myFrame == null) return false;
		boolean okay = false;
		Frame ancestorFrame = protegeGroup.getFrameByName(ancestorClass);
		if (ancestorFrame != null) {
			okay = protegeGroup.isa(myFrame, ancestorFrame);
		}
		return okay;
	}
	
	/**
	 * The abstractions of a <code>ProtegeFrameItem</code> are
	 * all of the superclasses of the encapsulated frame.
	 */
	public Collection<DMAPItem> allAbstractions() {
		Collection<Frame> frames = protegeGroup.allAbstractions(myFrame);
		if (frames == null) return null;
		return dmapItems(frames);
	}

	/**
	 * Convert a list of frames into a list of <code>ProtegeFrameItems</code>
	 * 
	 * @param frames The frames to convert.
	 * @return The DMAP Items encapsulating the frames.
	 */
	private Vector<DMAPItem> dmapItems(Collection<Frame> frames) {
		Vector<DMAPItem> returns = new Vector<DMAPItem>();
		for (Frame frame : frames) {
			returns.add(new ProtegeFrameItem(frame, protegeGroup));
		}
		return returns;
	}

	/**
	 * The instances of a <code>ProtegeFrameItem</code> are all
	 * the simple instances of the encapsulated frame.
	 */
	public Collection<DMAPItem> allInstances() {
		Collection<Frame> frames = protegeGroup.allInstances(myFrame);
		if (frames != null) {
			return dmapItems(frames);
		} else {
			return new Vector<DMAPItem>();
		}
	}

	/**
	 * Find the instances of the Protege Frame encapsulated by this
	 * item that have slot fillers consistent with those in the 
	 * supplied information.
	 */
	Vector<DMAPItem> findInstances (Collection<InfoPacket> information) {
		// First find all the instances of this frame
		Collection<DMAPItem> instances = allInstances();
		// Check each instance against the slot constraints
		Vector<DMAPItem> results = new Vector<DMAPItem>();
		for (DMAPItem instance: instances) {
			if (instance instanceof ProtegeFrameItem) {
				Frame realInstance = ((ProtegeFrameItem)instance).getFrame();
				if (realInstance instanceof SimpleInstance) {
					if (isInstanceConsistent((SimpleInstance) realInstance, information)) {
						results.add(instance);
					}
				}
			}
		}
		return results;
	}
	
	/**
	 * Check whether a Protege instance is consistent with the slot filler
	 * descriptions included in the supplied information.
	 * 
	 * @param instance The Protege instance to check.
	 * @param information The slot filler descriptions.
	 * @return True if the instance is consistent with the information.
	 */
	private boolean isInstanceConsistent(SimpleInstance instance, Collection<InfoPacket> information) {
		if (information == null) return true;
		boolean okay = true;
		for (InfoPacket ip: information) {
			DMAPItem key = ip.getKey();
			Reference reference = ip.getValue();
			if (key instanceof ProtegeSlotItem) {
				Slot slot = ((ProtegeSlotItem)key).getSlot();
				if (instance.hasOwnSlot(slot)) {
					Object slotValue = instance.getOwnSlotValue(slot);
					if (slotValue instanceof SimpleInstance) {
						DMAPItem refItem = reference.getItem();
						if (refItem instanceof ProtegeFrameItem) {
							if (protegeGroup.isa((SimpleInstance) slotValue, ((ProtegeFrameItem)refItem).getFrame())) {
								okay = isInstanceConsistent((SimpleInstance) slotValue, reference.getInformation());
							} else {
								okay = false;
							}
							if (!okay) break;
						}
					}
				}
			}
		}
		return okay;
	}
	
	/**
	 * Get the value of the slot of the encapsulated Protege frame
	 * with the given name.
	 * 
	 * @param slotName The name of the slot.
	 * @return The value of the slot.
	 */
	public DMAPItem getSlotValue(String slotName) {
		Collection slots = myFrame.getOwnSlots();
		for (Object slot: slots) {
			if (slot instanceof Slot) {
				if (slotName.equals(((Slot)slot).getName())) {
					Object value = myFrame.getOwnSlotValue((Slot) slot);
					if (value instanceof Frame) {
						return new ProtegeFrameItem((Frame) value, protegeGroup);
					} else {
						return new TextItem(value.toString());
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Get a text string naming this item.
	 */
	public String getText() {
		if (myFrame == null) {
			return "";
		} else {
			return myFrame.getBrowserText();
		}
	}

	/**
	 * Get a reference string descrbing this item for use in test suite output strings.
	 */
	String getReferenceString(Collection<InfoPacket> information, Collection<String> properties) {
		StringBuffer sb = new StringBuffer();
		sb.append('{');
		sb.append(getFriendlyName());
		if ((information != null) && !information.isEmpty()) {
			sb.append(' ');
			sb.append(InfoPacket.getInformationString(information));
		}
		if ((properties != null) && !properties.isEmpty()) {
			sb.append(" | ");
			boolean isFirst = true;
			for (String property: properties) {
				if (!isFirst) sb.append(' ');
				isFirst = false;
				sb.append(property);
			}
		}
		sb.append('}');
		return sb.toString();
	}
	
//	public DMAPItem getProtegeConcept(String name) {
//		if (protegeGroup == null) return null;
//		Frame frame = protegeGroup.getFrameByName(name);
//		if (frame == null) return null;
//		return new ProtegeFrameItem(frame, protegeGroup);
//	}

	/**
	 * Construct a human-readable name for this frame using
	 * the Protege display text and the actual frame name.
	 * 
	 * @param frame The frame to be named.
	 */
	private String getFriendlyName() {
		if (friendlyName == null) {
			if (myFrame == null) {
				friendlyName = "null";
			} else {
				String realName = myFrame.getName();
				String displayName = myFrame.getBrowserText();
				if ((realName == null) && (displayName == null)) {
					friendlyName = "null";
				} else if (realName == null) {
					friendlyName = displayName;
				} else if (displayName == null) {
					friendlyName = realName;
				} else if (realName.equals(displayName)) {
					friendlyName = realName;
				} else {
					friendlyName = realName + "." + displayName;
				}
			}
		}
		return friendlyName;
	}

	/* -------------------------------------------------------------- */
	/* Methods required to use this item in a pattern                 */
	
	/**
	 * The only target for a <code>ProtegeFrameItem</code> is itself.
	 */
	public Vector<DMAPItem> getTargets() {
		Vector<DMAPItem> v = new Vector<DMAPItem>(1);
		v.add(this);
		return v;
	}

	/*
	 * Return a pattern string representing this item.
	 * 
	 * @see DMAPItem#toPatternString
	 */
	public String toPatternString() {
		if (myFrame == null) {
			return "{NULL}";
		} else {
			return "{" + myFrame.getName() + "}";
		}
	}

	/* -------------------------------------------------------------- */
	/* Methods required to use this item in a prediction              */
	
	/**
	 * Get a new prediction map appropriate for this type
	 * of predicted item.
	 */
	public PredictionMap getNewPredictionMap() {
		{
			return new FramePredictionMap();
		}
	}

	/**
	 * Get a key that identifies which prediction map should
	 * be used for items of this class.
	 */
	public PredictionTable.Key getTableKey () {
		return PredictionTable.Key.FRAME;
	}

}
