/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smppsmspromo;

import mdeal.vn.thread.BindAndSendSms;

/**
 *
 * @author dieup
 */
public class SmppSmsPromo {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        BindAndSendSms smsPromo = new BindAndSendSms();
        smsPromo.start();
    }
    
}
