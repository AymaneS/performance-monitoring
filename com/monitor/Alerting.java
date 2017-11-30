package com.monitor;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

/**
 * Alerting check the data saved by CheckAvailability in siteAvailability and Alert if a website isn't available based on the configuration specified
 */
public class Alerting extends TimerTask {
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private boolean isSiteAvailable = true;
    private int interval;
    private int alertThreshold;
    private String directoryName;

    /**
     * @param interval timeframe of data we are monitoring
     * @param directoryName name of the directory where the data is stored
     * @param alertThreshold availability's percentage that define if we consider if a site is down or up
     */
    Alerting(int interval, String directoryName, int alertThreshold) {
        this.interval = interval;
        this.directoryName = directoryName;
        this.alertThreshold = alertThreshold;
    }

    /** Method that define the Alert logic
     * @param isSiteAvailable boolean that defines the status of the website before we run the method i.e. true/false <=> Available/Not Available
     * @param interval        interval timeframe of data we are monitoring
     * @param alertThreshold  availability's percentage that define if we consider if a site is down or up
     * @param directoryName   name of the directory where the data is stored
     * @return a String that tells if the Website is available, down or came back up
     */
    static String alertSystem(boolean isSiteAvailable, int interval, int alertThreshold, String directoryName) {
        try {
            int numberofLines = countLines(directoryName+"/Logs_Data");

            if(numberofLines>=interval) {
                List<String> dataLines = Files.readAllLines(Paths.get(directoryName+"/Logs_Data")).subList(numberofLines-interval, numberofLines);
                List<String> availabilityLines = new ArrayList<String>();
                for (int i =0; i<interval; i++) {
                    availabilityLines.add(dataLines.get(i).split("\\|")[1]);
                }
                // Int that tells us the number of time the request was a success
                int availability = 0;

                //If a line contains true, it means the request was success. Thus, we add 1 to the int availability
                for (int j = 0; j < availabilityLines.size(); j++) {
                    if (availabilityLines.get(j).contains("true")) {
                        availability++;
                    }
                }

                // Check if the percentage of availability is below the threshold
                // If it is, the website is down
                // If it is above the threshold and the website was down, it's back up
                // else, the website is still available
                if (availability * 100 / interval < alertThreshold) {
                    return "Website {"+directoryName.split("/")[1]+"} is down. Availability is "+availability * 100 / interval+"% for the past "+interval+"s";
                } else if (availability * 100 / interval > alertThreshold && !isSiteAvailable) {
                    return "Website {"+directoryName.split("/")[1]+"} is back up. Availability is "+availability * 100 / interval+"% for the past "+interval+"s";
                } else {
                    return "Website is available";
                }
            }

        } catch (IOException e) {
            return e.toString();
        }  catch(Exception ex) {
          ex.printStackTrace();
        }
        return "Not enough line yet";
    }

    /**Count the number of lines in a file
     * @param filename name of the file
     * @return the number of line of a file
     */
    private static int countLines(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        int lines = 0;
        while (reader.readLine() != null) lines++;
        reader.close();
        return lines;
    }

    /** If there was an alert event, store in a Logs_Alert file
     * @param alertMessage   Alert message that is stored in the logs
     * @param directoryName  Name of the directory where the data is stored
     * @param date           Time when the event was written in the log file
     */
    private static void writeAlertLogs(String alertMessage, String directoryName, String date, int interval) throws IOException {
        BufferedWriter outlog = null;
        try {
            FileWriter logstream = new FileWriter(directoryName+"/Logs_Alert", true); //true tells to append data.
            outlog = new BufferedWriter(logstream);
            if(alertMessage.contains("down")){
              outlog.write(date+"|down|"+interval+"\n");
            }else{
              outlog.write(date+"|backup|"+interval+"\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outlog != null) {
                try {
                    outlog.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch(Exception ex) {
                    ex.printStackTrace();
                }

            }
        }
    }


    /**
     * Each time this thread is scheduled and there is an alert, send a message in the console and store the log in Logs_Alert
     */
    public void run() {

        String alertingMessage = alertSystem(isSiteAvailable, interval, alertThreshold, directoryName);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        //If the message received from the Alert method contains back, the website is back up so a red alert is sent
        if (alertingMessage.contains("back")) {
            isSiteAvailable = true;
            System.out.println("-----------------------------");
            System.out.printf("%s%s%s%n", ANSI_GREEN, alertingMessage, ANSI_RESET);
            try {
                writeAlertLogs(alertingMessage, directoryName, dtf.format(now), interval);
            } catch (IOException e) {
                e.printStackTrace();
            } catch(Exception ex) {
              ex.printStackTrace();
            }
        } else if (alertingMessage.contains("down")) {
            //If the message received from the Alert method contains down, the website is down so a green alert is sent
            isSiteAvailable = false;
            System.out.println("-----------------------------");
            System.out.printf("%s%s%s%n", ANSI_RED, alertingMessage, ANSI_RESET);
            try {
                writeAlertLogs(alertingMessage, directoryName, dtf.format(now), interval);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
