/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mdeal.ctnet.vn.entity.vasapi;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author dieup
 */

public class ReturnObj {
    
    private String returncode;
    private String returndesc;
    private Pkg pkg;
    
    @XmlElement(name="returncode")
    public String getReturncode() {
        return returncode;
    }

    public void setReturncode(String returncode) {
        this.returncode = returncode;
    }
    @XmlElement(name="returndesc")
    public String getReturndesc() {
        return returndesc;
    }

    public void setReturndesc(String returndesc) {
        this.returndesc = returndesc;
    }
    @XmlElement(name="package")
    public Pkg getPkg() {
        return pkg;
    }

    public void setPkg(Pkg pkg) {
        this.pkg = pkg;
    }
    
}
