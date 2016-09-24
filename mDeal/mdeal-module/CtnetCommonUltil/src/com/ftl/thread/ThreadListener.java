package com.ftl.thread;

public abstract interface ThreadListener
{
  public abstract void started();
  
  public abstract void finished();
  
  public abstract void destroyed();
}
