package com.ftl.util;

import static java.lang.Float.NaN;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;

public class NumberUtil
{
  public static boolean isNumber(String source)
  {
    return isNumber(source, NumberFormat.getNumberInstance());
  }
  
  public static boolean isNumber(String source, String formatPattern)
  {
    return isNumber(source, new DecimalFormat(formatPattern));
  }
  
  public static boolean isNumber(String source, NumberFormat format)
  {
    try
    {
      ParsePosition position = new ParsePosition(0);
      if ((format.parse(source, position) == null) || 
        (position.getIndex() < source.length())) {
        return false;
      }
    }
    catch (Exception e)
    {
      return false;
    }
    return true;
  }
  
  public static double toNumber(String source)
  {
    return toNumber(source, NumberFormat.getNumberInstance());
  }
  
  public static double toNumber(String source, String formatPattern)
  {
    return toNumber(source, new DecimalFormat(formatPattern));
  }
  
  public static double toNumber(String source, NumberFormat format)
  {
    try
    {
      ParsePosition position = new ParsePosition(0);
      Number number = format.parse(source, position);
      if ((number == null) || (position.getIndex() < source.length())) {
        return NaN;
      }
      return number.doubleValue();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return NaN;
  }
}
