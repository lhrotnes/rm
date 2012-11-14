package no.snd.rm;

import java.io.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * This application monitors and sends reports for Riksmedia ads, integrated on Aftenposten
 */
public class App {

    private static Set<String> adDatabase;

    /**
     * Starting point for application
     *
     * @param args 0. database file ad ids, 1. backup dir, 2. url for html page, 3. email recipients (comma separated)
     */
    public static void main(String[] args) {
        //0. initialize the application
        init();

        //1. read existing ids from file into "database"
        readExistingIdsFromFile(adDatabase, args[0]);

        //2. read html with ads

        //3. extract ad ids from html

        //4. add new ids to database

        //5. store ids from database to file on disk

        //6. if today is friday, send report with ids to email recipients and store the report in backup directory, delete current db file.

    }


    public static void readExistingIdsFromFile(Set<String> set, String filePath) {
        try {
            //Try read file, if not exists, create the file
            File file = new File(filePath);
            if(!file.exists()){
                file.createNewFile();
            }

            FileInputStream fstream = new FileInputStream(filePath);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            //Read File Line By Line
            while ((strLine = br.readLine()) != null) {
                // Print the content on the console
                System.out.println(strLine);
                set.add(strLine);
            }
            //Close the input stream
            in.close();

            printSetToConsole();
        } catch (Exception e) {//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
    }


    private static void init() {
        adDatabase = new HashSet<String>();
    }

    private static void printSetToConsole(){
        for (Iterator<String> iterator = adDatabase.iterator(); iterator.hasNext(); ) {
            String next = iterator.next();
            System.out.println(next);
        }

    }


}
