/**
 * The OpenDMAP for Protege Project
 * December 2005
 */
package edu.uchsc.ccp.opendmap.configuration;

import java.util.Collection;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.uchsc.ccp.opendmap.Parser;
/**
 * This class represents an association between frames in two
 * different Protege projects.
 * 
 * <pre>
 *  Association type="isa"
 *    Parent project="kb1" frame="f1"
 *    Child  project="kb2" frame="f2"
 * </pre>
 * 
 * @author R. James Firby
 */
public class Association {

	private static final String ASSOCIATION = "Association";
	
	private static final String ASSOCIATION_ISA = "isa";
	
	private static final String PARENT = "Parent";
	private static final String CHILD = "Child";

	/* The type of association */
	private String association = null;
	
	private String parentProject = null;
	private String parentFrame = null;
	private String childProject = null;
	private String childFrame = null;
	
	/* A buffer for caching character reads from SAX */
	private String currentElement = null;
	
	/**
	 * Create a new Protege configuration with the given attributes.
	 * 
	 * @param kind The kind of XML element generating this configuration
	 * @param attributes The XML attributes passed to the element for this configuration
	 * @throws SAXException When there are errors in the attributes
	 */
	protected Association(String kind, Attributes attributes) throws SAXException {
		// Grab some standard arguments
		association = attributes.getValue("type");
		if (association == null)
			throw new SAXException("Missing type attribute in " + ASSOCIATION);
		if (!association.equalsIgnoreCase(ASSOCIATION_ISA))
			throw new SAXException("Invalid type attribute '" + association + "' in " + ASSOCIATION);
	}
	
	/**
	 * Called by the DMAP XMLConfigurationHandler when an XML element internal to a
	 * ProtegeProject element is encountered.
	 * 
	 * @param namespaceURI The element XML namespace
	 * @param localName The basic element name
	 * @param qualifiedName A fully qualified element name
	 * @param atts A SAX attributes set for the element
	 * @return True if this function makes use of the element, False otherwise
	 * @throws ConfigurationException When there is a problem with the subclass parsing XML
	 * @see org.xml.sax.Handler
	 * @see XMLConfigurationHandler
	 */
	protected boolean startElement(String namespaceURI, String localName, String qualifiedName, Attributes atts)
			throws SAXException {
		boolean consumed = true;
		if (currentElement != null) {
			// An unexpected element
			throwSaxException("Found unexpected <" + qualifiedName + "> nested within <" + currentElement + "> element");
		
		} else if (localName.equalsIgnoreCase(PARENT)) {
			// A Parent element
			if (parentProject == null) {
				parentProject = atts.getValue("project");
				parentFrame = atts.getValue("frame");
				if (parentProject == null)
					throwSaxException("Missing 'project' attribute in " + PARENT + " element"); 
				if (parentFrame == null)
					throwSaxException("Missing 'frame' attribute in " + PARENT + " element"); 
				currentElement = PARENT;
			} else {
				throwSaxException("Found multiple <" + PARENT + "> elements");
			}
			
		} else if (localName.equalsIgnoreCase(CHILD)) {
			// A Child element
			if (childProject == null) {
				childProject = atts.getValue("project");
				childFrame = atts.getValue("frame");
				if (childProject == null)
					throwSaxException("Missing 'project' attribute in " + CHILD + " element"); 
				if (childFrame == null)
					throwSaxException("Missing 'frame' attribute in " + CHILD + " element"); 
				currentElement = CHILD;
			} else {
				throwSaxException("Found multiple <" + CHILD + "> elements");
			}
			
		} else {
			// An unknown element
			consumed = false;
			//throw new SAXException("Found unknown <" + qualifiedName + "> element");			
		}
		return consumed;

	}
	
	/**
	 * Called by the DMAP XMLConfigurationHandler when an XML element close is encountered
	 * internal to a ProtegeProject element.
	 * 
	 * @param namespaceURI The element XML namespace
	 * @param localName The basic element name
	 * @param qualifiedName A fully qualified element name
	 * @return True if this function makes use of the element, False otherwise
	 * @throws ConfigurationException When there is a problem with the subclass parsing XML
	 * @see org.xml.sax.Handler
	 * @see XmlConfigurationHandler
	 */
	protected boolean endElement(String namespaceURI, String localName, String qualifiedName)
  		throws SAXException {
		boolean consumed = true;
		if (currentElement == null) {
			// Bad nesting
			throwSaxException("Found unexpected </" + qualifiedName + "> element");			
			
		} else if (localName.equals(PARENT)) {
			// A Parent element
			if (PARENT.equalsIgnoreCase(currentElement)) {
				currentElement = null;
			} else {
				throwSaxException("Found </" + PARENT + "> nested within <" + currentElement + "> element");
			}
			
		} else if (localName.equals(CHILD)) {
			// A Child element
			if (CHILD.equalsIgnoreCase(currentElement)) {
				currentElement = null;
			} else {
				throwSaxException("Found </" + CHILD + "> nested within <" + currentElement + "> element");
			}
			
		} else {
			// An unknown element
			//throw new SAXException("Found unexpected </" + qualifiedName + "> element");		
			consumed = false;
		}		
		return consumed;

	}
	
	/**
	 * Throw a SAX exception and add the name of the ProtegeProject element
	 * if one was given.
	 * 
	 * @param message The reason for the exception
	 * @throws SAXException
	 */
	private void throwSaxException(String message) throws SAXException {
		String error = message + " in " + ASSOCIATION;
		throw new SAXException(error);
	}

	
	/**
	 * Use the information read in from the XML configuration file to 
	 * configure an OpenDMAP parser.
	 * 
	 * @param parser The parser to configure
	 * @throws ConfigurationException When there is a problem with the configuration
	 */
	protected void configure(Parser parser, Collection<ProtegeProject> protegeProjects) throws ConfigurationException {
		// Find the parent frame
		if (parentProject == null)
			throwConfigurationException("Missing parent project attribute");
		if (parentFrame == null)
			throwConfigurationException("Missing parent frame attribute");
		KnowledgeBase kb = getProtegeKnowledgeBase(parentProject, protegeProjects);
		if (kb == null)
			throwConfigurationException("Cannot find Protege Project definition '" + parentProject + "'");
		Frame parent = kb.getFrame(parentFrame);
		// Find the child frame
		if (childProject == null)
			throwConfigurationException("Missing child project attribute");
		if (childFrame == null)
			throwConfigurationException("Missing child frame attribute");
		kb = getProtegeKnowledgeBase(childProject, protegeProjects);
		if (kb == null)
			throwConfigurationException("Cannot find Protege Project definition '" + childProject + "'");
		Frame child = kb.getFrame(childFrame);
		// Add the ISA association to the Parser's protege project group
		parser.getProtegeProjectGroup().addIsaRelationship(child, parent);
	}
	
	/**
	 * Get a particular knowledge base given its configuration file "name".
	 * 
	 * @param name The name of the project in the configuration file
	 * @param protegeProjects The collection of projects loaded in the configuration
	 * @return The Protege Knowledge Base for the project with the given name
	 */
	private KnowledgeBase getProtegeKnowledgeBase(String name, Collection<ProtegeProject> protegeProjects) {
		for (ProtegeProject project: protegeProjects) {
			if (name.equalsIgnoreCase(project.getName())) {
				return project.getKnowledgeBase();
			}
		}
		return null;
	}
	
	/**
	 * Attach the project name to Configuration error before throwing it.
	 * 
	 * @param message The error message
	 * @throws ConfigurationException
	 */
	private void throwConfigurationException(String message) throws ConfigurationException {
		throwConfigurationException(message, null);
	}
	
	/**
	 * Attach the project name to Configuration error before throwing it.
	 * 
	 * @param message The error message
	 * @param e The exception that caused this error (if there is one)
	 * @throws ConfigurationException
	 */
	private void throwConfigurationException(String message, Throwable e) throws ConfigurationException {
		String error = message + " in " + ASSOCIATION;
		if (e == null) {
			throw new ConfigurationException(error);
		} else {
			throw new ConfigurationException(error, e);
		}
	}

}
