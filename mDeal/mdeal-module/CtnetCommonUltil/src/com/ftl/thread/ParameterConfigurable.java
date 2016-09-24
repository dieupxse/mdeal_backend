package com.ftl.thread;

import com.ftl.dictionary.DictionaryNode;

public abstract interface ParameterConfigurable
{
  public abstract DictionaryNode getParameterDefinition();
  
  public abstract void fillParameter()
    throws Exception;
  
  public abstract void validateParameter()
    throws Exception;
  
  public abstract String getParameter(String paramString);
  
  public abstract void setParameter(String paramString1, String paramString2);
}
