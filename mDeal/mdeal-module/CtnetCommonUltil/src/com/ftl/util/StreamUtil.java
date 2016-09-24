package com.ftl.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamUtil
{
  public static final int BUFFER_SIZE = 65536;
  private static final int delayTime = 100;
  
  public static byte[] trim(byte[] source)
  {
    int firstIndex = 0;
    while ((firstIndex < source.length) && (source[firstIndex] <= 32)) {
      firstIndex++;
    }
    if (firstIndex >= source.length) {
      return new byte[0];
    }
    int lastIndex = source.length - 1;
    while ((lastIndex > firstIndex) && (source[lastIndex] <= 32)) {
      lastIndex--;
    }
    byte[] result = new byte[lastIndex - firstIndex + 1];
    System.arraycopy(source, firstIndex, result, 0, result.length);
    return result;
  }
  
  public static byte[] trimLeft(byte[] source)
  {
    int firstIndex = 0;
    while ((firstIndex < source.length) && (source[firstIndex] <= 32)) {
      firstIndex++;
    }
    if (firstIndex >= source.length) {
      return new byte[0];
    }
    byte[] result = new byte[source.length - firstIndex];
    System.arraycopy(source, firstIndex, result, 0, result.length);
    return result;
  }
  
  public static byte[] trimRight(byte[] source)
  {
    int lastIndex = source.length - 1;
    while ((lastIndex >= 0) && (source[lastIndex] <= 32)) {
      lastIndex--;
    }
    if (lastIndex < 0) {
      return new byte[0];
    }
    byte[] result = new byte[lastIndex + 1];
    System.arraycopy(source, 0, result, 0, result.length);
    return result;
  }
  
  public static byte[] getData(InputStream is)
    throws IOException
  {
    int length = is.available();
    if (length < 0) {
      throw new IOException("No data to read");
    }
    byte[] result = new byte[length];
    if (is.read(result) < length) {
      throw new IOException("No data to read");
    }
    return result;
  }
  
  public static byte[] getDataTerminatedBySymbol(InputStream is, String symbol)
    throws IOException
  {
    return getDataTerminatedBySymbol(is, symbol.getBytes());
  }
  
  public static byte[] getDataTerminatedBySymbol(InputStream is, byte[] symbol)
    throws IOException
  {
    ByteArrayOutputStream os = null;
    try
    {
      os = new ByteArrayOutputStream();
      
      int symbolIndex = 0;
      int bytesRead;
      while ((bytesRead = is.read()) >= 0)
      {
        os.write(bytesRead);
        if (bytesRead != symbol[symbolIndex])
        {
          symbolIndex = 0;
          if (bytesRead == symbol[symbolIndex]) {
            symbolIndex++;
          }
        }
        else
        {
          symbolIndex++;
        }
        if (symbolIndex >= symbol.length) {
          return os.toByteArray();
        }
      }
      throw new IOException("No data to read");
    }
    finally
    {
      safeClose(os);
    }
  }
  
  public static byte[] getDataTerminatedBySymbol(InputStream is, byte symbol)
    throws IOException
  {
    ByteArrayOutputStream os = null;
    try
    {
      os = new ByteArrayOutputStream();
      int bytesRead;
      while ((bytesRead = is.read()) >= 0)
      {
        os.write(bytesRead);
        if (bytesRead == symbol) {
          return os.toByteArray();
        }
      }
      throw new IOException("No data to read");
    }
    finally
    {
      safeClose(os);
    }
  }
  
  public static byte[] getDataTerminatedBySymbol(InputStream is, String symbol, boolean ignoreCase)
    throws IOException
  {
    if (ignoreCase)
    {
      byte[] lowerSymbol = symbol.toLowerCase().getBytes();
      byte[] upperSymbol = symbol.toUpperCase().getBytes();
      ByteArrayOutputStream os = null;
      try
      {
        os = new ByteArrayOutputStream();
        
        int symbolIndex = 0;
        int bytesRead;
        while ((bytesRead = is.read()) >= 0)
        {
          os.write(bytesRead);
          if ((bytesRead != lowerSymbol[symbolIndex]) && (bytesRead != upperSymbol[symbolIndex])) {
            symbolIndex = 0;
          } else {
            symbolIndex++;
          }
          if (symbolIndex >= lowerSymbol.length) {
            return os.toByteArray();
          }
        }
      }
      finally
      {
        safeClose(os);
      }
    }
    else
    {
      return getDataTerminatedBySymbol(is, symbol);
    }
    throw new IOException("No data to read");
  }
  
  public static byte[] getDataTerminatedBySymbol(InputStream is, String symbol, long timeOut)
    throws Exception
  {
    return getDataTerminatedBySymbol(is, symbol.getBytes(), timeOut);
  }
  
  public static byte[] getDataTerminatedBySymbol(InputStream is, byte[] symbol, long timeOut)
    throws Exception
  {
    ByteArrayOutputStream os = null;
    try
    {
      os = new ByteArrayOutputStream();
      int bytesRead = 0;
      int symbolIndex = 0;
      long expireTime = System.currentTimeMillis() + timeOut;
      do
      {
        while ((is.available() > 0) && ((bytesRead = is.read()) >= 0))
        {
          os.write(bytesRead);
          if (bytesRead != symbol[symbolIndex])
          {
            symbolIndex = 0;
            if (bytesRead == symbol[symbolIndex]) {
              symbolIndex++;
            }
          }
          else
          {
            symbolIndex++;
          }
          if (symbolIndex >= symbol.length) {
            return os.toByteArray();
          }
        }
        if (bytesRead < 0) {
          throw new IOException("No data to read");
        }
        Thread.sleep(100L);
      } while (System.currentTimeMillis() < expireTime);
      throw new IOException("Wait '" + new String(symbol) + "' for " + timeOut + " miliseconds was timed out");
    }
    finally
    {
      safeClose(os);
    }
  }
  
  public static byte[] getDataTerminatedBySymbol(InputStream is, byte symbol, long timeOut)
    throws Exception
  {
    ByteArrayOutputStream os = null;
    try
    {
      os = new ByteArrayOutputStream();
      int bytesRead = 0;
      long expireTime = System.currentTimeMillis() + timeOut;
      do
      {
        while ((is.available() > 0) && ((bytesRead = is.read()) >= 0))
        {
          os.write(bytesRead);
          if (bytesRead == symbol) {
            return os.toByteArray();
          }
        }
        if (bytesRead < 0) {
          throw new IOException("No data to read");
        }
        Thread.sleep(100L);
      } while (System.currentTimeMillis() < expireTime);
      throw new IOException("Wait '" + symbol + "' for " + timeOut + " miliseconds was timed out");
    }
    finally
    {
      safeClose(os);
    }
  }
  
  public static byte[] getDataTerminatedBySymbol(InputStream is, String symbol, long timeOut, boolean ignoreCase)
    throws Exception
  {
    if (ignoreCase)
    {
      ByteArrayOutputStream os = null;
      try
      {
        os = new ByteArrayOutputStream();
        byte[] lowerSymbol = symbol.toLowerCase().getBytes();
        byte[] upperSymbol = symbol.toUpperCase().getBytes();
        byte[] originSymbol = symbol.getBytes();
        int bytesRead = 0;
        int symbolIndex = 0;
        long expireTime = System.currentTimeMillis() + timeOut;
        do
        {
          while ((is.available() > 0) && ((bytesRead = is.read()) >= 0))
          {
            os.write(bytesRead);
            if ((bytesRead != lowerSymbol[symbolIndex]) && (bytesRead != upperSymbol[symbolIndex])) {
              symbolIndex = 0;
            } else {
              symbolIndex++;
            }
            if (symbolIndex >= originSymbol.length) {
              return os.toByteArray();
            }
          }
          if (bytesRead < 0) {
            throw new IOException("No data to read");
          }
          Thread.sleep(100L);
        } while (System.currentTimeMillis() < expireTime);
        throw new IOException("Wait '" + symbol + "' for " + timeOut + " miliseconds was timed out");
      }
      finally
      {
        safeClose(os);
      }
    }
    return getDataTerminatedBySymbol(is, symbol, timeOut);
  }
  
  public static byte[] getDataTerminatedBySymbolNoWait(InputStream is, String symbol)
    throws IOException
  {
    return getDataTerminatedBySymbolNoWait(is, symbol.getBytes());
  }
  
  public static byte[] getDataTerminatedBySymbolNoWait(InputStream is, byte[] symbol)
    throws IOException
  {
    ByteArrayOutputStream os = null;
    try
    {
      os = new ByteArrayOutputStream();
      int bytesRead = 0;
      int symbolIndex = 0;
      if (is.available() <= 0) {
        throw new IOException("No data to read");
      }
      while ((is.available() > 0) && ((bytesRead = is.read()) >= 0))
      {
        os.write(bytesRead);
        if (bytesRead != symbol[symbolIndex])
        {
          symbolIndex = 0;
          if (bytesRead == symbol[symbolIndex]) {
            symbolIndex++;
          }
        }
        else
        {
          symbolIndex++;
        }
        if (symbolIndex >= symbol.length) {
          return os.toByteArray();
        }
      }
      throw new IOException("No data to read");
    }
    finally
    {
      safeClose(os);
    }
  }
  
  public static byte[] getDataTerminatedBySymbolNoWait(InputStream is, byte symbol)
    throws IOException
  {
    ByteArrayOutputStream os = null;
    try
    {
      os = new ByteArrayOutputStream();
      int bytesRead = 0;
      if (is.available() <= 0) {
        throw new IOException("No data to read");
      }
      while ((is.available() > 0) && ((bytesRead = is.read()) >= 0))
      {
        os.write(bytesRead);
        if (bytesRead == symbol) {
          return os.toByteArray();
        }
      }
      throw new IOException("No data to read");
    }
    finally
    {
      safeClose(os);
    }
  }
  
  public static byte[] getDataTerminatedBySymbolNoWait(InputStream is, String symbol, boolean ignoreCase)
    throws IOException
  {
    if (is.available() <= 0) {
      throw new IOException("No data to read");
    }
    if (ignoreCase)
    {
      byte[] lowerSymbol = symbol.toLowerCase().getBytes();
      byte[] upperSymbol = symbol.toUpperCase().getBytes();
      ByteArrayOutputStream os = null;
      try
      {
        os = new ByteArrayOutputStream();
        int bytesRead = 0;
        int symbolIndex = 0;
        while ((is.available() > 0) && ((bytesRead = is.read()) >= 0))
        {
          os.write(bytesRead);
          if ((bytesRead != lowerSymbol[symbolIndex]) && (bytesRead != upperSymbol[symbolIndex])) {
            symbolIndex = 0;
          } else {
            symbolIndex++;
          }
          if (symbolIndex >= lowerSymbol.length) {
            return os.toByteArray();
          }
        }
        throw new IOException("No data to read");
      }
      finally
      {
        safeClose(os);
      }
    }
    return getDataTerminatedBySymbolNoWait(is, symbol);
  }
  
  public static byte[] getFixedSizeBuffer(InputStream is, int size)
    throws IOException
  {
    byte[] buffer = new byte[size];
    fillFixedSizeBuffer(is, buffer, 0, size);
    return buffer;
  }
  
  public static void fillFixedSizeBuffer(InputStream is, byte[] buffer, int offset, int size)
    throws IOException
  {
    int remainSize = size;
    int bytesRead = 0;
    while ((remainSize > 0) && 
      ((bytesRead = is.read(buffer, offset + size - remainSize, remainSize)) >= 0))
    {
      if (bytesRead == 0) {
        try
        {
          Thread.sleep(100L);
        }
        catch (InterruptedException ex)
        {
          ex.printStackTrace();
        }
      }
      remainSize -= bytesRead;
    }
    if (bytesRead < 0) {
      throw new IOException("No more data to read, " + (size - remainSize) + " bytes read, " + remainSize + "  bytes remain");
    }
  }
  
  public static String readStream(InputStream is)
    throws IOException
  {
    return new String(readStreamToByteArray(is));
  }
  
  public static String readStream(InputStream is, String encoding)
    throws IOException
  {
    return new String(readStreamToByteArray(is), encoding);
  }
  
  public static byte[] readStreamToByteArray(InputStream is)
    throws IOException
  {
    ByteArrayOutputStream os = null;
    try
    {
      os = new ByteArrayOutputStream();
      byte[] buffer;
      for (;;)
      {
        buffer = new byte[65536];
        int bytesRead = is.read(buffer);
        if (bytesRead < 0) {
          break;
        }
        if (bytesRead == 0) {
          try
          {
            Thread.sleep(50L);
          }
          catch (InterruptedException ex)
          {
            ex.printStackTrace();
          }
        }
        os.write(buffer, 0, bytesRead);
      }
      return os.toByteArray();
    }
    finally
    {
      safeClose(os);
    }
  }
  
  public static byte[] getDataTerminatedBySymbolOrReachEOF(InputStream is, String symbol)
    throws IOException
  {
    return getDataTerminatedBySymbolOrReachEOF(is, symbol.getBytes());
  }
  
  public static byte[] getDataTerminatedBySymbolOrReachEOF(InputStream is, byte[] symbol)
    throws IOException
  {
    ByteArrayOutputStream os = null;
    try
    {
      os = new ByteArrayOutputStream();
      
      int symbolIndex = 0;
      int bytesRead;
      byte[] arrayOfByte;
      while ((bytesRead = is.read()) >= 0)
      {
        os.write(bytesRead);
        if (bytesRead != symbol[symbolIndex])
        {
          symbolIndex = 0;
          if (bytesRead == symbol[symbolIndex]) {
            symbolIndex++;
          }
        }
        else
        {
          symbolIndex++;
        }
        if (symbolIndex >= symbol.length) {
          return os.toByteArray();
        }
      }
      return os.toByteArray();
    }
    finally
    {
      safeClose(os);
    }
  }
  
  public static byte[] getDataTerminatedBySymbolOrReachEOF(InputStream is, byte symbol)
    throws IOException
  {
    ByteArrayOutputStream os = null;
    try
    {
      os = new ByteArrayOutputStream();
      int bytesRead;
      byte[] arrayOfByte;
      while ((bytesRead = is.read()) >= 0)
      {
        os.write(bytesRead);
        if (bytesRead == symbol) {
          return os.toByteArray();
        }
      }
      return os.toByteArray();
    }
    finally
    {
      safeClose(os);
    }
  }
  
  public static byte[] getDataTerminatedBySymbolOrReachEOF(InputStream is, String symbol, boolean ignoreCase)
    throws IOException
  {
    if (ignoreCase)
    {
      byte[] lowerSymbol = symbol.toLowerCase().getBytes();
      byte[] upperSymbol = symbol.toUpperCase().getBytes();
      ByteArrayOutputStream os = null;
      try
      {
        os = new ByteArrayOutputStream();
        
        int symbolIndex = 0;
        int bytesRead;
        byte[] arrayOfByte1;
        while ((bytesRead = is.read()) >= 0)
        {
          os.write(bytesRead);
          if ((bytesRead != lowerSymbol[symbolIndex]) && (bytesRead != upperSymbol[symbolIndex])) {
            symbolIndex = 0;
          } else {
            symbolIndex++;
          }
          if (symbolIndex >= lowerSymbol.length) {
            return os.toByteArray();
          }
        }
        return os.toByteArray();
      }
      finally
      {
        safeClose(os);
      }
    }
    return getDataTerminatedBySymbolOrReachEOF(is, symbol);
  }
  
  public static byte[] getDataTerminatedBySymbolOrReachEOF(InputStream is, String symbol, long timeOut)
    throws Exception
  {
    return getDataTerminatedBySymbolOrReachEOF(is, symbol.getBytes(), timeOut);
  }
  
  public static byte[] getDataTerminatedBySymbolOrReachEOF(InputStream is, byte[] symbol, long timeOut)
    throws Exception
  {
    ByteArrayOutputStream os = null;
    try
    {
      os = new ByteArrayOutputStream();
      int bytesRead = 0;
      int symbolIndex = 0;
      long expireTime = System.currentTimeMillis() + timeOut;
      do
      {
        byte[] arrayOfByte;
        while ((is.available() > 0) && ((bytesRead = is.read()) >= 0))
        {
          os.write(bytesRead);
          if (bytesRead != symbol[symbolIndex])
          {
            symbolIndex = 0;
            if (bytesRead == symbol[symbolIndex]) {
              symbolIndex++;
            }
          }
          else
          {
            symbolIndex++;
          }
          if (symbolIndex >= symbol.length) {
            return os.toByteArray();
          }
        }
        if (bytesRead < 0) {
          return os.toByteArray();
        }
        Thread.sleep(100L);
      } while (System.currentTimeMillis() < expireTime);
      throw new IOException("Wait '" + new String(symbol) + "' for " + timeOut + " miliseconds was timed out");
    }
    finally
    {
      safeClose(os);
    }
  }
  
  public static byte[] getDataTerminatedBySymbolOrReachEOF(InputStream is, byte symbol, long timeOut)
    throws Exception
  {
    ByteArrayOutputStream os = null;
    try
    {
      os = new ByteArrayOutputStream();
      int bytesRead = 0;
      long expireTime = System.currentTimeMillis() + timeOut;
      do
      {
        byte[] arrayOfByte;
        while ((is.available() > 0) && ((bytesRead = is.read()) >= 0))
        {
          os.write(bytesRead);
          if (bytesRead == symbol) {
            return os.toByteArray();
          }
        }
        if (bytesRead < 0) {
          return os.toByteArray();
        }
        Thread.sleep(100L);
      } while (System.currentTimeMillis() < expireTime);
      throw new IOException("Wait '" + symbol + "' for " + timeOut + " miliseconds was timed out");
    }
    finally
    {
      safeClose(os);
    }
  }
  
  public static byte[] getDataTerminatedBySymbolOrReachEOF(InputStream is, String symbol, long timeOut, boolean ignoreCase)
    throws Exception
  {
    if (ignoreCase)
    {
      ByteArrayOutputStream os = null;
      try
      {
        os = new ByteArrayOutputStream();
        byte[] lowerSymbol = symbol.toLowerCase().getBytes();
        byte[] upperSymbol = symbol.toUpperCase().getBytes();
        byte[] originSymbol = symbol.getBytes();
        int bytesRead = 0;
        int symbolIndex = 0;
        long expireTime = System.currentTimeMillis() + timeOut;
        do
        {
          byte[] arrayOfByte1;
          while ((is.available() > 0) && ((bytesRead = is.read()) >= 0))
          {
            os.write(bytesRead);
            if ((bytesRead != lowerSymbol[symbolIndex]) && (bytesRead != upperSymbol[symbolIndex])) {
              symbolIndex = 0;
            } else {
              symbolIndex++;
            }
            if (symbolIndex >= originSymbol.length) {
              return os.toByteArray();
            }
          }
          if (bytesRead < 0) {
            return os.toByteArray();
          }
          Thread.sleep(100L);
        } while (System.currentTimeMillis() < expireTime);
        throw new IOException("Wait '" + symbol + "' for " + timeOut + " miliseconds was timed out");
      }
      finally
      {
        safeClose(os);
      }
    }
    return getDataTerminatedBySymbolOrReachEOF(is, symbol, timeOut);
  }
  
  public static void skipDataTerminatedBySymbolOrReachEOF(InputStream is, String symbol)
    throws IOException
  {
    skipDataTerminatedBySymbolOrReachEOF(is, symbol.getBytes());
  }
  
  public static void skipDataTerminatedBySymbolOrReachEOF(InputStream is, byte[] symbol)
    throws IOException
  {
    int symbolIndex = 0;
    int bytesRead;
    while ((bytesRead = is.read()) >= 0)
    {
      if (bytesRead != symbol[symbolIndex])
      {
        symbolIndex = 0;
        if (bytesRead == symbol[symbolIndex]) {
          symbolIndex++;
        }
      }
      else
      {
        symbolIndex++;
      }
      if (symbolIndex >= symbol.length) {}
    }
  }
  
  public static void skipDataTerminatedBySymbolOrReachEOF(InputStream is, byte symbol)
    throws IOException
  {
    int bytesRead;
    while ((bytesRead = is.read()) >= 0) {
      if (bytesRead == symbol) {}
    }
  }
  
  public static void skipDataTerminatedBySymbolOrReachEOF(InputStream is, String symbol, boolean ignoreCase)
    throws IOException
  {
    if (ignoreCase)
    {
      byte[] lowerSymbol = symbol.toLowerCase().getBytes();
      byte[] upperSymbol = symbol.toUpperCase().getBytes();
      
      int symbolIndex = 0;
      int bytesRead;
      while ((bytesRead = is.read()) >= 0)
      {
        if ((bytesRead != lowerSymbol[symbolIndex]) && (bytesRead != upperSymbol[symbolIndex])) {
          symbolIndex = 0;
        } else {
          symbolIndex++;
        }
        if (symbolIndex >= lowerSymbol.length) {
          return;
        }
      }
    }
    else
    {
      skipDataTerminatedBySymbolOrReachEOF(is, symbol);
    }
  }
  
  public static void skipDataTerminatedBySymbolOrReachEOF(InputStream is, String symbol, long timeOut)
    throws Exception
  {
    skipDataTerminatedBySymbolOrReachEOF(is, symbol.getBytes(), timeOut);
  }
  
  public static void skipDataTerminatedBySymbolOrReachEOF(InputStream is, byte[] symbol, long timeOut)
    throws Exception
  {
    int bytesRead = 0;
    int symbolIndex = 0;
    long expireTime = System.currentTimeMillis() + timeOut;
    do
    {
      while ((is.available() > 0) && ((bytesRead = is.read()) >= 0))
      {
        if (bytesRead != symbol[symbolIndex])
        {
          symbolIndex = 0;
          if (bytesRead == symbol[symbolIndex]) {
            symbolIndex++;
          }
        }
        else
        {
          symbolIndex++;
        }
        if (symbolIndex >= symbol.length) {
          return;
        }
      }
      if (bytesRead < 0) {
        return;
      }
      Thread.sleep(100L);
    } while (System.currentTimeMillis() < expireTime);
    throw new IOException("Wait '" + new String(symbol) + "' for " + timeOut + " miliseconds was timed out");
  }
  
  public static void skipDataTerminatedBySymbolOrReachEOF(InputStream is, byte symbol, long timeOut)
    throws Exception
  {
    int bytesRead = 0;
    long expireTime = System.currentTimeMillis() + timeOut;
    do
    {
      while ((is.available() > 0) && ((bytesRead = is.read()) >= 0)) {
        if (bytesRead == symbol) {
          return;
        }
      }
      if (bytesRead < 0) {
        return;
      }
      Thread.sleep(100L);
    } while (System.currentTimeMillis() < expireTime);
    throw new IOException("Wait '" + symbol + "' for" + " " + timeOut + " miliseconds was timed out");
  }
  
  public static void skipDataTerminatedBySymbolOrReachEOF(InputStream is, String symbol, long timeOut, boolean ignoreCase)
    throws Exception
  {
    if (ignoreCase)
    {
      byte[] lowerSymbol = symbol.toLowerCase().getBytes();
      byte[] upperSymbol = symbol.toUpperCase().getBytes();
      byte[] rawSymbol = symbol.getBytes();
      int bytesRead = 0;
      int symbolIndex = 0;
      long expireTime = System.currentTimeMillis() + timeOut;
      do
      {
        while ((is.available() > 0) && ((bytesRead = is.read()) >= 0))
        {
          if ((bytesRead != lowerSymbol[symbolIndex]) && (bytesRead != upperSymbol[symbolIndex])) {
            symbolIndex = 0;
          } else {
            symbolIndex++;
          }
          if (symbolIndex >= rawSymbol.length) {
            return;
          }
        }
        if (bytesRead < 0) {
          return;
        }
        Thread.sleep(100L);
      } while (System.currentTimeMillis() < expireTime);
      throw new IOException("Wait '" + symbol + "' for " + timeOut + " miliseconds was timed out");
    }
    skipDataTerminatedBySymbolOrReachEOF(is, symbol, timeOut);
  }
  
  public static void safeClose(InputStream is)
  {
    try
    {
      if (is != null) {
        is.close();
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  
  public static void safeClose(OutputStream os)
  {
    try
    {
      if (os != null) {
        os.close();
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  
  public static void copy(InputStream is, OutputStream os)
    throws IOException
  {
    byte[] data = new byte[65536];
    int length;
    while ((length = is.read(data)) != -1) {
      os.write(data, 0, length);
    }
  }
}
