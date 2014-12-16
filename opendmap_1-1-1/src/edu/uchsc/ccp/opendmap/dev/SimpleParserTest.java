/*
 * Created on Apr 3, 2005
 *
 */
package edu.uchsc.ccp.opendmap.dev;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;

import edu.uchsc.ccp.opendmap.Parser;
import edu.uchsc.ccp.opendmap.Reference;
import edu.uchsc.ccp.opendmap.pattern.ParseException;

import edu.stanford.smi.protege.model.*;

/**
 * @author Will Fitzgerald
 */
public class SimpleParserTest {

	private static final String PROJECT_FILE_NAME = 
		"test\\projects\\test\\test.pprj";
	private static final String PATTERN_FILE_NAME = 
		"test\\projects\\test\\test.patterns";
	private static final String PATTERN_FILE_NAME2 = "";

	private static final String INPUT_STRING = 
		"G H";
	
	public static void main(String[] args) throws ParseException{
		// Level.FINEST gives lots of detail
		// Level.FINER give a lot of detail
		// Level.FINE gives less
		Parser p = new Parser(Level.ALL);
		
		// Add recognition patterns to the parser
		Collection errors = new ArrayList();
		Project project = new Project(PROJECT_FILE_NAME, errors);
		if (errors.size() == 0) {
			KnowledgeBase kb = project.getKnowledgeBase();
//			String rules = 
//				  "{newspaper_00013}              := Mary; \n" 
//				+ "{newspaper_00020}              := Kelly; \n" 
//			//	+ "<Manager Supervision Relation> / <Manager> manages <Employee> ; \n"
//				+ "{Manager Supervision Relation} := I think [:FROM] manages [:TO]; \n "
//				;
			p.addPatternsFromFile(PATTERN_FILE_NAME, kb);
			if (!"".equals(PATTERN_FILE_NAME2)) p.addPatternsFromFile(PATTERN_FILE_NAME2, kb);
						
			// Parse a sentence
			p.parse(INPUT_STRING);
			
			// Print out all the references recognized 
			System.out.println("\nReferences from parser:");
			for (Reference reference: p.getReferences()) {
				System.out.println("  Parser saw: " + reference);
			}
			
			// Print out all the unsubsumed references recognized 
			System.out.println("\nUnsubsumed references from parser:");
			for (Reference reference: p.getUnsubsumedReferences()) {
				System.out.println("  Parser saw: " + reference);
			}
		}
	}
}
