/**
 * The OpenDMAP for Protege Project
 * January 2006
 */
package edu.uchsc.ccp.opendmap.example;

import java.util.ArrayList;
import java.util.Collection;

import edu.uchsc.ccp.opendmap.Parser;
import edu.uchsc.ccp.opendmap.Reference;

/**
 * This class implements a simple GeneRIF processor.  It hands a generif to
 * a DMAP parser and then extracts the concepts of interest, sorts them by
 * "score" and adds them to the result strings.
 * 
 * @author R. James Firby
 */
public class GenerifProcessor {

	/**
	 * Process a generif and return the concepts of interest.
	 * 
	 * @param parser The DMAP parser to parse the generif
	 * @param generifText The generif text
	 * @param interests A collection of concept names.  Only these concepts will be returned.
     * @param debug if true, processRecognizedReferences will print debugging information
	 * @return A list of text strings to print as output
	 */
	public static Collection<Reference> processUtterance(Parser parser, String generifText, Collection<String> interests, boolean debug) {
		// Reset the parser
		parser.reset();
		// Parse the utterance
		parser.parse(generifText.trim());
		// Extract the best matches
		return processRecognizedReferences(parser, interests, debug);
	}
	
	public static Collection<Reference> processRecognizedReferences(Parser parser, Collection<String> interests, boolean debug) {
		if (debug) {
			// Print out everything we saw
			Collection<Reference> all = parser.getReferences();
			System.out.println("\nAll references:");
			for (Reference r: all) {
				System.out.println(r.getStart() + ".." + r.getEnd() + " " + r.getReferenceString());
			}
		}
		
		// Find all of the unsubsumed references that were generated
		Collection<Reference> references = parser.getUnsubsumedReferences(debug);
		if (debug) {
			System.out.println("\nUnsubsumed references");
			for (Reference r: references) {
				System.out.println(r.getStart() + ".." + r.getEnd() + " " + r.getReferenceString());
			}
		}
		// Extract and score any interesting references found
		ArrayList<Reference> seen = new ArrayList<Reference>();
		for (Reference r: references) {
			if (isConceptOfInterest(r, interests)) {
				// Sort into list
				boolean done = false;
				for (int i=0; i<seen.size(); i++) {
					Reference ref = seen.get(i);
					if (calculateScore(r) > calculateScore(ref)) {
						seen.add(i, r);
						done = true;
						break;
					}
				}
				if (!done) {
                    seen.add(r);
                }
			}
		}
		// Done
		return seen;
	}
	
	/**
	 * Check whether a reference is a concept of interest.
	 * 
	 * @param r The reference to check.
	 * @param interests The collection of concepts of interest
	 * @return True if the reference is a concept of interest
	 */
	private static boolean isConceptOfInterest(Reference r, Collection<String> interests) {
		// If no interests specified, return everything
		if ((interests == null) || (interests.size() <= 0)) return true;
		// Check whether the reference is an interesting concept
		boolean found = false;
		for (String interest: interests) {
			if (r.isa(interest)) {
				found = true;
				break;
			}
		}
		return found;
	}
	
	/**
	 * Score the reference.
	 * 
	 * @param r The reference
	 * @return The score
	 */
	public static double calculateScore(Reference r) {
		return ((double)(calculateSpan(r) - r.getMissing())) / ((double)(calculateSpan(r)));
	}
	
	/**
	 * Calculate the span of a reference.
	 * 
	 * @param r The reference
	 * @return The span
	 */
	private static int calculateSpan(Reference r) {
		return r.getEnd() - r.getStart() + 1;
	}
}
