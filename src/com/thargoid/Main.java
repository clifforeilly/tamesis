package com.thargoid;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

//args
//0=process type
    //1=lassoing rhetoric
    //2=iceni
//1=work folder
//2=log to file (0 or 1)
//3=processes to run:
    // parse (p)
    // post-parse processing (a)
    // framenet (f)
    // post-frame processing (g)
    // lda (;)
    // text markup, e.g. ontological (o)
    // inference (i)
    // e.g. pafgloi
//4=delete files from each folder (0 or 1)


public class Main {

    static private Boolean LogToFile = true;
    static private String LogFileName;
    static private String WorkFolder;
    static private String quando;
    static private int AbstractArgCount;
    static private String[][] arguments;
    static private String[] args2;
    static private boolean deleteFiles;
    static List<String> nounNodeNames;
    static List<String> verbNodeNames;
    static List<String> adjectiveNodeNames;
    static List<String> adverbNodeNames;
    static List<String> determinerNodeNames;
    static List<String> prepositionNodeNames;
    static List<String> conjunctionNodeNames;
    static List<String> interjectionNodeNames;
    static List<String> pronounNodeNames;
    static StanfordCoreNLP pipeline;
    static int NumParsedColumns = 7;

    public static void main(String[] args) {

        args2 = args;
        AbstractArgCount = 5;
        arguments = new String[AbstractArgCount][3];
        arguments[0][0] = "ProcessType";
        arguments[0][1] = "0";
        arguments[0][2] = "1";
        arguments[1][0] = "WorkFolder";
        arguments[1][1] = "1";
        arguments[1][2] = "1";
        arguments[2][0] = "LogToFile";
        arguments[2][1] = "2";
        arguments[2][2] = "1";
        arguments[3][0] = "Processes";
        arguments[3][1] = "3";
        arguments[3][2] = "";
        arguments[4][0] = "DeleteFiles";
        arguments[4][1] = "4";
        arguments[4][2] = "1";

        WorkFolder = getArg("WorkFolder");
        if(getArg("LogToFile").equals("0"))
        {
            LogToFile = false;
        }
        else
        {
            LogToFile = true;
        }

        setup();
        for(int a = 0 ; a<args.length ; a++) {
            if(args[a] != null && !args[a].isEmpty())
            {
                log("args[" + a + "]=" + args[a]);
            }
        }
        log("Starting Tamesis");

        deleteFiles = false;
        if(getArg("DeleteFiles").equals("1"))
        {
            deleteFiles = true;
        }

        String Processes = getArg("Processes");

        for(int p = 0; p<Processes.length(); p++)
        {
            char pr = Processes.charAt(p);

            switch (pr)
            {
                case 'p':

                    //parse
                    log("Starting Parse");

                    try {
                        //Input Type:
                        //1 - plain text file
                        //2 - csv line per sentence with associated extra detail
                        //3 - csv two first columns are sentences to be parsed
                        setupParseLookups();
                        Parse(WorkFolder + File.separator + "1_in", "1", "1");
                    }
                    catch(Exception ex)
                    {
                        log(ex.getMessage());
                    }
                    log("Ending Parse");

                    break;

                case 'a':

                    //post-parse processing
                    log("Starting post-parse processing");

                    log("Ending post-parse processing");

                    break;

                case 'f':

                    //framenet
                    log("Starting Framenet");

                    log("Ending Framenet");

                    break;

                case 'g':

                    //post-framenet processing
                    log("Starting post-framenet processing");

                    log("Ending post-framenet processing");

                    break;

                case 'l':

                    //lda
                    log("Starting lda");

                    log("Ending lda");

                    break;

                case 'o':

                    //ontology population
                    log("Starting ontoloy population");

                    log("Ending ontoloy population");

                    break;


                case 'i':

                    //inference
                    log("Starting inference");

                    log("Ending inference");

                    break;

            }

        }


        log("Ending Tamesis");

    }

    static private boolean Parse(String pParseFolder, String pInputType, String pOutputType)
    {
        boolean output = false;
        String ParseFolder = pParseFolder;
        String InputType = pInputType;
        //1 - plain text file
        //2 - csv line per sentence with associated extra detail
        //3 - csv two first columns are sentences to be parsed
        String OutputType = pOutputType;


        String inFolder = "1_in";
        String outputFolder = ParseFolder.replace(inFolder, "2_parsed");
        Path p = Paths.get(outputFolder);

        try {
            if(Files.notExists(p))
            {
                Files.createDirectory(p);
                log("Created directory " + p);
            }

            if(deleteFiles)
            {
                File f = new File(outputFolder);
                File[] matchingFiles = f.listFiles();

                if(matchingFiles!=null)
                {
                    int c = 0;
                    for(File tf : matchingFiles)
                    {
                        tf.delete();
                        c++;
                    }
                    log("Deleted " + c + " files");
                }
            }

            File f = new File(ParseFolder);
            File[] matchingFiles = f.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".txt");
                }
            });

            int filecount=0;
            List<String[]> Lins = new ArrayList<String[]>();

            Properties props = new Properties();
            props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
            pipeline = new StanfordCoreNLP(props);

            int rowcount = 0;
            String InText = "";

            for(File tf : matchingFiles) {
                log("Reading input file " + tf.getAbsolutePath());
                BufferedReader br = new BufferedReader(new FileReader(tf.getAbsoluteFile()));
                try {
                    StringBuilder sb = new StringBuilder();
                    String line = br.readLine();

                    while (line != null) {
                        sb.append(line);
                        sb.append(System.lineSeparator());
                        line = br.readLine();
                    }
                    String everything = sb.toString();
                    System.out.print(everything);
                    InText = everything;

                } finally {
                    br.close();
                }

                Lins = parseText(2, InText);
                String[] rowout;
                String newFilename = outputFolder + File.separator + "parsed-" + tf.getName().replace("txt", "csv");
                CSVWriter csvout = new CSVWriter(new FileWriter(newFilename));

                for(String[] t : Lins)
                {
                    if(!t[NumParsedColumns-1].equals(".") & !t[NumParsedColumns-1].equals(",") & !t[NumParsedColumns-1].equals("!") & !t[NumParsedColumns-1].equals("?"))
                    {
                        if(t[6].equals("tendency"))
                        {
                            int y = 0;
                        }

                        rowout = new String[NumParsedColumns + 1];
                        int a = 0;

                        rowout[a]=tf.getName();
                        a++;

                        for(String s2 : t)
                        {
                            rowout[a]=s2;
                            a++;
                        }
                        csvout.writeNext(rowout);
                        rowcount++;
                        log("Written a row: " + rowcount);
                    }
                }
                log("Finished writing to file " + newFilename);
                csvout.close();

            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }

        return output;
    }

    static List<String[]> parseText(int type, String corpus)
    {
        List<String[]> Louts = new ArrayList<String[]>();
        try
        {
            String[] outs = null;
            Annotation doc = new Annotation(corpus);
            pipeline.annotate(doc);
            List<CoreMap> sentences = doc.get(CoreAnnotations.SentencesAnnotation.class);

            int s=0;
            for(CoreMap sentence : sentences)
            {
                s++;

                int w=0;
                for(CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class))
                {
                    outs = new String[NumParsedColumns];
                    w++;
                    String word = token.get(CoreAnnotations.TextAnnotation.class);
                    String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                    String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                    String lem = token.get(CoreAnnotations.LemmaAnnotation.class);
                    String postype = PartOfSpeechType(pos);

                    outs[0]=String.valueOf(s);
                    outs[1]=String.valueOf(w);
                    outs[2]=word;
                    outs[3]=pos;
                    outs[4]=postype;
                    outs[5]=ne;
                    outs[6]=lem;

                    Louts.add(outs);
                }
                log("Parsed sentence ... " + sentence.toString() + "");
            }
        }
        catch (Exception ex)
        {
            System.out.println("Error:-" + ex.toString() + ", " + ex.getMessage() + ", " + ex.getLocalizedMessage());
            ex.printStackTrace();
        }

        return Louts;
    }


    static private void setup()
    {
        try {
            quando = getNow();
            LogFileName = WorkFolder + File.separator + "log-" + quando + ".txt";
            log("Finished setup");
        }
        catch (Exception ex)
        {
            log(ex.getMessage());
        }
    }

    static String getNow()
    {
        return new SimpleDateFormat("yyyyMMddHHmmsss").format(new Date());
    }

    static private void log(String text)
    {
        try
        {
            if(LogToFile)
            {
                try(Writer writer = new BufferedWriter
                        (new OutputStreamWriter
                                (new FileOutputStream(LogFileName, true), "utf-8")
                        )
                    )
                {
                    writer.write(getNow() + ":" + text + System.lineSeparator());
                }   
            }
            System.out.println(getNow() + ":" + text);
           
        }
        catch(Exception ex)
        {
            System.out.println(ex.getMessage());
        }
    }
    
    static public String getArg(String arg)
    {
        String output = "No value set";
        
        for(int i = 0; i<AbstractArgCount; i++)
        {
            if(arguments[i][0].equals(arg))
            {
                if(args2.length>Integer.parseInt(arguments[i][1]))
                {
                    output = args2[Integer.parseInt(arguments[i][1])];
                }
                else
                {
                    output = arguments[i][2];
                }
            }
        }
        
        return output;
    }

    static String PartOfSpeechType(String pos)
    {
        String type = "";
        if(nounNodeNames.contains(pos))
        {
            type="Noun";
        }
        if(nounNodeNames.contains(pos))
        {
            type="Noun";
        }
        if(verbNodeNames.contains(pos))
        {
            type="Verb";
        }
        if(adjectiveNodeNames.contains(pos))
        {
            type="Adjective";
        }
        if(adverbNodeNames.contains(pos))
        {
            type="Adverb";
        }
        if(conjunctionNodeNames.contains(pos))
        {
            type="Conjunction";
        }
        if(determinerNodeNames.contains(pos))
        {
            type="Determiner";
        }
        if(prepositionNodeNames.contains(pos))
        {
            type="Preposition";
        }
        if(interjectionNodeNames.contains(pos))
        {
            type="Interjection";
        }

        return type;
    }

    static void setupParseLookups()
    {
        nounNodeNames = new ArrayList<String>();
        nounNodeNames.add( "NP");
        nounNodeNames.add( "NP$");
        nounNodeNames.add( "NPS");
        nounNodeNames.add( "NN");
        nounNodeNames.add( "NN$");
        nounNodeNames.add( "NNS");
        nounNodeNames.add( "NNS$");
        nounNodeNames.add( "NNP");
        nounNodeNames.add( "NNPS");

        verbNodeNames = new ArrayList<String>();
        verbNodeNames.add( "VB");
        verbNodeNames.add( "VBD");
        verbNodeNames.add( "VBG");
        verbNodeNames.add( "VBN");
        verbNodeNames.add( "VBP");
        verbNodeNames.add( "VBZ");
        verbNodeNames.add( "MD" );

        adjectiveNodeNames = new ArrayList<String>();
        adjectiveNodeNames.add( "JJ");
        adjectiveNodeNames.add( "JJR");
        adjectiveNodeNames.add( "JJS");

        adverbNodeNames = new ArrayList<String>();
        adverbNodeNames.add( "RB");
        adverbNodeNames.add( "RBR");
        adverbNodeNames.add( "RBS");

        determinerNodeNames = new ArrayList<String>();
        determinerNodeNames.add( "DT");

        prepositionNodeNames = new ArrayList<String>();
        prepositionNodeNames.add( "IN");

        conjunctionNodeNames = new ArrayList<String>();
        conjunctionNodeNames.add( "CC");

        interjectionNodeNames = new ArrayList<String>();
        interjectionNodeNames.add( "UH");

        pronounNodeNames = new ArrayList<String>();
        pronounNodeNames.add( "PRP");
        pronounNodeNames.add( "PRP$");
    }

}
