package com.ftl.sql;

import com.ftl.util.Global;
import com.ftl.util.StreamUtil;
import com.ftl.util.StringUtil;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class CSVExporter
{
  private static final int BUFFER_SIZE = 131072;
  private static final byte[] eor = "\n".getBytes();
  private static final byte[] eof = "\t".getBytes();
  protected SimpleDateFormat rawDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
  
  public long process(Connection cn, String sql, String file)
    throws Exception
  {
    return process(cn, sql, new File(file));
  }
  
  public long process(Connection cn, String sql, File file)
    throws Exception
  {
    OutputStream os = null;
    try
    {
      os = new FileOutputStream(file);
      return process(cn, sql, os);
    }
    finally
    {
      StreamUtil.safeClose(os);
    }
  }
  
  public long process(Connection cn, String sql, OutputStream os)
    throws Exception
  {
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try
    {
      stmt = cn.prepareStatement(sql);
      rs = stmt.executeQuery();
      return process(rs, os);
    }
    finally
    {
      Database.closeObject(rs);
      Database.closeObject(stmt);
    }
  }
  
  public long process(ResultSet rs, String file)
    throws Exception
  {
    return process(rs, new File(file));
  }
  
  public long process(ResultSet rs, File file)
    throws Exception
  {
    OutputStream os = null;
    try
    {
      os = new FileOutputStream(file);
      return process(rs, os);
    }
    finally
    {
      StreamUtil.safeClose(os);
    }
  }
  
  public long process(ResultSet rs, OutputStream os)
    throws Exception
  {
    ResultSetMetaData metaData = rs.getMetaData();
    int columnCount = metaData.getColumnCount();
    char[] columnTypes = new char[columnCount];
    for (int columnIndex = 0; columnIndex < columnCount; columnIndex++)
    {
      int type = metaData.getColumnType(columnIndex + 1);
      if ((type == 91) || (type == 93) || (type == 92)) {
        columnTypes[columnIndex] = 'D';
      } else if ((type == -6) || (type == 5) || (type == 4) || (type == -5) || (type == 6) || (type == 7) || (type == 8) || (type == 2) || (type == 3)) {
        columnTypes[columnIndex] = 'N';
      } else {
        columnTypes[columnIndex] = 'S';
      }
    }
    BufferedOutputStream bos = null;
    try
    {
      bos = new BufferedOutputStream(os, 131072);
      long recordCount = 0L;
      int columnIndex;
      while (rs.next())
      {
        for (columnIndex = 0; columnIndex < columnCount; columnIndex++)
        {
          if (columnTypes[columnIndex] == 'D')
          {
            Timestamp columnValue = rs.getTimestamp(columnIndex + 1);
            if (columnValue != null) {
              bos.write(this.rawDateFormat.format(columnValue).getBytes());
            }
          }
          else if (columnTypes[columnIndex] == 'N')
          {
            String columnValue = rs.getString(columnIndex + 1);
            if (columnValue != null) {
              bos.write(columnValue.getBytes());
            }
          }
          else
          {
            String columnValue = rs.getString(columnIndex + 1);
            if (columnValue != null)
            {
              columnValue = StringUtil.replaceAll(columnValue, "\\", "\\\\");
              columnValue = StringUtil.replaceAll(columnValue, "\t", "\\t");
              columnValue = StringUtil.replaceAll(columnValue, "\n", "\\n");
              columnValue = StringUtil.replaceAll(columnValue, "\r", "\\r");
              bos.write(columnValue.getBytes(Global.ENCODING));
            }
          }
          if (columnIndex < columnCount - 1) {
            bos.write(eof);
          }
        }
        bos.write(eor);
        recordCount += 1L;
      }
      return recordCount;
    }
    finally
    {
      StreamUtil.safeClose(bos);
    }
  }
}
