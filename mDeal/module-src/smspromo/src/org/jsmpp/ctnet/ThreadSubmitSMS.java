/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jsmpp.ctnet;

import java.util.Date;
import java.util.Random;
import org.jsmpp.bean.OptionalParameter;
import org.jsmpp.bean.OptionalParameters;
import org.jsmpp.session.SMPPSession;

/**
 *
 * @author jacob
 */
public class ThreadSubmitSMS extends Thread {

    SMPPSession session;
    String message;
    String msisdn;
    String brandName;

    /**
     * Hàm dựng
     *
     * @param session
     * @param message
     * @param msisdn
     * @param brandName
     */
    ThreadSubmitSMS(SMPPSession session, String message, String msisdn, String brandName) {
        this.session = session;
        this.message = message;
        this.msisdn = msisdn;
        this.brandName = brandName;
        System.out.println("Send sms...");
        System.out.println("From: " + brandName);
        System.out.println("To: " + msisdn);
        System.out.println("Message: " + message);
    }

    /**
     * Hàm chạy thread gởi tin nhắn
     */
    @Override
    public void run() {
        submitMessage(this.session, this.message, this.msisdn, this.brandName);
    }

    /**
     * Hàm gửi tin nhắn
     *
     * @param session
     * @param message
     * @param msisdn
     * @param brandName
     * @return
     */
    public String submitMessage(SMPPSession session, String message, String msisdn, String brandName) {
        
        final int totalSegments = countSegment(message.length());
        Random random = new Random();
        OptionalParameter sarMsgRefNum = OptionalParameters
                .newSarMsgRefNum((short) random.nextInt());
        OptionalParameter sarTotalSegments = OptionalParameters
                .newSarTotalSegments(totalSegments);

        String messageId = null;
        for (int i = 0; i < totalSegments; i++) {
            final int seqNum = i + 1;
            String mes = getMessageWithSegment(message, seqNum);
            OptionalParameter sarSegmentSeqnum = OptionalParameters
                    .newSarSegmentSeqnum(seqNum);
            messageId = vn.ctnet.mdeal.Process.submitMessage(session, mes,
                    sarMsgRefNum, sarSegmentSeqnum, sarTotalSegments, msisdn, brandName);
        }
        System.out.println();
        return messageId;
    }

    /**
     * Hàm lấy nội dung tin nhắn với segment tương ứng
     *
     * @param msg
     * @param seg
     * @return
     */
    public String getMessageWithSegment(String msg, int seg) {

        String rs = "";
        int partNum = (int) Math.ceil((double) msg.length() / (double) 150);
        for (int i = 1; i <= partNum; i++) {
            if (i == seg) {
                rs = msg.substring((seg - 1) * 150,
                        (seg * 150 > msg.length() ? msg.length() : seg * 150));
            }
        }
        return rs;
    }

    /**
     * Hàm đến segment với chiều dài tin nhắn
     * @param len
     * @return 
     */
    public int countSegment(int len) {

        return (int) Math.ceil((double) len / (double) 150);
    }
}
