/**
 * The OpenDMAP for Protege Project
 * August 2005
 */
package edu.uchsc.ccp.opendmap.test;

import java.util.Collection;
import java.util.logging.Level;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.uchsc.ccp.opendmap.Parser;
import edu.uchsc.ccp.opendmap.configuration.ConfigurationException;
import edu.uchsc.ccp.opendmap.configuration.ParserFactory;
import edu.uchsc.ccp.testing.TestContext;
import edu.uchsc.ccp.testing.TestSuite;

/**
 * This class implements a testing context appropriate for running
 * tests of the OpenDMAP system itself.  Instances of this class are
 * created automatically by the testing framework when needed.
 * <p>
 * This class expects the test suite file to specify a Protege project
 * and a set of pattern files.  Upon initialization, a {@link Parser} object
 * is created and the specified Protege project and patterns files are loaded.
 * <p>
 * The OpenDMAP testing classes get the initialized parser from this context.
 * 
 * @author R. James Firby
 */

/* Changes (most recent first)
 * 
 * 12-05-05 rjf - Added support for XML configuration files.
 * 09-26-05 rjf - Added check for retaining case in matching.
 */
public class ParserTestContext implements TestContext {
	
	private Parser parser = null;
	private KnowledgeBase kb = null;
	
	/**
	 * Get the DMAP parser created during initialization of this context.
	 * This method is used by the DMAP test classes when they are run.
	 * 
	 * @return The initialized parser.
	 */
	public Parser getParser() {
		return parser;
	}
	
	/**
	 * Get the Protege Knowledge Base created during initialization of this context.
	 * This method is used by the DMAP test classes when they are run.
	 * 
	 * @return The Protege Knowledge Base created during initialization.
	 */
	public KnowledgeBase getProtegeKnowledgeBase() {
		return kb;
	}
	
	/**
	 * Initialize a {@link Parser} and Protege Knowledge Base for use by tests.
	 * This method is used by the testing framework and should not be called by
	 * user code.
	 */
	public boolean initialize(TestSuite suite, Collection<Object> errors) {
		boolean okay = true;
		// Create a DMAP parser
		String configFile = suite.getValue("configuration");
		if (configFile != null) {
			try {
				parser = ParserFactory.newParser(configFile);
			} catch (ConfigurationException e) {
				// Some problem with the configuration file
				errors.add("Unable to load configuration file '" + configFile + "': " + e.getMessage());
				okay = false;
			}
		} else {
			String retainCase = suite.getValue("retain-case");
			if ((retainCase == null) || (!retainCase.equalsIgnoreCase("true"))) {
				parser = new Parser(false, Level.OFF);
			} else {
				parser = new Parser(true, Level.OFF);
			}
		}
		// Get a handle on the Protege project
		if (okay) {
			kb = null;
			String protegeProject = suite.getValue("protege-project");
			Project project = new Project(protegeProject, errors);
			if (errors.size() != 0) {
				// Protege errors
				okay = false;
			} else {
				// No errors, extract knowledge base
				kb = project.getKnowledgeBase();
			}
		}
		// Load any required pattern rule files
		if (okay) {
			Collection<String> patternFiles = suite.getValues("pattern-file");
			if (patternFiles != null) {
				for (String filename: patternFiles) {
					try {
						parser.addPatternsFromFile(filename, kb);
					} catch (Exception e) {
						errors.add("Unable to load pattern file: " + e.getMessage());
						okay = false;
						break;
					} catch (Error e) {
						errors.add("Unable to load pattern file: " + e.getMessage());
						okay = false;
						break;
					}
				}
			}
		}
		// Load any required patterns from slots
		if (okay) {
			Collection<String> patternSlots = suite.getValues("pattern-slot");
			if (patternSlots != null) {
				for (String slotName: patternSlots) {
					try {
						parser.addPatternsFromProtegeSlot(slotName, kb);
					} catch (Exception e) {
						errors.add("Unable to load slot patterns: " + e.getMessage());
						okay = false;
						break;
					} catch (Error e) {
						errors.add("Unable to load slot patterns: " + e.getMessage());
						okay = false;
						break;
					}
				}
			}
		}
		// At this point, the parser is ready to go
		return okay;
	}
	
	/**
	 * Tear down this context.  This method is used by the testing framework
	 * and should not be called by user code.
	 */
	public boolean terminate(Collection<Object> errors) {
		// We don't need to do anything to clean up
		parser = null;
		kb = null;
		return true;
	}

}
