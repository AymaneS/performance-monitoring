INTRO
----------------------------------------------------
This program allows you to monitor the performance and availability of any website.
The intent of this project is to be lightweight and require very little installation.

This program was developed for Mac OS and Linux.
It was tested on Windows and should work minus one feature.

CONFIGURATION
-----------------------------------------------------
A "config" file is provided in the performance-monitoring directory. It is critical that you follow the pre-existing template.
The configuration file is already completed to help you understand the formatting. The empty line between each section is necessary.

The [websites] section consists of the website URLs you want to monitor.
You need to follow the "[websitename].[extension]" formatting, remove the "http://www." part of your url before adding it to "config" or "http://" if your url is an IP adress.

The [stats intervals] define the check intervals of the metrics you compute.
The format is "Interval=[First Integer];DisplayRange=[Second Integer]".
It follows this sentence logic : Every [First Integer] seconds, display the stats for the past [Second Integer] seconds for each website.
As such, each number corresponds to seconds.
You can add several check intervals, each pair in a separate line.

The [alerting intervals] define the check intervals for the alerting system.
The numbers corresponds to seconds.
You can add several check intervals, each in a separate line.

The [alerting threshold] defines the percentage of availability under which we consider a website down.
Only one threshold is allowed.


INSTALLATION
-----------------------------------------------------
    SIMPLE USAGE
------------------------------
Requirements :
- Java

If you do not know if you have java, run in a console: java -version

The output should look like this (with maybe a different version):
java version "1.8.0_144"
Java(TM) SE Runtime Environment (build 1.8.0_144-b01)

If you do not have Java, download the Java Software Development Kit (SDK) from : https://www.oracle.com
If you do not want to test the alerting system or change the java code, you need nothing else and you can skip to RUNNING THE PROGRAM.

    TEST AND MODIFICATION
-------------------------------
If you want to test or change the java code, you need JUnit.
Download the latest version of JUnit jar file from http://www.junit.org.
This project was made using Junit-4.10.jar.
Copy the jar into a JUNIT folder. Set the JUNIT_HOME to point to that JUNIT folder then set the CLASSPATH environment variable to point to the JUNIT jar location.

If you do not know how to set up all of this or have trouble, follow the first 5 steps of this tutorial : http://www.tutorialspoint.com/junit/junit_environment_setup.htm


RUNNING THE PROGRAM
-----------------------------------------------------
To run this monitor program and every subsequent command, you need to have your terminal open in the performance-monitoring directory.
You then need to run :

'java com/monitor/PerformanceMonitoring'

And the monitoring started !

For each website, a folder is created at /data containing 3 files :
- Logs_Data containing the data recovered after the http request in the format : [date of the request]|[availability]|[response time]|[response code]
- Logs_Stats computing some stats with those data over the timeframes specified : Max response time, average response time, response codes received, availability percentage
  in the format : [date]|[interval]|[availability]|[min response time]|[max response time]|[average response time]|[response code 1 : number of time seen]#[response code 1 : number of time seen]
- Logs_Alert containing every alert messages sent, if no alert was made, this file is non-existent. Format is [date]|[statusofalert]|[interval]

Only alerts are displayed on the console (with a corresponding color on MacOS and Linux, not available on Windows), to display the stats you need to open the corresponding Logs_Stats file.

Press CTRL+C to stop the monitoring.

If you have setup JUnit, 'java com/monitor/TestRunner' will make you test the alert logic.

If you changed any java files, you need to run 'javac com/monitor/*.java' then the command you want.


TESTING ALERT LOGIC
-----------------------------------------------------
The test method launch the alerting method onto files that I have written myself in the testdata folder and know the availability percentage of.
The boolean isSiteAvailable defines the availability state of the website when the alerting method was launched.

Since there is only 4 combinations and we know for sure the content of the testfiles and the availability percentage, we can test the alerting logic manually
and confirm it works !


PROJECT DESIGN AND LIMITATIONS
-----------------------------------------------------
As this project was designed to be simple and lightweight, you can't compute efficiently too many websites at once.
This website is geared towards monitoring <10 websites quickly.
If you have several websites, I advise to only make one alert thread.

Remember that the number of thread follows this pattern : [numberofwebsites]x[number of stats intervals line]x[number of alerting intervals].
I recommend to always keep this number under 20 for optimal usage of this program.
In the optimal case, a request is made every second to each website.

3 java files are working separately : - Alerting.java to alert if necessary the user,
                                      - CheckAvailability.java to make request and save the data received,
                                      - DisplayStatistics.java to compute and store some metrics

PerformanceMonitoring.java is scheduling and running every thread needed based on the config file and the 3 precedent java files.
I decided to consider the time to make a HEAD request and get back the response code as the response time of the website.
I also considered a redirection as a success since this mean the server is available.

If the response code is 0, it means either you have no internet connection when the request was made or the server is down. An improvement would be to separate both cases.

If you want to scale this application and use more websites, the scheduling in PerformanceMonitoring need to be improved to lower the time complexity
If you want to connect this application to another system, you need to take the data folder and the files, then store them in a database.
In the case of heavy usage, a method to store somewhere the data folder and remove it from this folder is necessary for storage reasons.

To keep the folder small, you can take out the data folder or any file and new ones will be created.

The Logs files are created to easily allow a developer to take them and add them to any database/compute the statistics he want.
