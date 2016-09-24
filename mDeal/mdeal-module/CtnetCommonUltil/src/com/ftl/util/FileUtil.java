package com.ftl.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileUtil
{
  
  public static final int BUFFER_SIZE = 65536;
  
  public static void forceFolderExist(String folderPath)
    throws IOException
  {
    forceFolderExist(new File(folderPath));
  }
  
  public static void forceFolderExist(File folder)
    throws IOException
  {
    if (!folder.exists())
    {
      if (!folder.mkdirs()) {
        throw new IOException("Could not create folder " + folder.getPath());
      }
    }
    else if (!folder.isDirectory()) {
      throw new IOException("A file with name " + folder.getPath() + " already exist");
    }
  }
  
  public static boolean renameFile(String source, String destination)
  {
    return renameFile(source, destination, false);
  }
  
  public static boolean renameFile(String source, String destination, boolean deleteIfExist)
  {
    File sourceFile = new File(source);
    File destinationFile = new File(destination);
    return renameFile(sourceFile, destinationFile, deleteIfExist);
  }
  
  public static boolean renameFile(File sourceFile, File destinationFile)
  {
    return renameFile(sourceFile, destinationFile, false);
  }
  
  public static boolean renameFile(File sourceFile, File destinationFile, boolean deleteIfExist)
  {
    if (sourceFile.getAbsolutePath().equals(destinationFile.getAbsolutePath())) {
      return false;
    }
    if (destinationFile.exists()) {
      if (deleteIfExist)
      {
        if (!destinationFile.delete()) {
          return false;
        }
      }
      else {
        return false;
      }
    }
    return sourceFile.renameTo(destinationFile);
  }
  
  public static void copyFile(String sourcePath, String destinationPath)
    throws Exception
  {
    File sourceFile = new File(sourcePath);
    File destinationFile = new File(destinationPath);
    copyFile(sourceFile, destinationFile);
  }
  
  public static void copyFile(File sourceFile, File destinationFile)
    throws Exception
  {
    if (!sourceFile.exists()) {
      throw new Exception("Copy file from '" + sourceFile.getPath() + "' to '" + destinationFile.getPath() + "' failed, source file does not exists");
    }
    if (destinationFile.exists()) {
      if (!destinationFile.delete()) {
        throw new Exception("Copy file from '" + sourceFile.getPath() + "' to '" + destinationFile.getPath() + "' failed, destination file exists & unable to delete it");
      }
    }
    InputStream is = null;
    OutputStream os = null;
    try
    {
      is = new FileInputStream(sourceFile);
      os = new FileOutputStream(destinationFile);
      StreamUtil.copy(is, os);
      destinationFile.setLastModified(sourceFile.lastModified());
    }
    finally
    {
      StreamUtil.safeClose(is);
      StreamUtil.safeClose(os);
    }
  }
  
  public static void copyFile(URL sourceUrl, String destinationPath)
    throws Exception
  {
    copyFile(sourceUrl, new File(destinationPath));
  }
  
  public static void copyFile(URL sourceUrl, File destinationFile)
    throws Exception
  {
    if (sourceUrl == null) {
      throw new Exception("Can not copy data, url is null");
    }
    if (destinationFile.exists()) {
      if (!destinationFile.delete()) {
        throw new Exception("Copy data from '" + sourceUrl.toString() + "' to '" + destinationFile.getPath() + "' failed, destination file exists & unable to delete it");
      }
    }
    InputStream is = null;
    OutputStream os = null;
    try
    {
      is = sourceUrl.openStream();
      os = new FileOutputStream(destinationFile);
      StreamUtil.copy(is, os);
    }
    finally
    {
      StreamUtil.safeClose(is);
      StreamUtil.safeClose(os);
    }
  }
  
  public static boolean deleteFile(String source)
  {
    File sourceFile = new File(source);
    return sourceFile.delete();
  }
  
  public static void deleteOldFile(String path, String wildcard, int offset)
  {
    if (!path.endsWith("/")) {
      path = path + "/";
    }
    File folder = new File(path);
    if (!folder.exists()) {
      return;
    }
    String[] fileNames = folder.list(new WildcardFilter(wildcard));
    if ((fileNames != null) && (fileNames.length > 0))
    {
      long currentTime = new Date().getTime();
      for (int fileIndex = 0; fileIndex < fileNames.length; fileIndex++)
      {
        File file = new File(path + fileNames[fileIndex]);
        if (currentTime - file.lastModified() >= offset) {
          if (!file.delete()) {
            //logger.log(Level.WARNING, "Delete file {0} failed.", file.getAbsolutePath());
          }
        }
      }
    }
  }
  
  public static void copyDirectory(File srcDir, File destDir)
    throws Exception
  {
    copyDirectory(srcDir, destDir, null);
  }
  
  public static void copyDirectory(File srcDir, File destDir, FileFilter filter)
    throws Exception
  {
    if (srcDir == null) {
      throw new NullPointerException("Source must not be null");
    }
    if (destDir == null) {
      throw new NullPointerException("Destination must not be null");
    }
    if (!srcDir.exists()) {
      throw new FileNotFoundException("Source '" + srcDir + "' does not exist");
    }
    if (!srcDir.isDirectory()) {
      throw new IOException("Source '" + srcDir + "' exists but is not a directory");
    }
    if (srcDir.getCanonicalPath().equals(destDir.getCanonicalPath())) {
      throw new IOException("Source '" + srcDir + "' and destination '" + destDir + "' are the same");
    }
    List exclusionList = null;
    if (destDir.getCanonicalPath().startsWith(srcDir.getCanonicalPath()))
    {
      File[] srcFiles = filter == null ? srcDir.listFiles() : srcDir.listFiles(filter);
      if ((srcFiles != null) && (srcFiles.length > 0))
      {
        exclusionList = new ArrayList(srcFiles.length);
        for (int i = 0; i < srcFiles.length; i++)
        {
          File copiedFile = new File(destDir, srcFiles[i].getName());
          exclusionList.add(copiedFile.getCanonicalPath());
        }
      }
    }
    doCopyDirectory(srcDir, destDir, filter, exclusionList);
  }
  
  private static void doCopyDirectory(File srcDir, File destDir, FileFilter filter, List exclusionList)
    throws Exception
  {
    if (destDir.exists())
    {
      if (!destDir.isDirectory()) {
        throw new IOException("Destination '" + destDir + "' exists but is not a directory");
      }
    }
    else
    {
      if (!destDir.mkdirs()) {
        throw new IOException("Destination '" + destDir + "' directory cannot be created");
      }
      destDir.setLastModified(srcDir.lastModified());
    }
    if (!destDir.canWrite()) {
      throw new IOException("Destination '" + destDir + "' cannot be written to");
    }
    File[] files = filter == null ? srcDir.listFiles() : srcDir.listFiles(filter);
    if (files == null) {
      throw new IOException("Failed to list contents of " + srcDir);
    }
    for (int i = 0; i < files.length; i++)
    {
      File copiedFile = new File(destDir, files[i].getName());
      if ((exclusionList == null) || (!exclusionList.contains(files[i].getCanonicalPath()))) {
        if (files[i].isDirectory()) {
          doCopyDirectory(files[i], copiedFile, filter, exclusionList);
        } else {
          copyFile(files[i], copiedFile);
        }
      }
    }
  }
  
  public static void deleteDirectory(File dir)
    throws Exception
  {
    if (dir == null) {
      throw new NullPointerException("Source must not be null");
    }
    if (!dir.exists()) {
      throw new FileNotFoundException("Source '" + dir + "' does not exist");
    }
    if (!dir.isDirectory()) {
      throw new IOException("Source '" + dir + "' exists but is not a directory");
    }
    doDeleteDirectory(dir);
  }
  
  private static void doDeleteDirectory(File dir)
    throws Exception
  {
    File[] files = dir.listFiles();
    if (files == null) {
      throw new IOException("Failed to list contents of " + dir);
    }
    for (int i = 0; i < files.length; i++) {
      if (files[i].isDirectory()) {
        doDeleteDirectory(files[i]);
      } else if (!files[i].delete()) {
        //logger.log(Level.WARNING, "Delete file {0} failed.", files[i].getAbsolutePath());
      }
    }
    if (!dir.delete()) {
      //logger.log(Level.WARNING, "Delete file {0} failed.", dir.getAbsolutePath());
    }
  }
  
  public static void backup(String fileName, int maxSize)
  {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    File sourceFile = new File(fileName);
    if (sourceFile.length() > maxSize)
    {
      String newName = fileName + "." + dateFormat.format(new Date());
      renameFile(fileName, newName);
    }
  }
  
  public static String backup(String sourcePath, String backupPath, String sourceFile, String backupFile, String backupStyle)
    throws Exception
  {
    return backup(sourcePath, backupPath, sourceFile, backupFile, backupStyle, true);
  }
  
  public static String backup(String sourcePath, String backupPath, String sourceFile, String backupFile, String backupStyle, boolean replaceIfExist)
    throws Exception
  {
    return backup(sourcePath, backupPath, sourceFile, backupFile, backupStyle, "", replaceIfExist);
  }
  
  public static String backup(String sourcePath, String backupPath, String sourceFile, String backupFile, String backupStyle, String additionPath)
    throws Exception
  {
    return backup(sourcePath, backupPath, sourceFile, backupFile, backupStyle, additionPath, true);
  }
  
  public static String backup(String sourcePath, String backupPath, String sourceFile, String backupFile, String backupStyle, String additionPath, boolean replaceIfExist)
    throws Exception
  {
    if (backupStyle.equals("Delete file"))
    {
      if (!deleteFile(sourcePath + sourceFile)) {
        throw new Exception("Cannot delete file " + sourcePath + sourceFile);
      }
    }
    else if (backupPath.length() > 0)
    {
      String currentDate = "";
      if (backupStyle.equals("Daily")) {
        currentDate = StringUtil.format(new Date(), "yyyyMMdd") + "/";
      } else if (backupStyle.equals("Monthly")) {
        currentDate = StringUtil.format(new Date(), "yyyyMM") + "/";
      } else if (backupStyle.equals("Yearly")) {
        currentDate = StringUtil.format(new Date(), "yyyy") + "/";
      }
      forceFolderExist(backupPath + currentDate + additionPath);
      if (!renameFile(sourcePath + sourceFile, backupPath + currentDate + additionPath + backupFile, replaceIfExist)) {
        throw new Exception("Cannot rename file " + sourcePath + sourceFile + " to " + backupPath + currentDate + backupFile);
      }
      return backupPath + currentDate + backupFile;
    }
    return "";
  }
  
  public static long getSequenceValue(String fileName)
    throws Exception
  {
    return getSequenceValue(new File(fileName));
  }
  
  public static long getSequenceValue(File file)
    throws Exception
  {
    return getSequenceValue(file, 1L, 0L);
  }
  
  public static long getSequenceValue(File file, long splitCount, long splitIndex)
    throws Exception
  {
    String fileContent = readFile(file);
    long sequenceValue = Long.parseLong(fileContent);
    sequenceValue = (sequenceValue / splitCount + 1L) * splitCount + splitIndex;
    writeFile(file, String.valueOf(sequenceValue));
    return sequenceValue;
  }
  
  public static byte[] readFileToByteArray(String fileName)
    throws Exception
  {
    return readFileToByteArray(new File(fileName));
  }
  
  public static byte[] readFileToByteArray(File file)
    throws Exception
  {
    FileInputStream is = null;
    try
    {
      is = new FileInputStream(file);
      return StreamUtil.readStreamToByteArray(is);
    }
    finally
    {
      StreamUtil.safeClose(is);
    }
  }
  
  public static String readFile(String fileName)
    throws Exception
  {
    return readFile(fileName, Global.ENCODING);
  }
  
  public static String readFile(File file)
    throws Exception
  {
    return new String(readFileToByteArray(file));
  }
  
  public static String readFile(String fileName, String encoding)
    throws Exception
  {
    return new String(readFileToByteArray(fileName), encoding);
  }
  
  public static String readFile(File file, String encoding)
    throws Exception
  {
    return new String(readFileToByteArray(file), encoding);
  }
  
  public static void writeFile(String fileName, String content, String encoding)
    throws Exception
  {
    writeFile(fileName, content.getBytes(encoding));
  }
  
  public static void appendFile(String fileName, String content, String encoding)
    throws Exception
  {
    appendFile(fileName, content.getBytes(encoding));
  }
  
  public static void writeFile(File file, String content, String encoding)
    throws IOException
  {
    writeFile(file, content.getBytes(encoding), false);
  }
  
  public static void appendFile(File file, String content, String encoding)
    throws IOException
  {
    writeFile(file, content.getBytes(encoding), true);
  }
  
  public static void writeFile(String fileName, String content)
    throws Exception
  {
    writeFile(fileName, content.getBytes());
  }
  
  public static void appendFile(String fileName, String content)
    throws Exception
  {
    appendFile(fileName, content.getBytes());
  }
  
  public static void writeFile(File file, String content)
    throws IOException
  {
    writeFile(file, content.getBytes(), false);
  }
  
  public static void appendFile(File file, String content)
    throws IOException
  {
    writeFile(file, content.getBytes(), true);
  }
  
  public static void writeFile(String fileName, byte[] data)
    throws Exception
  {
    writeFile(new File(fileName), data, false);
  }
  
  public static void appendFile(String fileName, byte[] data)
    throws Exception
  {
    writeFile(new File(fileName), data, true);
  }
  
  public static void writeFile(File file, byte[] data)
    throws IOException
  {
    writeFile(file, data, false);
  }
  
  public static void writeFile(File file, byte[] data, boolean append)
    throws IOException
  {
    FileOutputStream os = null;
    try
    {
      os = new FileOutputStream(file, append);
      os.write(data);
    }
    finally
    {
      StreamUtil.safeClose(os);
    }
  }
  
  public static void writeFile(String fileName, InputStream is)
    throws Exception
  {
    writeFile(new File(fileName), is, false);
  }
  
  public static void appendFile(String fileName, InputStream is)
    throws Exception
  {
    writeFile(new File(fileName), is, true);
  }
  
  public static void writeFile(File file, InputStream is)
    throws IOException
  {
    writeFile(file, is, false);
  }
  
  public static void writeFile(File file, InputStream is, boolean append)
    throws IOException
  {
    FileOutputStream os = null;
    try
    {
      os = new FileOutputStream(file, append);
      byte[] data = new byte[65536];
      int length;
      while ((length = is.read(data)) != -1) {
        os.write(data, 0, length);
      }
    }
    finally
    {
      StreamUtil.safeClose(os);
    }
  }
}
