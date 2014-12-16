/* *********************************************************************************
 * The contents of this file are subject to the Mozilla Public License Version 1.1 *
 * (the "License"); you may not use this file except in compliance with the        *
 * License. You may obtain a copy of the License at http://www.mozilla.org/MPL/    *
 *                                                                                 *
 * Software distributed under the License is distributed on an "AS IS" basis,      *
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for    *
 * the specific language governing rights and limitations under the License.       *
 *                                                                                 *
 * The Original Code is OpenDMAP code, released August 22, 2007.                   *
 *                                                                                 *
 * The Initial Developer of the Original Code is the Center for Computational      *
 * Pharmacology, at the University of Colorado School of Medicine. Portions        *
 * created by the Center for Computational Pharmacology are Copyright (C) 2007     *
 * University of Colorado. All Rights Reserved.                                    *
 *                                                                                 *
 * *********************************************************************************/
package edu.uchsc.ccp.opendmap;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;
import java.util.logging.Level;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.uchsc.ccp.opendmap.pattern.ASTBaseElement;
import edu.uchsc.ccp.opendmap.pattern.ASTDMAPRule;
import edu.uchsc.ccp.opendmap.pattern.ASTDMAPRuleSet;
import edu.uchsc.ccp.opendmap.pattern.ASTEntry;
import edu.uchsc.ccp.opendmap.pattern.ASTPath;
import edu.uchsc.ccp.opendmap.pattern.ASTPattern;
import edu.uchsc.ccp.opendmap.pattern.ASTPatternComponent;
import edu.uchsc.ccp.opendmap.pattern.ASTPatterns;
import edu.uchsc.ccp.opendmap.pattern.ASTSlotName;
import edu.uchsc.ccp.opendmap.pattern.DMAPPatternParser;
import edu.uchsc.ccp.opendmap.pattern.Node;
import edu.uchsc.ccp.opendmap.pattern.ParseException;
import edu.uchsc.ccp.opendmap.pattern.SimpleNode;

/**
 * Processor for DMAP recognition patterns.
 * <p>
 * This class uses an AST parser created with JJTree to generate an AST tree structure for each text pattern. It then
 * converts that tree into a DMAP pattern structure that can be used in predictions.
 * 
 * @author Will Fitzgerald
 * @author R. James Firby
 */

/*
 * Changes (most recent first)
 * 
 * 02-01-06 rjf - Changed slot path handling to be property annotations. 01-05-06 rjf - Added @ clauses. 09-30-05 rjf -
 * Fixed more error messages. 09-29-05 rjf - Added slot path handling 09-27-05 rjf - Fixed bug preventing slots from
 * appearing in patterns on Protege SimpleInstances. 09-25-05 rjf - Added some better error messages for bad patterns.
 */
public class ASTConverter {

	/* The pattern parser, one instantiated */
	private static DMAPPatternParser patternParser = null;
	/* Whether or not test items should be case-sensitive */
	private static boolean retainCase = true;

	/**
	 * Initialze the pattern parser to read from the given string and create patterns that do or do not match using
	 * case.
	 * 
	 * @param ruleString
	 *            The input string to read patterns from.
	 * @param retainTextCase
	 *            Whether or not to retain case when matching test items.
	 */
	private static void initialize(String ruleString, boolean retainTextCase) {
		StringReader sr = new StringReader(ruleString);
		Reader r = new BufferedReader(sr);
		initialize(r, retainCase);
	}

	/**
	 * Initialze the pattern parser to read from the given <code>Reader</code> and create patterns that do or do not
	 * match using case.
	 * 
	 * @param r
	 *            The reader to read patterns from.
	 * @param retainTextCase
	 *            Whether or not to retain case when matching test items.
	 */
	@SuppressWarnings("static-access")
	private static void initialize(Reader r, boolean retainTextCase) {
		if (patternParser == null) {
			patternParser = new DMAPPatternParser(r);
		} else {
			patternParser.ReInit(r);
		}
		retainCase = retainTextCase;
	}

	/**
	 * Read recognition patterns for concepts in the knowledge base from a file or string and add them to the parser.
	 * 
	 * @param parser
	 *            The parser to get the patterns
	 * @param kb
	 *            The Protege Knowledge Base holding the concepts named by the patterns
	 * @param input
	 *            A Reader for the string containing the named rules
	 * @param retainCase
	 *            True if the pattern rules should match the case of words, False if case should be ignored.
	 * @param level
	 *            The level at which to log messages
	 * @throws ParseException
	 *             Means that a pattern doesn't parse
	 */
	@SuppressWarnings("static-access")
	static void addPredictionsFromRules(Parser parser, KnowledgeBase kb, Reader input, boolean retainCase, Level level)
			throws ParseException {
		// The string will contain a whole set of rules
		// First, parse them into a rule set
		initialize(input, retainCase);
		ASTDMAPRuleSet rules = patternParser.DMAPRuleSet();
		// Second, cycle through each rule and create patterns for it
		if (rules.jjtGetNumChildren() > 0) {
			for (int i = 0; i < rules.jjtGetNumChildren(); ++i) {
				Object ruleOrComment = rules.jjtGetChild(i);
				if (ruleOrComment instanceof ASTDMAPRule) {
					ASTDMAPRule rule = (ASTDMAPRule) ruleOrComment;
					addRule(parser, rule, kb, level);
				}
			}
		}
	}

	/**
	 * Process a single rule. A rule will be set of patterns.
	 * 
	 * Each pattern found will be added to the parser as an anytime prediction.
	 * 
	 * @param parser
	 *            The parser to get the patterns
	 * @param rule
	 *            The rule to be processed
	 * @param kb
	 *            The Protege knowledge base
	 * @param level
	 *            THe logging level to use while parsing this rule
	 */
	private static void addRule(Parser parser, ASTDMAPRule rule, KnowledgeBase kb, Level level) throws ParseException {
		PredictionManager manager = parser.getPredictionManager();
		ProtegeProjectGroup group = parser.getProtegeProjectGroup();
		// Pull out the Protege frame this rule applies to, its name is retained as the 'base'
		Frame baseF = kb.getFrame(rule.getBase());
		if (baseF == null)
			throw new ParseException("Pattern Error: No Protege frame named '" + rule.getBase() + "'");
		ProtegeFrameItem base = new ProtegeFrameItem(baseF, group);
		// A rule is a set of patterns
		if (rule.jjtGetNumChildren() > 0) {
			for (int i = 0; i < rule.jjtGetNumChildren(); ++i) {
				// Process each pattern individually
				ASTPatterns patterns = (ASTPatterns) rule.jjtGetChild(i);
				for (int k = 0; k < patterns.jjtGetNumChildren(); k++) {
					ASTPattern pat = (ASTPattern) patterns.jjtGetChild(k);
					Pattern dmapPattern = processPattern(pat, baseF, kb, group);
					// Log the pattern, if desired
					/* TODO: Fix logging */
					// parser.log(level, base.toPatternString() + " := " + dmapPattern + ";");
					// Add the pattern to the anytime predictions table
					manager.addAnytimePrediction(base, dmapPattern);
				}
			}
		}
	}

	/**
	 * Process a single pattern. A pattern is a set of ASTPatternComponents.
	 * 
	 * @param pat
	 *            The pattern to process
	 * @param baseF
	 *            The Protege frame to be recognized with this pattern
	 * @param kb
	 *            The Protege knowledge base
	 * @param group
	 *            The Protege project group this pattern will be part of
	 * @return A DMAP pattern
	 */
	private static Pattern processPattern(ASTPattern pat, Frame baseF, KnowledgeBase kb, ProtegeProjectGroup group) throws ParseException {
		if (pat.jjtGetNumChildren() == 1) {
			// There is only one pattern component
			ASTPatternComponent component = (ASTPatternComponent) pat.jjtGetChild(0);
			return processPatternList(component, Pattern.Operator.SEQUENCE, baseF, kb, group);
		} else if (pat.jjtGetNumChildren() > 1) {
			// The first pattern component is the main pattern
			ASTPatternComponent component = (ASTPatternComponent) pat.jjtGetChild(0);
			Pattern mainClause = processPatternList(component, Pattern.Operator.SEQUENCE, baseF, kb, group);
			mainClause.setAsMainClause(true);
			// The rest of the patterns are sub phrases
			Vector<Pattern> clauseSet = new Vector<Pattern>(pat.jjtGetNumChildren());
			clauseSet.add(mainClause);
			for (int i = 1; i < pat.jjtGetNumChildren(); ++i) {
				component = (ASTPatternComponent) pat.jjtGetChild(i);
				clauseSet.add(processPatternList(component, Pattern.Operator.SEQUENCE, baseF, kb, group));
			}
			// Create the pattern
			return new Pattern(Pattern.Operator.CLAUSESET, clauseSet);
		} else {
			// The pattern is empty
			return null;
		}
	}

	private static Pattern processPatternList(SimpleNode entry, Pattern.Operator op, Frame baseF, KnowledgeBase kb,
			ProtegeProjectGroup group) throws ParseException {
		// An entry that is an implied sequence
		if (entry.jjtGetNumChildren() > 1) {
			// Process each entry and add it to the sequence
			Vector<Pattern> items = new Vector<Pattern>();
			for (int i = 0; i < entry.jjtGetNumChildren(); ++i) {
				ASTEntry pitem = (ASTEntry) entry.jjtGetChild(i);
				Pattern item = processPatternItem(pitem, baseF, kb, group);
				if (item != null) {
					items.addElement(item);
				}
			}
			// Return the items as the expected pattern type, or as a single item
			if (items.size() > 1) {
				return new Pattern(op, items);
			} else if (items.size() == 1) {
				return (Pattern) items.elementAt(0);
			} else {
				return null;
			}
		} else if (entry.jjtGetNumChildren() == 1) {
			// It is just a single entry, don't build the vector
			return processPatternItem((ASTEntry) entry.jjtGetChild(0), baseF, kb, group);
		} else {
			// The pattern is empty
			return null;
		}
	}

	/**
	 * Process a single ASTEntry into a DMAP pattern. An entry will include a collection of children that will also be
	 * entries, except for a BASE entry. A BASE entry holds a single ASTBaseElement.
	 * 
	 * @param entry
	 *            The entry to be processed
	 * @param baseF
	 *            The Protege frame to be recognized
	 * @param kb
	 *            The Protege knowledge base
	 * @param group
	 *            The Protege project group this pattern will be part of
	 * @return A DMAP pattern for the entry
	 */
	private static Pattern processPatternItem(ASTEntry entry, Frame baseF, KnowledgeBase kb, ProtegeProjectGroup group)
			throws ParseException {
		// An entry contains its operator type tagged by jjtree
		Pattern.Operator entryType = entry.getOperatorType();
		// Compress the hierarchy somewhat
		if (entryType == Pattern.Operator.SINGLE) {
			// A single entry holds one entry, process that instead
			return processPatternItem((ASTEntry) entry.jjtGetChild(0), baseF, kb, group);
		} else if (entryType == Pattern.Operator.BASE) {
			// Create a base pattern item
			return newBasePatternItem(entry, baseF, kb, group);
		} else if (entryType == Pattern.Operator.SEQUENCE) {
			// An explicit sequence pattern, process each child
			return processPatternList(entry, entryType, baseF, kb, group);
		} else if (entryType == Pattern.Operator.ALTERNATION) {
			// An explicit sequence pattern, process each child
			return processPatternList(entry, entryType, baseF, kb, group);
		} else if (entry.jjtGetNumChildren() == 1) {
			// All other operators should have a single child
			Pattern item = processPatternItem((ASTEntry) entry.jjtGetChild(0), baseF, kb, group);
			if (item != null) {
				return new Pattern(entry.getOperatorType(), item);
			} else {
				return null;
			}
		} else {
			// Anything else is an error.
			throw new Error("Pattern error: No support for " + entryType + " with " + entry.jjtGetNumChildren() + " children.");
		}
	}

	/**
	 * Create a new BASE pattern item.
	 * 
	 * @param entry
	 *            The entry holding the BASE element
	 * @param baseF
	 *            The Protege frame to be recognized
	 * @param kb
	 *            The Protege knowledge base
	 * @param group
	 *            The Protege project group this pattern will part of
	 * @return A new DMAP BASE pattern
	 */
	private static Pattern newBasePatternItem(ASTEntry entry, Frame baseF, KnowledgeBase kb, ProtegeProjectGroup group)
			throws ParseException {
		// Build a new pattern item
		Pattern item = null;
		// This entry should hold one base element entry
		int nChildren = entry.jjtGetNumChildren();
		if (nChildren != 1) {
			// If the jjtree grammar is correct, this should NEVER happen
			throw new Error("Pattern error: Base entry with " + nChildren + " children.");
		} else {
			// Package up the base element and add it to the item
			ASTBaseElement base = (ASTBaseElement) entry.jjtGetChild(0);
			DMAPItem dmapItem = pcontent(base, baseF, kb, group);
			if (dmapItem != null)
				item = new Pattern(dmapItem);
		}
		// Return the new pattern item
		return item;
	}

	/**
	 * Turn an ASTBaseElement into a DMAP item that can be recognized by the parser.
	 * 
	 * @param element
	 *            The AST base element
	 * @param baseF
	 *            The Protege frame to be recognized
	 * @param kb
	 *            The Protege knowledge base
	 * @param group
	 *            The Protege project group this item will be part of
	 * @return A recognizable DMAP item
	 */
	private static DMAPItem pcontent(ASTBaseElement element, Frame baseF, KnowledgeBase kb, ProtegeProjectGroup group)
			throws ParseException {
		// A base element retains the text that matched it as its "Image"
		// It also retains the kind of element it is as its "EntryType"
		String image = element.getImage();
		switch (element.getEntryType()) {
		case STRING:
			// This is a plain text item
			if ((image == null) || (image.length() == 0))
				return null;
			if (retainCase) {
				return new TextItem(TextItem.unescapeString(image));
			} else {
				return new TextItem(TextItem.unescapeString(image).toLowerCase());
			}
		case REGEX:
			// This is a regular expression pattern
			if ((image == null) || (image.length() == 0))
				return null;
			return new RegexItem(image);
		case FRAME_NAME:
			// This is a Protege frame name. Pull out the actual frame.
			Frame frame = kb.getFrame(image);
			return new ProtegeFrameItem(frame, group);
		case PATH:
			// This is a slot reference, it should name a slot and may contain property annotations.
			// Pull out the actual Protege slot being named.
			// A PATH base element should contain exactly one child
			/* TODO: Add multiple slot path support. */
			int nPaths = element.jjtGetNumChildren();
			if (nPaths != 1) {
				// With a proper jjtree grammar this should NEVER happen
				throw new Error("Pattern error: Slot PATH entry with " + nPaths + " children.");
			} else {
				// A PATH should contain a number of steps, each will be an ASTSlotName
				ASTPath path = (ASTPath) element.jjtGetChild(0);
				int nSteps = path.jjtGetNumChildren();
				if (nSteps >= 1) {
					int firstPathStep = 1;
					// The first step might be the 'denotation' tag
					boolean isDenotation = false;
					ASTSlotName slotName = (ASTSlotName) path.jjtGetChild(0);
					String slotNameString = slotName.getName();
					if ((slotNameString != null) && (slotNameString.equals(":"))) {
						// This labels the slot as a 'denotation' holder, skip this marker
						isDenotation = true;
						firstPathStep = 2;
						if (nSteps < 2) {
							throw new Error("Pattern error: Slot PATH entry with no children.");
						} else {
							slotName = (ASTSlotName) path.jjtGetChild(1);
						}
					}
					Slot slot = kb.getSlot(slotName.getName());
					if (slot == null) {
						throw new ParseException("Pattern error: Protege frame '" + baseF.getBrowserText() + "' has no slot named '"
								+ slotName.getName() + "'");
					}
					ProtegeSlotItem pSlot = null;
					if (nSteps == firstPathStep) {
						// The default, a simple slot definition
						pSlot = new ProtegeSlotItem(slot, baseF, group, isDenotation);
					} else {
						// A multi-step slot path, [slot-name constraint constraint ...]
						// where constraint is a property to expect when filling this slot
						ArrayList<String> constraints = new ArrayList<String>();
						for (int i = firstPathStep; i < nSteps; i++) {
							ASTSlotName psName = (ASTSlotName) path.jjtGetChild(i);
							constraints.add(psName.getName());
						}
						pSlot = new ProtegeSlotItem(slot, baseF, group, isDenotation, constraints);
					}
					return pSlot;
				}
			}
			return null;
		}
		// No other base element type currently support
		throw new Error("Pattern Error: Unsupported base element type " + element.getEntryType());
	}

	/**
	 * Read recognition patterns for concepts in the knowledge base from a string and return them as DMAP patterns.
	 * 
	 * @param kb
	 *            The Protege Knowledge Base holding the concepts named by the patterns
	 * @param ruleString
	 *            The string containing the named rules
	 * @param retainCase
	 *            True if the pattern should match case, False if case should be ignored.
	 * @param group
	 *            The Protege project group these patterns will reference
	 * @return The patterns
	 * @throws ParseException
	 *             Means that a pattern doesn't parse
	 */
	@SuppressWarnings("static-access")
	public static ArrayList<Pattern> readPatternsFromString(KnowledgeBase kb, String ruleString, boolean retainCase,
			ProtegeProjectGroup group) throws ParseException {
		ArrayList<Pattern> results = new ArrayList<Pattern>();
		// The string will contain a whole set of rules
		// First, parse them into a rule set
		initialize(ruleString, retainCase);
		ASTDMAPRuleSet rules = patternParser.DMAPRuleSet();
		// Second, cycle through each rule and create patterns for it
		if (rules.jjtGetNumChildren() > 0) {
			for (int i = 0; i < rules.jjtGetNumChildren(); ++i) {
				ASTDMAPRule rule = (ASTDMAPRule) rules.jjtGetChild(i);
				readRule(rule, kb, results, group);
			}
		}
		return results;
	}

	/**
	 * Similar implementation to readPatternsFromString (above) but this method does not try to call new
	 * DMAPPatternParser, it instead uses DMAPPatternParser.ReInit(Reader) and thus avoids the error about calling the
	 * constructor of a static class a second time. This method is used by the OpenDMAP pattern editor when validating
	 * the pattern files.
	 * 
	 * @param kb
	 * @param patternString
	 * @param retainTextCase
	 * @param group
	 * @return
	 * @throws ParseException
	 */
	public static ArrayList<Pattern> readPatternsFromStringUseReInit(KnowledgeBase kb, String patternString, boolean retainTextCase,
			ProtegeProjectGroup group) throws ParseException {
		ArrayList<Pattern> results = new ArrayList<Pattern>();

		// Check for simple error
		if ((patternString == null) || (patternString.trim().equals(""))) {
			throw new ParseException("Empty patterns are not allowed.");
		}
		// The string will contain a single pattern
		StringReader sr = new StringReader(patternString);
		Reader r = new BufferedReader(sr);
		DMAPPatternParser.ReInit(r);
		retainCase = retainTextCase;

		ASTDMAPRuleSet rules = DMAPPatternParser.DMAPRuleSet();
		// Second, cycle through each rule and create patterns for it
		if (rules.jjtGetNumChildren() > 0) {
			for (int i = 0; i < rules.jjtGetNumChildren(); ++i) {
				Object possibleRule = rules.jjtGetChild(i);
				if (possibleRule instanceof ASTDMAPRule) {
					ASTDMAPRule rule = (ASTDMAPRule) possibleRule;
					readRule(rule, kb, results, group);
				} else {
					System.err.println("Expected ASTDMAPRule but observed a " + possibleRule.getClass().getName());
				}
			}
		}
		return results;

	}

	/**
	 * Read a single rule. A rule will be set of patterns.
	 * 
	 * Each pattern found will be added to the result list.
	 * 
	 * @param rule
	 *            The rule to be processed
	 * @param kb
	 *            The Protege knowledge base
	 * @param results
	 *            The patterns to read into
	 * @param group
	 *            The Protege project group this rule will be part of
	 */
	private static void readRule(ASTDMAPRule rule, KnowledgeBase kb, Collection<Pattern> results, ProtegeProjectGroup group)
			throws ParseException {
		// Pull out the Protege frame this rule applies to, its name is retained as the 'base'
		String baseName = rule.getBase();
		if (baseName == null)
			throw new Error("Pattern rule must include target frame.");
		Frame baseF = kb.getFrame(rule.getBase());
		// A rule is a set of patterns
		if (rule.jjtGetNumChildren() > 0) {
			for (int i = 0; i < rule.jjtGetNumChildren(); ++i) {
				// Process each pattern individually
				ASTPatterns patterns = (ASTPatterns) rule.jjtGetChild(i);
				for (int k = 0; k < patterns.jjtGetNumChildren(); k++) {
					ASTPattern pat = (ASTPattern) patterns.jjtGetChild(k);
					Pattern dmapPattern = processPattern(pat, baseF, kb, group);
					// Add the pattern
					results.add(dmapPattern);
				}
			}
		}
	}

	/**
	 * Read a single pattern from a string and return a DMAP <code>Pattern</code>.
	 * 
	 * @param patternString
	 *            The string holding the pattern
	 * @param kb
	 *            A Protege Knowledge base
	 * @param targetFrame
	 *            The name of the Protege frame this pattern is for
	 * @param retainCase
	 *            True if the pattern should match case, False if case should be ignored.
	 * @param group
	 *            The Protege project group this pattern will reference
	 * @return The object encoding this pattern for DMAP
	 * @throws ParseException
	 *             If there is a syntax error in the pattern string
	 */
	@SuppressWarnings("static-access")
	public static Pattern readPatternFromString(String patternString, KnowledgeBase kb, String targetFrame, boolean retainCase,
			ProtegeProjectGroup group) throws ParseException {
		// Check for simple error
		if ((patternString == null) || (patternString.trim().equals(""))) {
			throw new ParseException("Empty patterns are not allowed.");
		}
		// The string will contain a single pattern
		initialize(patternString, retainCase);
		ASTPattern pattern = null;
		try {
			pattern = patternParser.Pattern();
		} catch (ParseException e) {
			throw new ParseException("Error parsing '" + patternString + "': " + e.toString());
		}
		// Grab out the pattern target
		Frame baseF = null;
		if (targetFrame != null) {
			baseF = kb.getFrame(targetFrame);
		}
		return processPattern(pattern, baseF, kb, group);
	}

	/**
	 * Print out the syntactic parse tree for a pattern rule string. For debugging purposes only.
	 * 
	 * @param ruleString
	 *            The string holdsing the pattern rules.
	 * @param retainCase
	 *            True if the pattern should match case, False if case should be ignored.
	 * @throws ParseException
	 */
	@SuppressWarnings("static-access")
	public static void printPatternsFromString(String ruleString, boolean retainCase) throws ParseException {
		// The string will contain a whole set of rules
		// First, parse them into a rule set
		initialize(ruleString, retainCase);
		ASTDMAPRuleSet rules = patternParser.DMAPRuleSet();
		// Second, cycle through each rule and create a pattern for it
		if (rules.jjtGetNumChildren() > 0) {
			for (int i = 0; i < rules.jjtGetNumChildren(); ++i) {
				ASTDMAPRule rule = (ASTDMAPRule) rules.jjtGetChild(i);
				printRule(rule);
			}
		}
	}

	private static void printRule(ASTDMAPRule rule) {
		System.out.println("Rule for: <" + rule.getBase() + ">");
		if (rule.jjtGetNumChildren() > 0) {
			for (int i = 0; i < rule.jjtGetNumChildren(); ++i) {
				ASTPatterns patterns = (ASTPatterns) rule.jjtGetChild(i);
				for (int j = 0; j < patterns.jjtGetNumChildren(); j++) {
					ASTPattern pat = (ASTPattern) patterns.jjtGetChild(j);
					for (int k = 0; k < pat.jjtGetNumChildren(); ++k) {
						if (pat.jjtGetChild(k) instanceof ASTEntry) {
						printPatternEntry((ASTEntry) pat.jjtGetChild(k), 2);
						} else if (pat.jjtGetChild(k) instanceof ASTPatternComponent) {
							printPatternComponent((ASTPatternComponent) pat.jjtGetChild(k),2);
						} else {
							System.out.println("Error, class cast exception: expected ASTEntry or ASTPatternComponent but was: " + pat.jjtGetChild(k).getClass().getName());
						}
					}
				}
			}
		}
	}

	/**
	 * Added by Bill
	 * @param patternComponent
	 * @param indent
	 */
	private static void printPatternComponent(ASTPatternComponent patternComponent, int indent) {
		for (int i = 0; i < indent; i++)
			System.out.print(" ");
		for (int child=0; child < patternComponent.jjtGetNumChildren(); child++) {
			Node patternComponentChild = patternComponent.jjtGetChild(child);
			if (patternComponentChild instanceof ASTEntry) {
				printPatternEntry((ASTEntry) patternComponentChild,indent);
			} else {
				System.out.println("Found a non-entry in a pattern component: " + patternComponentChild.getClass().getName());
			}
			
		}
	}
	
	private static void printPatternEntry(ASTEntry entry, int indent) {
		for (int i = 0; i < indent; i++)
			System.out.print(" ");
		if (entry.getOperatorType() == Pattern.Operator.BASE) {
			System.out.print(entry.getOperatorType() + " ");
			for (int i = 0; i < entry.jjtGetNumChildren(); i++) {
				ASTBaseElement base = (ASTBaseElement) entry.jjtGetChild(i);
				printPatternBase(base);
			}
		} else {
			System.out.println(entry.getOperatorType());
			for (int i = 0; i < entry.jjtGetNumChildren(); i++) {
				ASTEntry base = (ASTEntry) entry.jjtGetChild(i);
				printPatternEntry(base, indent + 2);
			}
		}
	}

	private static void printPatternBase(ASTBaseElement element) {
		String image = element.getImage();
		switch (element.getEntryType()) {
		case STRING:
			if (image.length() == 0) {
				System.out.println("STRING: \"\"");
			} else {
				System.out.println("STRING: \"" + image + "\"");
			}
			return;
		case REGEX:
			System.out.println("REGEX: " + image);
			return;
		case FRAME_NAME:
			System.out.println("FRAME: " + image);
			return;
		case PATH:
			System.out.print("PATH:");
			for (int i = 0; i < element.jjtGetNumChildren(); i++) {
				ASTPath path = (ASTPath) element.jjtGetChild(i);
				for (int j = 0; j < path.jjtGetNumChildren(); j++) {
					ASTSlotName slotName = (ASTSlotName) path.jjtGetChild(j);
					System.out.print(" " + slotName.getName());
				}
			}
			System.out.println();
			return;
		}
		/* compiler not smart enough to know that these are the types? */
		System.out.println("OTHER: (" + element.getEntryType() + ") " + image);
	}

}
