package com.thargoid;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import de.saar.coli.salsa.reiter.framenet.*;
import de.saar.coli.salsa.reiter.framenet.FrameNet;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.ManchesterSyntaxDocumentFormat;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.io.StreamDocumentTarget;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.io.StringDocumentTarget;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasoner;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.search.Filters;
import org.semanticweb.owlapi.util.*;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.semanticweb.owlapi.vocab.OWLFacet;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import com.google.common.base.Optional;

import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

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
    // wordnet (w)
    // lda (;)
    // text markup, e.g. ontological (o)
    // inference (i)
    // e.g. pafgwloi
//4=delete files from each folder (0 or 1)
//5

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
    static String inFolder;
    static FrameNet fn;
    static String[] row;
    static String[] prevRow;
    static String[] rowout;
    static String lastRow = "1";
    static List<String[]> FrameColumns;
    static int ColIDCount;
    //static String FrameNetFolder = "C:\\Users\\co17\\LocalStuff\\MyStuff\\Personal\\MPhil\\Framenet\\fndata-1.5\\fndata-1.5";
    static String FrameNetFolder = "D:\\LaRheto\\fndata-1.5\\fndata-1.5";
   // static model model;


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
                        Parse("1", "1");
                        WorkFolder = WorkFolder.replace(inFolder, "2_parsed");
                    }
                    catch(Exception ex)
                    {
                        log("Error:-" + ex.toString() + ", " + ex.getMessage() + ", " + ex.getLocalizedMessage());
                    }
                    log("Ending Parse");

                    break;

                case 'a':

                    //post-parse processing
                    log("Starting post-parse processing");

                    log("Ending post-parse processing");

                    break;

                case 'f':
                    log("Starting Framenet");
                    try {
                        //framenet
                        Framer ("1");
                        WorkFolder = WorkFolder.replace(inFolder, "3_framed");
                    }
                    catch(Exception ex)
                    {
                        log("Error:-" + ex.toString() + ", " + ex.getMessage() + ", " + ex.getLocalizedMessage());
                    }
                    log("Ending Framenet");
                    break;

                case 'w':
                    log("Starting WordNet");
                    try {
                        //wordnet
                        //Framer ("1");
                        WorkFolder = WorkFolder.replace(inFolder, "4_wordnet");
                    }
                    catch(Exception ex)
                    {
                        log("Error:-" + ex.toString() + ", " + ex.getMessage() + ", " + ex.getLocalizedMessage());
                    }
                    log("Ending WordNet");
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

                        setupParseLookups();
                        ontoParse("1", "1");
                        WorkFolder = WorkFolder.replace(inFolder, "5_ontoparsed");
                        //set up model here?

                    log("Ending ontoloy population");

                    break;


                case 'i':

                    //inference
                    log("Starting inference");

                    log("Ending inference");

                    break;

            }
        }

        teardown();
        log("Ending Tamesis");

    }

    static private void setup()
    {
        try {
            quando = getNow();
            LogFileName = WorkFolder + File.separator + "log-" + quando + ".txt";
            inFolder = "1_in";
            FrameColumns = new ArrayList<String[]>();
            ColIDCount = 1;
            addColumnMetaData(String.valueOf(ColIDCount), "Filename");
            log("Finished setup");
        }
        catch (Exception ex)
        {
            log("Error:-" + ex.toString() + ", " + ex.getMessage() + ", " + ex.getLocalizedMessage());
        }
    }

    static private void teardown()
    {
        try {

            for (String[] fc:FrameColumns)
            {
                   log("Columns exported: " + fc[0] + ": " + fc[1]);
            }

            log("Finished teardown");
        }
        catch (Exception ex)
        {
            log("Error:-" + ex.toString() + ", " + ex.getMessage() + ", " + ex.getLocalizedMessage());
        }
    }





    static private boolean Framer(String pOutputType)
    {
        boolean output = false;
        String FramerFolder = WorkFolder + File.separator + inFolder;
        String OutputType = pOutputType;

        String outputFolder = FramerFolder.replace(inFolder, "3_framed");
        inFolder = "3_framed";
        Path p = Paths.get(outputFolder);

        try {
            if (Files.notExists(p)) {
                Files.createDirectory(p);
                log("Created directory " + p);
            }

            if (deleteFiles) {
                File f = new File(outputFolder);
                File[] matchingFiles = f.listFiles();

                if (matchingFiles != null) {
                    int c = 0;
                    for (File tf : matchingFiles) {
                        tf.delete();
                        c++;
                    }
                    log("Deleted " + c + " files");
                }
            }

            File f = new File(FramerFolder);
            File[] matchingFiles = f.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".csv");
                }
            });
            log("Scanned " + WorkFolder + " for csv files and found " + matchingFiles.length + " files");

            int filecount = 0;

            int luCol = 7;
            int posCol = 5;

            fn = new FrameNet();
            File fnHome = new File(FrameNetFolder);
            DatabaseReader reader = new FNDatabaseReader15(fnHome,true);
            fn.readData(reader);
            log("Set up framenet objects with folder " + FrameNetFolder);

            List<String[]> LUs = new ArrayList<String[]>();
            for(Frame fr : fn.getFrames())
            {
                for(LexicalUnit luv : fr.getLexicalUnits())
                {
                    String[] tmpLU = new String[3];
                    tmpLU[0]=luv.getLexemeString();
                    tmpLU[1] = luv.getPartOfSpeechAbbreviation();
                    tmpLU[2]=fr.getName();
                    LUs.add(tmpLU);
                }
            }
            log("Set up local LU List");

            addColumnMetaData(String.valueOf(ColIDCount), "FrameNames");
            addColumnMetaData(String.valueOf(ColIDCount), "FrameElements");
            addColumnMetaData(String.valueOf(ColIDCount), "LexicalUnits");
            addColumnMetaData(String.valueOf(ColIDCount), "IsInheritedBy");
            addColumnMetaData(String.valueOf(ColIDCount), "Perspectivized");
            addColumnMetaData(String.valueOf(ColIDCount), "Uses");
            addColumnMetaData(String.valueOf(ColIDCount), "UserBy");
            addColumnMetaData(String.valueOf(ColIDCount), "hasSubFrame");
            addColumnMetaData(String.valueOf(ColIDCount), "Inchoative");
            addColumnMetaData(String.valueOf(ColIDCount), "InchoativeStative");
            addColumnMetaData(String.valueOf(ColIDCount), "Causative");
            addColumnMetaData(String.valueOf(ColIDCount), "CausativeStative");
            addColumnMetaData(String.valueOf(ColIDCount), "AllInheritedFrames");
            addColumnMetaData(String.valueOf(ColIDCount), "AllInheritedFrames");
            addColumnMetaData(String.valueOf(ColIDCount), "Earlier");
            addColumnMetaData(String.valueOf(ColIDCount), "InheritsFrom");
            addColumnMetaData(String.valueOf(ColIDCount), "Later");
            addColumnMetaData(String.valueOf(ColIDCount), "Neutral");
            addColumnMetaData(String.valueOf(ColIDCount), "Referred");
            addColumnMetaData(String.valueOf(ColIDCount), "Referring");
            addColumnMetaData(String.valueOf(ColIDCount), "subFrameOf");

            int rowcount = 0;
            for(File tf : matchingFiles)
            {
                filecount++;
                CSVReader csvinput = new CSVReader(new FileReader(tf.getAbsolutePath()));
                log("Reading csv file ... " + tf.getAbsolutePath());
                List csvinputdata = csvinput.readAll();
                csvinput.close();

                String newFilename = outputFolder + File.separator + "ms-" + tf.getName();
                CSVWriter csvout = new CSVWriter(new FileWriter(newFilename));
                log("New file opened ... " + newFilename);

                List<String> framesSoFar = new ArrayList<String>();
                List<String> SuppliedFramesO  = new ArrayList<String>();

                for(Object ob : csvinputdata)
                {
                    List<String> frames = new ArrayList<String>();
                    row=(String[]) ob;
                    String tLU = row[luCol];
                    String tPOS = row[posCol];

                    boolean init = false;

                    for(String[] strlus : LUs)
                    {
                        if(strlus[0].trim().equals(tLU) && strlus[1].equals(tPOS))
                        {
                            log("Checking " + strlus[0] + ", " + strlus[1] + ", " + strlus[2]);
                            frames.add(strlus[2]);
                            log("Added frame " + strlus[2]);
                            framesSoFar.add(strlus[2]);
                            init=true;
                        }
                    }

                    int colCount = 30; //row.length + parsedColumns
                    int parsedColumns = 22;

                    if(init)
                    {
                        log("Attempting csv output - LU:" + tLU);
                        csvout.writeNext(writeFrameData(OutputType, colCount, frames, tLU, "False"));
                        rowcount++;
                        log("Written a row: " + rowcount);
                    }
                    else // no frames
                    {
                        log("No frames!");
                        rowout = new String[colCount];
                        int a = 0;

                        for(String s : row)
                        {
                            rowout[a] = s;
                            a++;
                        }

                        for(int n = 0 ; n<parsedColumns ; n++)
                        {
                            rowout[a+n] = "";
                        }

                        csvout.writeNext(rowout);
                        rowcount++;
                    }

                    lastRow = row[0];
                    prevRow = row;

                }

                csvout.close();
            }

        }
        catch(Exception ex)
        {
            log("Error:-" + ex.toString() + ", " + ex.getMessage() + ", " + ex.getLocalizedMessage());
        }

        return output;
    }


    static public List<String> getFrameElements(String frame)
    {
        List<String> outputFrameElements = new ArrayList<String>();
        try
        {
            Frame f = fn.getFrame(frame);
            for(FrameElement fe : f.getFrameElements().values())
            {
                outputFrameElements.add(fe.getName());
            }
        }
        catch (Exception ex)
        {
            log("Error:-" + ex.toString() + ", " + ex.getMessage() + ", " + ex.getLocalizedMessage());
        }
        return outputFrameElements;
    }

    static public List<String> getFrameLUs(String frame)
    {
        List<String> outputLUs = new ArrayList<String>();
        try
        {
            Frame f = fn.getFrame(frame);
            for(LexicalUnit lu : f.getLexicalUnits())
            {
                outputLUs.add(lu.getName());
            }
        }
        catch (Exception ex)
        {
            log("Error:-" + ex.toString() + ", " + ex.getMessage() + ", " + ex.getLocalizedMessage());
        }
        return outputLUs;
    }


    static public String[] writeFrameData(String type, int colCount, List<String> frames, String tLU, String extraLine)
    {
        //colCount = row.length + parsedColumns
        try
        {
            if(type=="1")  //basic framenet spacers
            {
                rowout = new String[colCount];

                for(int h=0 ; h<colCount ; h++)
                {
                    rowout[h]="";
                }

                for(String frnm : frames)
                {
                    Frame fr = fn.getFrame(frnm);
                    int a = 0;

                    if(extraLine.equals("True"))
                    {
                        for(String s : prevRow)
                        {
                            rowout[a] = s;
                            a++;
                        }
                        rowout[prevRow.length-1]="";
                        rowout[prevRow.length-2]="";
                        rowout[prevRow.length-3]="";
                        rowout[prevRow.length-4]="";
                        rowout[prevRow.length-5]="";
                        rowout[prevRow.length-6]="0";
                    }
                    else
                    {
                        for(String s : row)
                        {
                            rowout[a] = s;
                            a++;
                        }
                    }

                    //add Frame name
                    rowout[a]=rowout[a] + " " + fr.getName();
                    a++;


                    //add Frame Elements
                    String FEs = "";
                    for(String FE : getFrameElements(fr.getName()))
                    {
                        FEs = FEs + FE + " ";
                    }
                    FEs.trim();
                    rowout[a]=rowout[a] + " " + FEs;
                    a++;


                    //add Frame LUs
                    String fLUs = "";
                    for(String fLU : getFrameLUs(fr.getName()))
                    {
                        fLUs = fLUs + fLU + " ";
                    }
                    fLUs.trim();
                    rowout[a]=rowout[a] + " " + fLUs;
                    a++;

                    String ibFs = "";
                    for(Frame IdF : fr.isInheritedBy())
                    {
                        ibFs = ibFs + IdF + " ";
                    }
                    ibFs.trim();
                    rowout[a]=rowout[a] + " " + ibFs;
                    a++;

                    String pFs = "";
                    for(Frame IdF : fr.perspectivized())
                    {
                        pFs = pFs + IdF + " ";
                    }
                    pFs.trim();
                    rowout[a]=rowout[a] + " " + pFs;
                    a++;



                    String uFs = "";
                    for(Frame IdF : fr.uses())
                    {
                        uFs = uFs + IdF + " ";
                    }
                    uFs.trim();
                    rowout[a]=rowout[a] + " " + uFs;
                    a++;

                    String ubFs = "";
                    for(Frame IdF : fr.usedBy())
                    {
                        ubFs = ubFs + IdF + " ";
                    }
                    ubFs.trim();
                    rowout[a]=rowout[a] + " " + ubFs;
                    a++;

                    String hsfFs = "";
                    for(Frame IdF : fr.hasSubframe())
                    {
                        hsfFs = hsfFs + IdF + " ";
                    }
                    hsfFs.trim();
                    rowout[a]=rowout[a] + " " + hsfFs;
                    a++;


                    String incFs = "";
                    for(Frame IdF : fr.inchoative())
                    {
                        incFs = incFs + IdF + " ";
                    }
                    incFs.trim();
                    rowout[a]=rowout[a] + " " + incFs;
                    a++;

                    String incsFs = "";
                    for(Frame IdF : fr.inchoativeStative())
                    {
                        incsFs = incsFs + IdF + " ";
                    }
                    incsFs.trim();
                    rowout[a]=rowout[a] + " " + incsFs;
                    a++;

                    String cauFs = "";
                    for(Frame IdF : fr.causative())
                    {
                        cauFs = cauFs + IdF + " ";
                    }
                    cauFs.trim();
                    rowout[a]=rowout[a] + " " + cauFs;
                    a++;


                    String caustFs = "";
                    for(Frame IdF : fr.causativeStative())
                    {
                        caustFs = caustFs + IdF + " ";
                    }
                    caustFs.trim();
                    rowout[a]=rowout[a] + " " + caustFs;
                    a++;


                    String aifFs = "";
                    for(Frame IdF : fr.allInheritedFrames())
                    {
                        aifFs = aifFs + IdF + " ";
                    }
                    aifFs.trim();
                    rowout[a]=rowout[a] + " " + aifFs;
                    a++;


                    String aigfFs = "";
                    for(Frame IdF : fr.allInheritingFrames())
                    {
                        aigfFs = aigfFs + IdF + " ";
                    }
                    aigfFs.trim();
                    rowout[a]=rowout[a] + " " + aigfFs;
                    a++;


                    String earFs = "";
                    for(Frame IdF : fr.earlier())
                    {
                        earFs = earFs + IdF + " ";
                    }
                    earFs.trim();
                    rowout[a]=rowout[a] + " " + earFs;
                    a++;


                    String ifFs = "";
                    for(Frame IdF : fr.inheritsFrom())
                    {
                        ifFs = ifFs + IdF + " ";
                    }
                    ifFs.trim();
                    rowout[a]=rowout[a] + " " + ifFs;
                    a++;

                    String lFs = "";
                    for(Frame IdF : fr.later())
                    {
                        lFs = lFs + IdF + " ";
                    }
                    lFs.trim();
                    rowout[a]=rowout[a] + " " + lFs;
                    a++;


                    String nFs = "";
                    for(Frame IdF : fr.neutral())
                    {
                        nFs = nFs + IdF + " ";
                    }
                    nFs.trim();
                    rowout[a]=rowout[a] + " " + nFs;
                    a++;


                    String refFs = "";
                    for(Frame IdF : fr.referred())
                    {
                        refFs = refFs + IdF + " ";
                    }
                    refFs.trim();
                    rowout[a]=rowout[a] + " " + refFs;
                    a++;


                    String refrFs = "";
                    for(Frame IdF : fr.referring())
                    {
                        refrFs = refrFs + IdF + " ";
                    }
                    refrFs.trim();
                    rowout[a]=rowout[a] + " " + refrFs;
                    a++;


                    String sfoFs = "";
                    for(Frame IdF : fr.subframeOf())
                    {
                        sfoFs = sfoFs + IdF + " ";
                    }
                    sfoFs.trim();
                    rowout[a]=rowout[a] + " " + sfoFs;
                    a++;

                    if(extraLine.equals("True"))
                    {
                        rowout[a]="";
                    }
                    else
                    {
                        rowout[a]=tLU;
                    }
                }
            }
            if(type=="2")  //some more advanced spacers
            {
            }
        }
        catch (Exception ex)
        {
            log("Error:-" + ex.toString() + ", " + ex.getMessage() + ", " + ex.getLocalizedMessage());
        }

        return rowout;
    }


    static private boolean Parse(String pInputType, String pOutputType)
    {
        boolean output = false;
        String ParseFolder = WorkFolder + File.separator + inFolder;
        String InputType = pInputType;
        //1 - plain text file
        //2 - csv line per sentence with associated extra detail
        //3 - csv two first columns are sentences to be parsed
        String OutputType = pOutputType;

        String outputFolder = ParseFolder.replace(inFolder, "2_parsed");
        inFolder = "2_parsed";
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
            log("Error:-" + ex.toString() + ", " + ex.getMessage() + ", " + ex.getLocalizedMessage());
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

                addColumnMetaData(String.valueOf(ColIDCount), "SentenceNumber");
                addColumnMetaData(String.valueOf(ColIDCount), "WordNumber");
                addColumnMetaData(String.valueOf(ColIDCount), "OriginalWord");
                addColumnMetaData(String.valueOf(ColIDCount), "POSCode");
                addColumnMetaData(String.valueOf(ColIDCount), "POSType");
                addColumnMetaData(String.valueOf(ColIDCount), "NamedEntity");
                addColumnMetaData(String.valueOf(ColIDCount), "Lemma");

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
            log("Error:-" + ex.toString() + ", " + ex.getMessage() + ", " + ex.getLocalizedMessage());
        }

        return Louts;
    }



    static private boolean ontoParse(String pInputType, String pOutputType)
    {
        boolean output = false;
        String ParseFolder = WorkFolder + File.separator + inFolder;
        String InputType = pInputType;
        //1 - plain text file
        //2 - csv line per sentence with associated extra detail
        //3 - csv two first columns are sentences to be parsed
        String OutputType = pOutputType;

        String outputFolder = ParseFolder.replace(inFolder, "5_ontoparsed");
        inFolder = "5_ontoparsed";
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
            log("Error:-" + ex.toString() + ", " + ex.getMessage() + ", " + ex.getLocalizedMessage());
        }

        return output;
    }

    static List<String[]> ontoParseText(int type, String corpus)
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

                addColumnMetaData(String.valueOf(ColIDCount), "SentenceNumber");
                addColumnMetaData(String.valueOf(ColIDCount), "WordNumber");
                addColumnMetaData(String.valueOf(ColIDCount), "OriginalWord");
                addColumnMetaData(String.valueOf(ColIDCount), "POSCode");
                addColumnMetaData(String.valueOf(ColIDCount), "POSType");
                addColumnMetaData(String.valueOf(ColIDCount), "NamedEntity");
                addColumnMetaData(String.valueOf(ColIDCount), "Lemma");

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
            log("Error:-" + ex.toString() + ", " + ex.getMessage() + ", " + ex.getLocalizedMessage());
        }

        return Louts;
    }





    static void addColumnMetaData(String COlID, String Description)
    {
        String[] Cols = {COlID, Description};
        FrameColumns.add(Cols);
        ColIDCount++;
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
            System.out.println("Error:-" + ex.toString() + ", " + ex.getMessage() + ", " + ex.getLocalizedMessage());
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
