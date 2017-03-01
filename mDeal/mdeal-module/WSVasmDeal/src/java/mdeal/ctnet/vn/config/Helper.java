/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mdeal.ctnet.vn.config;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 *
 * @author dieup
 */
public class Helper {
    public static String initPhoneNumber(String phone, int type) {
        //using for smpp
        switch (type) {
        //using for chargin proxy
            case 1:
                if (phone.startsWith("0")) {
                    return phone.replaceFirst("0", "84");
                } else if (phone.startsWith("84")) {
                    return phone;
                } else if (phone.startsWith("+84")) {
                    return phone.replace("+", "");
                } else {
                    return phone;
                }
            case 2:
                if (phone.startsWith("0")) {
                    return phone.replaceFirst("0", "");
                } else if (phone.startsWith("84")) {
                    return phone.replaceFirst("84", "");
                } else if (phone.startsWith("+84")) {
                    return phone.replace("+84", "");
                } else {
                    return phone;
                }
            default:
                if (phone.startsWith("0")) {
                    return phone.replaceFirst("0", "84");
                } else if (phone.startsWith("84")) {
                    return phone;
                } else if (phone.startsWith("+84")) {
                    return phone.replace("+", "");
                } else {
                    return "84"+phone;
                }
        }
    }
    
    public static String FormatDate(Date date) {
        SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");//dd/MM/yyyy
        String strDate = sdfDate.format(date);
        return strDate;
    }
    
    
    
}
