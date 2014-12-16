package edu.uchsc.ccp.opendmap.patternusage;

import java.util.Set;

import org.junit.Test;

public class UsingSlotPathsInPatternsTest extends PatternUsageTester {

	private static final String TEST_TEXT_0 = "Here's a sentence with activated in it.";

	/*
	 * ------------------- Test Parse #0 -------------------
	 * 
	 * "Here's a sentence with activated in it."
	 * 
	 * The aim of this test is to verify that the parser works at the most basic of levels. Here we
	 * create a pattern that will recognize the word "activated" and another pattern that ties the
	 * {activate-keyword} frame to the {c-activate} frame.
	 */

	@Test
	public void testParse_simpleMatch() throws Exception {
		Set<String> patterns = createSet("{activation-keyword} := activated;", "{c-activate} := {activation-keyword};");

		Set<String> expectedReferenceStrs = createSet("{activation-keyword}", "{c-activate}");

		testParse(TEST_TEXT_0, patterns, expectedReferenceStrs, new ActivatedEntitySlotValidator((String[]) null),
				new ActivatorSlotValidator((String[]) null));
	}

	/*
	 * ------------------- Test Parse #0 Filling Trigger Slot Implicitly -------------------
	 * 
	 * "Here's a sentence with activated in it."
	 * 
	 * This test is a small extension of the TestParse0 test. We are essentially matching the same
	 * thing as above, but here we use the {activation-keyword} as a slot filler for the
	 * {c-activate} [trigger] slot. This test is called "implicitly" because the constraint for the
	 * [trigger] slot is defined implicitly based on the ontology (and not in the pattern, see
	 * below).
	 */

	@Test
	public void testParse_simpleMatchWithSlotFillerConstrainedImplicitlyViaOntology() throws Exception {
		Set<String> patterns = createSet("{activation-keyword} := activated;", "{c-activate} := [trigger];");

		Set<String> expectedReferenceStrs = createSet("{activation-keyword}",
				"{c-activate [trigger]={activation-keyword}}");

		testParse(TEST_TEXT_0, patterns, expectedReferenceStrs, new ActivatedEntitySlotValidator((String[]) null),
				new ActivatorSlotValidator((String[]) null));
	}

	/*
	 * ------------------- Test Parse #0 Filling Trigger Slot Explicitly -------------------
	 * 
	 * "Here's a sentence with activated in it."
	 * 
	 * This test is a small extension of the TestParse0 test. We are essentially matching the same
	 * thing as above, but here we use the {activation-keyword} as a slot filler for the
	 * {c-activate} [trigger] slot. This test is called "explicitly" because the constraint for the
	 * [trigger] slot is defined explicitly in the pattern. Note the difference between the pattern
	 * used here: {c-activate} := [trigger activation-keyword]; and the pattern used in the
	 * "implicit" case above: {c-activate} := [trigger];
	 * 
	 * TODO: Need more examples of these types of path constraints and reasons why they are useful.
	 */

	@Test
	public void testParse_simpleMatchWithSlotFillerConstrainedExplicitly() throws Exception {
		Set<String> patterns = createSet("{activation-keyword} := activated;",
				"{c-activate} := [trigger activation-keyword];");

		Set<String> expectedReferenceStrs = createSet("{activation-keyword}",
				"{c-activate [trigger]={activation-keyword}}");

		testParse(TEST_TEXT_0, patterns, expectedReferenceStrs, new ActivatedEntitySlotValidator((String[]) null),
				new ActivatorSlotValidator((String[]) null));
	}

	/*
	 * ------------------- Test Parse #0 Filling Trigger Slot Explicitly With + Sign
	 * -------------------
	 * 
	 * "Here's a sentence with activated in it."
	 * 
	 * A simple variant on the test above (filling trigger slot explicitly), this test demonstrates
	 * that the use of the "+" sign in front of slot constraints is the default action when no sign
	 * is given (as above).
	 * 
	 * TODO: Need more examples of how signs are to be used.
	 */

	@Test
	public void testParse_simpleMatchWithSlotFillerConstrainedExplicitlyWithPlusSign() throws Exception {
		Set<String> patterns = createSet("{activation-keyword} := activated;",
				"{c-activate} := [trigger +activation-keyword];");

		Set<String> expectedReferenceStrs = createSet("{activation-keyword}",
				"{c-activate [trigger]={activation-keyword}}");

		testParse(TEST_TEXT_0, patterns, expectedReferenceStrs, new ActivatedEntitySlotValidator((String[]) null),
				new ActivatorSlotValidator((String[]) null));
	}

	/*
	 * ------------------- Test Parse #0 Filling Trigger Slot Explicitly With - Sign
	 * -------------------
	 * 
	 * "Here's a sentence with activated in it."
	 * 
	 * A simple variant on the test above (filling trigger slot explicitly), this test demonstrates
	 * the use of the "-" sign in front of slot constraints is the default action when no sign is
	 * given (as above). Note that in this case, use of the "-" sign causes {c-activate
	 * [trigger]={activation-keyword}} to NOT be matched.
	 * 
	 * TODO: Need more examples of how signs are to be used.
	 */

	@Test
	public void testParse_simpleMatchWithSlotFillerConstrainedExplicitlyWithMinusSign() throws Exception {
		Set<String> patterns = createSet("{activation-keyword} := activated;",
				"{c-activate} := [trigger -activation-keyword];");

		Set<String> expectedReferenceStrs = createSet("{activation-keyword}");

		testParse(TEST_TEXT_0, patterns, expectedReferenceStrs, new ActivatedEntitySlotValidator((String[]) null),
				new ActivatorSlotValidator((String[]) null));
	}

}
