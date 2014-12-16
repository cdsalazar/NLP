/**
 * The OpenDMAP for Protege Project
 * July 2005
 * 
 */
package edu.uchsc.ccp.testing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;

import org.xml.sax.SAXException;

/**
 * <code>TestMain</code> is the top-level entry for running OpenDMAP
 * Test Suites.  The main method is reads test suites from XML test
 * suite files and then executes them, reporting results.
 * <p>
 * The main method accepts the following command line arguments:
 * <ul>
 * <li> <code>-h</code> Print usage information
 * <li> <code>-b</code> Stop when a failing test is encountered
 * <li> <i>filenames</i> ANy number of test suite XML filenames
 * </ul>
 * 
 * @author R. James Firby
 * @author W.A. Baumgartner, Jr.
 * 
 */

/* Changes (most recent first)
 * 
 * 05-31-06 wab - Added the runTests() method so that TestMain could be called from within the JUnit framework
 * 
 */

public class TestMain {
	
	/* The test suite filenames */
	private static ArrayList<String> suiteNames = new ArrayList<String>();
	/* Should we break on failure? */
	private static boolean breakOnFail = false;
	
	/**
	 * Process the command line arguments and set appropriate global
	 * state.
	 * 
	 * @param args The command line arguments
	 * @return
	 */
	private static boolean processArgs(String[] args) {
		// Loop through args looking for keywords
		for (int i=0; i<args.length; i++) {
			String arg = args[i];
			if (arg.equals("-h")) {
				// Print a help message
				System.out.println("TestMain [-bh] filename filename ...");
				System.out.println("  -h       - Print this message.");
				System.out.println("  -b       - Stop as soon as a test fails.");
				System.out.println("  filename - The name of an XML test suite file.");
				return false;
			} else if (arg.equals("-b")) {
				breakOnFail = true;
			} else {
				// Non-keyword arguments are test suite file names
				suiteNames.add(arg);
			}
		}
		return true;
	}

    
    /**
     * Clear the test suite names array
     */
    public static void reset() {
        suiteNames = new ArrayList<String>();
    }
    
    public static boolean runTests(String[] args) {
        
        // Process user arguments
        if (!processArgs(args)) return false;
        if (suiteNames.size() <= 0) {
            System.err.println("No test suite files to process.");
        }
        
        // Create a Test Suite Manager
        TestManager manager = new TestManager();
        
        // Read in the test suites
        for (String filename: suiteNames) {
            File path = new File(filename);
            try {
                manager.readXMLTestFile(filename);
            } catch (IOException e) {
                // File error
                System.err.println("Cannot read XML test file: '" + path.getAbsolutePath() + "'");
                System.err.println(e.getMessage());
            } catch (SAXException e) {
                // XML parsing error
                System.err.println("Cannot parse XML test file: '" + path.getAbsolutePath() + "'");
                System.err.println(e.getMessage());
            }
        }
        
        // Run all the test suites
        System.out.println("Running " + manager.numTestSuites() + " test suites.");
        boolean allSucceeded = true;
        Enumeration suites = manager.getTestSuites();
        while (suites.hasMoreElements()) {
            TestSuite suite = (TestSuite) suites.nextElement();
            boolean okay = suite.run(System.out, breakOnFail);
            if (!okay) allSucceeded = false;
            if (!okay && breakOnFail) break;
        }
        
        return allSucceeded;
        
    }
    
    
	/**
	 * Run all test suites defined in the XML files supplied in the
	 * command line arguments.
	 * 
	 * @param args Switches and XML test suite filenames.
	 */
	public static void main(String[] args) {
		// Print a welcome message
		System.out.println("OpenDMAP Test Suite Processor");
		
        boolean allSucceeded = runTests(args);
        
		if (allSucceeded) {
			System.out.println("All tests passed.");
		} else {
			System.out.println("Some tests failed or were not run.");
		}
	}

}
