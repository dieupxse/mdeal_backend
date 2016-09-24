/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mdealsmspromo;
import org.jsmpp.ctnet.*;
/**
 *
 * @author dieup
 */
public class MDealSMSPromo {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        //RewriteSentMessage app = new RewriteSentMessage();
        //app.start();
        TestBindConnect t = new TestBindConnect();
        t.start();
        
    }
    
}
