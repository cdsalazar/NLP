/*
 * Created on Apr 3, 2005
 *
 */
package edu.uchsc.ccp.opendmap.dev;

import java.util.logging.Level;

import edu.uchsc.ccp.opendmap.Parser;
import edu.uchsc.ccp.opendmap.Reference;
import edu.uchsc.ccp.opendmap.configuration.ConfigurationException;
import edu.uchsc.ccp.opendmap.configuration.ParserFactory;
import edu.uchsc.ccp.opendmap.pattern.ParseException;

/**
 * @author Will Fitzgerald
 */
public class SimpleGenerifTest {

	private static final String CONFIGURATION_FILE_NAME = 
		"generif\\configuration.xml";

	private static final String INPUT_STRING = 
		"JAZ translocated by src";
//		"JAZ is exported by exportin-5 but translocates back into nuclei by a facilitated diffusion mechanism";
	
	public static void main(String[] args) throws ParseException{
		// Level.FINEST gives lots of detail
		// Level.FINER give a lot of detail
		// Level.FINE gives less
		Parser p = null;
		try {
			p = ParserFactory.newParser(CONFIGURATION_FILE_NAME, Level.ALL);
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
		
		// Print out all the unsubsumed references recognized 
		System.out.println("\nUnsubsumed references from parser:");
		for (Reference reference: p.getUnsubsumedReferences()) {
			System.out.println("  Parser saw: " + reference);
		}
	}
}
