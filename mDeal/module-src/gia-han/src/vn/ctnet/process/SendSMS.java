/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ctnet.process;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Date;
import java.util.Properties;
import java.util.Random;
import vn.ctnet.dao.SmsMtDAO;
import vn.ctnet.entity.SmsMt;

/**
 *
 * @author jacob
 */
public class SendSMS extends Thread{
    private String msisdn, brandName, message,status;
    public SendSMS(String msisdn, String brandName, String message) {
        this.brandName = brandName;
        this.message = message;
        this.msisdn = msisdn;
        this.status = "PRE";
    }
     public SendSMS(String msisdn, String brandName, String message,String status) {
        this.brandName = brandName;
        this.message = message;
        this.msisdn = msisdn;
        this.status = status;
    }
    @Override
    public void run() {
        SmsMtDAO smsmtCtl = new SmsMtDAO();
        SmsMt smsmt = new SmsMt();
        smsmt.setMessage(message);
        smsmt.setMessageNum(countmessage(message.length()));
        smsmt.setReciever(msisdn);
        smsmt.setSmsID(BigInteger.valueOf(0));
        smsmt.setSentTime(new Date());
        smsmt.setSender(brandName);
        smsmt.setSentStatus(status);
        try {
            smsmtCtl.insert(smsmt);
        } catch (Exception ex) {
            ex.printStackTrace();
        } 
    }
    public int randInt(int min, int max) {

        // NOTE: This will (intentionally) not run as written so that folks
        // copy-pasting have to think about how to initialize their
        // Random instance.  Initialization of the Random instance is outside
        // the main scope of the question, but some decent options are to have
        // a field that is initialized once and then re-used as needed or to
        // use ThreadLocalRandom (if using at least Java 1.7).
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }
    public int countmessage(int message)
        {
            int result = 0;
            if (message <= 160)
            {
                result = 1;
            }
            if ((message > 160) && (message <= 306))
            {
                result = 2;
            }
            if ((message > 306) && (message <= 459))
            {
                result = 3;
            }
            if ((message > 459) && (message <= 612))
            {
                result = 4;
            }
            return result;
        }
    
     public  String getValue(String name) {
        Properties prop = new Properties();
        InputStream input = null;
        try {
            String path="C:\\mdeal_config\\config.properties";
            input = new FileInputStream(new File(path));

            // load a properties file
            prop.load(input);
            return prop.getProperty(name);

        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            return "";
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
