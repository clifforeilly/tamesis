package com.thargoid;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
//import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import de.saar.coli.salsa.reiter.framenet.*;
import de.saar.coli.salsa.reiter.framenet.FrameNet;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.jetbrains.annotations.NotNull;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.*;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.swrlapi.core.SWRLAPIRule;
import org.swrlapi.core.SWRLRuleEngine;
import org.swrlapi.factory.SWRLAPIFactory;
//import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
//import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import static com.thargoid.Main.getNow;
import static com.thargoid.Main.inFolder;
import static com.thargoid.Main.log;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.*;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;


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
    // lda (l)
    // text markup, e.g. ontological (o)
    // inference (i)
    // jena framework (j)
    // e.g. pafgwloi
//4=delete files from each folder (0 or 1)
//5

public class Main {

    static public String now;
    static private Boolean LogToFile = true;
    static private String LogFileName;
    static public String WorkFolder;
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
    static String FrameNetFolder = "E:\\LaRheto\\fndata-1.5\\fndata-1.5";
    static model model;
    static jmodel jmodel;
    static List<String> Figures;


    public static void main(String[] args) {

        now = getNow();
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

        Figures = new ArrayList<>();
        Figures.add("Epanaphora");

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

                        WorkFolder = WorkFolder.replace(inFolder, "5_OntoParsed");
                        //set up model here?

                    log("Ending ontoloy population");

                    break;

                case 'j':

                    //ontology population
                    log("Starting Jena ontoloy population");

                    setupParseLookups();
                    JontoParse("1", "1");

                    WorkFolder = WorkFolder.replace(inFolder, "6_JOntoParsed");
                    //set up model here?

                    log("Ending Jena ontoloy population");

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

    static private boolean JontoParse(String pInputType, String pOutputType)
    {
        log("Started Onto Parse");
        boolean output = false;
        String ParseFolder = WorkFolder + File.separator + inFolder;
        String InputType = pInputType;
        //1 - plain text file
        //2 - csv line per sentence with associated extra detail
        //3 - csv two first columns are sentences to be parsed
        String OutputType = pOutputType;

        String outputFolder = ParseFolder.replace(inFolder, "6_JOntoParsed");
        inFolder = "6_JOntoParsed";
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

                log("Created ontology model for " + tf.getName());

                jmodel= new jmodel();
                jmodel.addIndividual("DocStruct", "doc", "doc");

                String everything;
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
                    everything = sb.toString();
                    log(everything);
                } finally {
                    br.close();
                }
                log("Read file text into variable");

                JontoParseText(1, everything);
                jmodel.reasoning();
                String outputfile = jmodel.outputToFile(outputFolder, tf.getName());

                Scanner fileScanner = new Scanner(new File(outputfile));
                String line = null;
                fileScanner.nextLine();
                PrintWriter writer = new PrintWriter(outputfile.replace(".owl", "_2.owl"), "UTF-8");
                int o=0;
                while (fileScanner.hasNextLine()) {
                    line = fileScanner.next();
                    writer.println(line);
                    writer.flush();
                    o++;
                }

                writer.close();

                /*
                //Path path = Paths.get(outputfile);
                //Charset charset = StandardCharsets.UTF_8;
                //String content = new String(Files.readAllBytes(path), charset);
                //String content2 = null;

                Scanner fileScanner = new Scanner(new File(outputfile));
                String line = null;
                fileScanner.nextLine();

                PrintWriter writer = new PrintWriter(outputfile, "UTF-8");

                while (fileScanner.hasNextLine())
                {
                    line = fileScanner.next();

                    if(!line.contains("</")){
                        for(int figs=0;figs<Figures.size();figs++){

                            if(line.contains(":" + Figures.get(figs) + ">")) {
                                UUID uuid = UUID.randomUUID();
                                String randomUUIDString = uuid.toString();

                                line = line.replaceFirst(":" + Figures.get(figs) + ">", ":" + Figures.get(figs) + " rdf:resource=\"" + jmodel.ns_new + "#" + Figures.get(figs) + "_" + randomUUIDString.substring(1, 8) + "\">");
                            }
                        }
                    }
                    //content2 = content2 + line;
                    writer.println(line);
                }

                writer.flush();
                writer.close();
*/

                //Files.write(path, content.getBytes(content2));
            }
        }
        catch(Exception ex)
        {
            log("Error:-" + ex.toString() + ", " + ex.getMessage() + ", " + ex.getLocalizedMessage());
        }

        return output;
    }

    static void JontoParseText(int type, String corpus)
    {
        log("Started OntoParseText");
        try
        {
            String[] outs = null;
            Annotation doc = new Annotation(corpus);
            pipeline.annotate(doc);
            List<CoreMap> sentences = doc.get(CoreAnnotations.SentencesAnnotation.class);
            log("Created Annotation and Pipeline");

            jmodel.addIndividual("Gate", "Paragraph", "p1");
            jmodel.addObjectProperty("doc", "hasParagraph", "p1");

            int id = 0;
            int sc = 0;
            int wc = 0;
            int np = 1;
            for(CoreMap sentence : sentences)
            {
                id++;
                sc++;
                String sn = "s" + sc;
                String sp = "s" + Integer.toString(sc-1);
                String se = "s" + Integer.toString(sc+1);


                jmodel.addIndividual("Gate", "Sentence", sn);
                jmodel.addDatatypeProperty(sn, "hasID",  String.valueOf(id), "int");
                jmodel.addObjectProperty("p1", "hasSentence", sn);

                if(sc==1)
                {
                    jmodel.addObjectProperty("p1", "hasFirstSentence", sn);
                }
                else
                {
                    jmodel.addObjectProperty(sn, "hasPreviousSentence", sp);
                }


                if(sc==sentences.size())
                {
                    jmodel.addObjectProperty("p1", "hasLastSentence", sn);
                }
                else
                {
                    jmodel.addIndividual("Gate", "Sentence", se);
                    jmodel.addObjectProperty(sn, "hasNextSentence", se);
                }

                String Sx = sentence.toString();
                String[] words = Sx.split(" ");

                jmodel.addDatatypeProperty(sn, "hasStartNode",  String.valueOf(np), "int");

                log("Parsed sentence " + sc);
                int wc1 = 0;
                for(String w : words)
                {
                    if(w.length()>0) {
                        wc++;
                        wc1++;
                        w = w.replace(":", "");
                        w = w.replace(";", "");
                        w = w.replace(",", "");
                        w = w.replace(".", "");
                        w = w.replace("?", "");
                        w = w.replace("(", "");
                        w = w.replace(")", "");
                        String wn = "w" + wc;
                        String wp = "w" + Integer.toString(wc - 1);
                        String we = "w" + Integer.toString(wc + 1);
                        jmodel.addIndividual("Gate", "word", wn);
                        jmodel.addObjectProperty(sn, "hasWord", wn);
                        jmodel.addDatatypeProperty(wn, "hasString",  String.valueOf(w), "string");

                        id++;
                        jmodel.addDatatypeProperty(wn, "hasID",  String.valueOf(id), "int");

                        jmodel.addDatatypeProperty(wn, "hasStartNode",  String.valueOf(np), "int");
                        np = np + w.length();
                        jmodel.addDatatypeProperty(wn, "hasEndNode",  String.valueOf(np), "int");

                        if (wc1 == 1) {
                            jmodel.addObjectProperty(sn, "hasFirstWord", wn);
                        } else {
                            jmodel.addObjectProperty(wn, "hasPreviousWord", wp);
                        }

                        if (wc1 == words.length) {
                            jmodel.addDatatypeProperty(sn, "hasEndNode",  String.valueOf(np-1), "int");
                            jmodel.addObjectProperty(sn, "hasLastWord", wn);
                        } else {
                            jmodel.addIndividual("Gate", "word", we);
                            jmodel.addObjectProperty(wn, "hasNextWord", we);
                        }

                        jmodel.addDatatypeProperty(wn, "hasFirstCharacter",  w.substring(0, 1), "string");

                        log("Parsed word " + wc1);
                        // sameAsWord
                    }
                }
                int p=0;
            }
            log("Finished OntoParseText");
        }
        catch (Exception ex)
        {
            log("Error:-" + ex.toString() + ", " + ex.getMessage() + ", " + ex.getLocalizedMessage());
            ex.printStackTrace();
        }
    }

    static private boolean ontoParse(String pInputType, String pOutputType)
    {
        log("Started Onto Parse");
        boolean output = false;
        String ParseFolder = WorkFolder + File.separator + inFolder;
        String InputType = pInputType;
        //1 - plain text file
        //2 - csv line per sentence with associated extra detail
        //3 - csv two first columns are sentences to be parsed
        String OutputType = pOutputType;

        String outputFolder = ParseFolder.replace(inFolder, "5_OntoParsed");
        inFolder = "5_OntoParsed";
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

                log("Created ontology model for " + tf.getName());
                model = new model();
                model.addIndividual("DocStruct", "doc", "doc");

                String everything;
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
                    everything = sb.toString();
                    log(everything);
                } finally {
                    br.close();
                }
                log("Read file text into variable");

                ontoParseText(1, everything);
                //model.runSWRLAnaphora();
                model.runSWRL();
                model.reasonPellet();
                model.outputToFile(outputFolder, tf.getName());
            }
        }
        catch(Exception ex)
        {
            log("Error:-" + ex.toString() + ", " + ex.getMessage() + ", " + ex.getLocalizedMessage());
        }

        return output;
    }

    static void ontoParseText(int type, String corpus)
    {
        log("Started OntoParseText");
        try
        {
            String[] outs = null;
            Annotation doc = new Annotation(corpus);
            pipeline.annotate(doc);
            List<CoreMap> sentences = doc.get(CoreAnnotations.SentencesAnnotation.class);
            log("Created Annotation and Pipeline");

            model.addIndividual("Gate", "Paragraph", "p1");
            model.addObjectProperty("DocStruct", "hasParagraph", "doc", "p1");

            int id = 0;
            int sc = 0;
            int wc = 0;
            int np = 1;
            for(CoreMap sentence : sentences)
            {
                id++;
                sc++;
                String sn = "s" + sc;
                String sp = "s" + Integer.toString(sc-1);
                String se = "s" + Integer.toString(sc+1);

                model.addDatatypeProperty("Gate", "hasID", sn, String.valueOf(id), "int");
                model.addIndividual("Gate", "Sentence", sn);
                model.addObjectProperty("DocStruct", "hasSentence", "p1", sn);

                if(sc==1)
                {
                    model.addObjectProperty("DocStruct", "hasFirstSentence", "p1", sn);
                }
                else
                {
                    model.addObjectProperty("DocStruct", "hasPreviousSentence", sn, sp);
                }


                if(sc==sentences.size())
                {
                    model.addObjectProperty("DocStruct", "hasLastSentence", "p1", sn);
                }
                else
                {
                    model.addObjectProperty("DocStruct", "hasNextSentence", sn, se);
                }

                String Sx = sentence.toString();
                String[] words = Sx.split(" ");

                model.addDatatypeProperty("Gate", "hasStartNode", sn, String.valueOf(np), "int");

                log("Parsed sentence " + sc);
                int wc1 = 0;
                for(String w : words)
                {
                    if(w.length()>0) {
                        wc++;
                        wc1++;
                        w = w.replace(":", "");
                        w = w.replace(";", "");
                        w = w.replace(",", "");
                        w = w.replace(".", "");
                        w = w.replace("?", "");
                        w = w.replace("(", "");
                        w = w.replace(")", "");
                        String wn = "w" + wc;
                        String wp = "w" + Integer.toString(wc - 1);
                        String we = "w" + Integer.toString(wc + 1);
                        model.addIndividual("Gate", "word", wn);
                        model.addObjectProperty("DocStruct", "hasWord", sn, wn);
                        model.addDatatypeProperty("Gate", "hasString", wn, String.valueOf(w), "str");

                        id++;
                        model.addDatatypeProperty("Gate", "hasID", wn, String.valueOf(id), "int");

                        model.addDatatypeProperty("Gate", "hasStartNode", wn, String.valueOf(np), "int");
                        np = np + w.length();
                        model.addDatatypeProperty("Gate", "hasEndNode", wn, String.valueOf(np), "int");


                        if (wc1 == 1) {
                            model.addObjectProperty("DocStruct", "hasFirstWord", sn, wn);
                        } else {
                            model.addObjectProperty("DocStruct", "hasPreviousWord", wn, wp);
                        }

                        if (wc1 == words.length) {
                            model.addDatatypeProperty("Gate", "hasEndNode", sn, String.valueOf(np - 1), "int");
                            model.addObjectProperty("DocStruct", "hasLastWord", sn, wn);
                        } else {
                            model.addObjectProperty("DocStruct", "hasNextWord", wn, we);
                        }

                        model.addDatatypeProperty("DocStruct", "hasFirstCharacter", wn, w.substring(0, 1), "str");

                        log("Parsed word " + wc1);
                        // sameAsWord
                    }
                }
                int p=0;
            }
            log("Finished OntoParseText");
        }
        catch (Exception ex)
        {
            log("Error:-" + ex.toString() + ", " + ex.getMessage() + ", " + ex.getLocalizedMessage());
        }
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

    static public void log(String text)
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

        log("Parse lookups completed");
    }

}


class jmodel{

    String ns_DocStruct;
    String ns_gate;
    String ns_LassoingRhetoric;
    String ns_RhetDev;
    public String ns_new;
    OntModel mod_DocStruct;
    OntModel mod_gate;
    OntModel mod_LassoingRhetoric;
    OntModel mod_RhetDev;
    OntModel mod_new;
    OntClass c_Doc;
    OntClass c_Sentence;
    OntClass c_word;
    OntClass c_Paragraph;
    OntClass c_RhetoricalDevice;
    Individual i_Anaphora;
    ObjectProperty op_hasParagraph;
    ObjectProperty op_hasSentence;
    ObjectProperty op_hasNextWord;
    ObjectProperty op_hasPreviousWord;
    ObjectProperty op_hasNextSentence;
    ObjectProperty op_hasFirstSentence;
    ObjectProperty op_hasPreviousSentence;
    ObjectProperty op_hasLastSentence;
    ObjectProperty op_hasWord;
    DatatypeProperty dp_hasID;
    DatatypeProperty dp_hasString;
    DatatypeProperty dp_hasStartNode;
    DatatypeProperty dp_hasEndNode;
    DatatypeProperty dp_hasFirstCharacter;
    ObjectProperty op_hasFirstWord;
    ObjectProperty op_hasLastWord;
    ObjectProperty op_hasRhetoricalDevice;
    String outputformat;

    public jmodel(){

        try
        {
            ns_gate = "http://repositori.com/sw/onto/gate.owl";
            mod_gate = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF);
            mod_gate.read(ns_gate);

            ns_LassoingRhetoric = "http://repositori.com/sw/onto/LassoingRhetoric.owl";
            mod_LassoingRhetoric = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF);
            mod_LassoingRhetoric.read(ns_LassoingRhetoric);

            ns_DocStruct = "http://repositori.com/sw/onto/DocStruct.owl";
            mod_DocStruct = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF);
            mod_DocStruct.read(ns_DocStruct);

            ns_RhetDev = "http://www.repositori.com/sw/onto/RhetoricalDevices.owl";
            mod_RhetDev = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF);
            mod_RhetDev.read(ns_RhetDev);

            ns_new = "http://repositori.com/sw/onto/jj_" + Main.now + ".owl";
            mod_new = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF);

            outputformat = "RDF/XML-ABBREV";
            setupClasses();
            inFolder = "6_JOntoParsed";

        }
            catch (Exception e)
        {
            System.out.println("Error: " + e.toString() + " - " + e.getMessage());
            System.out.println(e.toString());
        }


    }

    public void reasoning()
    {
        try
        {
            Reasoner reasoner = new GenericRuleReasoner(Rule.rulesFromURL("E:\\tamesis\\rules.txt"));
            InfModel infModel = ModelFactory.createInfModel(reasoner, mod_new);

            StmtIterator it = infModel.listStatements();

            List<Statement> sts = new ArrayList<Statement>();

            while(it.hasNext()){

                Statement stmt = it.nextStatement();
                Resource subject = stmt.getSubject();
                Property predicate = stmt.getPredicate();
                RDFNode object = stmt.getObject();

                if(!subject.toString().contains("www.w3.org") & !predicate.toString().contains("rdf-schema#subPropertyOf") & !object.toString().contains("rdf-schema#Resource") & !object.toString().contains("rdf-syntax-ns#Property")) {


                    if(!subject.toString().contains("reposito")){
                        sts.add(stmt);
                    }

                    if(predicate.toString().contains("hasRhetoricalDevice")){
                        sts.add(stmt);
                    }

                    log(subject.toString() + " " + predicate.toString() + " " + object.toString());

                }
            }

            for(int h=0; h<sts.size() ; h++) {
                mod_new.add(sts.get(h));
            }

        }
                catch (Exception e)
        {
            System.out.println("Error: " + e.toString() + " - " + e.getMessage());
            System.out.println(e.toString());
        }
    }

    public void addObjectProperty(String i1, String objprop, String i2)
    {
        Resource r1 = mod_new.getIndividual(ns_new + '#' + i1);
        Resource r2 = mod_new.getIndividual(ns_new + '#' + i2);
        mod_new.add(r1, getOntObjProp(objprop), r2);
    }

    public void addDatatypeProperty(String i1, String datprop, String i2, String LitType)
    {
        Resource r1 = mod_new.getIndividual(ns_new + '#' + i1);
        Literal lit = null;

        if(LitType.equals("int"))
        {
            lit = mod_new.createTypedLiteral(i2, org.apache.jena.datatypes.xsd.XSDDatatype.XSDint);
        }
        else if(LitType.equals("string"))
        {
            lit = mod_new.createTypedLiteral(i2, org.apache.jena.datatypes.xsd.XSDDatatype.XSDstring);
        }

        mod_new.add(r1, getOntDataProp(datprop), lit);
    }


    public void addIndividual(String onto, String owclass, String value)
    {
        Iterator<Individual> i = mod_new.listIndividuals();
        boolean exists = false;


        while(i.hasNext()){

            Individual is = i.next();
            if(is.getLocalName().equals('#' + value)) {
                exists = true;
            }
        }

        if(!exists) {
            mod_new.createIndividual(ns_new + '#' + value, getOntClass(owclass));
        }
    }

    public Property getOntDataProp(String type) {
        Property r = null;

        if (type.equals("hasString")) {
            r = dp_hasString;
        } else if (type.equals("hasID")) {
            r = dp_hasID;
        } else if (type.equals("hasStartNode")) {
            r = dp_hasStartNode;
        } else if (type.equals("hasEndNode")) {
            r = dp_hasEndNode;
        } else if (type.equals("hasFirstCharacter")) {
            r = dp_hasFirstCharacter;
        }
        return r;
    }

    public Property getOntObjProp(String type)
    {
        Property r = null;

        if(type.equals("hasParagraph"))
        {
            r = op_hasParagraph;
        }
        else if(type.equals("hasSentence"))
        {
            r = op_hasSentence;
        }
        else if(type.equals("hasWord"))
        {
            r = op_hasWord;
        }
        else if(type.equals("hasNextWord"))
        {
            r = op_hasNextWord;
        }
        else if(type.equals("hasPreviousWord"))
        {
            r = op_hasPreviousWord;
        }
        else if(type.equals("hasNextSentence"))
        {
            r = op_hasNextSentence;
        }
        else if(type.equals("hasFirstSentence"))
        {
            r = op_hasFirstSentence;
        }
        else if(type.equals("hasPreviousSentence"))
        {
            r = op_hasPreviousSentence;
        }
        else if(type.equals("hasLastSentence"))
        {
            r = op_hasLastSentence;
        }
        else if(type.equals("hasFirstWord"))
        {
            r = op_hasFirstWord;
        }
        else if(type.equals("hasLastWord"))
        {
            r = op_hasLastWord;
        }
        else if(type.equals("hasRhetoricalDevice"))
        {
            r = op_hasRhetoricalDevice;
        }

        return r;
    }

    public OntClass getOntClass(String type)
    {
        OntClass r = null;

        if(type.equals("doc"))
        {
            r = c_Doc;
        }
        else if(type.equals("word"))
        {
            r = c_word;
        }
        else if(type.equals("Sentence"))
        {
            r = c_Sentence;
        }
        else if(type.equals("Paragraph"))
        {
            r = c_Paragraph;
        }
        else if(type.equals("Anaphore"))
        {
            r = c_RhetoricalDevice;
        }

        return r;
    }

    public String outputToFile(String saveFolder, String fileName) {
        String u="";
        try {
            fileName = fileName.replace(".", "");
            u = saveFolder + File.separator + "ont_" + Main.now + "_" + fileName + ".owl";

            log("Outputting file " + u);
            FileWriter out = new FileWriter( u );
            mod_new.write(out, outputformat);
            out.close();

        }
        catch (Exception e) {
            System.out.println("Error: " + e.toString() + " - " + e.getMessage());
            System.out.println(e.toString());
        }
        return u;
    }

    private void setupClasses()
    {
        c_Doc  = mod_DocStruct.getOntClass(ns_DocStruct + "#Doc");
        c_Sentence  = mod_gate.getOntClass(ns_gate + "#Sentence");
        c_word  = mod_gate.getOntClass(ns_gate + "#word");
        c_Paragraph  = mod_gate.getOntClass(ns_gate + "#Paragraph");
        c_RhetoricalDevice  = mod_RhetDev.getOntClass(ns_RhetDev + "#RhetoricalDevice");
        i_Anaphora = mod_RhetDev.getIndividual(ns_RhetDev + "#Anaphora");
        op_hasParagraph = mod_DocStruct.getObjectProperty(ns_DocStruct + "#hasParagraph");
        op_hasSentence = mod_DocStruct.getObjectProperty(ns_DocStruct + "#hasSentence");
        op_hasNextWord = mod_DocStruct.getObjectProperty(ns_DocStruct + "#hasNextWord");
        op_hasPreviousWord = mod_DocStruct.getObjectProperty(ns_DocStruct + "#hasPreviousWord");
        op_hasWord = mod_DocStruct.getObjectProperty(ns_DocStruct + "#hasWord");
        op_hasNextSentence = mod_DocStruct.getObjectProperty(ns_DocStruct + "#hasNextSentence");
        op_hasFirstSentence = mod_DocStruct.getObjectProperty(ns_DocStruct + "#hasFirstSentence");
        op_hasPreviousSentence = mod_DocStruct.getObjectProperty(ns_DocStruct + "#hasPreviousSentence");
        op_hasLastSentence = mod_DocStruct.getObjectProperty(ns_DocStruct + "#hasLastSentence");
        dp_hasString = mod_gate.getDatatypeProperty(ns_gate + "#hasString");
        dp_hasStartNode = mod_gate.getDatatypeProperty(ns_gate + "#hasStartNode");
        dp_hasEndNode = mod_gate.getDatatypeProperty(ns_gate + "#hasEndNode");
        dp_hasFirstCharacter = mod_DocStruct.getDatatypeProperty(ns_DocStruct + "#hasFirstCharacter");
        op_hasFirstWord = mod_DocStruct.getObjectProperty(ns_DocStruct + "#hasFirstWord");
        op_hasLastWord = mod_DocStruct.getObjectProperty(ns_DocStruct + "#hasLastWord");
        op_hasRhetoricalDevice = mod_RhetDev.getObjectProperty(ns_RhetDev + "#hasRhetoricalDevice");
        dp_hasID = mod_gate.getDatatypeProperty(ns_gate + "#hasID");

    }

}

class model {

    //static String NowD;
    static String folderbase;
    String baseIRI;
    OWLOntologyManager om;
    OWLOntology ont;
    OWLDataFactory df;
    DefaultPrefixManager pm;
    OWLOntologyManager om_DocStruct;
    OWLOntology ont_DocStruct;
    OWLDataFactory fac_DocStruct;
    DefaultPrefixManager pm_DocStruct;
    OWLOntologyManager om_LassRhet;
    OWLOntology ont_LassRhet;
    OWLDataFactory fac_LassRhet;
    DefaultPrefixManager pm_LassRhet;
    OWLOntologyManager om_Gate;
    OWLOntology ont_Gate;
    OWLDataFactory fac_Gate;
    DefaultPrefixManager pm_Gate;
    OWLOntologyManager om_RhetDev;
    OWLOntology ont_RhetDev;
    OWLDataFactory fac_RhetDev;
    DefaultPrefixManager pm_RhetDev;
    OWLClass DocStruct_doc;
    OWLClass Gate_word;
    OWLClass Gate_sentence;
    OWLClass Gate_paragraph;
    OWLClass RhetDev_RhetoricalDevice;
    OWLObjectProperty hasParagraph;
    OWLObjectProperty hasSentence;
    OWLObjectProperty hasNextWord;
    OWLObjectProperty hasFirstWord;
    OWLObjectProperty hasNextSentence;
    OWLObjectProperty hasRhetoricalDevice;
    OWLDataProperty hasString;
    OWLDataProperty hasStartNode;
    OWLDataProperty hasEndNode;
    OWLIndividual RhetDev_Anaphora;

    public model(){

        try {

            baseIRI = "http://repositori.com/sw/onto/" + getNow() + ".owl";
            IRI newIRI = IRI.create(baseIRI);
            om = OWLManager.createOWLOntologyManager();
            ont = om.createOntology(newIRI);
            df = om.getOWLDataFactory();
            pm = new DefaultPrefixManager(); //null, null, ont.toString()
            pm.setPrefix("DocStruct:", "http://repositori.com/sw/onto/DocStruct.owl#");
            pm.setPrefix("gate:", "http://repositori.com/sw/onto/gate.owl#");
            //OWLDocumentFormat odf = new OWLXMLDocumentFormat();
            //OWLOntologyXMLNamespaceManager onm = new OWLOntologyXMLNamespaceManager(ont, odf);
            //onm.setPrefix("DocStruct:", "http://repositori.com/sw/onto/DocStruct.owl#");

            String URLDocStruct = "http://repositori.com/sw/onto/DocStruct.owl";
            om_DocStruct = OWLManager.createOWLOntologyManager();
            IRI ontDocStruct = IRI.create(URLDocStruct);
            ont_DocStruct = om_DocStruct.loadOntology(ontDocStruct);
            fac_DocStruct = om_DocStruct.getOWLDataFactory();
            pm_DocStruct = new DefaultPrefixManager(null, null, ontDocStruct.toString());

            String URLGate = "http://repositori.com/sw/onto/gate.owl";
            om_Gate = OWLManager.createOWLOntologyManager();
            IRI ontGate = IRI.create(URLGate);
            ont_Gate = om_Gate.loadOntology(ontGate);
            fac_Gate = om_Gate.getOWLDataFactory();
            pm_Gate = new DefaultPrefixManager(null, null, ontGate.toString());

            String URLLassRhet = "http://repositori.com/sw/onto/LassoingRhetoric.owl";
            om_LassRhet = OWLManager.createOWLOntologyManager();
            IRI ontLassRhet = IRI.create(URLLassRhet);
            ont_LassRhet = om_LassRhet.loadOntology(ontLassRhet);
            fac_LassRhet = om_LassRhet.getOWLDataFactory();
            pm_LassRhet = new DefaultPrefixManager(null, null, ontLassRhet.toString());

            String URLRhetDev = "http://www.repositori.com/sw/onto/RhetoricalDevices.owl";
            om_RhetDev = OWLManager.createOWLOntologyManager();
            IRI ontRhetDev = IRI.create(URLRhetDev);
            ont_RhetDev = om_RhetDev.loadOntology(ontRhetDev);
            fac_RhetDev = om_RhetDev.getOWLDataFactory();
            pm_RhetDev= new DefaultPrefixManager(null, null, ontRhetDev.toString());

            setupClasses();

            String DocStruct_base = "http://repositori.com/sw/onto/DocStruct.owl#";
            String Gate_base = "http://repositori.com/sw/onto/gate.owl#";
            String LassRhet_base = "http://repositori.com/sw/onto/LassoingRhetoric.owl#";

            String OntoParseFolder = com.thargoid.Main.WorkFolder + File.separator + inFolder;
           // String folderbase = OntoParseFolder.replace(inFolder, "5_OntoParsed");
            inFolder = "5_OntoParsed";

        }
        catch (Exception e)
        {
            System.out.println("Error: " + e.toString() + " - " + e.getMessage());
            System.out.println(e.toString());
        }
    }

    public OWLClass getClassType(String type)
    {
        OWLClass r = null;

        if(type.equals("doc"))
        {
            r = DocStruct_doc;
        }
        else if(type.equals("word"))
        {
            r = Gate_word;
        }
        else if(type.equals("Sentence"))
        {
            r = Gate_sentence;
        }
        else if(type.equals("Paragraph"))
        {
            r = Gate_paragraph;
        }

        return r;
    }

    public void addIndividual(String onto, String owclass, String value)
    {
        OWLAxiom ax1 = null;
        if(onto.equals("DocStruct"))
        {
            OWLNamedIndividual i1 = df.getOWLNamedIndividual(IRI.create("#" + value));
            ax1 = df.getOWLClassAssertionAxiom(getClassType(owclass), i1);
        }
        else if(onto.equals("Gate"))
        {
            OWLNamedIndividual i1 = df.getOWLNamedIndividual(IRI.create("#" + value));
            ax1 = df.getOWLClassAssertionAxiom(getClassType(owclass), i1);
        }

        AddAxiom addax1 = new AddAxiom(ont, ax1);
        om.applyChange(addax1);
    }

    public void addObjectProperty(String onto, String owProp, String domain, String range)
    {
        OWLObjectPropertyAssertionAxiom ax1 = null;

        if(onto.equals("DocStruct"))
        {
            OWLNamedIndividual id = df.getOWLNamedIndividual(IRI.create("#" + domain));
            OWLNamedIndividual ir = df.getOWLNamedIndividual(IRI.create("#" + range));
            OWLObjectProperty p = fac_DocStruct.getOWLObjectProperty("#" + owProp, pm_DocStruct);
            ax1 = df.getOWLObjectPropertyAssertionAxiom(p, id, ir);
        }
        else if(onto.equals("Gate"))
        {
            OWLNamedIndividual id = df.getOWLNamedIndividual(IRI.create("#" + domain));
            OWLNamedIndividual ir = df.getOWLNamedIndividual(IRI.create("#" + range));
            OWLObjectProperty p = fac_Gate.getOWLObjectProperty("#" + owProp, pm_Gate);
            ax1 = df.getOWLObjectPropertyAssertionAxiom(p, id, ir);
        }

        AddAxiom addax1 = new AddAxiom(ont, ax1);
        om.applyChange(addax1);
    }

    public void addDatatypeProperty(String onto, String owProp, String domain, String value, String datatypetype)
    {
        OWLDataPropertyAssertionAxiom ax1 = null;

        if(onto.equals("DocStruct"))
        {
            OWLDatatype odt = null;
            OWLLiteral ol = null;

            if(datatypetype.equals("int"))
            {
                odt = df.getOWLDatatype(OWL2Datatype.XSD_INTEGER.getIRI());
                ol = df.getOWLLiteral(value, odt);
            }
            if(datatypetype.equals("str"))
            {
                odt = df.getOWLDatatype(OWL2Datatype.XSD_STRING.getIRI());
                ol = df.getOWLLiteral(value, odt);
            }

            OWLNamedIndividual id = df.getOWLNamedIndividual(IRI.create("#" + domain));
            OWLDataProperty p = fac_DocStruct.getOWLDataProperty("#" + owProp, pm_DocStruct);

            ax1 = df.getOWLDataPropertyAssertionAxiom(p, id, ol);
        }
        else if(onto.equals("Gate"))
        {
            OWLDatatype odt = null;
            OWLLiteral ol = null;

            if(datatypetype.equals("int"))
            {
                odt = df.getOWLDatatype(OWL2Datatype.XSD_INTEGER.getIRI());
                ol = df.getOWLLiteral(value, odt);
            }
            if(datatypetype.equals("str"))
            {
                odt = df.getOWLDatatype(OWL2Datatype.XSD_STRING.getIRI());
                ol = df.getOWLLiteral(value, odt);
            }

            OWLNamedIndividual id = df.getOWLNamedIndividual(IRI.create("#" + domain));
            OWLDataProperty p = fac_Gate.getOWLDataProperty("#" + owProp, pm_Gate);
            ax1 = df.getOWLDataPropertyAssertionAxiom(p, id, ol);
        }

        AddAxiom addax1 = new AddAxiom(ont, ax1);
        om.applyChange(addax1);
    }

    public void reasonPellet()
    {
        try {
            //PelletExplanation.setup();
            //ManchesterSyntaxExplanationRenderer renderer = new ManchesterSyntaxExplanationRenderer();
            //PrintWriter out = new PrintWriter(System.out);
            //renderer.startRendering(out);
/*
            PelletReasoner reasoner = PelletReasonerFactory.getInstance().createReasoner(ont);

            //PelletExplanation pellex = new PelletExplanation(reasoner);

            //OWLClass doc = OWL.Class(baseIRI + "#doc");
            //Set<Set<OWLAxiom>> exp = pellex.getUnsatisfiableExplanations(doc);
            //out.println(doc);

            //renderer.render(exp);

            //boolean t = true;
            //reasoner.getKB().setDoExplanation(t);

            reasoner.getKB().realize();
            log("Pellet consistency: " + reasoner.isConsistent());
            reasoner.getKB().printClassTree();
            Set<com.clarkparsia.pellet.rules.model.Rule> rules = reasoner.getKB().getRules();
            for (com.clarkparsia.pellet.rules.model.Rule r : rules) {
                log(r.toString());
            }
            log(reasoner.getKB().getInfo());
            log(reasoner.getKB().toString());

            /*InferredObjectPropertyAxiomGenerator<OWLObjectPropertyAxiom> generator = new InferredObjectPropertyAxiomGenerator<OWLObjectPropertyAxiom>(){

                @Override
                public String getLabel() {
                    return null;
                }

                @Override
                protected void addAxioms(OWLObjectProperty entity,
                                         OWLReasoner reasoner, OWLDataFactory dataFactory,
                                         Set<OWLObjectPropertyAxiom> result) {}
            };
            generator.createAxioms(df, reasoner);
            log(generator.toString());

            InferredOntologyGenerator gen = new InferredOntologyGenerator(reasoner);
            gen.fillOntology(df, ont);
            */
        }
        catch(Exception ex)
        {
            System.out.println("Error: " + ex.toString() + " - " + ex.getMessage());
            System.out.println(ex.toString());
        }
    }

    public void runSWRL()
    {
        try
        {
            SWRLRuleEngine ruleEngule = SWRLAPIFactory.createSWRLRuleEngine(ont_LassRhet);

            Set<SWRLAPIRule> rules = ruleEngule.getSWRLRules();

            for (SWRLAPIRule rule:rules)
            {
                log(rule.getRuleName().toString());
                log(rule.toString());
                if(rule.toString().contains("sameAsWord") && !rule.toString().contains("Chaismus")) {

                    ont.getOWLOntologyManager().addAxiom(ont, rule);
                }
            }


            //SWRLClassAtom at1 = fac_LassRhet.getSWRLClassAtom(IRI.create("#" + ))
            //SWRLRule r1 = fac_LassRhet.getSWRLRule(IRI.create("#lassoAnaphora1"));

            //SWRLRuleEngine ruleEngine = SWRLAPIFactory.createSWRLRuleEngine(ont_LassRhet);
            //ruleEngine.infer();
            //System.out.println(ruleEngine.toString());

            //SQWRLQueryEngine queryEngine = SWRLAPIFactory.createSQWRLQueryEngine(ont);
            //SQWRLResult result = queryEngine.runSQWRLQuery("q1", "swrlb:add(?x, 2, 2) -> sqwrl:select(?x)");

            //if (result.next())
            //  System.out.println("x: " + result.getLiteral("x").getInt());

            //SWRLRuleEngine sre = SWRLAPIFactory.createSWRLRuleEngine(ont);
            //sre.infer();

            //SQWRLQueryEngine sqe = SWRLAPIFactory.createSQWRLQueryEngine(ont);
            //SQWRLResult r = sqe.runSQWRLQuery("q1", "http://repositori.com/sw/onto/DocStruct.owl:Doc(?h) ^ hasParagraph(?h, ?i) ^ hasSentence(?i, ?z) ^ word(?x) ^ hasNextWord(?x, ?y) ^ Sentence(?z) ^ hasFirstWord(?z,?x) ^ word(?a) ^ hasNextWord(?a, ?b) ^ Sentence(?c) ^ hasFirstWord(?c, ?a) ^ hasNextSentence(?z, ?c) ^ hasString(?x, ?d) ^ hasString(?y, ?e) ^ hasString(?a, ?f) ^ hasString(?b, ?g) ^ swrlb:equal(?d, ?f) ^ swrlb:equal(?e, ?g) ^ hasStartNode(?x, ?j) ^ hasEndNode(?b, ?k) -> hasRhetoricalDevice(?h, Anaphora) ^ hasStartNode(Anaphora, ?j) ^ hasEndNode(Anaphora, ?k)");

            //SQWRLResult r = sqe.runSQWRLQuery("q1", "gate:hasWord(?s1, ?w1) ^ gate:hasWord(?s2, ?w2) ^ hasString(?w1, ?st1) ^ hasString(?w2, ?st2) ^ swrlb:equals(?st1, ?st2) -> hasNextSentence(?s1, ?s2)");

            //if(r.next())
            // {
            //  System.out.println("Name" + r.toString());
            // }

            //OWLClass cdoc = df.getOWLClass("#Doc", pm);
            //OWLClass cSentence = df.getOWLClass("#Sentence", pm);
/*
            IRI ix = IRI.create(baseIRI + "#x");
            SWRLVariable vx = df.getSWRLVariable(ix);

            IRI iy = IRI.create(baseIRI + "#y");
            SWRLVariable vy = df.getSWRLVariable(iy);

            IRI iz = IRI.create(baseIRI + "#z");
            SWRLVariable vz = df.getSWRLVariable(iz);

            IRI ia = IRI.create(baseIRI + "#a");
            SWRLVariable va = df.getSWRLVariable(ia);

            IRI ib = IRI.create(baseIRI + "#b");
            SWRLVariable vb = df.getSWRLVariable(ib);

            IRI id = IRI.create(baseIRI + "#d");
            SWRLVariable vd = df.getSWRLVariable(id);

            IRI ie = IRI.create(baseIRI + "#e");
            SWRLVariable ve = df.getSWRLVariable(ie);


            Set<SWRLAtom> body = new TreeSet<SWRLAtom>();
            //body.add(df.getSWRLClassAtom(DocStruct_doc, v1));
            body.add(df.getSWRLClassAtom(Gate_sentence, vx));
            body.add(df.getSWRLClassAtom(Gate_sentence, vy));
            body.add(df.getSWRLClassAtom(Gate_word, va));
            body.add(df.getSWRLClassAtom(Gate_word, vb));
            body.add(df.getSWRLObjectPropertyAtom(hasFirstWord, vz, va));
            body.add(df.getSWRLObjectPropertyAtom(hasFirstWord, vy, vb));
            body.add(df.getSWRLDataPropertyAtom(hasString, va, vd));
            body.add(df.getSWRLDataPropertyAtom(hasString, vb, ve));

            List<SWRLDArgument> ea1 = new ArrayList<>(2);
            ea1.add(vd);
            ea1.add(ve);
            body.add(df.getSWRLBuiltInAtom(IRI.create("http://www.w3.org/2003/11/swrlb#equal"), ea1));

            Set<SWRLAtom> head = new TreeSet<SWRLAtom>();
            //head.add(df.getSWRLClassAtom(Gate_word, v1));
            head.add(df.getSWRLObjectPropertyAtom(hasNextSentence, vy, vx));

            SWRLRule rule = df.getSWRLRule(body, head);
            ont.getOWLOntologyManager().addAxiom(ont, rule);
*/
        }
        catch (Exception e)
        {
            System.out.println("Error: " + e.toString() + " - " + e.getMessage());
            System.out.println(e.toString());
        }
    }

    public void runSWRLAnaphora()
    {
        try
        {
            IRI ih = IRI.create(baseIRI + "#h");
            SWRLVariable vh = df.getSWRLVariable(ih);

            IRI ii = IRI.create(baseIRI + "#i");
            SWRLVariable vi = df.getSWRLVariable(ii);

            IRI iz = IRI.create(baseIRI + "#z");
            SWRLVariable vz = df.getSWRLVariable(iz);

            IRI ix = IRI.create(baseIRI + "#x");
            SWRLVariable vx = df.getSWRLVariable(ix);

            IRI iy = IRI.create(baseIRI + "#y");
            SWRLVariable vy = df.getSWRLVariable(iy);

            IRI ia = IRI.create(baseIRI + "#a");
            SWRLVariable va = df.getSWRLVariable(ia);

            IRI ib = IRI.create(baseIRI + "#b");
            SWRLVariable vb = df.getSWRLVariable(ib);

            IRI ic = IRI.create(baseIRI + "#c");
            SWRLVariable vc = df.getSWRLVariable(ic);

            IRI id = IRI.create("http://www.w3.org/2001/XMLSchema#string");
            SWRLVariable vd = df.getSWRLVariable(id);

            IRI ie = IRI.create("http://www.w3.org/2001/XMLSchema#string");
            SWRLVariable ve = df.getSWRLVariable(ie);

            IRI iif = IRI.create("http://www.w3.org/2001/XMLSchema#string");
            SWRLVariable vf = df.getSWRLVariable(iif);

            IRI ig = IRI.create("http://www.w3.org/2001/XMLSchema#string");
            SWRLVariable vg = df.getSWRLVariable(ig);

            IRI ij = IRI.create(baseIRI + "#j");
            SWRLVariable vj = df.getSWRLVariable(ij);

            IRI ik = IRI.create(baseIRI + "#k");
            SWRLVariable vk = df.getSWRLVariable(ik);

            IRI il = IRI.create(baseIRI + "#l");
            SWRLVariable vl = df.getSWRLVariable(il);

            Set<SWRLAtom> body = new TreeSet<SWRLAtom>();
            body.add(df.getSWRLClassAtom(DocStruct_doc, vh));
            // body.add(df.getSWRLClassAtom(RhetDev_RhetoricalDevice, vl));
            //SWRLIndividualArgument sia = new SWRLIndividualArgumentImpl(RhetDev_Anaphora);
            // body.add(df.getSWRLClassAtom(RhetDev_Anaphora, vl));
            //body.add(df.getSWRLIndividualArgument(RhetDev_Anaphora, vl));
            body.add(df.getSWRLClassAtom(Gate_paragraph, vi));
            body.add(df.getSWRLClassAtom(Gate_sentence, vz));
            body.add(df.getSWRLObjectPropertyAtom(hasParagraph, vh, vi));
            body.add(df.getSWRLObjectPropertyAtom(hasSentence, vi, vz));
            body.add(df.getSWRLClassAtom(Gate_word, vx));
            body.add(df.getSWRLClassAtom(Gate_word, vy));
            body.add(df.getSWRLObjectPropertyAtom(hasNextWord, vx, vy));
            body.add(df.getSWRLObjectPropertyAtom(hasFirstWord, vz, vx));
            body.add(df.getSWRLClassAtom(Gate_word, va));
            body.add(df.getSWRLClassAtom(Gate_word, vb));
            body.add(df.getSWRLObjectPropertyAtom(hasNextWord, va, vb));
            body.add(df.getSWRLClassAtom(Gate_sentence, vc));
            body.add(df.getSWRLObjectPropertyAtom(hasFirstWord, vc, va));
            body.add(df.getSWRLObjectPropertyAtom(hasNextSentence, vz, vc));

            SWRLIndividualArgument vm = df.getSWRLIndividualArgument(RhetDev_Anaphora);
            //body.add(df.getOWLNamedIndividual(vm);
            //body.add(df.getSWRLClassAtom(IRI.create("http://www.w3.org/2001/XMLSchema#string"), vd));
            //body.add(df.getOWLDataProperty(IRI.create("http://www.w3.org/2001/XMLSchema#string")), vd);

            body.add(df.getSWRLDataPropertyAtom(hasString, vx, vd));
            body.add(df.getSWRLDataPropertyAtom(hasString, vy, ve));
            body.add(df.getSWRLDataPropertyAtom(hasString, va, vf));
            body.add(df.getSWRLDataPropertyAtom(hasString, vb, vg));
            List<SWRLDArgument> ea1 = new ArrayList<SWRLDArgument>(2);
            ea1.add(vd);
            ea1.add(vf);
            body.add(df.getSWRLBuiltInAtom(IRI.create("http://www.w3.org/2003/11/swrlb#equal"), ea1));
            List<SWRLDArgument> ea2 = new ArrayList<SWRLDArgument>(2);
            ea2.add(ve);
            ea2.add(vg);
            body.add(df.getSWRLBuiltInAtom(IRI.create("http://www.w3.org/2003/11/swrlb#equal"), ea2));




            //body.add(df.getSWRLDataPropertyAtom(hasStartNode, vx, vj));
            //body.add(df.getSWRLDataPropertyAtom(hasEndNode, vb, vk));

            Set<SWRLAtom> head = new TreeSet<SWRLAtom>();
            head.add(df.getSWRLObjectPropertyAtom(hasRhetoricalDevice, vh, vm));
            //head.add(df.getSWRLDataPropertyAtom(hasStartNode, vl, vj));
            //head.add(df.getSWRLDataPropertyAtom(hasEndNode, vl, vk));

            SWRLRule rule = df.getSWRLRule(body, head);
            ont.getOWLOntologyManager().addAxiom(ont, rule);
        }
        catch (Exception e) {
            System.out.println("Error: " + e.toString() + " - " + e.getMessage());
            System.out.println(e.toString());
        }
    }

    public void outputToFile(String saveFolder, String fileName) {
        try {
            fileName = fileName.replace(".", "");
            String u = saveFolder + File.separator + "ont_" + Main.now + "_" + fileName + ".owl";
            File f = new File(u);
            log("Outputting file " + u);
            om.saveOntology(ont, IRI.create(f.toURI()));
        }
        catch (Exception e) {
            System.out.println("Error: " + e.toString() + " - " + e.getMessage());
            System.out.println(e.toString());
        }
    }

    private void setupClasses()
    {
        DocStruct_doc = fac_DocStruct.getOWLClass("#Doc", pm_DocStruct);
        Gate_word = fac_Gate.getOWLClass("#word", pm_Gate);
        Gate_sentence = fac_Gate.getOWLClass("#Sentence", pm_Gate);
        Gate_paragraph = fac_Gate.getOWLClass("#Paragraph", pm_Gate);
        RhetDev_RhetoricalDevice = fac_RhetDev.getOWLClass("#RhetoricalDevice", pm_RhetDev);
        RhetDev_Anaphora = fac_RhetDev.getOWLNamedIndividual("#Anaphora", pm_RhetDev);
        hasParagraph = fac_DocStruct.getOWLObjectProperty("#hasParagraph", pm_DocStruct);
        hasSentence = fac_DocStruct.getOWLObjectProperty("#hasSentence", pm_DocStruct);
        hasNextWord = fac_DocStruct.getOWLObjectProperty("#hasNextWord", pm_DocStruct);
        hasNextSentence = fac_DocStruct.getOWLObjectProperty("#hasNextSentence", pm_DocStruct);
        hasString = fac_Gate.getOWLDataProperty("hasString", pm_Gate);
        hasStartNode = fac_Gate.getOWLDataProperty("#hasStartNode", pm_Gate);
        hasEndNode = fac_Gate.getOWLDataProperty("#hasEndNode", pm_Gate);
        hasFirstWord = fac_DocStruct.getOWLObjectProperty("#hasFirstWord", pm_DocStruct);
        hasRhetoricalDevice = fac_RhetDev.getOWLObjectProperty("#hasRhetoricalDevice", pm_RhetDev);
    }

    public void printAxioms()
    {
        Set<OWLAxiom> ax = ont_DocStruct.getAxioms();

        for (OWLAxiom a : ax) {
            System.out.println(a.toString());
        }
    }

}