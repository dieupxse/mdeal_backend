/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ctnet.entity;

import java.math.BigInteger;
import java.util.Date;

/**
 *
 * @author jacob
 */

public class SmsMt  {
    
    private Long id;
    private BigInteger smsID;
    
    private String sender;
    
    private String reciever;
    
    private String message;
    
    private int messageNum;
    
    private Date sentTime;
    
    private String sentStatus;

    public SmsMt() {
    }

    public SmsMt(Long id) {
        this.id = id;
    }

    public SmsMt(Long id, String sender, String reciever, String message, int messageNum, Date sentTime, String sentStatus) {
        this.id = id;
        this.sender = sender;
        this.reciever = reciever;
        this.message = message;
        this.messageNum = messageNum;
        this.sentTime = sentTime;
        this.sentStatus = sentStatus;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigInteger getSmsID() {
        return smsID;
    }

    public void setSmsID(BigInteger smsID) {
        this.smsID = smsID;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReciever() {
        return reciever;
    }

    public void setReciever(String reciever) {
        this.reciever = reciever;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getMessageNum() {
        return messageNum;
    }

    public void setMessageNum(int messageNum) {
        this.messageNum = messageNum;
    }

    public Date getSentTime() {
        return sentTime;
    }

    public void setSentTime(Date sentTime) {
        this.sentTime = sentTime;
    }

    public String getSentStatus() {
        return sentStatus;
    }

    public void setSentStatus(String sentStatus) {
        this.sentStatus = sentStatus;
    }

   
}
