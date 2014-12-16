/**
 * The OpenDMAP for Protege Project
 * August 2005
 */
package edu.uchsc.ccp.opendmap.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.uchsc.ccp.opendmap.Parser;
import edu.uchsc.ccp.opendmap.pattern.ParseException;

/**
 * This class wraps the <code>LoveHateProcessor</code> into
 * an interactive main program that can be used to demonstrate
 * DMAP.
 * 
 * @author Jim Firby
 *
 * Changes:
 * 8/12/2005    pvo    Changed static file names to use File.separator rather than using a hard coded system dependent separator (e.g. '\')
 * 8/12/2005    pvo    added import statement for java.io.File
 */
public class LoveHateConsole {

	/* Define the Protege project and pattern file for the LoveHateProcessor */
	private static final String PROJECT_FILE_NAME = 
		"test"+File.separator+"projects"+File.separator+"people-loving-people"+File.separator+"people-loving-people.pprj";
	private static final String PATTERN_FILE_NAME = 
		"test"+File.separator+"projects"+File.separator+"people-loving-people"+File.separator+"love-hate-query.patterns";

	/* Enable output tracing.  Level.ALL gives everything
	/*                         Level.FINEST gives lots of detail
	/*                         Level.FINER give a lot of detail
	/*                         Level.FINE gives less  */
private static final Level TRACELEVEL = Level.OFF;
	
	/**
	 * Get an input string from the user.
	 * 
	 * @return The input utterance from the user.
	 * @throws IOException When a read error occurs.
	 */
	private static String readInput() throws IOException {
		System.out.print("> ");
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		return reader.readLine();
	}
	
	/**
	 * Loop, processing user utterances until told to "quit".
	 * 
	 * @param args This method ignores command line arguments.
	 * @throws ParseException If there is a problem parsing a user utterence.
	 */
	public static void main(String[] args) throws ParseException{
		// Create a parser that ignores case (for ease of typing)
		//  and generates tracing output (if desired).
		Parser p = new Parser(false, TRACELEVEL);
		
		// Add recognition patterns to the parser
		Collection errors = new ArrayList();
		Project project = new Project(PROJECT_FILE_NAME, errors);
		if (errors.size() != 0) {
			// Print errors and exit
			for (Object e: errors) {
				System.err.println("Error: " + e.toString());
			}
			return;
		} else {
			KnowledgeBase kb = project.getKnowledgeBase();
			p.addPatternsFromFile(PATTERN_FILE_NAME, kb);
		}
		
		// Print initial prompt
		System.out.println("Welcome, please ask about love relationships between Mary, Chris, Charlie, Pat, and John.");
		
		// Loop until quit
		String utterance;
		boolean done = false;
		while (!done) {
			// Read an input utterance
			try {
				utterance = readInput();
			} catch (IOException e) {
				// If there is an exception just quit
				utterance = null;
				done = true;
			}
			
			// See if its the quit utterance
			if (utterance == null) done = true;
			if (utterance.equalsIgnoreCase("quit")) done = true;
			if (utterance.equalsIgnoreCase("done")) done = true;
			if (utterance.equalsIgnoreCase("stop")) done = true;
			if (utterance.equalsIgnoreCase("bye")) done = true;
			if (utterance.equalsIgnoreCase("goodbye")) done = true;
			
			// We're not quitting, process the utterance
			if (!done) {
				// Use the LoveHate processor
				Collection<String> responses = LoveHateProcessor.processUtterance(p, utterance);
				// Print out any responses
				for (String response: responses) {
					System.out.println(response);
				}
			}
		}
		
		// Done
		System.out.println("Goodbye.");
	}
}
