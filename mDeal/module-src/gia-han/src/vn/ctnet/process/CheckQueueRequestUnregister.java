/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ctnet.process;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import vn.ctnet.entity.QueueRequest;
import vn.ctnet.dao.QueueRequestDAO;
/**
 *
 * @author dieup
 */
public class CheckQueueRequestUnregister extends Thread{
    //time_to_check_queue_unregister
    private final QueueRequestDAO QueueDao = new QueueRequestDAO();
    @Override
    public void run() 
    {
        while(true){
        int timeToCheck = Integer.parseInt(vn.ctnet.confi.Ultility.getValue("time_to_check_queue_unregister"));
        try 
        {
            ArrayList<QueueRequest> qr = QueueDao.GetAll(0);
            if(qr!=null && qr.size()>0) 
            {
                for(QueueRequest q: qr) 
                {
                    Timestamp currentTime = new Timestamp(new Date().getTime());
                    if(q!=null && currentTime.after(q.getExpDate())) 
                    {
                        q.setStatus(true);
                        QueueDao.update(q);
                        System.out.println("Update Queue");
                    }
                }
            }
        } catch (Exception e) 
        {
            System.out.println(e.getMessage() + e.getStackTrace());
        }
            try {
                Thread.sleep(1000*timeToCheck);
            } catch (InterruptedException ex) {
                Logger.getLogger(CheckQueueRequestUnregister.class.getName()).log(Level.SEVERE, null, ex);
            }
       }
    }
}
