/**
 * The OpenDMAP for Protege Project
 * December 2005
 */
package edu.uchsc.ccp.opendmap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.SimpleInstance;

/**
 * This class represents a collection of Protege Knowledge
 * Bases that are being used together in the OpenDMAP parser.
 * <p>
 * This class maintains a list of "isa" associations between
 * these projects so that the parser can make "isa" inferences
 * across project boundaries.
 * 
 * @author R. James FIrby
 */
public class ProtegeProjectGroup {
	
	/* All of the protege knowledge bases used by this parser */
	private Vector<KnowledgeBase> knowledgeBases = new Vector<KnowledgeBase>();
	
	/* The ISA associations between frames in the above Knowledge Bases */
	private Vector<IsaRelation> isaRelations = new Vector<IsaRelation>();
	
	/**
	 * Add a new knowledge base to this protege project group.
	 * 
	 * @param kb The knowledge base to be added
	 */
	public void addKnowledgeBase(KnowledgeBase kb) {
		if (!knowledgeBases.contains(kb))
			knowledgeBases.add(kb);
	}
	
	/**
	 * Add a new relationship that says that frame a is a child
	 * of frame b. These frames can be in different knowledge
	 * bases.
	 * 
	 * @param a The child frame
	 * @param b The parent frame
	 */
	public void addIsaRelationship(Frame a, Frame b) {
		if ((a != null) && (b != null)) {
			KnowledgeBase akb = a.getKnowledgeBase();
			KnowledgeBase bkb = b.getKnowledgeBase();
			if (akb != bkb) {
				isaRelations.add(new IsaRelation(a, b));
			}
		}
	}
	
	/**
	 * Get a protege frame with the given name from one of the projects 
	 * in this group.  If there are frames with this name in multiple
	 * knowledge bases, just take the first one.
	 * 
	 * @param name The name of the frame to be found
	 * @return The frame
	 */
	public Frame getFrameByName(String name) {
		Frame result = null;
		for (KnowledgeBase kb: knowledgeBases) {
			result = kb.getFrame(name);
			if (result != null) break;
		}
		return result;
	}

	/**
	 * Check whether one Protege Frame is the ancestor of
	 * another.  Follow associations across knowledge base boundaries.
	 * 
	 * @param a The frame to check
	 * @param b The possible ancestor
	 * @return True if b is an ancestor of a
	 */
	public boolean isa(Frame a, Frame b) {
		if (a == null) return false;
		if (b == null) return false;
		if (inSameKb(a, b)) {
			// In the same KB, just use a normal ISA
			return isaInSingleKb(a, b);
		} else {
			// In different KBs, look for cross-over
			boolean result = false;
			for (IsaRelation isar: isaRelations) {
				if (isar.isParentOf(a)) {
					result = isa(isar.parent, b);
					if (result) break;
				}
			}
			return result;
		}
	}
	
	/**
	 * Check whether two frames are in the same KB or not.
	 * 
	 * @param a One frame
	 * @param b The other frame
	 * @return True if these two frames are in the same Protege KB
	 */
	private boolean inSameKb(Frame a, Frame b) {
		return a.getKnowledgeBase() == b.getKnowledgeBase();
	}
		
	/**
	 * Check a normal ISA relationshiop between two frames in the
	 * same Protege KB.
	 * 
	 * @param a The child frame
	 * @param b The parent frame
	 * @return True if the the parent is an ancestor of the child
	 */
	private boolean isaInSingleKb(Frame a, Frame b) {
		if (a.equals(b)) return true;
		if (b instanceof Cls) {
			if (a instanceof SimpleInstance) {
				// Does an instance belong to a class
				Frame p = ((Instance)a).getDirectType();
				if ((p == null) || (p == a)) {
					return false;
				} else if (p instanceof Cls) {
					return (p == b) || ((Cls) p).hasSuperclass((Cls) b);
				} else {
					return false;
				}
			} else if (a instanceof Cls) {
				// A subclass
				return (a == b) || ((Cls) a).hasSuperclass((Cls) b);
			} else {
				// Don't know what to do
				return false;
			}
		} else {
			// Cannot be true
			return false;
		}
	}
	
	/**
	 * Get all of the abstractions of a Protege Frame.  The
	 * abstractions are all of its superclasses, including those
	 * across Protege knowledge base boundaries.
	 * 
	 * @param frame The frame
	 * @return All of its superclasses
	 */
	public Collection<Frame> allAbstractions(Frame frame) {
		ArrayList<Frame> result = new ArrayList<Frame>();
		// First, get all the frames in own kb
		allAbstractionsInSingleKb(frame, result);
		// Now, look for any relations we are a child of
		for (IsaRelation isar: isaRelations) {
			if (isar.isParentOf(frame)) {
				allAbstractionsInSingleKb(isar.parent, result);
			}
		}
		return result;
	}
	
	/**
	 * Get all of the abstractions of a Protege Frame within
	 * a single KB.  The abstractions are all of its superclasses.
	 * 
	 * @param frame The frame
	 * @return All of its superclasses
	 */
	private void allAbstractionsInSingleKb(Frame frame, ArrayList<Frame> result) {
		if (frame instanceof Cls) {
			// If frame is a class then find its superclasses
			addAllAbstractionsInSingleKb((Cls) frame, result);
		} else if (frame instanceof SimpleInstance) {
			// If frame is an instance then find the superclasses of its class
			allAbstractionsInSingleKb((SimpleInstance) frame, result);
		} 
	}

	/**
	 * Extract the superclasses of a Protege instance.
	 * 
	 * @param instance The base instance to start from.
	 * @return All of the base instance superclasses.
	 */
	private void allAbstractionsInSingleKb(SimpleInstance instance, ArrayList<Frame> returns) {
		// Include the instance itself
		returns.add(instance);
		// Include all of its parent's and their superclasses
		Iterator parents = instance.getDirectTypes().iterator();
		while (parents.hasNext()) {
			Object obj = parents.next();
			if (obj instanceof Cls) {
				addAllAbstractionsInSingleKb((Cls) obj, returns);
			}
		}
	}

	/**
	 * Find all of the abstractions of a Protege Class without
	 * any duplicates in a single KB.
	 * 
	 * @param cls The Protege Class to get abstractions of.
	 * @param results A place to store the abstractions.
	 */
	private void addAllAbstractionsInSingleKb(Cls cls, ArrayList<Frame> results) {
		// Include the given class
		if (!results.contains(cls)) results.add(cls);
		// Look up all superclasses
		Iterator supers = cls.getSuperclasses().iterator();
		while (supers.hasNext()) {
			Object obj = supers.next();
			if (obj instanceof Cls) {
				Cls ancestor = (Cls) obj;
				if (!results.contains(ancestor)) results.add(ancestor);
			}
		}
	}
	
	/**
	 * Find all of the instances of a Protege Frame including
	 * those in other KBs if appropriate associations exist.
	 * 
	 * @param frame The frame
	 * @return All simple instances of the frame
	 */
	public Collection<Frame> allInstances(Frame frame) {
		ArrayList<Frame> results = new ArrayList<Frame>();
		// Do same kb first
		allInstancesInSingleKb(frame, results);
		// Now, look for any relations we are a parent of
		for (IsaRelation isar: isaRelations) {
			if (isar.isChildOf(frame)) {
				allInstancesInSingleKb(isar.child, results);
			}
		}		
		// Done
		return results;
	}
	
	/**
	 * Find all instances of a Protege Frame within a single KB.
	 * 
	 * @param frame The frame
	 * @param results All simple instances of the frame
	 */
	private void allInstancesInSingleKb(Frame frame, ArrayList<Frame> results) {
		if (frame instanceof Cls) {
			Iterator children = ((Cls) frame).getInstances().iterator();
			while (children.hasNext()) {
				Object child = children.next();
				if (child instanceof SimpleInstance) {
					SimpleInstance si = (SimpleInstance) child;
					if (!results.contains(si)) results.add(si);
				}
			}
		}
	}
	
	/**
	 * A private class to hold an ISA association.
	 * 
	 * @author R. James Firby
	 */
	private class IsaRelation {
		
		private Frame parent = null;
		private KnowledgeBase parentKb = null;
		private Frame child = null;
		private KnowledgeBase childKb = null;
		
		/**
		 * Create a new ISA relationship associating two Protege frames
		 * in different KBs.
		 * 
		 * @param a The child frame
		 * @param b The parent frame
		 */
		IsaRelation(Frame a, Frame b) {
			child = a;
			childKb = a.getKnowledgeBase();
			parent = b;
			parentKb = b.getKnowledgeBase();
		}
		
		/**
		 * Check whether this ISA relationship is a parent of the
		 * given frame.
		 * 
		 * @param c The possible child frame
		 * @return True if this frame is "below" the ISA relationship
		 */
		boolean isParentOf(Frame c) {
			KnowledgeBase kb = c.getKnowledgeBase();
			if (kb == childKb) {
				return isaInSingleKb(c, child);
			} else {
				return false;
			}
		}
		
		/**
		 * Check whether this ISA relationship is a child of the
		 * given frame.
		 * 
		 * @param c The possible parent frame
		 * @return True if this frame is "above" the ISA relationship
		 */
		boolean isChildOf(Frame c) {
			KnowledgeBase kb = c.getKnowledgeBase();
			if (kb == parentKb) {
				return isaInSingleKb(parent, c);
			} else {
				return false;
			}
		}
		
	}
	
}
