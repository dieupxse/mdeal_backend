/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smsmdealpromo;

import org.jsmpp.ctnet.RewriteSentMessage;

/**
 *
 * @author dieup
 */
public class SmsMdealPromo {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        RewriteSentMessage send = new RewriteSentMessage();
        send.start();
    }
    
}
