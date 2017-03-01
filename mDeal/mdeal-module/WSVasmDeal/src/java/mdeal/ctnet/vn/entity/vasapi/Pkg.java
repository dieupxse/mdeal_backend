/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mdeal.ctnet.vn.entity.vasapi;

import javax.xml.bind.annotation.XmlElement;

/**
 *
 * @author dieup
 */
public class Pkg {
    
    private String packagecode;
    private String cycle;
    private int price;
    private String desc;
    @XmlElement(name="packagecode")
    public String getPackagecode() {
        return packagecode;
    }

    public void setPackagecode(String packagecode) {
        this.packagecode = packagecode;
    }
    @XmlElement(name="cycle")
    public String getCycle() {
        return cycle;
    }

    public void setCycle(String cycle) {
        this.cycle = cycle;
    }
    @XmlElement(name="price")
    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
    @XmlElement(name="desc")
    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
