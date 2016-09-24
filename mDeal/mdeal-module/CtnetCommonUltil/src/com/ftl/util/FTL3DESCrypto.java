package com.ftl.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;

public class FTL3DESCrypto
{
  private static byte[] iv = StringUtil.hexStringToByteArray("8C01EB2AA256E9B5");
  private static byte[] key = StringUtil.hexStringToByteArray("9D7F45C16E132C1F54372A106DD3E9F81A731A915B619E89");
  private static Cipher encryptor = null;
  private static Cipher decryptor = null;
  
  static
  {
    InputStream ivStream = null;
    InputStream keyStream = null;
    try
    {
      ivStream = UrlUtil.getResource("FTL3DES.iv").openStream();
      keyStream = UrlUtil.getResource("FTL3DES.key").openStream();
      iv = StringUtil.hexStringToByteArray(StreamUtil.readStream(ivStream));
      key = StringUtil.hexStringToByteArray(StreamUtil.readStream(keyStream));
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    finally
    {
      StreamUtil.safeClose(ivStream);
      StreamUtil.safeClose(keyStream);
    }
    try
    {
      encryptor = Crypto.create3DESEncryptionCipher(key, iv);
      decryptor = Crypto.create3DESDecryptionCipher(key, iv);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
  
  public static synchronized String encrypt(String plain)
    throws Exception
  {
    if ((plain == null) || (plain.isEmpty())) {
      return "";
    }
    return StringUtil.byteArrayToHexString(encryptor.doFinal(plain.getBytes(Global.ENCODING)));
  }
  
  public static synchronized String decrypt(String encrypted)
    throws Exception
  {
    if ((encrypted == null) || (encrypted.isEmpty())) {
      return "";
    }
    return new String(decryptor.doFinal(StringUtil.hexStringToByteArray(encrypted)), Global.ENCODING);
  }
  
  public static Cipher getEncryptor()
  {
    return encryptor;
  }
  
  public static Cipher getDecryptor()
  {
    return decryptor;
  }
  
  public static void encryptFile(String inputFileName, String outputFileName)
    throws Exception
  {
    byte[] content = FileUtil.readFileToByteArray(inputFileName);
    OutputStream os = null;
    try
    {
      os = new FileOutputStream(outputFileName);
      CipherOutputStream cos = null;
      try
      {
        cos = new CipherOutputStream(os, getEncryptor());
        cos.write(content);
        cos.flush();
      }
      finally {}
    }
    finally
    {
      if (os != null) {
        os.close();
      }
    }
  }
  
  public static void decryptFile(String inputFileName, String outputFileName)
    throws Exception
  {
    File file = new File(inputFileName);
    FileInputStream is = null;
    try
    {
      is = new FileInputStream(file);
      CipherInputStream cis = null;
      byte[] content;
      try
      {
        cis = new CipherInputStream(is, getDecryptor());
        content = StreamUtil.readStreamToByteArray(cis);
      }
      finally
      {
        if (cis != null) {
          cis.close();
        }
      }
      FileUtil.writeFile(outputFileName, content);
    }
    finally
    {
      StreamUtil.safeClose(is);
    }
  }
}
