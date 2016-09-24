package com.ftl.dictionary;

import com.ftl.util.StringUtil;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ErrorDictionary
{
  private static final String DICTIONARY_INFO = "resource/com/ftl/dictionary/ERRDictionary.dic";
  private static Map<String, Dictionary> dictionaryMap = new HashMap();
  private static Dictionary currentDictionary;
  private static String currentKey;
  
  static
  {
    loadDictionary();
    setCurrentLanguage("VN");
  }
  
  public static void appendDictionary(String path, boolean replaceOldData)
  {
    Map<String, Dictionary> backupDictionaryMap = new HashMap(dictionaryMap);
    loadDictionary(path);
    for (Map.Entry<String, Dictionary> entry : dictionaryMap.entrySet())
    {
      String key = (String)entry.getKey();
      Dictionary dic = (Dictionary)entry.getValue();
      Dictionary dicBackup = (Dictionary)backupDictionaryMap.remove(key);
      if (dicBackup != null) {
        if (replaceOldData) {
          dic.merge(dicBackup, true, false);
        } else {
          dic.merge(dicBackup, false, false);
        }
      }
    }
    dictionaryMap.putAll(backupDictionaryMap);
    setCurrentLanguage(getCurrentLanguage());
  }
  
  public static void loadDictionary()
  {
    loadDictionary("resource/com/ftl/dictionary/ERRDictionary.dic");
  }
  
  public static void loadDictionary(String path)
  {
    try
    {
        Dictionary dic = new Dictionary(path);
      for (String key : dic.getRoot().getChildNameList()) {
        try
        {
          String language = dic.getString(key + ".Name");
          String iconPath = dic.getString(key + ".Image");
          String fileName = dic.getString(key + ".File");
          addDictionary(fileName, key, language, iconPath);
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }
    }
    catch (Exception e)
    {
      Dictionary dic;
      e.printStackTrace();
    }
  }
  
  public static void addDictionary(String fileName, String key, String language, String iconPath)
  {
    try
    {
      Dictionary dic = new ErrorDictionaryImpl(fileName);
      dic.setLanguage(language);
      dic.setIconPath(iconPath);
      dictionaryMap.remove(key);
      dictionaryMap.put(key, dic);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  
  public static Dictionary getDictionary(String key)
  {
    Dictionary dic = (Dictionary)dictionaryMap.get(key);
    if (dic == null) {
      dic = new ErrorDictionaryImpl();
    }
    return dic;
  }
  
  public static void setDictionary(String key, Dictionary dic)
  {
    dictionaryMap.put(key, dic);
  }
  
  public static Dictionary removeDictionary(String key)
  {
    return (Dictionary)dictionaryMap.remove(key);
  }
  
  public static String[] getSupportedLanguage()
  {
    Object[] languages = dictionaryMap.keySet().toArray();
    String[] result = new String[languages.length];
    for (int index = 0; index < languages.length; index++) {
      result[index] = languages[index].toString();
    }
    return result;
  }
  
  public static void setCurrentLanguage(String key)
  {
    currentKey = key;
    currentDictionary = getDictionary(currentKey);
  }
  
  public static String getCurrentLanguage()
  {
    return currentKey;
  }
  
  public static Dictionary getCurrentDictionary()
  {
    return currentDictionary;
  }
  
  public static String getString(String key)
  {
    return currentDictionary.getString(key);
  }
  
  public static String getString(String key, String param)
  {
    return currentDictionary.getString(key, param);
  }
  
  public static String getString(String key, String param1, String param2)
  {
    return currentDictionary.getString(key, param1, param2);
  }
  
  public static String getString(String key, String[] params)
  {
    return currentDictionary.getString(key, params);
  }
  
  private static class ErrorDictionaryImpl
    extends Dictionary
  {
    public ErrorDictionaryImpl() {}
    
    public ErrorDictionaryImpl(InputStream is)
      throws IOException
    {
      super();
    }
    
    public ErrorDictionaryImpl(String fileName)
      throws IOException
    {
      super();
    }
    
    public String getString(String key)
    {
      if (key == null) {
        return "";
      }
      String result = getStringLike(key);
      if ((result.length() == 0) && (!key.equals("{Unspecified}"))) {
        return StringUtil.nvl(getString("{Unspecified}"), "") + "\r\n" + key;
      }
      return result;
    }
  }
}
