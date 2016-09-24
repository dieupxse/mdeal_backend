package com.ftl.sql;

import com.ftl.util.FileUtil;
import com.ftl.util.Log;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Logger;

public class FileSequenceSchema
  implements SequenceSchema
{
  private static final Logger logger = Logger.getLogger("");
  private String sequenceFolder = "sequence/";
  private int splitCount = 1;
  private int splitIndex = 0;
  
  public String getSequenceValue(Connection cn, String sequenceName, boolean autoCreate, int defaultValue)
    throws Exception
  {
    File file = new File(this.sequenceFolder + sequenceName);
    if (file.exists()) {
      return String.valueOf(FileUtil.getSequenceValue(file, this.splitCount, this.splitIndex));
    }
    if (!autoCreate) {
      throw new Exception("Sequence " + sequenceName + " does not exists.");
    }
    String result = String.valueOf(defaultValue % this.splitCount * this.splitCount + this.splitIndex);
    FileUtil.writeFile(file, result);
    return result;
  }
  
  public String getSequenceValue(Connection cn, String sequenceName, String tableName, String sequenceFieldName)
    throws Exception
  {
    File file = new File(this.sequenceFolder + sequenceName);
    if (file.exists()) {
      return String.valueOf(FileUtil.getSequenceValue(file, this.splitCount, this.splitIndex));
    }
    int defaultValue = 1;
    if ((tableName != null) && (!tableName.isEmpty()) && (sequenceFieldName != null) && 
      (!sequenceFieldName.isEmpty()))
    {
      String sql = null;
      Statement stmt = null;
      ResultSet rs = null;
      try
      {
        sql = "SELECT NVL(MAX(DECODE(TRANSLATE(" + sequenceFieldName + ",'x0123456789','x'),NULL,TO_NUMBER(" + sequenceFieldName + "),0)),0) + 1 FROM " + tableName;
        
        stmt = cn.createStatement();
        rs = stmt.executeQuery(sql);
        if (rs.next()) {
          defaultValue = rs.getInt(1);
        }
      }
      catch (Exception ex)
      {
        logger.info(sql);
        ex.printStackTrace();
      }
      finally
      {
        Database.closeObject(rs);
        Database.closeObject(stmt);
      }
    }
    String result = String.valueOf(defaultValue % this.splitCount * this.splitCount + this.splitIndex);
    FileUtil.writeFile(file, result);
    return result;
  }
  
  public void setSequenceFolder(String sequenceFolder)
  {
    if (!sequenceFolder.endsWith("/")) {
      sequenceFolder = sequenceFolder + "/";
    }
    this.sequenceFolder = sequenceFolder;
  }
  
  public void setSplitCount(int splitCount)
  {
    this.splitCount = splitCount;
  }
  
  public void setSplitIndex(int splitIndex)
  {
    this.splitIndex = splitIndex;
  }
  
  public String getSequenceFolder()
  {
    return this.sequenceFolder;
  }
  
  public int getSplitCount()
  {
    return this.splitCount;
  }
  
  public int getSplitIndex()
  {
    return this.splitIndex;
  }
}
