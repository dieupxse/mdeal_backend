package com.ftl.dictionary;

import com.ftl.util.AppException;
import com.ftl.util.DateUtil;
import com.ftl.util.FileUtil;
import com.ftl.util.Global;
import com.ftl.util.StringUtil;
import java.util.Date;

public class ParameterHelper
{
  public static String getParameter(DictionaryNode config, String parameterName)
  {
    return config.getString(parameterName);
  }
  
  public static String loadMandatory(DictionaryNode config, String parameterName)
    throws AppException
  {
    String parameterValue = StringUtil.nvl(getParameter(config, parameterName), "");
    return loadMandatory(parameterName, parameterValue);
  }
  
  public static int loadInteger(DictionaryNode config, String parameterName)
    throws AppException
  {
    String parameterValue = StringUtil.nvl(getParameter(config, parameterName), "");
    return loadInteger(parameterName, parameterValue);
  }
  
  public static long loadLong(DictionaryNode config, String parameterName)
    throws AppException
  {
    String parameterValue = StringUtil.nvl(getParameter(config, parameterName), "");
    return loadLong(parameterName, parameterValue);
  }
  
  public static double loadDouble(DictionaryNode config, String parameterName)
    throws AppException
  {
    String parameterValue = StringUtil.nvl(getParameter(config, parameterName), "");
    return loadDouble(parameterName, parameterValue);
  }
  
  public static int loadUnsignedInteger(DictionaryNode config, String parameterName)
    throws AppException
  {
    String parameterValue = StringUtil.nvl(getParameter(config, parameterName), "");
    return loadUnsignedInteger(parameterName, parameterValue);
  }
  
  public static long loadUnsignedLong(DictionaryNode config, String parameterName)
    throws AppException
  {
    String parameterValue = StringUtil.nvl(getParameter(config, parameterName), "");
    return loadUnsignedLong(parameterName, parameterValue);
  }
  
  public static double loadUnsignedDouble(DictionaryNode config, String parameterName)
    throws AppException
  {
    String parameterValue = StringUtil.nvl(getParameter(config, parameterName), "");
    return loadUnsignedDouble(parameterName, parameterValue);
  }
  
  public static Date loadTime(DictionaryNode config, String parameterName)
    throws AppException
  {
    String parameterValue = StringUtil.nvl(getParameter(config, parameterName), "");
    return loadTime(parameterName, parameterValue);
  }
  
  public static Date loadDate(DictionaryNode config, String parameterName)
    throws AppException
  {
    String parameterValue = StringUtil.nvl(getParameter(config, parameterName), "");
    return loadDate(parameterName, parameterValue);
  }
  
  public static String loadCustomDate(DictionaryNode config, String parameterName, String dateFormat)
    throws AppException
  {
    String parameterValue = StringUtil.nvl(getParameter(config, parameterName), "");
    return loadCustomDate(parameterName, dateFormat, parameterValue);
  }
  
  public static String loadDirectory(DictionaryNode config, String parameterName, boolean autoCreate, boolean mandatory)
    throws AppException
  {
    String parameterValue = StringUtil.nvl(getParameter(config, parameterName), "");
    return loadDirectory(parameterName, parameterValue, autoCreate, mandatory);
  }
  
  public static Object loadClass(DictionaryNode config, String parameterName)
    throws AppException
  {
    String parameterValue = StringUtil.nvl(getParameter(config, parameterName), "");
    return loadClass(parameterName, parameterValue);
  }
  
  public static String loadResource(DictionaryNode config, String parameterName)
    throws AppException
  {
    String parameterValue = StringUtil.nvl(getParameter(config, parameterName), "");
    return loadResource(parameterName, parameterValue);
  }
  
  public static boolean loadBoolean(DictionaryNode config, String parameterName)
    throws AppException
  {
    String parameterValue = StringUtil.nvl(getParameter(config, parameterName), "");
    return loadBoolean(parameterName, parameterValue);
  }
  
  public static String loadMandatory(String parameterName, String parameterValue)
    throws AppException
  {
    if ((parameterValue == null) || (parameterValue.length() == 0)) {
      throw new AppException("Value of '" + parameterName + "' can not be null", parameterName);
    }
    return parameterValue;
  }
  
  public static long loadUnsignedLong(String parameterName, String parameterValue)
    throws AppException
  {
    long result = loadLong(parameterName, parameterValue);
    if (result < 0L) {
      throw new AppException("Value of '" + parameterName + "' ('" + parameterValue + "') must be a positive number", parameterName);
    }
    return result;
  }
  
  public static long loadLong(String parameterName, String parameterValue)
    throws AppException
  {
    try
    {
      return Long.parseLong(parameterValue);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      throw new AppException("Value of '" + parameterName + "' ('" + parameterValue + "') must be a number", parameterName);
    }
  }
  
  public static int loadInteger(String parameterName, String parameterValue)
    throws AppException
  {
    try
    {
      return Integer.parseInt(parameterValue);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      throw new AppException("Value of '" + parameterName + "' ('" + parameterValue + "') must be a number", parameterName);
    }
  }
  
  public static double loadUnsignedDouble(String parameterName, String parameterValue)
    throws AppException
  {
    double result = loadDouble(parameterName, parameterValue);
    if (result < 0.0D) {
      throw new AppException("Value of '" + parameterName + "' ('" + parameterValue + "') must be a positive number", parameterName);
    }
    return result;
  }
  
  public static double loadDouble(String parameterName, String parameterValue)
    throws AppException
  {
    try
    {
      return Double.parseDouble(parameterValue);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      throw new AppException("Value of '" + parameterName + "' ('" + parameterValue + "') must be a number", parameterName);
    }
  }
  
  public static int loadUnsignedInteger(String parameterName, String parameterValue)
    throws AppException
  {
    int result = loadInteger(parameterName, parameterValue);
    if (result < 0) {
      throw new AppException("Value of '" + parameterName + "' ('" + parameterValue + "') must be a positive number", parameterName);
    }
    return result;
  }
  
  public static Date loadTime(String parameterName, String parameterValue)
    throws AppException
  {
    if (parameterValue.length() > 0)
    {
      Date time = DateUtil.toDate(parameterValue, Global.TIME_FORMAT);
      if (time == null) {
        throw new AppException("Format of '" + parameterName + "' ('" + parameterValue + "') must be '" + Global.TIME_FORMAT + "'", parameterName);
      }
      return time;
    }
    return null;
  }
  
  public static Date loadDate(String parameterName, String parameterValue)
    throws AppException
  {
    if (parameterValue.length() > 0)
    {
      Date date = DateUtil.toDate(parameterValue, Global.DATE_FORMAT);
      if (date == null) {
        throw new AppException("Format of '" + parameterName + "' ('" + parameterValue + "') must be '" + Global.DATE_FORMAT + "'", parameterName);
      }
      return date;
    }
    return null;
  }
  
  public static String loadCustomDate(String parameterName, String dateFormat, String parameterValue)
    throws AppException
  {
    if (parameterValue.length() > 0)
    {
      if (!DateUtil.isDate(parameterValue, dateFormat)) {
        throw new AppException("Format of '" + parameterName + "' must be '" + dateFormat + "'", parameterName);
      }
      return parameterValue;
    }
    return null;
  }
  
  public static String loadDirectory(String parameterName, String parameterValue, boolean autoCreate, boolean mandatory)
    throws AppException
  {
    if (parameterValue.length() > 0)
    {
      if ((!parameterValue.endsWith("/")) && (!parameterValue.endsWith("\\"))) {
        parameterValue = parameterValue + "/";
      }
      if (autoCreate) {
        try
        {
          FileUtil.forceFolderExist(parameterValue);
        }
        catch (Exception e)
        {
          throw new AppException(e.getMessage(), parameterName);
        }
      }
    }
    else if (mandatory)
    {
      throw new AppException("Value of '" + parameterName + "' can not be null", parameterName);
    }
    return parameterValue;
  }
  
  public static Object loadClass(String parameterName, String parameterValue)
    throws AppException
  {
    try
    {
      return Class.forName(parameterValue).newInstance();
    }
    catch (Exception e)
    {
      e.printStackTrace();
      throw new AppException("Value of '" + parameterName + "' is invalid, class '" + parameterValue + "' not found", parameterName);
    }
  }
  
  public static String loadResource(String parameterName, String parameterValue)
    throws AppException
  {
    if (ParameterHelper.class.getResource(parameterValue) == null) {
      throw new AppException("Value of '" + parameterName + "' invalid, resource '" + parameterValue + "' not found", parameterName);
    }
    return parameterValue;
  }
  
  public static boolean loadBoolean(String parameterName, String parameterValue)
    throws AppException
  {
    return Boolean.parseBoolean(parameterValue);
  }
}
