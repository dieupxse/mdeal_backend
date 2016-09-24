package com.ftl.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class SmartZip
{
  public static final int BUFFER_SIZE = 131072;
  
  public static void zip(String[] fileNames, String zipFilePath, boolean includePath)
    throws IOException
  {
    byte[] buf = new byte[131072];
    
    ZipOutputStream os = null;
    try
    {
      os = new ZipOutputStream(new FileOutputStream(zipFilePath));
      for (String fileName : fileNames)
      {
        File file = new File(fileName);
        FileInputStream is = null;
        try
        {
          is = new FileInputStream(file);
          if (!includePath) {
            os.putNextEntry(new ZipEntry(file.getName()));
          } else {
            os.putNextEntry(new ZipEntry(fileName));
          }
          try
          {
            int len;
            while ((len = is.read(buf)) > 0) {
              os.write(buf, 0, len);
            }
          }
          finally
          {
            os.closeEntry();
          }
        }
        finally {}
      }
    }
    finally
    {
      StreamUtil.safeClose(os);
    }
  }
  
  public static void zip(String sourceFileList, String zipFilePath, boolean includePath)
    throws IOException
  {
    String[] fileNames = StringUtil.toStringArray(sourceFileList);
    zip(fileNames, zipFilePath, includePath);
  }
  
  public static List unzip(String zipFilePath, String outputFolder)
    throws IOException, ClassCastException
  {
    if (!outputFolder.substring(outputFolder.length() - 1).equals("/")) {
      outputFolder = outputFolder + "/";
    }
    List result = new ArrayList();
    ZipInputStream is = null;
    ZipFile zf = null;
    try
    {
      is = new ZipInputStream(new FileInputStream(zipFilePath));
      zf = new ZipFile(zipFilePath);
      
      Enumeration entries = zf.entries();
      while (entries.hasMoreElements())
      {
        ZipEntry ze = (ZipEntry)entries.nextElement();
        if (ze.isDirectory())
        {
          is.getNextEntry();
          File dirCreate = new File(outputFolder + ze.getName());
          if (!dirCreate.exists()) {
            if (!dirCreate.mkdirs()) {
              throw new IOException("Could not create directory " + dirCreate.getAbsolutePath());
            }
          }
        }
        else
        {
          String outFilename = ze.getName();
          result.add(outFilename);
          
          OutputStream os = null;
          try
          {
            outFilename = outputFolder + outFilename;
            os = new FileOutputStream(outFilename);
            is.getNextEntry();
            
            byte[] buf = new byte[131072];
            int len;
            while ((len = is.read(buf)) > 0) {
              os.write(buf, 0, len);
            }
          }
          finally
          {
            StreamUtil.safeClose(os);
          }
        }
      }
    }
    finally
    {
      try
      {
        if (zf != null) {
          zf.close();
        }
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
      StreamUtil.safeClose(is);
    }
    return result;
  }
  
  public static void unzipOneFile(String source, String destination)
    throws IOException
  {
    ZipInputStream is = null;
    ZipFile zf = null;
    try
    {
      is = new ZipInputStream(new FileInputStream(source));
      zf = new ZipFile(source);
      
      Enumeration entries = zf.entries();
      while (entries.hasMoreElements())
      {
        ZipEntry ze = (ZipEntry)entries.nextElement();
        if (ze.isDirectory())
        {
          is.getNextEntry();
        }
        else
        {
          String outFilename = ze.getName();
          
          OutputStream os = null;
          try
          {
            os = new FileOutputStream(destination);
            is.getNextEntry();
            
            byte[] buf = new byte[131072];
            int len;
            while ((len = is.read(buf)) > 0) {
              os.write(buf, 0, len);
            }
          }
          finally
          {
            StreamUtil.safeClose(os);
          }
          return;
        }
      }
    }
    finally
    {
      try
      {
        if (zf != null) {
          zf.close();
        }
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
      StreamUtil.safeClose(is);
    }
    throw new IOException("No entry to extract");
  }
  
  public static void gzip(String source, String destination)
    throws IOException
  {
    byte[] buf = new byte[131072];
    FileInputStream is = null;
    FileOutputStream os = null;
    GZIPOutputStream gos = null;
    try
    {
      is = new FileInputStream(source);
      os = new FileOutputStream(destination);
      gos = new GZIPOutputStream(os);
      int bytesRead;
      while ((bytesRead = is.read(buf)) >= 0) {
        gos.write(buf, 0, bytesRead);
      }
      gos.finish();
    }
    finally
    {
      StreamUtil.safeClose(gos);
      StreamUtil.safeClose(os);
      StreamUtil.safeClose(is);
    }
  }
  
  public static void gunzip(String source, String destination)
    throws IOException
  {
    byte[] buf = new byte[131072];
    FileInputStream is = null;
    GZIPInputStream gis = null;
    FileOutputStream os = null;
    try
    {
      is = new FileInputStream(source);
      os = new FileOutputStream(destination);
      gis = new GZIPInputStream(is);
      int bytesRead;
      while ((bytesRead = gis.read(buf)) >= 0) {
        os.write(buf, 0, bytesRead);
      }
    }
    finally
    {
      StreamUtil.safeClose(gis);
      StreamUtil.safeClose(os);
      StreamUtil.safeClose(is);
    }
  }
}
