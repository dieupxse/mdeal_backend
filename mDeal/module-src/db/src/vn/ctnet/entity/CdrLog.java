/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ctnet.entity;

import java.util.Date;

/**
 *
 * @author dieup
 */
public class CdrLog {
    private Long id;
    private Date chargeDate;
    private String isdn;
    private String bsisdn;
    private String result;
    private boolean isRecharge;
    private String sessionLogin;
    private String sessionRequest;
    private String amount;
    private String contentId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getChargeDate() {
        return chargeDate;
    }

    public void setChargeDate(Date chargeDate) {
        this.chargeDate = chargeDate;
    }

    public String getIsdn() {
        return isdn;
    }

    public void setIsdn(String isdn) {
        this.isdn = isdn;
    }

    public String getBsisdn() {
        return bsisdn;
    }

    public void setBsisdn(String bsisdn) {
        this.bsisdn = bsisdn;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public boolean isIsRecharge() {
        return isRecharge;
    }

    public void setIsRecharge(boolean isRecharge) {
        this.isRecharge = isRecharge;
    }

    public String getSessionLogin() {
        return sessionLogin;
    }

    public void setSessionLogin(String sessionLogin) {
        this.sessionLogin = sessionLogin;
    }

    public String getSessionRequest() {
        return sessionRequest;
    }

    public void setSessionRequest(String sessionRequest) {
        this.sessionRequest = sessionRequest;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public int getRequestAction() {
        return requestAction;
    }

    public void setRequestAction(int requestAction) {
        this.requestAction = requestAction;
    }
    private String categoryId;
    private String serviceCode;
    private int requestAction;
    
}
