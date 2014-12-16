package edu.uchsc.ccp.opendmap.dev;

import edu.uchsc.ccp.opendmap.ASTConverter;
import edu.uchsc.ccp.opendmap.pattern.ParseException;

/**
 * @author R. James Firby
 */
public class PatternExplorer {

	//private static final String PROJECT_FILE_NAME = "C:\\Program Files\\Protege_3.1_beta\\examples\\newspaper\\newspaper.pprj";

	public static void main(String[] args) throws ParseException{
		String rules = 
		      "{a} := Norm; \n" 
				+ "{a} := Mary, Jane; \n" 
				+ "{b} := (Kelly | Bob) | Frank; \n" 
				+ "{b} := Kelly | Bob | Frank; \n" 
				+ "{c} := Alice*; \n" 
  			+ "{c} := (Alice Julie)+; \n" 
  			+ "{c} := (Alice Julie)* (Jill Jones); \n" 
  			+ "{c} := Alice* | Jill+; \n" 
				+ "{c} := (Alice Julie)* | (Jill Jones); \n" 
				+ "{d} := Alice Fred | Bob; \n"
				//+ "{e} := {Manager} manages {Employee} ; \n"
				+ "{e} := I think [:FROM] manages [:TO]; \n "
				+ "{f} := \"Kelly\"?\"\"Mary\"\" ; \n "
				+ "{f} := '\\141\\142\\143\\n\\\\\\015'; \n "
				;
		ASTConverter.printPatternsFromString(rules, true);
	}
}
