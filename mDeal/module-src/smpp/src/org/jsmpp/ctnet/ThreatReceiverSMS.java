/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jsmpp.ctnet;

import vn.ctnet.mdeal.bo.SMSReciever;
import charging.Charging;
/**
 *
 * @author vanvtse90186
 */
public class ThreatReceiverSMS extends Thread {

    String message;
    String msisdn;
    String brandName;
    Charging charging;
    
    /**
     * Hàm dựng
     * @param message
     * @param msisdn
     * @param brandName 
     */
    ThreatReceiverSMS(String message, String msisdn, String brandName, Charging charging) {
        this.message = message;
        this.msisdn = msisdn;
        this.brandName = brandName;
        this.charging = charging;
        System.out.println("Nhan tin nhan tu: " + msisdn + " - Msg: " + message);
    }
    
    /**
     * Hàm chạy thread gửi tin
     */
    @Override
    public void run() {
        SMSReciever smsReci = new SMSReciever();
        smsReci.recieverSMS(message, msisdn, brandName,charging);
    }
}
