/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ctnet.ui;

import vn.ctnet.process.CheckDebit;
import vn.ctnet.process.CheckQueueRequestUnregister;
import vn.ctnet.process.RenewService;
import vn.ctnet.process.RetryRenewService;
import vn.ctnet.process.WarningExpD1;
import vn.ctnet.process.WarningExpD30;
import vn.ctnet.process.WarningExpD7;
import vn.ctnet.process.WarningExpFreeAccount;

/**
 *
 * @author jacob
 */
public class App {

    public static void main(String[] arg) {
        //thread kiem tra trang thai het han cua thue bao
        vn.ctnet.process.CheckExpAccount pro = new vn.ctnet.process.CheckExpAccount();
        pro.start();
        //thread gia han goi cuoc cho thue bao
        vn.ctnet.process.RenewService renew = new RenewService();
        renew.start();
        //thread retry gia han
        vn.ctnet.process.RetryRenewService retry = new RetryRenewService();
        retry.start();
        //thread thuc hien tru tien
        vn.ctnet.process.CheckDebit debit = new CheckDebit();
        debit.start();
        //canh bao het han goi mien phi
        vn.ctnet.process.WarningExpFreeAccount warning = new WarningExpFreeAccount();
        warning.start();
        //thong bao gia han thanh cong goi D1
        vn.ctnet.process.WarningExpD1 warningD1 = new WarningExpD1();
        warningD1.start();
        //thong bao gia han thanh cong goi D7
        vn.ctnet.process.WarningExpD7 warningD7 = new WarningExpD7();
        warningD7.start();
        //thong bao gia han thanh cong goi D30
        vn.ctnet.process.WarningExpD30 warningD30 = new WarningExpD30();
        warningD30.start();
        //Kiem tra request huy dich vu
        vn.ctnet.process.CheckQueueRequestUnregister qr = new CheckQueueRequestUnregister();
        qr.start();
    }
}
