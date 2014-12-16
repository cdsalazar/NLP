/**
 * The OpenDMAP for Protege Project
 * July 2005
 */
package edu.uchsc.ccp.testing;

import java.io.PrintStream;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


/**
 * The <code>Test</code> abstract class represents a single DMAP parser test.
 * <p>
 * There are two parts to implementing a test:
 * <ol>
 *  <li> XML handling to read the test in from a test file
 *  <li> A run() method for executing the test
 * </ol>
 * <p>
 * The test facility uses SAX for reading test suite files.
 * Each time a test definition element is read it will call 
 * the startElement and endElement methods so the specific 
 * test can configure itself.
 * <p>
 * When a test suite is executed, the run() method will be
 * called.
 * 
 * @author R. James Firby
 *
 */

/* Changes (most recent first)
 * 
 */
public abstract class Test {

	/* The name assigned to this test in the XML file */
	private String name = null;
	private boolean shouldFail = false;
	/* A buffer for caching character reads from SAX */
	private StringBuffer buffer = null;
	
	/**
	 * Creates a new test with the given attributes.
	 * 
	 * @param kind The kind of XML element generating this test
	 * @param attributes The XML attributes passed to the form for this test
	 * @throws SAXException When there are errors in the attributes
	 */
	protected Test(String kind, Attributes attributes) throws SAXException {
		// Grab some standard arguments
		name = attributes.getValue("name");
		if (name == null) {
			throw new SAXException("'name' attribute required in <" + kind + "> element");
		}
		String fail = attributes.getValue("fail");
		if (fail != null) shouldFail = Boolean.parseBoolean(fail);
	}
	
	/**
	 * Creates an empty, nonfunctional test.  This constructor
	 * exists so that subclasses can implement the <code>TestContext</code>
	 * interface if desired.  
	 */
	protected Test() {}
	
	/**
	 * Gets the name assigned to this test in the XML file.
	 * 
	 * @return The test's name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the name assigned to this test.  Used to override name
	 * when duplicates names are detected.
	 */
	void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Check if this test is supposed to fail.
	 * 
	 * @return True if it is supposed to fail.
	 */
	public boolean isFailTest() {
		return shouldFail;
	}
	
	/**
	 * Used by subclasses to begin buffering SAX character reads
	 *
	 */
	protected void startBuffering() {
		buffer = new StringBuffer();
	}
	
	/**
	 * Used by subclasses to get the text buffered from SAX character reads
	 * 
	 * @return Characters buffered since the last 'startBuffering' call.
	 */
	protected String getBuffer() {
		return buffer.toString();
	}
	
	/**
	 * Called by the DMAP XMLTestHandler to buffer characters between element tags.
	 * 
	 * @param text The characters to be buffered and more
	 * @param start The start position of the characters to be buffered
	 * @param length The number of characters to buffer
	 * @see XMLTestHandler
	 */
	protected void characters(char[] text, int start, int length) {
		if (buffer != null) buffer.append(text, start, length);
	}

	/**
	 * Called by the DMAP XMLTestHandler when an XML element internal to a
	 * test is encountered.
	 * 
	 * @param namespaceURI The element XML namespace
	 * @param localName The basic element name
	 * @param qualifiedName A fully qualified element name
	 * @param atts A SAX attributes set for the element
	 * @return True if this function makes use of the element, False otherwise
	 * @throws SAXException When there is a problem with the subclass parsing XML
	 * @see org.xml.sax.Handler
	 * @see XMLTestHandler
	 */
	protected abstract boolean startElement(String namespaceURI, String localName, String qualifiedName, Attributes atts)
			throws SAXException;
	
	/**
	 * Called by the DMAP XMLTestHandler when an XML element close internal to a
	 * test is encountered.
	 * 
	 * @param namespaceURI The element XML namespace
	 * @param localName The basic element name
	 * @param qualifiedName A fully qualified element name
	 * @return True if this function makes use of the element, False otherwise
	 * @throws SAXException When there is a problem with the subclass parsing XML
	 * @see org.xml.sax.Handler
	 * @see XMLTestHandler
	 */
	protected abstract boolean endElement(String namespaceURI, String localName, String qualifiedName)
  		throws SAXException;
	
	/**
	 * Called by TestSuite when this test is run.
	 * 
	 * @param context The testing context to use for this test
	 * @param stream Location to write information about failed tests
	 * @return Whether or not the test succeeds
	 * @see TestSuite#run
	 */
	public abstract boolean run(TestContext context, PrintStream stream);
 
}
