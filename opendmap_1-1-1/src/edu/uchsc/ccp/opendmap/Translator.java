package edu.uchsc.ccp.opendmap;

import edu.uchsc.ccp.opendmap.Reference;
import java.util.Collection;
import java.lang.StringBuilder;

public class Translator{

	private static String nl;
	private static Collection<Reference> matches;

	public Translator(String in, Collection<Reference> in1){
		//fill in
		nl = in;
		matches = in1;
	}

	//using the natural language and the matches, translate the code to NL
	public String translate(){
		//fill in
		StringBuilder code = new StringBuilder();
		int indent = 0;
		int variableCount = 0;
		int intCount = 0;
		int printIndex = 0;
		int peramIndex = 0;
		int peramSentIndex = 0;
		int thanIndex = 0;
		boolean callFlag = false;
		boolean ifFlag = false;
		boolean orFlag = false;

		for(Reference r : matches){


			if (r.getReferenceString().equals("{g-np [object]={c-if}}")){
				StringBuilder varName = new StringBuilder();
				int tempVarCount = variableCount;
				for(int i = 0; i < this.nl.length(); i++){
					if((int)nl.charAt(i) == 39){
						if(tempVarCount == 0){
							for(int j = i+1; this.nl.charAt(j) != 39; j++){
								varName.append(this.nl.charAt(j));								
							}
							variableCount += 2;
							break;
						}
						else
							tempVarCount--;
					}
				}
				if(indent > 0){
					StringBuilder tabs = new StringBuilder();
					for (int i = 0; i < indent; i++)
						tabs.append("\t");
					code.append(tabs.toString() + "if(" + varName);
				}
				else
					code.append("if(" + varName);
				indent++;
				ifFlag = true;
			}

			else if (r.getReferenceString().equals("{g-np [object]={c-else-if}}")){
				StringBuilder varName = new StringBuilder();
				int tempVarCount = variableCount;
				for(int i = 0; i < this.nl.length(); i++){
					if((int)nl.charAt(i) == 39){
						if(tempVarCount == 0){
							for(int j = i+1; this.nl.charAt(j) != 39; j++){
								varName.append(this.nl.charAt(j));								
							}
							variableCount += 2;
							break;
						}
						else
							tempVarCount--;
					}
				}
				if(indent > 0){
					StringBuilder tabs = new StringBuilder();
					for (int i = 0; i < indent-1; i++)
						tabs.append("\t");
					code.append(tabs.toString() + "elif(" + varName);
				}
				else
					code.append("elif(" + varName);
				ifFlag = true;
			}

			else if (r.getReferenceString().equals("{g-np [object]={c-else}}")){
				if(indent > 0){
					StringBuilder tabs = new StringBuilder();
					for (int i = 0; i < indent-1; i++)
						tabs.append("\t");
					code.append(tabs.toString() + "else:\n");
				}
				else
					code.append("else:\n");
			}

			else if (r.getReferenceString().equals("{g-np [object]={c-list}}")){
				StringBuilder varName = new StringBuilder();
				int tempVarCount = variableCount;
				for(int i = 0; i < this.nl.length(); i++){
					if((int)nl.charAt(i) == 39){
						if(tempVarCount == 0){
							for(int j = i+1; this.nl.charAt(j) != 39; j++){
								varName.append(this.nl.charAt(j));								
							}
							variableCount += 2;
							break;
						}
						else
							tempVarCount--;
					}
				}
				if(!code.toString().contains(varName))
					code.append(varName + " = []\n");
			}

			// else if (r.getReferenceString().equals("{g-np [object]={c-list-operation}}")){
			// 	code.append("list operation was caught");
			// }	

			else if (r.getReferenceString().equals("{g-np [object]={c-indent}}")){
				indent--;
			}

			else if (r.getReferenceString().equals("{g-np [object]={c-function}}")){
				if(!callFlag){
					StringBuilder varName = new StringBuilder();
					int tempVarCount = variableCount;
					for(int i = 0; i < this.nl.length(); i++){
						if((int)nl.charAt(i) == 39){
							if(tempVarCount == 0){
								for(int j = i+1; this.nl.charAt(j) != 39; j++){
									varName.append(this.nl.charAt(j));								
								}
								variableCount += 2;
								break;
							}
							else
								tempVarCount--;
						}
					}
					code.append(varName + "(");
					indent = 1;
				}
			}

			else if (r.getReferenceString().equals("{g-np [object]={c-call}}")){
				StringBuilder varName = new StringBuilder();
					int tempVarCount = variableCount;
					for(int i = 0; i < this.nl.length(); i++){
						if((int)nl.charAt(i) == 39){
							if(tempVarCount == 0){
								for(int j = i+1; this.nl.charAt(j) != 39; j++){
									varName.append(this.nl.charAt(j));								
								}
								variableCount += 2;
								break;
							}
							else
								tempVarCount--;
						}
					}
					code.append(varName + "(");
					indent = 1;
					callFlag = true;
			}

			// else if (r.getReferenceString().equals("{g-np [object]={c-import}}")){
			// 	code.append("It caught import");
			// }

			else if (r.getReferenceString().equals("{g-np [object]={c-for}}")){
				StringBuilder varName = new StringBuilder();
				int tempVarCount = variableCount;
				for(int i = 0; i < this.nl.length(); i++){
					if((int)nl.charAt(i) == 39){
						if(tempVarCount == 0){
							for(int j = i+1; this.nl.charAt(j) != 39; j++){
								varName.append(this.nl.charAt(j));								
							}
							variableCount += 2;
							break;
						}
						else
							tempVarCount--;
					}
				}
				if(indent > 0){
					StringBuilder tabs = new StringBuilder();
					for (int i = 0; i < indent; i++)
						tabs.append("\t");
					code.append(tabs.toString() + "for i in range(0, len(" + varName + ")):\n");
				}
				else
					code.append("for i in range(0, len(" + varName + ")):\n");
				indent++;
			}

			else if (r.getReferenceString().equals("{g-np [object]={c-while}}")){
				StringBuilder varName = new StringBuilder();
				int tempVarCount = variableCount;
				for(int i = 0; i < this.nl.length(); i++){
					if((int)nl.charAt(i) == 39){
						if(tempVarCount == 0){
							for(int j = i+1; this.nl.charAt(j) != 39; j++){
								varName.append(this.nl.charAt(j));								
							}
							variableCount += 2;
							break;
						}
						else
							tempVarCount--;
					}
				}
				if(indent > 0){
					StringBuilder tabs = new StringBuilder();
					for (int i = 0; i < indent; i++)
						tabs.append("\t");
					code.append(tabs.toString() + "while(" + varName);
				}
				else
					code.append("while(" + varName);
				indent++;
			}

			else if (r.getReferenceString().equals("{g-np [object]={c-for-each}}")){
				StringBuilder varName = new StringBuilder();
				int tempVarCount = variableCount;
				for(int i = 0; i < this.nl.length(); i++){
					if((int)nl.charAt(i) == 39){
						if(tempVarCount == 0){
							for(int j = i+1; this.nl.charAt(j) != 39; j++){
								varName.append(this.nl.charAt(j));								
							}
							variableCount += 2;
							break;
						}
						else
							tempVarCount--;
					}
				}
				if(indent > 0){
					StringBuilder tabs = new StringBuilder();
					for (int i = 0; i < indent; i++)
						tabs.append("\t");
					code.append(tabs.toString() + "for x in " + varName + ":\n");
				}
				else
					code.append("for x in " + varName + ":\n");
				indent++;
			}

			else if (r.getReferenceString().equals("{g-np [object]={c-perameters}}")){
				StringBuilder perams = new StringBuilder();
				int start = 0;
				String word = new String();

				String[] tempSentences = this.nl.split("\\.");
				int totalSize = 0;
				int peramCount = peramIndex;
				int realIndex = 0;
				if(peramIndex > 0){
					for(int i = 0; i < tempSentences.length; i++){
						if(tempSentences[i].contains("perameters") || tempSentences[i].contains("pass in")){
							if(peramCount == 0){
								realIndex = i;
								break;
							}
							else
								peramCount--;
						}
						totalSize += tempSentences[i].length();
					}
				}

				if(tempSentences[realIndex].contains("perameters"))
					word = "perameters";
				else if(tempSentences[realIndex].contains("pass in"))
					word = "in";
				
				
				if(word.equals("perameters")){
					if(peramIndex == 0)
						start = tempSentences[realIndex].indexOf(word) + 11 + totalSize;
					else
						start = tempSentences[realIndex].indexOf(word) + 11 + totalSize+ tempSentences.length-1;
				}
				else{
					if(peramIndex == 0)
						start = tempSentences[realIndex].indexOf(word) + 3 + totalSize;
					else
						start = tempSentences[realIndex].indexOf(word) + 3 + totalSize + tempSentences.length-1;
				}
				
				peramIndex++;
				
				for(int i = start; i < this.nl.length(); i++){
					if((int)nl.charAt(i) == 46 || (int)nl.charAt(i) <= 31)
						break;
					else if((int)nl.charAt(i) == 39){
						variableCount++;
					}
					else
						perams.append(nl.charAt(i));
				}
				if(callFlag){
					code.append(perams.toString() + ")\n");
					callFlag = false;
				}
				else
					code.append(perams.toString() + "):\n");
			}

			else if (r.getReferenceString().equals("{g-np [object]={c-print}}")){
				StringBuilder printState = new StringBuilder();
				int start = 0;

				if(printIndex == 0){
					start = this.nl.indexOf("print");
					printIndex = start + 4;
				}
				else{
					start = this.nl.indexOf("print", printIndex);
					printIndex = start + 4;
				}


				for(int i = start+6; i < this.nl.length(); i++){
					if((int)nl.charAt(i) == 46 || (int)nl.charAt(i) <= 31)
						break;
					else
						printState.append(nl.charAt(i));
				}
				if(indent > 0){
					StringBuilder tabs = new StringBuilder();
					for (int i = 0; i < indent; i++)
						tabs.append("\t");
					code.append(tabs.toString() + "print " + String.valueOf((char)34) + printState.toString() + String.valueOf((char)34) + "\n");
				}
				else
					code.append("print " + String.valueOf((char)34) + printState.toString() + String.valueOf((char)34) + "\n");
			}

			else if (r.getReferenceString().equals("{g-np [object]={c-add}}")){
				String intName = new String();
				int tempIntCount = intCount;
				StringBuilder varName = new StringBuilder();
				int tempVarCount = variableCount;
				for(int i = 0; i < this.nl.length(); i++){
					if((int)nl.charAt(i) == 39){
						if(tempVarCount == 0){
							for(int j = i+1; this.nl.charAt(j) != 39; j++){
								varName.append(this.nl.charAt(j));								
							}
							variableCount += 2;
							break;
						}
						else
							tempVarCount--;
					}
				}
				for(int i = 0; i < this.nl.length(); i++){
					if(47 < (int)nl.charAt(i) && (int)nl.charAt(i) < 58){
						if(tempIntCount == 0){
							intName = String.valueOf(nl.charAt(i));
							intCount++;
							break;
						}
						else
							tempIntCount--;
					}
				}
				if(indent > 0){
					StringBuilder tabs = new StringBuilder();
					for (int i = 0; i < indent; i++)
						tabs.append("\t");
					code.append(tabs.toString() + varName + " += " + intName + "\n");
				}
				else
					code.append(varName + " += " + intName + "\n");
			}

			else if (r.getReferenceString().equals("{g-np [object]={c-subtract}}")){
				String intName = new String();
				int tempIntCount = intCount;
				StringBuilder varName = new StringBuilder();
				int tempVarCount = variableCount;
				for(int i = 0; i < this.nl.length(); i++){
					if((int)nl.charAt(i) == 39){
						if(tempVarCount == 0){
							for(int j = i+1; this.nl.charAt(j) != 39; j++){
								varName.append(this.nl.charAt(j));								
							}
							variableCount += 2;
							break;
						}
						else
							tempVarCount--;
					}
				}
				for(int i = 0; i < this.nl.length(); i++){
					if(47 < (int)nl.charAt(i) && (int)nl.charAt(i) < 58){
						if(tempIntCount == 0){
							intName = String.valueOf(nl.charAt(i));
							intCount++;
							break;
						}
						else
							tempIntCount--;
					}
				}
				if(indent > 0){
					StringBuilder tabs = new StringBuilder();
					for (int i = 0; i < indent; i++)
						tabs.append("\t");
					code.append(tabs.toString() + varName + " -= " + intName + "\n");
				}
				else
					code.append(varName + " -= " + intName + "\n");
			}

			else if (r.getReferenceString().equals("{g-np [object]={c-divide}}")){
				String intName = new String();
				int tempIntCount = intCount;
				StringBuilder varName = new StringBuilder();
				int tempVarCount = variableCount;
				for(int i = 0; i < this.nl.length(); i++){
					if((int)nl.charAt(i) == 39){
						if(tempVarCount == 0){
							for(int j = i+1; this.nl.charAt(j) != 39; j++){
								varName.append(this.nl.charAt(j));								
							}
							variableCount += 2;
							break;
						}
						else
							tempVarCount--;
					}
				}
				for(int i = 0; i < this.nl.length(); i++){
					if(47 < (int)nl.charAt(i) && (int)nl.charAt(i) < 58){
						if(tempIntCount == 0){
							intName = String.valueOf(nl.charAt(i));
							intCount++;
							break;
						}
						else
							tempIntCount--;
					}
				}
				if(indent > 0){
					StringBuilder tabs = new StringBuilder();
					for (int i = 0; i < indent; i++)
						tabs.append("\t");
					code.append(tabs.toString() + varName + " /= " + intName + "\n");
				}
				else
					code.append(varName + " /= " + intName + "\n");
			}

			else if (r.getReferenceString().equals("{g-np [object]={c-multiply}}")){
				String intName = new String();
				int tempIntCount = intCount;
				StringBuilder varName = new StringBuilder();
				int tempVarCount = variableCount;
				for(int i = 0; i < this.nl.length(); i++){
					if((int)nl.charAt(i) == 39){
						if(tempVarCount == 0){
							for(int j = i+1; this.nl.charAt(j) != 39; j++){
								varName.append(this.nl.charAt(j));								
							}
							variableCount += 2;
							break;
						}
						else
							tempVarCount--;
					}
				}
				for(int i = 0; i < this.nl.length(); i++){
					if(47 < (int)nl.charAt(i) && (int)nl.charAt(i) < 58){
						if(tempIntCount == 0){
							intName = String.valueOf(nl.charAt(i));
							intCount++;
							break;
						}
						else
							tempIntCount--;
					}
				}
				if(indent > 0){
					StringBuilder tabs = new StringBuilder();
					for (int i = 0; i < indent; i++)
						tabs.append("\t");
					code.append(tabs.toString() + varName + " *= " + intName + "\n");
				}
				else
					code.append(varName + " *= " + intName + "\n");
			}

			else if (r.getReferenceString().equals("{g-np [object]={c-equal}}")){
				if(!orFlag){
					String intName = new String();
					int tempIntCount = intCount;
					for(int i = 0; i < this.nl.length(); i++){
						if(47 < (int)nl.charAt(i) && (int)nl.charAt(i) < 58){
							if(tempIntCount == 0){
								intName = String.valueOf(nl.charAt(i));
								intCount++;
								break;
							}
							else
								tempIntCount--;
						}
					}
					if(ifFlag)
						code.append(" == " + intName + "):\n");
					else{
						StringBuilder varName = new StringBuilder();
						int tempVarCount = variableCount;
						for(int i = 0; i < this.nl.length(); i++){
							if((int)nl.charAt(i) == 39){
								if(tempVarCount == 0){
									for(int j = i+1; this.nl.charAt(j) != 39; j++){
										varName.append(this.nl.charAt(j));								
									}
									variableCount += 2;
									break;
								}
								else
									tempVarCount--;
							}
						}
						if(indent > 0){
							StringBuilder tabs = new StringBuilder();
							for (int i = 0; i < indent; i++)
								tabs.append("\t");
							code.append(tabs.toString() + varName + " = " + intName + "\n");
						}
						else
							code.append(varName + " = " + intName + "\n");
					}

				}
				ifFlag = false;
			}

			else if (r.getReferenceString().equals("{g-np [object]={c-greater-than}}")){
				if(thanIndex == 0){
					int findOr = this.nl.indexOf("than") + 5;
					if(this.nl.charAt(findOr) == 'o')
						orFlag = true;
					thanIndex = this.nl.indexOf("than") + 5;
				}
				else{
					int findOr = this.nl.indexOf("than", thanIndex) + 5;
					if(this.nl.charAt(findOr) == 'o')
						orFlag = true;
					thanIndex = this.nl.indexOf("than", thanIndex) + 5;
				}

				if(!orFlag){
					String varName = new String();
					int tempIntCount = intCount;
					for(int i = 0; i < this.nl.length(); i++){
						if(47 < (int)nl.charAt(i) && (int)nl.charAt(i) < 58){
							if(tempIntCount == 0){
								varName = String.valueOf(nl.charAt(i));
								intCount++;
								break;
							}
							else
								tempIntCount--;
						}
					}
					code.append(" > " + varName + "):\n");
				}
				ifFlag = false;
			}

			else if (r.getReferenceString().equals("{g-np [object]={c-greater-than-equal}}")){
				String varName = new String();
				int tempIntCount = intCount;
				for(int i = 0; i < this.nl.length(); i++){
					if(47 < (int)nl.charAt(i) && (int)nl.charAt(i) < 58){
						if(tempIntCount == 0){
							varName = String.valueOf(nl.charAt(i));
							intCount++;
							break;
						}
						else
							tempIntCount--;
					}
				}
				code.append(" >= " + varName + "):\n");
				orFlag = false;
				ifFlag = false;
			}

			else if (r.getReferenceString().equals("{g-np [object]={c-less-than}}")){
				if(thanIndex == 0){
					int findOr = this.nl.indexOf("than") + 5;
					if(this.nl.charAt(findOr) == 'o')
						orFlag = true;
					thanIndex = this.nl.indexOf("than") + 5;
				}
				else{
					int findOr = this.nl.indexOf("than", thanIndex) + 5;
					if(this.nl.charAt(findOr) == 'o')
						orFlag = true;
					thanIndex = this.nl.indexOf("than", thanIndex) + 5;
				}

				if(!orFlag){
					String varName = new String();
					int tempIntCount = intCount;
					for(int i = 0; i < this.nl.length(); i++){
						if(47 < (int)nl.charAt(i) && (int)nl.charAt(i) < 58){
							if(tempIntCount == 0){
								varName = String.valueOf(nl.charAt(i));
								intCount++;
								break;
							}
							else
								tempIntCount--;
						}
					}
					code.append(" < " + varName + "):\n");
				}
				ifFlag = false;
			}

			else if (r.getReferenceString().equals("{g-np [object]={c-less-than-equal}}")){
				String varName = new String();
				int tempIntCount = intCount;
				for(int i = 0; i < this.nl.length(); i++){
					if(47 < (int)nl.charAt(i) && (int)nl.charAt(i) < 58){
						if(tempIntCount == 0){
							varName = String.valueOf(nl.charAt(i));
							intCount++;
							break;
						}
						else
							tempIntCount--;
					}
				}
				code.append(" <= " + varName + "):\n");
				orFlag = false;
				ifFlag = false;
			}

			else if (r.getReferenceString().equals("{g-np [object]={c-modulo}}")){
				String intName = new String();
				int tempIntCount = intCount;
				StringBuilder varName = new StringBuilder();
				int tempVarCount = variableCount;
				for(int i = 0; i < this.nl.length(); i++){
					if((int)nl.charAt(i) == 39){
						if(tempVarCount == 0){
							for(int j = i+1; this.nl.charAt(j) != 39; j++){
								varName.append(this.nl.charAt(j));								
							}
							variableCount += 2;
							break;
						}
						else
							tempVarCount--;
					}
				}
				for(int i = 0; i < this.nl.length(); i++){
					if(47 < (int)nl.charAt(i) && (int)nl.charAt(i) < 58){
						if(tempIntCount == 0){
							intName = String.valueOf(nl.charAt(i));
							intCount++;
							break;
						}
						else
							tempIntCount--;
					}
				}
				if(indent > 0){
					StringBuilder tabs = new StringBuilder();
					for (int i = 0; i < indent; i++)
						tabs.append("\t");
					code.append(tabs.toString() + varName + " %= " + intName + "\n");
				}
				else
					code.append(varName + " %= " + intName + "\n");
			}

			// System.out.println("r.getItem.toDescriptiveString: " + r.getItem().toDescriptiveString());
			// System.out.println("r.getItem.getTargets: " + r.getItem().getTargets());
			// System.out.println("r.getText: " + r.getText());
			// System.out.println("r.toString: " + r.toString());
			// System.out.print("r.getInfo: ");
			// System.out.println(r.getInfo());

		}
		return code.toString();
	}

}
