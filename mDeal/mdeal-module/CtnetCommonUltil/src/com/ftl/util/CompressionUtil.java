package com.ftl.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class CompressionUtil
{
  public static void updateZipFile(String filePath, String entryPath, InputStream dataStream)
    throws Exception
  {
    updateZipFile(filePath, filePath, entryPath, dataStream);
  }
  
  public static void updateZipFile(String sourcePath, String destinationPath, String entryPath, InputStream dataStream)
    throws Exception
  {
    File sourceFile = new File(sourcePath);
    File destinationFile = new File(destinationPath);
    if (!sourceFile.getAbsolutePath().equals(destinationFile.getAbsolutePath()))
    {
      if (!sourceFile.exists()) {
        throw new Exception("Update zip file from '" + sourcePath + "' to '" + destinationPath + "' failed, source file does not exists");
      }
      if (destinationFile.exists()) {
        if (!destinationFile.delete()) {
          throw new Exception("Update zip file from '" + sourcePath + "' to '" + destinationPath + "' failed, destination file exists & unable to delete it");
        }
      }
      InputStream is = null;
      OutputStream os = null;
      try
      {
        is = new FileInputStream(sourceFile);
        os = new FileOutputStream(destinationFile);
        updateZipFile(is, os, entryPath, dataStream);
      }
      finally
      {
        StreamUtil.safeClose(is);
        StreamUtil.safeClose(os);
      }
      Object destinationStream = null;
      try
      {
        destinationStream = new FileOutputStream(destinationPath);
      }
      finally
      {
        StreamUtil.safeClose((OutputStream)destinationStream);
      }
    }
    else
    {
      if (!sourceFile.exists()) {
        throw new Exception("Update zip file '" + sourcePath + "' failed, file does not exists");
      }
      File destinationTempFile = new File(destinationPath + ".tmp");
      if (destinationTempFile.exists()) {
        throw new Exception("Update zip file '" + sourcePath + " failed, temp file '" + destinationTempFile.getPath() + "' already exists");
      }
      InputStream is = null;
      Object os = null;
      try
      {
        is = new FileInputStream(sourceFile);
        os = new FileOutputStream(destinationTempFile);
        updateZipFile(is, (OutputStream)os, entryPath, dataStream);
      }
      finally
      {
        StreamUtil.safeClose(is);
        StreamUtil.safeClose((OutputStream)os);
      }
      Object destinationStream = null;
      try
      {
        destinationStream = new FileOutputStream(destinationPath);
      }
      finally
      {
        StreamUtil.safeClose((OutputStream)destinationStream);
      }
      if (!destinationFile.delete()) {
        throw new Exception("Update zip file '" + sourcePath + " failed, can not delete file '" + destinationFile.getPath() + "'");
      }
      if (!destinationTempFile.renameTo(destinationFile)) {
        throw new Exception("Update zip file '" + sourcePath + " failed, can not rename file '" + destinationTempFile.getPath() + "' to '" + destinationFile.getPath() + "'");
      }
    }
  }
  
  public static void updateZipFile(URL sourceUrl, String destinationPath, String entryPath, InputStream dataStream)
    throws Exception
  {
    if (sourceUrl == null) {
      throw new Exception("Can not update zip file, url is null");
    }
    File destinationFile = new File(destinationPath);
    if (destinationFile.exists()) {
      if (!destinationFile.delete()) {
        throw new Exception("Update zip file from '" + sourceUrl.toString() + "' to '" + destinationPath + "' failed, destination file exists & unable to delete it");
      }
    }
    OutputStream destinationStream = null;
    try
    {
      destinationStream = new FileOutputStream(destinationFile);
      updateZipFile(sourceUrl, destinationStream, entryPath, dataStream);
    }
    finally
    {
      StreamUtil.safeClose(destinationStream);
    }
  }
  
  public static void updateZipFile(URL sourceUrl, OutputStream destinationStream, String entryPath, InputStream dataStream)
    throws Exception
  {
    if (sourceUrl == null) {
      throw new Exception("Can not update zip file, url is null");
    }
    InputStream sourceStream = null;
    try
    {
      sourceStream = sourceUrl.openStream();
      updateZipFile(sourceStream, destinationStream, entryPath, dataStream);
    }
    finally
    {
      StreamUtil.safeClose(sourceStream);
    }
  }
  
  public static void updateZipFile(InputStream sourceStream, OutputStream destinationStream, String entryPath, InputStream dataStream)
    throws Exception
  {
    ZipInputStream zipInput = null;
    ZipOutputStream zipOutput = null;
    try
    {
      zipInput = new ZipInputStream(sourceStream);
      zipOutput = new ZipOutputStream(destinationStream);
      
      boolean updated = false;
      ZipEntry zipEntry;
      while ((zipEntry = zipInput.getNextEntry()) != null) {
        if (entryPath.equalsIgnoreCase(zipEntry.getName()))
        {
          zipOutput.putNextEntry(new ZipEntry(entryPath));
          StreamUtil.copy(dataStream, zipOutput);
          updated = true;
        }
        else if (!zipEntry.isDirectory())
        {
          zipOutput.putNextEntry(new ZipEntry(zipEntry.getName()));
          StreamUtil.copy(zipInput, zipOutput);
        }
      }
      if (!updated)
      {
        zipOutput.putNextEntry(new ZipEntry(entryPath));
        StreamUtil.copy(dataStream, zipOutput);
      }
    }
    finally
    {
      StreamUtil.safeClose(zipInput);
      StreamUtil.safeClose(zipOutput);
    }
  }
  
  public static void updateZipFileUsingStreams(String filePath, List<String> entryPaths, List<InputStream> dataStreams)
    throws Exception
  {
    updateZipFileUsingStreams(filePath, filePath, entryPaths, dataStreams);
  }
  
  public static void updateZipFileUsingStreams(String sourcePath, String destinationPath, List<String> entryPaths, List<InputStream> dataStreams)
    throws Exception
  {
    File sourceFile = new File(sourcePath);
    File destinationFile = new File(destinationPath);
    if (!sourceFile.getAbsolutePath().equals(destinationFile.getAbsolutePath()))
    {
      if (!sourceFile.exists()) {
        throw new Exception("Update zip file from '" + sourcePath + "' to '" + destinationPath + "' failed, source file does not exists");
      }
      if (destinationFile.exists()) {
        if (!destinationFile.delete()) {
          throw new Exception("Update zip file from '" + sourcePath + "' to '" + destinationPath + "' failed, destination file exists & unable to delete it");
        }
      }
      InputStream is = null;
      OutputStream os = null;
      try
      {
        is = new FileInputStream(sourceFile);
        os = new FileOutputStream(destinationFile);
        updateZipFileUsingStreams(is, os, entryPaths, dataStreams);
      }
      finally
      {
        StreamUtil.safeClose(is);
        StreamUtil.safeClose(os);
      }
      Object destinationStream = null;
      try
      {
        destinationStream = new FileOutputStream(destinationPath);
      }
      finally
      {
        StreamUtil.safeClose((OutputStream)destinationStream);
      }
    }
    else
    {
      if (!sourceFile.exists()) {
        throw new Exception("Update zip file '" + sourcePath + "' failed, file does not exists");
      }
      File destinationTempFile = new File(destinationPath + ".tmp");
      if (destinationTempFile.exists()) {
        throw new Exception("Update zip file '" + sourcePath + " failed, temp file '" + destinationTempFile.getPath() + "' already exists");
      }
      InputStream is = null;
      Object os = null;
      try
      {
        is = new FileInputStream(sourceFile);
        os = new FileOutputStream(destinationTempFile);
        updateZipFileUsingStreams(is, (OutputStream)os, entryPaths, dataStreams);
      }
      finally
      {
        StreamUtil.safeClose(is);
        StreamUtil.safeClose((OutputStream)os);
      }
      Object destinationStream = null;
      try
      {
        destinationStream = new FileOutputStream(destinationPath);
      }
      finally
      {
        StreamUtil.safeClose((OutputStream)destinationStream);
      }
      if (!destinationFile.delete()) {
        throw new Exception("Update zip file '" + sourcePath + " failed, can not delete file '" + destinationFile.getPath() + "'");
      }
      if (!destinationTempFile.renameTo(destinationFile)) {
        throw new Exception("Update zip file '" + sourcePath + " failed, can not rename file '" + destinationTempFile.getPath() + "' to '" + destinationFile.getPath() + "'");
      }
    }
  }
  
  public static void updateZipFileUsingStreams(URL sourceUrl, String destinationPath, List<String> entryPaths, List<InputStream> dataStreams)
    throws Exception
  {
    if (sourceUrl == null) {
      throw new Exception("Can not update zip file, url is null");
    }
    File destinationFile = new File(destinationPath);
    if (destinationFile.exists()) {
      if (!destinationFile.delete()) {
        throw new Exception("Update zip file from '" + sourceUrl.toString() + "' to '" + destinationPath + "' failed, destination file exists & unable to delete it");
      }
    }
    OutputStream destinationStream = null;
    try
    {
      destinationStream = new FileOutputStream(destinationFile);
      updateZipFileUsingStreams(sourceUrl, destinationStream, entryPaths, dataStreams);
    }
    finally
    {
      StreamUtil.safeClose(destinationStream);
    }
  }
  
  public static void updateZipFileUsingStreams(URL sourceUrl, OutputStream destinationStream, List<String> entryPaths, List<InputStream> dataStreams)
    throws Exception
  {
    if (sourceUrl == null) {
      throw new Exception("Can not update zip file, url is null");
    }
    InputStream sourceStream = null;
    try
    {
      sourceStream = sourceUrl.openStream();
      updateZipFileUsingStreams(sourceStream, destinationStream, entryPaths, dataStreams);
    }
    finally
    {
      StreamUtil.safeClose(sourceStream);
    }
  }
  
  public static void updateZipFileUsingStreams(InputStream sourceStream, OutputStream destinationStream, List<String> entryPaths, List<InputStream> dataStreams)
    throws Exception
  {
    ZipInputStream zipInput = null;
    ZipOutputStream zipOutput = null;
    try
    {
      zipInput = new ZipInputStream(sourceStream);
      zipOutput = new ZipOutputStream(destinationStream);
      
      ArrayList<String> updatedEntries = new ArrayList();
      ZipEntry zipEntry;
      while ((zipEntry = zipInput.getNextEntry()) != null)
      {
        int result = entryPaths.indexOf(zipEntry.getName());
        if (result >= 0)
        {
          zipOutput.putNextEntry(new ZipEntry(zipEntry.getName()));
          StreamUtil.copy((InputStream)dataStreams.get(result), zipOutput);
          updatedEntries.add(zipEntry.getName());
        }
        else if (!zipEntry.isDirectory())
        {
          zipOutput.putNextEntry(new ZipEntry(zipEntry.getName()));
          StreamUtil.copy(zipInput, zipOutput);
        }
      }
      int index = 0;
      for (String entryPath : entryPaths)
      {
        if (updatedEntries.indexOf(entryPath) < 0)
        {
          zipOutput.putNextEntry(new ZipEntry(entryPath));
          StreamUtil.copy((InputStream)dataStreams.get(index), zipOutput);
        }
        index++;
      }
    }
    finally
    {
      List<String> updatedEntries;
      int index;
      StreamUtil.safeClose(zipInput);
      StreamUtil.safeClose(zipOutput);
    }
  }
  
  public static void updateZipFileUsingUrls(String filePath, List<String> entryPaths, List<URL> dataUrls)
    throws Exception
  {
    updateZipFileUsingUrls(filePath, filePath, entryPaths, dataUrls);
  }
  
  public static void updateZipFileUsingUrls(String sourcePath, String destinationPath, List<String> entryPaths, List<URL> dataUrls)
    throws Exception
  {
    File sourceFile = new File(sourcePath);
    File destinationFile = new File(destinationPath);
    if (!sourceFile.getAbsolutePath().equals(destinationFile.getAbsolutePath()))
    {
      if (!sourceFile.exists()) {
        throw new Exception("Update zip file from '" + sourcePath + "' to '" + destinationPath + "' failed, source file does not exists");
      }
      if (destinationFile.exists()) {
        if (!destinationFile.delete()) {
          throw new Exception("Update zip file from '" + sourcePath + "' to '" + destinationPath + "' failed, destination file exists & unable to delete it");
        }
      }
      InputStream is = null;
      OutputStream os = null;
      try
      {
        is = new FileInputStream(sourceFile);
        os = new FileOutputStream(destinationFile);
        updateZipFileUsingUrls(is, os, entryPaths, dataUrls);
      }
      finally
      {
        StreamUtil.safeClose(is);
        StreamUtil.safeClose(os);
      }
      Object destinationStream = null;
      try
      {
        destinationStream = new FileOutputStream(destinationPath);
      }
      finally
      {
        StreamUtil.safeClose((OutputStream)destinationStream);
      }
    }
    else
    {
      if (!sourceFile.exists()) {
        throw new Exception("Update zip file '" + sourcePath + "' failed, file does not exists");
      }
      File destinationTempFile = new File(destinationPath + ".tmp");
      if (destinationTempFile.exists()) {
        throw new Exception("Update zip file '" + sourcePath + " failed, temp file '" + destinationTempFile.getPath() + "' already exists");
      }
      InputStream is = null;
      Object os = null;
      try
      {
        is = new FileInputStream(sourceFile);
        os = new FileOutputStream(destinationTempFile);
        updateZipFileUsingUrls(is, (OutputStream)os, entryPaths, dataUrls);
      }
      finally
      {
        StreamUtil.safeClose(is);
        StreamUtil.safeClose((OutputStream)os);
      }
      Object destinationStream = null;
      try
      {
        destinationStream = new FileOutputStream(destinationPath);
      }
      finally
      {
        StreamUtil.safeClose((OutputStream)destinationStream);
      }
      if (!destinationFile.delete()) {
        throw new Exception("Update zip file '" + sourcePath + " failed, can not delete file '" + destinationFile.getPath() + "'");
      }
      if (!destinationTempFile.renameTo(destinationFile)) {
        throw new Exception("Update zip file '" + sourcePath + " failed, can not rename file '" + destinationTempFile.getPath() + "' to '" + destinationFile.getPath() + "'");
      }
    }
  }
  
  public static void updateZipFileUsingUrls(URL sourceUrl, String destinationPath, List<String> entryPaths, List<URL> dataUrls)
    throws Exception
  {
    if (sourceUrl == null) {
      throw new Exception("Can not update zip file, url is null");
    }
    File destinationFile = new File(destinationPath);
    if (destinationFile.exists()) {
      if (!destinationFile.delete()) {
        throw new Exception("Update zip file from '" + sourceUrl.toString() + "' to '" + destinationPath + "' failed, destination file exists & unable to delete it");
      }
    }
    OutputStream destinationStream = null;
    try
    {
      destinationStream = new FileOutputStream(destinationFile);
      updateZipFileUsingUrls(sourceUrl, destinationStream, entryPaths, dataUrls);
    }
    finally
    {
      StreamUtil.safeClose(destinationStream);
    }
  }
  
  public static void updateZipFileUsingUrls(URL sourceUrl, OutputStream destinationStream, List<String> entryPaths, List<URL> dataUrls)
    throws Exception
  {
    if (sourceUrl == null) {
      throw new Exception("Can not update zip file, url is null");
    }
    InputStream sourceStream = null;
    try
    {
      sourceStream = sourceUrl.openStream();
      updateZipFileUsingUrls(sourceStream, destinationStream, entryPaths, dataUrls);
    }
    finally
    {
      StreamUtil.safeClose(sourceStream);
    }
  }
  
  public static void updateZipFileUsingUrls(InputStream sourceStream, OutputStream destinationStream, List<String> entryPaths, List<URL> dataUrls)
    throws Exception
  {
    ZipInputStream zipInput = null;
    ZipOutputStream zipOutput = null;
    try
    {
      zipInput = new ZipInputStream(sourceStream);
      zipOutput = new ZipOutputStream(destinationStream);
      
      ArrayList<String> updatedEntries = new ArrayList();
      ZipEntry zipEntry;
      while ((zipEntry = zipInput.getNextEntry()) != null)
      {
        int result = entryPaths.indexOf(zipEntry.getName());
        if (result >= 0)
        {
          zipOutput.putNextEntry(new ZipEntry(zipEntry.getName()));
          
          InputStream dataStream = null;
          try
          {
            dataStream = ((URL)dataUrls.get(result)).openStream();
            StreamUtil.copy(dataStream, zipOutput);
          }
          finally
          {
            StreamUtil.safeClose(dataStream);
          }
          updatedEntries.add(zipEntry.getName());
        }
        else if (!zipEntry.isDirectory())
        {
          zipOutput.putNextEntry(new ZipEntry(zipEntry.getName()));
          StreamUtil.copy(zipInput, zipOutput);
        }
      }
      int index = 0;
      for (String entryPath : entryPaths)
      {
        if (updatedEntries.indexOf(entryPath) < 0)
        {
          zipOutput.putNextEntry(new ZipEntry(entryPath));
          InputStream dataStream = null;
          try
          {
            dataStream = ((URL)dataUrls.get(index)).openStream();
            StreamUtil.copy(dataStream, zipOutput);
          }
          finally
          {
            StreamUtil.safeClose(dataStream);
          }
        }
        index++;
      }
    }
    finally
    {
      List<String> updatedEntries;
      InputStream dataStream;
      int index;
      StreamUtil.safeClose(zipInput);
      StreamUtil.safeClose(zipOutput);
    }
  }
}
