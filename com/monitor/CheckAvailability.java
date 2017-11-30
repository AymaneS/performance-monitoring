package com.monitor;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimerTask;

/**
 * CheckAvailability is the class that will check if the website given is available and store the data received from the request
 */
public class CheckAvailability extends TimerTask {
    private String websiteURL;

    CheckAvailability(String websiteURL) {
        this.websiteURL = websiteURL;
    }

    /** Method that send a http request and get back the
     * @param url the website's url that need to be monitored
     * @return a boolean true/false if the website is available/not available, the response time and the response code
     */
    private static String[] pingURL(String url) {
        url= "http://"+url;

        try {
            //Open the Http Connection, set the Timeout and request method to "Head"
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(1000);
            connection.setReadTimeout(1000);
            connection.setRequestMethod("HEAD");

            //Save the current time, send the request to get the Response Code and then get by substraction get the time taken to send and get the Response Code
            long startTime = System.currentTimeMillis();
            int responseCode = connection.getResponseCode();
            long responseTime = System.currentTimeMillis()-startTime;

            //If the response Code is between 200 and 399, it means the site was available and the request was a success or a redirection
            return new String[]{Boolean.toString(200 <= responseCode && responseCode <= 399), Long.toString(responseTime), Integer.toString(responseCode)};
        } catch (IOException exception) {
            //If there was an error when the HttpURLConnection was opened, we send it as response Code 0 and 0 ms taken to get the response since there was no request
            return new String[]{Boolean.toString(false), Long.toString(0), Integer.toString(0)};
        } catch (Exception ex) {
            ex.printStackTrace();
            return new String[]{Boolean.toString(false), Long.toString(0), Integer.toString(0)};
        }
    }

    /**
     * Each time this thread is scheduled, send a http request and store the data received in three files
     */
    public void run() {
        BufferedWriter outdata = null;
        String newLine = System.getProperty("line.separator");
        String columnSeparator = "|";
        String directoryName = String.format("data/%s", websiteURL);

        try {
            //Create/Write on three files for each type of data : Availability, Response Time and Response Code
            FileWriter datastream = new FileWriter(directoryName+"/Logs_Data", true); //true tells to append data.
            outdata = new BufferedWriter(datastream);

            //Send the request at the website URL specified and get back the data
            String[] requestData = pingURL(websiteURL);

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            //Write each data in its specific file
            outdata.write(String.format("%s%s%s%s%s%s%s%s", dtf.format(now), columnSeparator, requestData[0], columnSeparator, requestData[1], columnSeparator, requestData[2], newLine));

        } catch (IOException e) {
            System.err.println("Error: "+e.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (outdata != null ) {
                try {
                    outdata.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        }
    }
}
