/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package charging;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

/**
 *
 * @author jacob
 */
public class DateProc {
    public static String getYYYYMMDDHHMISS(Timestamp time) {
        SimpleDateFormat format = new SimpleDateFormat("yy/MM/ddHHMISS");
        return format.format(time);
        
    }
}
