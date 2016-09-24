package com.ftl.util;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Cache
{
  public static final long CHECK_INTERVAL = 20000L;
  public static final long CACHE_DURATION = 7200000L;
  private static long nextCheckTime = 0L;
  private static Map cache = new ConcurrentHashMap();
  
  public static Map getCache()
  {
    return cache;
  }
  
  public static synchronized void putCache(Object key, CachedObject obj)
  {
    cache.put(key, obj);
  }
  
  public static CachedObject getCache(Object key)
  {
    clearExpiredCache();
    return (CachedObject)cache.get(key);
  }
  
  public static synchronized void clearCache()
  {
    cache = new ConcurrentHashMap();
  }
  
  public static boolean shouldCheckCacheNow()
  {
    if (System.currentTimeMillis() > nextCheckTime) {
      return true;
    }
    return false;
  }
  
  public static void clearExpiredCache()
  {
    Map map = getCache();
    if (shouldCheckCacheNow())
    {
      Iterator iterator = map.entrySet().iterator();
      while (iterator.hasNext())
      {
        Map.Entry entry = (Map.Entry)iterator.next();
        CachedObject cacheTemp = (CachedObject)entry.getValue();
        if (cacheTemp.expireTime < System.currentTimeMillis()) {
          map.remove(entry.getKey());
        }
      }
      updateCheckTime();
    }
  }
  
  public static synchronized void updateCheckTime()
  {
    nextCheckTime = System.currentTimeMillis() + 20000L;
  }
}
