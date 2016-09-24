package com.ftl.util;

import java.io.File;
import java.io.FilenameFilter;

public class WildcardFilter
  implements FilenameFilter
{
  private String wildcard;
  
  public WildcardFilter(String wildcard)
  {
    this.wildcard = wildcard;
  }
  
  public boolean accept(File dir, String name)
  {
    return WildcardUtil.match(this.wildcard, name);
  }
}
