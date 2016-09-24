package com.ftl.util;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

public class UrlUtil
{
  public static final Pattern ROOT_PATTERN = Pattern.compile("/.*|\\\\.*|[a-zA-Z]:/.*|[a-zA-Z]:\\\\.*");
  
  public static long getResourceLastModified(String fileName)
  {
    try
    {
      File file = new File(fileName);
      if (!file.exists()) {
        if (fileName.startsWith("/")) {
          file = new File(Global.RESOURCE_DIR + fileName.substring(1));
        } else {
          file = new File(Global.RESOURCE_DIR + fileName);
        }
      }
      if (file.exists()) {
        return file.lastModified();
      }
    }
    catch (Exception ex) {}
    return 0L;
  }
  
  public static URL getResource(String fileName)
  {
    return getResource(fileName, null);
  }
  
  public static URL getResource(String fileName, LongHolder lastModified)
  {
    try
    {
      File file = new File(fileName);
      if (!file.exists()) {
        if (fileName.startsWith("/")) {
          file = new File(Global.RESOURCE_DIR + fileName.substring(1));
        } else {
          file = new File(Global.RESOURCE_DIR + fileName);
        }
      }
      if (file.exists())
      {
        if (lastModified != null) {
          lastModified.value = file.lastModified();
        }
        return file.toURI().toURL();
      }
      return new URL(fileName);
    }
    catch (Exception e)
    {
      if ((!fileName.startsWith("/")) || (!fileName.startsWith("\\"))) {
        fileName = "/" + fileName;
      }
    }
    return UrlUtil.class.getResource(fileName);
  }
  
  public static String buildQueryString(Map parameterMap)
  {
    return buildQueryString(parameterMap, (List)null);
  }
  
  public static String buildQueryString(Map parameterMap, String[] excludeList)
  {
    Iterator iterator = parameterMap.entrySet().iterator();
    StringBuilder str = new StringBuilder();
    Map.Entry entry;
    boolean exclude;
    while (iterator.hasNext())
    {
      entry = (Map.Entry)iterator.next();
      String key = (String)entry.getKey();
      Object obj = entry.getValue();
      //String val;
      if ((obj instanceof String[]))
      {
        //boolean exclude;
        if (excludeList != null)
        {
          exclude = false;
          for (String excludeValue : excludeList) {
            if (excludeValue.equals(key)) {
              exclude = true;
            }
          }
          if (exclude) {}
        }
        else
        {
          for (String val : (String[])obj) {
            if ((val != null) && (!val.isEmpty()))
            {
              str.append((String)entry.getKey());
              str.append('=');
              try
              {
                str.append(URLEncoder.encode(val, Global.ENCODING));
              }
              catch (UnsupportedEncodingException e)
              {
                e.printStackTrace();
              }
              str.append('&');
            }
          }
        }
      }
      else if ((obj instanceof List))
      {
        List value = (List)obj;
        if (excludeList != null)
        {
          exclude = false;
          String[] arrayOfString2 = excludeList;int localUnsupportedEncodingException1 = arrayOfString2.length;
            int e;
          for (e = 0; e < localUnsupportedEncodingException1; e++)
          {
            String excludeValue = arrayOfString2[e];
            if (excludeValue.equals(key)) {
              exclude = true;
            }
          }
          if (exclude) {}
        }
        else
        {
          for (Object raw : value)
          {
            String val = String.valueOf(raw);
            if ((val != null) && (!val.isEmpty()))
            {
              str.append((String)entry.getKey());
              str.append('=');
              try
              {
                str.append(URLEncoder.encode(val, Global.ENCODING));
              }
              catch (UnsupportedEncodingException e)
              {
                e.printStackTrace();
              }
              str.append('&');
            }
          }
        }
      }
    }
    if (str.length() > 0) {
      str.delete(str.length() - 1, str.length());
    }
    return str.toString();
  }
  
  public static String buildQueryString(Map parameterMap, List excludeList)
  {
    Iterator iterator = parameterMap.entrySet().iterator();
    StringBuilder str = new StringBuilder();
    Map.Entry entry;
    while (iterator.hasNext())
    {
      entry = (Map.Entry)iterator.next();
      String key = (String)entry.getKey();
      if ((excludeList == null) || 
      
        (excludeList.indexOf(key) < 0))
      {
        Object obj = entry.getValue();
        if ((obj instanceof String[]))
        {
          for (String val : (String[])obj)
          {
            str.append((String)entry.getKey());
            str.append('=');
            try
            {
              if ((val != null) && (!val.isEmpty())) {
                str.append(URLEncoder.encode(val, Global.ENCODING));
              }
            }
            catch (UnsupportedEncodingException e)
            {
              e.printStackTrace();
            }
            str.append('&');
          }
        }
        else if ((obj instanceof List))
        {
          List value = (List)obj;
          for (Object raw : value)
          {
            str.append((String)entry.getKey());
            str.append('=');
            try
            {
              String val = String.valueOf(raw);
              if ((val != null) && (!val.isEmpty())) {
                str.append(URLEncoder.encode(val, Global.ENCODING));
              }
            }
            catch (UnsupportedEncodingException e)
            {
              e.printStackTrace();
            }
            str.append('&');
          }
        }
      }
    }
    if (str.length() > 0) {
      str.delete(str.length() - 1, str.length());
    }
    return str.toString();
  }
  
  public static String readUrlConnectionResponse(URLConnection conn)
    throws Exception
  {
    InputStream is = conn.getInputStream();Throwable localThrowable3 = null;
    try
    {
      String contentType = conn.getContentType();
      String[] values = contentType.split(";");
      String charset = null;
      for (String value : values)
      {
        value = value.trim();
        if (value.toLowerCase().startsWith("charset=")) {
          charset = value.substring("charset=".length());
        }
      }
      if ((charset == null) || (charset.isEmpty())) {
        charset = StandardCharsets.ISO_8859_1.name();
      }
      return StreamUtil.readStream(is, charset);
    }
    catch (Throwable localThrowable1)
    {
      localThrowable3 = localThrowable1;throw localThrowable1;
    }
    finally
    {
      if (is != null) {
        if (localThrowable3 != null) {
          try
          {
            is.close();
          }
          catch (Throwable localThrowable2)
          {
            localThrowable3.addSuppressed(localThrowable2);
          }
        } else {
          is.close();
        }
      }
    }
  }
  
  public static String sendPost(String host, String[] keyList, String[] valueList)
    throws Exception
  {
    StringBuilder data = new StringBuilder();
    for (int index = 0; index < keyList.length; index++)
    {
      if (index > 0) {
        data.append('&');
      }
      data.append(URLEncoder.encode(keyList[index], "UTF-8"));
      data.append('=');
      data.append(URLEncoder.encode(valueList[index], "UTF-8"));
    }
    return sendPost(host, data.toString());
  }
  
  public static String sendPost(String host, String requestData)
    throws Exception
  {
    return sendPost(new URL(host).openConnection(), requestData);
  }
  
  public static String sendPost(URLConnection conn, String requestData)
    throws Exception
  {
    conn.setDoOutput(true);
    OutputStream os = null;
    OutputStreamWriter wr = null;
    try
    {
      os = conn.getOutputStream();
      wr = new OutputStreamWriter(os);
      wr.write(requestData);
      wr.flush();
    }
    finally
    {
      if (wr != null) {
        wr.close();
      }
      StreamUtil.safeClose(os);
    }
    return readUrlConnectionResponse(conn);
  }
  
  public static String getContent(String url)
    throws Exception
  {
    return readUrlConnectionResponse(new URL(url).openConnection());
  }
  
  public static String getContent(String url, String proxyIp, int proxyPort)
    throws Exception
  {
    Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyIp, proxyPort));
    return readUrlConnectionResponse(new URL(url).openConnection(proxy));
  }
}
