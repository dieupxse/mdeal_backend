/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ctnet.ws.response;

import javax.xml.bind.annotation.XmlElement;

/**
 *
 * @author dieup
 */
public class WsResponse {
    private String returnCode;
    private String returnDes;
    private Pkg pkg;

    public WsResponse() {
    }
    
    public WsResponse(String returnCode, String returnDes) {
        this.returnCode = returnCode;
        this.returnDes = returnDes;
    }
    @XmlElement(name="returncode")
    public String getReturncode() {
        return returnCode;
    }

    public void setReturncode(String returnCode) {
        this.returnCode = returnCode;
    }
    @XmlElement(name="returndesc")
    public String getReturndes() {
        return returnDes;
    }

    public void setReturndes(String returnDes) {
        this.returnDes = returnDes;
    }
    @XmlElement(name="package")
    public Pkg getPkg() {
        return pkg;
    }

    public void setPkg(Pkg pkg) {
        this.pkg = pkg;
    }
    
    
}
