package com.ftl.util;

public class ObjectHolder
{
  public Object value;
  
  public ObjectHolder(Object value)
  {
    this.value = value;
  }
  
  public String toString()
  {
    return String.valueOf(this.value);
  }
}
