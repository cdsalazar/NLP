package edu.uchsc.ccp.opendmap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import org.junit.Test;

import edu.uchsc.ccp.opendmap.configuration.ParserFactory;
import edu.uchsc.ccp.opendmap.dependency.DMAPDependencyGraphFunctionHandler;
import edu.uchsc.ccp.opendmap.dependency.DependencyRelation;

/**
 * This test class is used primarily to test pattern syntax (or rather pattern features) to ensure they work as
 * expected. This class is also meant to serve as working documentation for how to write OpenDMAP patterns. <br>
 * <br>
 * Many of the tests in this class deal with the "activate" frame. This frame has two slots:
 * 
 * <pre>
 * c-activate:
 *    activator &lt; c-protein
 *    activated-entity &lt; c-molecule
 *    trigger &lt;
 * </pre>
 * 
 * 
 * @author Bill Baumgartner
 * 
 */
public class PatternUsageTest {

	/*
	 * This configuration file points to the dmap-test-ontology, but does not include any pattern files. All patterns
	 * must be added manually for these unit tests.
	 */
	private static final File OPENDMAP_CONFIG_FILE = new File("data/test/edu.uchsc.ccp.opendmap.pattern-usage-test/configuration-unix.xml");
	private static final Level TRACELEVEL = Level.OFF;
	private static final boolean RETAIN_CASE_IS_FALSE = false;

	private static final String TEST_TEXT_0 = "Here's a sentence with activated in it.";
	private static final String TEST_TEXT_1 = "The Ki-Ras activated another entity.";
	private static final String TEST_TEXT_2 = "The Ki-Ras, which is a protein, activated another entity.";
	private static final String TEST_TEXT_3 = "The protein, which governs ki-ras, activated another entity.";
	private static final String TEST_TEXT_4 = "The Ki-Ras activated the brca1.";
	private static final String TEST_TEXT_5 = "The Ki-Ras activated the green Brca1.";
	private static final String TEST_TEXT_6 = "The Ki-Ras activated the brca1 protein IMP3.";
	private static final String TEST_TEXT_7 = "Ki-Ras, which governs ABC-2, activated the BRCA1 protein IMP3.";

	private static final String ACTIVATE_FRAME = "c-activate";
	private static final String ACTIVATOR_SLOT = "activator";
	private static final String ACTIVATED_ENTITY_SLOT = "activated-entity";

	// private static final String TEST_TEXT_7 =
	// "5-LOX and 5-LOX activating protein (FLAP) are coexpressed in lymphoid cells but not in monocytic or epithelial cells.";
	// private static final String TEST_TEXT_8 =
	// "Lymphocyte activation results in the upregulation of Fas expression and the acquisition of sensitivity to FasL-mediated apoptosis.";
	// private static final String TEST_TEXT_9 =
	// "IL-10 preincubation resulted in the inhibition of gene expression for several IFN-induced genes, such as IP-10, ISG54, and intercellular adhesion molecule-1.";

	/**
	 * Helper method that initializes a new parser using the configuration file noted above.
	 * 
	 * @param patterns
	 * @return
	 * @throws Exception
	 */
	private Parser initializeParser(Collection<String> patterns) throws Exception {
		StringBuffer sb = new StringBuffer();
		for (String pattern : patterns) {
			sb.append(pattern + "\n");
		}
		return ParserFactory.newParser(OPENDMAP_CONFIG_FILE, sb.toString(), TRACELEVEL);
	}

	/**
	 * Returns the frame reference strings output by the parser
	 * 
	 * @param parser
	 * @return
	 */
	private Set<String> getFrameReferenceStrings(Parser parser) {
		Set<String> frameReferenceStrings = new HashSet<String>();
		Collection<Reference> references = parser.getFrameReferences();
		for (Reference r : references) {
			frameReferenceStrings.add(r.getReferenceString());
		}
		return frameReferenceStrings;
	}

	/**
	 * Validates the expected slot fillers for the Activator Slot
	 * 
	 * @param parser
	 * @param expectedTextValues
	 */
	private void validateActivatorSlotValues(Parser parser, String... expectedTextValues) {
		validateSlotValues(parser, ACTIVATE_FRAME, ACTIVATOR_SLOT, expectedTextValues);
	}

	/**
	 * Validates the expected slot fillers for the Activator Slot
	 * 
	 * @param parser
	 * @param expectedTextValues
	 */
	private void validateActivatedEntitySlotValues(Parser parser, String... expectedTextValues) {
		validateSlotValues(parser, ACTIVATE_FRAME, ACTIVATED_ENTITY_SLOT, expectedTextValues);
	}

	/**
	 * Validates the expected slot fillers for a slot with the given name belonging to the given frame
	 * 
	 * @param parser
	 * @param frameName
	 * @param slotName
	 * @param expectedTextValues
	 */
	private void validateSlotValues(Parser parser, String frameName, String slotName, String... expectedTextValues) {
		Collection<String> slotValuesText = normalizeCase(getSlotValuesText(parser, frameName, slotName));
		if (expectedTextValues == null) {
			assertNull(slotValuesText);
		} else {
			Collection<String> expectedSlotValuesText = normalizeCase(Arrays.asList(expectedTextValues));
			assertNotNull(slotValuesText);
			assertEquals(expectedSlotValuesText.size(), slotValuesText.size());
			assertEquals(expectedSlotValuesText, slotValuesText);
		}
	}

	/**
	 * Returns a copy of the input collection with all Strings lowercase
	 * 
	 * @param inputCollection
	 * @return
	 */
	private Collection<String> normalizeCase(Collection<String> inputCollection) {
		if (inputCollection == null) {
			return null;
		}
		Collection<String> outputCollection = new ArrayList<String>();
		for (String s : inputCollection) {
			outputCollection.add(s.toLowerCase());
		}
		return outputCollection;
	}

	/**
	 * This method returns the slot values for the first c-activate frame it comes across in the parser output
	 * 
	 * @param parser
	 * @return
	 */
	private Collection<String> getSlotValuesText(Parser parser, String frameName, String slotName) {
		Collection<Reference> references = parser.getFrameReferences();
		for (Reference r : references) {
			if (r.getText().equals(frameName)) {
				return getSlotValues(r, slotName);
			}
		}
		return null;
	}

	/**
	 * Returns the covered text for the slot values held by the input reference and given slot name.
	 * 
	 * @param r
	 * @param slotName
	 * @return
	 */
	private Collection<String> getSlotValues(Reference r, String slotName) {
		Collection<Reference> slotValues = r.getSlotValues(slotName);
		if (slotValues == null) {
			return null;
		}
		Collection<String> slotValuesText = new ArrayList<String>();
		for (Reference slotReference : slotValues) {
			slotValuesText.add(getTokensText(slotReference.getTokens()));
		}
		return slotValuesText;
	}

	/**
	 * Returns a String composed of each token's text separated by a space
	 * 
	 * @param tokens
	 * @return
	 */
	private String getTokensText(Collection<DMAPToken> tokens) {
		StringBuffer aggregateTokenBuffer = new StringBuffer();
		for (DMAPToken token : tokens) {
			aggregateTokenBuffer.append(token.getItem().getText() + " ");
		}
		return aggregateTokenBuffer.toString().trim();
	}

	private void printReferences(Collection<Reference> references) {
		for (Reference r : references) {
			System.out.println("reference: " + r.getReferenceString());
		}
	}

	/*
	 * ------------------- Test Parse #0 -------------------
	 * 
	 * "Here's a sentence with activated in it."
	 * 
	 * The aim of this test is to verify that the parser works at the most basic of levels. Here we create a pattern
	 * that will recognize the word "activated" and another pattern that ties the {activate-keyword} frame to the
	 * {c-activate} frame.
	 */

	/**
	 * Returns the patterns to use for this test parse
	 */
	private Collection<String> getTestParse0Patterns() {
		Collection<String> patterns = new ArrayList<String>();
		patterns.add("{activation-keyword} := activated;");
		patterns.add("{c-activate} := {activation-keyword};");
		return patterns;
	}

	/**
	 * Returns the expected results (in the form of Reference Strings) for this test parse
	 */
	private Set<String> getExpectedReferenceStringsForTestParse0() {
		Set<String> expectedReferenceStrings = new HashSet<String>();
		expectedReferenceStrings.add("{activation-keyword}");
		expectedReferenceStrings.add("{c-activate}");
		return expectedReferenceStrings;
	}

	@Test
	public void testParse0() throws Exception {
		Parser parser = initializeParser(getTestParse0Patterns());
		parser.parse(TEST_TEXT_0);
		Set<String> frameReferenceStrings = getFrameReferenceStrings(parser);

		assertEquals(getExpectedReferenceStringsForTestParse0(), frameReferenceStrings);
		validateActivatedEntitySlotValues(parser, (String[]) null);
		validateActivatorSlotValues(parser, (String[]) null);
	}

	/*
	 * ------------------- Test Parse #0 Filling Trigger Slot Implicitly -------------------
	 * 
	 * "Here's a sentence with activated in it."
	 * 
	 * This test is a small extension of the TestParse0 test. We are essentially matching the same thing as above, but
	 * here we use the {activation-keyword} as a slot filler for the {c-activate} [trigger] slot. This test is called
	 * "implicitly" because the constraint for the [trigger] slot is defined implicitly based on the ontology (and not
	 * in the pattern, see below).
	 */

	/**
	 * Returns the patterns to use for this test parse
	 */
	private Collection<String> getTestParse0FillingTriggerSlotImplicitlyPatterns() {
		Collection<String> patterns = new ArrayList<String>();
		patterns.add("{activation-keyword} := activated;");
		patterns.add("{c-activate} := [trigger];");
		return patterns;
	}

	/**
	 * Returns the expected results (in the form of Reference Strings) for this test parse
	 */
	private Set<String> getExpectedReferenceStringsForTestParse0FillingTriggerSlot() {
		Set<String> expectedReferenceStrings = new HashSet<String>();
		expectedReferenceStrings.add("{activation-keyword}");
		expectedReferenceStrings.add("{c-activate [trigger]={activation-keyword}}");
		return expectedReferenceStrings;
	}

	@Test
	public void testParse0_fillingTriggerSlotImplicitly() throws Exception {
		Parser parser = initializeParser(getTestParse0FillingTriggerSlotImplicitlyPatterns());
		parser.parse(TEST_TEXT_0);
		Set<String> frameReferenceStrings = getFrameReferenceStrings(parser);

		assertEquals(getExpectedReferenceStringsForTestParse0FillingTriggerSlot(), frameReferenceStrings);
		validateActivatedEntitySlotValues(parser, (String[]) null);
		validateActivatorSlotValues(parser, (String[]) null);
	}

	/*
	 * ------------------- Test Parse #0 Filling Trigger Slot Explicitly -------------------
	 * 
	 * "Here's a sentence with activated in it."
	 * 
	 * This test is a small extension of the TestParse0 test. We are essentially matching the same thing as above, but
	 * here we use the {activation-keyword} as a slot filler for the {c-activate} [trigger] slot. This test is called
	 * "explicitly" because the constraint for the [trigger] slot is defined explicitly in the pattern. Note the
	 * difference between the pattern used here: {c-activate} := [trigger activation-keyword]; and the pattern used in
	 * the "implicit" case above: {c-activate} := [trigger];
	 * 
	 * TODO: Need more examples of these types of path constraints and reasons why they are useful.
	 */

	/**
	 * Returns the patterns to use for this test parse
	 */
	private Collection<String> getTestParse0FillingTriggerSlotExplicitlyPatterns() {
		Collection<String> patterns = new ArrayList<String>();
		patterns.add("{activation-keyword} := activated;");
		patterns.add("{c-activate} := [trigger activation-keyword];");
		return patterns;
	}

	@Test
	public void testParse0_fillingTriggerSlotExplicitly() throws Exception {
		Parser parser = initializeParser(getTestParse0FillingTriggerSlotExplicitlyPatterns());
		parser.parse(TEST_TEXT_0);
		Set<String> frameReferenceStrings = getFrameReferenceStrings(parser);

		assertEquals(getExpectedReferenceStringsForTestParse0FillingTriggerSlot(), frameReferenceStrings);
		validateActivatedEntitySlotValues(parser, (String[]) null);
		validateActivatorSlotValues(parser, (String[]) null);
	}

	/*
	 * ------------------- Test Parse #0 Filling Trigger Slot Explicitly With + Sign -------------------
	 * 
	 * "Here's a sentence with activated in it."
	 * 
	 * A simple variant on the test above (filling trigger slot explicitly), this test demonstrates that the use of the
	 * "+" sign in front of slot constraints is the default action when no sign is given (as above).
	 * 
	 * TODO: Need more examples of how signs are to be used.
	 */

	/**
	 * Returns the patterns to use for this test parse
	 */
	private Collection<String> getTestParse0FillingTriggerSlotExplicitlyWithPlusSignPatterns() {
		Collection<String> patterns = new ArrayList<String>();
		patterns.add("{activation-keyword} := activated;");
		patterns.add("{c-activate} := [trigger +activation-keyword];"); // note the "+" sign
		return patterns;
	}

	@Test
	public void testParse0_fillingTriggerSlotExplicitlyWithPlusSign() throws Exception {
		Parser parser = initializeParser(getTestParse0FillingTriggerSlotExplicitlyWithPlusSignPatterns());
		parser.parse(TEST_TEXT_0);
		Set<String> frameReferenceStrings = getFrameReferenceStrings(parser);

		assertEquals(getExpectedReferenceStringsForTestParse0FillingTriggerSlot(), frameReferenceStrings);
		validateActivatedEntitySlotValues(parser, (String[]) null);
		validateActivatorSlotValues(parser, (String[]) null);
	}

	/*
	 * ------------------- Test Parse #0 Filling Trigger Slot Explicitly With - Sign -------------------
	 * 
	 * "Here's a sentence with activated in it."
	 * 
	 * A simple variant on the test above (filling trigger slot explicitly), this test demonstrates the use of the "-"
	 * sign in front of slot constraints is the default action when no sign is given (as above). Note that in this case,
	 * use of the "-" sign causes {c-activate [trigger]={activation-keyword}} to NOT be matched.
	 * 
	 * TODO: Need more examples of how signs are to be used.
	 */

	/**
	 * Returns the patterns to use for this test parse
	 */
	private Collection<String> getTestParse0FillingTriggerSlotExplicitlyWithMinusSignPatterns() {
		Collection<String> patterns = new ArrayList<String>();
		patterns.add("{activation-keyword} := activated;");
		patterns.add("{c-activate} := [trigger -activation-keyword];"); // note the "-" sign
		return patterns;
	}

	/**
	 * Returns the expected results (in the form of Reference Strings) for this test parse
	 */
	private Set<String> getExpectedReferenceStringsForTestParse0FillingTriggerSlotExplicitlyWithMinusSign() {
		Set<String> expectedReferenceStrings = new HashSet<String>();
		expectedReferenceStrings.add("{activation-keyword}");
		return expectedReferenceStrings;
	}

	@Test
	public void testParse0_fillingTriggerSlotExplicitlyWithMinusSign() throws Exception {
		Parser parser = initializeParser(getTestParse0FillingTriggerSlotExplicitlyWithMinusSignPatterns());
		parser.parse(TEST_TEXT_0);
		Set<String> frameReferenceStrings = getFrameReferenceStrings(parser);

		assertEquals(getExpectedReferenceStringsForTestParse0FillingTriggerSlotExplicitlyWithMinusSign(), frameReferenceStrings);
		validateActivatedEntitySlotValues(parser, (String[]) null);
		validateActivatorSlotValues(parser, (String[]) null);
	}

	/*
	 * ------------------- Test Parse #1 -------------------
	 * 
	 * "The Ki-Ras activated another entity."
	 * 
	 * This test demonstrates the addition (and filling) of a second slot [activator].
	 */

	/**
	 * Returns the patterns to use for this test parse
	 */
	private Collection<String> getTestParse1Patterns() {
		Collection<String> patterns = new ArrayList<String>();
		patterns.add("{c-protein} := ki-ras;");
		patterns.add("{activation-keyword} := activated;");
		patterns.add("{c-activate} := [activator] [trigger activation-keyword];");
		return patterns;
	}

	/**
	 * Returns the expected results (in the form of Reference Strings) for this test parse
	 */
	private Set<String> getExpectedReferenceStringsForTestParse1() {
		Set<String> expectedReferenceStrings = new HashSet<String>();
		expectedReferenceStrings.add("{activation-keyword}");
		expectedReferenceStrings.add("{c-protein}");
		expectedReferenceStrings.add("{c-activate [activator]={c-protein} [trigger]={activation-keyword}}");
		return expectedReferenceStrings;
	}

	@Test
	public void testParse1() throws Exception {
		Parser parser = initializeParser(getTestParse1Patterns());
		parser.parse(TEST_TEXT_1);
		Set<String> frameReferenceStrings = getFrameReferenceStrings(parser);

		assertEquals(getExpectedReferenceStringsForTestParse1(), frameReferenceStrings);
		validateActivatedEntitySlotValues(parser, (String[]) null);
		validateActivatorSlotValues(parser, "Ki-Ras");
	}

	/*
	 * ------------------- Test Parse #1 - simple use of dependency information -------------------
	 * 
	 * "The Ki-Ras activated another entity."
	 * 
	 * This test demonstrates that the use of correct dependency information does not result in a bad parse. (It doesn't
	 * actually prove that the system is using the dependency information, that will come with a later test).
	 */

	/**
	 * Returns the patterns to use for this test parse
	 */
	private Collection<String> getTestParse1WithDependencyInfoPatterns() {
		Collection<String> patterns = new ArrayList<String>();
		patterns.add("{c-protein} := ki-ras;");
		patterns.add("{activation-keyword} := activated;");
		patterns.add("{c-activate} := [activator dep:x] [trigger activation-keyword head:x];");
		return patterns;
	}

	/**
	 * Returns the dependency relations to use for this test parse
	 */
	private Collection<DependencyRelation> getTestParse1DependencyRelations() {
		Collection<DependencyRelation> dependencyRelations = new ArrayList<DependencyRelation>();
		dependencyRelations.add(new DependencyRelation(DependencyRelation.DET, 1, "Ki-Ras", 0, "The"));
		dependencyRelations.add(new DependencyRelation(DependencyRelation.NSUBJ, 2, "activated", 1, "Ki-Ras"));
		dependencyRelations.add(new DependencyRelation(DependencyRelation.DOBJ, 2, "activated", 4, "entity"));
		dependencyRelations.add(new DependencyRelation(DependencyRelation.DET, 4, "entity", 3, "another"));
		return dependencyRelations;
	}

	/**
	 * Returns the expected results (in the form of Reference Strings) for this test parse
	 */
	private Set<String> getExpectedReferenceStringsForTestParse1WithDependencyRelations() {
		Set<String> expectedReferenceStrings = new HashSet<String>();
		expectedReferenceStrings.add("{activation-keyword}");
		expectedReferenceStrings.add("{c-protein}");
		expectedReferenceStrings.add("{c-activate [activator]={c-protein} [trigger]={activation-keyword}}");
		return expectedReferenceStrings;
	}

	@Test
	public void testParse1WithDependencyRelations() throws Exception {
		DMAPPropertyFunctionHandler dependencyGraphHandler = new DMAPDependencyGraphFunctionHandler(getTestParse1DependencyRelations());
		DMAPTokenizer dmapTokenizer = new DefaultTokenizer(TEST_TEXT_1, RETAIN_CASE_IS_FALSE);
		Parser parser = initializeParser(getTestParse1WithDependencyInfoPatterns());

		parser.parse(dmapTokenizer, dependencyGraphHandler);
		Set<String> frameReferenceStrings = getFrameReferenceStrings(parser);

		assertEquals(getExpectedReferenceStringsForTestParse1WithDependencyRelations(), frameReferenceStrings);
		validateActivatedEntitySlotValues(parser, (String[]) null);
		validateActivatorSlotValues(parser, "Ki-Ras");
	}

	/*
	 * ------------------- Test Parse #2 -------------------
	 * 
	 * "The Ki-Ras, which is a protein, activated another entity."
	 * 
	 * This test demonstrates a common problem often faced when constructing OpenDMAP patterns: accounting for
	 * intervening text. Here, we show how the simple pattern used previously is broken by the intervening text
	 * ", which is a protein,"
	 */

	/**
	 * Returns the patterns to use for this test parse
	 */
	private Collection<String> getTestParse2Patterns() {
		return getTestParse1Patterns();
	}

	/**
	 * Returns the expected results (in the form of Reference Strings) for this test parse
	 */
	private Set<String> getExpectedReferenceStringsForTestParse2() {
		Set<String> expectedReferenceStrings = new HashSet<String>();
		expectedReferenceStrings.add("{activation-keyword}");
		expectedReferenceStrings.add("{c-protein}");
		return expectedReferenceStrings;
	}

	@Test
	public void testParse2() throws Exception {
		Parser parser = initializeParser(getTestParse2Patterns());
		parser.parse(TEST_TEXT_2);
		Set<String> frameReferenceStrings = getFrameReferenceStrings(parser);

		assertEquals(getExpectedReferenceStringsForTestParse2(), frameReferenceStrings);
		validateActivatedEntitySlotValues(parser, (String[]) null);
		validateActivatorSlotValues(parser, (String[]) null);
	}

	/*
	 * ------------------- Test Parse #2 - using underscore to defeat intervening text -------------------
	 * 
	 * "The Ki-Ras, which is a protein, activated another entity."
	 * 
	 * This test demonstrates a common problem often faced when constructing OpenDMAP patterns: accounting for
	 * intervening text. Here, we show how the underscore pattern element can be used to ignore intervening text.
	 */

	/**
	 * Returns the patterns to use for this test parse
	 */
	private Collection<String> getTestParse2WithUnderscorePatterns() {
		Collection<String> patterns = new ArrayList<String>();
		patterns.add("{c-protein} := ki-ras;");
		patterns.add("{activation-keyword} := activated;");
		patterns.add("{c-activate} := [activator dep:x] _ [trigger activation-keyword head:x];");
		return patterns;
	}

	/**
	 * Returns the expected results (in the form of Reference Strings) for this test parse
	 */
	private Set<String> getExpectedReferenceStringsForTestParse2WithUnderScore() {
		Set<String> expectedReferenceStrings = new HashSet<String>();
		expectedReferenceStrings.add("{activation-keyword}");
		expectedReferenceStrings.add("{c-protein}");
		expectedReferenceStrings.add("{c-activate [activator]={c-protein} [trigger]={activation-keyword}}");
		return expectedReferenceStrings;
	}

	@Test
	public void testParse2WithUnderscore() throws Exception {
		Parser parser = initializeParser(getTestParse2WithUnderscorePatterns());
		parser.parse(TEST_TEXT_2);
		Set<String> frameReferenceStrings = getFrameReferenceStrings(parser);

		assertEquals(getExpectedReferenceStringsForTestParse2WithUnderScore(), frameReferenceStrings);
		validateActivatedEntitySlotValues(parser, (String[]) null);
		validateActivatorSlotValues(parser, "Ki-Ras");
	}

	/*
	 * ------------------- Test Parse #2 - using dependency information and underscore -------------------
	 * 
	 * "The Ki-Ras, which is a protein, activated another entity."
	 * 
	 * This test demonstrates that the use of correct dependency information does not result in a bad parse. (Again, it
	 * doesn't actually prove that the system is using the dependency information, that will come with a later test).
	 */

	/**
	 * Returns the patterns to use for this test parse
	 */
	private Collection<String> getTestParse2WithDependencyInfoPatterns() {
		Collection<String> patterns = new ArrayList<String>();
		patterns.add("{c-protein} := ki-ras;");
		patterns.add("{activation-keyword} := activated;");
		patterns.add("{c-activate} := [activator dep:x] _ [trigger activation-keyword head:x];");
		return patterns;
	}

	/**
	 * Returns the dependency relations to use for this test parse
	 * 
	 * <pre>
	 * 6 activated - nsubj (6,1)
	 *   1 Ki-Ras - det (1,0)
	 *     0 The - det (1,0)
	 *   1 Ki-Ras - nsubj (6,1)
	 *   1 Ki-Ras - rcmod (1,5)
	 *     5 protein - rcmod (1,5)
	 *     5 protein - nsubj (5,2)
	 *       2 which - nsubj (5,2)
	 *     5 protein - cop (5,3)
	 *       3 is - cop (5,3)
	 *     5 protein - det (5,4)
	 *       4 a - det (5,4)
	 * 6 activated - dobj (6,8)
	 *   8 entity - dobj (6,8)
	 *   8 entity - det (8,7)
	 *     7 another - det (8,7)
	 * </pre>
	 */
	private Collection<DependencyRelation> getTestParse2DependencyRelations() {
		Collection<DependencyRelation> dependencyRelations = new ArrayList<DependencyRelation>();
		dependencyRelations.add(new DependencyRelation(DependencyRelation.DET, 1, "Ki-Ras", 0, "The"));
		dependencyRelations.add(new DependencyRelation(DependencyRelation.NSUBJ, 6, "activated", 1, "Ki-Ras"));
		dependencyRelations.add(new DependencyRelation(DependencyRelation.RCMOD, 1, "Ki-Ras", 5, "protein"));
		dependencyRelations.add(new DependencyRelation(DependencyRelation.NSUBJ, 5, "protein", 2, "which"));
		dependencyRelations.add(new DependencyRelation(DependencyRelation.COP, 5, "protein", 3, "is"));
		dependencyRelations.add(new DependencyRelation(DependencyRelation.DET, 5, "protein", 4, "a"));
		dependencyRelations.add(new DependencyRelation(DependencyRelation.DOBJ, 6, "activated", 8, "entity"));
		dependencyRelations.add(new DependencyRelation(DependencyRelation.DET, 8, "entity", 7, "another"));
		return dependencyRelations;
	}

	/**
	 * Returns the expected results (in the form of Reference Strings) for this test parse
	 */
	private Set<String> getExpectedReferenceStringsForTestParse2WithDependencyRelations() {
		Set<String> expectedReferenceStrings = new HashSet<String>();
		expectedReferenceStrings.add("{activation-keyword}");
		expectedReferenceStrings.add("{c-protein}");
		expectedReferenceStrings.add("{c-activate [activator]={c-protein} [trigger]={activation-keyword}}");
		return expectedReferenceStrings;
	}

	@Test
	public void testParse2WithDependencyRelations() throws Exception {
		DMAPPropertyFunctionHandler dependencyGraphHandler = new DMAPDependencyGraphFunctionHandler(getTestParse2DependencyRelations());
		DMAPTokenizer dmapTokenizer = new DefaultTokenizer(TEST_TEXT_2, RETAIN_CASE_IS_FALSE);
		Parser parser = initializeParser(getTestParse2WithDependencyInfoPatterns());

		parser.parse(dmapTokenizer, dependencyGraphHandler);
		Set<String> frameReferenceStrings = getFrameReferenceStrings(parser);

		assertEquals(getExpectedReferenceStringsForTestParse2WithDependencyRelations(), frameReferenceStrings);
		validateActivatedEntitySlotValues(parser, (String[]) null);
		validateActivatorSlotValues(parser, "Ki-Ras");
	}

	/*
	 * ------------------- Test Parse #2 - using dependency information in pattern but not in parser -------------------
	 * 
	 * "The Ki-Ras, which is a protein, activated another entity."
	 * 
	 * This test interestingly demonstrates a potential bug. Note above where we showed that a slot constraint with no
	 * sign is given the "+" sign by default. This standard does not seem to apply to the functional constraints. In
	 * this example we show that the same patterns used above containing the single dependency relation also work when
	 * no dependency information is provided to the parser. This probably should not be the case.
	 */

	/**
	 * Returns the dependency relations to use for this test parse
	 */
	private Collection<DependencyRelation> getTestParse2DependencyRelationsReturnsEmpty() {
		Collection<DependencyRelation> dependencyRelations = new ArrayList<DependencyRelation>();
		return dependencyRelations;
	}

	@Test
	public void testParse2WithDependencyRelationsInPatternButNotInParser() throws Exception {
		DMAPPropertyFunctionHandler dependencyGraphHandler = new DMAPDependencyGraphFunctionHandler(
				getTestParse2DependencyRelationsReturnsEmpty());
		DMAPTokenizer dmapTokenizer = new DefaultTokenizer(TEST_TEXT_2, RETAIN_CASE_IS_FALSE);
		Parser parser = initializeParser(getTestParse2WithDependencyInfoPatterns());

		parser.parse(dmapTokenizer, dependencyGraphHandler);
		Set<String> frameReferenceStrings = getFrameReferenceStrings(parser);

		assertEquals(getExpectedReferenceStringsForTestParse2WithDependencyRelations(), frameReferenceStrings);
		validateActivatedEntitySlotValues(parser, (String[]) null);
		validateActivatorSlotValues(parser, "Ki-Ras");
	}

	/*
	 * ------------------- Test Parse #2 - using dependency information and "-" sign -------------------
	 * 
	 * "The Ki-Ras, which is a protein, activated another entity."
	 * 
	 * The two tests below demonstrates that the use of a "-" sign in front of the "dep:x" constraint results in a
	 * failed match for both cases when dependency information is given to the parser and when it is withheld from the
	 * parser. (The same thing happens if the "-" sign is placed in front of the "head:x" constraint, and in front of
	 * both constraints)
	 */

	/**
	 * Returns the patterns to use for this test parse
	 */
	private Collection<String> getTestParse2WithDependencyInfoWithMinusSignPatterns() {
		Collection<String> patterns = new ArrayList<String>();
		patterns.add("{c-protein} := ki-ras;");
		patterns.add("{activation-keyword} := activated;");
		patterns.add("{c-activate} := [activator -dep:x] _ [trigger activation-keyword head:x];");
		return patterns;
	}

	/**
	 * Returns the expected results (in the form of Reference Strings) for this test parse
	 */
	private Set<String> getExpectedReferenceStringsForTestParse2WithDependencyRelationsWithMinusSign() {
		Set<String> expectedReferenceStrings = new HashSet<String>();
		expectedReferenceStrings.add("{activation-keyword}");
		expectedReferenceStrings.add("{c-protein}");
		return expectedReferenceStrings;
	}

	@Test
	public void testParse2WithDependencyRelationsWithMinusSign() throws Exception {
		DMAPDependencyGraphFunctionHandler dependencyGraphHandler = new DMAPDependencyGraphFunctionHandler(
				getTestParse2DependencyRelations());
		DMAPTokenizer dmapTokenizer = new DefaultTokenizer(TEST_TEXT_2, RETAIN_CASE_IS_FALSE);
		Parser parser = initializeParser(getTestParse2WithDependencyInfoWithMinusSignPatterns());

		parser.parse(dmapTokenizer, dependencyGraphHandler);
		Set<String> frameReferenceStrings = getFrameReferenceStrings(parser);

		assertEquals(getExpectedReferenceStringsForTestParse2WithDependencyRelationsWithMinusSign(), frameReferenceStrings);
		validateActivatedEntitySlotValues(parser, (String[]) null);
		validateActivatorSlotValues(parser, (String[]) null);
	}

	@Test
	public void testParse2WithDependencyRelationsWithMinusSignAndWithheldDependencyInfo() throws Exception {
		DMAPDependencyGraphFunctionHandler dependencyGraphHandler = new DMAPDependencyGraphFunctionHandler(
				getTestParse2DependencyRelationsReturnsEmpty());

		DMAPTokenizer dmapTokenizer = new DefaultTokenizer(TEST_TEXT_2, RETAIN_CASE_IS_FALSE);
		Parser parser = initializeParser(getTestParse2WithDependencyInfoWithMinusSignPatterns());

		parser.parse(dmapTokenizer, dependencyGraphHandler);
		Set<String> frameReferenceStrings = getFrameReferenceStrings(parser);

		assertEquals(getExpectedReferenceStringsForTestParse2WithDependencyRelationsWithMinusSign(), frameReferenceStrings);
		validateActivatedEntitySlotValues(parser, (String[]) null);
		validateActivatorSlotValues(parser, (String[]) null);
	}

	/*
	 * ------------------- Test Parse #3 -------------------
	 * 
	 * "The protein, which governs ki-ras, activated another entity."
	 * 
	 * Another example where intervening text can be a problem is demonstrated in this test. Here we see that the simple
	 * pattern used to parse Sentence #1 matches part of Sentence #3, however this is an incorrect match.
	 */

	/**
	 * Returns the patterns to use for this test parse
	 */
	private Collection<String> getTestParse3Patterns() {
		return getTestParse1Patterns();
	}

	/**
	 * Returns the expected results (in the form of Reference Strings) for this test parse
	 */
	private Set<String> getExpectedReferenceStringsForTestParse3() {
		Set<String> expectedReferenceStrings = new HashSet<String>();
		expectedReferenceStrings.add("{activation-keyword}");
		expectedReferenceStrings.add("{c-protein}");
		// the following reference string is a false positive in this case
		expectedReferenceStrings.add("{c-activate [activator]={c-protein} [trigger]={activation-keyword}}");
		return expectedReferenceStrings;
	}

	@Test
	public void testParse3() throws Exception {
		Parser parser = initializeParser(getTestParse3Patterns());
		parser.parse(TEST_TEXT_3);
		Set<String> frameReferenceStrings = getFrameReferenceStrings(parser);

		assertEquals(getExpectedReferenceStringsForTestParse3(), frameReferenceStrings);
		validateActivatedEntitySlotValues(parser, (String[]) null);
		// again, we are asserting that this is an incorrect match just to make a point
		validateActivatorSlotValues(parser, "Ki-Ras");
	}

	/*
	 * ------------------- Test Parse #2 - using dependency information and underscore -------------------
	 * 
	 * "The protein, which governs ki-ras, activated another entity."
	 * 
	 * This test should demonstrate that the use of correct dependency information can result in the remediation of a
	 * bad parse.
	 */

	/**
	 * Returns the patterns to use for this test parse
	 */
	private Collection<String> getTestParse3WithDependencyInfoPatterns() {
		Collection<String> patterns = new ArrayList<String>();
		patterns.add("{c-protein} := ki-ras;");
		patterns.add("{activation-keyword} := activated;");
		patterns.add("{c-activate} := [activator dep:x] _ [trigger activation-keyword head:x];");
		return patterns;
	}

	/**
	 * Returns the dependency relations to use for this test parse
	 * 
	 * <pre>
	 * 5 activated - nsubj (5,1)
	 *   1 protein - det (1,0)
	 *     0 The - det (1,0)
	 *   1 protein - nsubj (5,1)
	 *   1 protein - rcmod (1,3)
	 *     3 governs - rcmod (1,3)
	 *     3 governs - nsubj (3,2)
	 *       2 which - nsubj (3,2)
	 *     3 goverms - dobj (3,4)
	 *       4 Ki-Ras - dobj (3,4)
	 * 5 activated - dobj (5,7)
	 *   7 entity - dobj (5,7)
	 *   7 entity - det (7,6)
	 *     6 another - det (7,6)
	 * </pre>
	 */
	private Collection<DependencyRelation> getTestParse3DependencyRelations() {
		Collection<DependencyRelation> dependencyRelations = new ArrayList<DependencyRelation>();
		dependencyRelations.add(new DependencyRelation(DependencyRelation.DET, 1, "protein", 0, "The"));
		dependencyRelations.add(new DependencyRelation(DependencyRelation.NSUBJ, 5, "activated", 1, "protein"));
		dependencyRelations.add(new DependencyRelation(DependencyRelation.RCMOD, 1, "protein", 3, "governs"));
		dependencyRelations.add(new DependencyRelation(DependencyRelation.NSUBJ, 3, "governs", 2, "which"));
		dependencyRelations.add(new DependencyRelation(DependencyRelation.DOBJ, 3, "goverms", 4, "Ki-Ras"));
		dependencyRelations.add(new DependencyRelation(DependencyRelation.DOBJ, 5, "activated", 7, "entity"));
		dependencyRelations.add(new DependencyRelation(DependencyRelation.DET, 7, "entity", 6, "another"));
		return dependencyRelations;
	}

	/**
	 * Returns the expected results (in the form of Reference Strings) for this test parse
	 */
	private Set<String> getExpectedReferenceStringsForTestParse3WithDependencyRelations() {
		Set<String> expectedReferenceStrings = new HashSet<String>();
		expectedReferenceStrings.add("{activation-keyword}");
		expectedReferenceStrings.add("{c-protein}");
		// expectedReferenceStrings.add("{c-activate [activator]={c-protein} [trigger]={activation-keyword}}");
		return expectedReferenceStrings;
	}

	@Test
	public void testParse3WithDependencyRelations() throws Exception {
		DMAPDependencyGraphFunctionHandler dependencyGraphHandler = new DMAPDependencyGraphFunctionHandler(
				getTestParse3DependencyRelations());
		DMAPTokenizer dmapTokenizer = new DefaultTokenizer(TEST_TEXT_3, RETAIN_CASE_IS_FALSE);
		Parser parser = initializeParser(getTestParse3WithDependencyInfoPatterns());

		parser.parse(dmapTokenizer, dependencyGraphHandler);
		Set<String> frameReferenceStrings = getFrameReferenceStrings(parser);

		assertEquals(getExpectedReferenceStringsForTestParse3WithDependencyRelations(), frameReferenceStrings);
		validateActivatedEntitySlotValues(parser, (String[]) null);
		validateActivatorSlotValues(parser, (String[]) null);
	}

	/*
	 * ------------------- Test Parse #4 -------------------
	 * 
	 * "The Ki-Ras activated the brca1."
	 * 
	 * Adding the activated-entity slot.
	 */

	/**
	 * Returns the patterns to use for this test parse
	 */
	private Collection<String> getTestParse4WithDependencyInfoPatterns() {
		Collection<String> patterns = new ArrayList<String>();
		patterns.add("{c-protein} := ki-ras;");
		patterns.add("{c-protein} := brca1;");
		patterns.add("{activation-keyword} := activated;");
		patterns.add("{c-activate} := [activator] [trigger activation-keyword head:x] _ [activated-entity dep:x];");
		return patterns;
	}

	/**
	 * Returns the dependency relations to use for this test parse
	 */
	private Collection<DependencyRelation> getTestParse4DependencyRelations() {
		Collection<DependencyRelation> dependencyRelations = new ArrayList<DependencyRelation>();
		dependencyRelations.add(new DependencyRelation(DependencyRelation.DET, 1, "Ki-Ras", 0, "The"));
		dependencyRelations.add(new DependencyRelation(DependencyRelation.NSUBJ, 2, "activated", 1, "Ki-Ras"));
		dependencyRelations.add(new DependencyRelation(DependencyRelation.DOBJ, 2, "activated", 4, "brca1"));
		dependencyRelations.add(new DependencyRelation(DependencyRelation.DET, 4, "brca1", 3, "the"));
		return dependencyRelations;
	}

	/**
	 * Returns the expected results (in the form of Reference Strings) for this test parse
	 */
	private Set<String> getExpectedReferenceStringsForTestParse4WithDependencyRelations() {
		Set<String> expectedReferenceStrings = new HashSet<String>();
		expectedReferenceStrings.add("{activation-keyword}");
		expectedReferenceStrings.add("{c-protein}");
		expectedReferenceStrings.add("{c-activate [activator]={c-protein} [trigger]={activation-keyword} [activated-entity]={c-protein}}");
		return expectedReferenceStrings;
	}

	@Test
	public void testParse4WithDependencyRelations() throws Exception {
		DMAPPropertyFunctionHandler dependencyGraphHandler = new DMAPDependencyGraphFunctionHandler(getTestParse4DependencyRelations());
		DMAPTokenizer dmapTokenizer = new DefaultTokenizer(TEST_TEXT_4, RETAIN_CASE_IS_FALSE);
		Parser parser = initializeParser(getTestParse4WithDependencyInfoPatterns());

		parser.parse(dmapTokenizer, dependencyGraphHandler);
		Set<String> frameReferenceStrings = getFrameReferenceStrings(parser);

		assertEquals(getExpectedReferenceStringsForTestParse4WithDependencyRelations(), frameReferenceStrings);
		validateActivatedEntitySlotValues(parser, "BRCA1");
		validateActivatorSlotValues(parser, "Ki-Ras");
	}

	/*
	 * ------------------- Test Parse #5 -------------------
	 * 
	 * "The Ki-Ras activated the green brca1."
	 * 
	 * Adding the activated-entity slot with some extra intervening text: "green".
	 */

	/**
	 * Returns the patterns to use for this test parse
	 */
	private Collection<String> getTestParse5WithDependencyInfoPatterns() {
		return getTestParse4WithDependencyInfoPatterns();
	}

	/**
	 * Returns the dependency relations to use for this test parse
	 */
	private Collection<DependencyRelation> getTestParse5DependencyRelations() {
		Collection<DependencyRelation> dependencyRelations = new ArrayList<DependencyRelation>();
		dependencyRelations.add(new DependencyRelation(DependencyRelation.DET, 1, "Ki-Ras", 0, "The"));
		dependencyRelations.add(new DependencyRelation(DependencyRelation.NSUBJ, 2, "activated", 1, "Ki-Ras"));
		dependencyRelations.add(new DependencyRelation(DependencyRelation.DOBJ, 2, "activated", 5, "brca1"));
		dependencyRelations.add(new DependencyRelation(DependencyRelation.DET, 5, "brca1", 3, "the"));
		dependencyRelations.add(new DependencyRelation(DependencyRelation.AMOD, 5, "brca1", 4, "green"));
		return dependencyRelations;
	}

	/**
	 * Returns the expected results (in the form of Reference Strings) for this test parse
	 */
	private Set<String> getExpectedReferenceStringsForTestParse5WithDependencyRelations() {
		return getExpectedReferenceStringsForTestParse4WithDependencyRelations();
	}

	@Test
	public void testParse5WithDependencyRelations() throws Exception {
		DMAPPropertyFunctionHandler dependencyGraphHandler = new DMAPDependencyGraphFunctionHandler(getTestParse5DependencyRelations());
		DMAPTokenizer dmapTokenizer = new DefaultTokenizer(TEST_TEXT_5, RETAIN_CASE_IS_FALSE);
		Parser parser = initializeParser(getTestParse5WithDependencyInfoPatterns());

		parser.parse(dmapTokenizer, dependencyGraphHandler);
		Set<String> frameReferenceStrings = getFrameReferenceStrings(parser);

		assertEquals(getExpectedReferenceStringsForTestParse5WithDependencyRelations(), frameReferenceStrings);
		validateActivatedEntitySlotValues(parser, "BRCA1");
		validateActivatorSlotValues(parser, "Ki-Ras");
	}

	/*
	 * ------------------- Test Parse #6 -------------------
	 * 
	 * "The Ki-Ras activated the brca1 protein IMP3."
	 * 
	 * Adding the activated-entity slot and using dependency information to ensure the proper protein is extracted as
	 * the activated-entity.
	 */

	/**
	 * Returns the patterns to use for this test parse
	 */
	private Collection<String> getTestParse6WithDependencyInfoPatterns() {
		Collection<String> patterns = new ArrayList<String>();
		patterns.add("{c-protein} := ki-ras;");
		patterns.add("{c-protein} := brca1;");
		patterns.add("{c-protein} := IMP3;");
		patterns.add("{activation-keyword} := activated;");
		patterns.add("{c-activate} := [activator] [trigger activation-keyword head:x] _ [activated-entity dep:x];");
		return patterns;
	}

	/**
	 * Returns the dependency relations to use for this test parse
	 */
	private Collection<DependencyRelation> getTestParse6DependencyRelations() {
		Collection<DependencyRelation> dependencyRelations = new ArrayList<DependencyRelation>();
		dependencyRelations.add(new DependencyRelation(DependencyRelation.DET, 1, "Ki-Ras", 0, "The"));
		dependencyRelations.add(new DependencyRelation(DependencyRelation.NSUBJ, 2, "activated", 1, "Ki-Ras"));
		dependencyRelations.add(new DependencyRelation(DependencyRelation.DOBJ, 2, "activated", 6, "IMP3"));
		dependencyRelations.add(new DependencyRelation(DependencyRelation.DET, 6, "IMP3", 3, "the"));
		dependencyRelations.add(new DependencyRelation(DependencyRelation.NN, 6, "IMP3", 4, "brca1"));
		dependencyRelations.add(new DependencyRelation(DependencyRelation.NN, 6, "IMP3", 5, "protein"));
		return dependencyRelations;
	}

	/**
	 * Returns the expected results (in the form of Reference Strings) for this test parse
	 */
	private Set<String> getExpectedReferenceStringsForTestParse6WithDependencyRelations() {
		return getExpectedReferenceStringsForTestParse4WithDependencyRelations();
	}

	@Test
	public void testParse6WithDependencyRelations() throws Exception {
		DMAPPropertyFunctionHandler dependencyGraphHandler = new DMAPDependencyGraphFunctionHandler(getTestParse6DependencyRelations());
		DMAPTokenizer dmapTokenizer = new DefaultTokenizer(TEST_TEXT_6, RETAIN_CASE_IS_FALSE);
		Parser parser = initializeParser(getTestParse6WithDependencyInfoPatterns());

		parser.parse(dmapTokenizer, dependencyGraphHandler);
		Set<String> frameReferenceStrings = getFrameReferenceStrings(parser);

		assertEquals(getExpectedReferenceStringsForTestParse6WithDependencyRelations(), frameReferenceStrings);
		validateActivatedEntitySlotValues(parser, "IMP3");
		validateActivatorSlotValues(parser, "Ki-Ras");
	}

	/*
	 * ------------------- Test Parse #7 - getting fancy with dependency relations -------------------
	 * 
	 * "Ki-Ras, which governs ABC-2, activated the BRCA1 protein IMP3."
	 * 
	 * This test should demonstrate that the use of correct dependency information in conjunction with the underscore
	 * can resolve a somewhat difficult sentence. Here we introduce two dependency constraints (x and y) to get the
	 * correct parse.
	 */

	/**
	 * Returns the patterns to use for this test parse
	 */
	private Collection<String> getTestParse7WithDependencyInfoPatterns() {
		Collection<String> patterns = new ArrayList<String>();
		patterns.add("{c-protein} := ki-ras;");
		patterns.add("{c-protein} := ABC-2;");
		patterns.add("{c-protein} := BRCA1;");
		patterns.add("{c-protein} := IMP3;");
		patterns.add("{activation-keyword} := activated;");
		patterns.add("{c-activate} := [activator dep:x] _ [trigger activation-keyword head:x head:y] _ [activated-entity dep:y];");
		return patterns;
	}

	/**
	 * Returns the dependency relations to use for this test parse
	 * 
	 * <pre>
	 * 5 activated - nsubj (5,1)
	 *   1 protein - det (1,0)
	 *     0 The - det (1,0)
	 *   1 protein - nsubj (5,1)
	 *   1 protein - rcmod (1,3)
	 *     3 governs - rcmod (1,3)
	 *     3 governs - nsubj (3,2)
	 *       2 which - nsubj (3,2)
	 *     3 goverms - dobj (3,4)
	 *       4 Ki-Ras - dobj (3,4)
	 * 5 activated - dobj (5,7)
	 *   7 entity - dobj (5,7)
	 *   7 entity - det (7,6)
	 *     6 another - det (7,6)
	 * </pre>
	 */
	private Collection<DependencyRelation> getTestParse7DependencyRelations() {
		Collection<DependencyRelation> dependencyRelations = new ArrayList<DependencyRelation>();
		dependencyRelations.add(new DependencyRelation(DependencyRelation.NSUBJ, 4, "activated", 0, "KiRas"));
		dependencyRelations.add(new DependencyRelation(DependencyRelation.RCMOD, 0, "Ki-Ras", 2, "governs"));
		dependencyRelations.add(new DependencyRelation(DependencyRelation.NSUBJ, 2, "governs", 1, "which"));
		dependencyRelations.add(new DependencyRelation(DependencyRelation.DOBJ, 2, "governs", 3, "ABC-2"));
		dependencyRelations.add(new DependencyRelation(DependencyRelation.DOBJ, 4, "activated", 8, "IMP3"));
		dependencyRelations.add(new DependencyRelation(DependencyRelation.DET, 8, "IMP3", 5, "the"));
		dependencyRelations.add(new DependencyRelation(DependencyRelation.NN, 8, "IMP3", 6, "BRCA1"));
		dependencyRelations.add(new DependencyRelation(DependencyRelation.NN, 8, "IMP3", 7, "protein"));
		return dependencyRelations;
	}

	/**
	 * Returns the expected results (in the form of Reference Strings) for this test parse
	 */
	private Set<String> getExpectedReferenceStringsForTestParse7WithDependencyRelations() {
		Set<String> expectedReferenceStrings = new HashSet<String>();
		expectedReferenceStrings.add("{activation-keyword}");
		expectedReferenceStrings.add("{c-protein}");
		expectedReferenceStrings.add("{c-activate [activator]={c-protein} [trigger]={activation-keyword} [activated-entity]={c-protein}}");
		return expectedReferenceStrings;
	}

	@Test
	public void testParse7WithDependencyRelations() throws Exception {
		DMAPDependencyGraphFunctionHandler dependencyGraphHandler = new DMAPDependencyGraphFunctionHandler(
				getTestParse7DependencyRelations());
		DMAPTokenizer dmapTokenizer = new DefaultTokenizer(TEST_TEXT_7, RETAIN_CASE_IS_FALSE);
		Parser parser = initializeParser(getTestParse7WithDependencyInfoPatterns());

		parser.parse(dmapTokenizer, dependencyGraphHandler);
		Set<String> frameReferenceStrings = getFrameReferenceStrings(parser);

		assertEquals(getExpectedReferenceStringsForTestParse7WithDependencyRelations(), frameReferenceStrings);
		validateActivatedEntitySlotValues(parser, "IMP3");
		validateActivatorSlotValues(parser, "Ki-Ras");
	}

	/*
	 * ------------------- Test Parse #7 - getting fancy with dependency relations -------------------
	 * 
	 * "Ki-Ras, which governs ABC-2, activated the BRCA1 protein IMP3."
	 * 
	 * A different take.. note the slight change in pattern constraints, (dep:x head:x dep:x) instead of (dep:x head:x head:y dep:y)
	 */

	/**
	 * Returns the patterns to use for this test parse
	 */
	private Collection<String> getTestParse7WithDependencyInfoPatternsMinorVariation() {
		Collection<String> patterns = new ArrayList<String>();
		patterns.add("{c-protein} := ki-ras;");
		patterns.add("{c-protein} := ABC-2;");
		patterns.add("{c-protein} := BRCA1;");
		patterns.add("{c-protein} := IMP3;");
		patterns.add("{activation-keyword} := activated;");
		patterns.add("{c-activate} := [activator dep:x] _ [trigger activation-keyword head:x] _ [activated-entity dep:x];");
		return patterns;
	}

	@Test
	public void testParse7WithDependencyRelationsMinorVariation() throws Exception {
		DMAPDependencyGraphFunctionHandler dependencyGraphHandler = new DMAPDependencyGraphFunctionHandler(
				getTestParse7DependencyRelations());
		DMAPTokenizer dmapTokenizer = new DefaultTokenizer(TEST_TEXT_7, RETAIN_CASE_IS_FALSE);
		Parser parser = initializeParser(getTestParse7WithDependencyInfoPatternsMinorVariation());

		parser.parse(dmapTokenizer, dependencyGraphHandler);
		Set<String> frameReferenceStrings = getFrameReferenceStrings(parser);

		assertEquals(getExpectedReferenceStringsForTestParse7WithDependencyRelations(), frameReferenceStrings);
		validateActivatedEntitySlotValues(parser, "IMP3");
		validateActivatorSlotValues(parser, "Ki-Ras");
	}
	
}
