/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ctnet.mdeal.bo;

import java.math.BigInteger;
import java.util.Date;
import vn.ctnet.dao.SmsMtDAO;
import vn.ctnet.entity.SmsMt;

/**
 *
 * @author jacob
 * Class InsertSMS
 * Mô tả: Hàm này có chức năng gởi insert bản tin MT vào CSDL sử dụng thread
 */
public class InsertSMS extends Thread {
    /*
    Varibale
    */
    private String msisdn, brandName, message,status;
    private long smsID;
    
    /*
    Hàm dựng 4 tham số
    params:
    - msisdn: Số điện thoại
    - brandName: gởi từ brandName nào 9193/mDeal
    - message: nội dung tin nhắn
    - smsId: moId nếu có
    */
    public InsertSMS(String msisdn, String brandName, String message, long smsID) {
        this.brandName = brandName;
        this.message = message;
        this.msisdn = msisdn;
        this.smsID = smsID;
        this.status = "PRE";
    }
    
    /*
    Hàm dựng 5 tham số
    params:
    - msisdn: Số điện thoại
    - brandName: gởi từ brandName nào 9193/mDeal
    - message: nội dung tin nhắn
    - smsId: moId nếu có
    - status: trạng thái bản tin MT PRE/SENT
    */
    public InsertSMS(String msisdn, String brandName, String message, long smsID,String status) {
        this.brandName = brandName;
        this.message = message;
        this.msisdn = msisdn;
        this.smsID = smsID;
        this.status = status;
    }
    
    /*
    Hàm chạy sử dụng thread
    */
    @Override
    public void run() {
        SmsMtDAO smsmtCtl = new SmsMtDAO();
        SmsMt smsmt = new SmsMt();
        smsmt.setMessage(message);
        smsmt.setMessageNum(countmessage(message.length()));
        smsmt.setReciever(msisdn);
        smsmt.setSmsID(BigInteger.valueOf(smsID));
        smsmt.setSentTime(new Date());
        smsmt.setSender(brandName);
        smsmt.setSentStatus(status);
        try {
            smsmtCtl.insert(smsmt);
        } catch (Exception ex) {
            ex.printStackTrace();
        } 
    }
    
    /*
    Hàm đêm số tin nhắn của nội dung tin nhắn
    */
    public int countmessage(int message)
        {
            int result = 0;
            if (message <= 160)
            {
                result = 1;
            }
            if ((message > 160) && (message <= 306))
            {
                result = 2;
            }
            if ((message > 306) && (message <= 459))
            {
                result = 3;
            }
            if ((message > 459) && (message <= 612))
            {
                result = 4;
            }
            return result;
        }
}
