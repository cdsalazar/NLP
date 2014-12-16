/*
 * DMAPStanfordPhraseGraph.java
 * Copyright (C) 2007 Center for Computational Pharmacology, University of Colorado School of Medicine
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 */

package edu.uchsc.ccp.opendmap.dependency;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import edu.uchsc.ccp.opendmap.DMAPPropertyFunctionException;
import edu.uchsc.ccp.opendmap.DMAPPropertyFunctionHandler;
import edu.uchsc.ccp.opendmap.DMAPToken;
import edu.uchsc.ccp.opendmap.Reference;

/**
 * This class allows some syntactic constraints to be issued in DMAP patterns based on a dependency graph structure
 * modeled after the Stanford Parser output.
 * 
 * 
 * This class packages up a Stanford Phrase Tree as a DMAP Property Function Handler.
 * <p>
 * The goal with this class is to make syntactic constraints that the Stanford Parser identifies available to DMAP to
 * use as constraints between the fillers of separate slots. For example:
 * 
 * <pre>
 *    {c-statement} := [subject dep:x] [action head:x] [object dep:x];
 * </pre>
 * 
 * This pattern is intended to mean that a c-statement can be recognized when DMAP sees a [subject] an [action] and an
 * [object]. However, in addition, the property function constraints on [subject] and [object] says that they must be
 * syntactically "dependent" parts of the phrase encompassing the [action] (the head of the phrase).
 * <p>
 * The syntactic property functions "dep" and "head" only make sense in the context of some sort of syntactic
 * information. This StanfordPhraseGraph class defines these two property functions and uses the information from the
 * Stanford Parser to evaluate them.
 * <p>
 * In future, it is possible to add additional syntactic functions like "subj:x" and "obj:x" to use more Stanford Parser
 * information.
 * <p>
 * <i>Note</i> The main problem with this at the moment is that the Stanford Parser is often wrong about these syntactic
 * assignments in GeneRIFs.
 * 
 * @author R. James Firby
 */
public class DMAPDependencyGraphFunctionHandler implements DMAPPropertyFunctionHandler {

	/* A processed version of the Stanford Parser output highlighting dependency relationships */
	protected DependencyParseTree tree = new DependencyParseTree();

	/* True if addtional debugging information should be printed */
	protected boolean debug = false;

	/* Property function constraints already seen by this handler */
	protected HashMap<String, Collection<BindingValue>> bindings = null;

	/**
	 * Private constructor used for cloning.
	 */
	protected DMAPDependencyGraphFunctionHandler() {
	}

	public DMAPDependencyGraphFunctionHandler(Collection<DependencyRelation> dependencyRelations) {
		this.debug = false;
		initializeDependencyGraph(dependencyRelations);
	}

	public DMAPDependencyGraphFunctionHandler(Collection<DependencyRelation> dependencyRelations, boolean debug) {
		this.debug = debug;
		initializeDependencyGraph(dependencyRelations);
	}

	protected void initializeDependencyGraph(Collection<DependencyRelation> dependencyRelations) {
		for (DependencyRelation dependency : dependencyRelations) {
			int governor = dependency.getGovernorTokenNumber();
			int dependent = dependency.getDependentTokenNumber();
			String relation = dependency.getType();
			tree.addRelation(governor, governor, relation, dependent, dependency.getGovernorTokenText());
			tree.addRelation(dependent, governor, relation, dependent, dependency.getDependentTokenText());
		}
		tree.finalize();
	}

	/**
	 * Check whether this property function handler wants to handle the specified function.
	 */
	public boolean handles(String func) {
		if (func == null)
			return false;
		if (func.equalsIgnoreCase("dep"))
			return true;
		if (func.equalsIgnoreCase("head"))
			return true;
		return false;
	}

	/**
	 * Evaluate a function. That is, check to see whether the reference is consistent with this function and all
	 * functions saved away from other slots. If all are satisfied then reference is a consistent slot filler for the
	 * slot with this property function.
	 */
	public boolean evalFunction(String func, String arg, Reference reference) throws DMAPPropertyFunctionException {
		// System.out.println("********* Evaluating function: " + func + "(" + arg + ") for reference: " +
		// reference.getReferenceString()
		// + "=" + reference.getTokens().get(0).getItem().getText());
		int group = getPhraseGroup(reference);
		String key = arg.toLowerCase();
		if (bindings.containsKey(key)) {
			// System.out.println("*** Checking bindings for key:" + key);
			Collection<BindingValue> values = bindings.get(key);
			boolean validated = validateBindings(values, new BindingValue(func, group));
			// System.out.println("*** Bindings validated? " + validated + " Evaluating function returning: " +
			// validated);
			return validated;
		} else {
			// System.out.println("*** No bindings to validate. Evaluating function returning true.");
			return true;
		}
	}

	/**
	 * Record a property function constraint for later evaluation.
	 */
	public void recordFunction(String func, String arg, Reference reference) {
		// System.out.println("========== Recording function constraint (binding): " + func + "(" + arg +
		// ") for reference: "
		// + reference.getReferenceString() + "=" + reference.getTokens().get(0).getItem().getText());
		if (bindings == null) {
			bindings = new HashMap<String, Collection<BindingValue>>();
		}
		int group = getPhraseGroup(reference);
		String key = arg.toLowerCase();
		Collection<BindingValue> values = null;
		if (bindings.containsKey(key)) {
			values = bindings.get(key);
		} else {
			values = new ArrayList<BindingValue>(1);
			bindings.put(key, values);
		}
		// System.out.println("Recording new binding pair for key:" + key + " => func:" + func + " group:" + group);
		values.add(new BindingValue(func, group));
		// System.out.println("Bindings for key:" + key);
		for (BindingValue bv : values) {
			// System.out.println(bv.toString());
		}
	}

	/**
	 * Copy this property function handler and any internal record functions.
	 */
	public DMAPDependencyGraphFunctionHandler clone() {
		DMAPDependencyGraphFunctionHandler graph = new DMAPDependencyGraphFunctionHandler();
		graph.tree = tree;
		graph.debug = debug;
		if (bindings != null) {
			HashMap<String, Collection<BindingValue>> newBindings = new HashMap<String, Collection<BindingValue>>();
			for (String key : bindings.keySet()) {
				Collection<BindingValue> values = bindings.get(key);
				if (values != null) {
					ArrayList<BindingValue> newValues = new ArrayList<BindingValue>(values.size());
					for (BindingValue value : values)
						newValues.add(value);
					newBindings.put(key, newValues);
				}
			}
			graph.bindings = newBindings;
		}
		return graph;
	}

	/**
	 * Print a human readable version of this phrase graph.
	 * 
	 * @param stream
	 *            The stream to print to.
	 */
	public void print(PrintStream stream) {
		tree.print(stream);
	}

	/**
	 * See if all the functions associated with this variable are satisfied.
	 * 
	 * @param graph
	 *            The phrase graph to check in
	 * @param values
	 *            The func/phrase pairs already in the bindings
	 * @param test
	 *            The func/phrase pair being checked
	 * @return True if the test is consistent with all stored func/phrase pairs
	 * @throws DMAPPropertyFunctionException
	 */
	private boolean validateBindings(Collection<BindingValue> values, BindingValue test) throws DMAPPropertyFunctionException {
		// System.out.println("Validating bindings... test binding: " + test.toString());
		// for (BindingValue binding : values) {
		// System.out.println("Other " + binding.toString());
		// }

		if ((values == null) || (values.size() < 1))
			return false;
		// Find the head, if there is one.
		int head = -1;
		if ((test != null) && test.function.equals("head")) {
			head = test.value;
		} else {
			for (BindingValue val : values) {
				if (val.function.equals("head")) {
					if (head != -1) {
						throw new DMAPPropertyFunctionException("Pattern found with multiple head: annotations.");
					}
					head = val.value;
				}
			}
		}
		if (head == -1) {
			// System.out.println("Head binding value = -1, returning true from validateBindings()");
			return true;
		}
		// Check that all dependents are children of the head
		boolean okay = true;
		if ((test != null) && test.function.equals("dep")) {
			okay = tree.governs(head, test.value);
		}
		if (okay) {
			for (BindingValue val : values) {
				if (val.function.equals("dep")) {
					okay = tree.governs(head, val.value);
					if (!okay)
						break;
				} else if (!val.function.equals("head")) {
					throw new DMAPPropertyFunctionException("Use of unknown pattern annotation function '" + val.function + "'");
				}
			}
		}
		return okay;
	}

	/**
	 * Get the phrase group that this reference belongs to. Check each token that was used in recognizing the reference
	 * to see what phrase it it in. Then find the lowest common parent that includes them all.
	 * 
	 * @param reference
	 *            The reference to process.
	 * @return The phrase that this reference is part of.
	 */
	private int getPhraseGroup(Reference reference) {
		// System.out.println("Getting PhraseGroup for reference: " + reference.getReferenceString() + "="
		// + reference.getTokens().get(0).getItem().getText());
		// Find the phrase group for each token in the reference
		List<DMAPToken> tokens = reference.getTokens();
		if ((tokens == null) || tokens.isEmpty()) {
			// System.out.println("Phrase group is -1 for " + reference.getReferenceString() + " No tokens.");
			return -1;
		}
		ArrayList<Integer> ids = new ArrayList<Integer>();
		if ((tokens.size() == 1) && (tokens.get(0).getStart() == tokens.get(0).getEnd())) {
			ids.add(new Integer(tokens.get(0).getStart()));
		} else {
			for (DMAPToken token : tokens) {
				for (int i = token.getStart(); i <= token.getEnd(); i++) {
					if (tree.isRelevantToken(i)) {
						Integer id = new Integer(i);
						if (!ids.contains(id))
							ids.add(id);
					}
				}
			}
		}
		if (ids.isEmpty()) {
			// System.out.println("Phrase group is -1 for " + reference.getReferenceString() + " No IDs.");
			return -1;
		}
		// Find the lowest common ancestor for all of these groups
		int realIds[] = new int[ids.size()];
		for (int i = 0; i < ids.size(); i++) {
			realIds[i] = ids.get(i).intValue();
		}
		int group = tree.lowestCommonAncestor(realIds);
		if (group < 0) {
			// System.out.println("Phrase group is " + group + " for " + reference.getReferenceString() + " Group < 0");
			return -1;
		}
		group = tree.backupAncestor(group);
		// Done
		// if (debug) {
		// System.out.println("Phrase group is " + group + " for " + reference.getReferenceString() + "="
		// + reference.getTokens().get(0).getItem().getText());
		// }
		return group;
	}

	/**
	 * Helper class to hold func/phrase pairs. These represent cached property function constraints that have been saved
	 * within this handler.
	 * 
	 * @author R. James Firby
	 */
	protected class BindingValue {

		String function = null;

		int value = -1;

		public BindingValue(String func, int val) {
			function = func;
			value = val;
		}

		@Override
		public String toString() {
			return "Binding= function:" + function + " value:" + value;
		}

	}

	// /**
	// * Helper class to hold the Stanford Phrase Tree
	// * <p>
	// * The phrase tree is constructed from the Stanford Parser output and highlights the dependencies between tokens.
	// In
	// * particular, the hierarchy represents a set of phrases that the token belongs to.
	// *
	// * @author R. James Firby
	// */
	// private class ParseTree {
	//
	// ArrayList<ParseTreeNode> roots = new ArrayList<ParseTreeNode>();
	//
	// ArrayList<ParseTreeNode> nodes = new ArrayList<ParseTreeNode>();
	//
	// /**
	// * Add a new dependency relationship to the phrase graph.
	// *
	// * @param id
	// * The identity of this relationship. (It's phrase group number)
	// * @param governor
	// * The identity of the governor of this phrase.
	// * @param relation
	// * The type of relationship between the governor and the dependent.
	// * @param dependent
	// * The identiy of the dependent node in this phrase.
	// * @param text
	// * The text string of this phrase.
	// */
	// public void addRelation(int id, int governor, String relation, int dependent, String text) {
	// ParseTreeNode newNode = new ParseTreeNode(id, governor, dependent, relation, text);
	// boolean found = false;
	// for (ParseTreeNode node : nodes) {
	// if (node.equals(newNode)) {
	// found = true;
	// break;
	// }
	// }
	// if (!found)
	// nodes.add(newNode);
	// }
	//
	// /**
	// * Calculate all the connected subtress in the graph and cache them.
	// */
	// public void finalize() {
	// for (ParseTreeNode node : nodes) {
	// ArrayList<ParseTreeNode> parents = findGovernors(node.id);
	// if (parents.isEmpty()) {
	// roots.add(node);
	// } else {
	// for (ParseTreeNode parent : parents) {
	// parent.addChild(node);
	// }
	// }
	// }
	// }
	//
	// /**
	// * Check whether a particular node/token governs another.
	// *
	// * @param governor
	// * The identity of the possible governor.
	// * @param dependent
	// * The identify of the possible dependent.
	// * @return True if the dependent depends on the governor.
	// */
	// public boolean governs(int governor, int dependent) {
	// for (ParseTreeNode node : nodes) {
	// if (node.id == governor) {
	// if (node.hasChild(dependent)) {
	// return true;
	// }
	// }
	// }
	// return false;
	// }
	//
	// /**
	// * Find the lowest governor of all the dependents supplied.
	// *
	// * @param children
	// * The identified of the dependent children.
	// * @return The common ancestor (governor) of all the children.
	// */
	// public int lowestCommonAncestor(int children[]) {
	// if (children.length < 1) {
	// System.out.println("LowestCommonAncestor returning -1, children.length < 1");
	// return -1;
	// }
	// // if (children.length == 1) return children[0];
	// int key = children[0];
	// int best = -1;
	// for (ParseTreeNode node : nodes) {
	// if (node.id == key) {
	// int ancestor = lowestCommonAncestor(node, children);
	// if (ancestor != -1) {
	// if ((best == -1) || governs(best, ancestor)) {
	// best = ancestor;
	// }
	// }
	// }
	// }
	// System.out.println("LowestCommonAncestor returning " + best);
	// return best;
	// }
	//
	// /**
	// * Find the lowest common ancestor of all the children that has the given node as a child.
	// *
	// * @param node
	// * The node that must be a child.
	// * @param children
	// * The identity of all the children.
	// * @return The identity of the lowest common ancestor of the children looking up from node.
	// */
	// private int lowestCommonAncestor(ParseTreeNode node, int children[]) {
	// ParseTreeNode lowest = null;
	// if (governsAll(node, children)) {
	// lowest = node;
	// } else {
	// for (ParseTreeNode ancestor : node.ancestors()) {
	// if (governsAll(ancestor, children)) {
	// lowest = ancestor;
	// break;
	// }
	// }
	// }
	// if (lowest == null) {
	// return -1;
	// } else {
	// // lowest = backupAncestor(lowest);
	// return lowest.id;
	// }
	// }
	//
	// /**
	// * Check whether a node governs a list of children.
	// *
	// * @param node
	// * The governing node.
	// * @param children
	// * The identities of the children.
	// * @return True if the governing node really governs all the children.
	// */
	// private boolean governsAll(ParseTreeNode node, int children[]) {
	// boolean governsAll = true;
	// for (int i = 1; i < children.length; i++) {
	// if (!governs(node.id, children[i])) {
	// governsAll = false;
	// break;
	// }
	// }
	// return governsAll;
	// }
	//
	// /**
	// * Backup the ancestor tree to on some types of Stanford Parser relations to find the true governing phrase.
	// *
	// * @param id
	// * The identity of the phrase to check.
	// * @return The tru governing phrase.
	// */
	// public int backupAncestor(int id) {
	// // Find the node corresponding to the given id
	// ParseTreeNode node = null;
	// for (ParseTreeNode n : nodes) {
	// if (n.id == id) {
	// node = n;
	// break;
	// }
	// }
	// if (node == null)
	// return id;
	// // Look for an ancestor that is the "head" of a phrase that this node belongs to.
	// // We really want the relationships between "head" components, not individual tokens.
	// ParseTreeNode best = node;
	// for (ParseTreeNode parent : node.ancestors()) {
	// boolean okay = false;
	// for (ParseTreeNode child : parent.children) {
	// if (child.id == best.id) {
	// // If the parent is the real head of a phrase that child is part of, backup.
	// if (subsumes(parent, child)) {
	// best = parent;
	// okay = true;
	// }
	// }
	// }
	// if (!okay)
	// break;
	// }
	// System.out.println("BackupAncestor returning: ");
	// best.print(System.out, 0);
	// return best.id;
	// }
	//
	// /**
	// * Check to see if parent is the head of a phrase that child is part of. We are typically looking for dependency
	// * relationships between head phrases.
	// *
	// * @param parent
	// * The parent phrase node.
	// * @param child
	// * The child phrase node.
	// * @return True if the parent is the head of a subphrase holding child.
	// */
	// private boolean subsumes(ParseTreeNode parent, ParseTreeNode child) {
	// System.out.println("Checking subsumption (parent is head of a phrase that child is part of.");
	// System.out.print("Parent: ");
	// parent.print(System.out, 0);
	// System.out.println();
	// System.out.print("Child: ");
	// child.print(System.out, 0);
	//			
	// if ((child.governor == parent.id) && child.relation.equalsIgnoreCase("nn")) {
	// // If the child is part of a noun phrase, (ie. "nn") then
	// // we really want the head noun. So, backup.
	// System.out.println("Subsumption check = true");
	// return true;
	// } else if ((child.governor == parent.id) && child.relation.equalsIgnoreCase("dep")) {
	// // If the child is a dependent subclause, then look for the root clause
	// System.out.println("Subsumption check = true");
	// return true;
	// } else if ((child.governor == parent.id) && child.relation.equalsIgnoreCase("partmod")) {
	// // If the child is modified by a participle, then look for the root clause
	// System.out.println("Subsumption check = true");
	// return true;
	// } else if ((child.governor == parent.id) && child.relation.equalsIgnoreCase("conj")) {
	// // If the child is part of a conjunct, then look for the root clause
	// System.out.println("Subsumption check = true");
	// return true;
	// } else if ((child.governor == parent.id) && child.relation.equalsIgnoreCase("ccomp")) {
	// // If the child is part of a conjunct, then look for the root clause
	// System.out.println("Subsumption check = true");
	// return true;
	// // } else if ((child.governor == parent.id) && child.relation.equalsIgnoreCase("dobj")) {
	// // // If the child is part of a direct object, then look for the root clause
	// // return true;
	// } else if ((child.governor == parent.id) && child.relation.equalsIgnoreCase("nsubj")) {
	// // If the child is part of a conjunct, then look for the root clause
	// System.out.println("Subsumption check = true - nsubj");
	// return true;
	// } else {
	// System.out.println("Subsumption check = FALSE");
	// return false;
	// }
	// }
	//
	// /**
	// * Check for Stanford Parser tokens that we should ignore.
	// *
	// * @param id
	// * The id of the Stanford Parser token.
	// * @return True if we should include this token in the phrase graph.
	// */
	// public boolean isRelevantToken(int id) {
	// // Ignore "num" nodes because they are almost always labelled wrong
	// for (int i = 0; i < nodes.size(); i++) {
	// ParseTreeNode node = nodes.get(i);
	// if (node.id == id) {
	// if (node.relation.equalsIgnoreCase("num"))
	// return false;
	// }
	// }
	// return true;
	// }
	//
	// /**
	// * Find all of the governors of a Stanford Parser token.
	// *
	// * @param id
	// * The token id.
	// * @return The governor nodes (akk parents from all partial subtrees).
	// */
	// private ArrayList<ParseTreeNode> findGovernors(int id) {
	// ArrayList<ParseTreeNode> governors = new ArrayList<ParseTreeNode>();
	// for (ParseTreeNode node : nodes) {
	// if ((node.id != id) && (node.dependent == id))
	// governors.add(node);
	// }
	// return governors;
	// }
	//
	// /**
	// * Print the parse tree in human readable form.
	// *
	// * @param stream
	// */
	// public void print(PrintStream stream) {
	// if (roots.isEmpty()) {
	// stream
	// .println("No roots in parse tree. This means that there is no typed dependency information available for use by OpenDMAP!!! ");
	// } else {
	// for (ParseTreeNode root : roots) {
	// root.print(stream, 0);
	// }
	// }
	// }
	//
	// }
	//
	// /**
	// * Helper class to represent a single Phrase Tree Node.
	// *
	// * @author R. James Firby
	// */
	// private class ParseTreeNode {
	//
	// private int id = -1;
	//
	// private int governor = -1;
	//
	// private int dependent = -1;
	//
	// private String relation = null;
	//
	// private String text = null;
	//
	// /* The direct parent of this node */
	// private ParseTreeNode parent = null;
	//
	// /* All of the children of this node */
	// private ArrayList<ParseTreeNode> children = null;
	//
	// /* A cached list of all ancestors of this node */
	// private ArrayList<ParseTreeNode> ancestors = null;
	//
	// /**
	// * Create a new parse tree node.
	// *
	// * @param id
	// * The id of this node/phrase/token.
	// * @param governor
	// * The id of the governing token.
	// * @param dependent
	// * The id of the dependent token.
	// * @param relation
	// * The Stanford Parser relationship between these tokens.
	// * @param text
	// * The text of this token.
	// */
	// public ParseTreeNode(int id, int governor, int dependent, String relation, String text) {
	// this.id = id;
	// this.governor = governor;
	// this.dependent = dependent;
	// this.relation = relation;
	// this.text = text;
	// }
	//
	// /**
	// * Add a child node to this node.
	// *
	// * @param node
	// * The child to add.
	// */
	// public void addChild(ParseTreeNode node) {
	// ancestors = null;
	// if (children == null)
	// children = new ArrayList<ParseTreeNode>();
	// children.add(node);
	// node.parent = this;
	// }
	//
	// /**
	// * Check whether this node has a child with the given id.
	// *
	// * @param childId
	// * The id of the child node.
	// * @return True if this node has such a child.
	// */
	// private boolean hasChild(int childId) {
	// if (id == childId)
	// return true;
	// if (children == null)
	// return false;
	// for (ParseTreeNode child : children) {
	// if (child.hasChild(childId)) {
	// return true;
	// }
	// }
	// return false;
	// }
	//
	// /**
	// * Compute all of the ancestors for this node and cache them away.
	// *
	// * @return All ancestors for this node.
	// */
	// public ArrayList<ParseTreeNode> ancestors() {
	// if (ancestors == null) {
	// ancestors = new ArrayList<ParseTreeNode>();
	// computeAncestors(ancestors, this);
	// }
	// return ancestors;
	// }
	//
	// private void computeAncestors(ArrayList<ParseTreeNode> ancestors, ParseTreeNode node) {
	// if (node.parent != null) {
	// ancestors.add(node.parent);
	// computeAncestors(ancestors, node.parent);
	// }
	// }
	//
	// /**
	// * Compare two nodes to see if they represent the same information.
	// */
	// public boolean equals(Object thing) {
	// if (!(thing instanceof ParseTreeNode))
	// return false;
	// ParseTreeNode other = (ParseTreeNode) thing;
	// if (other.id != id)
	// return false;
	// if (other.governor != governor)
	// return false;
	// if (other.dependent != dependent)
	// return false;
	// if ((other.relation == null) && (relation == null))
	// return true;
	// if ((other.relation == null) || (relation == null))
	// return false;
	// return (other.relation.equalsIgnoreCase(relation));
	// }
	//
	// /**
	// * Print out this node in a human-readable format.
	// *
	// * @param stream
	// * The output stream.
	// * @param indent
	// * A prety-printing indent.
	// */
	// public void print(PrintStream stream, int indent) {
	// for (int i = 0; i < indent; i++)
	// stream.print(" ");
	// stream.print(id);
	// stream.print(" ");
	// stream.print(text);
	// stream.print(" - ");
	// stream.print(relation);
	// stream.print(" (");
	// stream.print(governor);
	// stream.print(",");
	// stream.print(dependent);
	// stream.println(")");
	// if (children != null) {
	// for (ParseTreeNode node : children) {
	// node.print(stream, indent + 2);
	// }
	// }
	// }
	// }
}
