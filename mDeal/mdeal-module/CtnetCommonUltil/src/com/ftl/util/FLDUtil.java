package com.ftl.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FLDUtil
{
  public static int readFLDLength(InputStream is)
    throws IOException
  {
    int length = is.read();
    if (length <= 250) {
      return length;
    }
    byte[] lengthData = StreamUtil.getFixedSizeBuffer(is, length - 250);
    return StringUtil.byteArrayToInt(lengthData);
  }
  
  public static String readFLDString(InputStream is)
    throws IOException
  {
    return new String(readFLDData(is));
  }
  
  public static byte[] readFLDData(InputStream is)
    throws IOException
  {
    int length = readFLDLength(is);
    if (length < 0) {
      return null;
    }
    if (length == 0) {
      return new byte[0];
    }
    return StreamUtil.getFixedSizeBuffer(is, length);
  }
  
  public static void writeFLDString(OutputStream os, String content)
    throws IOException
  {
    if ((content == null) || (content.isEmpty()))
    {
      os.write(0);
      return;
    }
    writeFLDData(os, content.getBytes(Global.ENCODING));
  }
  
  public static void writeFLDData(OutputStream os, byte[] content)
    throws IOException
  {
    if (content == null)
    {
      os.write(0);
      return;
    }
    if (content.length <= 250)
    {
      os.write(StringUtil.intToByteArray(content.length));
    }
    else
    {
      byte[] lengthData = StringUtil.intToByteArray(content.length);
      os.write(lengthData.length + 250);
      os.write(lengthData);
    }
    os.write(content);
  }
  
  public static byte[] intToByteArray(int value)
  {
    byte[] result = new byte[4];
    for (int i = 0; i < 4; i++) {
      result[(3 - i)] = ((byte)(value >>> i * 8));
    }
    return result;
  }
  
  public static int byteArrayToInt(byte[] value)
  {
    int result = 0;
    for (int i = 0; i < 4; i++)
    {
      result <<= 8;
      result ^= value[i] & 0xFF;
    }
    return result;
  }
  
  public static byte[] longToByteArray(long value)
  {
    byte[] result = new byte[8];
    for (int i = 0; i < 8; i++) {
      result[(7 - i)] = ((byte)(int)(value >>> i * 8));
    }
    return result;
  }
  
  public static long byteArrayToLong(byte[] value)
  {
    long result = 0L;
    for (int i = 0; i < 8; i++)
    {
      result <<= 8;
      result ^= value[i] & 0xFF;
    }
    return result;
  }
}
