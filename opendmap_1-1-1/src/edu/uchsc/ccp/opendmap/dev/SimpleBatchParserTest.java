/*
 * Created on Apr 3, 2005
 *
 */
package edu.uchsc.ccp.opendmap.dev;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;

import edu.uchsc.ccp.opendmap.Parser;
import edu.uchsc.ccp.opendmap.Reference;
import edu.uchsc.ccp.opendmap.pattern.ParseException;

import edu.stanford.smi.protege.model.*;


/**
 * @author Will Fitzgerald
 */
public class SimpleBatchParserTest 
{
    public static void main(String[] args) throws ParseException
    {
        try
        {
            String projectFileName = args[0];
            String rulesFileName = args[1];
            String sentencesFileName = args[2];
            
            Parser parser = new Parser(Level.OFF);
                    
            Collection errors = new ArrayList();
            Project project = new Project(projectFileName, errors);
            if (errors.size() == 0) 
            {
                KnowledgeBase kb = project.getKnowledgeBase();
                String rulesString = fileToString(rulesFileName);
                parser.addPatternsFromString(rulesString, kb);
                    
                String[] sentences = readFileLines(sentencesFileName);
                
                for(int i=0; i<sentences.length;i++)
                {
                    System.out.println("\n\n\n\nsentence:= "+sentences[i]);
                    
                    parser.parse(sentences[i]);

                    // Print out all the references recognized 
                    System.out.println("\nReferences from parser:");
                    for (Reference reference: parser.getReferences()) 
                    {
                        System.out.println("  Parser saw: " + reference);
                    }
                    parser.clear(false);
                }
            }
        }
        catch(Exception ioe)
        {
            ioe.printStackTrace();
        }
    }
    
    public static String fileToString(String infile) throws IOException
    {
        InputStream inputStream = new FileInputStream(infile);
        String returnValue = fileToString(inputStream);
        inputStream.close();
        return returnValue;
    }
    
    public static String fileToString(InputStream inputStream) throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuffer contents = new StringBuffer();
        
        String line = new String();
        while ((line = reader.readLine()) != null) 
        {
            contents.append(line+"\n");
        } 
        return contents.toString();
    }
    
    
    public static String[] readFileLines(String infile) throws IOException
    {
        return readFileLines(new FileInputStream(infile));
    }
    
    /**
     * @param comment - if the line begins with the comment string, then the line will
     *                  not be included in the returned array.
     */
    public static String[] readFileLines(String infile, String comment) throws IOException
    {
        return readFileLines(new FileInputStream(infile), comment);
    }

    public static String[] readFileLines(InputStream inputStream) throws IOException
    {
        return readFileLines(inputStream, null);
    }
    
    /**
     * @param comment - if the line begins with the comment string, then the line will
     *                  not be included in the returned array.
     */
    public static String[] readFileLines(InputStream inputStream, String comment) throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String line = new String();
        ArrayList<String> lines = new ArrayList<String>();
            
        while ((line = reader.readLine()) != null) 
        {
            if(comment == null || !line.startsWith(comment))
                lines.add(line);
        } 
        return (String[])(lines.toArray(new String[lines.size()]));
    }
    


}
