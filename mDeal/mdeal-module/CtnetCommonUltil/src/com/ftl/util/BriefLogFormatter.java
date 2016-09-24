package com.ftl.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class BriefLogFormatter
  extends Formatter
{
  Date dat = new Date();
  private static final String format = "{0,date,dd/MM} {0,time,HH:mm:ss.SSS}";
  private MessageFormat formatter;
  private final Object[] args = new Object[1];
  private final String lineSeparator = "\n";
  
  public synchronized String format(LogRecord record)
  {
    StringBuilder sb = new StringBuilder();
    
    this.dat.setTime(record.getMillis());
    this.args[0] = this.dat;
    StringBuffer text = new StringBuffer();
    if (this.formatter == null) {
      this.formatter = new MessageFormat("{0,date,dd/MM} {0,time,HH:mm:ss.SSS}");
    }
    this.formatter.format(this.args, text, null);
    sb.append(text);
    sb.append(" ");
    
    String message = formatMessage(record);
    sb.append(record.getLevel().getLocalizedName());
    sb.append(": ");
    sb.append(message);
    sb.append("\n");
    if (record.getThrown() != null) {
      try
      {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        record.getThrown().printStackTrace(pw);
        pw.close();
        sb.append(sw.toString());
      }
      catch (Exception ex) {}
    }
    return sb.toString();
  }
}
