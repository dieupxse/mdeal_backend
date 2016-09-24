package com.ftl.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Log
{
  private static final Logger instance;
  private static final ExecutorService loggingService = new ThreadPoolExecutor(0, 1, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue());
  
  static
  {
    LogManager logManager = LogManager.getLogManager();
    Enumeration<String> loggerNames = logManager.getLoggerNames();
    while (loggerNames.hasMoreElements())
    {
      String loggerName = (String)loggerNames.nextElement();
      for (Handler handler : logManager.getLogger(loggerName).getHandlers()) {
        handler.setFormatter(new BriefLogFormatter());
      }
    }
    instance = Logger.getLogger(Log.class.getName());
    
    Logger logger = Logger.getLogger("stdout");
    LoggingOutputStream los = new LoggingOutputStream(logger, StdLevel.STDOUT);
    System.setOut(new PrintStream(los, true));
    logger = Logger.getLogger("stderr");
    los = new LoggingOutputStream(logger, StdLevel.STDERR);
    System.setErr(new PrintStream(los, true));
  }
  
  public static Logger getLogger()
  {
    return Logger.getLogger(Thread.currentThread().getStackTrace()[2].getClassName());
  }
  
  public static Logger getInstance()
  {
    return instance;
  }
  
  public static void entering(String sourceClass, final String sourceMethod)
  {
    loggingService.execute(new Runnable()
    {
      public void run()
      {
//        Log.instance.entering(this.val$sourceClass, sourceMethod);
      }
    });
  }
  
  public static void entering(String sourceClass, final String sourceMethod, final Object param1)
  {
    loggingService.execute(new Runnable()
    {
      public void run()
      {
//        Log.instance.entering(this.val$sourceClass, sourceMethod, param1);
      }
    });
  }
  
  public static void entering(String sourceClass, final String sourceMethod, final Object[] params)
  {
    loggingService.execute(new Runnable()
    {
      public void run()
      {
//        Log.instance.entering(this.val$sourceClass, sourceMethod, params);
      }
    });
  }
  
  public static void exiting(String sourceClass, final String sourceMethod)
  {
    loggingService.execute(new Runnable()
    {
      public void run()
      {
       // Log.instance.exiting(this.val$sourceClass, sourceMethod);
      }
    });
  }
  
  public static void exiting(String sourceClass, final String sourceMethod, final Object result)
  {
    loggingService.execute(new Runnable()
    {
      public void run()
      {
//        Log.instance.exiting(this.val$sourceClass, sourceMethod, result);
      }
    });
  }
  
  public static void throwing(String sourceClass, final String sourceMethod, final Throwable thrown)
  {
    loggingService.execute(new Runnable()
    {
      public void run()
      {
//        Log.instance.throwing(this.val$sourceClass, sourceMethod, thrown);
      }
    });
  }
  
  public static void severe(String msg)
  {
    loggingService.execute(new Runnable()
    {
      public void run()
      {
        //Log.instance.severe(this.msg);
      }
    });
  }
  
  public static void severe(final String msg, Exception ex)
  {
    loggingService.execute(new Runnable()
    {
      public void run()
      {
        
        Log.instance.severe(msg);
      }
    });
  }
  
  public static void error(String msg)
  {
    severe(msg);
  }
  
  public static void error(String msg, Exception ex)
  {
    severe(msg, ex);
  }
  
  public static void warning(final String msg)
  {
    loggingService.execute(new Runnable()
    {
      public void run()
      {
        Log.instance.warning(msg);
      }
    });
  }
  
  public static void warning(final String msg, Exception ex)
  {
    loggingService.execute(new Runnable()
    {
      public void run()
      {
        Log.instance.warning(msg);
      }
    });
  }
  
  public static void warn(String msg)
  {
    warning(msg);
  }
  
  public static void warn(String msg, Exception ex)
  {
    warning(msg, ex);
  }
  
  public static void info(final String msg)
  {
    loggingService.execute(new Runnable()
    {
      public void run()
      {
        Log.instance.info(msg);
      }
    });
  }
  
  public static void fine(final String msg)
  {
    loggingService.execute(new Runnable()
    {
      public void run()
      {
        Log.instance.fine(msg);
      }
    });
  }
  
  public static void debug(String msg)
  {
    fine(msg);
  }
  
  public static void finer(final String msg)
  {
    loggingService.execute(new Runnable()
    {
      public void run()
      {
        Log.instance.finer(msg);
      }
    });
  }
  
  public static void finest(final String msg)
  {
    loggingService.execute(new Runnable()
    {
      public void run()
      {
        Log.instance.finest(msg);
      }
    });
  }
  
  public static class StdLevel
    extends Level
  {
    private StdLevel(String name, int value)
    {
      super(name,value);
    }
    
    public static Level STDOUT = new StdLevel("STDOUT", Level.INFO
      .intValue() + 53);
    public static Level STDERR = new StdLevel("STDERR", Level.INFO
      .intValue() + 54);
    
    protected Object readResolve()
      throws ObjectStreamException
    {
      if (intValue() == STDOUT.intValue()) {
        return STDOUT;
      }
      if (intValue() == STDERR.intValue()) {
        return STDERR;
      }
      throw new InvalidObjectException("Unknown instance :" + this);
    }
  }
  
  public static class LoggingOutputStream
    extends ByteArrayOutputStream
  {
    private String lineSeparator;
    private Logger logger;
    private Level level;
    
    public LoggingOutputStream(Logger logger, Level level)
    {
      this.logger = logger;
      this.level = level;
      this.lineSeparator = System.getProperty("line.separator");
    }
    
    public void flush()
      throws IOException
    {
      synchronized (this)
      {
        super.flush();
        String record = toString();
        super.reset();
        if ((record.length() == 0) || (record.equals(this.lineSeparator))) {
          return;
        }
        this.logger.logp(this.level, "", "", record);
      }
    }
  }
}
