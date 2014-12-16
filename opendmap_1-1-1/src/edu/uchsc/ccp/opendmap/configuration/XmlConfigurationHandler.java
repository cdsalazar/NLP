/**
 * The OpenDMAP for Protege Project
 * December 2005
 */
package edu.uchsc.ccp.opendmap.configuration;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class processes an OpenDMAP XML configuration file.  It
 * is called indirectly by the Java SAX XML parser.
 * 
 * @author R. Jame Firby
 */
public class XmlConfigurationHandler extends DefaultHandler {

	private static final String CONFIGURATION = "configuration";
	private static final String PROTEGEPROJECT = "ProtegeProject";
	private static final String ASSOCIATION = "Association";
	
	/* The configuration being defined */
	private Configuration configuration = null;
	
	/* Things currently being read */
	private ProtegeProject protege = null;
	private Association association = null;
	
	/* State that must be cached while parsing XML */
	private Locator currentLocator = null;
	private StringBuffer buffer = null;
	private String currentElement = null;
	private String currentElementName = null;
	private boolean inConfiguration = false;
	
	/* Whether or not to stop when an XML error is encountered */
	private boolean breakOnError = false;
	
	/**
	 * Create a SAX handler for OpenDMAP configuration XML files.
	 * This handler will add new configuration information to the supplied configuration.
	 * This handler will print warnings but not throw an exception if an XML error is encountered.
	 * @param configuration The configuration to hold the information read by this handler
	 */
	public XmlConfigurationHandler(Configuration configuration) {
		this.configuration = configuration;
	}

	/**
	 * Create a SAX handler for OpenDMAP configuration XML files.
	 * This handler will add new configuration information to the supplied configuration.
	 * @param configuration The configuration to hold the information read by this handler
	 * @param breakOnError If true, throw an exception when an XML error is encountered
	 */
	public XmlConfigurationHandler(Configuration configuration, boolean breakOnError) {
		this.configuration = configuration;
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
	 * the configuration they are defining.
	 */
	public void startElement(String namespaceURI, String localName, String qualifiedName, Attributes atts)
  		throws SAXException {
		if (protege != null) {
			// An element internal to a protege project definition
			try {
				if (!protege.startElement(namespaceURI, localName, qualifiedName, atts)) {
					throwSAXException("Found unexpected <" + qualifiedName + "> element");
				}
			} catch (SAXException e) {
				throwSAXException(e.getMessage());
			}
			
		} else if (association != null) {
			// An element internal to an association definition
			try {
				if (!association.startElement(namespaceURI, localName, qualifiedName, atts)) {
					throwSAXException("Found unexpected <" + qualifiedName + "> element");
				}
			} catch (SAXException e) {
				throwSAXException(e.getMessage());
			}
				
		} else if (currentElement != null) {
			// A top level start element out of context
			throwSAXException("Unexpected <" + qualifiedName + "> element");
			
		} else if (localName.equalsIgnoreCase(CONFIGURATION)) {
			// Starting a new configuration definition
			if (inConfiguration) {
				throwSAXException("Nested <" + CONFIGURATION + "> elements are not allowed");
			} else {
				inConfiguration = true;
				currentElement = null;
			}
			
		} else if (!inConfiguration) {
			// An orphaned element, not in a suite
			throwSAXException("Orphan <" + qualifiedName + "> element.");
			currentElement = null;
			
//		} else if (localName.equalsIgnoreCase(XMLHANDLERELEMENT)) {
//			// A new ProtegeProject definition
//			String name = atts.getValue("name");
//			if (name == null) {
//				throwSAXException("'name' attribute required in <" + XMLHANDLERELEMENT + "> element");
//			} else {
//				buffer = new StringBuffer();
//				currentElement = XMLHANDLERELEMENT;
//				currentElementName = name;
//			}
		
		} else if (startProtegeElement(localName, atts)) {
			// A new protege project, clean up some state
			buffer = null;
			currentElement = localName;
			
		} else if (startAssociationElement(localName, atts)) {
			// A new association, clean up some state
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
	private boolean startProtegeElement(String elementName, Attributes atts) throws SAXException {
		boolean okay = true;
		// Starting a new rule definition, check for some errors
		if (!inConfiguration) {
			throwSAXException("Orphan <" + elementName + "> element");
			okay = false;
		} else if (protege != null) {
			throwSAXException("Found <" + elementName + "> within element <" + elementName + ">");
			okay = false;
		} else if (!PROTEGEPROJECT.equalsIgnoreCase(elementName)) {
			okay = false;
		}
		if (!okay) return false;
		// Create the appropriate handler structure
		protege = new ProtegeProject(elementName, atts, configuration.parentFile().getParentFile());
		// Done
		return okay;
	}
	
	private boolean startAssociationElement(String elementName, Attributes atts) throws SAXException {
		boolean okay = true;
		// Starting a new rule definition, check for some errors
		if (!inConfiguration) {
			throwSAXException("Orphan <" + elementName + "> element");
			okay = false;
		} else if (protege != null) {
			throwSAXException("Found <" + elementName + "> within element <" + elementName + ">");
			okay = false;
		} else if (!ASSOCIATION.equalsIgnoreCase(elementName)) {
			okay = false;
		}
		if (!okay) return false;
		// Create the appropriate handler structure
		association = new Association(elementName, atts);
		// Done
		return okay;
	}

	
	/**
	 * Called by the SAX XML reader when an XML end element is encountered.  This
	 * method adds newly defined test to the current test suite.
	 */
	public void endElement(String namespaceURI, String localName, String qualifiedName)
  		throws SAXException {
		if (localName.equalsIgnoreCase(CONFIGURATION)) {
			// Closing a configuration definition
			if (!inConfiguration) {
				throwSAXException("Orphan </" + CONFIGURATION + "> element");
			} else if (protege != null) {
				throwSAXException("Unexpected </" + CONFIGURATION + "> element");
			} else if (currentElement != null) {
				throwSAXException("Unexpected </" + CONFIGURATION + "> element");
			} else {
				inConfiguration = false;
				currentElement = null;
			}
			
//		} else if (localName.equalsIgnoreCase(XMLHANDLERELEMENT)) {
//			// Closing a test handler specification
//			if (suite == null) {
//				throwSAXException("Orphan </" + XMLHANDLERELEMENT + "> element");
//			} else if (test != null) {
//				throwSAXException("Unexpected </" + XMLHANDLERELEMENT + "> element");
//			} else if (buffer == null) {
//				throwSAXException("Unexpected </" + XMLHANDLERELEMENT + "> element");
//			} else if (!XMLHANDLERELEMENT.equals(currentElement) || (currentElementName == null)) {
//				throwSAXException("Unexpected </" + XMLHANDLERELEMENT + "> element");
//			} else {
//				String xmlTestName = currentElementName;
//				String javaTestHandler = buffer.toString();
//				String handlerName = handlerClasses.get(xmlTestName);
//				if (handlerName != null) {
//					// A duplicate entry, if not just a copy, signal an error
//					if (!handlerName.equals(javaTestHandler)) {
//						throwSAXException("Duplicate <" + XMLHANDLERELEMENT + " name='" + xmlTestName + "'> element found");
//					}
//				} else {
//					// A new handler, add it to the list
//					handlerClasses.put(xmlTestName, javaTestHandler);
//				}
//				buffer = null;
//				currentElement = null;
//				currentElementName = null;
//			}

		} else if ((association == null) && (protege == null) && localName.equalsIgnoreCase(currentElement)) {
			// Closing an open top-level element that isn't a test
			if (!inConfiguration) {
				throwSAXException("Orphan </" + currentElement + "> element");
			} else if (buffer == null) {
				throwSAXException("Unexpected </" + currentElement + "> element");
			} else if (currentElementName == null) {
				throwSAXException("Unexpected </" + currentElement + "> element");
			} else {
				configuration.addPropertyValue(currentElementName, buffer.toString());
				buffer = null;
				currentElement = null;
				currentElementName = null;
			}
			
		} else if (endProtegeElement(localName)) {
			// Closing the most recent test
			protege = null;
			currentElement = null;
			
		} else if (endAssociationElement(localName)) {
			// Closing the most recent test
			association = null;
			currentElement = null;
			
		} else if (protege != null) {
			// Closing something internal to a test definition
			try {
				if (!protege.endElement(namespaceURI, localName, qualifiedName)) {
					throwSAXException("Found unexpected </" + qualifiedName + "> element");
				}
			} catch (SAXException e) {
				throwSAXException(e.getMessage());
			}
			
		} else if (association != null) {
			// Closing something internal to a test definition
			try {
				if (!association.endElement(namespaceURI, localName, qualifiedName)) {
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
	private boolean endProtegeElement(String localName) throws SAXException {
		boolean okay = true;
		// Closing a test definition, make some checks
		if (protege == null) {
			//throwSAXException("Orphan </" + localName + "> element");
			okay = false;
		} else if (!PROTEGEPROJECT.equalsIgnoreCase(localName)) {
			//throwSAXException("Found </" + localName + "> element within <" + testType + ">");
			okay = false;
		}
		if (okay) {
			// Add the protege configuration to the configuration
			configuration.addProtegeConfiguration(protege);
		}
		// Done
		return okay;
	}
	
	private boolean endAssociationElement(String localName) throws SAXException {
		boolean okay = true;
		// Closing a test definition, make some checks
		if (association == null) {
			//throwSAXException("Orphan </" + localName + "> element");
			okay = false;
		} else if (!ASSOCIATION.equalsIgnoreCase(localName)) {
			//throwSAXException("Found </" + localName + "> element within <" + testType + ">");
			okay = false;
		}
		if (okay) {
			// Add the protege configuration to the configuration
			configuration.addAssociation(association);
		}
		// Done
		return okay;
	}
	
	/**
	 * Called by the SAX XML reader when characters are encountered between XML
	 * elements.  This method simply buffers them up to be used in <code>endElement()</code>.
	 */
  public void characters(char[] text, int start, int length) throws SAXException {
  	if (protege == null) {
  		if (buffer != null)
  			buffer.append(text, start, length);
  	} else {
  		protege.characters(text, start, length);
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
