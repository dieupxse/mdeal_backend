/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mdeal.ctnet.vn.entity.vasapi;
/**
 *
 * @author dieup
 */
public class Return {
   
    public  static ReturnObj Register(String returncode, String returndesc, String packagecode, String cycle, int price, String desc) {
        ReturnObj rs = new ReturnObj();
        rs.setReturncode(returncode);
        rs.setReturndesc(returndesc);
        Pkg pkg = new Pkg();
        if("1".equals(returncode)) {
            pkg.setCycle(cycle.equals("") || cycle.equals("0") ? "1": cycle);
            pkg.setDesc(desc);
            pkg.setPackagecode(packagecode);
            pkg.setPrice(price);
            rs.setPkg(pkg);
            
        }
        
        return rs;
    }
    
    public static ReturnObj CheckUnRegister(String returncode, String returndesc) {
        ReturnObj rs = new ReturnObj();
        rs.setReturncode(returncode);
        rs.setReturndesc(returndesc);
        return rs;
    }
    
     public static ReturnObj CheckStatus(String returncode, String returndesc, String packagecode, String cycle, int price, String desc)  {
        ReturnObj rs = new ReturnObj();
        rs.setReturncode(returncode);
        rs.setReturndesc(returndesc);
        Pkg pkg = new Pkg();
        if("1".equals(returncode)) {
            pkg.setCycle(cycle);
            pkg.setDesc(desc);
            pkg.setPackagecode(packagecode);
            pkg.setPrice(price);
            rs.setPkg(pkg);
        }
        
        return rs;
    }
    
    
}
