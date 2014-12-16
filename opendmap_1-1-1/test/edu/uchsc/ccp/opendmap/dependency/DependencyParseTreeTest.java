package edu.uchsc.ccp.opendmap.dependency;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;

public class DependencyParseTreeTest {

	/**
	 * <pre>
	 * 7 shut - nsubj (7,2)
	 *   2 rain - det (2,0)
	 *     0 The - det (2,0)
	 *   2 rain - amod (2,1)
	 *     1 strongest - amod (2,1)
	 *   2 rain - nsubj (7,2)
	 *   2 rain - partmod (2,4)
	 *     4 recorded - advmod (4,3)
	 *       3 ever - advmod (4,3)
	 *     4 recorded - partmod (2,4)
	 *     4 recorded - prep_in (4,6)
	 *       6 India - prep_in (4,6)
	 * 7 shut - prt (7,8)
	 *   8 down - prt (7,8)
	 * 7 shut - dobj (7,11)
	 *   11 hub - det (11,9)
	 *     9 the - det (11,9)
	 *   11 hub - amod (11,10)
	 *     10 financial - amod (11,10)
	 *   11 hub - dobj (7,11)
	 *   11 hub - prep_of (11,13)
	 *     13 Mumbai - prep_of (11,13)
	 * </pre>
	 * 
	 * @return
	 */
	private Collection<DependencyRelation> getSampleDependencyRelations() {
		Collection<DependencyRelation> dependencies = new ArrayList<DependencyRelation>();
		dependencies.add(new DependencyRelation(DependencyRelation.DET, 2, "rain", 0, "The"));
		dependencies.add(new DependencyRelation(DependencyRelation.AMOD, 2, "rain", 1, "strongest"));
		dependencies.add(new DependencyRelation(DependencyRelation.NSUBJ, 7, "shut", 2, "rain"));
		dependencies.add(new DependencyRelation(DependencyRelation.ADVMOD, 4, "recorded", 3, "ever"));
		dependencies.add(new DependencyRelation(DependencyRelation.PARTMOD, 2, "rain", 4, "recorded"));
		dependencies.add(new DependencyRelation(DependencyRelation.PREP, 4, "recorded", 6, "India"));
		dependencies.add(new DependencyRelation(DependencyRelation.PRT, 7, "shut", 8, "down"));
		dependencies.add(new DependencyRelation(DependencyRelation.DET, 11, "hub", 9, "the"));
		dependencies.add(new DependencyRelation(DependencyRelation.AMOD, 11, "hub", 10, "financial"));
		dependencies.add(new DependencyRelation(DependencyRelation.DOBJ, 7, "shut", 11, "hub"));
		dependencies.add(new DependencyRelation(DependencyRelation.PREP, 11, "hub", 13, "Mumbai"));
		return dependencies;
	}

	private DependencyParseTree initDependencyParseTree(Collection<DependencyRelation> dependencyRelations) {
		DependencyParseTree parseTree = new DependencyParseTree();
		for (DependencyRelation dependency : dependencyRelations) {
			int governor = dependency.getGovernorTokenNumber();
			int dependent = dependency.getDependentTokenNumber();
			String relation = dependency.getType();
			parseTree.addRelation(governor, governor, relation, dependent, dependency.getGovernorTokenText());
			parseTree.addRelation(dependent, governor, relation, dependent, dependency.getDependentTokenText());
		}
		parseTree.finalize();
		return parseTree;
	}

	@Test
	public void testLowestCommonAncestor() throws Exception {
		DependencyParseTree parseTree = initDependencyParseTree(getSampleDependencyRelations());
		parseTree.print(System.out);
		assertEquals(2, parseTree.lowestCommonAncestor(new int[] { 0, 6 }));
		assertEquals(7, parseTree.lowestCommonAncestor(new int[] { 7, 11 }));
		assertEquals(7, parseTree.lowestCommonAncestor(new int[] { 8, 2 }));
	}

	@Test
	public void testGoverns() throws Exception {
		DependencyParseTree parseTree = initDependencyParseTree(getSampleDependencyRelations());
		assertTrue(parseTree.governs(2, 0));
		assertFalse(parseTree.governs(0, 2));
		assertTrue(parseTree.governs(2, 1));
		assertTrue(parseTree.governs(7, 2));
		assertTrue(parseTree.governs(4, 3));
		assertTrue(parseTree.governs(2, 4));
		assertTrue(parseTree.governs(4, 6));
		assertTrue(parseTree.governs(7, 8));
		assertTrue(parseTree.governs(11, 9));
		assertTrue(parseTree.governs(11, 10));
		assertTrue(parseTree.governs(7, 11));
		assertTrue(parseTree.governs(11, 13));
		assertFalse(parseTree.governs(6, 3));
		assertFalse(parseTree.governs(2, 6));
	}

	@Test
	public void testBackupAncestor() throws Exception {
		DependencyParseTree parseTree = initDependencyParseTree(getSampleDependencyRelations());
		assertEquals(0, parseTree.backupAncestor(0));
		assertEquals(1, parseTree.backupAncestor(1));
		assertEquals(2, parseTree.backupAncestor(2));
		assertEquals(3, parseTree.backupAncestor(3));
		assertEquals(4, parseTree.backupAncestor(4)); // note 4 --> 2 here b/c of partmod relation - see subsumes(),
														// removed subsumes so this no longer holds true
		assertEquals(5, parseTree.backupAncestor(5));
		assertEquals(6, parseTree.backupAncestor(6));
		assertEquals(7, parseTree.backupAncestor(7));
		assertEquals(8, parseTree.backupAncestor(8));
		assertEquals(9, parseTree.backupAncestor(9));
		assertEquals(10, parseTree.backupAncestor(10));
		assertEquals(11, parseTree.backupAncestor(11));
		assertEquals(12, parseTree.backupAncestor(12));
		assertEquals(13, parseTree.backupAncestor(13));
	}

	@Test
	public void testHasChild() throws Exception {
		DependencyParseTree parseTree = initDependencyParseTree(getSampleDependencyRelations());
		assertTrue(parseTree.hasChild(7, 0));
		assertTrue(parseTree.hasChild(7, 1));
		assertTrue(parseTree.hasChild(7, 2));
		assertTrue(parseTree.hasChild(7, 3));
		assertTrue(parseTree.hasChild(7, 4));
		assertFalse(parseTree.hasChild(7, 5));
		assertTrue(parseTree.hasChild(7, 6));
		assertTrue(parseTree.hasChild(7, 7));
		assertTrue(parseTree.hasChild(7, 8));
		assertTrue(parseTree.hasChild(7, 9));
		assertTrue(parseTree.hasChild(7, 10));
		assertTrue(parseTree.hasChild(7, 11));
		assertFalse(parseTree.hasChild(7, 12));
		assertTrue(parseTree.hasChild(7, 13));
		assertTrue(parseTree.hasChild(7, 7)); // not sure this should return true
		assertTrue(parseTree.hasChild(2, 1));
		assertTrue(parseTree.hasChild(2, 4));
		assertTrue(parseTree.hasChild(2, 3));
		assertTrue(parseTree.hasChild(2, 6));
		assertFalse(parseTree.hasChild(6, 2));

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
	private Collection<DependencyRelation> getSampleDependencyRelations2() {
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

	@Test
	public void testLowestCommonAncestor2() throws Exception {
		DependencyParseTree parseTree = initDependencyParseTree(getSampleDependencyRelations2());
		parseTree.print(System.out);
		assertEquals(6, parseTree.lowestCommonAncestor(new int[] { 6 }));
	}

	@Test
	public void testGoverns2() throws Exception {
		DependencyParseTree parseTree = initDependencyParseTree(getSampleDependencyRelations2());
		assertTrue(parseTree.governs(1, 0));
		assertFalse(parseTree.governs(0, 1));
		assertTrue(parseTree.governs(6, 1));
		assertTrue(parseTree.governs(1, 5));
		assertTrue(parseTree.governs(5, 2));
		assertTrue(parseTree.governs(5, 3));
		assertTrue(parseTree.governs(5, 4));
		assertTrue(parseTree.governs(6, 8));
		assertTrue(parseTree.governs(8, 7));
		assertFalse(parseTree.governs(7, 8));
		assertFalse(parseTree.governs(1, 4));
	}

	@Test
	public void testBackupAncestor2() throws Exception {
		DependencyParseTree parseTree = initDependencyParseTree(getSampleDependencyRelations2());
		assertEquals(0, parseTree.backupAncestor(0));
		assertEquals(1, parseTree.backupAncestor(1));
		assertEquals(2, parseTree.backupAncestor(2));
		assertEquals(3, parseTree.backupAncestor(3));
		assertEquals(4, parseTree.backupAncestor(4));
		assertEquals(5, parseTree.backupAncestor(5));
		assertEquals(6, parseTree.backupAncestor(6));
		assertEquals(7, parseTree.backupAncestor(7));
		assertEquals(8, parseTree.backupAncestor(8));
	}
}
