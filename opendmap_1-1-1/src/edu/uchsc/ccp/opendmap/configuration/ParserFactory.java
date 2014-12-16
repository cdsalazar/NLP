/**
 * The OpenDMAP for Protege Project
 * December 2005
 */
package edu.uchsc.ccp.opendmap.configuration;

import java.io.File;
import java.util.logging.Level;

import edu.uchsc.ccp.opendmap.Parser;

/**
 * This is a factory class for creating new parser instances.  It
 * simplifies some of the configuration of the parser using XML
 * configuration files.
 * <p>
 * Using this class is the best way to create a new parser instance.
 * 
 * @author R. James Firby
 */
public class ParserFactory {
	
	/**
	 * Create a new parser with all properties set to their defaults.
	 * 
	 * @return An OpenDMAP parser
	 */
	public static Parser newParser() {
		return new Parser();
	}
	
	/**
	 * Create a new parser that does, or does not, retain case.
	 * 
	 * @param retainCase True if this parser should respect case, false if the parser should ignore case
	 * @return An OpenDMAP parser
	 */
	public static Parser newParser(boolean retainCase) {
		return new Parser(retainCase);
	}
	
	/**
	 * Create a new parser that does, or does not, retain case.
	 * This parser will generate logging messages at the supplied
	 * level of detail.
	 * 
	 * @param retainCase True if this parser should respect case, false if the parser should ignore case
	 * @param level The logging level for this parser
	 * @return An OpenDMAP parser
	 */
	public static Parser newParser(boolean retainCase, Level level) {
		return new Parser(retainCase, level);
	}
	
	/**
	 * Create a new parser configured according to the supplied
	 * XML configuration file.
	 * 
	 * @param filename The name of the XML configuration file
	 * @return A configured OpenDMAP parser
	 * @throws ConfigurationException When the configuration has a problem
	 */
	public static Parser newParser(String filename) throws ConfigurationException {
		return newParser(new File(filename));
	}

	/**
	 * Create a new parser configured according to the supplied
	 * XML configuration file.
	 * 
	 * @param filename The name of the XML configuration file
	 * @param level The logging level for this parser
	 * @return A configured OpenDMAP parser
	 * @throws ConfigurationException When the configuration has a problem
	 */
	public static Parser newParser(String filename, Level level) throws ConfigurationException {
		return newParser(new File(filename), level);
	}

	/**
	 * Create a new parser configured according to the supplied
	 * XML configuration file.
	 * 
	 * @param file The XML configuration file
	 * @return A configured OpenDMAP parser
	 * @throws ConfigurationException When the configuration has a problem
	 */
	public static Parser newParser(File file) throws ConfigurationException {
		Configuration configuration = new Configuration(file);
		Parser parser = new Parser(configuration.retainCase(Parser.DEFAULT_RETAIN_CASE));
		configuration.configure(parser);
		return parser;
	}
	
	/**
	 * Create a new parser configured according to the supplied
	 * XML configuration file.
	 * 
	 * @param file The XML configuration file
	 * @param level The logging level for this parser
	 * @return A configured OpenDMAP parser
	 * @throws ConfigurationException When the configuration has a problem
	 */
	public static Parser newParser(File file, Level level) throws ConfigurationException {
		Configuration configuration = new Configuration(file);
		Parser parser = new Parser(configuration.retainCase(Parser.DEFAULT_RETAIN_CASE), level);
		configuration.configure(parser);
		return parser;
	}
	
	/**
	 * Create a new parser configured according to the supplied XML configuration file, and including supplemental patterns contained in the "supplementalPatterns" String.
	 * @param configurationFile
	 * @param supplementalPatterns
	 * @return
	 * @throws ConfigurationException
	 */
	public static Parser newParser(File configurationFile, String supplementalPatterns, Level level) throws ConfigurationException{
		Configuration configuration = new Configuration(configurationFile);
		Parser parser = new Parser(configuration.retainCase(Parser.DEFAULT_RETAIN_CASE), level);
		configuration.configure(parser, supplementalPatterns);
		return parser;
	}

}
