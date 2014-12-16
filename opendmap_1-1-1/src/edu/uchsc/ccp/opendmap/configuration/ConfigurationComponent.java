package edu.uchsc.ccp.opendmap.configuration;

public abstract class ConfigurationComponent {

	/* A buffer for caching character reads from SAX */
	private StringBuffer buffer = null;

	/**
	 * Used by subclasses to begin buffering SAX character reads
	 *
	 */
	void startBuffering() {
		buffer = new StringBuffer();
	}
	
	boolean isBuffer() {
		return (buffer != null);
	}
	
	/**
	 * Used by subclasses to get the text buffered from SAX character reads
	 * 
	 * @return Characters buffered since the last 'startBuffering' call.
	 */
	String getBuffer() {
		return buffer.toString();
	}
	
	String endBuffering() {
		String result = buffer.toString();
		buffer = null;
		return result;
	}
	
	/**
	 * Called by the DMAP XMLConfigurationHandler to buffer characters between element tags.
	 * 
	 * @param text The characters to be buffered and more
	 * @param start The start position of the characters to be buffered
	 * @param length The number of characters to buffer
	 * @see XMLConfigurationHandler
	 */
	protected void characters(char[] text, int start, int length) {
		if (buffer != null) buffer.append(text, start, length);
	}

}
