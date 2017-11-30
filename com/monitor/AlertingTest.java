package com.monitor;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class AlertingTest {

    @Test
  public void run() {
        //Test that if the website was available and still is, there is no alert to make
        String resultAvailable = Alerting.alertSystem(true,60, 80, "testdata/sampleWebsiteUp");
        assertEquals("Website is available",resultAvailable);

        //Test that if the website wasn't available and now is, there is an alert to notify the site is back up
        String resultIsNowAvailable = Alerting.alertSystem(false,60, 80, "testdata/sampleWebsiteUp");
        assertEquals("Website {sampleWebsiteUp} is back up. Availability is " + 91 + "% for the past "+ 60 + "s",resultIsNowAvailable);

        //Test that if the website was available and now isn't, there is an alert to notify the site is down
        String resultIsNotAvailable = Alerting.alertSystem(true,60, 80, "testdata/sampleWebsiteDown");
        assertEquals("Website {sampleWebsiteDown} is down. Availability is " + 78 + "% for the past "+ 60 + "s",resultIsNotAvailable);

        //Test that if the website wasn't available and now isn't, there is an alert to notify the site is down
        String resultIsStillNotAvailable = Alerting.alertSystem(false,60, 80, "testdata/sampleWebsiteDown");
        assertEquals("Website {sampleWebsiteDown} is down. Availability is " + 78 + "% for the past "+ 60 + "s",resultIsStillNotAvailable);
    }

}
