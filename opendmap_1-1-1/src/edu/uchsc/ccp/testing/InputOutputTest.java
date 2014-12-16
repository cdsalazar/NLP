/**
 * The OpenDMAP for Protege Project
 * August 2005
 */
package edu.uchsc.ccp.testing;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * An <code>InputOutputTest</code> is a test that expects one
 * input string and generates a set of output strings.  The test
 * succeeds if all of the output strings are generated when the input
 * is processed and no additional output results.
 * <p>
 * This class must be further specialized by adding a <code>runTest</code>
 * method which will be called to actually process the input and
 * generate the output strings.
 * 
 * @author R. James Firby
 */

/* Changes (most recent first)
 * 
 * 09-23-05 rjf - Removed misleading text from test failure messages.
 */
public abstract class InputOutputTest extends Test {
	
	/* The XML element names used within a pattern rule test */
	private static final String INPUTELEMENT = "input";
	private static final String OUTPUTELEMENT = "output";
	
	/* The current XML element being read */
	private String currentElement = null;

	/* The input pattern rule to be tested */
	private String input = null;
	/* The expected Pattern strings from this pattern rule */
	private ArrayList<String> outputs = new ArrayList<String>();
	
	/**
	 * Creates a new parser test with the given attributes.
	 * 
	 * @param kind The kind of XML element generating this test
	 * @param attributes The XML attributes passed to the form for this test
	 * @throws SAXException When there are errors in the attibutes for this test
	 */
	protected InputOutputTest(String kind, Attributes attributes) throws SAXException {
		super(kind, attributes);
	}
	
	/**
	 * Creates an empty, nonfunctional test.  This constructor
	 * exists so that subclasses can implement the <code>TestContext</code>
	 * interface if they desire.
	 */
	protected InputOutputTest() {}
	
	/**
	 * Called by the <code>XMLTestHandler</code> when an XML element is encountered withing the
	 * description of this test.
	 */
	protected boolean startElement(String namespaceURI, String localName, String qualifiedName, Attributes atts)
			throws SAXException {
		boolean consumed = true;
		if (currentElement != null) {
			// An unexpected element
			throw new SAXException("Found unexpected <" + qualifiedName + "> nested within <" + currentElement + "> element");
		
		} else if (localName.equalsIgnoreCase(INPUTELEMENT)) {
			// An input element
			if (input == null) {
				currentElement = INPUTELEMENT;
				startBuffering();
			} else {
				throw new SAXException("Found multiple <" + INPUTELEMENT + "> elements");
			}
			
		} else if (localName.equalsIgnoreCase(OUTPUTELEMENT)) {
			// An output element
			currentElement = OUTPUTELEMENT;
			startBuffering();
			
		} else  {
			// An unknown element
			consumed = false;
			//throw new SAXException("Found unknown <" + qualifiedName + "> element");			
		}
		return consumed;
	}

	/**
	 * Called by the <code>XMLTestHandler</code> to finish parsing an XML element in the
	 * description of this test.
	 */
	protected boolean endElement(String namespaceURI, String localName, String qualifiedName)
			throws SAXException {
		boolean consumed = true;
		if (currentElement == null) {
			// Bad nesting
			throw new SAXException("Found unexpected </" + qualifiedName + "> element");			
			
		} else if (localName.equals(INPUTELEMENT)) {
			// An input element
			if ((input == null) && (INPUTELEMENT.equalsIgnoreCase(currentElement))) {
				input = getBuffer();
				currentElement = null;
			} else {
				throw new SAXException("Found </" + INPUTELEMENT + "> nested within <" + currentElement + "> element");
			}
			
		} else if (localName.equals(OUTPUTELEMENT)) {
			// An output element
			if (OUTPUTELEMENT.equalsIgnoreCase(currentElement)) {
				outputs.add(getBuffer());
				currentElement = null;
			} else {
				throw new SAXException("Found </" + INPUTELEMENT + "> nested within <" + currentElement + "> element");
			}
			
		} else {
			// An unknown element
			//throw new SAXException("Found unexpected </" + qualifiedName + "> element");		
			consumed = false;
		}		
		return consumed;
	}

	/**
	 * Called by <code>TestSuite</code> to run this test.
	 */
	public boolean run(TestContext context, PrintStream stream) {
		// Parse the pattern
		ArrayList<String> actualOutputs = new ArrayList<String>();
		String message = runTest(context, input, actualOutputs);
		// See if any errors occurred
		boolean okay = (message == null);
		// Did we get the expected number of outputs
		okay = okay && (outputs.size() == actualOutputs.size());
		// Make sure every result was expected
		int i = 0;
		while (okay && (i < actualOutputs.size())) {
			okay = outputs.contains(actualOutputs.get(i++));
		}
		// Make sure every expected result was found
		i = 0;
		while (okay && (i < outputs.size())) {
			okay = actualOutputs.contains(outputs.get(i++));
		}
		// If not errors, then we're done
		if (okay && !isFailTest()) return true;
		if (!okay && isFailTest()) return true;
		// Otherwise, assemble an error report
		if (isFailTest()) {
			stream.println("** Test passed but should have failed: " + getName());
		} else {
			stream.println("** Test failed: " + getName());
		}
		stream.println("     Input: " + input);
		if (message != null) {
			// An actual parsing error
			stream.println("     Error: " + message);
		} else {
			// Say how many matches there were
			stream.println("     Expected " + outputs.size() + " outputs and got " + actualOutputs.size() + ".");
			// The patterns don't match.
			for (String out: actualOutputs) {
				if (outputs.contains(out)) {
					stream.println("     Good output: " + out);
				} else {
					stream.println("     Unexpected output: " + out);
				}
			}
			for (String xout: outputs) {
				if (!actualOutputs.contains(xout)) {
					stream.println("     Missing output: " + xout);
				}
			}
		}
		return false;
	}
	
	/**
	 * Called by <code>InputOutputTest</code> to run a single test within the
	 * supplied test context.
	 * 
	 * @param context The context in which to execute this test.
	 * @param input The input string defining the test.
	 * @param outputStrings An list to fill with output strings that result from the test.
	 * @return Null if the test executes, an error string if something goes wrong.
	 */
	protected abstract String runTest(TestContext context, String input, Collection<String> outputStrings);

}
