package com.ftl.util;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class StringUtil
{
  public static final int ALIGN_CENTER = 0;
  public static final int ALIGN_LEFT = 1;
  public static final int ALIGN_RIGHT = 2;
  static final int[] hexDecode = new int['?'];
  static final char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
  
  static
  {
    for (int i = 0; i < hexDecode.length; i++) {
      hexDecode[i] = -1;
    }
    for (int i = 48; i <= 57; i++) {
      hexDecode[i] = (i - 48);
    }
    for (int i = 65; i <= 70; i++) {
      hexDecode[i] = (i - 65 + 10);
    }
    for (int i = 97; i <= 102; i++) {
      hexDecode[i] = (i - 97 + 10);
    }
  }
  
  protected static int getHex(char c)
  {
    int x = hexDecode[c];
    if (x < 0) {
      throw new NumberFormatException("Bad hex digit " + c);
    }
    return x;
  }
  
  public static String byteArrayToBcdString(byte[] b)
  {
    return byteArrayToBcdString(b, 0, b.length);
  }
  
  public static String byteArrayToBcdString(byte[] b, int offset, int length)
  {
    StringBuilder buf = new StringBuilder(length * 2);
    for (int i = offset; i < length; i++)
    {
      buf.append((char)(((b[i] & 0xF0) >> 4) + 48));
      if ((i != length) && ((b[i] & 0xF) != 10)) {
        buf.append((char)((b[i] & 0xF) + 48));
      }
    }
    return buf.toString();
  }
  
  public static String byteArrayToHexString(byte[] b)
  {
    return byteArrayToHexString(b, 0, b.length);
  }
  
  public static String byteArrayToHexString(byte[] b, int offset, int length)
  {
    char[] buf = new char[length * 2];
    int j = 0;
    for (int i = offset; i < length; i++)
    {
      buf[(j++)] = hexDigits[((b[i] & 0xF0) >> 4)];
      buf[(j++)] = hexDigits[(b[i] & 0xF)];
    }
    return new String(buf);
  }
  
  
  
  public static int byteArrayToInt(byte[] data)
  {
    return byteArrayToInt(data, 0, data.length);
  }
  
  public static int byteArrayToInt(byte[] data, int offset, int length)
  {
    int lastOffset = offset + length;
    if ((offset < 0) || (data.length < lastOffset) || (length < 1)) {
      throw new IllegalArgumentException("Invalid offset/length " + offset + "/" + length);
    }
    int value = 0;
    for (int index = offset; index < lastOffset; index++)
    {
      value <<= 8;
      value |= data[index] & 0xFF;
    }
    return value;
  }
  
  public static long byteArrayToLong(byte[] data)
  {
    return byteArrayToLong(data, 0, data.length);
  }
  
  public static long byteArrayToLong(byte[] data, int offset, int length)
  {
    int lastOffset = offset + length;
    if ((offset < 0) || (data.length < lastOffset) || (length < 1)) {
      throw new IllegalArgumentException("Invalid offset/length " + offset + "/" + length);
    }
    long value = 0L;
    for (int index = offset; index < lastOffset; index++)
    {
      value <<= 8;
      value |= data[index] & 0xFF;
    }
    return value;
  }
  
  public static byte[] hexStringToByteArray(String s)
  {
    int max = s.length();
    int odd = max & 0x1;
    byte[] buf = new byte[max / 2 + odd];
    int i = 0;int j = 0;
    if (odd == 1) {
      buf[(j++)] = ((byte)getHex(s.charAt(i++)));
    }
    while (i < max) {
      buf[(j++)] = ((byte)(getHex(s.charAt(i++)) << 4 | getHex(s.charAt(i++))));
    }
    return buf;
  }
  
  
  
  public static byte[] bcdStringToByteArray(String s)
  {
    int i = 0;int j = 0;
    int max = s.length() - s.length() % 2;
    byte[] buf = new byte[(s.length() + s.length() % 2) / 2];
    while (i < max) {
      buf[(j++)] = ((byte)(s.charAt(i++) - '0' << 4 | s.charAt(i++) - '0'));
    }
    if (Math.abs(s.length() % 2) == 1) {
      buf[j] = ((byte)(s.charAt(i++) - '0' << 4 | 0xA));
    }
    return buf;
  }
  
  public static byte[] intToByteArray(int value)
  {
    if (value < 0) {
      throw new IllegalArgumentException("Can not encode negative number");
    }
    if (value == 0) {
      return new byte[1];
    }
    byte[] data = new byte[4];
    int index = 0;
    while (value > 0)
    {
      data[(index++)] = ((byte)(value & 0xFF));
      value >>= 8;
    }
    int count = index;
    byte[] result = new byte[count];
    while (index > 0) {
      result[(count - index--)] = data[index];
    }
    return result;
  }
  
  public static byte[] longToByteArray(long value)
  {
    if (value < 0L) {
      throw new IllegalArgumentException("Can not encode negative number");
    }
    if (value == 0L) {
      return new byte[1];
    }
    byte[] data = new byte[8];
    int index = 0;
    while (value > 0L)
    {
      data[(index++)] = ((byte)(int)(value & 0xFF));
      value >>= 8;
    }
    int count = index;
    byte[] result = new byte[count];
    while (index > 0) {
      result[(count - index--)] = data[index];
    }
    return result;
  }
  
  public static String format(Date date, String pattern)
  {
    if (date == null) {
      return null;
    }
    SimpleDateFormat fmt = new SimpleDateFormat(pattern);
    return fmt.format(date);
  }
  
  public static String format(long number, String pattern)
  {
    DecimalFormat fmt = new DecimalFormat(pattern);
    return fmt.format(number);
  }
  
  public static String format(double number, String pattern)
  {
    DecimalFormat fmt = new DecimalFormat(pattern);
    return fmt.format(number);
  }
  
  public static String format(Number number, String pattern)
  {
    if (number == null) {
      return "";
    }
    return format(number.doubleValue(), pattern);
  }
  
  public static String replaceAll(String source, String find, String replace)
  {
    if ((find == null) || (find.length() == 0)) {
      return source;
    }
    int offset = 0;
    int lastOffset = 0;
    StringBuilder result = new StringBuilder();
    while ((offset = source.indexOf(find, offset)) >= 0)
    {
      result.append(source.substring(lastOffset, offset));
      result.append(replace);
      offset += find.length();
      lastOffset = offset;
    }
    result.append(source.substring(lastOffset, source.length()));
    return result.toString();
  }
  
  public static String replaceAll(String source, String find, Collection<String> replaceList)
  {
    if ((find == null) || (find.length() == 0) || (replaceList == null) || 
      (replaceList.isEmpty())) {
      return source;
    }
    int offset = 0;
    int lastOffset = 0;
    StringBuilder result = new StringBuilder();
    for (String replace : replaceList)
    {
      if ((offset = source.indexOf(find, offset)) < 0) {
        break;
      }
      result.append(source.substring(lastOffset, offset));
      result.append(replace);
      offset += find.length();
      lastOffset = offset;
    }
    result.append(source.substring(lastOffset, source.length()));
    return result.toString();
  }
  
  public static String replaceAll(String source, String find, String replace, int maxNumberOfReplacement)
  {
    int offset = 0;
    if ((find == null) || (find.length() == 0)) {
      return source;
    }
    int lastOffset = 0;
    int count = 0;
    StringBuilder result = new StringBuilder();
    while (((offset = source.indexOf(find, offset)) >= 0) && (count < maxNumberOfReplacement))
    {
      result.append(source.substring(lastOffset, offset));
      result.append(replace);
      count++;
      offset += find.length();
      lastOffset = offset;
    }
    result.append(source.substring(lastOffset, source.length()));
    return result.toString();
  }
  
  public static String replaceAll(String source, String find, String[] replaces)
  {
    int offset = 0;
    if ((find == null) || (find.length() == 0)) {
      return source;
    }
    int lastOffset = 0;
    int count = 0;
    StringBuilder result = new StringBuilder();
    while (((offset = source.indexOf(find, offset)) >= 0) && (count < replaces.length))
    {
      result.append(source.substring(lastOffset, offset));
      result.append(replaces[count]);
      count++;
      offset += find.length();
      lastOffset = offset;
    }
    result.append(source.substring(lastOffset, source.length()));
    return result.toString();
  }
  
  public static String replaceAllIgnoreCase(String source, String find, String replace)
  {
    if ((find == null) || (find.length() == 0)) {
      return source;
    }
    String sourceUpper = source.toUpperCase();
    find = find.toUpperCase();
    
    int offset = 0;
    int lastOffset = 0;
    StringBuilder result = new StringBuilder();
    while ((offset = sourceUpper.indexOf(find, offset)) >= 0)
    {
      result.append(source.substring(lastOffset, offset));
      result.append(replace);
      offset += find.length();
      lastOffset = offset;
    }
    result.append(source.substring(lastOffset, source.length()));
    return result.toString();
  }
  
  public static String nvl(Object input, String nullValue)
  {
    if (input == null) {
      return nullValue;
    }
    return input.toString();
  }
  
  public static String replaceAll(String source, Map map)
  {
    if ((source == null) || (source.isEmpty())) {
      return source;
    }
    Iterator iterator = map.entrySet().iterator();
    while (iterator.hasNext())
    {
      Map.Entry entry = (Map.Entry)iterator.next();
      Object key = entry.getKey();
      if ((key instanceof String))
      {
        Object value = entry.getValue();
        if ((value instanceof String)) {
          source = replaceAll(source, "$" + (String)key + "$", (String)value);
        } else if ((value instanceof Date)) {
          source = replaceAll(source, "$" + (String)key + "$", 
            format((Date)value, Global.DATE_FORMAT));
        }
      }
    }
    return source;
  }
  
  public static String replaceAll(String source, Map map, String[] keys)
  {
    if ((source == null) || (source.isEmpty())) {
      return source;
    }
    for (String key : keys)
    {
      Object value = map.get(key);
      if ((value instanceof String)) {
        source = replaceAll(source, "$" + key + "$", (String)value);
      } else if ((value instanceof Date)) {
        source = replaceAll(source, "$" + key + "$", 
          format((Date)value, Global.DATE_FORMAT));
      }
    }
    return source;
  }
  
  public static String nvlEx(Object input, String nullValue)
  {
    if ((input == null) || (input.equals(""))) {
      return nullValue;
    }
    return input.toString();
  }
  
  public static int indexOfLetter(String source, int offset)
  {
    while (offset < source.length())
    {
      char c = source.charAt(offset);
      if (c > ' ') {
        return offset;
      }
      offset++;
    }
    return -1;
  }
  
  public static int indexOfLetter(String source, int offset, String additionWhitespaceSymbol)
  {
    while (offset < source.length())
    {
      char c = source.charAt(offset);
      if ((c > ' ') && (additionWhitespaceSymbol.indexOf(c) < 0)) {
        return offset;
      }
      offset++;
    }
    return -1;
  }
  
  public static int indexOfSpace(String source, int offset)
  {
    while (offset < source.length())
    {
      char c = source.charAt(offset);
      if (c > ' ') {
        offset++;
      } else {
        return offset;
      }
    }
    return -1;
  }
  
  public static int indexOfSpace(String source, int offset, String additionWhitespaceSymbol)
  {
    while (offset < source.length())
    {
      char c = source.charAt(offset);
      if ((c > ' ') && (additionWhitespaceSymbol.indexOf(c) < 0)) {
        offset++;
      } else {
        return offset;
      }
    }
    return -1;
  }
  
  public static int countSymbol(String source, String symbol, int offset)
  {
    if ((symbol == null) || (symbol.length() == 0)) {
      return 0;
    }
    int count = 0;
    while ((offset = source.indexOf(symbol, offset) + 1) > 0) {
      count++;
    }
    return count;
  }
  
  public static int countSymbol(String source, char symbol, int offset)
  {
    int count = 0;
    for (int index = offset; index < source.length(); index++) {
      if (source.charAt(index) == symbol) {
        count++;
      }
    }
    return count;
  }
  
  public static int countLetter(String source, int offset)
  {
    int count = 0;
    for (int index = offset; index < source.length(); index++)
    {
      char c = source.charAt(index);
      if (((c >= 'A') && (c <= 'Z')) || ((c >= 'a') && (c <= 'z'))) {
        count++;
      }
    }
    return count;
  }
  
  public static int countUppercaseLetter(String source, int offset)
  {
    int count = 0;
    for (int index = offset; index < source.length(); index++)
    {
      char c = source.charAt(index);
      if ((c >= 'A') && (c <= 'Z')) {
        count++;
      }
    }
    return count;
  }
  
  public static int countLowercaseLetter(String source, int offset)
  {
    int count = 0;
    for (int index = offset; index < source.length(); index++)
    {
      char c = source.charAt(index);
      if ((c >= 'a') && (c <= 'z')) {
        count++;
      }
    }
    return count;
  }
  
  public static int countNumeric(String source, int offset)
  {
    int count = 0;
    for (int index = offset; index < source.length(); index++)
    {
      char c = source.charAt(index);
      if ((c >= '0') && (c <= '9')) {
        count++;
      }
    }
    return count;
  }
  
  public static String[] toStringArray(String source, String separator, boolean unescape)
  {
    List<String> values = toStringVector(source, separator, unescape);
    String[] result = new String[values.size()];
    for (int index = 0; index < result.length; index++) {
      result[index] = ((String)values.get(index));
    }
    return result;
  }
  
  public static String[] toStringArray(String source, char separator, boolean unescape)
  {
    List<String> values = toStringVector(source, separator, unescape);
    String[] result = new String[values.size()];
    for (int index = 0; index < result.length; index++) {
      result[index] = ((String)values.get(index));
    }
    return result;
  }
  
  public static String[] toStringArray(String source, String separator)
  {
    return toStringArray(source, separator, false);
  }
  
  public static String[] toStringArray(String source, char separator)
  {
    return toStringArray(source, separator, false);
  }
  
  public static String[] toUnescapedStringArray(String source, String separator)
  {
    return toStringArray(source, separator, true);
  }
  
  public static String[] toUnescapedStringArray(String source, char separator)
  {
    return toStringArray(source, separator, true);
  }
  
  public static List<String> toStringVector(String source, String separator, boolean unescape)
  {
    if ((separator == null) || (separator.isEmpty())) {
      throw new IllegalArgumentException("Separator can not be empty");
    }
    List<String> result = new ArrayList();
    if ((source != null) && (source.length() > 0))
    {
      int lastIndex = 0;
      int index;
      while ((index = source.indexOf(separator, lastIndex)) >= 0)
      {
        if (unescape) {
          result.add(StringEscapeUtil.unescapeJava(source.substring(lastIndex, index).trim()));
        } else {
          result.add(source.substring(lastIndex, index).trim());
        }
        lastIndex = index + separator.length();
      }
      if (lastIndex <= source.length()) {
        if (unescape) {
          result.add(StringEscapeUtil.unescapeJava(source.substring(lastIndex, source.length()).trim()));
        } else {
          result.add(source.substring(lastIndex, source.length()).trim());
        }
      }
    }
    return result;
  }
  
  public static List<String> toStringVector(String source, char separator, boolean unescape)
  {
    List<String> result = new ArrayList();
    if ((source != null) && (source.length() > 0))
    {
      int lastIndex = 0;
      int index;
      while ((index = source.indexOf(separator, lastIndex)) >= 0)
      {
        if (unescape) {
          result.add(StringEscapeUtil.unescapeJava(source.substring(lastIndex, index).trim()));
        } else {
          result.add(source.substring(lastIndex, index).trim());
        }
        lastIndex = index + 1;
      }
      if (lastIndex <= source.length()) {
        if (unescape) {
          result.add(StringEscapeUtil.unescapeJava(source.substring(lastIndex, source.length()).trim()));
        } else {
          result.add(source.substring(lastIndex, source.length()).trim());
        }
      }
    }
    return result;
  }
  
  public static String[] toStringArray(String source)
  {
    return toStringArray(source, ',');
  }
  
  public static String[] toUnescapedStringArray(String source)
  {
    return toUnescapedStringArray(source, ',');
  }
  
  public static List<String> toStringVector(String source)
  {
    return toStringVector(source, ',');
  }
  
  public static List<String> toStringVector(String source, String separator)
  {
    return toStringVector(source, separator, false);
  }
  
  public static List<String> toStringVector(String source, char separator)
  {
    return toStringVector(source, separator, false);
  }
  
  public static List<String> toUnescapedStringVector(String source)
  {
    return toUnescapedStringVector(source, ',');
  }
  
  public static List<String> toUnescapedStringVector(String source, String separator)
  {
    return toStringVector(source, separator, true);
  }
  
  public static List<String> toUnescapedStringVector(String source, char separator)
  {
    return toStringVector(source, separator, true);
  }
  
  public static String[] spaceSeparatedStringToStringArray(String source)
  {
    List<String> values = spaceSeparatedStringToStringVector(source);
    String[] result = new String[values.size()];
    for (int index = 0; index < result.length; index++) {
      result[index] = ((String)values.get(index));
    }
    return result;
  }
  
  public static List<String> spaceSeparatedStringToStringVector(String source)
  {
    return spaceSeparatedStringToStringVector(source, -1);
  }
  
  public static List<String> spaceSeparatedStringToStringVector(String source, int maxResultSize)
  {
    List<String> result = new ArrayList();
    if (source.length() > 0)
    {
      int length = source.length();
      int letterIndex = 0;
      for (;;)
      {
        if ((maxResultSize > 0) && (result.size() >= maxResultSize - 1)) {
          if (letterIndex < length)
          {
            String piece = source.substring(letterIndex, length).trim();
            if ((piece != null) && (!piece.isEmpty()))
            {
              result.add(piece);
              break;
            }
          }
        }
        while ((letterIndex < length) && (source.charAt(letterIndex) <= ' ')) {
          letterIndex++;
        }
        if (letterIndex >= length) {
          break;
        }
        int spaceIndex = letterIndex;
        while ((spaceIndex < length) && (source.charAt(spaceIndex) > ' ')) {
          spaceIndex++;
        }
        result.add(source.substring(letterIndex, spaceIndex).trim());
        letterIndex = spaceIndex;
      }
    }
    return result;
  }
  
  public static List<String> spaceSeparatedStringToStringVector(String source, String additionWhitespaceSymbol)
  {
    return spaceSeparatedStringToStringVector(source, additionWhitespaceSymbol, -1);
  }
  
  public static List<String> spaceSeparatedStringToStringVector(String source, String additionWhitespaceSymbol, int maxResultSize)
  {
    List<String> result = new ArrayList();
    if (source.length() > 0)
    {
      int length = source.length();
      int letterIndex = 0;
      for (;;)
      {
        if ((maxResultSize > 0) && (result.size() >= maxResultSize - 1)) {
          if (letterIndex < length)
          {
            String piece = source.substring(letterIndex, length).trim();
            if ((piece != null) && (!piece.isEmpty()))
            {
              result.add(piece);
              break;
            }
          }
        }
        while ((letterIndex < length) && (
          (source.charAt(letterIndex) <= ' ') || (additionWhitespaceSymbol.indexOf(source.charAt(letterIndex)) >= 0))) {
          letterIndex++;
        }
        if (letterIndex >= length) {
          break;
        }
        int spaceIndex = letterIndex;
        while ((spaceIndex < length) && (source.charAt(spaceIndex) > ' ') && 
          (additionWhitespaceSymbol.indexOf(source.charAt(spaceIndex)) < 0)) {
          spaceIndex++;
        }
        result.add(source.substring(letterIndex, spaceIndex).trim());
        letterIndex = spaceIndex;
      }
    }
    return result;
  }
  
  public static List<String> toStringVector(String[] array)
  {
    List<String> values = new ArrayList();
    values.addAll(Arrays.asList(array));
    return values;
  }
  
  public static String convertCharForm(String source, String sourceCharform, String destinationCharform)
  {
    if (source == null) {
      return null;
    }
    int length = source.length();
    
    StringBuilder result = new StringBuilder();
    for (int index = 0; index < length; index++)
    {
      char c = source.charAt(index);
      int charIndex;
      if ((charIndex = sourceCharform.indexOf(c)) >= 0) {
        result.append(destinationCharform.charAt(charIndex));
      } else {
        result.append(c);
      }
    }
    return result.toString();
  }
  
  public static String align(String source, int alignment, int length, char c)
  {
    if (source == null) {
      return null;
    }
    if (source.length() > length) {
      return source.substring(0, length);
    }
    StringBuilder buf = new StringBuilder();
    if (alignment == 0)
    {
      source = lpad(source, source.length() + (length - source.length()) / 2, c);
      return rpad(source, length, c);
    }
    if (alignment == 2) {
      return lpad(source, length, c);
    }
    if (alignment == 1) {
      return rpad(source, length, c);
    }
    return buf.toString();
  }
  
  public static String align(String source, int alignment, int length)
  {
    return align(source, alignment, length, ' ');
  }
  
  public static String lpad(String source, int length)
  {
    return lpad(source, length, ' ');
  }
  
  public static String rpad(String source, int length)
  {
    return rpad(source, length, ' ');
  }
  
  public static String lpad(String source, int length, char c)
  {
    if (source == null) {
      return null;
    }
    int count = length - source.length();
    if (count > 0) {
      return createMonoString(c, count) + source;
    }
    return source;
  }
  
  public static String rpad(String source, int length, char c)
  {
    if (source == null) {
      return null;
    }
    int count = length - source.length();
    if (count > 0) {
      return source + createMonoString(c, count);
    }
    return source;
  }
  
  public static String createMonoString(char c, int length)
  {
    StringBuilder buf = new StringBuilder();
    for (int index = 0; index < length; index++) {
      buf.append(c);
    }
    return buf.toString();
  }
  
  public static String createMonoString(String c, int length)
  {
    StringBuilder buf = new StringBuilder();
    for (int index = 0; index < length; index++) {
      buf.append(c);
    }
    return buf.toString();
  }
  
  public static String join(Object[] items)
  {
    return joinEscape(items, ",");
  }
  
  public static String joinEscape(Object[] items)
  {
    return joinEscape(items, ",");
  }
  
  public static String join(Object[] items, String delim)
  {
    return join(items, delim, false);
  }
  
  public static String joinEscape(Object[] items, String delim)
  {
    return join(items, delim, true);
  }
  
  public static String join(Object[] items, String delim, boolean escape)
  {
    if ((items == null) || (items.length == 0)) {
      return "";
    }
    StringBuilder sbuf = new StringBuilder();
    for (int i = 0; i < items.length; i++)
    {
      if (escape) {
        sbuf.append(StringEscapeUtil.escapeJava(nvl(items[i], "")));
      } else {
        sbuf.append(nvl(items[i], ""));
      }
      if (i < items.length - 1) {
        sbuf.append(delim);
      }
    }
    return sbuf.toString();
  }
  
  public static String join(Collection values)
  {
    return join(values, ",");
  }
  
  public static String joinEscape(Collection values)
  {
    return joinEscape(values, ",");
  }
  
  public static String join(Collection values, String delim)
  {
    return join(values, delim, false);
  }
  
  public static String joinEscape(Collection values, String delim)
  {
    return join(values, delim, true);
  }
  
  public static String join(Collection values, String delim, boolean escape)
  {
    if ((values == null) || (values.isEmpty())) {
      return "";
    }
    StringBuilder sbuf = new StringBuilder();
    int index = 0;
    for (Object value : values)
    {
      if (escape) {
        sbuf.append(StringEscapeUtil.escapeJava(nvl(value, "")));
      } else {
        sbuf.append(nvl(value, ""));
      }
      if (index < values.size() - 1) {
        sbuf.append(delim);
      }
      index++;
    }
    return sbuf.toString();
  }
  
  public static String capitalize(String source)
  {
    StringBuilder buf = new StringBuilder();
    int offset;
    while ((offset = indexOfLetter(source, buf.length())) >= 0)
    {
      buf.append(source.substring(buf.length(), offset));
      buf.append(Character.toUpperCase(source.charAt(buf.length())));
      offset = indexOfSpace(source, buf.length());
      if (offset < 0)
      {
        buf.append(source.substring(buf.length(), source.length()).toLowerCase());
        break;
      }
      buf.append(source.substring(buf.length(), offset).toLowerCase());
    }
    return buf.toString();
  }
  
  public static byte[] replaceAll(byte[] source, byte[] find, byte[] replace)
  {
    if ((find == null) || (find.length == 0)) {
      return source;
    }
    int offset = 0;
    int lastOffset = 0;
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    while ((offset = indexOf(source, find, offset)) >= 0)
    {
      os.write(source, lastOffset, offset - lastOffset);
      os.write(replace, 0, replace.length);
      offset += find.length;
      lastOffset = offset;
    }
    os.write(source, lastOffset, source.length - lastOffset);
    return os.toByteArray();
  }
  
  public static String removeSpace(String source)
  {
    StringBuilder buf = new StringBuilder();
    for (int i = 0; i < source.length(); i++)
    {
      char c = source.charAt(i);
      if (c > ' ') {
        buf.append(c);
      }
    }
    return buf.toString();
  }
  
  public static String removeSpace(String source, String additionWhitespaceSymbol)
  {
    StringBuilder buf = new StringBuilder();
    for (int i = 0; i < source.length(); i++)
    {
      char c = source.charAt(i);
      if ((c > ' ') && (additionWhitespaceSymbol.indexOf(c) < 0)) {
        buf.append(c);
      }
    }
    return buf.toString();
  }
  
  public static String removeSymbols(String source, List<Character> symbols)
  {
    StringBuilder buf = new StringBuilder();
    for (int i = 0; i < source.length(); i++)
    {
      char c = source.charAt(i);
      if (symbols.indexOf(Character.valueOf(c)) < 0) {
        buf.append(c);
      }
    }
    return buf.toString();
  }
  
  public static String removeSymbols(String source, char[] symbols)
  {
    List<Character> symbolList = new ArrayList(symbols.length);
    for (char c : symbols) {
      symbolList.add(Character.valueOf('\000'));
    }
    return removeSymbols(source, symbolList);
  }
  
  public static int indexOf(byte[] source, byte[] find, int offset)
  {
    if ((find == null) || (find.length == 0)) {
      return offset;
    }
    int index = 0;
    while (offset < source.length) {
      if (find[(index++)] == source[(offset++)])
      {
        if (index >= find.length) {
          return offset - find.length;
        }
      }
      else {
        index = 0;
      }
    }
    return -1;
  }
  
  public static boolean startWithsIgnoreWhiteSpace(String source, String prefix)
  {
    return getRawPrefixIgnoreWhiteSpace(source, prefix) != null;
  }
  
  public static String getRawPrefixIgnoreWhiteSpace(String source, String prefix)
  {
    int sourceOffset = 0;int prefixOffset = 0;
    
    StringBuilder buf = new StringBuilder();
    for (;;)
    {
      if (prefixOffset >= prefix.length()) {
        return buf.toString();
      }
      if (sourceOffset >= source.length()) {
        return null;
      }
      char sourceChar = source.charAt(sourceOffset);
      char prefixChar = prefix.charAt(prefixOffset);
      if (sourceChar == prefixChar)
      {
        buf.append(sourceChar);
        sourceOffset++;
        prefixOffset++;
      }
      else
      {
        if (sourceChar > ' ') {
          break;
        }
        buf.append(sourceChar);
        sourceOffset++;
      }
    }
    return null;
  }
  
  public static String getRawPrefixIgnoreWhiteSpace(String source, String prefix, String additionWhitespaceSymbol)
  {
    int sourceOffset = 0;int prefixOffset = 0;
    
    StringBuilder buf = new StringBuilder();
    for (;;)
    {
      if (prefixOffset >= prefix.length()) {
        return buf.toString();
      }
      if (sourceOffset >= source.length()) {
        return null;
      }
      char sourceChar = source.charAt(sourceOffset);
      char prefixChar = prefix.charAt(prefixOffset);
      if (sourceChar == prefixChar)
      {
        buf.append(sourceChar);
        sourceOffset++;
        prefixOffset++;
      }
      else
      {
        if ((sourceChar > ' ') && (additionWhitespaceSymbol.indexOf(sourceChar) < 0)) {
          break;
        }
        buf.append(sourceChar);
        sourceOffset++;
      }
    }
    return null;
  }
  
  public static Map<String, String> stringToMap(String value, String entrySeparator, String valueSeparator)
  {
    Map<String, String> map = new LinkedHashMap();
    String[] entries = toStringArray(value, entrySeparator);
    for (String entry : entries)
    {
      entry = entry.trim();
      if (!entry.isEmpty())
      {
        int index = entry.indexOf(valueSeparator);
        if (index >= 0) {
          map.put(entry.substring(0, index).trim(), entry.substring(index + 1).trim());
        } else {
          map.put(entry, "");
        }
      }
    }
    return map;
  }
}
