/**
 * The OpenDMAP for Protege Project
 * August 2005
 */
package edu.uchsc.ccp.opendmap.example;

import java.util.Collection;
import java.util.Vector;
import java.util.logging.Level;

import edu.uchsc.ccp.opendmap.DMAPItem;
import edu.uchsc.ccp.opendmap.Parser;
import edu.uchsc.ccp.opendmap.Reference;

/**
 * This class is an example to illustrate how DMAP references
 * can be processed to extract their meaning and generate an
 * appropriate response.
 * <p>
 * It implements a very simplistic processor for 'Loves' and
 * 'Hates' relationships as defined in the 'people-loving-people'
 * sample Protege project.
 * 
 * @author R. James Firby
 */
public class LoveHateProcessor {
	
	/**
	 * Process an utterance by dealing with all of the 'Loves' and
	 * 'Hates' references that it generates.
	 * 
	 * @param parser The DMAP parser to parse the utterance and generate the references.
	 * @param utterance The utterance to parse.
	 * @return A set of response strings that result from dealing with the parsed references.
	 */
	public static Collection<String> processUtterance(Parser parser, String utterance) {
		// Reset the parser
		parser.reset();
		// Parse the utterance
		parser.parse(utterance);
		// Grab up any response strings
		Vector<String> responses = new Vector<String>();
		// Process any love relationships
		Collection<Reference> references = parser.getFrameReferences("Loves");
		if ((references != null) && !references.isEmpty()) {
			for (Reference ref: references) {
				processLoveReference(parser, ref, responses);
			}
		}
		// Process any hate relationships
		references = parser.getFrameReferences("Hates");
		if ((references != null) && !references.isEmpty()) {
			for (Reference ref: references) {
				processHateReference(parser, ref, responses);
			}
		}
		// If no response generated, we didn't understand what was said
		if (responses.isEmpty()) {
			responses.add("I didn't understand that.");
		}
		// Done
		return responses;
	}

	/**
	 * Process 'Loves' references and generate appropriate output for the
	 * different cases that might be generated.
	 * 
	 * @param p The DMAP parser.
	 * @param reference A single 'Loves' reference.
	 * @param responses A place to add responses that result from processing this reference.
	 */
	private static void processLoveReference(Parser p, Reference reference, Vector<String> responses) {		
		// Look up all instances consistent with what was spoken.
		//  We should get a list of "Loves" relationships with both slots filled in and
		//  either the 'lover' or 'beloved' (or both) slot constrained by what the user spoke.
		Collection<DMAPItem> insts = reference.getConsistentInstances();
		// To figure out how to reply, pull out the spoken pieces
		Reference lover = reference.getSlotValue("lover");
		Reference beloved = reference.getSlotValue("beloved");
		// Add a few logging messages if we're doing logging
		if (lover != null) p.log(Level.INFO, "Lover referenced as " + lover.getText());
		if (beloved != null) p.log(Level.INFO, "Beloved referenced as " + beloved.getText());
		// See what piece of information is missing
		if ((lover == null) && (beloved == null)) {
			// Just said "loves", or something similar
			//  We have nothing to say
			return;
			
		} else if ((lover == null) || (beloved == null) || !lover.isInstance() || !beloved.isInstance()) {
			// Asking who loves who, reply with all consistent relationships
			if ((insts == null) || insts.isEmpty()) {
				// There are no love relationship with the person mentioned
				if ((lover != null) && lover.isInstance()) {
					// The lonely person is the lover, eg. "Who does John love"
					responses.add(lover.getText() + " is not known to love anyone.");
				} else if ((beloved != null) && beloved.isInstance()) {
					// The lonely person is the beloved, eg. "Who loves John"
					responses.add("Nobody is known to love " + beloved.getText() + ".");
				} else {
					// A query about a non-specific lover, eg "Who does anyone love", "Who loves somebody"
					responses.add("Nobody is known to love anyone.");
				}
			} else {
				// There are love relationships, print them
				for (DMAPItem i: insts) {
					DMAPItem ilover = i.getSlotValue("lover");
					DMAPItem ibeloved = i.getSlotValue("beloved");
					responses.add(ilover.getText() + " loves " + ibeloved.getText() + ".");
				}
			}
			
		} else if (p.hasWordReference("does")) {
			// User spoke both slots as a question, reply with yes or no
			if ((insts == null) || insts.isEmpty()) {
				// The database says there are no 'loves' relations consistent with the question
				responses.add(lover.getText() + " is not known to love " + beloved.getText() + ".");
			} else {
				// There is at least one instance of the relationship asked about
				responses.add("Yes, " + lover.getText() + " loves " + beloved.getText() + ".");
			}
			
		} else {
			// The user has spoken both slots as a statement
			if ((insts == null) || insts.isEmpty()) {
				// The database says there are no 'loves' relations consistent with the statement
				responses.add("I didn't know that.");
			} else {
				// There is at least one instance of the specific relationship asked about
				responses.add("Yes, I knew that " + lover.getText() + " loves " + beloved.getText() + ".");
			}
		}

	}

	/**
	 * Process 'Hates' references and generate appropriate output for the
	 * different cases that might be generated.
	 * 
	 * @param p The DMAP parser.
	 * @param reference A single 'Hates' reference.
	 * @param responses A place to add responses that result from processing this reference.
	 */
	private static void processHateReference(Parser p, Reference reference, Vector<String> responses) {		
		// Look up all instances consistent with what was spoken.
		//  We should get a list of "Hates" relationships with both slots filled in and
		//  either the 'hater' or 'hated' (or both) slot constrained by what the user spoke.
		Collection<DMAPItem> insts = reference.getConsistentInstances();
		
		// To figure out how to reply, pull out the spoken pieces
		Reference hater = reference.getSlotValue("hater");
		Reference hated = reference.getSlotValue("hated");
		if (hater != null) p.log(Level.INFO, "Hater referenced as " + hater.getText());
		if (hated != null) p.log(Level.INFO, "Hated referenced as " + hated.getText());
		// See what piece of information is missing
		if ((hater == null) && (hated == null)) {
			// Just said "hates", or something similar
			//System.out.println("I don't understand what you said.");
			return;
			
		} else if ((hater == null) || (hated == null) || !hater.isInstance() || !hated.isInstance()) {
			// Asking who loves who, reply with all consistent relationships
			if ((insts == null) || insts.isEmpty()) {
				// There are no love relationship with the person mentioned
				if ((hater != null) && hater.isInstance()) {
					// The lonely person is the hater, eg. "Who does John hate"
					responses.add(hater.getText() + " is not known to hate anyone.");
				} else if ((hated != null) && hated.isInstance()) {
					// The lonely person is the hated, eg. "Who hates John"
					responses.add("Nobody is known to hate " + hated.getText() + ".");
				} else {
					// A query about a non-specific lover, eg "Who does anyone hate", "Who hates somebody"
					responses.add("Nobody is known to hate anyone.");
				}
			} else {
				// There are love relationships, print them
				for (DMAPItem i: insts) {
					DMAPItem ilover = i.getSlotValue("hater");
					DMAPItem ibeloved = i.getSlotValue("hated");
					responses.add(ilover.getText() + " hates " + ibeloved.getText() + ".");
				}
			}
			
		} else if (p.hasWordReference("does")) {
			// User spoke both slots as a question, reply with yes or no
			if ((insts == null) || insts.isEmpty()) {
				// The database says there are no 'hates' relations consistent with the question
				responses.add(hater.getText() + " is not known to hate " + hated.getText() + ".");
			} else {
				// There is at least one instance of the relationship asked about
				responses.add("Yes, " + hater.getText() + " hates " + hated.getText() + ".");
			}
			
		} else {
			// The user has spoken both slots as a statement
			if ((insts == null) || insts.isEmpty()) {
				// The database says there are no 'hates' relations consistent with the statement
				responses.add("I didn't know that.");
			} else {
				// There is at least one instance of the specific relationship asked about
				responses.add("Yes, I knew that " + hater.getText() + " hates " + hated.getText() + ".");
			}
		}

	}
	
}
