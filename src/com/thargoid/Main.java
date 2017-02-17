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

    public static void main(String[] args) {

        WorkFolder = args[1];
        if(args[2].equals("0"))
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

        if(args[0].equals("1"))
        {
            //lassoing rhetoric


        }

        if(args[0].equals("2"))
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
            try(Writer writer = new BufferedWriter
                    (new OutputStreamWriter
                            (new FileOutputStream(LogFileName, true), "utf-8")
                    )
                )
            {
                writer.write(getNow() + ":" + text + "\r");
                System.out.println(getNow() + ":" + text);
            }
        }
        catch(Exception ex)
        {
            System.out.println(ex.getMessage());
        }
    }


}
