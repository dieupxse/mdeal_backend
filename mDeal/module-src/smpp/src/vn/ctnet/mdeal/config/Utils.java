/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ctnet.mdeal.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

/**
 *
 * @author vanvtse90186
 */
public class Utils {

    public static String getMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger number = new BigInteger(1, messageDigest);
            String hashtext = number.toString(16);
            // Now we need to zero pad it if you actually want the full 32 chars.
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext.toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static Date adddays(int day) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Calendar c = Calendar.getInstance();
        c.setTime(new Date()); // Now use today date.
        c.add(Calendar.DATE, day); // Adding 5 days        
        return c.getTime();

    }
    public static Date addHour(int hour) {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date()); // Now use today date.
        c.add(Calendar.HOUR_OF_DAY, hour); // Adding 5 days        
        return c.getTime();

    }
    
    public static Date addMinute(int min) {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date()); // Now use today date.
        c.add(Calendar.MINUTE, min); // Adding 5 days        
        return c.getTime();

    }
    
     public static int countmessage(int message)
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
      public static String getCategoryId(String pkid,int len) {
           
           String rs = "";
           if(pkid==null || pkid.equals("")) {
               for(int i=1; i<len-1;i++) {
                   rs+='0';
               }
               rs+="1";
               return rs;
           }
           pkid = pkid.toUpperCase();
           pkid = pkid.replace("D", "");
           rs="";
           for(int k = 0; k<len - pkid.length();k++) {
               rs+='0';
           }
           rs+=pkid;
           return rs;
       }
      
      public static String getValue(String name) {

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
       public static String getSms(String name) {

        Properties prop = new Properties();
        InputStream input = null;

        try {
            String path="C:\\mdeal_config\\sms.properties";
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
      public static String getMessageWithSegment(String msg, int seg) {

		String rs = "";
		int partNum = (int)Math.ceil((double)msg.length() / (double)150);
		for (int i = 1; i <= partNum; i++) {
			if (i == seg) {
				rs = msg.substring((seg - 1) * 150,
						(seg * 150 > msg.length() ? msg.length() : seg * 150));
			}
		}
		return rs;
	}
	
	public static int countSegment(int len) {
		
		return (int)Math.ceil((double)len / (double)150);
	}
      
}
