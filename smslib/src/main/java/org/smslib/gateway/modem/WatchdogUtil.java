package org.smslib.gateway.modem;

public class WatchdogUtil {

    private static ISmsWatchdog smsWatchdog;

    public static void setWatchdog(ISmsWatchdog watchdog){
        smsWatchdog = watchdog;
    }

    public static void announceRssiPolled(){
        if(smsWatchdog!=null){
            smsWatchdog.rssiPolled();
        }
    }
}
