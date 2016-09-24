package com.ftl.sql;

import com.ftl.util.Log;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Logger;

public class OracleSequenceSchema
  implements SequenceSchema
{
  private static final Logger logger = Logger.getLogger("");
  
  public String getSequenceValue(Connection cn, String sequenceName, boolean autoCreate, int defaultValue)
    throws Exception
  {
    String dbName = cn.getMetaData().getDatabaseProductName();
    
    Statement stmt = null;
    ResultSet rs = null;
    try
    {
      stmt = cn.createStatement();
      if (dbName.equalsIgnoreCase("ORACLE")) {
        rs = stmt.executeQuery("SELECT " + sequenceName + ".NEXTVAL FROM DUAL");
      } else {
        rs = stmt.executeQuery("SELECT " + sequenceName + ".nextval FROM DUAL");
      }
      if (rs.next()) {
        return rs.getString(1);
      }
      throw new Exception("ORA-02289");
    }
    catch (Exception e)
    {
      if ((e.getMessage() != null) && (e.getMessage().startsWith("ORA-02289")))
      {
        if (!autoCreate) {
          throw new Exception("Sequence " + sequenceName + " does not exists");
        }
        Database.closeObject(rs);
        Database.closeObject(stmt);
        stmt = cn.createStatement();
        stmt.executeUpdate("CREATE SEQUENCE " + sequenceName + " START WITH " + String.valueOf(defaultValue + 1));
        return String.valueOf(defaultValue);
      }
      throw e;
    }
    finally
    {
      Database.closeObject(rs);
      Database.closeObject(stmt);
    }
  }
  
  public String getSequenceValue(Connection cn, String sequenceName, String tableName, String sequenceFieldName)
    throws Exception
  {
    String dbName = cn.getMetaData().getDatabaseProductName();
    
    Statement stmt = null;
    ResultSet rs = null;
    try
    {
      stmt = cn.createStatement();
      if (dbName.equalsIgnoreCase("ORACLE")) {
        rs = stmt.executeQuery("SELECT " + sequenceName + ".NEXTVAL FROM DUAL");
      } else {
        rs = stmt.executeQuery("SELECT " + sequenceName + ".nextval FROM DUAL");
      }
      if (rs.next()) {
        return rs.getString(1);
      }
      throw new Exception("ORA-02289");
    }
    catch (Exception e)
    {
      if ((e.getMessage() != null) && 
        (e.getMessage().startsWith("ORA-02289")))
      {
        Database.closeObject(rs);
        Database.closeObject(stmt);
        stmt = cn.createStatement();
        int defaultValue = 1;
        String sql;
        if ((tableName != null) && (!tableName.isEmpty()) && (sequenceFieldName != null) && 
          (!sequenceFieldName.isEmpty()))
        {
          sql = "SELECT NVL(MAX(DECODE(TRANSLATE(" + sequenceFieldName + ",'x0123456789','x'),NULL,TO_NUMBER(" + sequenceFieldName + "),0)),0) + 1 FROM " + tableName;
          try
          {
            rs = stmt.executeQuery(sql);
            rs.next();
            defaultValue = rs.getInt(1);
            rs.close();
          }
          catch (Exception ex)
          {
            logger.info(sql);
            ex.printStackTrace();
          }
        }
        stmt.executeUpdate("CREATE SEQUENCE " + sequenceName + " START WITH " + String.valueOf(defaultValue + 1));
        stmt.close();
        return String.valueOf(defaultValue);
      }
      throw e;
    }
    finally
    {
      Database.closeObject(rs);
      Database.closeObject(stmt);
    }
  }
}
