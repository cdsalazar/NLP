/**
 * The OpenDMAP for Protege Project
 * August 2005
 */
package edu.uchsc.ccp.opendmap.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.logging.Level;

import edu.uchsc.ccp.opendmap.Parser;
import edu.uchsc.ccp.opendmap.Reference;
import edu.uchsc.ccp.opendmap.configuration.ConfigurationException;
import edu.uchsc.ccp.opendmap.configuration.ParserFactory;

/**
 * This class is a generic interactive main program that can 
 * be used to demonstrate DMAP.
 * <p>
 * It loads the configuration file supplied on the command line
 * into a new parser.  It then parses user input strings and prints
 * all of the references DMAP recognizes in the input.
 * 
 * @author Jim Firby
 */
public class ParserConsole {

    private static final Level TRACELEVEL = Level.ALL;
	
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
	 * @param args The first argument is the OpenDMAP XML configuration file to load.
	 */
	public static void main(String[] args) {
    // First, get the configuration file from the arguments
		String configFilename = null;
		if (args.length > 0) configFilename = args[0];
		if (configFilename == null) {
			System.err.println("No configuration file specified on command line.");
			return;
		} else {
			System.out.println("OpenDMAP generic parser test console");
			System.out.println("Loading configuration: " + (new File(configFilename)).getAbsolutePath());
		}
            
    // Create a parser configured according to the file supplied.
		//  Generate tracing output (if desired).
		Parser parser;
		try {
			parser = ParserFactory.newParser(configFilename, TRACELEVEL);
		} catch (ConfigurationException e1) {
			// There is a problem loading the config file
			System.err.println(e1.getMessage());
			return;
		}
		
		// Print initial prompt
		System.out.println("Enter an input sentence and read the output");
                
		// Loop until quit
		String utterance;
		boolean done = false;
		while (!done) {
			// Read an input utterance
			try {
				utterance = readInput();
			} catch (IOException e) {
				// If there is an exception just quit
				System.err.println(e.getMessage());
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
        parser.reset();
        parser.parse(utterance);
        Collection<Reference> references = parser.getFrameReferences();
        for(Reference reference : references) {
          System.out.println("reference["+reference.getStart()+":"+reference.getEnd()+"] = "+reference.getReferenceString());
        }
        System.out.println("");
			}
		}
	}
}
