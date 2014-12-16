/**
 * The OpenDMAP for Protege Project
 * March 2005
 */
package edu.uchsc.ccp.opendmap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;

/**
 * A <code>Prediction</code> is the heart of the DMAP parsing process. DMAP is attempting to recognize things that it and the knowledge base predict
 * may appear.
 * <p>
 * Each prediction includes a pattern that it is watching for and a concept, or <code>DMAPItem</code>, that it will signal, or reference, when it
 * recognizes the pattern. The pattern is a way that the input text strings might refer to the concept.
 * <p>
 * A prediction always normalizes its pattern so that it is waiting for exactly one DMAP Item to be seen in the input. When this item is recognized,
 * the pattern is advanced to the next normalized item. Once all items have been recognized and the pattern is complete, the base concept is said to
 * have been referenced.
 * 
 * @author Will Fitzgerald
 * @author R. James Firby
 */

/*
 * Changes (most recent first)
 * 02-18-09 kmv - Added methods for determining "completeness" of a Prediction.
 * 
 * 03-09-06 rjf - Added environment to handle cross-pattern constraint functions 02-01-06 rjf - Added token list and missing token counting 12-21-05
 * rjf - Added denotation as a separate concept 10-12-05 rjf - Added character positions to references 09-28-05 rjf - Added check to make sure the key
 * is consistent with a reference before advancing. 09-15-05 rjf - Made recursive predictions behave like :heads, not strictly a good idea 09-15-05
 * rjf - Added a check to prevent infinite recursive frame reconition. 09-14-05 rjf - Changed advanceAux to record only slot fillers in information.
 */

public class Prediction implements Cloneable {

    /* The base concept to be recognized and referenced */
    private DMAPItem base;

    /* The pattern this prediction is trying to find */
    private PatternGroup pattern = null;

    /* The specific item this prediction is waiting to see next */
    private DMAPItem key;

    /* The position in the input token stream where this pattern began to be matched */
    private int startIndex = 1;

    /* The position in the input token stream that the next item must appear */
    private int nextIndex = -1;

    /* The position of the last reference that matched this prediction's key */
    private int lastMatchingIndex = -1;

    /* The number of spanned tokens ignored by this prediction */
    private int missingTokenCount = 0;

    private ArrayList<DMAPToken> tokens = new ArrayList<DMAPToken>();

    /* A record of all items recognized during processing of this pattern */
    private Vector<InfoPacket> information = new Vector<InfoPacket>();

    /* The start character position of this prediction in the input */
    private int cStart = -1;

    /* The function property annotation binding environment for this prediction */
    private PredictionEnvironment environment = null;

    /* The parser using this prediction */
    private Parser parser;

    /**
     * Create a new prediction for this parser.
     * 
     * @param forParser
     *            The parser using this prediction.
     */
    private Prediction(Parser forParser) {
        super();
        base = null;
        key = null;
        startIndex = -1;
        nextIndex = -1;
        pattern = null;
        parser = forParser;
    }

    /**
     * Create a new prediction for the given base item based on recognizing the given pattern.
     * 
     * @param forParser
     *            The parser using this prediction.
     * @param base
     *            The base concept to be recognized and referenced.
     * @param pattern
     *            The recognition pattern for this concept.
     */
    Prediction(Parser forParser, DMAPItem base, PatternGroup pattern) {
        super();
        if (pattern == null) {
            throw new Error("Prediction pattern cannot be null.");
        }
        this.base = base;
        this.pattern = pattern;
        this.startIndex = -1;
        this.nextIndex = -1;
        this.parser = forParser;
        this.key = pattern.getKey();
    }

    private boolean isStartNull() {
        return (startIndex == -1);
    }

    private boolean isNextNull() {
        return (nextIndex == -1);
    }

    private boolean isCharacterStartNull() {
        return (cStart < 0);
    }

    /**
     * Check that the supplied index is acceptable as the next reference position matching this prediction. An index is acceptable if it is the next
     * index along. If a skip token has been seen then any later index is acceptable. However, is a skip token has been seen and a matching next token
     * already identified, then the index must be less than or equal to that tokens index. This ensures that the skip space is always as small as
     * possible.
     * 
     * @param index
     *            The index to check
     * @return True if this index is allowed as the next index
     */
    private boolean isAcceptableIndex(int index) {
        if ((pattern != null) && pattern.isDiscontinuous()) {
            if (lastMatchingIndex < 0) {
                return nextIndex <= index;
            } else {
                return ((nextIndex <= index) && (index <= lastMatchingIndex));
            }
        } else {
            return nextIndex == index;
        }
    }

    /**
     * Creates a deep copy of this prediction.
     */

    public Prediction clone() {
        Prediction newP = new Prediction(parser);
        newP.base = base;
        newP.key = key;
        newP.startIndex = startIndex;
        newP.nextIndex = nextIndex;
        newP.missingTokenCount = missingTokenCount;
        newP.pattern = pattern;
        newP.information = copyInformation();
        newP.cStart = cStart;
        for (DMAPToken token : tokens)
            newP.tokens.add(token);
        if (environment != null)
            newP.environment = environment.clone();
        return newP;
    }

    /**
     * Clone the internal information for this prediction.
     * 
     * @return A copy of the information packets in this prediction.
     */
    private Vector<InfoPacket> copyInformation() {
        Vector<InfoPacket> newInformation = new Vector<InfoPacket>(information.size());
        for (InfoPacket ip : information) {
            newInformation.add(ip);
        }
        return newInformation;
    }

    /**
     * Get the base concept for this prediction.
     * 
     * @return Returns the base concept.
     */
    public DMAPItem getBase() {
        return base;
    }

    /**
     * Get the concept that this prediction is waiting to see next.
     * 
     * @return Returns the key concept.
     */
    public DMAPItem getKey() {
        return key;
    }

    /**
     * Advance this prediction to the next item it expects to see in its pattern. If the pattern is complete, then signal a reference to the base item
     * (or concept).
     * <p>
     * This method is called whenever the parser recognizes a reference to the key concept that this prediction is waiting for.
     * 
     * @param reference
     *            The reference to the key concept this prediction is waiting for.
     * @param isDenotation
     *            True if this reference was recognized as a denotation.
     * @return A set of new predictions generated by advancing this prediction.
     */
    Vector<Prediction> advance(Reference reference, boolean isDenotation) {
        // If this reference starts where our pattern left off, then we're good
        if ((isNextNull()) || isAcceptableIndex(reference.getStart())) { // (nextIndex == reference.getStart())
            // Does this reference really match the item we're waiting for?
            if (!isDenotation || !isRecursivePatternComponent(reference)) {
                // If this reference is to a slot, invert it
                Reference denotation = reference;
                DMAPItem slotishThing = pattern.getContent();
                if (slotishThing instanceof ProtegeSlotItem) {
                    denotation = reference.invert((ProtegeSlotItem) slotishThing);
                }
                if (isPathConstraintSatisfied(denotation)) {
                    // Log this advance
                    DMAPLogger logger = parser.getLogger();
                    if (logger.isLoggable(Level.FINE)) {
                        logger.log(Level.FINE, "Prediction: Advancing " + this + "  FROM REFERENCE PREDICTION: "
                                + reference.getPrediction() + " KEY TEXT: " + key.getText());
                        logger.addIndent();
                    }

                    /* keep track of a few things so that we can map between predictions and their evidence and patterns */
                    parser.getPredictionManager().setAdvancingPredictionHashcode(Integer.toString(this.hashCode()));
                    parser.getPredictionManager().setEvidencePrediction(reference.getPrediction());
                    parser.getPredictionManager().setEvidenceText(key.getText());

                    // Copy first because a different match may also occur for this prediction
                    // so this prediction must stay waiting for another instance of the same key

                    lastMatchingIndex = reference.getStart();
                    Vector<Prediction> results = clone().advanceAux(reference, denotation);

                    /*
                     * wab - 11/30/06 - store a mapping from the prediction to the advanced predictions so that we can eventually traceback from the
                     * output to the original pattern that was matched
                     */
                    if (results != null) {
                        for (Prediction pred : results) {
//                            System.out.println("RESULTING PREDICTION: " + pred.hashCode() + " with parent: " + this.hashCode());
                            boolean isPreAnnotated = false;
                            if (reference != null && reference.getPrediction() != null) {
//                                System.out.println("USING EVIDENCE: " + reference.getPrediction().hashCode() + " Key text: "  + this.key.getText());
                            } else if (reference!=null && reference.getReferenceString().trim().startsWith("{")){
//                                System.out.println("REFERENCE IS PRE_ANNOTATED ENTITY: " + reference.getReferenceString());
                                isPreAnnotated = true;
                            }else {
//                                System.out.println("USING EVIDENCE: null  Key text: " + this.key.getText() + "  DESCRIPTIVE: " + key.toDescriptiveString());
                                
                            }
                            parser.getPredictionManager().addDynamicPrediction2parentLink(Integer.toString(pred.hashCode()),
                                    Integer.toString(this.hashCode()));

                            String evidenceText = parser.getPredictionManager().getEvidenceText();
                            if (isPreAnnotated) {
                                evidenceText = "{" + evidenceText + " } " + reference.getText();
                            }
                            parser.inputAdvancingEvidence(Integer.toString(pred.hashCode()),
                            							  Integer.toString(this.hashCode()), 
                            							  evidenceText, isPreAnnotated, 
                            							  reference.getCharacterStart(), reference.getCharacterEnd());
                            
//                            .getPredictionManager().inputAdvancingEvidence(Integer.toString(pred.hashCode()),
//                                    Integer.toString(this.hashCode()), parser.getPredictionManager().getEvidencePrediction(),
//                                    evidenceText, isPreAnnotated, reference.getCharacterStart(), reference.getCharacterEnd());
                            
//                            System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++");
//                            System.out.println("+++++++++++++ creating evidence  ++++++++++++++");
//                            System.out.println(this.hashCode() + " --- " + parser.getPredictionManager().getEvidenceText());
//                            
//                            for (DMAPToken token : tokens) {
//                                System.out.println("TOKEN IS PART OF REFERENCE: " + token.getItem().getText() + "  [" + token.getCharacterStart() + ".." + token.getCharacterEnd() + "]"); 
//                            }
//                            System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++");
                            
                        }
                    } else {
//                        System.out.println("RESULTS ARE NULL - this hc = " + this.hashCode());
                    }
                    // Finishing logging
                    if (logger.isLoggable(Level.FINE)) {
                        logger.removeIndent();
                    }
                    // Done
                    return results;
                } else {
                    // This reference doesn't match because it is not a valid slot filler
                    DMAPLogger logger = parser.getLogger();
                    if (logger.isLoggable(Level.FINE)) {
                        logger.log(Level.FINE, "Prediction: Not consistent slot filler for " + this);
                    }
                    return null;
                }
            } else {
                // Not really a match, the reference is not consistent with the item
                DMAPLogger logger = parser.getLogger();
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "Prediction: Not legal recursion for " + this);
                }
                return null;
            }
        } else {
            // This reference doesn't really match this prediction. It doesn't start
            // at the right place in the token stream.
            DMAPLogger logger = parser.getLogger();
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "Prediction: Not correct token position for " + this);
            }
            return null;
        }
    }

    /**
     * Check whether this reference satisfies any property constraints (tags) attached to the this this prediction is waiting for.
     * 
     * @param reference
     *            The reference that might satisfy the constraints.
     * @return True if the reference satisfies the constraints.
     */
    private boolean isPathConstraintSatisfied(Reference reference) {
        return pattern.getContent().pathSatisfiedIn(reference, this);
    }

    /**
     * Check whether this reference would cause a recursive prediction. (This check might be too strict)
     * 
     * @param reference
     *            The reference that might cause the recursion.
     * @return True if this reference would cause a recursive prediction.
     */
    private boolean isRecursivePatternComponent(Reference reference) {
        return reference.getItem().isa(base);
    }

    /**
     * Advance this prediction to the next pattern step.
     * 
     * @param reference
     *            The recognized reference matching the key to this prediction.
     * @param binding
     *            The reference to record if we are recognizing a slot filler.
     * @return A set of new predictions that result from advancing this prediction pattern.
     */
    private Vector<Prediction> advanceAux(Reference reference, Reference binding) {
        // Extract some information from the reference
        int start = reference.getStart();
        int end = reference.getEnd();
        DMAPItem item = reference.getItem();
        // Update the expected token positions for this prediction
        int from;
        if (!isStartNull())
            from = startIndex;
        else
            from = start;
        int next = end + 1;
        // Update the count of missing, ie. ignored, tokens spanned by this prediction
        int missing = reference.getMissing();
        if (!isNextNull())
            missing = missing + (start - nextIndex);
        missingTokenCount = missingTokenCount + missing;
        // Update this prediction character start position
        if (isCharacterStartNull()) {
            cStart = reference.getCharacterStart();
        }
        // Record this reference as seen during this prediction
        DMAPLogger logger = parser.getLogger();
        DMAPItem slotishThing = pattern.getContent();
        slotishThing.addPathBindingsFor(binding, this);
        if ((slotishThing instanceof ProtegeSlotItem) && (!slotishThing.equals(item))) {
            ProtegeSlotItem slot = (ProtegeSlotItem) slotishThing;
            if (logger.isLoggable(Level.FINER)) {
                logger.log(Level.FINER, "Prediction: Recording " + slot + " = " + binding);
            }
            information.add(new InfoPacket(slot, binding));
        }
        for (DMAPToken token : reference.getTokens()) {
            if (!tokens.contains(token))
                tokens.add(token);
        }
        // Advance our pattern
        Collection<PatternGroup> remainders = pattern.advance();
        if (remainders == null) {
            // The pattern is complete.
            // Our base concept has been recognized, signal a reference to it
            reference(base, from, end, missingTokenCount, information, reference, logger);
            return null;
        } else {
            // There are more pieces of pattern to match. Create appropriate predictions
            Vector<Prediction> results = new Vector<Prediction>();
            for (PatternGroup newPattern : remainders) {
                if (newPattern == null) {
                    // A null pattern means an optional element, the pattern is really complete
                    // Our base concept has been recognized, signal a reference to it
                    reference(base, from, end, missingTokenCount, copyInformation(), reference, logger);
                } else {
                    // A new pattern requires a new prediction, copy
                    Prediction newPrediction = this.clone();
                    newPrediction.pattern = newPattern;
                    newPrediction.key = newPattern.getKey();
                    if (from >= 0)
                        newPrediction.startIndex = from;
                    if (next >= 0)
                        newPrediction.nextIndex = next;
                    // A :head-like prediction, shift the base to the reference
                    if (slotishThing.equals(base)) {
                        newPrediction.base = item;
                        if (reference.getInformation() != null) {
                            for (InfoPacket info : reference.getInformation()) {
                                newPrediction.information.add(info);
                            }
                        }
                    }
                    // Ready
                    results.addElement(newPrediction);
                }
            }
            // Return predictions if list not empty
            if (results.isEmpty()) {
                return null;
            } else {
                return results;
            }
        }
    }

    protected void reference(DMAPItem base, int from, int to, int missing, Vector<InfoPacket> information, Reference cause, DMAPLogger logger) {
        // Check for infinite loop
        boolean isSelfReference = base.equals(cause.getItem()) || base.equals(cause.getDenotation());
        if ((from == cause.getStart()) && (to == cause.getEnd()) && isSelfReference) {
            // A reference has caused recognition of itself, stop recursion now!
            logger.log(Level.FINER, "Prediction: Prediction complete but redundant, ignored.");
        } else {
            // Things are okay, keep going
            logger.log(Level.FINER, "Prediction: Prediction complete.");
            Reference newReference = new Reference(base, from, to, missing, information, cStart, cause.getCharacterEnd(), this);

            logger.log(Level.FINER, "Created new reference for prediction: " + this.hashCode() + " -- " + base.toDescriptiveString());
            logger.log(Level.FINER, "The parent for this prediction is: advancingPredictionHashcode: "
                    + parser.getPredictionManager().getAdvancingPredictionHashcode());

            /*
             * wab - 11/30/06 - store a mapping from the prediction to the advanced predictions so that we can eventually traceback from the output to
             * the original pattern that was matched
             */
            parser.getPredictionManager().addDynamicPrediction2parentLink(Integer.toString(this.hashCode()),
                    parser.getPredictionManager().getAdvancingPredictionHashcode());

            /* here we store the evidence necessary to advance the prediction and create the reference */
//            parser.getPredictionManager().inputAdvancingEvidence(Integer.toString(this.hashCode()),
//                    parser.getPredictionManager().getAdvancingPredictionHashcode(), parser.getPredictionManager().getEvidencePrediction(),
//                    parser.getPredictionManager().getEvidenceText(), false, newReference.getCharacterStart(), newReference.getCharacterEnd());
            parser.inputAdvancingEvidence(Integer.toString(this.hashCode()),
					  false,  newReference.getCharacterStart(), newReference.getCharacterEnd());
//            System.out.println("----------- creating evidence reference ------------");
//            System.out.println(this.hashCode() + " --- " + parser.getPredictionManager().getEvidenceText());
            
            for (DMAPToken token : tokens) {
                newReference.addToken(token);
//                System.out.println("TOKEN IS PART OF REFERENCE: " + token.getItem().getText() + "  [" + token.getCharacterStart() + ".." + token.getCharacterEnd() + "]"); 
            }
//            System.out.println("-------------------------------------------------------");
            parser.reference(newReference);
        }
    }

    /**
     * Generate an informative string for debugging output
     */
    public String toString() {
        String strstr;
        if (this.isStartNull() && this.isNextNull()) {
            strstr = " ";
        } else if (this.isStartNull()) {
            strstr = " (nil-" + nextIndex + ") ";
        } else if (this.isNextNull()) {
            strstr = " (" + startIndex + "-nil) ";
        } else {
            strstr = " (" + startIndex + "-" + nextIndex + ") ";
        }
        return "<Pred #" + this.hashCode() + " on " + key + strstr + "from: " + base + "; " + this.pattern + ">";
    }

    /**
     * This method will be called from within DMAPItems when they are asked to check path constraints. Any property function constraints encountered
     * will call back through this to see if they are acceptable in the context of this prediction's binding environment.
     * 
     * @param func
     *            The property constraint function
     * @param arg
     *            The argument variable for that function
     * @param reference
     *            The reference being considered as a match
     * @return True if the property constraint is satisfied
     */
    boolean applyPropertyFunction(String func, String arg, Reference reference) {
        if (environment == null)
            return true;
        return environment.applyPropertyFunction(func, arg, reference);
    }

    /**
     * Add a property constraint function variable binding. This method is also called by DMAPItems when they are selected as a match for this
     * prediction. Any property constraint functions the DMAPItem holds may create a variable binding that must be shared across the prediction
     * pattern.
     * 
     * @param func
     *            The property constraint function
     * @param arg
     *            The argument variable to bind
     * @param reference
     *            The reference that was selected as a match
     */
    void addBinding(String func, String arg, Reference reference) {
        if (environment == null) {
            environment = new PredictionEnvironment(parser);
        }
        environment.addBinding(func, arg, reference);
    }
    
    
    /**
     * Determine the count of subpatterns to be satisfied in this Prediction.
     * @return the maximum number of subpatterns remaining
     */
    public int countRemainder() {
    	return pattern.testAdvance();
    }

}
