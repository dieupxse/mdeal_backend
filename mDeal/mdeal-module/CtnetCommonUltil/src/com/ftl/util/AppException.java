package com.ftl.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class AppException
  extends Exception
{
  private String reason = null;
  private String info = null;
  private Map data;
  
  public AppException(String reason, Map data)
  {
    setReason(reason);
    setData(data);
  }
  
  public AppException(String reason, String info, Map data)
  {
    setInfo(info);
    setReason(reason);
    setData(data);
  }
  
  public AppException(String reason, String[] info, Map data)
  {
    setInfo(info);
    setReason(reason);
    setData(data);
  }
  
  public AppException(Exception e, Map data)
  {
    super(e);
    if ((e instanceof AppException))
    {
      setReason(((AppException)e).getReason());
      setRawInfo(((AppException)e).getRawInfo());
      Map newData = ((AppException)e).getData();
      if (newData == null) {
        newData = new HashMap();
      }
      newData.putAll(data);
      setData(newData);
    }
    else
    {
      setReason(e.getMessage());
      setData(data);
    }
  }
  
  public AppException(Exception e, String info, Map data)
  {
    super(e);
    setInfo(info);
    if ((e instanceof AppException))
    {
      setReason(((AppException)e).getReason());
      Map newData = ((AppException)e).getData();
      if (newData == null) {
        newData = new HashMap();
      }
      newData.putAll(data);
      setData(newData);
    }
    else
    {
      setReason(e.getMessage());
      setData(data);
    }
  }
  
  public AppException(Exception e, String[] info, Map data)
  {
    super(e);
    setInfo(info);
    if ((e instanceof AppException))
    {
      setReason(((AppException)e).getReason());
      Map newData = ((AppException)e).getData();
      if (newData == null) {
        newData = new HashMap();
      }
      newData.putAll(data);
      setData(newData);
    }
    else
    {
      setReason(e.getMessage());
      setData(data);
    }
  }
  
  public AppException(String reason)
  {
    setReason(reason);
  }
  
  public AppException(String reason, String info)
  {
    setInfo(info);
    setReason(reason);
  }
  
  public AppException(String reason, String[] info)
  {
    setInfo(info);
    setReason(reason);
  }
  
  public AppException(Exception e)
  {
    super(e);
    if ((e instanceof AppException))
    {
      setReason(((AppException)e).getReason());
      setRawInfo(((AppException)e).getRawInfo());
      setData(((AppException)e).getData());
    }
    else
    {
      setReason(e.getMessage());
    }
  }
  
  public AppException(Exception e, String info)
  {
    super(e);
    setInfo(info);
    if ((e instanceof AppException))
    {
      setReason(((AppException)e).getReason());
      setData(((AppException)e).getData());
    }
    else
    {
      setReason(e.getMessage());
    }
  }
  
  public AppException(Exception e, String[] info)
  {
    super(e);
    setInfo(info);
    if ((e instanceof AppException))
    {
      setReason(((AppException)e).getReason());
      setData(((AppException)e).getData());
    }
    else
    {
      setReason(e.getMessage());
    }
  }
  
  public String getLocalizedMessage()
  {
    return this.reason;
  }
  
  public String getMessage()
  {
    String message = this.reason;
    if ((this.info != null) && (this.info.length() > 0)) {
      message = message + "\r\nAdditional info: " + this.info;
    }
    if ((this.data != null) && (this.data.size() > 0)) {
      message = message + "\r\nData: " + this.data;
    }
    return message;
  }
  
  public String toString()
  {
    return getMessage();
  }
  
  public String getRawInfo()
  {
    return this.info;
  }
  
  public void setRawInfo(String info)
  {
    this.info = info;
  }
  
  public String getInfo()
  {
    return StringEscapeUtil.unescapeJava(this.info);
  }
  
  public String[] getInfoAsArray()
  {
    return StringUtil.toUnescapedStringArray(this.info);
  }
  
  public List getInfoAsVector()
  {
    return StringUtil.toUnescapedStringVector(this.info);
  }
  
  public void setInfo(String info)
  {
    this.info = StringEscapeUtil.escapeJava(info);
  }
  
  public void setInfo(String[] info)
  {
    this.info = StringUtil.joinEscape(info);
  }
  
  public String getReason()
  {
    return this.reason;
  }
  
  public void setReason(String reason)
  {
    if (reason == null) {
      this.reason = "Null pointer exception";
    } else {
      this.reason = reason.trim();
    }
  }
  
  public List toVector()
  {
    List result = new ArrayList();
    result.add(getReason());
    result.add(getRawInfo());
    if ((this.data != null) && (this.data.size() > 0))
    {
      List dataNames = new ArrayList();
      List dataValues = new ArrayList();
      Iterator iterator = this.data.entrySet().iterator();
      while (iterator.hasNext())
      {
        Map.Entry entry = (Map.Entry)iterator.next();
        Object value = entry.getValue();
        if ((value instanceof String))
        {
          dataNames.add(entry.getKey());
          dataValues.add(value);
        }
      }
      result.add(dataNames);
      result.add(dataValues);
    }
    return result;
  }
  
  public static AppException fromVector(List vt)
  {
    AppException ex = new AppException((String)vt.get(0));
    String info = (String)vt.get(1);
    if ((info != null) && (!info.isEmpty())) {
      ex.setRawInfo(info);
    }
    if (vt.size() > 2)
    {
      List dataNames = (List)vt.get(2);
      List dataValues = (List)vt.get(3);
      Map data = new HashMap();
      for (int index = 0; index < dataNames.size(); index++) {
        data.put(dataNames.get(index), dataValues.get(index));
      }
      ex.setData(data);
    }
    return ex;
  }
  
  public void setData(Map data)
  {
    this.data = data;
  }
  
  public Map getData()
  {
    return this.data;
  }
}
