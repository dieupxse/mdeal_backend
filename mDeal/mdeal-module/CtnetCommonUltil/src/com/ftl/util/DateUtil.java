package com.ftl.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil
{
  public static boolean isDate(String source)
  {
    return isDate(source, DateFormat.getDateInstance());
  }
  
  public static boolean isDate(String source, String formatPattern)
  {
    return isDate(source, new SimpleDateFormat(formatPattern));
  }
  
  public static boolean isDate(String source, DateFormat format)
  {
    try
    {
      format.setLenient(false);
      if (format.parse(source) == null) {
        return false;
      }
    }
    catch (Exception e)
    {
      return false;
    }
    return true;
  }
  
  public static Date toDate(String source)
  {
    return toDate(source, DateFormat.getDateInstance());
  }
  
  public static Date toDate(String source, String formatPattern)
  {
    return toDate(source, new SimpleDateFormat(formatPattern));
  }
  
  public static Date toDate(String source, DateFormat format)
  {
    try
    {
      format.setLenient(false);
      return format.parse(source);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return null;
  }
  
  public static Date addSecond(Date source, int value)
  {
    return add(source, value, 13);
  }
  
  public static Date addMinute(Date source, int value)
  {
    return add(source, value, 12);
  }
  
  public static Date addHour(Date source, int value)
  {
    return add(source, value, 10);
  }
  
  public static Date addDay(Date source, int value)
  {
    return add(source, value, 5);
  }
  
  public static Date addMonth(Date source, int value)
  {
    return add(source, value, 2);
  }
  
  public static Date addYear(Date source, int value)
  {
    return add(source, value, 1);
  }
  
  public static Date add(Date source, int value, int type)
  {
    Calendar cld = Calendar.getInstance();
    cld.setTime(source);
    cld.add(type, value);
    return cld.getTime();
  }
  
  public static Date trim(Date source)
  {
    return trim(source, new int[] { 14, 13, 12, 11 });
  }
  
  public static Date trim(Date source, int type)
  {
    Calendar cld = Calendar.getInstance();
    cld.setTime(source);
    cld.set(type, cld.getMinimum(type));
    return cld.getTime();
  }
  
  public static Date trim(Date source, int[] types)
  {
    Calendar cld = Calendar.getInstance();
    cld.setTime(source);
    for (int type : types) {
      cld.set(type, cld.getMinimum(type));
    }
    return cld.getTime();
  }
}
