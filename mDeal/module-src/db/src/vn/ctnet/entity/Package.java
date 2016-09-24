/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ctnet.entity;


public class Package  {
    private String packageID;
   
    private String packageName;
   
    private double price;
   
    private int numOfDate;
   
    private String description;

    public Package() {
    }

    public Package(String packageID) {
        this.packageID = packageID;
    }

    public Package(String packageID, String packageName, double price, int numOfDate) {
        this.packageID = packageID;
        this.packageName = packageName;
        this.price = price;
        this.numOfDate = numOfDate;
    }

    public String getPackageID() {
        return packageID;
    }

    public void setPackageID(String packageID) {
        this.packageID = packageID;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getNumOfDate() {
        return numOfDate;
    }

    public void setNumOfDate(int numOfDate) {
        this.numOfDate = numOfDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

   
    
}
