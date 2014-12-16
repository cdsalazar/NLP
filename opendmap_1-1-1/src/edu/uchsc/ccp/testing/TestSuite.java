/**
 * The OpenDMAP for Protege Project
 * July 2005
 */
package edu.uchsc.ccp.testing;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;


/**
 * A <code>TestSuite</code> is a set of tests designed to go with a
 * particular Protege project.  When the suite is run, each test is
 * run individually and a summary of the results is generated.
 * 
 * @author R. James Firby
 */
public class TestSuite {
	
	/* The name of the test suite */
	private String name = null;
	
	/* The set off implicit properties attached to this suite */
	private Hashtable<String, Vector<String>> properties = null;
	
	/* The tests in this suite */
	private HashSet<String> testNames = null;
	private ArrayList<Test> tests = null;
	
	/* The testing context */
	private TestContext context = null;

	/**
	 * Create a new test suite with the supplied name.  This method is
	 * called by the XML handler for the test suite file defining
	 * this suite.
	 * @param name The name for this suite
	 */
	protected TestSuite(String name) {
		this.name = name;
		this.properties = new Hashtable<String, Vector<String>>();
		this.testNames = new HashSet<String>();
		this.tests = new ArrayList<Test>();
	}
	
	/**
	 * Get the name assigned to this suite when it was created.
	 * @return The name of this test suite
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Add a new value to this test suite.  If values with the same key
	 * added multiple times, each value is added to the collection
	 * attached to that key.  No duplicate checking is done.
	 * 
	 * @param id The key naming this property.
	 * @param value The value assigned to this property.
	 * @return True if the property is added successfully.
	 */
	protected boolean addValue(String key, String value) {
		Vector<String> values = properties.get(key);
		if (values == null) {
			values = new Vector<String>();
			properties.put(key, values);
		}
		values.add(value);
		return true;
	}
	
	/**
	 * Get a collection of all of the values in this test suite
	 * associated with the specified key.
	 * 
	 * @param key The name of the value.
	 * @return A collection of values associated with this name.
	 */
	public Collection<String> getValues(String key) {
		return properties.get(key);
	}
	
	/**
	 * Get the first value associated with the specified key.
	 * 
	 * @param key key The name of the value.
	 * @return The first value associated with this name.
	 */
	public String getValue(String key) {
		Vector<String> values = properties.get(key);
		if ((values == null) || values.isEmpty()) {
			return null;
		} else {
			return values.get(0);
		}
	}
	
	/**
	 * Add a new <code>Test</code> to this test suite.  This method is
	 * called by the XML handler for the test suite file defining
	 * this suite.
	 * @param test The test to add
	 * @return False if a test with the same name is already in this test suite
	 */
	protected boolean addTest(Test test) {
		String testName = test.getName();
		if (testName == null) {
			return false;
		} else if (testNames.contains(testName)) {
			test.setName(getNewTestName(testName));
			testNames.add(testName);
			tests.add(test);
			return false;
		} else {
			testNames.add(testName);
			tests.add(test);
			return true;
		}
	}
	
	private String getNewTestName(String testName) {
		int i = 0;
		String newName = testName;
		if (testNames.contains(newName)) {
			newName = testName + "<" + i + ">";
		}
		return newName;
	}
	
	/**
	 * Run this <code>TestSuite</code> and print the results to
	 * the supplied stream.
	 * @param stream A stream to capture the test results from this run
	 * @param breakOnFail Stop if a failure occurs
	 * @return True if all of the tests in this suite pass
	 */
	public boolean run(PrintStream stream, boolean breakOnFail) {
		boolean okay = true;
		// Mention we are running the test suite
		stream.println("Test Suite \"" + getName() + "\" (" + tests.size() + " tests) ...");
		// Count successes and failures
		int count = 0;
		int success = 0;
		
		// Create the testing context
		String contextClassName = getValue("test-context");
		ArrayList<Object> errors = new ArrayList<Object>();
		if (contextClassName == null) {
			// No context
			stream.println("-- Warning: No testing context specified.");
			context = null;
		} else {
			// Instantiate the context
			try {
				Class contextClass = Class.forName(contextClassName);
				Object contextObject = contextClass.newInstance();
				if (contextObject instanceof TestContext) {
					context = (TestContext) contextObject;
					okay = context.initialize(this, errors);
					for (Object e: errors) {
						if (okay) {
							stream.println("-- Warning initializing testing context: " + e.toString());							
						} else {
							stream.println("-- Error initializing testing context: " + e.toString());
						}
					}
				}
			} catch (Exception e) {
				stream.println("-- Error creating testing context: " + e.toString());
				okay = false;
			}
		}
		
		// Run each test
		if (okay) {
			for (Test test: tests) {
				try {
					count = count + 1;
					if (test.run(context, stream)) {
						success = success + 1;
					} else if (breakOnFail) {
						break;
					}
				} catch (Exception e) {
					stream.println("-- Error running test: " + e.toString());
					if (breakOnFail) break;
				}
			}
		}
		
		// Done
		if (okay && (context != null)) {
			errors.clear();
			context.terminate(errors);
			for (Object e: errors) {
				stream.println("-- Error terminating test context: " + e.toString());
			}		
		}
		stream.println("... test suite \"" + getName() + "\" finished.");
		// Print summary of results
		if (count == tests.size()) {
			stream.println("Ran all " + count + " tests, " + success + " passed, " + (count-success) + " failed.");
		} else {
			stream.println("Ran " + count + " of " + tests.size() + " tests, " + success + " passed, " + (count-success) + " failed.");			
		}
		return (success == tests.size());
	}
	
}
