package edu.uchsc.ccp.opendmap.dependency;

import java.io.PrintStream;
import java.util.ArrayList;

/**
 * Helper class to hold the Stanford Phrase Tree
 * <p>
 * The phrase tree is constructed from the Stanford Parser output and highlights the dependencies between tokens. In
 * particular, the hierarchy represents a set of phrases that the token belongs to. <br>
 * <br>
 * NOTE: In fixing the dependency bug, the most significant changes were made to this class.<br>
 * 1) The governs(int governor, int dependent) method was mislabeled. It was actually determining if the dependent node
 * is a child of the governing node instead of whether there is a dependency relation where one is governed by the
 * other. <br>
 * To fix, this method was revised so that it returns true if there is a relation whereby the dependent node is governed
 * by the governor node, and false otherwise.<br>
 * 2) The governs(int governor, int dependent) method was use in a number of places (in particular in
 * lowestCommonAncestor()). The fix described in (1) broke lowestCommonAncestor. <br>
 * To fix, a new method was created called hasChild(int parentNodeID, int possibleChildNodeID). This method has the
 * exact same functionality as the original governs() method had. It returns true if the parent node is an ancestor of
 * the possibleChildNode. Where governs() was used previously in lowestCommonAncestor(), hasChild() is now used.<br>
 * 3) There was a method called governsAll(ParseTreeNode node, int children[]). The name once again is misleading. The
 * function of this method was to determine if the given node was an ancestor to all of the given children.<br>
 * To Fix, this method has been renamed hasAllChildren(), and the governs() method was replaced with hasChild(). <br>
 * 4) Removed the subsumes method. I don't quite understand what he was trying to do with subsumes(), and it doesn't
 * seem to be working properly. Perhaps with more examples we can put subsumes() back in and be confident that it is
 * working as intended.
 * 
 * @author R. James Firby
 */
public class DependencyParseTree {

	ArrayList<ParseTreeNode> roots = new ArrayList<ParseTreeNode>();

	ArrayList<ParseTreeNode> nodes = new ArrayList<ParseTreeNode>();

	/**
	 * Add a new dependency relationship to the phrase graph.
	 * 
	 * @param id
	 *            The identity of this relationship. (It's phrase group number)
	 * @param governor
	 *            The identity of the governor of this phrase.
	 * @param relation
	 *            The type of relationship between the governor and the dependent.
	 * @param dependent
	 *            The identiy of the dependent node in this phrase.
	 * @param text
	 *            The text string of this phrase.
	 */
	public void addRelation(int id, int governor, String relation, int dependent, String text) {
		ParseTreeNode newNode = new ParseTreeNode(id, governor, dependent, relation, text);
		boolean found = false;
		for (ParseTreeNode node : nodes) {
			if (node.equals(newNode)) {
				found = true;
				break;
			}
		}
		if (!found)
			nodes.add(newNode);
	}

	/**
	 * Calculate all the connected subtress in the graph and cache them.
	 */
	public void finalize() {
		for (ParseTreeNode node : nodes) {
			ArrayList<ParseTreeNode> parents = findGovernors(node.id);
			if (parents.isEmpty()) {
				roots.add(node);
			} else {
				for (ParseTreeNode parent : parents) {
					parent.addChild(node);
				}
			}
		}
	}

	/**
	 * Check whether a particular node/token governs another. <br>
	 * <br>
	 * NOTE: All this is really checking is if the governor is an ancestor of the dependent... doesn't seem right to me.
	 * 
	 * @param governor
	 *            The identity of the possible governor.
	 * @param dependent
	 *            The identify of the possible dependent.
	 * @return True if the dependent depends on the governor.
	 */
	public boolean governs(int governor, int dependent) {
		//System.out.print(governor + " governs " + dependent + "? ");
		for (ParseTreeNode node : nodes) {
			if (node.id == governor) {
				// if (node.hasChild(dependent)) {
				if (node.dependent == dependent) {
					//System.out.println("true");
					return true;
				}
			}
		}
		//System.out.println("false");
		return false;
	}

	/**
	 * Checks whether a particular node is an ancestor of the other
	 * 
	 * @param parentNodeID
	 * @param possibleChildNodeID
	 * @return
	 */
	boolean hasChild(int parentNodeID, int possibleChildNodeID) {
		//System.out.print(parentNodeID + " hasChild " + possibleChildNodeID + "? ");
		for (ParseTreeNode node : nodes) {
			if (node.id == parentNodeID) {
				if (node.hasChild(possibleChildNodeID)) {
					//System.out.println("true");
					return true;
				}
			}
		}
		//System.out.println("false");
		return false;
	}

	/**
	 * Find the lowest governor of all the dependents supplied.
	 * 
	 * @param children
	 *            The identified of the dependent children.
	 * @return The common ancestor (governor) of all the children.
	 */
	public int lowestCommonAncestor(int children[]) {
		//System.out.println("LowestCommonAncestor over children: " + Arrays.toString(children));
		if (children.length < 1) {
			//System.out.println("LowestCommonAncestor returning -1, children.length < 1");
			return -1;
		}
		// if (children.length == 1) return children[0];
		int key = children[0];
		int best = -1;
		for (ParseTreeNode node : nodes) {
			if (node.id == key) {
				int ancestor = lowestCommonAncestor(node, children);
				if (ancestor != -1) {
					// if ((best == -1) || governs(best, ancestor)) {
					if ((best == -1) || hasChild(best, ancestor)) {
						best = ancestor;
					}
				}
			}
		}
		//System.out.println("LowestCommonAncestor returning " + best);
		return best;
	}

	/**
	 * Find the lowest common ancestor of all the children that has the given node as a child.
	 * 
	 * @param node
	 *            The node that must be a child.
	 * @param children
	 *            The identity of all the children.
	 * @return The identity of the lowest common ancestor of the children looking up from node.
	 */
	private int lowestCommonAncestor(ParseTreeNode node, int children[]) {
		ParseTreeNode lowest = null;
		if (hasAllChildren(node, children)) {
			lowest = node;
		} else {
			for (ParseTreeNode ancestor : node.ancestors()) {
				if (hasAllChildren(ancestor, children)) {
					lowest = ancestor;
					break;
				}
			}
		}
		if (lowest == null) {
			return -1;
		} else {
			// lowest = backupAncestor(lowest);
			return lowest.id;
		}
	}

	/**
	 * Check whether a node is an ancestor to all children in the input array.
	 * 
	 * @param node
	 *            The governing node.
	 * @param children
	 *            The identities of the children.
	 * @return True if the governing node really governs all the children.
	 */
	private boolean hasAllChildren(ParseTreeNode node, int children[]) {
		boolean governsAll = true;
		for (int i = 1; i < children.length; i++) {
			// if (!governs(node.id, children[i])) {
			if (!hasChild(node.id, children[i])) {
				governsAll = false;
				break;
			}
		}
		return governsAll;
	}

	/**
	 * Backup the ancestor tree to on some types of Stanford Parser relations to find the true governing phrase.
	 * 
	 * @param id
	 *            The identity of the phrase to check.
	 * @return The tru governing phrase.
	 */
	public int backupAncestor(int id) {
		// Find the node corresponding to the given id
		ParseTreeNode node = null;
		for (ParseTreeNode n : nodes) {
			if (n.id == id) {
				node = n;
				break;
			}
		}
		if (node == null)
			return id;
		// Look for an ancestor that is the "head" of a phrase that this node belongs to.
		// We really want the relationships between "head" components, not individual tokens.
		ParseTreeNode best = node;
		for (ParseTreeNode parent : node.ancestors()) {
			boolean okay = false;
			for (ParseTreeNode child : parent.children) {
				if (child.id == best.id) {
					// // If the parent is the real head of a phrase that child is part of, backup.
					// if (subsumes(parent, child)) {
					// best = parent;
					// okay = true;
					// }
				}
			}
			if (!okay)
				break;
		}
		//System.out.println("BackupAncestor returning: " + best.id);
		return best.id;
	}

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
	// System.out.print("Checking subsumption (parent is head of a phrase that child is part of) Parent: " + parent.id +
	// " Child: "
	// + child.id + " -- ");
	//
	// if ((child.governor == parent.id) && child.relation.equalsIgnoreCase("nn")) {
	// // If the child is part of a noun phrase, (ie. "nn") then
	// // we really want the head noun. So, backup.
	// System.out.println("Subsumption check = true (nn)");
	// return true;
	// } else if ((child.governor == parent.id) && child.relation.equalsIgnoreCase("dep")) {
	// // If the child is a dependent subclause, then look for the root clause
	// System.out.println("Subsumption check = true (dep)");
	// return true;
	// } else if ((child.governor == parent.id) && child.relation.equalsIgnoreCase("partmod")) {
	// // If the child is modified by a participle, then look for the root clause
	// System.out.println("Subsumption check = true (partmod)");
	// return true;
	// } else if ((child.governor == parent.id) && child.relation.equalsIgnoreCase("conj")) {
	// // If the child is part of a conjunct, then look for the root clause
	// System.out.println("Subsumption check = true (conj)");
	// return true;
	// } else if ((child.governor == parent.id) && child.relation.equalsIgnoreCase("ccomp")) {
	// // If the child is part of a conjunct, then look for the root clause
	// System.out.println("Subsumption check = true (ccomp)");
	// return true;
	// // }else if ((child.governor == parent.id) && child.relation.equalsIgnoreCase("nsubj")) {
	// // // If the child is part of a conjunct, then look for the root clause
	// // System.out.println("Subsumption check = true");
	// // return true;
	// // } else if ((child.governor == parent.id) && child.relation.equalsIgnoreCase("dobj")) {
	// // // If the child is part of a direct object, then look for the root clause
	// // return true;
	// } else {
	// System.out.println("Subsumption check = FALSE");
	// return false;
	// }
	// }

	/**
	 * Check for Stanford Parser tokens that we should ignore.
	 * 
	 * @param id
	 *            The id of the Stanford Parser token.
	 * @return True if we should include this token in the phrase graph.
	 */
	public boolean isRelevantToken(int id) {
		// Ignore "num" nodes because they are almost always labelled wrong
		for (int i = 0; i < nodes.size(); i++) {
			ParseTreeNode node = nodes.get(i);
			if (node.id == id) {
				if (node.relation.equalsIgnoreCase("num"))
					return false;
			}
		}
		return true;
	}

	/**
	 * Find all of the governors of a Stanford Parser token.
	 * 
	 * @param id
	 *            The token id.
	 * @return The governor nodes (akk parents from all partial subtrees).
	 */
	private ArrayList<ParseTreeNode> findGovernors(int id) {
		ArrayList<ParseTreeNode> governors = new ArrayList<ParseTreeNode>();
		for (ParseTreeNode node : nodes) {
			if ((node.id != id) && (node.dependent == id))
				governors.add(node);
		}
		return governors;
	}

	/**
	 * Print the parse tree in human readable form.
	 * 
	 * @param stream
	 */
	public void print(PrintStream stream) {
		if (roots.isEmpty()) {
			stream
					.println("No roots in parse tree. This means that there is no typed dependency information available for use by OpenDMAP!!! ");
		} else {
			for (ParseTreeNode root : roots) {
				root.print(stream, 0);
			}
		}
	}

	/**
	 * Helper class to represent a single Phrase Tree Node.
	 * 
	 * @author R. James Firby
	 */
	private class ParseTreeNode {

		private int id = -1;

		private int governor = -1;

		private int dependent = -1;

		private String relation = null;

		private String text = null;

		/* The direct parent of this node */
		private ParseTreeNode parent = null;

		/* All of the children of this node */
		private ArrayList<ParseTreeNode> children = null;

		/* A cached list of all ancestors of this node */
		private ArrayList<ParseTreeNode> ancestors = null;

		/**
		 * Create a new parse tree node.
		 * 
		 * @param id
		 *            The id of this node/phrase/token.
		 * @param governor
		 *            The id of the governing token.
		 * @param dependent
		 *            The id of the dependent token.
		 * @param relation
		 *            The Stanford Parser relationship between these tokens.
		 * @param text
		 *            The text of this token.
		 */
		public ParseTreeNode(int id, int governor, int dependent, String relation, String text) {
			this.id = id;
			this.governor = governor;
			this.dependent = dependent;
			this.relation = relation;
			this.text = text;
		}

		/**
		 * Add a child node to this node.
		 * 
		 * @param node
		 *            The child to add.
		 */
		public void addChild(ParseTreeNode node) {
			ancestors = null;
			if (children == null)
				children = new ArrayList<ParseTreeNode>();
			children.add(node);
			node.parent = this;
		}

		/**
		 * Check whether this node has a child with the given id.
		 * 
		 * @param childId
		 *            The id of the child node.
		 * @return True if this node has such a child.
		 */
		private boolean hasChild(int childId) {
			if (id == childId)
				return true;
			if (children == null)
				return false;
			for (ParseTreeNode child : children) {
				if (child.hasChild(childId)) {
					return true;
				}
			}
			return false;
		}

		/**
		 * Compute all of the ancestors for this node and cache them away.
		 * 
		 * @return All ancestors for this node.
		 */
		public ArrayList<ParseTreeNode> ancestors() {
			if (ancestors == null) {
				ancestors = new ArrayList<ParseTreeNode>();
				computeAncestors(ancestors, this);
			}
			return ancestors;
		}

		private void computeAncestors(ArrayList<ParseTreeNode> ancestors, ParseTreeNode node) {
			if (node.parent != null) {
				ancestors.add(node.parent);
				computeAncestors(ancestors, node.parent);
			}
		}

		/**
		 * Compare two nodes to see if they represent the same information.
		 */
		public boolean equals(Object thing) {
			if (!(thing instanceof ParseTreeNode))
				return false;
			ParseTreeNode other = (ParseTreeNode) thing;
			if (other.id != id)
				return false;
			if (other.governor != governor)
				return false;
			if (other.dependent != dependent)
				return false;
			if ((other.relation == null) && (relation == null))
				return true;
			if ((other.relation == null) || (relation == null))
				return false;
			return (other.relation.equalsIgnoreCase(relation));
		}

		/**
		 * Print out this node in a human-readable format.
		 * 
		 * @param stream
		 *            The output stream.
		 * @param indent
		 *            A prety-printing indent.
		 */
		public void print(PrintStream stream, int indent) {
			for (int i = 0; i < indent; i++)
				stream.print(" ");
			stream.print(id);
			stream.print(" ");
			stream.print(text);
			stream.print(" - ");
			stream.print(relation);
			stream.print(" (");
			stream.print(governor);
			stream.print(",");
			stream.print(dependent);
			stream.println(")");
			if (children != null) {
				for (ParseTreeNode node : children) {
					node.print(stream, indent + 2);
				}
			}
		}
	}
}
