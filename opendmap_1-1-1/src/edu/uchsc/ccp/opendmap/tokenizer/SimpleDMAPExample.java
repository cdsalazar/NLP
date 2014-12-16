package edu.uchsc.ccp.opendmap.tokenizer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;

import edu.uchsc.ccp.opendmap.DMAPItem;
import edu.uchsc.ccp.opendmap.DMAPToken;
import edu.uchsc.ccp.opendmap.DMAPTokenizer;
import edu.uchsc.ccp.opendmap.InfoPacket;
import edu.uchsc.ccp.opendmap.Parser;
import edu.uchsc.ccp.opendmap.Reference;
import edu.uchsc.ccp.opendmap.configuration.ConfigurationException;
import edu.uchsc.ccp.opendmap.configuration.ParserFactory;
import edu.uchsc.ccp.opendmap.pattern.ParseException;

/**
 * This is meant to be a straightforward example showing how you might add arbitrary entity types prior to DMAP processing. To do this, we make use of
 * the EnityTokenizer which is an extension of the DMAPTokenizer. Much of the code for this class was scavenged from the other example OpenDMAP
 * classes. See the main methods for details on how to add entity annotations.
 * 
 * @author Bill Baumgartner
 * 
 */
public class SimpleDMAPExample {
    private static final Level TRACELEVEL = Level.OFF;

    /**
     * Generate a string of text representing the tokens used in recognizing this reference.
     * 
     * @param r
     *            The reference
     * @return The string holding the tokens recognized
     */
    private static String makeReferenceString(Reference r) {
        StringBuffer sb = new StringBuffer();
        int next = -1;
        for (DMAPToken token : r.getTokens()) {
            if ((next == -1) || (next == token.getStart())) {
                if (next != -1)
                    sb.append(' ');
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
     * @param reference
     *            The reference.
     * @param indent
     *            An indent to use before any prefix
     * @param response
     *            The response strings to add this description to
     * @param prefix
     *            A prefix to add to the first line of the description
     */
    private static void writeOutput(Reference reference, int indent, String prefix) {
        writeOutput(reference.getItem(), reference, indent, prefix);
        Collection<InfoPacket> slots = reference.getInformation();
        int newIndent = indent + 2;
        if (prefix == null) {
            writeLine(newIndent, "\"" + makeReferenceString(reference) + "\"");
        } else {
            newIndent = newIndent + prefix.length();
        }
        if ((slots != null) && !slots.isEmpty()) {
            for (InfoPacket info : slots) {
                writeOutput(info.getValue(), indent + 2, info.getKey().getText() + " =");
            }
        }
    }

    /**
     * Add a test description of a DMAPItem to the response strings.
     * 
     * @param item
     *            The DMAPItem
     * @param ref
     *            The reference holding the item
     * @param indent
     *            An indent to use
     * @param response
     *            The response strings to add the description to
     * @param prefix
     *            A prefix to add to this description
     */
    private static void writeOutput(DMAPItem item, Reference ref, int indent, String prefix) {
        if (prefix != null) {
            writeLine(indent, prefix, item.getText() + " (" + ref.getStart() + "-" + ref.getEnd() + "-" + ref.getMissing() + ")");
        } else {
            int start = ref.getStart();
            int end = ref.getEnd();
            int span = end - start + 1;
            writeLine(indent, item.getText() + " (" + start + "-" + end + "-" + ref.getMissing() + ") " + "Matches="
                    + (span - ref.getMissing()) + " Span=" + span);
        }
    }

    /**
     * Create a text string.
     * 
     * @param indent
     *            An indent to use
     * @param message
     *            The text to put in the string
     * @return A new text string holding the message
     */
    private static void writeLine(int indent, String message) {
        for (int i = 0; i < indent; i++)
            System.out.print(" ");
        System.out.println(message);
    }

    /**
     * Create a text string.
     * 
     * @param indent
     *            An indent to use
     * @param prefix
     *            A prefix to add before the message
     * @param message
     *            The text to put in the string
     * @return A new text string holding the message
     */

    private static void writeLine(int indent, String prefix, String message) {
        for (int i = 0; i < indent; i++)
            System.out.print(" ");
        System.out.print(prefix);
        System.out.print(" ");
        System.out.println(message);
    }

    public static Collection<Reference> processUtterance(Parser parser, DMAPTokenizer tokenizer, Collection<String> interests) {
        // Reset the parser
        parser.reset();
        // Parse the utterance
        parser.parse(tokenizer);
        // Extract the best matches
        return processRecognizedReferences(parser, interests, false);
    }

    public static Collection<Reference> processRecognizedReferences(Parser parser, Collection<String> interests, boolean debug) {
        if (debug) {
            // Print out everything we saw
            Collection<Reference> all = parser.getReferences();
            System.out.println("\nAll references:");
            for (Reference r : all) {
                System.out.println(r.getStart() + ".." + r.getEnd() + " " + r.getReferenceString());
            }
        }

        // Find all of the unsubsumed references that were generated
        Collection<Reference> references = parser.getUnsubsumedReferences(debug);
        if (debug) {
            System.out.println("\nUnsubsumed references");
            for (Reference r : references) {
                System.out.println(r.getStart() + ".." + r.getEnd() + " " + r.getReferenceString());
            }
        }
        // Extract and score any interesting references found
        ArrayList<Reference> seen = new ArrayList<Reference>();
        for (Reference r : references) {
            if (isConceptOfInterest(r, interests)) {
                // Sort into list
                boolean done = false;
                for (int i = 0; i < seen.size(); i++) {
                    Reference ref = seen.get(i);
                    if (calculateScore(r) > calculateScore(ref)) {
                        seen.add(i, r);
                        done = true;
                        break;
                    }
                }
                if (!done)
                    seen.add(r);
            }
        }
        // Done
        return seen;
    }

    /**
     * Check whether a reference is a concept of interest.
     * 
     * @param r
     *            The reference to check.
     * @param interests
     *            The collection of concepts of interest
     * @return True if the reference is a concept of interest
     */
    private static boolean isConceptOfInterest(Reference r, Collection<String> interests) {
        // If no interests specified, return everything
        if ((interests == null) || (interests.size() <= 0))
            return true;
        // Check whether the reference is an interesting concept
        boolean found = false;
        for (String interest : interests) {
            if (r.isa(interest)) {
                found = true;
                break;
            }
        }
        return found;
    }

    /**
     * Score the reference.
     * 
     * @param r
     *            The reference
     * @return The score
     */
    public static double calculateScore(Reference r) {
        return ((double) (calculateSpan(r) - r.getMissing())) / ((double) (calculateSpan(r)));
    }

    /**
     * Calculate the span of a reference.
     * 
     * @param r
     *            The reference
     * @return The span
     */
    private static int calculateSpan(Reference r) {
        return r.getEnd() - r.getStart() + 1;
    }

    /**
     * This is meant to be a straightforward example showing how you might add arbitrary entity types prior to DMAP processing. To do this, we make
     * use of the EnityTokenizer which is an extension of the DMAPTokenizer.
     * 
     * @param args
     *            args[0] = configuration file<br>
     *            args[1...n] = concepts of interest (Adding further arguments will cause the output to be filtered for just the listed
     *            "concepts of interest". If no arguments other than the configuration file are listed, then the output is not filtered.
     * @throws ParseException
     */
    public static void main(String[] args) throws ParseException {
        /* Pull out the configuration filename from the arguments */
        String configFilename = null;
        if (args.length > 0)
            configFilename = args[0];
        if (configFilename == null) {
            System.err.println("No configuration file specified on command line.");
            return;
        } else {
            System.out.println("Loading configuration: " + (new File(configFilename)).getAbsolutePath());
        }

        /* Pull out all other arguments as concepts of interest */
        ArrayList<String> conceptsOfInterest = new ArrayList<String>();
        for (int i = 1; i < args.length; i++) {
            conceptsOfInterest.add(args[i]);
        }

        /* Build and configure a parser */
        Parser p = null;
        try {
            p = ParserFactory.newParser(configFilename, TRACELEVEL);
        } catch (ConfigurationException e1) {
            /* Bad configuration */
            e1.printStackTrace();
            return;
        }

        /* For the purposes of this example, the text to process is hard coded so that we can add the proper EntityAnnotations */
        /* ____________________ 01234567890123456789012345678901234567890123456789 */
        String textToProcess = "Entityabc interacts with Entitydef and Entityxyz.";

        /*
         * Create 3 protein annotations.
         */
        List<EntityAnnotation> entityAnnotations = new ArrayList<EntityAnnotation>();
        entityAnnotations.add(new EntityAnnotation(0, 9, "c-protein"));
        entityAnnotations.add(new EntityAnnotation(25, 34, "c-protein"));
        entityAnnotations.add(new EntityAnnotation(39, 48, "c-protein"));

        /* Initialize a new entity tokenizer */
        EntityTokenizer entityTokenizer = new EntityTokenizer(p, textToProcess, false, entityAnnotations);

        /* Parse the text and extract matched concepts */
        Collection<Reference> responses = processUtterance(p, entityTokenizer, conceptsOfInterest);

        /* Print out any responses. */
        if (responses.isEmpty()) {
            System.out.println("I didn't see any interesting concepts.");
            System.out.println();
        } else {
            /*
             * It is possible to have multiple proteins fill the interactor slots, therefore in order to output pairwise interactions we will need to
             * keep track of the interactors that are detected and create the pairwise interactions as a post-processing step.
             */
            Set<String> interactors1 = new HashSet<String>();
            Set<String> interactors2 = new HashSet<String>();

            /* Cycle through the responses and look for protein-protein interaction annotations */
            for (Reference r : responses) {
                String annotationClass = r.getText();
                /* if this is a protein-protein interaction annotation, then cache its slot fillers */
                if (annotationClass.equals("c-interact")) {
                    Vector<InfoPacket> slots = r.getInformation();
                    if ((slots != null) && !slots.isEmpty()) {
                        for (InfoPacket info : slots) {
                            Reference slotReference = info.getValue();
                            String slotCoveredText = textToProcess.substring(slotReference.getCharacterStart(), slotReference.getCharacterEnd());
                            /* Remove "Entity" from the covered text */
                            slotCoveredText = slotCoveredText.substring(6);

                            /* Save the covered text so that pairwise interactions can be created later */
                            String slotName = info.getKey().getText();
                            if (slotName.equals("interactor1")) {
                                interactors1.add(slotCoveredText);
                            } else {
                                interactors2.add(slotCoveredText);
                            }
                        }
                    }
                }
                
                /* Create pairwise interactions */
                for (String interactor1 : interactors1) {
                    for (String interactor2 : interactors2) {
                        System.out.println("Interacting entities before mapping are " + interactor1 + " and " + interactor2);
                    }
                }
            }
        }
    }

}
