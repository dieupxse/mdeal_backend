package com.ftl.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class StringEscapeUtil
{
  public static String escapeUrl(String source)
  {
    if (source == null) {
      return null;
    }
    try
    {
      return URLEncoder.encode(source, Global.ENCODING);
    }
    catch (UnsupportedEncodingException e) {}
    return null;
  }
  
  public static String unescapeUrl(String source)
  {
    if (source == null) {
      return null;
    }
    try
    {
      return URLDecoder.decode(source, Global.ENCODING);
    }
    catch (UnsupportedEncodingException e) {}
    return null;
  }
  
  public static String escapeJava(String source)
  {
    return escapeJavaStyleString(source, false);
  }
  
  public static byte[] escapeJava(byte[] source)
  {
    return escapeJavaStyleString(source, false);
  }
  
  public static void escapeJava(Writer out, String source)
    throws IOException
  {
    escapeJavaStyleString(out, source, false);
  }
  
  public static String escapeJavaScript(String source)
  {
    return escapeJavaStyleString(source, true);
  }
  
  public static String escapeJs(String source)
  {
    return escapeJavaStyleString(source, true);
  }
  
  public static String escapeJsXml(String source)
  {
    return escapeXml(escapeJavaStyleString(source, true));
  }
  
  public static String escapeXmlJs(String source)
  {
    return escapeJavaStyleString(escapeXml(source), true);
  }
  
  public static void escapeJavaScript(Writer out, String source)
    throws IOException
  {
    escapeJavaStyleString(out, source, true);
  }
  
  private static String escapeJavaStyleString(String source, boolean escapeSingleQuotes)
  {
    if (source == null) {
      return null;
    }
    try
    {
      StringPrintWriter writer = new StringPrintWriter(source.length() * 2);
      escapeJavaStyleString(writer, source, escapeSingleQuotes);
      return writer.getString();
    }
    catch (IOException ioe)
    {
      ioe.printStackTrace();
    }
    return null;
  }
  
  private static byte[] escapeJavaStyleString(byte[] source, boolean escapeSingleQuotes)
  {
    if (source == null) {
      return null;
    }
    ByteArrayOutputStream writer = null;
    try
    {
      writer = new ByteArrayOutputStream();
      escapeJavaStyleString(writer, source, escapeSingleQuotes);
      return writer.toByteArray();
    }
    catch (IOException ioe)
    {
      ioe.printStackTrace();
      return null;
    }
    finally
    {
      StreamUtil.safeClose(writer);
    }
  }
  
  private static void escapeJavaStyleString(Writer out, String source, boolean escapeSingleQuote)
    throws IOException
  {
    if (out == null) {
      throw new IllegalArgumentException("The Writer must not be null");
    }
    if (source == null) {
      return;
    }
    int sz = source.length();
    for (int i = 0; i < sz; i++)
    {
      char ch = source.charAt(i);
      if (ch < ' ') {
        switch (ch)
        {
        case '\b': 
          out.write(92);
          out.write(98);
          break;
        case '\n': 
          out.write(92);
          out.write(110);
          break;
        case '\t': 
          out.write(92);
          out.write(116);
          break;
        case '\f': 
          out.write(92);
          out.write(102);
          break;
        case '\r': 
          out.write(92);
          out.write(114);
          break;
        case '\013': 
        default: 
          if (ch > '\017') {
            out.write("\\u00" + hex(ch));
          } else {
            out.write("\\u000" + hex(ch));
          }
          break;
        }
      } else {
        switch (ch)
        {
        case '\'': 
          if (escapeSingleQuote) {
            out.write(92);
          }
          out.write(39);
          break;
        case '"': 
          out.write(92);
          out.write(34);
          break;
        case '\\': 
          out.write(92);
          out.write(92);
          break;
        default: 
          out.write(ch);
        }
      }
    }
  }
  
  private static void escapeJavaStyleString(ByteArrayOutputStream out, byte[] source, boolean escapeSingleQuote)
    throws IOException
  {
    if (out == null) {
      throw new IllegalArgumentException("The Writer must not be null");
    }
    if (source == null) {
      return;
    }
    int sz = source.length;
    for (int i = 0; i < sz; i++)
    {
      byte ch = source[i];
      if (ch < 32) {
        switch (ch)
        {
        case 8: 
          out.write(92);
          out.write(98);
          break;
        case 10: 
          out.write(92);
          out.write(110);
          break;
        case 9: 
          out.write(92);
          out.write(116);
          break;
        case 12: 
          out.write(92);
          out.write(102);
          break;
        case 13: 
          out.write(92);
          out.write(114);
          break;
        case 11: 
        default: 
          out.write(ch);
          
          break;
        }
      } else {
        switch (ch)
        {
        case 39: 
          if (escapeSingleQuote) {
            out.write(92);
          }
          out.write(39);
          break;
        case 34: 
          out.write(92);
          out.write(34);
          break;
        case 92: 
          out.write(92);
          out.write(92);
          break;
        default: 
          out.write(ch);
        }
      }
    }
  }
  
  private static String hex(char ch)
  {
    return Integer.toHexString(ch).toUpperCase();
  }
  
  private static String hex(byte source)
  {
    return Integer.toHexString(source).toUpperCase();
  }
  
  public static String unescapeJava(String source)
  {
    if (source == null) {
      return null;
    }
    try
    {
      StringPrintWriter writer = new StringPrintWriter(source.length());
      unescapeJava(writer, source);
      return writer.getString();
    }
    catch (IOException ioe)
    {
      ioe.printStackTrace();
    }
    return null;
  }
  
  public static byte[] unescapeJava(byte[] source)
  {
    if (source == null) {
      return null;
    }
    ByteArrayOutputStream writer = null;
    try
    {
      writer = new ByteArrayOutputStream();
      unescapeJava(writer, source);
      return writer.toByteArray();
    }
    catch (IOException ioe)
    {
      ioe.printStackTrace();
      return null;
    }
    finally
    {
      StreamUtil.safeClose(writer);
    }
  }
  
  public static void unescapeJava(Writer out, String source)
    throws IOException
  {
    if (out == null) {
      throw new IllegalArgumentException("The Writer must not be null");
    }
    if (source == null) {
      return;
    }
    int sz = source.length();
    StringBuilder unicode = new StringBuilder(4);
    boolean hadSlash = false;
    boolean inUnicode = false;
    for (int i = 0; i < sz; i++)
    {
      char ch = source.charAt(i);
      if (inUnicode)
      {
        unicode.append(ch);
        if (unicode.length() == 4) {
          try
          {
            int value = Integer.parseInt(unicode.toString(), 16);
            out.write((char)value);
            unicode.setLength(0);
            inUnicode = false;
            hadSlash = false;
          }
          catch (NumberFormatException nfe)
          {
            throw new IOException("Unable to parse unicode value: " + unicode);
          }
        }
      }
      else if (hadSlash)
      {
        hadSlash = false;
        switch (ch)
        {
        case '\\': 
          out.write(92);
          break;
        case '\'': 
          out.write(39);
          break;
        case '"': 
          out.write(34);
          break;
        case 'r': 
          out.write(13);
          break;
        case 'f': 
          out.write(12);
          break;
        case 't': 
          out.write(9);
          break;
        case 'n': 
          out.write(10);
          break;
        case 'b': 
          out.write(8);
          break;
        case 'u': 
          inUnicode = true;
          break;
        default: 
          out.write(ch);
          break;
        }
      }
      else if (ch == '\\')
      {
        hadSlash = true;
      }
      else
      {
        out.write(ch);
      }
    }
    if (hadSlash) {
      out.write(92);
    }
  }
  
  public static void unescapeJava(ByteArrayOutputStream out, byte[] source)
    throws IOException
  {
    if (out == null) {
      throw new IllegalArgumentException("The Writer must not be null");
    }
    if (source == null) {
      return;
    }
    int sz = source.length;
    StringBuilder unicode = new StringBuilder(4);
    boolean hadSlash = false;
    boolean inUnicode = false;
    for (int i = 0; i < sz; i++)
    {
      byte ch = source[i];
      if (inUnicode)
      {
        unicode.append(ch);
        if (unicode.length() == 4) {
          try
          {
            int value = Integer.parseInt(unicode.toString(), 16);
            out.write(value);
            unicode.setLength(0);
            inUnicode = false;
            hadSlash = false;
          }
          catch (NumberFormatException nfe)
          {
            throw new IOException("Unable to parse unicode value: " + unicode);
          }
        }
      }
      else if (hadSlash)
      {
        hadSlash = false;
        switch (ch)
        {
        case 92: 
          out.write(92);
          break;
        case 39: 
          out.write(39);
          break;
        case 34: 
          out.write(34);
          break;
        case 114: 
          out.write(13);
          break;
        case 102: 
          out.write(12);
          break;
        case 116: 
          out.write(9);
          break;
        case 110: 
          out.write(10);
          break;
        case 98: 
          out.write(8);
          break;
        case 117: 
          inUnicode = true;
          break;
        default: 
          out.write(ch);
          break;
        }
      }
      else if (ch == 92)
      {
        hadSlash = true;
      }
      else
      {
        out.write(ch);
      }
    }
    if (hadSlash) {
      out.write(92);
    }
  }
  
  public static String unescapeJavaScriptString(String source)
  {
    if (source == null) {
      return null;
    }
    try
    {
      StringPrintWriter writer = new StringPrintWriter(source.length());
      unescapeJavaScriptString(writer, source);
      return writer.getString();
    }
    catch (IOException ioe)
    {
      ioe.printStackTrace();
    }
    return null;
  }
  
  public static byte[] unescapeJavaScriptString(byte[] source)
  {
    if (source == null) {
      return null;
    }
    ByteArrayOutputStream writer = null;
    try
    {
      writer = new ByteArrayOutputStream();
      unescapeJavaScriptString(writer, source);
      return writer.toByteArray();
    }
    catch (IOException ioe)
    {
      ioe.printStackTrace();
      return null;
    }
    finally
    {
      StreamUtil.safeClose(writer);
    }
  }
  
  public static void unescapeJavaScriptString(Writer out, String source)
    throws IOException
  {
    if (out == null) {
      throw new IllegalArgumentException("The Writer must not be null");
    }
    if (source == null) {
      return;
    }
    int sz = source.length();
    StringBuilder unicode = new StringBuilder(4);
    boolean hadSlash = false;
    boolean inUnicode = false;
    for (int i = 0; i < sz; i++)
    {
      char ch = source.charAt(i);
      if (inUnicode)
      {
        unicode.append(ch);
        if (unicode.length() == 4) {
          try
          {
            int value = Integer.parseInt(unicode.toString(), 16);
            out.write((char)value);
            unicode.setLength(0);
            inUnicode = false;
            hadSlash = false;
          }
          catch (NumberFormatException nfe)
          {
            throw new IOException("Unable to parse unicode value: " + unicode);
          }
        }
      }
      else if (hadSlash)
      {
        hadSlash = false;
        switch (ch)
        {
        case '%': 
          out.write(37);
          break;
        case 'u': 
          inUnicode = true;
          break;
        default: 
          i++;
          StringBuilder buf = new StringBuilder(2);
          buf.append(ch);
          buf.append(source.charAt(i));
          try
          {
            int value = Integer.parseInt(buf.toString(), 16);
            out.write(value);
            hadSlash = false;
          }
          catch (NumberFormatException nfe)
          {
            throw new IOException("Unable to parse unicode value: " + unicode);
          }
        }
      }
      else if (ch == '%')
      {
        hadSlash = true;
      }
      else
      {
        out.write(ch);
      }
    }
    if (hadSlash) {
      out.write(37);
    }
  }
  
  public static void unescapeJavaScriptString(ByteArrayOutputStream out, byte[] source)
    throws IOException
  {
    if (out == null) {
      throw new IllegalArgumentException("The Writer must not be null");
    }
    if (source == null) {
      return;
    }
    int sz = source.length;
    StringBuilder unicode = new StringBuilder(4);
    boolean hadSlash = false;
    boolean inUnicode = false;
    for (int i = 0; i < sz; i++)
    {
      byte ch = source[i];
      if (inUnicode)
      {
        unicode.append(ch);
        if (unicode.length() == 4) {
          try
          {
            int value = Integer.parseInt(unicode.toString(), 16);
            out.write(value);
            unicode.setLength(0);
            inUnicode = false;
            hadSlash = false;
          }
          catch (NumberFormatException nfe)
          {
            throw new IOException("Unable to parse unicode value: " + unicode);
          }
        }
      }
      else if (hadSlash)
      {
        hadSlash = false;
        switch (ch)
        {
        case 37: 
          out.write(37);
          break;
        case 117: 
          inUnicode = true;
          break;
        default: 
          i++;
          byte[] bytes = new byte[2];
          bytes[0] = ch;
          bytes[1] = source[i];
          try
          {
            int value = Integer.parseInt(new String(bytes), 16);
            out.write(value);
            hadSlash = false;
          }
          catch (NumberFormatException nfe)
          {
            throw new IOException("Unable to parse unicode value: " + unicode);
          }
        }
      }
      else if (ch == 37)
      {
        hadSlash = true;
      }
      else
      {
        out.write(ch);
      }
    }
    if (hadSlash) {
      out.write(37);
    }
  }
  
  public static String unescapeJavaScript(String source)
  {
    return unescapeJava(source);
  }
  
  public static void unescapeJavaScript(Writer out, String source)
    throws IOException
  {
    unescapeJava(out, source);
  }
  
  public static String escapeHtml(String source)
  {
    if (source == null) {
      return null;
    }
    return Entities.HTML40.escape(source);
  }
  
  public static String unescapeHtml(String source)
  {
    if (source == null) {
      return null;
    }
    return Entities.HTML40.unescape(source);
  }
  
  public static String escapeXml(String source)
  {
    if (source == null) {
      return null;
    }
    return Entities.XML.escape(source);
  }
  
  public static String unescapeXml(String source)
  {
    if (source == null) {
      return null;
    }
    return Entities.XML.unescape(source);
  }
  
  public static String escapeSql(String source)
  {
    if (source == null) {
      return null;
    }
    return StringUtil.replaceAll(source, "'", "''");
  }
  
  public static byte[] escapeByteArray(byte[] source, byte keepSymbol, byte escapeSymbol)
  {
    if (source == null) {
      return source;
    }
    ByteArrayOutputStream os = null;
    try
    {
      os = new ByteArrayOutputStream(source.length);
      
      int length = source.length;
      for (int i = 0; i < length; i++)
      {
        byte value = source[i];
        if ((value == escapeSymbol) || (value == keepSymbol)) {
          os.write(escapeSymbol);
        }
        os.write(value);
      }
      return os.toByteArray();
    }
    finally
    {
      StreamUtil.safeClose(os);
    }
  }
  
  public static byte[] unescapeByteArray(byte[] source, byte keepSymbol, byte escapeSymbol)
  {
    if (source == null) {
      return source;
    }
    ByteArrayOutputStream os = null;
    try
    {
      os = new ByteArrayOutputStream(source.length);
      
      int length = source.length;
      boolean hadEscape = false;
      for (int i = 0; i < length; i++)
      {
        byte value = source[i];
        if (value == escapeSymbol)
        {
          if (!hadEscape)
          {
            hadEscape = true;
          }
          else
          {
            hadEscape = false;
            os.write(value);
          }
        }
        else if (value == keepSymbol)
        {
          if (hadEscape) {
            hadEscape = false;
          }
          os.write(value);
        }
        else
        {
          if (hadEscape)
          {
            os.write(escapeSymbol);
            hadEscape = false;
          }
          os.write(value);
        }
      }
      return os.toByteArray();
    }
    finally
    {
      StreamUtil.safeClose(os);
    }
  }
  
  public static String escapeJavaIgnoreWideCharacter(String source)
  {
    return escapeJavaStyleStringIgnoreWideCharacter(source, false);
  }
  
  public static byte[] escapeJavaIgnoreWideCharacter(byte[] source)
  {
    return escapeJavaStyleStringIgnoreWideCharacter(source, false);
  }
  
  private static String escapeJavaStyleStringIgnoreWideCharacter(String source, boolean escapeSingleQuotes)
  {
    if (source == null) {
      return null;
    }
    try
    {
      StringPrintWriter writer = new StringPrintWriter(source.length() * 2);
      escapeJavaStyleStringIgnoreWideCharacter(writer, source, escapeSingleQuotes);
      return writer.getString();
    }
    catch (IOException ioe)
    {
      ioe.printStackTrace();
    }
    return null;
  }
  
  private static byte[] escapeJavaStyleStringIgnoreWideCharacter(byte[] source, boolean escapeSingleQuotes)
  {
    if (source == null) {
      return null;
    }
    ByteArrayOutputStream writer = null;
    try
    {
      writer = new ByteArrayOutputStream();
      escapeJavaStyleStringIgnoreWideCharacter(writer, source, escapeSingleQuotes);
      return writer.toByteArray();
    }
    catch (IOException ioe)
    {
      ioe.printStackTrace();
      return null;
    }
    finally
    {
      StreamUtil.safeClose(writer);
    }
  }
  
  private static void escapeJavaStyleStringIgnoreWideCharacter(Writer out, String source, boolean escapeSingleQuote)
    throws IOException
  {
    if (out == null) {
      throw new IllegalArgumentException("The Writer must not be null");
    }
    if (source == null) {
      return;
    }
    int sz = source.length();
    for (int i = 0; i < sz; i++)
    {
      char ch = source.charAt(i);
      if (ch < ' ') {
        switch (ch)
        {
        case '\b': 
          out.write(92);
          out.write(98);
          break;
        case '\n': 
          out.write(92);
          out.write(110);
          break;
        case '\t': 
          out.write(92);
          out.write(116);
          break;
        case '\f': 
          out.write(92);
          out.write(102);
          break;
        case '\r': 
          out.write(92);
          out.write(114);
          break;
        case '\013': 
        default: 
          out.write(ch);
          break;
        }
      } else {
        switch (ch)
        {
        case '\'': 
          if (escapeSingleQuote) {
            out.write(92);
          }
          out.write(39);
          break;
        case '"': 
          out.write(92);
          out.write(34);
          break;
        case '\\': 
          out.write(92);
          out.write(92);
          break;
        default: 
          out.write(ch);
        }
      }
    }
  }
  
  private static void escapeJavaStyleStringIgnoreWideCharacter(ByteArrayOutputStream out, byte[] source, boolean escapeSingleQuote)
    throws IOException
  {
    if (out == null) {
      throw new IllegalArgumentException("The Writer must not be null");
    }
    if (source == null) {
      return;
    }
    int sz = source.length;
    for (int i = 0; i < sz; i++)
    {
      byte ch = source[i];
      if (ch < 32) {
        switch (ch)
        {
        case 8: 
          out.write(92);
          out.write(98);
          break;
        case 10: 
          out.write(92);
          out.write(110);
          break;
        case 9: 
          out.write(92);
          out.write(116);
          break;
        case 12: 
          out.write(92);
          out.write(102);
          break;
        case 13: 
          out.write(92);
          out.write(114);
          break;
        case 11: 
        default: 
          out.write(ch);
          break;
        }
      } else {
        switch (ch)
        {
        case 39: 
          if (escapeSingleQuote) {
            out.write(92);
          }
          out.write(39);
          break;
        case 34: 
          out.write(92);
          out.write(34);
          break;
        case 92: 
          out.write(92);
          out.write(92);
          break;
        default: 
          out.write(ch);
        }
      }
    }
  }
}
