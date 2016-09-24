package com.ftl.sql;

import com.ftl.util.IntegerHolder;
import com.ftl.util.StringEscapeUtil;
import com.ftl.util.StringHolder;
import com.ftl.util.StringUtil;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SqlClause
{
  private String sql;
  private LinkedList<Object> args;
  
  public SqlClause() {}
  
  public SqlClause(String sql)
  {
    this.sql = sql;
  }
  
  public SqlClause(String sql, List<Object> args)
  {
    this.sql = sql;
    if (args != null) {
      this.args = new LinkedList(args);
    }
  }
  
  public SqlClause(SqlClause clause)
  {
    if (clause != null)
    {
      this.sql = clause.getSql();
      if (clause.getArgs() != null) {
        this.args = new LinkedList(clause.getArgs());
      }
    }
  }
  
  public String getSql()
  {
    return this.sql;
  }
  
  public List<Object> getArgs()
  {
    return this.args;
  }
  
  public boolean isEmpty()
  {
    return (this.sql == null) || (this.sql.isEmpty());
  }
  
  public PreparedStatement createPreparedStatement(Connection cn)
    throws Exception
  {
    PreparedStatement stmt = cn.prepareStatement(getSql());
    applyArgs(stmt);
    return stmt;
  }
  
  public PreparedStatement createPreparedStatement(Connection cn, String[] generatedKeys)
    throws Exception
  {
    if (generatedKeys == null) {
      return createPreparedStatement(cn);
    }
    PreparedStatement stmt = cn.prepareStatement(getSql(), generatedKeys);
    applyArgs(stmt);
    return stmt;
  }
  
  public void applyArgs(PreparedStatement stmt)
    throws SQLException
  {
    if (this.args == null) {
      return;
    }
    int index = 0;
    for (Object arg : this.args)
    {
      index++;
      if ((!(arg instanceof IntegerHolder)) && (!(arg instanceof StringHolder))) {
        setString(stmt, index, StringUtil.nvl(arg, ""));
      }
    }
  }
  
  public String createPlainStatement()
  {
    if (this.args == null) {
      return getSql();
    }
    List<String> formattedArgs = new LinkedList();
    for (Object arg : this.args) {
      formattedArgs.add("'" + StringEscapeUtil.escapeHtml(StringUtil.nvl(arg, "")) + "'");
    }
    return StringUtil.replaceAll(getSql(), "?", formattedArgs);
  }
  
  public void applyIndexedArgs(PreparedStatement stmt, List indexedArgs)
    throws Exception
  {
    if ((this.args == null) || (indexedArgs == null)) {
      return;
    }
    int index = 0;
    for (Object arg : this.args)
    {
      index++;
      if ((arg instanceof IntegerHolder)) {
        setString(stmt, index, StringUtil.nvl(indexedArgs.get(((IntegerHolder)arg).value), ""));
      }
    }
  }
  
  public void applyNamedArgs(PreparedStatement stmt, Map namedArgs)
    throws Exception
  {
    if ((this.args == null) || (namedArgs == null)) {
      return;
    }
    int index = 0;
    for (Object arg : this.args)
    {
      index++;
      if ((arg instanceof StringHolder)) {
        setString(stmt, index, StringUtil.nvl(namedArgs.get(((StringHolder)arg).value), ""));
      }
    }
  }
  
  public SqlClause append(SqlClause clause, String open, String close)
  {
    if (clause == null) {
      return this;
    }
    return append(clause.getSql(), clause.getArgs(), open, close);
  }
  
  public SqlClause append(String sql, List<Object> args, String open, String close)
  {
    if ((sql != null) && (!sql.isEmpty())) {
      if ((this.sql == null) || (this.sql.isEmpty())) {
        this.sql = sql;
      } else {
        this.sql = (this.sql + open + sql + close);
      }
    }
    if (args != null)
    {
      if (this.args == null) {
        this.args = new LinkedList();
      }
      this.args.addAll(args);
    }
    return this;
  }
  
  public SqlClause prepend(SqlClause clause, String open, String close)
  {
    if (clause == null) {
      return this;
    }
    return prepend(clause.getSql(), clause.getArgs(), open, close);
  }
  
  public SqlClause prepend(String sql, List<Object> args, String open, String close)
  {
    if ((sql != null) && (!sql.isEmpty())) {
      if ((this.sql == null) || (this.sql.isEmpty())) {
        this.sql = sql;
      } else {
        this.sql = (sql + open + this.sql + close);
      }
    }
    if (args != null)
    {
      LinkedList<Object> newArgs = new LinkedList(args);
      if (this.args != null) {
        newArgs.addAll(this.args);
      }
      this.args = newArgs;
    }
    return this;
  }
  
  public SqlClause appendArg(Object arg)
  {
    if (this.args == null) {
      this.args = new LinkedList();
    }
    this.args.addLast(arg);
    return this;
  }
  
  public SqlClause prependArg(Object arg)
  {
    if (this.args == null) {
      this.args = new LinkedList();
    }
    this.args.addFirst(arg);
    return this;
  }
  
  private static void setString(PreparedStatement stmt, int index, String value)
    throws SQLException
  {
    int length = value.length();
    if (length > 2048) {
      stmt.setClob(index, new StringReader(value), length);
    } else {
      stmt.setString(index, value);
    }
  }
  
  public String toString()
  {
    return getSql() + ":" + getArgs();
  }
}
