package com.ftl.util;

import java.io.PrintWriter;
import java.io.StringWriter;

class StringPrintWriter
  extends PrintWriter
{
  public StringPrintWriter()
  {
    super(new StringWriter());
  }
  
  public StringPrintWriter(int initialSize)
  {
    super(new StringWriter(initialSize));
  }
  
  public String getString()
  {
    flush();
    return ((StringWriter)this.out).toString();
  }
}
