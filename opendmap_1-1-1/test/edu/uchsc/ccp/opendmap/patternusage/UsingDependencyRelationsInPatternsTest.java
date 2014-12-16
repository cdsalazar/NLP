package edu.uchsc.ccp.opendmap.patternusage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.junit.Test;

import edu.uchsc.ccp.opendmap.dependency.DependencyRelation;

public class UsingDependencyRelationsInPatternsTest extends PatternUsageTester {
	private static final String TEST_TEXT_1 = "The Ki-Ras activated another entity.";
	private static final String TEST_TEXT_2 = "The Ki-Ras, which is a protein, activated another entity.";
	private static final String TEST_TEXT_3 = "The protein, which governs ki-ras, activated another entity.";
	private static final String TEST_TEXT_4 = "The Ki-Ras activated the brca1.";
	private static final String TEST_TEXT_5 = "The Ki-Ras activated the green Brca1.";
	private static final String TEST_TEXT_6 = "The Ki-Ras activated the brca1 protein IMP3.";
	private static final String TEST_TEXT_7 = "Ki-Ras, which governs ABC-2, activated the BRCA1 protein IMP3.";

	/**
	 * Returns the dependency relations to use for this test parse
	 */
	private Collection<DependencyRelation> getDependencyRelationsForTestText1() {
		Collection<DependencyRelation> dependencyRelations = new ArrayList<DependencyRelation>();
		dependencyRelations.add(new DependencyRelation(DependencyRelation.DET, 1, "Ki-Ras", 0, "The"));
		dependencyRelations.add(new DependencyRelation(DependencyRelation.NSUBJ, 2, "activated", 1, "Ki-Ras"));
		dependencyRelations.add(new DependencyRelation(DependencyRelation.DOBJ, 2, "activated", 4, "entity"));
		dependencyRelations.add(new DependencyRelation(DependencyRelation.DET, 4, "entity", 3, "another"));
		return dependencyRelations;
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
	private Collection<DependencyRelation> getDependencyRelationsForTestText2() {
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
	private Collection<DependencyRelation> getDependencyRelationsForTestText3() {
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
	 * Returns the dependency relations to use for this test parse
	 */
	private Collection<DependencyRelation> getDependencyRelationsForTestText4() {
		Collection<DependencyRelation> dependencyRelations = new ArrayList<DependencyRelation>();
		dependencyRelations.add(new DependencyRelation(DependencyRelation.DET, 1, "Ki-Ras", 0, "The"));
		dependencyRelations.add(new DependencyRelation(DependencyRelation.NSUBJ, 2, "activated", 1, "Ki-Ras"));
		dependencyRelations.add(new DependencyRelation(DependencyRelation.DOBJ, 2, "activated", 4, "brca1"));
		dependencyRelations.add(new DependencyRelation(DependencyRelation.DET, 4, "brca1", 3, "the"));
		return dependencyRelations;
	}

	/**
	 * Returns the dependency relations to use for this test parse
	 */
	private Collection<DependencyRelation> getDependencyRelationsForTestText5() {
		Collection<DependencyRelation> dependencyRelations = new ArrayList<DependencyRelation>();
		dependencyRelations.add(new DependencyRelation(DependencyRelation.DET, 1, "Ki-Ras", 0, "The"));
		dependencyRelations.add(new DependencyRelation(DependencyRelation.NSUBJ, 2, "activated", 1, "Ki-Ras"));
		dependencyRelations.add(new DependencyRelation(DependencyRelation.DOBJ, 2, "activated", 5, "brca1"));
		dependencyRelations.add(new DependencyRelation(DependencyRelation.DET, 5, "brca1", 3, "the"));
		dependencyRelations.add(new DependencyRelation(DependencyRelation.AMOD, 5, "brca1", 4, "green"));
		return dependencyRelations;
	}

	/**
	 * Returns the dependency relations to use for this test parse
	 */
	private Collection<DependencyRelation> getDependencyRelationsForTestText6() {
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
	private Collection<DependencyRelation> getDependencyRelationsForTestText7() {
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

	/*
	 * ------------------- Test Parse #1 -------------------
	 * 
	 * "The Ki-Ras activated another entity."
	 * 
	 * This test demonstrates the addition (and filling) of a second slot [activator].
	 */

	@Test
	public void testParse_fillingTwoSlots() throws Exception {
		Set<String> patterns = createSet("{c-protein} := ki-ras;", "{activation-keyword} := activated;",
				"{c-activate} := [activator] [trigger activation-keyword];");

		Set<String> expectedReferenceStrs = createSet("{activation-keyword}", "{c-protein}",
				"{c-activate [activator]={c-protein} [trigger]={activation-keyword}}");

		testParse(TEST_TEXT_1, patterns, expectedReferenceStrs, new ActivatedEntitySlotValidator((String[]) null),
				new ActivatorSlotValidator("Ki-Ras"));
	}

	/*
	 * ------------------- Test Parse #1 - simple use of dependency information -------------------
	 * 
	 * "The Ki-Ras activated another entity."
	 * 
	 * This test demonstrates that the use of correct dependency information does not result in a
	 * bad parse. (It doesn't actually prove that the system is using the dependency information,
	 * that will come with a later test).
	 */

	@Test
	public void testParse_usingDependencyRelationsDoesNotBreakThings() throws Exception {
		Set<String> patterns = createSet("{c-protein} := ki-ras;", "{activation-keyword} := activated;",
				"{c-activate} := [activator dep:x] [trigger activation-keyword head:x];");

		Set<String> expectedReferenceStrs = createSet("{activation-keyword}", "{c-protein}",
				"{c-activate [activator]={c-protein} [trigger]={activation-keyword}}");

		testParseUsingDependencyRelations(TEST_TEXT_1, patterns, expectedReferenceStrs,
				getDependencyRelationsForTestText1(), new ActivatedEntitySlotValidator((String[]) null),
				new ActivatorSlotValidator("Ki-Ras"));
	}

	/*
	 * ------------------- Test Parse #2 -------------------
	 * 
	 * "The Ki-Ras, which is a protein, activated another entity."
	 * 
	 * This test demonstrates a common problem often faced when constructing OpenDMAP patterns:
	 * accounting for intervening text. Here, we show how the simple pattern used previously is
	 * broken by the intervening text ", which is a protein,"
	 */

	@Test
	public void testParse_interveningTextDoesBreakThings() throws Exception {
		Set<String> patterns = createSet("{c-protein} := ki-ras;", "{activation-keyword} := activated;",
				"{c-activate} := [activator dep:x] [trigger activation-keyword head:x];");

		Set<String> expectedReferenceStrs = createSet("{activation-keyword}", "{c-protein}");

		testParse(TEST_TEXT_2, patterns, expectedReferenceStrs, new ActivatedEntitySlotValidator((String[]) null),
				new ActivatorSlotValidator((String[]) null));
	}

	/*
	 * --------------- Test Parse #2 - using dependency information and underscore ---------------
	 * 
	 * "The Ki-Ras, which is a protein, activated another entity."
	 * 
	 * This test demonstrates that the use of correct dependency information does not result in a
	 * bad parse. (Again, it doesn't actually prove that the system is using the dependency
	 * information, that will come with a later test).
	 */

	@Test
	public void testParse_usingDependencyRelationsDoesNotBreakThingsWithUnderscore() throws Exception {
		Set<String> patterns = createSet("{c-protein} := ki-ras;", "{activation-keyword} := activated;",
				"{c-activate} := [activator dep:x] _ [trigger activation-keyword head:x];");

		Set<String> expectedReferenceStrs = createSet("{activation-keyword}", "{c-protein}",
				"{c-activate [activator]={c-protein} [trigger]={activation-keyword}}");

		testParseUsingDependencyRelations(TEST_TEXT_2, patterns, expectedReferenceStrs,
				getDependencyRelationsForTestText2(), new ActivatedEntitySlotValidator((String[]) null),
				new ActivatorSlotValidator("Ki-Ras"));
	}

	/*
	 * --------- Test Parse #2 - using dependency information in pattern but not in parser ---------
	 * 
	 * "The Ki-Ras, which is a protein, activated another entity."
	 * 
	 * This test interestingly demonstrates a potential bug. Note in the slot path test suite where
	 * we showed that a slot constraint with no sign is given the "+" sign by default. This standard
	 * does not seem to apply to the functional constraints. In this example we show that the same
	 * patterns used above containing the single dependency relation also work when no dependency
	 * information is provided to the parser. This probably should not be the case.
	 */

	@Test
	public void testParse_usingDependencyRelationsInPatternWithoutDependencyRelationsOnText() throws Exception {
		Set<String> patterns = createSet("{c-protein} := ki-ras;", "{activation-keyword} := activated;",
				"{c-activate} := [activator dep:x] _ [trigger activation-keyword head:x];");

		Set<String> expectedReferenceStrs = createSet("{activation-keyword}", "{c-protein}",
				"{c-activate [activator]={c-protein} [trigger]={activation-keyword}}");

		testParseUsingDependencyRelations(TEST_TEXT_2, patterns, expectedReferenceStrs,
				new ArrayList<DependencyRelation>(), new ActivatedEntitySlotValidator((String[]) null),
				new ActivatorSlotValidator("Ki-Ras"));
	}

	/*
	 * --------------- Test Parse #2 - using dependency information and "-" sign ---------------
	 * 
	 * "The Ki-Ras, which is a protein, activated another entity."
	 * 
	 * The two tests below demonstrates that the use of a "-" sign in front of the "dep:x"
	 * constraint results in a failed match for both cases when dependency information is given to
	 * the parser and when it is withheld from the parser. (The same thing happens if the "-" sign
	 * is placed in front of the "head:x" constraint, and in front of both constraints)
	 */

	@Test
	public void testParse_usingDependencyRelationsWithMinusSign() throws Exception {
		Set<String> patterns = createSet("{c-protein} := ki-ras;", "{activation-keyword} := activated;",
				"{c-activate} := [activator -dep:x] _ [trigger activation-keyword head:x];");

		Set<String> expectedReferenceStrs = createSet("{activation-keyword}", "{c-protein}");

		testParseUsingDependencyRelations(TEST_TEXT_2, patterns, expectedReferenceStrs,
				getDependencyRelationsForTestText2(), new ActivatedEntitySlotValidator((String[]) null),
				new ActivatorSlotValidator((String[]) null));
	}

	@Test
	public void testParse_usingDependencyRelationsWithMinusSignAndWithheldDependencyInfo() throws Exception {
		Set<String> patterns = createSet("{c-protein} := ki-ras;", "{activation-keyword} := activated;",
				"{c-activate} := [activator -dep:x] _ [trigger activation-keyword head:x];");

		Set<String> expectedReferenceStrs = createSet("{activation-keyword}", "{c-protein}");

		testParseUsingDependencyRelations(TEST_TEXT_2, patterns, expectedReferenceStrs,
				new ArrayList<DependencyRelation>(), new ActivatedEntitySlotValidator((String[]) null),
				new ActivatorSlotValidator((String[]) null));
	}

	/*
	 * ------------------- Test Parse #3 -------------------
	 * 
	 * "The protein, which governs ki-ras, activated another entity."
	 * 
	 * Another example where intervening text can be a problem is demonstrated in this test. Here we
	 * see that the simple pattern used to parse Sentence #1 matches part of Sentence #3, however
	 * this is an incorrect match.
	 */

	@Test
	public void testParse_anotherWhereInterveningTextDoesBreakThings() throws Exception {
		Set<String> patterns = createSet("{c-protein} := ki-ras;", "{activation-keyword} := activated;",
				"{c-activate} := [activator dep:x] [trigger activation-keyword head:x];");

		Set<String> expectedReferenceStrs = createSet("{activation-keyword}", "{c-protein}",
		// the following reference string is a false positive in this case
				"{c-activate [activator]={c-protein} [trigger]={activation-keyword}}");

		testParse(TEST_TEXT_3, patterns, expectedReferenceStrs, new ActivatedEntitySlotValidator((String[]) null),
				new ActivatorSlotValidator("Ki-Ras"));
	}

	/*
	 * --------------- Test Parse #2 - using dependency information and underscore ---------------
	 * 
	 * "The protein, which governs ki-ras, activated another entity."
	 * 
	 * This test should demonstrate that the use of correct dependency information can result in the
	 * remediation of a bad parse.
	 */

	@Test
	public void testParse_usingDependencyRelationsToResolveABadParse() throws Exception {
		Set<String> patterns = createSet("{c-protein} := ki-ras;", "{activation-keyword} := activated;",
				"{c-activate} := [activator dep:x] _ [trigger activation-keyword head:x];");

		Set<String> expectedReferenceStrs = createSet("{activation-keyword}", "{c-protein}");

		testParseUsingDependencyRelations(TEST_TEXT_3, patterns, expectedReferenceStrs,
				getDependencyRelationsForTestText3(), new ActivatedEntitySlotValidator((String[]) null),
				new ActivatorSlotValidator((String[]) null));

	}

	/*
	 * ------------------- Test Parse #4 -------------------
	 * 
	 * "The Ki-Ras activated the brca1."
	 * 
	 * Adding the activated-entity slot.
	 */

	@Test
	public void testParse_usingDependencyRelationsFillingMultipleSlots() throws Exception {
		Set<String> patterns = createSet("{c-protein} := ki-ras;", "{c-protein} := brca1;",
				"{activation-keyword} := activated;",
				"{c-activate} := [activator] [trigger activation-keyword head:x] _ [activated-entity dep:x];");

		Set<String> expectedReferenceStrs = createSet("{activation-keyword}", "{c-protein}",
				"{c-activate [activator]={c-protein} [trigger]={activation-keyword} [activated-entity]={c-protein}}");

		testParseUsingDependencyRelations(TEST_TEXT_4, patterns, expectedReferenceStrs,
				getDependencyRelationsForTestText4(), new ActivatedEntitySlotValidator("BRCA1"),
				new ActivatorSlotValidator("Ki-Ras"));

	}

	/*
	 * ------------------- Test Parse #5 -------------------
	 * 
	 * "The Ki-Ras activated the green brca1."
	 * 
	 * Adding the activated-entity slot with some extra intervening text: "green".
	 */

	@Test
	public void testParse_usingDependencyRelationsFillingMultipleSlotsWithInterveningText() throws Exception {
		Set<String> patterns = createSet("{c-protein} := ki-ras;", "{c-protein} := brca1;",
				"{activation-keyword} := activated;",
				"{c-activate} := [activator] [trigger activation-keyword head:x] _ [activated-entity dep:x];");

		Set<String> expectedReferenceStrs = createSet("{activation-keyword}", "{c-protein}",
				"{c-activate [activator]={c-protein} [trigger]={activation-keyword} [activated-entity]={c-protein}}");

		testParseUsingDependencyRelations(TEST_TEXT_5, patterns, expectedReferenceStrs,
				getDependencyRelationsForTestText5(), new ActivatedEntitySlotValidator("BRCA1"),
				new ActivatorSlotValidator("Ki-Ras"));

	}

	/*
	 * ------------------- Test Parse #6 -------------------
	 * 
	 * "The Ki-Ras activated the brca1 protein IMP3."
	 * 
	 * Adding the activated-entity slot and using dependency information to ensure the proper
	 * protein is extracted as the activated-entity.
	 */

	@Test
	public void testParse_usingDependencyRelationsFillingMultipleSlotsWithMulipleProteins() throws Exception {
		Set<String> patterns = createSet("{c-protein} := ki-ras;", "{c-protein} := brca1;", "{c-protein} := IMP3;",
				"{activation-keyword} := activated;",
				"{c-activate} := [activator] [trigger activation-keyword head:x] _ [activated-entity dep:x];");

		Set<String> expectedReferenceStrs = createSet("{activation-keyword}", "{c-protein}",
				"{c-activate [activator]={c-protein} [trigger]={activation-keyword} [activated-entity]={c-protein}}");

		testParseUsingDependencyRelations(TEST_TEXT_6, patterns, expectedReferenceStrs,
				getDependencyRelationsForTestText6(), new ActivatedEntitySlotValidator("IMP3"),
				new ActivatorSlotValidator("Ki-Ras"));

	}

	/*
	 * --------------- Test Parse #7 - getting fancy with dependency relations ---------------
	 * 
	 * "Ki-Ras, which governs ABC-2, activated the BRCA1 protein IMP3."
	 * 
	 * This test should demonstrate that the use of correct dependency information in conjunction
	 * with the underscore can resolve a somewhat difficult sentence. Here we introduce two
	 * dependency constraints (x and y) to get the correct parse.
	 */

	@Test
	public void testParse_usingDependencyRelationsFillingMultipleSlotsWithMulipleDependencyConstraintsXHHY()
			throws Exception {
		Set<String> patterns = createSet("{c-protein} := ki-ras;", "{c-protein} := brca1;", "{c-protein} := IMP3;",
				"{c-protein} := ABC-2;", "{activation-keyword} := activated;",
				"{c-activate} := [activator dep:x] _ [trigger activation-keyword head:x head:y] _ [activated-entity dep:y];");

		Set<String> expectedReferenceStrs = createSet("{activation-keyword}", "{c-protein}",
				"{c-activate [activator]={c-protein} [trigger]={activation-keyword} [activated-entity]={c-protein}}");

		testParseUsingDependencyRelations(TEST_TEXT_7, patterns, expectedReferenceStrs,
				getDependencyRelationsForTestText7(), new ActivatedEntitySlotValidator("IMP3"),
				new ActivatorSlotValidator("Ki-Ras"));

	}

	/*
	 * --------------- Test Parse #7 - getting fancy with dependency relations ---------------
	 * 
	 * "Ki-Ras, which governs ABC-2, activated the BRCA1 protein IMP3."
	 * 
	 * A different take.. note the slight change in pattern constraints, (dep:x head:x dep:x)
	 * instead of (dep:x head:x head:y dep:y)
	 */

	@Test
	public void testParse_usingDependencyRelationsFillingMultipleSlotsWithMulipleDependencyConstraintsXHX()
			throws Exception {
		Set<String> patterns = createSet("{c-protein} := ki-ras;", "{c-protein} := brca1;", "{c-protein} := IMP3;",
				"{c-protein} := ABC-2;", "{activation-keyword} := activated;",
				"{c-activate} := [activator dep:x] _ [trigger activation-keyword head:x] _ [activated-entity dep:x];");

		Set<String> expectedReferenceStrs = createSet("{activation-keyword}", "{c-protein}",
				"{c-activate [activator]={c-protein} [trigger]={activation-keyword} [activated-entity]={c-protein}}");

		testParseUsingDependencyRelations(TEST_TEXT_7, patterns, expectedReferenceStrs,
				getDependencyRelationsForTestText7(), new ActivatedEntitySlotValidator("IMP3"),
				new ActivatorSlotValidator("Ki-Ras"));

	}
}
