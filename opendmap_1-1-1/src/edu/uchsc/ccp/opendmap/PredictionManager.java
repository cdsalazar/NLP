/**
 * The DMAP for Protege Project
 * July 2005
 */
package edu.uchsc.ccp.opendmap;

import java.awt.FontMetrics;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;

//import javax.swing.JPanel;

//import edu.uchsc.ccp.opendmap.debug.AdvancingEvidence;
//import edu.uchsc.ccp.opendmap.debug.DMAPPatternTracebackVisualizer;
//import edu.uchsc.ccp.opendmap.debug.ReferenceListTabbedPane;

/**
 * This class manages the set of predictions needed by the parser. Predictions are broken into two types: anytime predictions that correspond to the
 * patterns that the parser starts out with, and dynamic predictions that are generated while parsing when predictions are advanced.
 * <p>
 * This class bundles these two tables together and manages their use. Splitting out the prediction manager as a separate class greatly simplifies the
 * parser itself.
 * 
 * @author R. James Firby
 * @author Will Fitzgerald
 * @author William A. Baumgartner, Jr.
 */

/*
 * Changes (most recent first)
 * 
 * 11-30-06 wab - Added maps (anytimePrediction2patternMap, dynamicPrediction2parentMap) to store enable traceback from resulting match to original
 * pattern - for debugging purposes mostly. 09-15-05 rjf - Added numRemaining to PredictionIterator
 */
public class PredictionManager {

    /* The parser using this prediction table */
    protected Parser parser = null;

    /* The two different prediction tables */
    private PredictionTable anytimePredictionTable = new PredictionTable("Anytime Predictions");

    private PredictionTable dynamicPredictionTable = new PredictionTable("Dynamic Predictions");

    /* Storage to enable predictions to be mapped back to their original patterns - for debugging purposes mostly */
    /*
     * The anytimePrediction2patternMap maps between the anytime predictions which are created from the original patterns to the orginal patterns
     * themselves
     */
    protected Map<String, Pattern> anytimePrediction2patternMap = new HashMap<String, Pattern>();

    /*
     * The dynamicPrediction2parentMap links between a prediction's hashcode and the hashcode of it's parent (the parent is the prediction that was
     * advanced to get to the child) Currently, the Prediction class uses the default Object.hashCode() method. This may not be the safest
     * implementation.
     */
    protected Map<String, String> dynamicPrediction2parentMap = new HashMap<String, String>();

    /*
     * The advancingPredictionHashcode keeps track of the prediction that is being advanced. This is used when storing the mapping between a completed
     * prediction and its parent.
     */
    private String advancingPredictionHashcode = "-1";

    /*
     * The advancement2evidenceMap keeps track of the evidence (Prediction or text) that was utilized to advance a prediction. The key for this map is
     * currently a String which represents the prediction advancement as "prediction-hashcode --- parent-prediction-hashcode"
     */


    protected Map<String, Prediction> anytimePredictionMap = new HashMap<String, Prediction>();

    protected Map<String, Prediction> dynamicPredictionMap = new HashMap<String, Prediction>();

    /**
     * This stores IDs for each pattern. The ID is based on the order the patterns are loaded.
     */
    protected Map<String, Integer> anytimePattern2IDMap = new HashMap<String, Integer>();
    private int anytimePatternCount = 0;
    
    private Prediction evidencePrediction;

    private String evidenceText;

    public Prediction getEvidencePrediction() {
        return evidencePrediction;
    }

    public void setEvidencePrediction(Prediction evidencePrediction) {
        this.evidencePrediction = evidencePrediction;
    }

    public String getEvidenceText() {
        return evidenceText;
    }

    public void setEvidenceText(String evidenceText) {
        this.evidenceText = evidenceText;
    }

    /**
     * Creates a new <code>PredictionManager</code> for use by the supplied parser.
     * 
     * @param parser
     *            The user of this prediction manager
     */
    protected PredictionManager(Parser parser) {
        this.parser = parser;
    }

    /**
     * Add anytime predictions on the supplied item derived from the supplied recognition pattern. More than one prediction may be created for a given
     * pattern.
     * 
     * @param item
     *            The item to recognize
     * @param pattern
     *            The recognition pattern for the item
     * @return The number of actual predictions installed
     */
    int addAnytimePrediction(DMAPItem base, Pattern pattern) {
        int added = 0;
        // This pattern is not normalized, create a prediction for each possible target
        Collection<PatternGroup> patterns = PatternGroup.normalize(pattern);
        for (PatternGroup normalizedPattern : patterns) {
            if (normalizedPattern == null) {
                throw new Error("Pattern " + pattern.toPatternString() + " is not allowed because it will match anything.");
            }
            Prediction p = new Prediction(parser, base, normalizedPattern);
            if (anytimePredictionTable.addPrediction(p)) {
                if (parser.isLoggable(Level.FINER)) {
                    parser.log(Level.FINER, "PredictionManager: From pattern: " + pattern + " -- New anytime prediction: " + p);
                }
                /* store the mapping from anytime prediction to the pattern used to generate it from the patterns file */
                anytimePrediction2patternMap.put(Integer.toString(p.hashCode()), pattern);
                anytimePredictionMap.put(Integer.toString(p.hashCode()), p);
                if (!anytimePattern2IDMap.containsKey(pattern.toString())) {
                anytimePattern2IDMap.put(pattern.toString(),anytimePatternCount);
//                System.err.println("PredictionManager: Pattern ID: " + anytimePatternCount + " -- " + pattern.toString());
                anytimePatternCount++;
                } else {
                    System.err.println("Duplicate anytime pattern discovered: " + pattern.toString());
                }
                added++;
            }
        }
        return added;
    }

    /**
     * Add the supplied prediction as a dynamic prediction.
     * 
     * @param prediction
     *            The prediction being added
     * @return 1 for success, 0 otherwise
     */
    int addDynamicPrediction(Prediction prediction) {

        /* wab - this line for debugging purposes so we can keep track of the predictions */
        dynamicPredictionMap.put(Integer.toString(prediction.hashCode()), prediction);

        if (dynamicPredictionTable.addPrediction(prediction)) {
            if (parser.isLoggable(Level.FINER)) {
                parser.log(Level.FINER, "PredictionManager: New dynamic prediction: " + prediction);
            }
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * Clears the predictions being managed.
     * <p>
     * If the supplied parameter is <i>true</i>, all predictions are erased. If the parameter is 'false', only the dynamic predictions are erased.
     * 
     * @param all
     *            True to clear all predictions, false to clear only dynamic predictions
     */
    protected void clear(boolean all) {
        if (all) {
            anytimePredictionTable.clearPredictions();
            anytimePrediction2patternMap = new HashMap<String, Pattern>();
            anytimePredictionMap = new HashMap<String, Prediction>();
            anytimePattern2IDMap = new HashMap<String, Integer>();
            anytimePatternCount = 0;
        }

        dynamicPredictionTable.clearPredictions();
        dynamicPrediction2parentMap = new HashMap<String, String>();
        dynamicPredictionMap = new HashMap<String, Prediction>();
    }

    /**
     * Get all of the predictions currently waiting for something that matches the supplied item.
     * 
     * @param item
     *            The item to look on for predictions
     * @return An iterator holding all predictions on the item
     */
    PredictionIterator getPredictionsOn(DMAPItem item) {
        return new PredictionIterator(item);
    }

    /**
     * Dump the contents of this manager to the supplied logger.
     * 
     * @param logger
     *            The destination for the information
     * @param level
     *            The level at which it should be written
     */
    protected void log(DMAPLogger logger, Level level) {
        anytimePredictionTable.log(logger, level);
        dynamicPredictionTable.log(logger, level);

        /* dump the contents of the prediction2parent/pattern maps to the logger */
        logger.log(Level.FINER, "--------------- ANYTIME PREDICTION 2 PATTERN MAP ------------------");
        Set<String> preds = anytimePrediction2patternMap.keySet();
        for (String pred : preds) {
            logger.log(Level.FINER, "PredictionManager: Prediction2Pattern: " + pred + " ------ " + anytimePrediction2patternMap.get(pred));
        }
        logger.log(Level.FINER, "--------------- DYNAMIC PREDICTION 2 PARENT MAP ------------------");
        preds = dynamicPrediction2parentMap.keySet();
        for (String pred : preds) {
            logger.log(Level.FINER, "PredictionManager: Prediction2Parent: " + pred + " ------ " + dynamicPrediction2parentMap.get(pred));
        }

        /* print the evidence map */
//        logger.log(Level.FINER, "========================   ADVANCEMENT TO EVIDENCE MAP   =========================");
//        for (String advancement : advancement2evidenceMap.keySet()) {
//            logger.log(Level.FINER, "PredictionManager: " + advancement + "  EVIDENCE: "
//                    + advancement2evidenceMap.get(advancement).toString());
//        }
        logger.log(Level.FINER, "==================================================================================");

    }

    /**
     * An iterator holding all of the predictions (both dynamic and anytime) on some item.
     * 
     * @author Will Fitzgerald
     */
    class PredictionIterator implements Iterator<Prediction> {

        private int i = 0;

        private int j = 0;

        private Vector<Prediction> anytimes;

        private Vector<Prediction> dynamics;

        /**
         * Create an iterator for all of the predictions currently on the supplied item.
         * 
         * @param item
         *            The item the predictions are on
         */
        PredictionIterator(DMAPItem item) {
            if (item == null) {
                anytimes = null;
                dynamics = null;
            } else {
                anytimes = anytimePredictionTable.getPredictions(item);
                dynamics = dynamicPredictionTable.getPredictions(item);
            }
        }

        /**
         * Does this iterator contain another prediction?
         */
        public boolean hasNext() {
            if ((anytimes != null) && (i < anytimes.size()))
                return true;
            if ((dynamics != null) && (j < dynamics.size()))
                return true;
            return false;
        }

        /**
         * Get the next prediction held in the iterator.
         */
        public Prediction next() {
            if ((anytimes != null) && (i < anytimes.size())) {
                return anytimes.elementAt(i++);
            }
            if ((dynamics != null) && (j < dynamics.size())) {
                return dynamics.elementAt(j++);
            }
            throw new java.util.NoSuchElementException();
        }

        /**
         * Remove not supported.
         */
        public void remove() {
            throw new UnsupportedOperationException();
        }

        /**
         * Get the number of predictions remaining in this iterator.
         * 
         * @return The number of predictions remaining.
         */
        public int numRemaining() {
            int count = 0;
            if ((anytimes != null) && (i < anytimes.size()))
                count = count + (anytimes.size() - i);
            if ((dynamics != null) && (j < dynamics.size()))
                count = count + (dynamics.size() - j);
            return count;
        }

    }

    /**
     * 
     * @return the advancing prediction hashcode
     */
    public String getAdvancingPredictionHashcode() {
        return advancingPredictionHashcode;
    }

    /**
     * Set the advancing prediction hashcode (stored as a string)
     * 
     * @param advancingPredictionHashcode
     */
    public void setAdvancingPredictionHashcode(String advancingPredictionHashcode) {
        this.advancingPredictionHashcode = advancingPredictionHashcode;
    }

    /**
     * Adds a mapping between predictions. This is used to enable the traceback from a prediction to the pattern that was originally responsible for
     * generating it
     * 
     * @param predictionHashCode
     *            a String representation of the hashcode for a prediction
     * @param parentPredictionHashCode
     *            a String representation of the hashcode for the prediction that was advanced to get to the first input parameter
     */
    public void addDynamicPrediction2parentLink(String predictionHashCode, String parentPredictionHashCode) {
        if (!dynamicPrediction2parentMap.containsKey(predictionHashCode)) {
            dynamicPrediction2parentMap.put(predictionHashCode, parentPredictionHashCode);
        } else {
            parser.log(Level.FINER, "PredictionManager: Attempting to add prediction2parent link when prediction already exists in map.");
        }
    }

    
    public Pattern getPatternUsedToMatchReference(Reference r) {
        return tracebackToOriginalPattern(r);
    }
    
    /**
     * This method facilitates the traceback from a resulting prediction to the original pattern from which it was derived.
     * 
     * @param resultPrediction
     * @return the Pattern originally responsible for the input Prediction
     */
    private Pattern tracebackToOriginalPattern(Reference r) {

        Prediction resultPrediction = r.getPrediction();
        if ( resultPrediction == null ) {
	        parser.log(Level.FINER, "Did not find mapping to original pattern for: " + r.toString());
	        return null;
        }
        
        /* output visualization of the traceback */
//        new DMAPPatternTracebackVisualizer().createDMAPTraceback(r.getReferenceString(), resultPrediction.hashCode(), anytimePrediction2patternMap, dynamicPrediction2parentMap, advancement2evidenceMap, anytimePredictionMap, dynamicPredictionMap);
        
        /* log traceback to original pattern */
        String nextHashCode = Integer.toString(resultPrediction.hashCode());

        while (dynamicPrediction2parentMap.containsKey(nextHashCode)) {
            String prevHashCode = nextHashCode;
            nextHashCode = dynamicPrediction2parentMap.get(nextHashCode);
            if (dynamicPredictionMap.get(nextHashCode) != null) {
                parser.log(Level.FINER, "Traceback from: " + prevHashCode + " to " + nextHashCode + " which was waiting for: "
                        + dynamicPredictionMap.get(nextHashCode).getKey());
            } else {
                parser.log(Level.FINER, "Traceback from: " + prevHashCode + " to " + nextHashCode + " which was waiting for: "
                        + anytimePredictionMap.get(nextHashCode).getKey());
            }
        }

        /* at this point the next hashcode should be an anytime prediction hashcode, and we should be able to get to the original pattern */
        if (anytimePrediction2patternMap.containsKey(nextHashCode)) {
            parser.log(Level.FINER, "Original pattern: " + anytimePrediction2patternMap.get(nextHashCode));
            return anytimePrediction2patternMap.get(nextHashCode);
        } else {
            parser.log(Level.FINER, "Did not find mapping to original pattern from: " + nextHashCode);
            return null;
        }

       

    }





    public Map<String, Prediction> getDynamicPredictions() {
        return dynamicPredictionMap;
    }
    
    public Map<String, String> getDynamicPrediction2ParentMap() {
        return dynamicPrediction2parentMap;
    }
    
    public String getParentForPrediction(String predictionHC) {
        return dynamicPrediction2parentMap.get(predictionHC);
    }
    
//    public void inputAdvancingEvidence(String predictionHashcode, String parentPredictionHashcode, Prediction evidencePrediction,
//            String evidenceText, boolean isPreAnnotated, int spanStart, int spanEnd) {
//        String advancementString = predictionHashcode + " --- " + parentPredictionHashcode;
//        AdvancingEvidence evidence = new AdvancingEvidence(evidencePrediction, evidenceText, isPreAnnotated, spanStart, spanEnd);
//        if (!advancement2evidenceMap.containsKey(advancementString)) {
//            advancement2evidenceMap.put(advancementString, evidence);
//        } else {
//            parser.log(Level.FINER, "PredictionManager: evidence for this advancement is already stored: " + advancementString);
//        }
//
//    }

 
   public Map<String, Integer> getPatternToIDMap() {
       return anytimePattern2IDMap;
   }

}
