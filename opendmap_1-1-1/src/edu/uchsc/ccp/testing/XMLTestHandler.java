/**
 * The OpenDMAP for Protege Project
 * July 2005
 */
package edu.uchsc.ccp.testing;

import java.lang.reflect.Constructor;
import java.util.Hashtable;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * This class implements a SAX XML handler for the XML forms in a
 * Test Suite file.  This handler processes the top level
 * XML forms and passes processing of the internal elements for those
 * forms to the <code>Test</code> objects they create.
 * <p>
 * The entire test suite must appear within:
 * <blockquote>
 *   &lt;test-suite name="<i>suitename</i>"&gt;
 *   &lt;/test-suite&gt;
 * </blockquote>
 * <p>
 * Within a test suite the following top level forms are defined:
 * <blockquote>
 *   &lt;test-type name="<i>test</i>"&gt<i>classname</i>&lt;/test-type&gt;
 * </blockquote>
 * This names the Java class that should be instantiated
 * to handle test with the XML form <i>test</i>.
 * 
 * @author R. James Firby
 */

/* Changes (most recent first)
 * 
 * 09-26-05 rjf - Updated Javadoc comments
 */
public class XMLTestHandler extends DefaultHandler {
	
	private static final String XMLSUITEELEMENT = "test-suite";
	private static final String XMLHANDLERELEMENT = "test-type";
	
	/* The test manager this handler will add new test suites to */
	private TestManager manager = null;
	/* The test suite currently being read */
	private TestSuite suite = null;
	
	/* Test handlers defined in the test file */
	private Hashtable<String, String> handlerClasses = new Hashtable<String, String>();;
	
	/* The test currently being read */
	private String testType = null;
	private Test test = null;
	
	/* State that must be cached while parsing XML */
	private Locator currentLocator = null;
	private StringBuffer buffer = null;
	private String currentElement = null;
	private String currentElementName = null;
	
	/* Whether or not to stop when an XML error is encountered */
	private boolean breakOnError = false;
	
	/**
	 * Create a SAX handler for OpenDMAP test suite XML files.
	 * This handler will add new test suites to the supplied test manager.
	 * This handler will print warnings but not throw an exception if an XML error is encountered.
	 * @param manager The test manager to hold the test suites read by this handler
	 */
	public XMLTestHandler(TestManager manager) {
		this.manager = manager;
	}

	/**
	 * Create a SAX handler for OpenDMAP test suite XML files.
	 * This handler will add new test suites to the supplied test manager.
	 * @param manager The test manager to hold the test suites read by this handler
	 * @param breakOnError If true, throw an exception when an XML error is encountered
	 */
	public XMLTestHandler(TestManager manager, boolean breakOnError) {
		this.manager = manager;
		this.breakOnError = breakOnError;
	}
	
	/**
	 * Called by the SAX XML Reader before each element call.
	 */
	public void setDocumentLocator(Locator locator) {
		currentLocator = locator;
	}

	/**
	 * Called by the SAX XML reader when an XML element is encountered.  This
	 * method handles top level OpenDMAP XML forms and passes internal forms to
	 * the test they are defining.
	 */
	public void startElement(String namespaceURI, String localName, String qualifiedName, Attributes atts)
  		throws SAXException {
		if (test != null) {
			// An element internal to a test definition
			try {
				if (!test.startElement(namespaceURI, localName, qualifiedName, atts)) {
					throwSAXException("Found unexpected <" + qualifiedName + "> element");
				}
			} catch (SAXException e) {
				throwSAXException(e.getMessage());
			}
			
		} else if (currentElement != null) {
			// A top level start element out of context
			throwSAXException("Unexpected <" + qualifiedName + "> element");
			
		} else if (localName.equalsIgnoreCase(XMLSUITEELEMENT)) {
			// Starting a new test suite definition
			if (suite != null) {
				throwSAXException("Nested <" + XMLSUITEELEMENT + "> elements are not allowed");
			} else {
				String name = atts.getValue("name");
				if (name == null) {
					throwSAXException("'name' attribute required in <" + XMLSUITEELEMENT + "> element");
				} else {
					suite = manager.getTestSuite(name);
					currentElement = null;
				}
			}
			
		} else if (suite == null) {
			// An orphaned element, not in a suite
			throwSAXException("Orphan <" + qualifiedName + "> element.");
			currentElement = null;
			
		} else if (localName.equalsIgnoreCase(XMLHANDLERELEMENT)) {
			// A new test handler class name
			String name = atts.getValue("name");
			if (name == null) {
				throwSAXException("'name' attribute required in <" + XMLHANDLERELEMENT + "> element");
			} else {
				buffer = new StringBuffer();
				currentElement = XMLHANDLERELEMENT;
				currentElementName = name;
			}
		
		} else if (startTest(localName, atts)) {
			// A new test, clean up some state
			testType = localName;
			buffer = null;
			currentElement = localName;
			
		} else {
			// A start element, at the top-level, that isn't a test.  Must be a property.
			buffer = new StringBuffer();
			currentElement = localName;
			currentElementName = localName;
		}
	}
	
	/**
	 * This method creates the test object for a new top level definition.
	 * Change this method to add new types of XML test forms.
	 * @param testFormName The XML name of the test to create
	 * @param atts The attributes of the XML element beginning the test
	 * @return True if this is a valid XML test form
	 * @throws SAXException When an XML nesting error occurs
	 */
	private boolean startTest(String testFormName, Attributes atts) throws SAXException {
		boolean okay = true;
		// Starting a new rule definition, check for some errors
		if (suite == null) {
			throwSAXException("Orphan <" + testFormName + "> element");
			okay = false;
		} else if (test != null) {
			throwSAXException("Found <" + testFormName + "> within element <" + testType + ">");
			okay = false;
		}
		if (!okay) return false;
		// Create the appropriate test structure
		try {
			// Look up the handler class for this test
			String handlerClassName = handlerClasses.get(testFormName);
			if (handlerClassName == null) {
				// This is not a defined test type
				okay = false;
			} else {
				// Try to instantiate an object for this test
				Class handlerClass = Class.forName(handlerClassName);
				Constructor handlerConstructor = handlerClass.getConstructor(new Class[] {String.class, Attributes.class});
				Object handler = handlerConstructor.newInstance(new Object[] {testFormName, atts});
				if (handler instanceof Test) {
					test = (Test) handler;
				}
			}
		} catch (Exception e)	{
			// Didn't work, can't create the test
			throwSAXException("Cannot create handler for <" + testFormName + ">: " + e.toString());
		}
		// Done
		return okay;
	}
	
	/**
	 * Called by the SAX XML reader when an XML end element is encountered.  This
	 * method adds newly defined test to the current test suite.
	 */
	public void endElement(String namespaceURI, String localName, String qualifiedName)
  		throws SAXException {
		if (localName.equalsIgnoreCase(XMLSUITEELEMENT)) {
			// Closing a test suite definition
			if (suite == null) {
				throwSAXException("Orphan </" + XMLSUITEELEMENT + "> element");
			} else if (test != null) {
				throwSAXException("Unexpected </" + XMLSUITEELEMENT + "> element");
			} else if (currentElement != null) {
				throwSAXException("Unexpected </" + XMLSUITEELEMENT + "> element");
			} else {
				suite = null;
				currentElement = null;
			}
			
		} else if (localName.equalsIgnoreCase(XMLHANDLERELEMENT)) {
			// Closing a test handler specification
			if (suite == null) {
				throwSAXException("Orphan </" + XMLHANDLERELEMENT + "> element");
			} else if (test != null) {
				throwSAXException("Unexpected </" + XMLHANDLERELEMENT + "> element");
			} else if (buffer == null) {
				throwSAXException("Unexpected </" + XMLHANDLERELEMENT + "> element");
			} else if (!XMLHANDLERELEMENT.equals(currentElement) || (currentElementName == null)) {
				throwSAXException("Unexpected </" + XMLHANDLERELEMENT + "> element");
			} else {
				String xmlTestName = currentElementName;
				String javaTestHandler = buffer.toString();
				String handlerName = handlerClasses.get(xmlTestName);
				if (handlerName != null) {
					// A duplicate entry, if not just a copy, signal an error
					if (!handlerName.equals(javaTestHandler)) {
						throwSAXException("Duplicate <" + XMLHANDLERELEMENT + " name='" + xmlTestName + "'> element found");
					}
				} else {
					// A new handler, add it to the list
					handlerClasses.put(xmlTestName, javaTestHandler);
				}
				buffer = null;
				currentElement = null;
				currentElementName = null;
			}

		} else if ((test == null) && localName.equalsIgnoreCase(currentElement)) {
			// Closing an open top-level element that isn't a test
			if (suite == null) {
				throwSAXException("Orphan </" + currentElement + "> element");
			} else if (buffer == null) {
				throwSAXException("Unexpected </" + currentElement + "> element");
			} else if (currentElementName == null) {
				throwSAXException("Unexpected </" + currentElement + "> element");
			} else {
				suite.addValue(currentElementName, buffer.toString());
				buffer = null;
				currentElement = null;
				currentElementName = null;
			}
			
		} else if (endTest(localName)) {
			// Closing the most recent test
			test = null;
			currentElement = null;
			
		} else if (test != null) {
			// Closing something internal to a test definition
			try {
				if (!test.endElement(namespaceURI, localName, qualifiedName)) {
					throwSAXException("Found unexpected </" + qualifiedName + "> element");
				}
			} catch (SAXException e) {
				throwSAXException(e.getMessage());
			}
			
		} else {
			// An orphan element
			throwSAXException("Unknown </" + qualifiedName + "> element");
		}
	}
	
	/**
	 * This method adds a completed test to the current test suite.  This
	 * method should not need to be altered when adding a new XML test type.
	 * @param localName The XML element being closed
	 * @return True if the finished test could be added to the test suite
	 * @throws SAXException If an XML nesting problem occurs
	 */
	private boolean endTest(String localName) throws SAXException {
		boolean okay = true;
		// Closing a test definition, make some checks
		if (test == null) {
			//throwSAXException("Orphan </" + localName + "> element");
			okay = false;
		} else if (!testType.equalsIgnoreCase(localName)) {
			//throwSAXException("Found </" + localName + "> element within <" + testType + ">");
			okay = false;
		} 
		if (!okay) return false;
		// Add the test to the suite
		if (!suite.addTest(test)) {
			throwSAXException("Duplicate <" + localName + " name='" + test.getName() + "'> element in suite '" + suite.getName() + "'");
		}
		// Done
		return okay;
	}
	
	/**
	 * Called by the SAX XML reader when characters are encountered between XML
	 * elements.  This method simply buffers them up to be used in <code>endElement()</code>.
	 */
  public void characters(char[] text, int start, int length) throws SAXException {
  	if (test == null) {
  		if (buffer != null)
  			buffer.append(text, start, length);
  	} else {
  		test.characters(text, start, length);
  	}
  }
  
  /**
   * Throws an exception is desired.  Otherwise writes a message to the err output
   * when an XML problem occurs.
   * @param message The message to throw or write
   * @throws SAXException When requested in the constructor for this class
   */
  private void throwSAXException(String message) throws SAXException {
  	String text = null;
  	if (currentLocator != null) {
  		text = message + " at line " + currentLocator.getLineNumber() + ".";
  	} else {
  		text = message;
  	}
  	if (breakOnError) {
  		throw new SAXException(text);
  	} else {
  		System.err.println(text);
  	}
   }

}
