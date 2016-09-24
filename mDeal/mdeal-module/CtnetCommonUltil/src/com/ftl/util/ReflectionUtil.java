package com.ftl.util;

import com.ftl.dictionary.DictionaryNode;

public class ReflectionUtil
{
  public static <T> Class<T> loadClass(DictionaryNode classNode, Class<T> clazz)
    throws Exception
  {
    return loadClass(classNode.getValue(), clazz, classNode);
  }
  
  public static <T> Class<T> loadClass(String className, Class<T> clazz, DictionaryNode trace)
    throws Exception
  {
    if ((className == null) || (className.isEmpty())) {
      throw new Exception("Value of node " + trace.getNodeName() + " should be name of a " + clazz.getName() + "'s subclass, at " + trace);
    }
    Class cls = null;
    try
    {
      cls = Class.forName(className);
      if (!clazz.isAssignableFrom(cls)) {
        throw new Exception("Class '" + className + "' should be an instanceof " + clazz.getName() + ", at " + trace.getTrace());
      }
    }
    catch (ClassNotFoundException ex)
    {
      throw new Exception("Class '" + className + "' not found, at " + trace.getTrace(), ex);
    }
    return cls;
  }
  
  public static <T> Class<T> loadClass(String className, Class<T> clazz)
    throws Exception
  {
    Class cls = Class.forName(className);
    if (!clazz.isAssignableFrom(cls)) {
      throw new Exception("Class '" + className + "' should be an instanceof " + clazz.getName());
    }
    return cls;
  }
}
