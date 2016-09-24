/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mdeal.ctnet.vn.ws;

import charging.Charging;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import mdeal.ctnet.vn.entity.Reply;
import vn.ctnet.mdeal.bo.*;
import mdeal.ctnet.vn.config.OutputString;
import mdeal.ctnet.vn.entity.Info;
import vn.ctnet.dao.*;
import vn.ctnet.entity.Service;
import vn.ctnet.entity.Users;
import vn.ctnet.entity.Package;
import vn.ctnet.mdeal.config.Utils;

/**
 *
 * @author vanvtse90186
 */
@WebService(serviceName = "CtnetVas")
public class CtnetVas {
   Charging charging = new Charging();
    /**
     * Web service operation
     *
     * @param msisdn
     * @param packageCode
     * @param username
     * @param password
     * @return
     */
    @WebMethod(operationName = "Register")
    public Reply Register(@WebParam(name = "msisdn") String msisdn, @WebParam(name = "packageCode") String packageCode, @WebParam(name = "username") String username, @WebParam(name = "password") String password) {
        //TODO write your implementation code here:
        OutputString output = new OutputString();
        try {

            UsersDao userDAO = new UsersDao();
            
            Users user = userDAO.login(username, Utils.getMD5(password));
            if (user == null) {
                return output.register("0", "0|DANG_KY_KHONG_THANH_CONG_XIN_VUI_LONG_THU_LAI_SAU");
            }
 
            SMSProcess sm = new SMSProcess();
            String rs = sm.register(initPhoneNumber(msisdn, 3), packageCode, 0, "VAS",charging);
            
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
     * @param username
     * @param password
     * @return
     */
    @WebMethod(operationName = "Unregister")
    public Reply Unregister(@WebParam(name = "msisdn") String msisdn, @WebParam(name = "packageCode") String packageCode, @WebParam(name = "username") String username, @WebParam(name = "password") String password) {
        //TODO write your implementation code here:
        OutputString output = new OutputString();
        try {

            UsersDao userCtrl = new UsersDao();
            Users user = userCtrl.login(username, Utils.getMD5(password));
        
            if (user == null) {
                return output.unregister("0", "0|HUY_KHONG_THANH_CONG_XIN_VUI_LONG_THU_LAI_SAU");
            }
            //   System.out.println("3");
            SMSProcess sm = new SMSProcess();
            String rs = sm.unregister(initPhoneNumber(msisdn, 3), packageCode, 0, "VAS");
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
    public Reply CheckStatus(@WebParam(name = "msisdn") String msisdn, @WebParam(name = "username") String username, @WebParam(name = "password") String password) {
        //TODO write your implementation code here:
        try {
            UsersDao userCtrl = new UsersDao();
            Users user = userCtrl.login(username, Utils.getMD5(password));
            if (user == null) {
                Reply rp = new Reply();
                rp.setErrorId("0");
                rp.setErrorDesc("0|THUE_BAO_CHUA_SU DUNG DICH_VU");
                return rp;
            }

            SMSProcess sm = new SMSProcess();

            Service rs = sm.checkProfile(initPhoneNumber(msisdn, 3), 0, "VAS");
            System.out.println("Web service check "+msisdn);
            if (rs != null) {
                System.out.println("get service ok");
                PackageDAO packCtrl = new PackageDAO();
                Package pack = packCtrl.getPackageByID(rs.getPackageID());
                mdeal.ctnet.vn.entity.Subscripton service = new mdeal.ctnet.vn.entity.Subscripton();
                service.setPackageCode(rs.getPackageID());
                service.setCycle(pack.getNumOfDate());
                service.setEndtime(rs.getExpDate());
                service.setPrice((int)pack.getPrice());

                Reply rp = new Reply();
                rp.setErrorId("1");
                rp.setErrorDesc("1|THUE_BAO_DANG_SU DUNG DICH_VU");
                ArrayList<mdeal.ctnet.vn.entity.Subscripton> list = new ArrayList<>();
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

        } catch (ClassNotFoundException | SQLException e) {
            Reply rp = new Reply();
            rp.setErrorId("0");
            rp.setErrorDesc("0|THUE_BAO_CHUA_SU DUNG DICH_VU");
            return rp;
        } 
    }

    @WebMethod(operationName = "GetListPackage")
    public Reply GetListPackage(@WebParam(name = "username") String username, @WebParam(name = "password") String password) {
        //TODO write your implementation code here:
        OutputString output = new OutputString();
        try {
            System.out.println("1");
            UsersDao userCtrl = new UsersDao();
            Users user = userCtrl.login(username, Utils.getMD5(password));
            System.out.println("2");
            if (user == null) {
                Reply rp = new Reply();
//                rp.setErrorId("0");
//                rp.setErrorDesc("INVALID_USER");
                return rp;
            }

            PackageDAO packCtrl = new PackageDAO();
            List<Package> list = packCtrl.getListPackge();
            ArrayList<Info> listSub = new ArrayList<>();
            for (vn.ctnet.entity.Package list1 : list) {
                Info pack = new Info();
                pack.setPackagecode(list1.getPackageID());
                pack.setCycle(list1.getNumOfDate());
                pack.setPrice((int)list1.getPrice());
                pack.setDesc(list1.getDescription());

                listSub.add(pack);
            }

            Reply rp = new Reply();

            rp.setServiceList(listSub);
            return rp;
        } catch (ClassNotFoundException | SQLException e) {
            Reply rp = new Reply();
            return rp;
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
