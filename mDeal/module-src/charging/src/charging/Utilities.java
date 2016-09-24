/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package charging;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author jacob
 */
public class Utilities {

    public static String CPID = Utilities.getValue("CPID");
    public static String SP_ID = Utilities.getValue("SP_ID");
    public static String CHARGING_PROXY_SHORT_CODE = Utilities.getValue("CHARGING_PROXY_SHORT_CODE");
    public static String SP_SHORT_CODE = Utilities.getValue("SP_SHORT_CODE");
    public static String IP_LOCAL = Utilities.getValue("IP_LOCAL");
    public static String CHARGE_USER = Utilities.getValue("CHARGE_USER");
    public static String CHARGE_PASSWORD = Utilities.getValue("CHARGE_PASSWORD");
    public static int NUM_CHARGE_SESSION = 10;

    public static String formatUserId(String isdn) {
        if (isdn.startsWith("0")) {
            return isdn.replaceFirst("0", "");
        } else if (isdn.startsWith("84")) {
            return isdn.replaceFirst("84", "");
        } else {
            return isdn;
        }
    }

    public static String getValue(String name) {
        Utilities ultil=  new Utilities();
        Properties prop = new Properties();
        InputStream input = null;
        try {
            String path="C:\\mdeal_config\\config.properties";
            input = new FileInputStream(new File(path));
            // load a properties file
            prop.load(input);
            System.out.println("Load config: "+name + " = "+prop.getProperty(name));
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
