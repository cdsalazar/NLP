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
 * The <code>PatternRuleTest</code> class represents a single DMAP pattern rule
 * test.
 * <p>
 * A pattern rule test checks to make sure that a pattern rule text
 * string parses into a DMAP <code>Pattern</code> the way it is supposed to.  It
 * tests the code that reads rules and turns then into Patterns.
 * <p>
 * The format for a pattern rule test is:
 * <pre>
 *  &lt;pattern-rule name="name" [fail="true"]&gt;
 *    &lt;input&gt;the pattern rule string&lt;/input&gt;
 *    &lt;output&gt;a string matching a DMAP Pattern when printed&lt;/output&gt;
 *    &lt;output&gt;...&lt;/output&gt;
 *  &lt;/pattern-rule&gt;
 * </pre>
 * <p>
 * A pattern rule test must have one <code>input</code> pattern rule
 * and any number of <code>output</code> patterns to represent the 
 * results of parsing the pattern rule.  Every output string must be
 * generated and matched exactly for the test to pass.
 * 
 * @author R. James Firby
 *
 */
public class PatternRuleTest extends InputOutputTest {
	
	/**
	 * Creates a new pattern rule test with the given attributes.
	 * 
	 * @param kind The kind of XML element generating this test
	 * @param attributes The XML attributes passed to the form for this test
	 * @throws SAXException When there are errors in the attibutes for this test
	 */
	public PatternRuleTest(String kind, Attributes attributes) throws SAXException {
		super(kind, attributes);
	}
	
	/**
	 * Runs a single pattern rule test within the supplied test context.
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
				patterns = ASTConverter.readPatternsFromString(kb, input, true, null);
				if (patterns == null) error = "Rule parses to null.";
			} catch (Exception e) {
				error = e.toString(); //e.getMessage();
			} catch (Error e) {
				error = e.getMessage();
			}
		}
		// Generate appropriate output
		if (error == null) {
			// Turn the patterns into strings
			for (Pattern p: patterns) outputStrings.add(p.toPatternString());
		}
		// Done
		return error;
	}
}
