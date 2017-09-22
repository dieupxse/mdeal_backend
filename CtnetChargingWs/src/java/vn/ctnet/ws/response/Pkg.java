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
public class Pkg {
    private String packageCode;
    private int cycle;
    private int price;
    private String desc;

    public Pkg() {
    }
    
    public Pkg(String packageCode, int cycle, int price, String desc) {
        this.packageCode = packageCode;
        this.cycle = cycle;
        this.price = price;
        this.desc = desc;
    }
    @XmlElement(name="desc")
    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
    
    @XmlElement(name="packagecode")
    public String getPackagecode() {
        return packageCode;
    }

    public void setPackagecode(String packageCode) {
        this.packageCode = packageCode;
    }
    @XmlElement(name="cycle")
    public int getCycle() {
        return cycle;
    }

    public void setCycle(int cycle) {
        this.cycle = cycle;
    }
    @XmlElement(name="price")
    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
    
}
