package com.ftl.util;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

public class SystemUtil
{
  public static int exec(String command, byte[] data, long timeout, OutputStream out, OutputStream err)
    throws Exception
  {
    return exec(null, command, data, timeout, out, err);
  }
  
  public static int exec(File cwd, String command, byte[] data, long timeout, OutputStream out, OutputStream err)
    throws Exception
  {
    final Process process = Runtime.getRuntime().exec(command, null, cwd);
    try
    {
      if ((data != null) && (data.length > 0))
      {
        OutputStream os = process.getOutputStream();
        os.write(data);
        os.flush();
      }
      Thread processRunner = new Thread(new Runnable()
      {
        public void run()
        {
          try
          {
            process.waitFor();
          }
          catch (Exception e)
          {
            e.printStackTrace();
          }
        }
      });
      Thread outputSynchronizer = createStreamSynchronizer(process.getInputStream(), out);
      Thread errorSynchronizer = createStreamSynchronizer(process.getErrorStream(), err);
      outputSynchronizer.start();
      errorSynchronizer.start();
      processRunner.start();
      processRunner.join(timeout);
      if (processRunner.isAlive())
      {
        process.destroy();
        throw new Exception("Execution of command '" + command + "' was timedout after " + timeout + " miliseconds.");
      }
      outputSynchronizer.join();
      errorSynchronizer.join();
      return process.exitValue();
    }
    finally
    {
      StreamUtil.safeClose(process.getOutputStream());
      StreamUtil.safeClose(process.getInputStream());
      StreamUtil.safeClose(process.getErrorStream());
    }
  }
  
  private static Thread createStreamSynchronizer(final InputStream input, final OutputStream output)
  {
    Thread thread = new Thread(new Runnable()
    {
      public void run()
      {
        try
        {
          byte[] buffer = new byte['?'];
          int bytesRead;
          while ((bytesRead = input.read(buffer)) >= 0) {
            output.write(buffer, 0, bytesRead);
          }
        }
        catch (Exception ex) {}
      }
    });
    return thread;
  }
}
