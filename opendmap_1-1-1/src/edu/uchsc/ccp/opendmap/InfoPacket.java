/**
 * The OpenDMAP for Protege Project
 * July 2005
 */
package edu.uchsc.ccp.opendmap;

import java.util.ArrayList;
import java.util.Collection;

/**
 * An <code>InfoPacket</code> is a binding between a DMAPItem
 * that was expected in a pattern, and the specific value, or
 * Reference that was matched against that item.
 * <p>
 * Typically, info packets bind slots to references that might
 * fill them.
 * 
 * @author Will Fitzgerald
 * @author R. James Firby
 */
public class InfoPacket {
	
	/* The DMPAItem key concept that was waiting to be recognized */
	private DMAPItem key = null;

	/* The Reference that was found and matched against the key */
	private Reference value = null;

	/**
	 * Create a new <code>InfoPacket</code> binding the key to the value.
	 * 
	 * @param key The DMAPItem that was expected.
	 * @param value The reference that was found that matched the key.
	 */
	public InfoPacket(DMAPItem key, Reference value) {
		super();
		this.key = key;
		this.value = value;
	}

	/**
	 * Get the key associated with this packet.
	 * 
	 * @return The key associated with this packet.
	 */
	public DMAPItem getKey() {
		return key;
	}

	/**
	 * Get the value associated with this packet.
	 * 
	 * @return The value associated with this packet.
	 */
	public Reference getValue() {
		return value;
	}

	/**
	 * Get a string suitable for debugging output.
	 */
	public String toString() {
		return key.toString() + "/" + value.toString();
	}
	
	/**
	 * Get a string describing the binding represented by this
	 * <code>InfoPacket</code>.
	 * 
	 * @return A string describing the binding in this packet.
	 */
	private String getReferenceString() {
		return key.getReferenceString(null, null) + "=" + value.getReferenceString();
	}
	
	/**
	 * Get a string that describes a collection of information packets.
	 * 
	 * @param information The packets to include in the string.
	 * @return The string describing the information.
	 */
	public static String getInformationString(Collection<InfoPacket> information) {
		if ((information == null) || (information.isEmpty())) {
			return "";
		} else {
			// Sort the information packets to get a consistent ordering
			ArrayList<InfoPacket> packets = sortInformation(information);
			// Generate an output string for the sorted information
			StringBuilder sb = new StringBuilder();
			boolean isFirst = true;
			for (InfoPacket ip: packets) {
				if (!isFirst) sb.append(" ");
				isFirst = false;
				sb.append(ip.getReferenceString());
			}
			return sb.toString();
		}
	}
	
	/**
	 * Sort information packets according to the start position of their references.
	 * 
	 * @param information The packets to be sorted.
	 * @return The sorted result.
	 */
	private static ArrayList<InfoPacket> sortInformation(Collection<InfoPacket> information) {
		ArrayList<InfoPacket> results = new ArrayList<InfoPacket>();
		for (InfoPacket ip: information) {
			int i = 0;
			while (i < results.size()) {
				if (ip.getValue().getStart() < results.get(i++).getValue().getStart()) {
					break;
				}
			}
			results.add(i, ip);
		}
		return results;
	}
	
//	public static InfoPacket newInfoPacket(String key, String value) {
//		return new InfoPacket(new TextItem(key), new Reference(new TextItem(value), 0, 0, 0, null, 0, 0));
//	}

}
