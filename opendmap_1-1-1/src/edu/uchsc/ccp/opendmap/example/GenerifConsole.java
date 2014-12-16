/**
 * The OpenDMAP for Protege Project
 * September 2005
 */
package edu.uchsc.ccp.opendmap.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;

import edu.uchsc.ccp.opendmap.DMAPToken;
import edu.uchsc.ccp.opendmap.Parser;
import edu.uchsc.ccp.opendmap.Reference;
import edu.uchsc.ccp.opendmap.configuration.ConfigurationException;
import edu.uchsc.ccp.opendmap.configuration.ParserFactory;

/**
 * This class wraps the <code>GenerifProcessor</code> into
 * an interactive main program that can be used to demonstrate
 * parsing GeneRifs.
 * 
 * @author Jim Firby
 */
public class GenerifConsole {
	
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
	 * Generate a string of text representing the tokens used in recognizing this reference.
	 * 
	 * @param r The reference
	 * @return The string holding the tokens recognized
	 */
	private static String makeReferenceString(Reference r) {
		StringBuffer sb = new StringBuffer();
		int next = -1;
		for (DMAPToken token: r.getTokens()) {
			if ((next == -1) || (next == token.getStart())) {
				if (next != -1) sb.append(' ');
				sb.append(token.getItem().getText());
			} else {
				sb.append(" ... ");
				sb.append(token.getItem().getText());
			}
			next = token.getEnd() + 1;
		}
		return sb.toString();
	}

	/**
	 * Add a text description of this reference to the response strings.
	 * 
	 * @param reference The reference.
	 * @param indent An indent to use before any prefix
	 * @param response The response strings to add this description to
	 * @param prefix A prefix to add to the first line of the description
	 */
	public static void writeOutput(Reference reference, int indent, String prefix) {
		writeOutput(reference.getText(), reference, indent, prefix);
		Collection<String> slotNames = reference.getSlotNames();
		int newIndent = indent + 2;
		if (prefix == null) {
			writeLine(newIndent, "\"" + makeReferenceString(reference) + "\"");
		} else {
			newIndent = newIndent + prefix.length();
		}
		if ((slotNames != null) && !slotNames.isEmpty()) {
			for (String name: slotNames) {
				writeOutput(reference.getSlotValue(name), indent+2, name + " =");
			}
		}
	}
	
	/**
	 * Add a text description of a DMAPItem to the response strings.
	 * 
	 * @param kind The name of the concept this reference recognized
	 * @param ref The reference holding the item
	 * @param indent An indent to use
	 * @param response The response strings to add the description to
	 * @param prefix A prefix to add to this description
	 */
	public static void writeOutput(String kind, Reference ref, int indent, String prefix) {
		if (prefix != null) {
			writeLine(indent, prefix, kind + " (" + ref.getStart() + "-" + ref.getEnd() + "-" + ref.getMissing() + ")");
		} else {
			int start = ref.getStart();
			int end = ref.getEnd();
			int span = end - start + 1;
			writeLine(indent, kind + " (" + start + "-" + end + "-" + ref.getMissing() + ") " 
				+ "Matches=" + (span - ref.getMissing()) + " Span=" + span);
		}
	}
	
	/**
	 * Create a text string.
	 * 
	 * @param indent An indent to use
	 * @param message The text to put in the string
	 * @return A new text string holding the message
	 */
	private static void writeLine(int indent, String message) {
		for (int i=0; i<indent; i++) System.out.print(" ");
		System.out.println(message);
	}

	/**
	 * Create a text string.
	 * 
	 * @param indent An indent to use
	 * @param prefix A prefix to add before the message
	 * @param message The text to put in the string
	 * @return A new text string holding the message
	 */

	private static void writeLine(int indent, String prefix, String message) {
		for (int i=0; i<indent; i++) System.out.print(" ");
		System.out.print(prefix);
		System.out.print(" ");
		System.out.println(message);
	}

	
	/**
	 * Loop, processing user utterances until told to "quit".
	 * 
	 * @param args The first command line argument is the OpenDMAP configuration file to load.
	 * Remaining arguments are concepts of interest.
	 */
	public static void main(String[] args) {
		// Pull out the configuration filename from the arguments
		String configFilename = null;
		if (args.length > 0) configFilename = args[0];
		if (configFilename == null) {
			System.err.println("No configuration file specified on command line.");
			return;
		} else {
			System.out.println("OpenDMAP GeneRIF Parser test console");
			System.out.println("Loading configuration: " + (new File(configFilename)).getAbsolutePath());
		}
		
		// Pull out all other arguments as concepts of interest
		ArrayList<String> conceptsOfInterest = new ArrayList<String>();
		for (int i=1; i<args.length; i++) {
			conceptsOfInterest.add(args[i]);
		}
		
		// Build and configure a parser
		Parser p = null;;
		try {
			p = ParserFactory.newParser(configFilename, TRACELEVEL);
		} catch (ConfigurationException e1) {
			// Bad configuration
			e1.printStackTrace();
			return;
		}
		
		// Print initial prompt
		System.out.println();
		System.out.println("Welcome, please enter a GeneRif:");
		
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
			
			// See if it is the quit utterance
			if (utterance == null) done = true;
			if (utterance.equalsIgnoreCase("quit")) done = true;
			if (utterance.equalsIgnoreCase("done")) done = true;
			if (utterance.equalsIgnoreCase("stop")) done = true;
			if (utterance.equalsIgnoreCase("bye")) done = true;
			if (utterance.equalsIgnoreCase("goodbye")) done = true;
			
			// We're not quitting, process the utterance
			if (!done) {
				// Use the Generif processor
				Collection<Reference> responses = GenerifProcessor.processUtterance(p, utterance, conceptsOfInterest, false);
				// Print out any responses
				// If no response generated, we didn't understand what was said
				if (responses.isEmpty()) {
					System.out.println("I didn't see any interesting concepts.");
					System.out.println();
				} else {
					// Generate a text description of each reference
					for (Reference r: responses) {
						System.out.println(r.getReferenceString());
						writeOutput(r, 0, null);
						System.out.println();
					}
				}
			}
		}
		
		// Done
		System.out.println("Goodbye.");
	}
}