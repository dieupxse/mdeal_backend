package com.ftl.util;

public class StringHolder
{
  public String value;
  
  public StringHolder(String value)
  {
    this.value = value;
  }
  
  public String toString()
  {
    return String.valueOf(this.value);
  }
}
