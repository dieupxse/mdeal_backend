package com.ftl.sql;

import com.ftl.util.Global;
import com.ftl.util.StreamUtil;
import com.ftl.util.StringEscapeUtil;
import com.ftl.util.StringUtil;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;

public class CSVImporter
{
  private static final int BUFFER_SIZE = 131072;
  private static final byte eof = 9;
  private static final byte eor = 10;
  private int batchSize = 100;
  
  public long process(Connection cn, String sql, int columnCount, String file)
    throws Exception
  {
    return process(cn, sql, columnCount, new File(file));
  }
  
  public long process(Connection cn, String sql, int columnCount, File file)
    throws Exception
  {
    InputStream is = null;
    try
    {
      is = new FileInputStream(file);
      return process(cn, sql, columnCount, is);
    }
    finally
    {
      StreamUtil.safeClose(is);
    }
  }
  
  public long process(Connection cn, String sql, int columnCount, InputStream is)
    throws Exception
  {
    PreparedStatement stmt = null;
    try
    {
      stmt = cn.prepareStatement(sql);
      return process(stmt, columnCount, is);
    }
    finally
    {
      Database.closeObject(stmt);
    }
  }
  
  public long process(PreparedStatement stmt, int columnCount, String file)
    throws Exception
  {
    return process(stmt, columnCount, new File(file));
  }
  
  public long process(PreparedStatement stmt, int columnCount, File file)
    throws Exception
  {
    InputStream is = null;
    try
    {
      is = new FileInputStream(file);
      return process(stmt, columnCount, is);
    }
    finally
    {
      StreamUtil.safeClose(is);
    }
  }
  
  private List<String> unicodeSignatures = StringUtil.toStringVector("EFBBBF,FEFF,FFFE");
  
  public long process(PreparedStatement stmt, int columnCount, InputStream is)
    throws Exception
  {
    BufferedInputStream bis = null;
    int insertedCount = 0;
    try
    {
      bis = new BufferedInputStream(is);
      for (String unicodeSignature : this.unicodeSignatures)
      {
        byte[] unicodeSignatureData = StringUtil.hexStringToByteArray(unicodeSignature);
          byte[] compareData = new byte[unicodeSignatureData.length];
        
        bis.mark(64);
          int bytesRead = bis.read(compareData);
        if ((bytesRead == compareData.length) && (Arrays.equals(unicodeSignatureData, compareData))) {
          break;
        }
        bis.reset();
      }
      int preparedCount = 0;
      List<String> row;
      while ((row = readLine(bis)) != null)
      {
        for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
          stmt.setString(columnIndex + 1, StringEscapeUtil.unescapeJava((String)row.get(columnIndex)));
        }
        stmt.addBatch();
        preparedCount++;
        if (preparedCount >= getBatchSize())
        {
          int[] updatedCounts = stmt.executeBatch();
          for (int updatedCount : updatedCounts) {
            if (updatedCount == -2)
            {
              insertedCount++;
            }
            else
            {
              if (updatedCount == -3) {
                throw new Exception("Import record #" + insertedCount + " failed");
              }
              insertedCount += updatedCount;
            }
          }
          preparedCount = 0;
        }
      }
      if (preparedCount > 0)
      {
          int[] updatedCounts = stmt.executeBatch();
        for (int updatedCount : updatedCounts) {
          if (updatedCount == -2)
          {
            insertedCount++;
          }
          else
          {
            if (updatedCount == -3) {
              throw new Exception("Import record #" + insertedCount + " failed");
            }
            insertedCount += updatedCount;
          }
        }
      }
      return insertedCount;
    }
    catch (BatchUpdateException ex)
    {
      byte[] compareData;
      int bytesRead;
      int[] updatedCounts = ex.getUpdateCounts();
      for (int updatedCount : updatedCounts) {
        if (updatedCount == -2)
        {
          insertedCount++;
        }
        else
        {
          if (updatedCount == -3) {
            break;
          }
          insertedCount += updatedCount;
        }
      }
      throw new Exception("Import record #" + insertedCount + " failed: " + ex.getMessage(), ex);
    }
    finally
    {
      StreamUtil.safeClose(bis);
    }
  }
  
  public List<String> readLine(InputStream is)
    throws Exception
  {
    if (is.available() <= 0) {
      return null;
    }
    byte[] record = StreamUtil.getDataTerminatedBySymbolOrReachEOF(is, (byte)10);
    if (record == null) {
      return null;
    }
    return StringUtil.toStringVector(new String(record, Global.ENCODING), '\t');
  }
  
  public int getBatchSize()
  {
    return this.batchSize;
  }
  
  public void setBatchSize(int batchSize)
  {
    this.batchSize = batchSize;
  }
}
