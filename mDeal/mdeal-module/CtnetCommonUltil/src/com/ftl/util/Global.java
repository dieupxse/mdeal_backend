package com.ftl.util;

import java.text.DecimalFormat;
import java.util.Date;

public class Global
{
  public static String CONFIG_DIR = System.getProperty("user.home", "") + "/" + "ftllib/";
  public static String RESOURCE_DIR = "";
  public static String ENCODING = "utf-8";
  public static DecimalFormat NUMBER_FORMAT = new DecimalFormat("#.#");
  public static String DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm:ss";
  public static String DATE_FORMAT = "dd/MM/yyyy";
  public static String TIME_FORMAT = "HH:mm:ss";
  public static String DB_DATE_TIME_FORMAT = "DD/MM/YYYY HH24:MI:SS";
  public static String DB_DATE_FORMAT = "DD/MM/YYYY";
  public static String DB_TIME_FORMAT = "HH24:MI:SS";
  public static String NULL_DATE_SQL = "TO_DATE('00010101','YYYYMMDD')";
  public static Date NULL_DATE = new Date(-62135794800000L);
  public static int NULL_ID = -1;
  public static String DEFAULT_APPLICATION_ID = "0";
  public static String APP_NAME = "FTL standard java library";
  public static String APP_VERSION = "1.0.0";
  public static long GENERIC_TIME = System.currentTimeMillis() + 1800000L;
  public static long GENERIC_END_TIME = 0L;
}
