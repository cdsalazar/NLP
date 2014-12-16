/**
 * The OpenDMAP for Protege Project
 * July 2005
 */
package edu.uchsc.ccp.opendmap.test;

import java.util.Collection;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.uchsc.ccp.opendmap.ASTConverter;
import edu.uchsc.ccp.opendmap.Pattern;
import edu.uchsc.ccp.testing.InputOutputTest;
import edu.uchsc.ccp.testing.TestContext;

/**
 * The <code>PatternTest</code> class represents a single DMAP pattern
 * normalization test.
 * <p>
 * A prediction test checks to make sure that a pattern string parses
 * into a DMAP <code>Pattern</code> that then normalizes the way it is 
 * supposed to.  It tests the code needed to predict and advance Patterns.
 * <p>
 * The format for a prediction test is:
 * <pre>
 *  &lt;pattern name="name"&gt;
 *    [&lt;frame&gt;Protege frame name&lt;frame&gt;]
 *    &lt;input&gt;the pattern string&lt;/input&gt;
 *    &lt;output&gt;a string matching a DMAP Prediction when printed&lt;/output&gt;
 *    &lt;output&gt;...&lt;/output&gt;
 *  &lt;/pattern&gt;
 * </pre>
 * <p>
 * A prediction test must have one <code>input</code> pattern string
 * and any number of <code>output</code> strings to represent the 
 * results of parsing the pattern and normalizing it.  If the pattern
 * contains a slot reference, the <code>frame</code> element
 * must also be included to name the protege frame holding the slots.  
 * <p>
 * Every output string must be
 * generated and matched exactly for the test to pass.
 * 
 * @author R. James Firby
 *
 */
public class PatternTest extends InputOutputTest {
	
	/* The XML element names used within a pattern rule test */
	private static final String TARGETELEMENT = "frame";
	
	/* The current XML element being read */
	private String currentElement = null;

	/* The target frame for this pattern */
	private String frameName = null;
	
	/**
	 * Creates a new pattern test with the given attributes.
	 * 
	 * @param kind The kind of XML element generating this test
	 * @param attributes The XML attributes passed to the form for this test
	 * @throws SAXException When there are errors in the attibutes for this test
	 */
	public PatternTest(String kind, Attributes attributes) throws SAXException {
		super(kind, attributes);
	}
	
	/**
	 * Called by the <code>XMLTestHandler</code> when an XML element is encountered withing the
	 * description of this test.
	 */
	protected boolean startElement(String namespaceURI, String localName, String qualifiedName, Attributes atts)
			throws SAXException {
		if (super.startElement(namespaceURI, localName, qualifiedName, atts)) {
			// Our parent wanted this xml element, we're done
			return true;
		} else if (localName.equalsIgnoreCase(TARGETELEMENT)) {
			// A frame element
			if (frameName == null) {
				currentElement = TARGETELEMENT;
				startBuffering();
				return true;
			} else {
				throw new SAXException("Found multiple <" + TARGETELEMENT + "> elements");
			}
		} else {
			// An unknown element
			return false;
		}
	}

	/**
	 * Called by the <code>XMLTestHandler</code> to finish parsing an XML element in the
	 * description of this test.
	 */
	protected boolean endElement(String namespaceURI, String localName, String qualifiedName)
			throws SAXException {
		if (localName.equals(TARGETELEMENT)) {
			// A frame element
			if ((frameName == null) && (TARGETELEMENT.equalsIgnoreCase(currentElement))) {
				frameName = getBuffer();
				currentElement = null;
				return true;
			} else {
				throw new SAXException("Found </" + TARGETELEMENT + "> out of context.");
			}

		} else {
			// An unknown element, perhaps our parent wants it
			return super.endElement(namespaceURI, localName, qualifiedName);			
		}		
	}

	/**
	 * Runs a single pattern test within the supplied test context.
	 */
	protected String runTest(TestContext context, String input, Collection<String> outputStrings) {
		String error = null;
		// Check for the correct context
		KnowledgeBase kb = null;
		if (context instanceof ParserTestContext) {
			kb = ((ParserTestContext)context).getProtegeKnowledgeBase();
		} else {
			error = "Incorrect testing context supplied.";
		}
		// Parse the input into Patterns
		Collection<Pattern> patterns = null;
		if (error == null) {
			try {
				//if (getName().equals("Test-308")) {
				//	error ="Badness";
				//}
				Pattern pattern = ASTConverter.readPatternFromString(input, kb, frameName, true, null);
				if (pattern == null) {
					error = "Pattern parses to null.";
				} else {
					patterns = pattern.normalize();
				}
			} catch (Exception e) {
				error = e.toString(); //e.getMessage();
			} catch (Error e) {
				error = e.getMessage();
			}
		}
		// Generate appropriate output
		if (error == null) {
			// Turn the patterns into strings
			for (Pattern p: patterns) {
				if (p == null) {
					outputStrings.add("");
				} else {
					outputStrings.add(p.toNormalizedString());
				}
			}
		}
		// Done
		return error;
	}
}
