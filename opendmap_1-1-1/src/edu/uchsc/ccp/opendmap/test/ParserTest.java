/**
 * The OpenDMAP for Protege Project
 * July 2005
 */
package edu.uchsc.ccp.opendmap.test;

import java.util.Collection;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import edu.uchsc.ccp.opendmap.Parser;
import edu.uchsc.ccp.opendmap.Reference;
import edu.uchsc.ccp.opendmap.TextItem;
import edu.uchsc.ccp.testing.InputOutputTest;
import edu.uchsc.ccp.testing.TestContext;

/**
 * The <code>ParserTest</code> class represents a single DMAP parser test.
 * <p>
 * A parser test checks to make sure that a piece of input text
 * parses into a set of DMAP <code>Reference</code> objects the way it is 
 * supposed to.  It tests the basic DMAP code from end to end.
 * <p>
 * The format for a parser test is:
 * <pre>
 *  &lt;test name="name" [fail="true"] [includeText="true"] [includeSubsumed="true"]&gt;
 *    &lt;input&gt;the input text&lt;/input&gt;
 *    &lt;output&gt;a string matching a DMAP Reference when printed&lt;/output&gt;
 *    &lt;output&gt;...&lt;/output&gt;
 *  &lt;/test&gt;
 * </pre>
 * <p>
 * A parser test must have one <code>input</code> phrase
 * and any number of <code>output</code> references to represent the 
 * results of parsing the phrase.  Every output string must be
 * generated and matched exactly for the test to pass.
 * 
 * @author R. James Firby
 */

/* Changes (most recent first)
 * 
 * 09-26-05 rjf - Moved subsumes method into Reference.
 * 09-15-05 rjf - Strengthened isSubsumed to include references that are slot fillers
 */
public class ParserTest extends InputOutputTest {
	
	/* Ignore text references */
	boolean ignoreText = true;
	/* Ignore subsumed frames and instances */
	boolean ignoreSubsumed = true;
	
	/**
	 * Creates a new parser test with the given attributes.
	 * 
	 * @param kind The kind of XML element generating this test
	 * @param attributes The XML attributes passed to the form for this test
	 * @throws SAXException When there are errors in the attibutes for this test
	 */
	public ParserTest(String kind, Attributes attributes) throws SAXException {
		super(kind, attributes);
		// Grab some specialized attributes
		String words = attributes.getValue("includeText");
		if (words != null) ignoreText = !Boolean.parseBoolean(words);
		String subsumes = attributes.getValue("includeSubsumed");
		if (subsumes != null) ignoreSubsumed = !Boolean.parseBoolean(subsumes);
	}
	
	/**
	 * Runs a single parser test within the supplied test context.
	 */
	protected String runTest(TestContext context, String input, Collection<String> outputStrings) {
		String error = null;
		// Check for the correct context
		Parser parser = null;
		if (context instanceof ParserTestContext) {
			parser = ((ParserTestContext)context).getParser();
		} else {
			error = "Incorrect testing context supplied.";
		}
		// Parse the input into References
		Collection<Reference> references = null;
		if (error == null) {
			try {
				parser.reset();
				parser.parse(input);
				if (ignoreSubsumed) {
					references = parser.getUnsubsumedReferences();
				} else {
					references = parser.getReferences();
				}
				if (references == null) error = "Input parses to null.";
			} catch (Exception e) {
				error = e.toString(); //e.getMessage();
			} catch (Error e) {
				error = e.getMessage();
			}
		}
		// Generate appropriate output
		if (error == null) {
			// Turn the patterns into strings
			for (Reference r: references)
				if (ignoreText && (r.getItem() instanceof TextItem)) {
					// Ignore this reference
				//} else if (ignoreSubsumed && isSubsumed(r, references)) {
					// Ignore this reference
				} else {
					// Count this reference
					if (r.getMissing() > 0) {
						outputStrings.add(r.getReferenceString() + " from " + r.getStart() + " to " + r.getEnd() + " ignoring " + r.getMissing());
					} else {
						outputStrings.add(r.getReferenceString() + " from " + r.getStart() + " to " + r.getEnd());
					}
				}
		}
		// Done
		return error;
	}
	
//	/**
//	 * Determine if a reference is subsumed by another reference
//	 * in a set.
//	 * 
//	 * @param r The reference to check
//	 * @param references Possible subsuming references
//	 * @return True if r is subsumed by a member of references
//	 */
//	private boolean isSubsumed(Reference r, Collection<Reference> references) {
//		boolean subsumed = false;
//		int rStart = r.getStart();
//		int rEnd = r.getEnd();
//		for (Reference s: references) {
//			if (s != r) {
//				int sStart = s.getStart();
//				int sEnd = s.getEnd();
//				if ((sStart <= rStart) && (rEnd <= sEnd) && ((sStart != rStart) || (sEnd != rEnd))) {
//					subsumed = true;
//					break;
//				} else if ((sStart == rStart) && (sEnd == rEnd) && s.subsumes(r)) {
//					subsumed = true;
//					break;
//				}
//			}
//		}
//		return subsumed;
//	}
	
}
