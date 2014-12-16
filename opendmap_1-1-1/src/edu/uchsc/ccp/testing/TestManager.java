/**
 * The OpenDMAP for Protege Project
 * July 2005
 */
package edu.uchsc.ccp.testing;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


/**
 * A <code>TestManager</code> holds a set of <code>TestSuite</code> objects.
 * 
 * @author firby
 * @see TestSuite
 */
public class TestManager {
	
	/* An XML test file reader */
	private XMLReader reader = null;
	
	/* The table of test suites */
	private Hashtable<String, TestSuite> suites = new Hashtable<String, TestSuite>();
	
	/**
	 * Gets a specific test suite from this manager.
	 * @param name The name of the test suite to return
	 * @return The names test suite or <code>null</code> if it does not exist
	 */
	public TestSuite getTestSuite(String name) {
		if (name == null) {
			return null;
		} else {
			TestSuite suite = suites.get(name);
			if (suite == null) {
				suite = new TestSuite(name);
				suites.put(name, suite);
			}
			return suite;
		}
	}
	
	/**
	 * Gets the number of test suites held by this manager.
	 * @return The number of contains test suites
	 */
	public int numTestSuites() {
		return suites.size();
	}
	
	/**
	 * Gets all of the test suties held by this manager.
	 * @return All of the test suites
	 */
	public Enumeration getTestSuites() {
		return suites.elements();
	}
	
	/**
	 * Read an XML test suite file into this <code>TestManager</code>.
	 * 
	 * @param filename The name of the XML test suite file.
	 * @throws IOException If there is an error accessing the file.
	 * @throws SAXException If there is an XML parsing error while reading the file.
	 */
	public void readXMLTestFile(String filename) throws IOException, SAXException {
		// Create a reader, if necessary
		if (reader == null) {
			reader = XMLReaderFactory.createXMLReader();
			reader.setContentHandler(new XMLTestHandler(this));			
		}
		// Open the input file for reading
		InputStream fileIn = new FileInputStream(filename);
		// Parse the XML into Test Cases
		try {
			reader.parse(new InputSource(fileIn));
		} catch (IOException e) {
			// File error
			fileIn.close();
			throw e;
		} catch (SAXException e) {
			// XML parsing error
			fileIn.close();
			throw e;
		}
	}


}
