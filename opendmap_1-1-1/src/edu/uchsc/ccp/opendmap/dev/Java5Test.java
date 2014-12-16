/*
 * Created on Apr 9, 2005
 *
 */
package edu.uchsc.ccp.opendmap.dev;

import java.util.*;

import edu.uchsc.ccp.opendmap.DMAPItem;
import edu.uchsc.ccp.opendmap.Prediction;

/**
 * @author Will Fitzgerald
 */
public class Java5Test {

	public Map<? extends DMAPItem, Vector<Prediction>> bob = new Hashtable<DMAPItem, Vector<Prediction>>();

	public static void main(String[] args) {
		List<Integer> numbers = new ArrayList<Integer>();
		numbers.add(new Integer(89));
		numbers.add(100);
		Vector<Integer> testV = null;
		if (testV != null) {
			for (Integer i : testV) {
				System.out.println(i);
			}
		}
	}
}
