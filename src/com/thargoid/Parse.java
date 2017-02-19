package com.thargoid;

//import edu.stanford.nlp.ling.CoreAnnotations;
//import edu.stanford.nlp.ling.CoreLabel;
//import edu.stanford.nlp.pipeline.Annotation;
//import edu.stanford.nlp.pipeline.StanfordCoreNLP;
//import edu.stanford.nlp.util.CoreMap;
//import com.opencsv.CSVReader;
//import com.opencsv.CSVWriter;

public class Parse {
    
    private String ParseFolder;
    private String InputType;
    private String OutputType;
    
    public Parse(String pParseFolder, String pInputType, String pOutputType)
    {
        ParseFolder = pParseFolder;
        InputType = pInputType;
        OutputType = pOutputType;
    }
    
    public Boolean Execute()
    {
        Boolean output = false;
        
        //for each file in the \in folder run the parser
        //and save the files to the \parsed folder  
        
        ParseFolder = "";
        InputType = "";
        OutputType = "";
        
        return output;
    }
    
    
    
}
