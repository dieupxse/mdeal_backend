package com.ftl.util;

import java.io.File;
import java.io.FileFilter;

public class WildcardFileFilter
  implements FileFilter
{
  private String wildcard;
  private boolean includeDirectory;
  
  public WildcardFileFilter(String wildcard, boolean includeDirectory)
  {
    this.wildcard = wildcard;
    this.includeDirectory = includeDirectory;
  }
  
  public boolean accept(File file)
  {
    if (file.isDirectory())
    {
      if (this.includeDirectory) {
        return true;
      }
      return false;
    }
    return WildcardUtil.match(this.wildcard, file.getName());
  }
}
