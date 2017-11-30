package com.monitor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Timer;
import java.util.stream.Collectors;

public class PerformanceMonitoring {
    public static Timer timer;

    /**
     *  PerfomanceMonitoring is gonna run separate threads to execute each monitoring task
     */
    public static void main(String[] args) {
        //Deploy our ShutdownHook to end every thread and stop gracefully our program
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
        System.out.println("---- Monitoring Started ----");

        //Timer is gonna schedule and run every task for each website and with the timeframes specified in config
        timer = new Timer();

        //If it is the first time we run the program/the data was taken out, the data directory is created
        File basedirectory = new File("data");
        basedirectory.mkdir();

        try {
            //Read the config file and setup lists with the websites and timeframes specified
            //If the formatting of the config file is incorrect, the program is gonna stop and throw an error here
            int websitesLine = Files.readAllLines(Paths.get("config")).indexOf("[websites]");
            int statsIntervalLine = Files.readAllLines(Paths.get("config")).indexOf("[stats intervals]");
            int alertingLine = Files.readAllLines(Paths.get("config")).indexOf("[alerting intervals]");
            int alertThresholdLine = Files.readAllLines(Paths.get("config")).indexOf("[alerting threshold]");

            List<String> everyWebsites = Files.readAllLines(Paths.get("config")).subList(websitesLine+1, statsIntervalLine-1);
            List<String> everyStatsIntervals = Files.readAllLines(Paths.get("config")).subList(statsIntervalLine+1, alertingLine-1);
            List<Integer> everyAlertingIntervals = Files.readAllLines(Paths.get("config")).subList(alertingLine+1, alertThresholdLine-1).stream().map(Integer::parseInt).collect(Collectors.toList());
            int alertThreshold = Integer.parseInt(Files.readAllLines(Paths.get("config")).get(alertThresholdLine+1).split("=")[1]);

            for (String everyWebsite : everyWebsites) {
                //Get the directory name from the website url to efficiently store the data and logs
                String directoryName = String.format("data/%s", everyWebsite);
                //If the directory doesn't exist, it is created
                File dir = new File(directoryName);
                dir.mkdir();
                //Start a thread to make a http request to each Website every second and store the data received
                timer.schedule(new CheckAvailability(everyWebsite), 0, 1000);

                for (String everyStatsInterval : everyStatsIntervals) {
                    //For each timeframe specified, start a thread that gather the stats and store them in a Logs_data file
                    timer.schedule(new DisplayStatistics(Integer.parseInt(everyStatsInterval.split(";")[0].split("=")[1]), directoryName), Integer.parseInt(everyStatsInterval.split(";")[1].split("=")[1]) * 1000, Integer.parseInt(everyStatsInterval.split(";")[0].split("=")[1]) * 1000);
                }
                for (Integer everyAlertingInterval : everyAlertingIntervals) {
                    //For each timeframe specified, start a thread that monitor each website specified and alert if one's availability is below the threshold given
                    timer.schedule(new Alerting(everyAlertingInterval, directoryName, alertThreshold), everyAlertingInterval * 1000, everyAlertingInterval * 1000);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }  catch(Exception ex) {
          ex.printStackTrace();
        }
    }
}
