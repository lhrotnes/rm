package no.snd.rm;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This application monitors and sends reports for Riksmedia ads, integrated on Aftenposten
 *
 * Testing branch
 */
public class App {

    private static SortedSet<Integer> adDatabase;
    private static String host = "www.jobbdirekte.no";
    private static String db_file = "";
    private static String backup_dir = "";
    private static String ad_url = "";
    private static String recipients = "";
    private static int port = 80;

    private static Properties properties = new Properties();

    /**
     * Starting point for application
     *
     * @param args 0. database file ad ids, 1. backup dir, 2. url for html page, 3. email recipients (comma separated)
     */
    public static void main(String[] args) {
        //0.1 Load properties
        if (args.length != 1) {
            System.err.println("Wrong argument. Usage: App <propertyFile>");
            System.exit(1);
        } else {
            try {
                properties.load(new FileInputStream(args[0]));
            } catch (IOException e) {
                System.err.println("Wrong argument. Usage: Jobbdirekte <propertyFile>\n" + e.getMessage() );
                System.exit(1);
            }
        }

        //0. initialize the application
        init();

        //1. read existing ids from file into "database"
        readExistingIdsFromFile(adDatabase, db_file);

        //2. read html with ads
        String htmlAds = getHtmlFile(ad_url);

        //3. extract ad ids from html & add to database
        addIdsToDatabase(adDatabase, htmlAds);

        //4. store ids from database to file on disk
        writeDatabaseToFile(adDatabase, null, db_file);

        //5. if today is friday, send report with ids to email recipients and store the report in backup directory, clean and delete current database and file
        sendReport(adDatabase, recipients, backup_dir,db_file, true);

        //debug
        printSetToConsole();
    }

    private static void sendReport(SortedSet<Integer> adDatabase, String recipients, String backupDir, String dbFile, boolean sendNow) {

        Date dNow = new Date( );
        SimpleDateFormat ft = new SimpleDateFormat ("yyyyMMdd");

        //create Calendar instance
        Calendar now = Calendar.getInstance();
        //If friday, send report
        if(6 == now.get(Calendar.DAY_OF_WEEK) || sendNow ){
            //send email
            // Sender's email ID needs to be mentioned
            String from = "generator@medianorge.no";
            // Assuming you are sending email from localhost
            String host = "postmann.aftenposten.no";
            // Get system properties
            Properties properties = System.getProperties();
            // Setup mail server
            properties.setProperty("mail.smtp.host", host);
            // Get the default Session object.
            Session session = Session.getDefaultInstance(properties);

            try{
                // Create a default MimeMessage object.
                MimeMessage message = new MimeMessage(session);

                // Set From: header field of the header.
                message.setFrom(new InternetAddress(from));

                String[] addresses = recipients.split(";");
                InternetAddress[] internetAddresses = new InternetAddress[addresses.length];
                for (int i = 0; i < addresses.length; i++) {
                    String address = addresses[i];
                    internetAddresses[i] = new InternetAddress(address);
                }

                // Set To: header field of the header.
                message.addRecipients(Message.RecipientType.TO,
                        internetAddresses);

                // Set Subject: header field
                message.setSubject("Riksmedia1 " + ft.format(dNow));

                // Now set the actual message
                message.setText("The following ad ids have been published since last report:\n" + adsDataBaseToString());

                // Send message
                Transport.send(message);
                System.out.println("Sent message successfully....");
            }catch (MessagingException mex) {
                mex.printStackTrace();
            }

            //write file to backup dir
            writeDatabaseToFile(adDatabase, backupDir,ft.format(dNow) +"_backupreport.txt");
            //clean database
            adDatabase.clear();
            writeDatabaseToFile(adDatabase,null,dbFile);
        }
    }

    private static void addIdsToDatabase(SortedSet<Integer> adDatabase, String htmlAds) {

        Pattern pattern = Pattern.compile("jobid=(\\d+)");
        Matcher matcher = pattern.matcher(htmlAds);

        while (matcher.find()) {
            //System.out.println(matcher.group(1));
            adDatabase.add(new Integer(matcher.group(1)));
        }
    }

    private static String getHtmlFile(String stringUrl) {
        StringBuffer buffer = new StringBuffer();
        try {
            URL url;
            URLConnection urlConn;
            DataInputStream dis;

            url = new URL(stringUrl);

            urlConn = url.openConnection();
            urlConn.setDoInput(true);
            urlConn.setUseCaches(false);

            dis = new DataInputStream(urlConn.getInputStream());
            String s;


            while ((s = dis.readLine()) != null) {
                buffer.append(s);
            }
            dis.close();
        } catch (IOException mue) {
            System.out.printf("Error" + mue.getMessage());
        }

        return buffer.toString();
    }


    public static void readExistingIdsFromFile(SortedSet<Integer> set, String filePath) {
        try {
            //Try read file, if not exists, create the file
            File file = new File(filePath);
            if (!file.exists()) {
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
                set.add(new Integer(strLine));
            }
            //Close the input stream
            in.close();

        } catch (Exception e) {//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void writeDatabaseToFile(SortedSet<Integer> adDatabase, String dir, String filePath) {
        try {
            File bac_dir = null;
            if(dir != null ) {
                bac_dir = new File(dir);
                bac_dir.mkdir();
            }

            File file = new File(bac_dir, filePath);

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            for (Iterator<Integer> iterator = adDatabase.iterator(); iterator.hasNext(); ) {
                Integer next = iterator.next();
                bw.write(next + "\n");
            }

            bw.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }


    private static void init() {
        //Set properties
        host = properties.getProperty("host");
        port = new Integer(properties.getProperty("port", "80")).intValue();
        db_file = properties.getProperty("db_file");
        backup_dir = properties.getProperty("backup_dir");
        ad_url = properties.getProperty("ad_url");
        recipients = properties.getProperty("recipients");

        adDatabase = new TreeSet<Integer>();
    }

    private static void printSetToConsole() {
        for (Iterator<Integer> iterator = adDatabase.iterator(); iterator.hasNext(); ) {
            Integer next = iterator.next();
            System.out.println(next);
        }
    }

    private static String adsDataBaseToString() {
        StringBuffer buffer = new StringBuffer();
        for (Iterator<Integer> iterator = adDatabase.iterator(); iterator.hasNext(); ) {
            Integer next = iterator.next();
            buffer.append(next).append("\n");
        }
        return buffer.toString();
    }


}
