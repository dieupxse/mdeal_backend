package com.ftl.sql;

import java.sql.Connection;

public abstract interface SequenceSchema
{
  public abstract String getSequenceValue(Connection paramConnection, String paramString, boolean paramBoolean, int paramInt)
    throws Exception;
  
  public abstract String getSequenceValue(Connection paramConnection, String paramString1, String paramString2, String paramString3)
    throws Exception;
}
