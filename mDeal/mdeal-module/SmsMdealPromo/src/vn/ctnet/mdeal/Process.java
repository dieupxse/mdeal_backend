/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ctnet.mdeal;

import java.io.IOException;
import java.util.Date;
import org.jsmpp.InvalidResponseException;
import org.jsmpp.PDUException;
import org.jsmpp.bean.Alphabet;
import org.jsmpp.bean.ESMClass;
import org.jsmpp.bean.GeneralDataCoding;
import org.jsmpp.bean.MessageClass;
import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.OptionalParameter;
import org.jsmpp.bean.RegisteredDelivery;
import org.jsmpp.bean.SMSCDeliveryReceipt;
import org.jsmpp.bean.TypeOfNumber;
import org.jsmpp.extra.NegativeResponseException;
import org.jsmpp.extra.ResponseTimeoutException;
import org.jsmpp.session.SMPPSession;
import org.jsmpp.util.AbsoluteTimeFormatter;
import org.jsmpp.util.TimeFormatter;

/**
 *
 * @author jacob
 */
public class Process {

    private static final TimeFormatter timeFormatter = new AbsoluteTimeFormatter();

    public static String submitMessage(SMPPSession session, String message,
            OptionalParameter sarMsgRefNum, OptionalParameter sarSegmentSeqnum,
            OptionalParameter sarTotalSegments, String phone, String sendFrom) {

        String messageId = null;
        try {
            messageId = session.submitShortMessage("CMT",
                    TypeOfNumber.ALPHANUMERIC, NumberingPlanIndicator.UNKNOWN,
                    sendFrom, TypeOfNumber.UNKNOWN,
                    NumberingPlanIndicator.UNKNOWN, phone,
                    new ESMClass(), (byte) 0, (byte) 1, timeFormatter
                    .format(new Date()), null, new RegisteredDelivery(
                            SMSCDeliveryReceipt.DEFAULT), (byte) 0,
                    new GeneralDataCoding(false, true, MessageClass.CLASS1,
                            Alphabet.ALPHA_DEFAULT), (byte) 0, message
                    .getBytes(), sarMsgRefNum, sarSegmentSeqnum,
                    sarTotalSegments);

        } catch (PDUException e) {
            // Invalid PDU parameter
            System.err.println("Invalid PDU parameter");
        } catch (ResponseTimeoutException e) {
            // Response timeout
            System.err.println("Response timeout");
        } catch (InvalidResponseException e) {
            // Invalid response
            System.err.println("Receive invalid respose");
        } catch (NegativeResponseException e) {
            // Receiving negative response (non-zero command_status)
            System.err.println("Receive negative response");
        } catch (IOException e) {
            System.err.println("IO error occur");
        }
        return messageId;
    }
}
