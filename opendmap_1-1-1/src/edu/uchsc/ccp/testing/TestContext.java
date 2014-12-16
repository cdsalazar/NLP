/**
 * The OpenDMAP for Protege Project
 * August 2005
 */
package edu.uchsc.ccp.testing;

import java.util.Collection;


/**
 * A <code>TestContext</code> sets up and then tears down the
 * context needed to run a customized user test suite.  
 * Instances of <code>TestContext</code> classes are
 * created automatically by the testing framework when needed.
 * <p>
 * When a test is run, it is passed the test context specified
 * for the test suite as an parameter.
 * 
 * @author R. James Firby
 */
public interface TestContext {
		
	/**
	 * Initialize this test context for the test suite passed in.
	 * If there are errors during initialization, add them to the errors
	 * collection passed in.
	 * <p>
	 * This method is called by a <code>TestSuite</code> before the test
	 * in the suite are run.
	 * <p>
	 * The <code>TestSuite</code> passed in as a parameter can be used to
	 * access initialization values included in the XML definition of the
	 * suite.
	 * 
	 * @param suite The test suite that is requesting this context.
	 * @param errors Errors added to this collection will be mentioned in the test suite output.
	 * @return True if initialization succeeds, false if the context cannot be used for tests.
	 */
	public boolean initialize(TestSuite suite, Collection<Object> errors);
	
	/**
	 * Tear down any state that needs to be unwound before this
	 * context is abandoned.
	 * 
	 * @param errors Errors added to this collection will be mentioned in the test suite output.
	 * @return False if the state cannot be unwound successfully.
	 */
	public boolean terminate(Collection<Object> errors);

}
