/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ctnet.charging.ws;

import charging.Charging;
import java.util.ArrayList;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import vn.ctnet.config.OutputString;
import vn.ctnet.dao.PackageDAO;
import vn.ctnet.entity.Reply;
import vn.ctnet.entity.Service;
import vn.ctnet.mdeal.bo.CustomServiceProcess;
import vn.ctnet.mdeal.bo.ServiceProcess;

/**
 *
 * @author jacob
 */
@WebService(serviceName = "ChargingWs")
public class ChargingWs {
    private final Charging charging = new Charging();
    /**
     * Web service operation
     *
     * @param msisdn
     * @param packageCode
     * @param channel
     * @return
     */
    @WebMethod(operationName = "Register")
    public Reply Register(
            @WebParam(name = "msisdn") String msisdn, 
            @WebParam(name = "packageCode") String packageCode, 
            @WebParam(name = "channel") String channel,
            @WebParam(name = "user") String user) {
        //TODO write your implementation code here:
        OutputString output = new OutputString();
        try {
            ServiceProcess sm = new ServiceProcess();
            String rs = sm.register(initPhoneNumber(msisdn, 3), packageCode, 0,channel,charging,user);
            rs = rs.replace("|", ":");
            String message = rs.replace(":", "|");
            return output.register(rs.split(":")[0], message);
        } catch (Exception e) {
            return output.register("0", "0|DANG_KY_KHONG_THANH_CONG_XIN_VUI_LONG_THU_LAI_SAU");
        } 

    }
    
    @WebMethod(operationName = "RegisterDirect")
    public Reply RegisterDirect(
            @WebParam(name = "msisdn") String msisdn, 
            @WebParam(name = "packageCode") String packageCode, 
            @WebParam(name = "channel") String channel
        )
    {
        //TODO write your implementation code here:
        OutputString output = new OutputString();
        try {
            ServiceProcess sm = new ServiceProcess();
            String rs = sm.register_direct(initPhoneNumber(msisdn, 3), packageCode, 0,channel,charging,"");
            rs = rs.replace("|", ":");
            String message = rs.replace(":", "|");
            return output.register(rs.split(":")[0], message);
        } catch (Exception e) {
            return output.register("0", "0|DANG_KY_KHONG_THANH_CONG_XIN_VUI_LONG_THU_LAI_SAU");
        } 

    }
    @WebMethod(operationName = "RegisterCustom")
    public Reply RegisterCustom(
            @WebParam(name = "msisdn") String msisdn, 
            @WebParam(name = "packageCode") String packageCode, 
            @WebParam(name = "channel") String channel,
            @WebParam(name = "statusMT") String statusMT,
            @WebParam(name = "note") String note) {
        //TODO write your implementation code here:
        OutputString output = new OutputString();
        try {
            CustomServiceProcess sm = new CustomServiceProcess();
            String rs = sm.register_custom(initPhoneNumber(msisdn, 3), packageCode, 0,channel,charging,statusMT,note);
            rs = rs.replace("|", ":");
            String message = rs.replace(":", "|");
            return output.register(rs.split(":")[0], message);
        } catch (Exception e) {
            return output.register("0", "0|DANG_KY_KHONG_THANH_CONG_XIN_VUI_LONG_THU_LAI_SAU");
        } 

    }

    /**
     * Web service operation
     *
     * @param msisdn
     * @param packageCode
     * @param channel
     * @return
     */
    @WebMethod(operationName = "Unregister")
    public Reply Unregister(
            @WebParam(name = "msisdn") String msisdn, 
            @WebParam(name = "packageCode") String packageCode, 
            @WebParam(name = "channel") String channel,
            @WebParam(name = "user") String user) {
        //TODO write your implementation code here:
        OutputString output = new OutputString();
        try {
            //   System.out.println("3");
            ServiceProcess sm = new ServiceProcess();
            String rs = sm.unregister(initPhoneNumber(msisdn, 3), packageCode, 0, channel,user);
//sm.unregister(msisdn, packageCode, 0, "VAS");
            //     System.out.println(rs);
            //     System.out.println("4");
            rs = rs.replace("|", ":");
            String message = rs.replace(":", "|");
            return output.unregister(rs.split(":")[0], message);
        } catch (Exception e) {
            return output.register("0", "0|HUY_KHONG_THANH_CONG_XIN_VUI_LONG_THU_LAI_SAU");
        } 
    }

    /**
     * Web service operation
     *
     * @param msisdn
     * @param username
     * @param password
     * @return
     */
    @WebMethod(operationName = "CheckStatus")
    public Reply CheckStatus(@WebParam(name = "msisdn") String msisdn) {
        //TODO write your implementation code here:
        try {
            
            ServiceProcess sm = new ServiceProcess();

            Service rs = sm.checkProfile(initPhoneNumber(msisdn, 3), 0, "VAS");
            System.out.println("Web service check "+msisdn);
            if (rs != null) {
                System.out.println("get service ok");
                PackageDAO packCtrl = new PackageDAO();
                vn.ctnet.entity.Package pack = packCtrl.getPackageByID(rs.getPackageID());
                vn.ctnet.entity.Subscripton service = new vn.ctnet.entity.Subscripton();
                service.setPackageCode(rs.getPackageID());
                service.setCycle(pack.getNumOfDate());
                service.setEndtime(rs.getExpDate());
                service.setPrice((int)pack.getPrice());

                Reply rp = new Reply();
                rp.setErrorId("1");
                rp.setErrorDesc("1|THUE_BAO_DANG_SU DUNG DICH_VU");
                ArrayList<vn.ctnet.entity.Subscripton> list = new ArrayList<>();
                list.add(service);
                rp.setSubscriptonList(list);
                return rp;
            } else {
                System.out.println("get service not ok");
                Reply rp = new Reply();
                rp.setErrorId("0");
                rp.setErrorDesc("THUE_BAO_CHUA_SU DUNG DICH_VU");
                return rp;
            }

        } catch (Exception e) {
            Reply rp = new Reply();
            rp.setErrorId("0");
            rp.setErrorDesc("0|THUE_BAO_CHUA_SU DUNG DICH_VU");
            return rp;
        } 
    }
    /**
     * Get Profile
     * @param msisdn
     * @return 
     */
    @WebMethod(operationName = "GetProfile")
    public Service GetProfile(@WebParam(name="msisdn")String msisdn) {
        try{
            ServiceProcess sm = new ServiceProcess();
            Service sv = sm.getService(initPhoneNumber(msisdn,3));
            return sv;
        } catch(Exception e) {
            return null;
        }
        
    }
    
    
    public String initPhoneNumber(String phone, int type) {
        //using for smpp
        if (type == 1) {
            if (phone.startsWith("0")) {
                return phone.replaceFirst("0", "84");
            } else if (phone.startsWith("84")) {
                return phone;
            } else if (phone.startsWith("+84")) {
                return phone.replace("+", "");
            } else {
                return phone;
            }
        } //using for chargin proxy
        else if(type==2) {
            if (phone.startsWith("0")) {
                return phone.replaceFirst("0", "");
            } else if (phone.startsWith("84")) {
                return phone.replaceFirst("84", "");
            } else if (phone.startsWith("+84")) {
                return phone.replace("+84", "");
            } else {
                return phone;
            }
        }else{
            return "84"+phone;
        }
    }
}
