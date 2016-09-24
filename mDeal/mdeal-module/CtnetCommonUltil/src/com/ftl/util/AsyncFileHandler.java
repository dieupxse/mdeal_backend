package com.ftl.util;

import java.util.LinkedList;
import java.util.logging.FileHandler;
import java.util.logging.LogRecord;

public class AsyncFileHandler
  extends FileHandler
  implements Runnable
{
  private final Thread thread;
  private boolean stopFlag = false;
  
  public AsyncFileHandler()
    throws Exception
  {
    this.thread = new Thread(this, "AsyncFileHandler");
    this.thread.start();
  }
  
  private final LinkedList<LogRecord> queue = new LinkedList();
  
  public void publish(LogRecord record)
  {
    synchronized (this.queue)
    {
      if (this.queue.size() < 1000)
      {
        this.queue.addLast(record);
        this.queue.notify();
      }
    }
  }
  
  private void doLogging()
    throws Exception
  {
    for (;;)
    {
      LogRecord record;
      synchronized (this.queue)
      {
        record = (LogRecord)this.queue.pollFirst();
      }
      if (record == null) {
        break;
      }
      super.publish(record);
    }
  }
  
  public void close()
    throws SecurityException
  {
    try
    {
      synchronized (this.queue)
      {
        this.stopFlag = true;
        this.queue.notifyAll();
      }
      this.thread.join();
    }
    catch (Exception ex) {}
    super.close();
  }
  
  public void run()
  {
    while (!this.stopFlag) {
      try
      {
        doLogging();
        synchronized (this.queue)
        {
          this.queue.wait();
        }
      }
      catch (Exception ex) {}
    }
  }
}
