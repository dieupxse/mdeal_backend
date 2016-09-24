package com.ftl.sql;

import com.ftl.util.Log;
import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringWriter;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Database
{
  private static final Logger logger = Logger.getLogger("");
  public static final Pattern DB_TYPE_HINT = Pattern.compile("/\\*DBTYPE:(.*)\\*/");
  public static SequenceSchema sequenceSchema = new OracleSequenceSchema();
  
  public static void executeScript(Connection cn, Reader in)
    throws Exception
  {
    executeScript(cn, in, true);
  }
  
  public static void executeScript(Connection cn, Reader in, boolean stopOnError)
    throws Exception
  {
    String databaseType = cn.getMetaData().getDatabaseProductName();
    BufferedReader reader = new BufferedReader(in);
    StringBuilder buf = new StringBuilder();
    String line;
    while ((line = reader.readLine()) != null) {
      if (line.trim().equals("/"))
      {
        String sql = buf.toString().trim();
        buf.setLength(0);
        if ((sql != null) && (!sql.isEmpty()))
        {
          Matcher matcher = DB_TYPE_HINT.matcher(sql);
          if (matcher != null) {
            if (matcher.find())
            {
              String expectedType = matcher.group(1).trim();
              if (!expectedType.equalsIgnoreCase(databaseType))
              {
                logger.log(Level.INFO, "Ignored statement:\n{0}", sql);
                continue;
              }
            }
          }
          try
          {
            logger.log(Level.INFO, "Start executing statement:\n{0}", sql);
            executeUpdate(cn, sql);
            logger.info("Completed");
          }
          catch (Exception ex)
          {
            logger.log(Level.WARNING, "Error occured:{0}", ex.getMessage());
            if (stopOnError) {
              throw ex;
            }
          }
        }
      }
      else
      {
        buf.append(line);
        buf.append("\n");
      }
    }
  }
  
  public static List<List<String>> executeQuery(Connection cn, String sql)
    throws Exception
  {
    return executeQuery(cn, sql, Integer.MAX_VALUE);
  }
  
  public static List<List<String>> executeQuery(Connection cn, String sql, int maxNumberOfRows)
    throws Exception
  {
    Statement stmt = null;
    ResultSet rs = null;
    try
    {
      stmt = cn.createStatement();
      rs = stmt.executeQuery(sql);
      return convertToVector(rs, maxNumberOfRows);
    }
    catch (SQLException e)
    {
      logger.info(sql);
      throw e;
    }
    finally
    {
      closeObject(rs);
      closeObject(stmt);
    }
  }
  
  public static List<List<String>> executeQuery(Connection cn, String sql, List<String> params)
    throws Exception
  {
    return executeQuery(cn, sql, params, Integer.MAX_VALUE);
  }
  
  public static List<List<String>> executeQuery(Connection cn, String sql, List<String> params, int maxNumberOfRows)
    throws Exception
  {
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try
    {
      stmt = cn.prepareStatement(sql);
      for (int i = 0; i < params.size(); i++) {
        stmt.setString(i + 1, (String)params.get(i));
      }
      rs = stmt.executeQuery();
      return convertToVector(rs, maxNumberOfRows);
    }
    catch (SQLException e)
    {
      logger.info(sql);
      throw e;
    }
    finally
    {
      closeObject(rs);
      closeObject(stmt);
    }
  }
  
  public static int executeUpdate(Connection cn, String sql)
    throws Exception
  {
    Statement stmt = null;
    try
    {
      stmt = cn.createStatement();
      return stmt.executeUpdate(sql);
    }
    catch (SQLException e)
    {
      logger.info(sql);
      throw e;
    }
    finally
    {
      closeObject(stmt);
    }
  }
  
  public static int executeUpdate(Connection cn, String sql, List<String> params)
    throws Exception
  {
    PreparedStatement stmt = null;
    try
    {
      stmt = cn.prepareStatement(sql);
      for (int i = 0; i < params.size(); i++) {
        stmt.setString(i + 1, (String)params.get(i));
      }
      return stmt.executeUpdate();
    }
    catch (SQLException e)
    {
      logger.info(sql);
      throw e;
    }
    finally
    {
      closeObject(stmt);
    }
  }
  
  public static List<List<String>> convertToVector(ResultSet rs)
    throws Exception
  {
    return convertToVector(rs, Integer.MAX_VALUE);
  }
  
  public static List<List<String>> convertToVector(ResultSet rs, int maxNumberOfRows)
    throws Exception
  {
    List result = new ArrayList();
    int columnCount = rs.getMetaData().getColumnCount();
    while ((rs.next()) && (result.size() < maxNumberOfRows))
    {
      List<String> row = new ArrayList();
      for (int i = 1; i <= columnCount; i++)
      {
        String value = rs.getString(i);
        if (value == null) {
          value = "";
        }
        row.add(value);
      }
      result.add(row);
    }
    return result;
  }
  
  public static Connection getConnection(String url, String userName, String password)
    throws Exception
  {
    return getConnection("oracle.jdbc.driver.OracleDriver", url, userName, password);
  }
  
  public static Connection getConnection(String driver, String url, String userName, String password)
    throws Exception
  {
    Properties prtConnect = new Properties();
    prtConnect.setProperty("user", userName);
    prtConnect.setProperty("password", password);
    Driver drv = (Driver)Class.forName(driver).newInstance();
    return drv.connect(url, prtConnect);
  }
  
  public static String getValue(Connection cn, String sql)
    throws Exception
  {
    Statement stmt = null;
    ResultSet rs = null;
    try
    {
      stmt = cn.createStatement();
      rs = stmt.executeQuery(sql);
      if (!rs.next()) {
        throw new Exception("No data found");
      }
      return rs.getString(1);
    }
    catch (SQLException e)
    {
      logger.info(sql);
      throw e;
    }
    finally
    {
      closeObject(rs);
      closeObject(stmt);
    }
  }
  
  public static String getValue(Connection cn, String sql, String defaultValue)
    throws Exception
  {
    Statement stmt = null;
    ResultSet rs = null;
    try
    {
      stmt = cn.createStatement();
      rs = stmt.executeQuery(sql);
      String str;
      if (!rs.next()) {
        return defaultValue;
      }
      return rs.getString(1);
    }
    catch (SQLException e)
    {
      logger.info(sql);
      throw e;
    }
    finally
    {
      closeObject(rs);
      closeObject(stmt);
    }
  }
  
  public static String getValueEx(Connection cn, String sql)
    throws Exception
  {
    Statement stmt = null;
    ResultSet rs = null;
    try
    {
      stmt = cn.createStatement();
      rs = stmt.executeQuery(sql);
      String str;
      if (!rs.next()) {
        return null;
      }
      return rs.getString(1);
    }
    catch (SQLException e)
    {
      logger.info(sql);
      throw e;
    }
    finally
    {
      closeObject(rs);
      closeObject(stmt);
    }
  }
  
  public static String getValue(Connection cn, String sql, List<String> params)
    throws Exception
  {
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try
    {
      stmt = cn.prepareStatement(sql);
      for (int i = 0; i < params.size(); i++) {
        stmt.setString(i + 1, (String)params.get(i));
      }
      rs = stmt.executeQuery();
      if (!rs.next()) {
        throw new Exception("No data found");
      }
      return rs.getString(1);
    }
    catch (SQLException e)
    {
      logger.info(sql);
      throw e;
    }
    finally
    {
      closeObject(rs);
      closeObject(stmt);
    }
  }
  
  public static String getValue(Connection cn, String sql, List<String> params, String defaultValue)
    throws Exception
  {
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try
    {
      stmt = cn.prepareStatement(sql);
      for (int i = 0; i < params.size(); i++) {
        stmt.setString(i + 1, (String)params.get(i));
      }
      rs = stmt.executeQuery();
      if (!rs.next()) {
        return defaultValue;
      }
      return rs.getString(1);
    }
    catch (SQLException e)
    {
      logger.info(sql);
      throw e;
    }
    finally
    {
      closeObject(rs);
      closeObject(stmt);
    }
  }
  
  public static String getValue(Connection cn, String tableName, String fieldName, String condition)
    throws Exception
  {
    return getValue(cn, "SELECT " + fieldName + " FROM " + tableName + " WHERE " + condition);
  }
  
  public static String getValueEx(Connection cn, String tableName, String fieldName, String condition)
    throws Exception
  {
    return getValueEx(cn, "SELECT " + fieldName + " FROM " + tableName + " WHERE " + condition);
  }
  
  public static String getClobValue(Connection cn, String sql)
    throws Exception
  {
    Statement stmt = null;
    ResultSet rs = null;
    try
    {
      stmt = cn.createStatement();
      rs = stmt.executeQuery(sql);
      if (!rs.next()) {
        throw new Exception("No data found");
      }
      Reader reader = rs.getClob(1).getCharacterStream();
      StringWriter writer = new StringWriter();
      char[] buffer = new char['?'];
      int charsRead;
      while ((charsRead = reader.read(buffer)) > 0) {
        writer.write(buffer, 0, charsRead);
      }
      writer.close();
      reader.close();
      return writer.toString();
    }
    catch (SQLException e)
    {
      logger.info(sql);
      throw e;
    }
    finally
    {
      closeObject(rs);
      closeObject(stmt);
    }
  }
  
  public static String getClobValue(Connection cn, String sql, String defaultValue)
    throws Exception
  {
    Statement stmt = null;
    ResultSet rs = null;
    try
    {
      stmt = cn.createStatement();
      rs = stmt.executeQuery(sql);
      if (!rs.next()) {
        return defaultValue;
      }
      Reader reader = rs.getClob(1).getCharacterStream();
      StringWriter writer = new StringWriter();
      char[] buffer = new char['?'];
      int charsRead;
      while ((charsRead = reader.read(buffer)) > 0) {
        writer.write(buffer, 0, charsRead);
      }
      writer.close();
      reader.close();
      return writer.toString();
    }
    catch (SQLException e)
    {
      logger.info(sql);
      throw e;
    }
    finally
    {
      closeObject(rs);
      closeObject(stmt);
    }
  }
  
  public static String getClobValue(Connection cn, String sql, List<String> params)
    throws Exception
  {
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try
    {
      stmt = cn.prepareStatement(sql);
      for (int i = 0; i < params.size(); i++) {
        stmt.setString(i + 1, (String)params.get(i));
      }
      rs = stmt.executeQuery();
      if (!rs.next()) {
        throw new Exception("No data found");
      }
      Reader reader = rs.getClob(1).getCharacterStream();
      StringWriter writer = new StringWriter();
      char[] buffer = new char['?'];
      int charsRead;
      while ((charsRead = reader.read(buffer)) > 0) {
        writer.write(buffer, 0, charsRead);
      }
      writer.close();
      reader.close();
      return writer.toString();
    }
    catch (SQLException e)
    {
      logger.info(sql);
      throw e;
    }
    finally
    {
      closeObject(rs);
      closeObject(stmt);
    }
  }
  
  public static String getClobValue(Connection cn, String sql, List<String> params, String defaultValue)
    throws Exception
  {
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try
    {
      stmt = cn.prepareStatement(sql);
      for (int i = 0; i < params.size(); i++) {
        stmt.setString(i + 1, (String)params.get(i));
      }
      rs = stmt.executeQuery();
      if (!rs.next()) {
        return defaultValue;
      }
      Reader reader = rs.getClob(1).getCharacterStream();
      StringWriter writer = new StringWriter();
      char[] buffer = new char['?'];
      int charsRead;
      while ((charsRead = reader.read(buffer)) > 0) {
        writer.write(buffer, 0, charsRead);
      }
      writer.close();
      reader.close();
      return writer.toString();
    }
    catch (SQLException e)
    {
      logger.info(sql);
      throw e;
    }
    finally
    {
      closeObject(rs);
      closeObject(stmt);
    }
  }
  
  public static String getClobValue(Connection cn, String tableName, String fieldName, String condition)
    throws Exception
  {
    return getClobValue(cn, "SELECT " + fieldName + " FROM " + tableName + " WHERE " + condition);
  }
  
  public static String getSequenceValue(Connection cn, String sequenceName)
    throws Exception
  {
    return getSequenceValue(cn, sequenceName, true, 1);
  }
  
  public static String getSequenceValue(Connection cn, String sequenceName, int defaultValue)
    throws Exception
  {
    return getSequenceValue(cn, sequenceName, true, defaultValue);
  }
  
  public static String getSequenceValue(Connection cn, String sequenceName, boolean autoCreate, int defaultValue)
    throws Exception
  {
    return sequenceSchema.getSequenceValue(cn, sequenceName, autoCreate, defaultValue);
  }
  
  public static String getSequenceValue(Connection cn, String sequenceName, String tableName, String sequenceFieldName)
    throws Exception
  {
    return sequenceSchema.getSequenceValue(cn, sequenceName, tableName, sequenceFieldName);
  }
  
  public static void closeObject(Statement obj)
  {
    try
    {
      if (obj != null) {
        obj.close();
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  
  public static void closeObject(ResultSet obj)
  {
    try
    {
      if (obj != null) {
        obj.close();
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  
  public static void closeObject(Connection obj)
  {
    try
    {
      if (obj != null) {
        if (!obj.isClosed())
        {
          if (!obj.getAutoCommit()) {
            obj.rollback();
          }
          obj.close();
        }
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
}
