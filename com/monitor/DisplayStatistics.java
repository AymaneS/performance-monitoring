package com.monitor;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * DisplayStatistics uses the data made by CheckAvailability and store them in a Logs_Stats file in each Website folder
 */

public class DisplayStatistics extends TimerTask {
    private int interval;
    private String directoryName;

    /**
     * @param interval      the timeframe (in seconds) of data we are displaying, which also is the number of line we are considering
     * @param directoryName the name of the directory where the data is stored
     */
    DisplayStatistics(int interval, String directoryName) {
        this.interval = interval;
        this.directoryName = directoryName;
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

    /**
     * Method that write on a Logs_Stats file the stats we have computed
     * @param statsMessage  The stats we want to save
     * @param directoryName The directory where the logs will be saved
     * @param date          The date of when the log was saved
     */
    private static void writeStatsLogs(String statsMessage, String directoryName, String date) throws IOException {
        BufferedWriter outlog = null;
        try {
            FileWriter logstream = new FileWriter(directoryName+"/Logs_Stats", true); //true tells to append data.
            outlog = new BufferedWriter(logstream);
            outlog.write(date+"|"+statsMessage+"\n");
        } catch (IOException e) {
            e.printStackTrace();
        }  catch(Exception ex) {
            ex.printStackTrace();
        } finally {
            if (outlog != null) {
                try {
                    outlog.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }  catch(Exception ex) {
                    ex.printStackTrace();
                }

            }
        }
    }

    /**
     * Each time this thread is scheduled, compute and save the Stats in a Logs_files
     */
    public void run() {
        try {

            int numberofLines = countLines(directoryName+"/Logs_Data");
            if(numberofLines>=interval){
                int sumresponsetime = 0;
                int maxresponsetime = Integer.MIN_VALUE;
                int minresponsetime = Integer.MAX_VALUE;

                //Open the three files were the data we want is stored and put only the lines we want in three lists
                List<String> dataLines = Files.readAllLines(Paths.get(directoryName+"/Logs_Data")).subList(numberofLines-interval, numberofLines);
                List<String[]> availabilityLines = new ArrayList<String[]>();

                for (String dataLine : dataLines) {
                    String[] data = {dataLine.split("\\|")[1],dataLine.split("\\|")[2],dataLine.split("\\|")[3]};
                    availabilityLines.add(data);
                }
                // To display the response code received, we are using a HashMap that takes the response code as a key and the number of times it was received as value
                HashMap<String, Integer> responseCodeMap = new HashMap<String, Integer>();

                // Int that tells us the number of time the request was a success
                int availability = 0;

                //The three data files contains the same number of lines, we thus go through them at the same time to compute our stats
                for (int j = 0; j < availabilityLines.size(); j++) {
                    String availabilityBoolean = availabilityLines.get(j)[0];
                    int responseTime = Integer.parseInt(availabilityLines.get(j)[1]);
                    String responseCode = availabilityLines.get(j)[2];
                    if (!responseCodeMap.containsKey(responseCode)) {
                        //if the HashMap doesn't contain the response code, we add it to the HashMap
                        responseCodeMap.put(responseCode, 1);
                    } else {
                        //if the response code is already in the HashMap, add 1 to the number of times the response code was received
                        responseCodeMap.put(responseCode, responseCodeMap.get(responseCode)+1);
                    }

                    if (responseTime > maxresponsetime) {
                        //Save the max response time we had in the timeframe specified
                        maxresponsetime = responseTime;
                    }
                    if (responseTime < minresponsetime) {
                        //Save the max response time we had in the timeframe specified
                        minresponsetime = responseTime;
                    }

                    //Sum the response time of every request to later compute the average response time
                    sumresponsetime += responseTime;

                    //If a line contains true, it means the request was success. Thus, we add 1 to the int availability
                    if (availabilityBoolean.contains("true")) {
                        availability++;
                    }
                }

                //We go through the Hashmap made and take the stats stored in a String array
                String[] responseCodeArray = new String[responseCodeMap.size()];
                int pointer = 0;
                Set<Map.Entry<String, Integer>> set = responseCodeMap.entrySet();
                for (Map.Entry<String, Integer> aSet : set) {
                    Map.Entry<String, Integer> mentry = aSet;
                    responseCodeArray[pointer] = mentry.getKey()+":"+mentry.getValue();
                    pointer++;
                }

                //Formate the stats we have computed for a easier log reading experience
                String responsetimeMessage = "Website {"+directoryName.split("/")[1]+"} Average response time is "+Integer.toString(sumresponsetime / interval)+"ms. Max response time is "+Integer.toString(maxresponsetime)+"ms";
                String availabilityMessage = "Website {"+directoryName.split("/")[1]+"} was available "+Integer.toString(availability * 100 / interval)+"% of the time for the past "+Integer.toString(interval)+"s";
                String responseCodeMessage = String.join("#", responseCodeArray);

                //Get the current time to add it to the log
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                LocalDateTime now = LocalDateTime.now();
                try {
                    //Store the stats in a Logs_Stats file
                    writeStatsLogs(Integer.toString(interval)+"|"+Integer.toString(availability * 100 / interval)+"|"+Integer.toString(minresponsetime)+"|"+Integer.toString(maxresponsetime)+"|"+Integer.toString(sumresponsetime / interval)+"|"+responseCodeMessage, directoryName, dtf.format(now));
                } catch (IOException e) {
                    e.printStackTrace();
                  }  catch(Exception ex) {
                    ex.printStackTrace();
                  }
            }

        } catch (IOException e) {
            e.printStackTrace();
          }  catch(Exception ex) {
            ex.printStackTrace();
          }

    }
}
