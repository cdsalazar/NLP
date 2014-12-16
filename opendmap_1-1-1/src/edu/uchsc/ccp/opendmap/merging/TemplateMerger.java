package edu.uchsc.ccp.opendmap.merging;

import java.util.ArrayList;
import java.util.Collection;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Slot;
import edu.uchsc.ccp.opendmap.DMAPItem;
import edu.uchsc.ccp.opendmap.InfoPacket;
import edu.uchsc.ccp.opendmap.ProtegeFrameItem;
import edu.uchsc.ccp.opendmap.ProtegeProjectGroup;
import edu.uchsc.ccp.opendmap.ProtegeSlotItem;
import edu.uchsc.ccp.opendmap.Reference;

public class TemplateMerger {

	ArrayList<Reference> contextReferences;
	ProtegeProjectGroup protegeProjectGroup;

	public TemplateMerger(ProtegeProjectGroup ppg) {
		contextReferences = new ArrayList<Reference>();
		protegeProjectGroup = ppg;
	}

	/** 
	 * Add a set of References to the context.
	 * @param newRefs the Collection of new References to add
	 */
	public void addContextReferences(Collection<Reference> newRefs) {
		contextReferences.addAll(newRefs);
	}
	
	/**
	 * Add a Reference to the context
	 * @param ref the new Reference to add
	 */
	public void addContextReference(Reference ref) {
		contextReferences.add(ref);
	}

	/**
	 * Add a set of References to the context, but try to update them with
	 * information from the context first.
	 * @param newRefs the Collection of new References to add/process
	 */
	public void mergeReferences(ArrayList<Reference> refs) {
		for (Reference ref : refs) {
			mergeReference(ref);
		}
	}

	/**
	 * Add a Reference to the context, but try to update it with
	 * information from the context first.  This uses a recency algorithm.
	 * @param ref a References to merge
	 */
	@SuppressWarnings("unchecked")
	public void mergeReference(Reference ref) {
		Collection<String> missingSlots = ref.missingSlots();
		if ( !missingSlots.isEmpty() ) {
			DMAPItem refItem = ref.getItem();
			if ( refItem instanceof ProtegeFrameItem ) {
				Frame frame = ((ProtegeFrameItem)refItem).getFrame();
				if ( frame instanceof Cls ) {
					Cls cls = (Cls) frame;

					//Collection<Slot> slots = getAllSlots(cls);
					Collection<Slot> slots = cls.getTemplateSlots();
					for ( Slot slot : slots ) {
						if ( missingSlots.contains(slot.getName())) {
							// this is a missing slot

							// Go through Context References to find a valid filler for the slot
							for ( int i=contextReferences.size()-1; i>= 0; i--) {
								Reference recentRef = contextReferences.get(i);
								// don't fill in a slot with an alternative match for the same span
								// TODO: expand to overlapping spans?
								if ( recentRef.getStart() == ref.getStart() && recentRef.getEnd() == ref.getEnd())
									continue; 

								// Create a new slot item to name this slot
								ProtegeSlotItem slotItem = new ProtegeSlotItem(slot, frame, protegeProjectGroup, false);
								// Verify that the value is a valid filler for the slot
								if (slotItem.validFiller(recentRef.getItem())) {
									// special case for "is_equivalent_to" -- verify complete compatibility
									if ( slot.getName().equals("is_equivalent_to") ) {
										if ( recentRef.getItem() instanceof ProtegeFrameItem ) {
											if ( !(ref.unifies(recentRef) )) {
													continue;
											}
										}
									}
									
									System.err.println("**TM** Adding new slot item " + slot.getName() + 
											", filler is " + recentRef.getText() + "[" + recentRef.getStart() + "-" + recentRef.getEnd() + "] " + "(" +recentRef.getCharacterStart() + "," +  recentRef.getCharacterEnd() + ")");
									// Build and InfoPacket for the slot
									InfoPacket info = new InfoPacket(slotItem, recentRef);
									ref.addInfoPacket(info);
									
									// Now we've filled the slot, it's not missing any more
									missingSlots.remove(slot.getName());
									
									// Don't go back any further in the context!
									break;
								}
							}
						}				
					}
				}
			}
		}
		//TODO: alternatively, only add after all of the current set have been processed.
		addContextReference(ref);
	}

/*	@SuppressWarnings("unchecked")
	private static Collection<Slot> getAllSlots(Cls cls) {
		Collection<Slot> slots = new ArrayList<Slot>(cls.getOwnSlots());
		slots.addAll(cls.getTemplateSlots());
		return slots;
	}*/

}
