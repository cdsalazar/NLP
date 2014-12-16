/**
 * The OpenDMAP for Protege Project
 * December 2005
 */
package edu.uchsc.ccp.opendmap.configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.uchsc.ccp.opendmap.Parser;
import edu.uchsc.ccp.opendmap.pattern.ParseException;

/**
 * The <code>ProtegeProject</code> class represents the definition of a single Protege Project to be used by the DMAP
 * parser. A configuration may include multiple Protege configurations.
 * <p>
 * There are two parts to this configuration:
 * <ol>
 * <li>XML handling to read the configuration in from a file
 * <li>A configure() method for applyting the configuration to a parser
 * </ol>
 * <p>
 * The configuration uses SAX for reading configuration files. Each time a configuration element is read it will call
 * the startElement and endElement methods.
 * <p>
 * When a new parser is configured, the configure() method will be called.
 * 
 * @author R. James Firby
 */
public class ProtegeProject extends ConfigurationComponent {

	private static final String PROJECTFILE = "ProjectFile";
	private static final String PATTERNFILE = "PatternFile";
	private static final String PATTERNSLOT = "PatternSlot";

	/* The name assigned to this Protege Project configuration in the XML file */
	private String name = null;
	private KnowledgeBase kb = null;

	private String projectFilename = null;
	private ArrayList<String> patternFiles = new ArrayList<String>();
	private ArrayList<String[]> patternSlots = new ArrayList<String[]>();

	private File parentFile = null;

	/* State needed while parsing the XML */
	private String currentElement = null;
	private String attributeValue = null;

	/**
	 * Create a new Protege configuration with the given attributes.
	 * 
	 * @param kind
	 *            The kind of XML element generating this configuration
	 * @param attributes
	 *            The XML attributes passed to the element for this configuration
	 * @param parent
	 *            The file holding the XML being parsed
	 * @throws SAXException
	 *             When there are errors in the attributes
	 */
	protected ProtegeProject(String kind, Attributes attributes, File parent) throws SAXException {
		parentFile = parent;
		// Grab some standard arguments
		name = attributes.getValue("name");
		if (name == null)
			name = "unnamed";
	}

	/**
	 * Get the name assigned to this configuration in the XML file.
	 * 
	 * @return The Protege Project configuration name.
	 */
	public String getName() {
		return name;
	}

	public KnowledgeBase getKnowledgeBase() {
		return kb;
	}

	/**
	 * Called by the DMAP XMLConfigurationHandler when an XML element internal to a ProtegeProject element is
	 * encountered.
	 * 
	 * @param namespaceURI
	 *            The element XML namespace
	 * @param localName
	 *            The basic element name
	 * @param qualifiedName
	 *            A fully qualified element name
	 * @param atts
	 *            A SAX attributes set for the element
	 * @return True if this function makes use of the element, False otherwise
	 * @throws ConfigurationException
	 *             When there is a problem with the subclass parsing XML
	 * @see org.xml.sax.Handler
	 * @see XMLConfigurationHandler
	 */
	protected boolean startElement(String namespaceURI, String localName, String qualifiedName, Attributes atts) throws SAXException {
		boolean consumed = true;
		if (currentElement != null) {
			// An unexpected element
			throwSaxException("Found unexpected <" + qualifiedName + "> nested within <" + currentElement + "> element");

		} else if (localName.equalsIgnoreCase(PROJECTFILE)) {
			// An ProjectFile element
			if (projectFilename == null) {
				currentElement = PROJECTFILE;
				startBuffering();
			} else {
				throwSaxException("Found multiple <" + PROJECTFILE + "> elements");
			}

		} else if (localName.equalsIgnoreCase(PATTERNFILE)) {
			// A PatternFile element
			currentElement = PATTERNFILE;
			startBuffering();

		} else if (localName.equalsIgnoreCase(PATTERNSLOT)) {
			// A PatternSlot element
			currentElement = PATTERNSLOT;
			attributeValue = atts.getValue("root");
			startBuffering();

		} else {
			// An unknown element
			consumed = false;
			// throw new SAXException("Found unknown <" + qualifiedName + "> element");
		}
		return consumed;

	}

	/**
	 * Called by the DMAP XMLConfigurationHandler when an XML element close is encountered internal to a ProtegeProject
	 * element.
	 * 
	 * @param namespaceURI
	 *            The element XML namespace
	 * @param localName
	 *            The basic element name
	 * @param qualifiedName
	 *            A fully qualified element name
	 * @return True if this function makes use of the element, False otherwise
	 * @throws ConfigurationException
	 *             When there is a problem with the subclass parsing XML
	 * @see org.xml.sax.Handler
	 * @see XmlConfigurationHandler
	 */
	protected boolean endElement(String namespaceURI, String localName, String qualifiedName) throws SAXException {
		boolean consumed = true;
		if (currentElement == null) {
			// Bad nesting
			throwSaxException("Found unexpected </" + qualifiedName + "> element");

		} else if (localName.equals(PROJECTFILE)) {
			// A ProjectFile element
			if ((projectFilename == null) && (PROJECTFILE.equalsIgnoreCase(currentElement))) {
				projectFilename = endBuffering();
				currentElement = null;
			} else {
				throwSaxException("Found </" + PROJECTFILE + "> nested within <" + currentElement + "> element");
			}

		} else if (localName.equals(PATTERNFILE)) {
			// A PatternFile element
			if (PATTERNFILE.equalsIgnoreCase(currentElement)) {
				String filename = endBuffering().trim();
				if (patternFiles.contains(filename)) {
					throwSaxException("Duplicate pattern file entry '" + filename + "'");
				}
				patternFiles.add(filename);
				currentElement = null;
			} else {
				throwSaxException("Found </" + PATTERNFILE + "> nested within <" + currentElement + "> element");
			}

		} else if (localName.equals(PATTERNSLOT)) {
			// A PatternSlot element
			if (PATTERNSLOT.equalsIgnoreCase(currentElement)) {
				String root = attributeValue;
				String slot = endBuffering().trim();
				if (alreadyHaveSlot(root, slot)) {
					throwSaxException("Duplicate pattern file entry ");
				}
				patternSlots.add(new String[] { root, slot });
				currentElement = null;
			} else {
				throwSaxException("Found </" + PATTERNSLOT + "> nested within <" + currentElement + "> element");
			}

		} else {
			// An unknown element
			// throw new SAXException("Found unexpected </" + qualifiedName + "> element");
			consumed = false;
		}
		return consumed;

	}

	/**
	 * Check to see if this configuration already holds a reference to the pattern slot (or subsumes it).
	 * 
	 * @param root
	 *            The Protege concept that is the root of these patterns
	 * @param slot
	 *            The name of the slot holding the patterns in concepts at or below this root
	 * @return
	 */
	private boolean alreadyHaveSlot(String root, String slot) {
		for (String[] pair : patternSlots) {
			if (slot.equals(pair[1])) {
				if ((root == null) || (pair[0] == null))
					return true;
				if (root.equals(pair[0]))
					return true;
			}
		}
		return false;
	}

	/**
	 * Throw a SAX exception and add the name of the ProtegeProject element if one was given.
	 * 
	 * @param message
	 *            The reason for the exception
	 * @throws SAXException
	 */
	private void throwSaxException(String message) throws SAXException {
		String error = message;
		if (name != null)
			error = message + " in ProtegeProject configuration '" + name + "'";
		throw new SAXException(error);
	}

	/**
	 * Use the information read in from the XML configuration file to configure an OpenDMAP parser.
	 * 
	 * @param parser
	 *            The parser to configure
	 * @throws ConfigurationException
	 *             When there is a problem with the configuration
	 */
	protected void configure(Parser parser) throws ConfigurationException {
		if (projectFilename == null) {
			throwConfigurationException("Missing Protege project file");
		}
		KnowledgeBase kb = loadProtegeProject((new File(parentFile, projectFilename)).getAbsolutePath());
		for (String file : patternFiles) {
			loadPatternsFromFile(parser, (new File(parentFile, file)).getAbsolutePath(), kb);
		}
		for (String[] pair : patternSlots) {
			loadPatternsFromSlot(parser, pair[0], pair[1], kb);
		}
	}

	/**
	 * Augments the patterns indicated by the XML configuration file with those listed in the input "patternsString." This method is helpful for testing purposes in particular.
	 * 
	 * @param parser
	 * @param patternsString
	 * @throws ConfigurationException
	 */
	protected void configure(Parser parser, String patternsString) throws ConfigurationException {
		configure(parser);
		loadPatternsFromString(parser, patternsString, kb);
	}

	/**
	 * Load a protege project so that it can be referenced in pattern files and slots.
	 * 
	 * @param filename
	 *            The name of the Protege project file
	 * @return The Protege knowledge base corresponding to this project
	 * @throws ConfigurationException
	 *             When the project cannot be loaded
	 */
	private KnowledgeBase loadProtegeProject(String filename) throws ConfigurationException {
		Collection errors = new ArrayList();
		Project project = new Project(filename, errors);
		if (errors.size() != 0) {
			// Throw the first error if one encountered
			Object e = errors.iterator().next();
			if (e instanceof Throwable) {
				throwConfigurationException("Cannot create Protege project '" + filename + "'", (Throwable) e);
			} else {
				throwConfigurationException("Cannot create Protege project '" + filename + "': " + e.toString());
			}
		} else {
			kb = project.getKnowledgeBase();
		}
		return kb;
	}

	/**
	 * Load a pattern file into the parser.
	 * 
	 * @param parser
	 *            The parser to be configured
	 * @param filename
	 *            The pattern file to load into the parser
	 * @param kb
	 *            The Protege knowlege base for these patterns
	 * @throws ConfigurationException
	 *             When the pattern file cannot be loaded
	 */
	private void loadPatternsFromFile(Parser parser, String filename, KnowledgeBase kb) throws ConfigurationException {
		try {
			parser.addPatternsFromFile(filename, kb);
		} catch (ParseException e) {
			// Translate to a configuration exception
			String message = "Problem loading pattern file: " + e.getMessage();
			throwConfigurationException(message, e);
		}
	}

	/**
	 * Load patterns into the parser from a Protege slot. If a root is specified then only slots on that frame or its
	 * children are considered. If the root is null, all frames are considered.
	 * 
	 * @param parser
	 *            The parser to be configured
	 * @param root
	 *            The root frame to look for patterns on
	 * @param slot
	 *            The slot name to use for the patterns
	 * @param kb
	 *            The Protege knowledge base holding the root and slots
	 * @throws ConfigurationException
	 *             When the patterns from the slots cannot be loaded
	 */
	private void loadPatternsFromSlot(Parser parser, String root, String slot, KnowledgeBase kb) throws ConfigurationException {
		try {
			parser.addPatternsFromProtegeFrame(root, slot, kb);
		} catch (ParseException e) {
			// Translate to a configuration exception
			String message = "Problem processing pattern slot '" + slot + "'";
			if (root != null)
				message = message + " in subtree '" + root + "'";
			throwConfigurationException(message, e);
		}
	}

	/**
	 * Loads patterns into the parser from the input String.
	 * 
	 * @param parser
	 * @param patternsString
	 * @param kb
	 * @throws ConfigurationException
	 */
	private void loadPatternsFromString(Parser parser, String patternsString, KnowledgeBase kb) throws ConfigurationException {
		try {
			parser.addPatternsFromString(patternsString, kb);
		} catch (ParseException e) {
			// Translate to a configuration exception
			String message = "Problem loading patterns from String: " + e.getMessage();
			throwConfigurationException(message, e);
		}
	}

	/**
	 * Attach the project name to Configuration error before throwing it.
	 * 
	 * @param message
	 *            The error message
	 * @throws ConfigurationException
	 */
	private void throwConfigurationException(String message) throws ConfigurationException {
		throwConfigurationException(message, null);
	}

	/**
	 * Attach the project name to Configuration error before throwing it.
	 * 
	 * @param message
	 *            The error message
	 * @param e
	 *            The exception that caused this error (if there is one)
	 * @throws ConfigurationException
	 */
	private void throwConfigurationException(String message, Throwable e) throws ConfigurationException {
		String error = message;
		if (name != null)
			error = message + " in ProtegeProject configuration '" + name + "'";
		if (e == null) {
			throw new ConfigurationException(error);
		} else {
			throw new ConfigurationException(error, e);
		}
	}

}
