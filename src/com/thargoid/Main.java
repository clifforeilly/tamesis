package com.thargoid;

//import edu.stanford.nlp.ling.CoreAnnotations;
//import edu.stanford.nlp.ling.CoreLabel;
//import edu.stanford.nlp.pipeline.Annotation;
//import edu.stanford.nlp.pipeline.StanfordCoreNLP;
//import edu.stanford.nlp.util.CoreMap;
//import com.opencsv.CSVReader;
//import com.opencsv.CSVWriter;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;


//args
//0=process type
    //1=lassoing rhetoric
    //2=iceni
//1=work folder
//2=log to file (0 or 1)

public class Main {

    static private Boolean LogToFile = true;
    static private String LogFileName;
    static private String WorkFolder;
    static private String quando;
    static private int AbstractArgCount;
    static private String[][] arguments;
    static private String[] args2;

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

    public static void main(String[] args) {

        args2 = args;
        AbstractArgCount = 3;
        arguments = new String[AbstractArgCount][2];
        arguments[0][0] = "ProcessType";
        arguments[0][1] = "0";
        arguments[0][2] = "1";
        arguments[1][0] = "WorkFolder";
        arguments[1][1] = "1";
        arguments[1][2] = "1";
        arguments[2][0] = "LogToFile";
        arguments[2][1] = "1";
        arguments[2][2] = "1";
        


        WorkFolder = getArg("WorkFolder");
        if(getArg("LogToFile").equals("0"))
        {
            LogToFile = false;
        }
        else
        {
            LogToFile = true;
        }

        for(int a = 0 ; a<args.length ; a++) {
            if(args[a] != null && !args[a].isEmpty())
            {
                log("args[" + a + "]=" + args[a]);
            }
        }
        setup();

        if(getArg("ProcessType").equals("1"))
        {
            //lassoing rhetoric


        }

        if(getArg("ProcessType").equals("2"))
        {
            //iceni
        }

        log("Ending Tamesis");

    }


    static private void setup()
    {
        try {
            quando = getNow();
            LogFileName = WorkFolder + File.separator + "log-" + quando + ".txt";
            if (LogToFile) {
                log("Starting Tamesis");
            }
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
                    writer.write(getNow() + ":" + text + "\r");
                }   
            }
            System.out.println(getNow() + ":" + text);
           
        }
        catch(Exception ex)
        {
            System.out.println(ex.getMessage());
        }
    }


}
