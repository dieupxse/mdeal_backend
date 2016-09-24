/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ctnet.confi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

/**
 *
 * @author jacob
 */
public class Ultility {

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

    public static Date adddays(int day) {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date()); // Now use today date.
        c.add(Calendar.DAY_OF_YEAR, day); // Adding 5 days        
        return c.getTime();

    }

    public static Date addHour(int hour) {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date()); // Now use today date.
        c.add(Calendar.HOUR, hour); // Adding 5 days        
        return c.getTime();

    }

    public static Integer countmessage(int len) {
        int result = 0;
        if (len <= 160) {
            result = 1;
        }
        if ((len > 160) && (len <= 306)) {
            result = 2;
        }
        if ((len > 306) && (len <= 459)) {
            result = 3;
        }
        if ((len > 459) && (len <= 612)) {
            result = 4;
        }
        return result;
    }
    
    public  static String initPhoneNumber(String phone, int type) {
        //using for smpp
        if (type == 1) {
            if (phone.startsWith("0")) {
                return phone.replaceFirst("0", "84");
            } else if (phone.startsWith("84")) {
                return phone;
            } else if (phone.startsWith("+84")) {
                return phone.replace("+", "");
            } else {
                return phone;
            }
        } 
        //using for chargin proxy
        else {
            if (phone.startsWith("0")) {
                return phone.replaceFirst("0", "");
            } else if(phone.startsWith("84")) {
                return phone.replaceFirst("84","");
            } else if(phone.startsWith("+84")) {
                return phone.replace("+84","");
            } else return phone;
        }
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
}
