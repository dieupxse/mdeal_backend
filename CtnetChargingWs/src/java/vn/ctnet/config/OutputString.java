/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ctnet.config;

import vn.ctnet.entity.Reply;

/**
 *
 * @author jacob
 */
public class OutputString {
    public Reply register(String errorCode, String message) {

        Reply rp = new Reply();
        rp.setErrorId(errorCode);
        rp.setErrorDesc(message);
        return rp;
    }

    public Reply unregister(String errorCode, String message) {
        Reply rp = new Reply();

        rp.setErrorId(errorCode);
        rp.setErrorDesc(message);
        return rp;
    }

    public Reply checkstatus(Reply service) {

        return service;

    }
    public Reply GetListPackage(Reply service) {

        return service;

    }
}
