/**
 * The OpenDMAP for Protege Project
 * August 2005
 */
package edu.uchsc.ccp.opendmap.example;

import java.util.Collection;
import java.util.logging.Level;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.uchsc.ccp.opendmap.Parser;
import edu.uchsc.ccp.opendmap.pattern.ParseException;
import edu.uchsc.ccp.testing.InputOutputTest;
import edu.uchsc.ccp.testing.TestContext;
import edu.uchsc.ccp.testing.TestSuite;

/**
 * This class implements everything needed to create 'Loves' and
 * 'Hates' reference test suites within the general testing framework.
 * <p>
 * This class extends the <code>InputOutputTest</code> class to 
 * represent a single Love/Hate test.
 * <p>
 * This class also implements the <code>TestContext</code> interface
 * to be used by these tests.
 * 
 * @author R. James Firby
 */
public class LoveHateTester extends InputOutputTest implements TestContext {

	/**
	 * The parser initialized when this is a testing context.
	 */
	public static Parser parser = null;
	
	/**
	 * Create an instance of this class as a TestContext.
	 */
	public LoveHateTester() {}
	
	/**
	 * Initialize this as a new Love/Hate testing context
	 */
	public boolean initialize(TestSuite suite, Collection<Object> errors) {
		// Construct the necessary context for running tests
		parser = new Parser(Level.OFF);
		
		// Setup the desired protege project
		boolean okay = true;
		String protegeProject = suite.getValue("protege-project");
		Project project = new Project(protegeProject, errors);
		if (errors.size() != 0) return false;
		KnowledgeBase kb = project.getKnowledgeBase();
		// Add recognition patterns to the parser
		Collection<String> patternFiles = suite.getValues("pattern-file");
		if ((patternFiles == null) || patternFiles.isEmpty()) {
			errors.add("No pattern files specified.");
		} else {
			for (String filename: patternFiles) {
				try {
					parser.addPatternsFromFile(filename, kb);
				} catch (ParseException e) {
					errors.add("Problem loading pattern file: " + e.toString());
					okay = false;
					break;
				}
			}
		}
		
		// Done
		return okay;
	}

	/**
	 * Tear down the Love/Hate testing state
	 */
	public boolean terminate(Collection<Object> errors) {
		// Nothing to do here really
		parser = null;
		return true;
	}

	/**
	 * Creates a new love/hate test instance with the given attributes.
	 * 
	 * @param kind The kind of XML element generating this test
	 * @param attributes The XML attributes passed to the form for this test
	 * @throws SAXException When there are errors in the attibutes for this test
	 */
	public LoveHateTester(String kind, Attributes attributes) throws SAXException {
		super(kind, attributes);
	}
	
	/**
	 * Run a Love/Hate processor test
	 */
	@Override
	protected String runTest(TestContext context, String input,
		Collection<String> outputStrings) {
		// Run the test
		if (context instanceof LoveHateTester) {
			//Parser p = ((LoveHateTester)context).parser;
			if (parser == null) return "Uninitialized test context :" + context.toString();
			Collection<String> responses = LoveHateProcessor.processUtterance(parser, input);
			if (responses != null) {
				for (String response: responses) {
					outputStrings.add(response);
				}
			}
		} else {
			return "Invalid test context :" + context.toString();
		}
		// Done
		return null;
	}


}
