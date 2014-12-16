/**
 * The OpenDMAP for Protege Project
 * December 2005
 */
package edu.uchsc.ccp.opendmap.dev;

import java.util.logging.Level;

import edu.uchsc.ccp.opendmap.Parser;
import edu.uchsc.ccp.opendmap.Reference;
import edu.uchsc.ccp.opendmap.configuration.ConfigurationException;
import edu.uchsc.ccp.opendmap.configuration.ParserFactory;
import edu.uchsc.ccp.opendmap.pattern.ParseException;

public class ConfigurationTest {

	private static final String CONFIGURATION_FILE_NAME = 
		"test\\configurations\\test.xml";

	private static final String INPUT_STRING = 
		"phagocytic vesicle membrane";
	
	private static final Level level = Level.OFF;
	
	public static void main(String[] args) throws ParseException{
		// Level.FINEST gives lots of detail
		// Level.FINER give a lot of detail
		// Level.FINE gives less
		
		// Setup the parser
		Parser p = null;
		try {
			p = ParserFactory.newParser(CONFIGURATION_FILE_NAME, level);
		} catch (ConfigurationException e) {
			e.printStackTrace();
			return;
		}
								
		// Parse a sentence
		p.parse(INPUT_STRING);
		
		// Print out all the references recognized 
		System.out.println("\nReferences from parser:");
		for (Reference reference: p.getReferences()) {
			System.out.println("  Parser saw: " + reference);
		}
	}

}
