package edu.uchsc.ccp.opendmap.dependency;

/**
 * Simple class for storing dependency relations between tokens
 * 
 * @author Bill Baumgartner
 * 
 */
public class DependencyRelation {
	public static final String DET = "det";
	public static final String AMOD = "amod";
	public static final String NSUBJ = "nsubj";
	public static final String ADVMOD = "advmod";
	public static final String PARTMOD = "partmod";
	public static final String PREP = "prep";
	public static final String CCOMP = "ccomp";
	public static final String PRT = "prt";
	public static final String DOBJ = "dobj";
	public static final String RCMOD = "rcmod";
	public static final String COP = "cop";
	public static final String NN = "nn";

	private String type;
	private int governorTokenNumber;
	private int dependentTokenNumber;
	private String governorTokenText;
	private String dependentTokenText;

	public DependencyRelation(String type, int governorTokenNumber, String governorTokenText, int dependentTokenNumber,
			String dependentTokenText) {
		this.type = type;
		this.governorTokenNumber = governorTokenNumber;
		this.dependentTokenNumber = dependentTokenNumber;
		this.governorTokenText = governorTokenText;
		this.dependentTokenText = dependentTokenText;
	}

	public String getGovernorTokenText() {
		return governorTokenText;
	}

	public String getDependentTokenText() {
		return dependentTokenText;
	}

	public String getType() {
		return type;
	}

	public int getGovernorTokenNumber() {
		return governorTokenNumber;
	}

	public int getDependentTokenNumber() {
		return dependentTokenNumber;
	}
}
