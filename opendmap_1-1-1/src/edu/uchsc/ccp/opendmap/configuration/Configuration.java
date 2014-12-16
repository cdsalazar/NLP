/**
 * The OpenDMAP for Protege Project
 * December 2005
 */
package edu.uchsc.ccp.opendmap.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import edu.uchsc.ccp.opendmap.Parser;

/** 
 * This class implements a configuation facility for the OpenDMAP parser.
 * It reads an XML configuration file and uses the data it includes
 * to configure parser instances.
 * 
 * @author R. James Firby
 */
public class Configuration {
	
	/* The Protege Projects that are configured for this parser to use */
	private ArrayList<ProtegeProject> protegeConfigurations = new ArrayList<ProtegeProject>();
	
	/* The Protege Projects that are configured for this parser to use */
	private ArrayList<Association> associations = new ArrayList<Association>();
	
	/* Any properties included in the parser configuration */
	private Hashtable<String, String> properties = new Hashtable<String, String>();
	
	/* The main configuration file */
	private File configurationFile = null;
	
	/**
	 * Create a new configuration based on the XML configuration file
	 * supplied.
	 * 
	 * @param filename The configuration filename
	 * @throws ConfigurationException When the configuration cannot be read
	 */
	public Configuration(String filename) throws ConfigurationException {
		this(new File(filename));
	}
	
	/**
	 * Create a new configuration based on the XML configuration file
	 * supplied.
	 * 
	 * @param file The configuration file
	 * @throws ConfigurationException When the configuration cannot be read
	 */
	public Configuration(File file) throws  ConfigurationException {
		// Get a handle on the XML configuration file
		if (!file.exists()) {
			throw new ConfigurationException("Configuration file '" + file.getAbsolutePath() + "' does not exist");
		}
		// Create an XML reader
		XMLReader reader = null;;
		try {
			reader = XMLReaderFactory.createXMLReader();
		} catch (SAXException e) {
			// Cannot find XML
			throw new ConfigurationException("No XML implementation can be found", e);
		}
		reader.setContentHandler(new XmlConfigurationHandler(this));			
		// Open the input file for reading
		InputStream fileIn = null;
		try {
			fileIn = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// File not found
			throw new ConfigurationException("Cannot find configuration file '" + file.getAbsolutePath() + "'", e);
		}
		// Parse the XML into this configuration
		configurationFile = file;
		try {
			reader.parse(new InputSource(fileIn));
		} catch (IOException e) {
			// File error
			try {
				fileIn.close();
			} catch (IOException e1) {}
			throw new ConfigurationException("Problem reading configuration file '" + file.getAbsolutePath() + "'", e);
		} catch (SAXException e) {
			// XML parsing error
			try {
				fileIn.close();
			} catch (IOException e1) {}
			throw new ConfigurationException("Invalid configuration '" + file.getAbsolutePath() + "'", e);
		}
		
		
		
		
	}
	
	/**
	 * Add a new Protege Project to this parser configuration.  This
	 * method is called by the XmlConfigurationHandler when a Protege
	 * Project is encountered.
	 * 
	 * @param config The Protege project configuration to add
	 * @see XmlConfigurationHandler
	 */
	protected void addProtegeConfiguration(ProtegeProject config) {
		protegeConfigurations.add(config);
	}
	
	/**
	 * Add a new Protege association to this parser configuration.  This
	 * method is called by the XmlConfigurationHandler when an Association
	 * is encountered.
	 * 
	 * @param assoc The Association configuration to add
	 * @see XmlConfigurationHandler
	 */
	protected void addAssociation(Association assoc) {
		associations.add(assoc);
	}
	
	/**
	 * Add a new property value to this parser configuration.  This
	 * method is called by the XmlConfigurationHandler when an XML
	 * tag is encountered.
	 * 
	 * @param name The name of the property (XML tag name)
	 * @param value The value of the property (XML text content)
	 */
	protected void addPropertyValue(String name, String value) {
		if (name != null) {
			properties.put(name.toLowerCase().trim(), value);
		}
	}
	
	/**
	 * Get the "parent" file for this configuration.
	 * 
	 * @return The parent configuration file.
	 */
	public File parentFile() {
		return configurationFile;
	}
	
	/**
	 * True if this configuration is for a parser that retains case
	 * while it is parsing.
	 * 
	 * @param defaultRetainCase The default case value to return
	 * @return True if this parser should be case sensitive
	 */
	public boolean retainCase(boolean defaultRetainCase) {
		if (properties.containsKey("retaincase")) {
			String value = properties.get("retaincase");
			if (value == null) return defaultRetainCase;
			return Boolean.parseBoolean(value);
		} else {
			return defaultRetainCase;
		}
	}
	
	/**
	 * Configure an OpenDMAP parser according to this configuration.
	 * 
	 * @param parser The parser to configure
	 * @throws ConfigurationException If there is a problem with the configuration
	 */
	public void configure(Parser parser) throws ConfigurationException {
		for (ProtegeProject protege: protegeConfigurations) {
			protege.configure(parser);
		}
		for (Association assoc: associations) {
			assoc.configure(parser, protegeConfigurations);
		}
	}
	
	/**
	 * Configure an OpenDMAP parser according to this configuration, and include supplemental patterns contained in the "supplementalPatterns" String.
	 * @param parser
	 * @param supplementalPatterns
	 * @throws ConfigurationException
	 */
	public void configure(Parser parser, String supplementalPatterns) throws ConfigurationException {
		for (ProtegeProject protege: protegeConfigurations) {
			protege.configure(parser, supplementalPatterns);
		}
		for (Association assoc: associations) {
			assoc.configure(parser, protegeConfigurations);
		}
	}
	

}
