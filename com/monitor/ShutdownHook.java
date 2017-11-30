package com.monitor;

public class ShutdownHook extends Thread {

    @Override
    public void run() {
        System.out.println("=== Shutdown Activated");
        PerformanceMonitoring.timer.cancel();
        System.out.println("---- Monitoring Stopped ----");
    }

}