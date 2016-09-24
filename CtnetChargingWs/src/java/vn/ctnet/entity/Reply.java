/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ctnet.entity;

import java.util.List;

/**
 *
 * @author jacob
 */
public class Reply {
    private String errorId;
    private String errorDesc;
    private List<Info> serviceList;
    private List<Subscripton> subscriptonList;

    public String getErrorId() {
        return errorId;
    }

    public void setErrorId(String errorId) {
        this.errorId = errorId;
    }

    public String getErrorDesc() {
        return errorDesc;
    }

    public void setErrorDesc(String errorDesc) {
        this.errorDesc = errorDesc;
    }

    public List<Info> getServiceList() {
        return serviceList;
    }

    public void setServiceList(List<Info> serviceList) {
        this.serviceList = serviceList;
    }

    public List<Subscripton> getSubscriptonList() {
        return subscriptonList;
    }

    public void setSubscriptonList(List<Subscripton> subscriptonList) {
        this.subscriptonList = subscriptonList;
    }
}
