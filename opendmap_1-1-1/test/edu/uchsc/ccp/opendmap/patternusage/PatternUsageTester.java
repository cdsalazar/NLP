package edu.uchsc.ccp.opendmap.patternusage;

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

import edu.uchsc.ccp.opendmap.DMAPPropertyFunctionHandler;
import edu.uchsc.ccp.opendmap.DMAPToken;
import edu.uchsc.ccp.opendmap.DMAPTokenizer;
import edu.uchsc.ccp.opendmap.DefaultTokenizer;
import edu.uchsc.ccp.opendmap.Parser;
import edu.uchsc.ccp.opendmap.Reference;
import edu.uchsc.ccp.opendmap.configuration.ParserFactory;
import edu.uchsc.ccp.opendmap.dependency.DMAPDependencyGraphFunctionHandler;
import edu.uchsc.ccp.opendmap.dependency.DependencyRelation;

/**
 * This abstract test class is used primarily to set up OpenDMAP pattern syntax tests to ensure they
 * work as expected. Test classes that extend this class should be written as live-documentation for
 * how to write OpenDMAP patterns. <br>
 * <br>
 * Many of the tests used by subclasses of this class deal with the "activate" frame. This frame has
 * two slots:
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
public abstract class PatternUsageTester {

	/*
	 * This configuration file points to the dmap-test-ontology, but does not include any pattern
	 * files. All patterns must be added manually for these unit tests.
	 */
	private static final File OPENDMAP_CONFIG_FILE = new File(
			"data/test/edu.uchsc.ccp.opendmap.pattern-usage-test/configuration-unix.xml");
	private static final Level TRACELEVEL = Level.OFF;
	private static final boolean RETAIN_CASE = false;

	private static final String ACTIVATE_FRAME = "c-activate";
	private static final String ACTIVATOR_SLOT = "activator";
	private static final String ACTIVATED_ENTITY_SLOT = "activated-entity";

	/**
	 * This method parses the input text using DMAP with the patterns provided. DMAP matches are
	 * compared to the expected reference strings. Slot fillers are validated with the input
	 * SlotValidator objects.
	 * 
	 * @param textToParse
	 * @param patterns
	 * @param expectedReferenceStrs
	 * @param slotValidators
	 * @throws Exception
	 */
	protected void testParse(String textToParse, Set<String> patterns, Set<String> expectedReferenceStrs,
			SlotValidator... slotValidators) throws Exception {
		Parser parser = initializeParser(patterns);
		parser.parse(textToParse);
		checkOutput(parser, expectedReferenceStrs, slotValidators);
	}

	/**
	 * This method parses the input text using DMAP with the patterns provided. DMAP matches are
	 * compared to the expected reference strings. Slot fillers are validated with the input
	 * SlotValidator objects. This method takes advantage of provided dependency relation
	 * information. The DefaultDMAPTokenizer is used to create tokens.
	 * 
	 * @param textToParse
	 * @param patterns
	 * @param expectedReferenceStrs
	 * @param dependencyRelations
	 * @param slotValidators
	 * @throws Exception
	 */
	protected void testParseUsingDependencyRelations(String textToParse, Set<String> patterns,
			Set<String> expectedReferenceStrs, Collection<DependencyRelation> dependencyRelations,
			SlotValidator... slotValidators) throws Exception {
		Parser parser = initializeParser(patterns);

		DMAPTokenizer dmapTokenizer = new DefaultTokenizer(textToParse, RETAIN_CASE);
		DMAPPropertyFunctionHandler dependencyGraphHandler = new DMAPDependencyGraphFunctionHandler(dependencyRelations);
		parser.parse(dmapTokenizer, dependencyGraphHandler);
		checkOutput(parser, expectedReferenceStrs, slotValidators);
	}

	/**
	 * This helper method compares the parser results to the expected reference strings and
	 * validates the slot fillers
	 * 
	 * @param parser
	 * @param expectedReferenceStrs
	 * @param slotValidators
	 */
	private void checkOutput(Parser parser, Set<String> expectedReferenceStrs, SlotValidator... slotValidators) {
		assertEquals(expectedReferenceStrs, getFrameReferenceStrings(parser));
		for (SlotValidator slotValidator : slotValidators) {
			slotValidator.validateSlotValues(parser);
		}
	}

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
	 * This method returns the slot values for the first c-activate frame it comes across in the
	 * parser output
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

	/**
	 * An extension of the SlotValidator class specifically for the Activate frame activated-entity
	 * slot
	 * 
	 * @author Bill Baumgartner
	 * 
	 */
	protected class ActivatedEntitySlotValidator extends SlotValidator {

		public ActivatedEntitySlotValidator(String... expectedSlotValuesAsStrings) {
			super(ACTIVATE_FRAME, ACTIVATED_ENTITY_SLOT, expectedSlotValuesAsStrings);
		}

	}

	/**
	 * An extension of the SlotValidator class specifically for the Activate frame activator slot
	 * 
	 * @author Bill Baumgartner
	 * 
	 */
	protected class ActivatorSlotValidator extends SlotValidator {

		public ActivatorSlotValidator(String... expectedSlotValuesAsStrings) {
			super(ACTIVATE_FRAME, ACTIVATOR_SLOT, expectedSlotValuesAsStrings);
		}

	}

	/**
	 * A utility class for validating slot fillers
	 * 
	 * @author Bill Baumgartner
	 * 
	 */
	protected class SlotValidator {

		private String frameName;
		private String slotName;
		private String[] expectedSlotValuesAsStrings;

		public SlotValidator(String frameName, String slotName, String... expectedSlotValuesAsStrings) {
			this.frameName = frameName;
			this.slotName = slotName;
			this.expectedSlotValuesAsStrings = expectedSlotValuesAsStrings;
		}

		public void validateSlotValues(Parser parser) {
			validateSlotValues(parser, frameName, slotName, expectedSlotValuesAsStrings);
		}

		/**
		 * Validates the expected slot fillers for a slot with the given name belonging to the given
		 * frame
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
	}

	protected <E extends Object> Set<E> createSet(E... objects) {
		Set<E> set = new HashSet<E>();
		for (E obj : objects) {
			set.add(obj);
		}
		return set;
	}

}
