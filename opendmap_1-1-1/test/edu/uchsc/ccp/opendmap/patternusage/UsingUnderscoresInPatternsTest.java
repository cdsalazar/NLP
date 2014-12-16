package edu.uchsc.ccp.opendmap.patternusage;

import java.util.Set;

import org.junit.Test;

public class UsingUnderscoresInPatternsTest extends PatternUsageTester {
	private static final String TEST_TEXT_2 = "The Ki-Ras, which is a protein, activated another entity.";

	/*
	 * --------------- Test Parse #2 - using underscore to defeat intervening text ---------------
	 * 
	 * "The Ki-Ras, which is a protein, activated another entity."
	 * 
	 * This test demonstrates a common problem often faced when constructing OpenDMAP patterns:
	 * accounting for intervening text. Here, we show how the underscore pattern element can be used
	 * to ignore intervening text.
	 */

	@Test
	public void testParse_simpleMatch() throws Exception {
		Set<String> patterns = createSet("{c-protein} := ki-ras;", "{activation-keyword} := activated;",
				"{c-activate} := [activator dep:x] _ [trigger activation-keyword head:x];");

		Set<String> expectedReferenceStrs = createSet("{activation-keyword}", "{c-protein}",
				"{c-activate [activator]={c-protein} [trigger]={activation-keyword}}");

		testParse(TEST_TEXT_2, patterns, expectedReferenceStrs, new ActivatedEntitySlotValidator((String[]) null),
				new ActivatorSlotValidator("Ki-Ras"));
	}

}
