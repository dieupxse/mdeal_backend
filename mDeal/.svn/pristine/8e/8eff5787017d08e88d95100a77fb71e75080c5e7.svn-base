/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author jacob
 */
public class Test {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        Test t= new Test();
        Date d = new Date();
        Date cp = t.addHour(12);
        SimpleDateFormat fm = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        System.out.println(fm.format(d));
        System.out.println(d.before(cp));
        System.out.println(fm.format(cp));
        System.out.println(d.compareTo(cp));
    }
    
     public  Date adddays(int day) {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date()); // Now use today date.
        c.add(Calendar.DAY_OF_YEAR, day); // Adding 5 days        
        return c.getTime();

    }

    public  Date addHour(int hour) {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date()); // Now use today date.
        c.add(Calendar.HOUR_OF_DAY, hour); // Adding 5 days        
        return c.getTime();

    }

}
