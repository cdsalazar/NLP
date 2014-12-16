package edu.uchsc.ccp.opendmap.dev;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

import edu.uchsc.ccp.opendmap.DMAPItem;
import edu.uchsc.ccp.opendmap.InfoPacket;
import edu.uchsc.ccp.opendmap.Parser;
import edu.uchsc.ccp.opendmap.ProtegeFrameItem;
import edu.uchsc.ccp.opendmap.Reference;

public class GenerifProcessorOld {

	public static Collection<String> processUtterance(Parser parser, String generifText) {
		// Reset the parser
		parser.reset();
		// Parse the utterance
		parser.parse(generifText.trim());
		// Grab up any generated output
		Vector<String> responses = new Vector<String>();
		// Find all of the unsubsumed references that were generated
		Collection<Reference> references = compressReferences(parser.getUnsubsumedReferences());
		// Process any references found
		for (Reference r: references) {
			responses.add(r.getReferenceString());
			if (r.isa("c-biobelief")) {
				processBioBelief(r, responses, 0);
			} else if (r.isa("c-biofunction")) {
				processBioFunction(r, responses, 0);
			} else {
				responses.add("I didn't understand that.");
			}
		}
		// If no response generated, we didn't understand what was said
		if (responses.isEmpty()) {
			responses.add("I didn't understand that.");
		}
		// Done
		return responses;
	}
	
	/* -------------------------------------------------------------------------------------
	 * Do some post-processing on the recognized references to remove some redundant nesting
	 * and logically equivalent parses.
	 */
	
	public static Collection<Reference> compressReferences(Collection<Reference> references) {
		// Compress references and filter out duplicates
		ArrayList<Reference> compressed = new ArrayList<Reference>();
		for (Reference reference: references) {
			Reference ref = compressReference(reference);
			if (ref != null) {
				boolean found = false;
				for (Reference other: compressed) {
					if ((other.getStart() == ref.getStart()) && (other.getEnd() == ref.getEnd())) {
						if (other.equals(ref)) {
							found = true;
							break;
						}
					}
				}
				if (!found) compressed.add(ref);
			}
		}
		// Now, filter out lower info content references
		ArrayList<Reference> result = new ArrayList<Reference>();
		for (Reference ref: compressed) {
			boolean found = false;
			for (Reference other: compressed) {
				if ((other != ref) && (other.getStart() == ref.getStart()) && (other.getEnd() == ref.getEnd())) {
					if (ref.isa("c-biofunction") && other.isa("c-biobelief")) {
						found = true;
						break;
					}
				}
			}
			if (!found) result.add(ref);
		}
		return result;
	}
	
	private static Reference compressReference(Reference reference) {
		if (reference.isa("g-pp")) {
			// Remove prepositional phrases, lift their internal object up.
			//  In a more complete grammar this will be too simplistic.
			return compressReference(reference.getSlotValue("object"));
		} else if (reference.isa("c-biofunction-agent")) {
			// Lift some bio function combinations
			return compressBiofunctionReference(reference);
		} else {
			// Compress slot fillers
			return compressSlotFillers(reference);
		}
	}
	
	private static Reference compressBiofunctionReference(Reference reference) {
		// If we have only a cause, then raise it up
		if (reference.isa("c-biofunction-agent")) {
			Reference agent = reference.getSlotValue("agent");
			Reference role = reference.getSlotValue("role");
			Reference cause = reference.getSlotValue("cause");
			Reference effect = reference.getSlotValue("effect");
			if ((agent == null) && (role == null) && (effect == null)) {
				return compressBiofunctionReference(cause);
			} else {
				return compressSlotFillers(reference);
			}
		} else {
			return compressSlotFillers(reference);
		}
	}
		
	private static Reference compressSlotFillers(Reference reference) {
		Vector<InfoPacket> slots = reference.getInformation();
		if ((slots == null) || (slots.size() == 0)) {
			return reference;
		} else {
			Vector<InfoPacket> newSlots = new Vector<InfoPacket>(slots.size());
			for (int i=0; i<slots.size(); i++) {
				InfoPacket oldSlot = slots.get(i);
				newSlots.add(new InfoPacket(oldSlot.getKey(), compressReference(oldSlot.getValue())));
			}
			return reference.duplicate(newSlots);
		}
	}
	
	/* -------------------------------------------------------------------------------------
	 * Process a parse and generate a nice text output describing it.  This is where
	 * some real work should go on to figure out what to do in response to the meaning
	 * of the reference.  All that happens now is generation of output text.
	 */

	private static boolean processBioBelief(Reference ref, Collection<String> responses, int indent) {
		if (ref.isa("c-biobelief-part")) {
			Reference agent = ref.getSlotValue("agent");
			//Reference belief = ref.getSlotValue("belief");
			Reference component = ref.getSlotValue("component");
			responses.add(makeLine(indent, "Statement of belief that agent has component:"));
			responses.add(makeLine(indent+2, "Agent =", getReferenceString(agent)));
			responses.add(makeLine(indent+2, "Belief =", getValueString(ref, "belief")));
			responses.add(makeLine(indent+2, "Component =", getReferenceString(component)));
			return true;
		} else if (ref.isa("c-biobelief-function")) {
			boolean okay = true;
			Reference agent = ref.getSlotValue("agent");
			Reference belief = ref.getSlotValue("belief");
			Reference function = ref.getSlotValue("function");
			int newIndent = indent;
			if ((agent != null) || (belief != null)) {
				responses.add(makeLine(indent, "Statement of belief that agent has function:"));
				newIndent = newIndent + 2;
				if (agent != null) {
					responses.add(makeLine(newIndent, "Agent =", getReferenceString(agent)));
				}
				if (belief != null) {
					responses.add(makeLine(newIndent, "Belief =", getValueString(belief)));
				} else {
					DMAPItem item = ref.getSlotDefault("belief");
					if (item != null) {
						responses.add(makeLine(newIndent, "Belief =", getValueString(item)));
					}
				}
			} else {
				responses.add(makeLine(indent, "Statement of belief that process takes place:"));
				newIndent = newIndent + 2;
			}
			okay = processBioFunction(function, responses, newIndent);
			return okay;
		} else {
			return false;
		}
	}
	
	private static boolean processBioFunction(Reference ref, Collection<String> responses, int indent) {
		if (ref.isa("c-biofunction-simple")) {
			Reference process = ref.getSlotValue("process");
			return processBioProcess(process, responses, indent);
//			Reference action = ref.getSlotValue("action");
//			return processBioAction(action, responses, indent);
		} else if (ref.isa("c-biofunction-agent") || ref.isa("c-biofunction-role")) {
			Reference agent = ref.getSlotValue("agent");
			Reference role = ref.getSlotValue("role");
			int newIndent = indent;
			if ((agent != null) || (role != null)) {
				responses.add(makeLine(indent, "Statement of function:"));
				newIndent = newIndent + 2;
				if (agent != null) {
					responses.add(makeLine(newIndent, "Agent =", getReferenceString(agent)));
				}
				if (role != null) {
					responses.add(makeLine(newIndent, "Role =", getValueString(role)));
				} else {
					DMAPItem item = ref.getSlotDefault("role");
					if (item != null) {
						responses.add(makeLine(newIndent, "Role =", getValueString(item)));
					}
				}
			}
			boolean okay = true;
			Reference cause = ref.getSlotValue("cause");
			Reference effect = ref.getSlotValue("effect");
			okay = processBioFunction(cause, responses, newIndent);
			if (okay && (effect != null)) {
				responses.add(makeLine(newIndent, "Causing:"));
				okay = processBioFunction(effect, responses, newIndent+2);
			}		
			return okay;
		} else {
			responses.add("Huh?");
			return true;
		}
	}
	
	private static boolean processBioProcess(Reference ref, Collection<String> responses, int indent) {
		if (ref.isa("c-transport")) {
			Reference patient = ref.getSlotValue("patient");
			Reference from = ref.getSlotValue("source");
			Reference to = ref.getSlotValue("destination");
			Reference frequency = ref.getSlotValue("frequency");
			Reference locale = ref.getSlotValue("locale");
			if (ref.isa("c-transport-two-way")) {
				responses.add(makeLine(indent, "Transport Process (Two Way):"));
			} else {
				responses.add(makeLine(indent, "Transport Process:"));
			}
			responses.add(makeLine(indent+2, "Of =", getReferenceString(patient)));
			if (ref.isa("i-nuclear-export")) {
				responses.add(makeLine(indent+2, "From =", getObjectString(ref, "i-nucleus")));
			} else {
				responses.add(makeLine(indent+2, "From =", getReferenceString(from)));
			}
			if (ref.isa("i-nuclear-import")) {
				responses.add(makeLine(indent+2, "To =", getObjectString(ref, "i-nucleus")));
			} else {
				responses.add(makeLine(indent+2, "To =", getReferenceString(to)));
			}
			if (frequency != null) {
				responses.add(makeLine(indent+2, "Frequency =", getObjectString(frequency)));
			}
			if (locale != null) {
				responses.add(makeLine(indent+2, "Locale =", getObjectString(locale)));
			}
			return true;
		} else if (ref.isa("c-grow")) {
			Reference patient = ref.getSlotValue("cell");
			responses.add(makeLine(indent, "Growth Process:"));
			responses.add(makeLine(indent+2, "Of =", getReferenceString(patient)));
			return true;
		} else if (ref.isa("c-activate")) {
			Reference patient = ref.getSlotValue("patient");
			responses.add(makeLine(indent, "Activation Process:"));
			responses.add(makeLine(indent+2, "Of =", getReferenceString(patient)));
			return true;
		} else if (ref.isa("c-anchor")) {
			Reference patient = ref.getSlotValue("patient");
			Reference location = ref.getSlotValue("location");
			responses.add(makeLine(indent, "Anchor Process:"));
			responses.add(makeLine(indent+2, "Of =", getReferenceString(patient)));
			responses.add(makeLine(indent+2, "To =", getReferenceString(location)));
			return true;
		} else {
			return false;
		}
	}
	
	private static String getReferenceString(Reference ref) {
		if (ref == null) return "<unknown>";
		Collection<Reference> things = ref.getSlotValues("object");
		if (things == null) return "<unknown>";
		boolean disjunct = ref.isa("c-reference-disjunct");
		StringBuffer sb = new StringBuffer();
		int i = 1;
		for (Reference thing: things) {
			if (i > 1) {
				if (i == things.size()) {
					if (disjunct) sb.append(" or ");
					else sb.append(" and ");
				} else {
					sb.append(", ");
				}
			}
			sb.append(getObjectString(thing));
			i = i + 1;
		}
		return sb.toString();
	}
	
	private static String getObjectString(Reference ref) {
		if (ref.isa("c-protein-receptor")) {
			// This slot needs to hold a reference
			Reference protein = ref.getSlotValue("protein");
			return "receptor for " + getObjectString(protein);
		} else {
			// Get the best thing to say
			DMAPItem item = ref.getItem();
			item = item.getSlotValue("english-singular");
			if (item == null) {
				return ref.getText();
			} else {
				return item.getText();
			}
		}
	}
	
	private static String getObjectString(Reference ref, String concept) {
		// Get the best thing to say
		DMAPItem item = ref.getItem();
		if (item instanceof ProtegeFrameItem) {
//			DMAPItem newItem = ((ProtegeFrameItem) item).getProtegeConcept(concept);
//			newItem = newItem.getSlotValue("english-singular");
//			if (newItem == null) {
//				return newItem.getText();
//			} else {
//				return concept;
//			}
			return concept;
		} else {
			return concept;
		}
	}
	
	private static String getValueString(Reference ref, String slot) {
		Reference val = ref.getSlotValue(slot);
		if (val != null) {
			return getValueString(val);
		} else {
			DMAPItem item = ref.getSlotDefault(slot);
			if (item != null) {
				return getValueString(item);
			} else {
				return "<unknown>";
			}
		}
	}
	
	private static String getValueString(Reference ref) {
		// Get the best thing to say
		DMAPItem item = ref.getItem();
		return getValueString(item);
	}
	
	private static String getValueString(DMAPItem refItem) {
		DMAPItem item = refItem.getSlotValue("english-plural");
		if (item == null) {
			item = refItem.getSlotValue("english-singular");
		}
		if (item == null) {
			return refItem.getText();
		} else {
			return item.getText();
		}
	}
	
	private static String makeLine(int indent, String message) {
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<indent; i++) sb.append(" ");
		sb.append(message);
		return sb.toString();
	}

	private static String makeLine(int indent, String prefix, String message) {
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<indent; i++) sb.append(" ");
		sb.append(prefix);
		sb.append(" ");
		sb.append(message);
		return sb.toString();
	}
}
