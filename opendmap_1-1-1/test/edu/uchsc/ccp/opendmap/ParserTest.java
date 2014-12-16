package edu.uchsc.ccp.opendmap;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import junit.framework.TestCase;
import edu.uchsc.ccp.opendmap.configuration.ConfigurationException;
import edu.uchsc.ccp.opendmap.configuration.ParserFactory;

public class ParserTest extends TestCase {

    private Parser parser;

    private static final String CONFIG_FILE_NAME = "generif/configuration-unix.xml";

    private static final Level TRACELEVEL = Level.OFF;

    @Override
    protected void setUp() throws Exception {

        try {
            parser = ParserFactory.newParser(CONFIG_FILE_NAME, TRACELEVEL);
        } catch (ConfigurationException e1) {
            /* Bad configuration */
            e1.printStackTrace();
            return;
        }

        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        parser = null;
        super.tearDown();
    }

    /**
     * Config file is good. This should load without error.
     * @throws Exception
     */
    public void testDMAParserOnGoodConfigFile() throws Exception {
    	try {
    		parser = ParserFactory.newParser("data/test/test-configuration-files/config-file_is-good.xml", Level.OFF);
    	} catch (ConfigurationException ce) {
    		System.err.println("message1: " + ce.getMessage());
    		fail("Should not have thrown an exception on good config file.");
    	}
    }
    
    /**
     * Configuration file does not exist
     * @throws Exception
     */
    public void testDMAParserConfigFileDoesNotExist() throws Exception {
    	try {
    		parser = ParserFactory.newParser("config-file-does-not-exist.xml", Level.OFF);
    		fail("Should have thrown an exception.");
    	} catch (ConfigurationException ce) {
    		System.err.println("message2: " + ce.getMessage());
    		assertTrue(ce.getMessage().contains("config-file-does-not-exist.xml' does not exist"));
    	}
    }
    
    /**
     * Config file has an invalid protege project name.
     * @throws Exception
     */
    public void testDMAParserConfigFileWithBadProtegeProjectName() throws Exception {
    	try {
    		parser = ParserFactory.newParser("data/test/test-configuration-files/config-file_a-protege-proj-file-dne.xml", Level.OFF);
    		fail("Should have thrown an exception.");
    	} catch (ConfigurationException ce) {
    		System.err.println("message3: " + ce.getMessage());
    		assertTrue(ce.getMessage().contains("Cannot create Protege project"));
    	}
    }
    
    /**
     * A pattern file in the config file does not exist
     * @throws Exception
     */
    public void testDMAParserConfigFileWithBadProtegePatternFileName() throws Exception {
    	try {
    		parser = ParserFactory.newParser("data/test/test-configuration-files/config-file_a-pattern-file-dne.xml", Level.OFF);
    		fail("Should have thrown an exception.");
    	} catch (ConfigurationException ce) {
    		System.err.println("message4: " + ce.getMessage());
    		assertTrue(ce.getMessage().contains("language" + File.separator + "generif-entity.patterns.dne (No such file or directory)"));
    	}
    }
    
    /**
     * Pattern file has an error on line 5. Pattern is missing semi-colon at end.
     * @throws Exception
     */
    public void testDMAParserConfigFileWithPatternFileWithBadPattern() throws Exception {
    	try {
    		parser = ParserFactory.newParser("data/test/test-configuration-files/config-file_a-pattern-file-with-a-bad-pattern.xml", Level.OFF);
    		fail("Should have thrown an exception.");
    	} catch (ConfigurationException ce) {
    		System.err.println("message5: " + ce.getMessage());
    		assertTrue(ce.getMessage().contains("Encountered \"//{g-np} := the? [: object -g-np -g-pp-source -g-pp-destination -g-pp-agent];\\n\" at line 6, column 1."));
    	}
    }
    
    /**
     * Pattern file uses ontology class that is not in a Protege project
     * @throws Exception
     */
    public void testDMAParserConfigFileWithPatternThatUsesOntologyClassNotInProtegeProject() throws Exception {
    	try {
    		parser = ParserFactory.newParser("data/test/test-configuration-files/config-file_a-pattern-file-that-uses-class-not-in-ontology.xml", Level.OFF);
    		fail("Should have thrown an exception.");
    	} catch (ConfigurationException ce) {
    		System.err.println("message6: " + ce.getMessage());
    		assertTrue(ce.getMessage().contains("language/generif-process.patterns.uses-invalid-class -- Pattern Error: No Protege frame named 'c-invalid-class' in ProtegeProject configuration 'language'"));
    	}
    }
    
    
    /**
     * Pattern file has a valid class but uses an invalid slot.
     * @throws Exception
     */
    public void testDMAParserConfigFileWithPatternThatUsesInvalidSlot() throws Exception {
    	try {
    		parser = ParserFactory.newParser("data/test/test-configuration-files/config-file_a-pattern-file-that-uses-an-invalid-slot.xml", Level.OFF);
    		fail("Should have thrown an exception.");
    	} catch (ConfigurationException ce) {
    		System.err.println("message7: " + ce.getMessage());
    		assertTrue(ce.getMessage().contains("Pattern error: Protege frame 'c-transport' has no slot named 'invalidslot' in ProtegeProject configuration 'language'"));
    	}
    }
    
    
    
    public void testDMAPParser() throws Exception {
        List<String> conceptsOfInterest = new ArrayList<String>();
//        conceptsOfInterest.add("c-nucleus");
        
//        conceptsOfInterest.add("i-p57kip2");
//        conceptsOfInterest.add("g-pp-agent");
        
        conceptsOfInterest.add("c-transport");
//        conceptsOfInterest.add("g-np");

        String utterance = "LIM-kinase 1 is translocated to the nucleus after being bound by p57kip2";

//        parser.reset();
//        parser.parse(utterance.trim());
//        
//        
//        Collection<Reference> responses = GenerifProcessor.processRecognizedReferences(parser, conceptsOfInterest, true);
//        /* Print out any responses */
//        if (responses.isEmpty()) {
//            System.out.println("I didn't see any interesting concepts.");
//            System.out.println();
//        } else {
//            /* Generate a text description of each reference */
//            for (Reference r : responses) {
//                System.out.println(r.getReferenceString());
//                System.out.println(r.getPrediction());
//                
//                /* traceback from prediction to original pattern here */
//                Pattern pattern = parser.getPredictionManager().getPatternUsedToMatchReference(r);
//                System.out.println("Traced back to pattern: " + pattern);
//                GenerifConsole.writeOutput(r, 0, null);
//                System.out.println();
//            }
//        }
    }

  
}
