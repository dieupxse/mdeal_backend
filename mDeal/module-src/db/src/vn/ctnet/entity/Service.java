/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ctnet.entity;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Date;

/**
 *
 * @author jacob
 */

public class Service  {
    
    private Long serviceID;
    
    private BigInteger smsmoID;
    
    private String phone;
    
    private String packageID;
    
    private Timestamp regDate;
    
    private Timestamp startDate;
    
    private Timestamp expDate;
    
    private String channel;
    
    private Boolean isSynched;
    
    private String registerChannel;
    
    private String status;
    
    private Boolean autoAdjourn;
    
    private Integer retry;
    
    private Timestamp adjournDate;
    
    private Double remainMoney;
    
    private Integer remainAdjournDate;
    
    private String description;
    
    private Date modifiedDate;
    
    private Integer isPaid;
    
    private Integer isPushMsg;

    private String userModified;
    private String note;

    public String getUserModified() {
        return userModified;
    }

    public void setUserModified(String userModified) {
        this.userModified = userModified;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
    public Service() {
    }

    public Service(Long serviceID) {
        this.serviceID = serviceID;
    }

    public Service(Long serviceID, String phone, String packageID, Timestamp regDate, Timestamp startDate, Timestamp expDate, String status) {
        this.serviceID = serviceID;
        this.phone = phone;
        this.packageID = packageID;
        this.regDate = regDate;
        this.startDate = startDate;
        this.expDate = expDate;
        this.status = status;
    }

    public Long getServiceID() {
        return serviceID;
    }

    public void setServiceID(Long serviceID) {
        this.serviceID = serviceID;
    }

    public BigInteger getSmsmoID() {
        return smsmoID;
    }

    public void setSmsmoID(BigInteger smsmoID) {
        this.smsmoID = smsmoID;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPackageID() {
        return packageID;
    }

    public void setPackageID(String packageID) {
        this.packageID = packageID;
    }

    public Date getRegDate() {
        return regDate;
    }

    public void setRegDate(Timestamp regDate) {
        this.regDate = regDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Timestamp startDate) {
        this.startDate = startDate;
    }

    public Date getExpDate() {
        return expDate;
    }

    public void setExpDate(Timestamp expDate) {
        this.expDate = expDate;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public Boolean getIsSynched() {
        return isSynched;
    }

    public void setIsSynched(Boolean isSynched) {
        this.isSynched = isSynched;
    }

    public String getRegisterChannel() {
        return registerChannel;
    }

    public void setRegisterChannel(String registerChannel) {
        this.registerChannel = registerChannel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getAutoAdjourn() {
        return autoAdjourn;
    }

    public void setAutoAdjourn(Boolean autoAdjourn) {
        this.autoAdjourn = autoAdjourn;
    }

    public Integer getRetry() {
        return retry;
    }

    public void setRetry(Integer retry) {
        this.retry = retry;
    }

    public Date getAdjournDate() {
        return adjournDate;
    }

    public void setAdjournDate(Timestamp adjournDate) {
        this.adjournDate = adjournDate;
    }

    public Double getRemainMoney() {
        return remainMoney;
    }

    public void setRemainMoney(Double remainMoney) {
        this.remainMoney = remainMoney;
    }

    public Integer getRemainAdjournDate() {
        return remainAdjournDate;
    }

    public void setRemainAdjournDate(Integer remainAdjournDate) {
        this.remainAdjournDate = remainAdjournDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public Integer getIsPaid() {
        return isPaid;
    }

    public void setIsPaid(Integer isPaid) {
        this.isPaid = isPaid;
    }

    public Integer getIsPushMsg() {
        return isPushMsg;
    }

    public void setIsPushMsg(Integer isPushMsg) {
        this.isPushMsg = isPushMsg;
    }

   
    
}
