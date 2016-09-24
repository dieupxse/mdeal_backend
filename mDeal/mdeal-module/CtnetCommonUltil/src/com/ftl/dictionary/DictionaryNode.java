package com.ftl.dictionary;

import com.ftl.util.AppException;
import com.ftl.util.IntegerHolder;
import com.ftl.util.Log;
import com.ftl.util.StringUtil;
import com.ftl.util.UrlUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.xml.sax.Attributes;

public class DictionaryNode
  implements Cloneable, Map
{
  private static final Logger logger = Logger.getLogger("");
  public static final int TYPE_VALUE = 0;
  public static final int TYPE_REFERENCE = 1;
  public static final int TYPE_VECTOR = 2;
  public static final int TYPE_LONG_STRING = 3;
  public static final String PATH_SEPARATOR_SYMBOL = ".";
  public static final String INDEX_SEPARATOR_SYMBOL = "->";
  public static final String ATTRIBUTE_BEGIN_SYMBOL = ".[";
  public static final String ATTRIBUTE_END_SYMBOL = "]";
  public int type = 0;
  public Dictionary root;
  public String name;
  public String value;
  public List<DictionaryNode> children;
  public Attributes attributes;
  
  public DictionaryNode() {}
  
  public DictionaryNode(Dictionary root)
  {
    this.root = root;
  }
  
  public void addChild(DictionaryNode nd)
  {
    if (this.children == null) {
      this.children = new ArrayList();
    }
    int lineNumber = getLineNumber() + this.children.size();
    this.children.add(nd);
    if (this.root != null) {
      this.root.setLineNumber(nd, lineNumber);
    }
  }
  
  public void setChildList(List<DictionaryNode> children)
  {
    this.children = children;
  }
  
  public static String getPath(DictionaryNode parentNode, DictionaryNode nd)
  {
    for (DictionaryNode childNode : parentNode.getChildList())
    {
      if (childNode == nd) {
        return childNode.name;
      }
      String path = getPath(childNode, nd);
      if (path != null) {
        return childNode.name + "." + path;
      }
    }
    return null;
  }
  
  public String getTrace()
  {
    if (this.root == null) {
      return "Trace is not available";
    }
    return getPath() + ", file " + this.root.getSourceFilePath() + ", line " + getLineNumber();
  }
  
  public String getPath()
  {
    return getPath(this.root, this);
  }
  
  public DictionaryNode getChild(String path, boolean createIfNotExist)
  {
    if ((path == null) || (path.isEmpty())) {
      return null;
    }
    if (this.root != null) {
      this.root.update();
    }
    if (this.type == 1) {
      fillReference();
    }
    int separatorIndex = path.indexOf(".");
    if (separatorIndex < 0)
    {
      path = path.trim();
      int index = path.indexOf("->");
      DictionaryNode childNode;
      if (index < 0)
      {
        Iterator localIterator1;
        if (this.children != null) {
          for (localIterator1 = this.children.iterator(); localIterator1.hasNext();)
          {
            childNode = (DictionaryNode)localIterator1.next();
            if (childNode.name.equals(path)) {
              return childNode;
            }
          }
        }
        if (createIfNotExist)
        {
          DictionaryNode resultNode = new DictionaryNode(this.root);
          resultNode.name = path;
          addChild(resultNode);
          return resultNode;
        }
        return null;
      }
      String indexString = path.substring(index + "->".length(), path.length());
      path = path.substring(0, index);
      index = Integer.parseInt(indexString);
      if (this.children != null) {
        for (DictionaryNode c : this.children) {
          if (c.name.equals(path))
          {
            if (index <= 0) {
              return c;
            }
            index--;
          }
        }
      }
      if (createIfNotExist)
      {
        DictionaryNode resultNode = null;
        while (index >= 0)
        {
          resultNode = new DictionaryNode(this.root);
          resultNode.name = path;
          addChild(resultNode);
          index--;
        }
        return resultNode;
      }
      return null;
    }
    String nodeName = path.substring(0, separatorIndex).trim();
    String childPath = path.substring(separatorIndex + 1, path.length()).trim();
    int index = nodeName.indexOf("->");
    if (index < 0)
    {
      DictionaryNode resultNode = getChild(nodeName, createIfNotExist);
      if (resultNode == null) {
        return null;
      }
      if (childPath.length() > 0) {
        return resultNode.getChild(childPath, createIfNotExist);
      }
      return resultNode;
    }
    String indexString = nodeName.substring(index + "->".length(), nodeName.length());
    nodeName = nodeName.substring(0, index);
    index = Integer.parseInt(indexString);
    if (this.children != null) {
      for (DictionaryNode childNode : this.children) {
        if (childNode.name.equals(nodeName))
        {
          if (index <= 0)
          {
            if (childPath.length() > 0) {
              return childNode.getChild(childPath, createIfNotExist);
            }
            return childNode;
          }
          index--;
        }
      }
    }
    if (createIfNotExist)
    {
      DictionaryNode resultNode = null;
      while (index >= 0)
      {
        resultNode = new DictionaryNode(this.root);
        resultNode.name = nodeName;
        addChild(resultNode);
        index--;
      }
      if (resultNode == null) {
        return null;
      }
      if (childPath.length() > 0) {
        return resultNode.getChild(childPath, createIfNotExist);
      }
      return resultNode;
    }
    return null;
  }
  
  public DictionaryNode getChild(String path)
  {
    return getChild(path, false);
  }
  
  public DictionaryNode getChildLike(String path)
  {
    if (this.root != null) {
      this.root.update();
    }
    if (this.type == 1) {
      fillReference();
    }
    if (path == null) {
      return null;
    }
    DictionaryNode resultNode = null;
    int separatorIndex = path.indexOf(".");
    if (separatorIndex < 0)
    {
      if (this.children != null)
      {
        path = path.trim();
        for (DictionaryNode childNode : this.children) {
          if (path.startsWith(childNode.name)) {
            return childNode;
          }
        }
      }
    }
    else
    {
      String nodeName = path.substring(0, separatorIndex).trim();
      String childPath = path.substring(separatorIndex + 1, path.length()).trim();
      resultNode = getChildLike(nodeName);
      if ((resultNode != null) && (childPath.length() > 0))
      {
        DictionaryNode nd = resultNode.getChildLike(childPath);
        if (nd != null) {
          resultNode = nd;
        }
      }
    }
    return resultNode;
  }
  
  public DictionaryNode setChildValue(String name, String value)
  {
    DictionaryNode nd = getChild(name, true);
    nd.value = value;
    return nd;
  }
  
  public DictionaryNode setChildValue(String name, String value, List<DictionaryNode> children)
  {
    DictionaryNode nd = getChild(name, true);
    nd.value = value;
    nd.children = children;
    return nd;
  }
  
  public DictionaryNode setChildVector(String name, List<Object> values)
  {
    DictionaryNode nd = setChildValue(name, "");
    nd.type = 2;
    for (int index = 0; index < values.size(); index++)
    {
      Object obj = values.get(index);
      if ((obj != null) && ((obj instanceof List))) {
        nd.setChildVector(String.valueOf(index), (List)obj);
      } else {
        nd.setChildValue(String.valueOf(index), (String)obj);
      }
    }
    return nd;
  }
  
  public String getStringLike(String key)
  {
    DictionaryNode nd = getChildLike(key);
    if (nd == null) {
      return "";
    }
    return StringUtil.nvl(nd.getValue(), "");
  }
  
  public String getString(String key)
  {
    if (key == null) {
      return "";
    }
    int result = key.lastIndexOf(".[");
    if (result >= 0)
    {
      String attribute = key.substring(result + ".[".length()).trim();
      if (attribute.endsWith("]")) {
        attribute = attribute.substring(0, attribute.length() - 1).trim();
      }
      key = key.substring(0, result);
      DictionaryNode nd = getChild(key);
      if (nd == null) {
        return "";
      }
      if (nd.attributes == null) {
        return "";
      }
      return StringUtil.nvl(nd.attributes.getValue(attribute), "");
    }
    DictionaryNode nd = getChild(key);
    if (nd == null) {
      return "";
    }
    return StringUtil.nvl(nd.getValue(), "");
  }
  
  public String getString(String key, String parameter)
  {
    return StringUtil.replaceAll(StringUtil.replaceAll(getString(key), "<%p>", parameter), "<%p0>", parameter);
  }
  
  public String getString(String key, String parameter1, String parameter2)
  {
    String[] parameters = { parameter1, parameter2 };
    return getString(key, parameters);
  }
  
  public String getString(String key, String[] parameters)
  {
    String tempValue = getString(key);
    if (parameters != null) {
      for (int i = 0; i < parameters.length; i++)
      {
        tempValue = StringUtil.replaceAll(tempValue, "<%p>", parameters[i], 1);
        tempValue = StringUtil.replaceAll(tempValue, "<%p" + i + ">", parameters[i]);
      }
    }
    return tempValue;
  }
  
  public String getString(Exception e)
  {
    return getString(e, true);
  }
  
  public String getString(Exception e, boolean returnErrorMessageIfNotFound)
  {
    if (e == null) {
      return "";
    }
    String key = e.getClass().getName();
    int index = key.lastIndexOf('.');
    if (index >= 0) {
      if (index < key.length()) {
        key = key.substring(index + 1, key.length());
      } else {
        key = "";
      }
    }
    String localValue = getStringLike("{" + key + "}");
    if ((localValue != null) && (localValue.length() > 0))
    {
      localValue = StringUtil.replaceAll(localValue, "<%p>", e.getMessage());
      localValue = StringUtil.replaceAll(localValue, "<%p0>", e.getMessage());
      return localValue;
    }
    if ((e instanceof AppException))
    {
      localValue = getStringLike(((AppException)e).getReason());
      String[] parameters = ((AppException)e).getInfoAsArray();
      if (parameters != null) {
        for (int i = 0; i < parameters.length; i++)
        {
          localValue = StringUtil.replaceAll(localValue, "<%p>", parameters[i], 1);
          localValue = StringUtil.replaceAll(localValue, "<%p" + i + ">", parameters[i]);
        }
      }
      if ((localValue != null) && (localValue.length() > 0))
      {
        Map data = ((AppException)e).getData();
        if ((data != null) && (data.size() > 0)) {
          return StringUtil.replaceAll(localValue, data);
        }
        return localValue;
      }
    }
    else
    {
      localValue = getStringLike(e.getMessage());
    }
    if ((localValue != null) && (localValue.length() > 0)) {
      return localValue;
    }
    if (returnErrorMessageIfNotFound) {
      return e.getMessage();
    }
    return "";
  }
  
  public DictionaryNode removeChild(String path)
  {
    if ((path == null) || (path.isEmpty())) {
      throw new IllegalArgumentException("Path to remove can not be null or empty");
    }
    if (this.type == 1) {
      fillReference();
    }
    String nodeName;
    String childPath;
    int index;
    if (this.children != null)
    {
      int separatorIndex = path.indexOf(".");
      if (separatorIndex < 0)
      {
        path = path.trim();
        index = path.indexOf("->");
        if (index < 0)
        {
          for (int childIndex = 0; childIndex < this.children.size(); childIndex++)
          {
            DictionaryNode childNode = (DictionaryNode)this.children.get(childIndex);
            if (childNode.name.equals(path)) {
              return (DictionaryNode)this.children.remove(childIndex);
            }
          }
        }
        else
        {
          String indexString = path.substring(index + "->".length(), path.length());
          path = path.substring(0, index);
          index = Integer.parseInt(indexString);
          for (int childIndex = 0; childIndex < this.children.size(); childIndex++)
          {
            DictionaryNode resultNode = (DictionaryNode)this.children.get(childIndex);
            if (resultNode.name.equals(path))
            {
              if (index <= 0) {
                return (DictionaryNode)this.children.remove(childIndex);
              }
              index--;
            }
          }
        }
      }
      else
      {
        nodeName = path.substring(0, separatorIndex).trim();
        childPath = path.substring(separatorIndex + 1, path.length()).trim();
        if (childPath.length() > 0)
        {
          index = nodeName.indexOf("->");
          if (index < 0)
          {
            DictionaryNode resultNode = getChild(nodeName);
            if (resultNode != null) {
              return resultNode.removeChild(childPath);
            }
          }
          else
          {
            String indexString = nodeName.substring(index + "->".length(), nodeName.length());
            nodeName = nodeName.substring(0, index);
            index = Integer.parseInt(indexString);
            for (DictionaryNode resultNode : this.children) {
              if (resultNode.name.equals(nodeName))
              {
                if (index <= 0) {
                  return resultNode.removeChild(childPath);
                }
                index--;
              }
            }
          }
        }
      }
    }
    return null;
  }
  
  public DictionaryNode removeChild(DictionaryNode nd)
  {
    if (this.type == 1) {
      fillReference();
    }
    if (this.children != null) {
      for (int childIndex = 0; childIndex < this.children.size(); childIndex++)
      {
        DictionaryNode childNode = (DictionaryNode)this.children.get(childIndex);
        if (childNode == nd) {
          return (DictionaryNode)this.children.remove(childIndex);
        }
        DictionaryNode ndTemp = childNode.removeChild(nd);
        if (ndTemp != null) {
          return ndTemp;
        }
      }
    }
    return null;
  }
  
  public void setString(String key, String value)
  {
    if (key != null) {
      getChild(key, true).value = value;
    }
  }
  
  public DictionaryNode getChildIgnoreCase(String path, boolean createIfNotExist)
  {
    if (this.root != null) {
      this.root.update();
    }
    if (this.type == 1) {
      fillReference();
    }
    int separatorIndex = path.indexOf(".");
    if (separatorIndex < 0)
    {
      path = path.trim();
      int index = path.indexOf("->");
      DictionaryNode childNode;
      if (index < 0)
      {
        Iterator localIterator1;
        if (this.children != null) {
          for (localIterator1 = this.children.iterator(); localIterator1.hasNext();)
          {
            childNode = (DictionaryNode)localIterator1.next();
            if (childNode.name.equalsIgnoreCase(path)) {
              return childNode;
            }
          }
        }
        if (createIfNotExist)
        {
          DictionaryNode resultNode = new DictionaryNode(this.root);
          resultNode.name = path;
          addChild(resultNode);
          return resultNode;
        }
        return null;
      }
      String indexString = path.substring(index + "->".length(), path.length());
      path = path.substring(0, index);
      index = Integer.parseInt(indexString);
      if (this.children != null) {
        for (DictionaryNode resultNode : this.children) {
          if (resultNode.name.equalsIgnoreCase(path))
          {
            if (index <= 0) {
              return resultNode;
            }
            index--;
          }
        }
      }
      if (createIfNotExist)
      {
        DictionaryNode resultNode = null;
        while (index >= 0)
        {
          resultNode = new DictionaryNode(this.root);
          resultNode.name = path;
          addChild(resultNode);
          index--;
        }
        return resultNode;
      }
      return null;
    }
    String nodeName = path.substring(0, separatorIndex).trim();
    String childPath = path.substring(separatorIndex + 1, path.length()).trim();
    int index = nodeName.indexOf("->");
    if (index < 0)
    {
      DictionaryNode resultNode = getChildIgnoreCase(nodeName, createIfNotExist);
      if (resultNode == null) {
        return null;
      }
      if (childPath.length() > 0) {
        return resultNode.getChildIgnoreCase(childPath, createIfNotExist);
      }
      return resultNode;
    }
    String indexString = nodeName.substring(index + "->".length(), nodeName.length());
    nodeName = nodeName.substring(0, index);
    index = Integer.parseInt(indexString);
    if (this.children != null) {
      for (DictionaryNode resultNode : this.children) {
        if (resultNode.name.equalsIgnoreCase(nodeName))
        {
          if (index <= 0)
          {
            if (childPath.length() > 0) {
              return resultNode.getChildIgnoreCase(childPath, createIfNotExist);
            }
            return resultNode;
          }
          index--;
        }
      }
    }
    if (createIfNotExist)
    {
      DictionaryNode resultNode = null;
      while (index >= 0)
      {
        resultNode = new DictionaryNode(this.root);
        resultNode.name = nodeName;
        addChild(resultNode);
        index--;
      }
      if (resultNode == null) {
        return null;
      }
      if (childPath.length() > 0) {
        return resultNode.getChildIgnoreCase(childPath, createIfNotExist);
      }
      return resultNode;
    }
    return null;
  }
  
  public DictionaryNode getChildIgnoreCase(String path)
  {
    return getChildIgnoreCase(path, false);
  }
  
  public DictionaryNode getChildLikeIgnoreCase(String path)
  {
    if (this.root != null) {
      this.root.update();
    }
    if (this.type == 1) {
      fillReference();
    }
    if (path == null) {
      return null;
    }
    DictionaryNode resultNode = null;
    int separatorIndex = path.indexOf(".");
    if (separatorIndex < 0)
    {
      if (this.children != null)
      {
        path = path.trim();
        for (DictionaryNode childNode : this.children) {
          if (path.startsWith(childNode.name.toUpperCase())) {
            return childNode;
          }
        }
      }
    }
    else
    {
      String nodeName = path.substring(0, separatorIndex).trim();
      String childPath = path.substring(separatorIndex + 1, path.length()).trim();
      resultNode = getChildLikeIgnoreCase(nodeName);
      if ((resultNode != null) && (childPath.length() > 0))
      {
        DictionaryNode nd = resultNode.getChildLikeIgnoreCase(childPath);
        if (nd != null) {
          resultNode = nd;
        }
      }
    }
    return resultNode;
  }
  
  public DictionaryNode setChildValueIgnoreCase(String name, String value)
  {
    DictionaryNode nd = getChildIgnoreCase(name, true);
    nd.value = value;
    return nd;
  }
  
  public DictionaryNode setChildVectorIgnoreCase(String name, List<Object> values)
  {
    DictionaryNode nd = setChildValueIgnoreCase(name, "");
    nd.type = 2;
    for (int index = 0; index < values.size(); index++)
    {
      Object obj = values.get(index);
      if ((obj != null) && ((obj instanceof List))) {
        nd.setChildVectorIgnoreCase(String.valueOf(index), (List)obj);
      } else {
        nd.setChildValueIgnoreCase(String.valueOf(index), (String)obj);
      }
    }
    return nd;
  }
  
  public DictionaryNode setChildValueIgnoreCase(String name, String value, List<DictionaryNode> children)
  {
    DictionaryNode nd = getChildIgnoreCase(name, true);
    nd.value = value;
    nd.children = children;
    return nd;
  }
  
  public String getStringLikeIgnoreCase(String key)
  {
    DictionaryNode nd = getChildLikeIgnoreCase(key);
    if (nd == null) {
      return "";
    }
    return StringUtil.nvl(nd.getValue(), "");
  }
  
  public String getStringIgnoreCase(String key)
  {
    if (key == null) {
      return "";
    }
    DictionaryNode nd = getChildIgnoreCase(key);
    if (nd == null) {
      return "";
    }
    return StringUtil.nvl(nd.getValue(), "");
  }
  
  public String getStringIgnoreCase(String key, String parameter)
  {
    return StringUtil.replaceAll(StringUtil.replaceAll(getStringIgnoreCase(key), "<%p>", parameter), "<%p0>", parameter);
  }
  
  public String getStringIgnoreCase(String key, String parameter1, String parameter2)
  {
    String[] parameters = { parameter1, parameter2 };
    return getStringIgnoreCase(key, parameters);
  }
  
  public String getStringIgnoreCase(String key, String[] parameters)
  {
    String tempValue = getStringIgnoreCase(key);
    if (parameters != null) {
      for (int i = 0; i < parameters.length; i++)
      {
        tempValue = StringUtil.replaceAll(tempValue, "<%p>", parameters[i], 1);
        tempValue = StringUtil.replaceAll(tempValue, "<%p" + i + ">", parameters[i]);
      }
    }
    return tempValue;
  }
  
  public String getStringIgnoreCase(Exception e)
  {
    if (e == null) {
      return "";
    }
    String key = e.getClass().getName();
    int index = key.lastIndexOf('.');
    if (index >= 0) {
      if (index < key.length()) {
        key = key.substring(index + 1, key.length());
      } else {
        key = "";
      }
    }
    String localValue = getStringLikeIgnoreCase("{" + key + "}");
    if ((localValue != null) && (localValue.length() > 0))
    {
      localValue = StringUtil.replaceAll(localValue, "<%p>", e.getMessage());
      localValue = StringUtil.replaceAll(localValue, "<%p0>", e.getMessage());
      return localValue;
    }
    if ((e instanceof AppException))
    {
      localValue = getStringLikeIgnoreCase(((AppException)e).getReason());
      String[] parameters = ((AppException)e).getInfoAsArray();
      if (parameters != null) {
        for (int i = 0; i < parameters.length; i++)
        {
          localValue = StringUtil.replaceAll(localValue, "<%p>", parameters[i], 1);
          localValue = StringUtil.replaceAll(localValue, "<%p" + i + ">", parameters[i]);
        }
      }
      if ((localValue != null) && (localValue.length() > 0))
      {
        Map data = ((AppException)e).getData();
        if ((data != null) && (data.size() > 0)) {
          return StringUtil.replaceAll(localValue, data);
        }
        return localValue;
      }
    }
    else
    {
      localValue = getStringLikeIgnoreCase(e.getMessage());
    }
    if ((localValue != null) && (localValue.length() > 0)) {
      return localValue;
    }
    return e.getMessage();
  }
  
  public DictionaryNode removeChildIgnoreCase(String path)
  {
    if (this.type == 1) {
      fillReference();
    }
    String nodeName;
    String childPath;
    int index;
    if (this.children != null)
    {
      int separatorIndex = path.indexOf(".");
      if (separatorIndex < 0)
      {
        path = path.trim();
        index = path.indexOf("->");
        if (index < 0)
        {
          for (int childIndex = 0; childIndex < this.children.size(); childIndex++)
          {
            DictionaryNode childNode = (DictionaryNode)this.children.get(childIndex);
            if (childNode.name.equalsIgnoreCase(path)) {
              return (DictionaryNode)this.children.remove(childIndex);
            }
          }
        }
        else
        {
          String indexString = path.substring(index + "->".length(), path.length());
          path = path.substring(0, index);
          index = Integer.parseInt(indexString);
          for (int childIndex = 0; childIndex < this.children.size(); childIndex++)
          {
            DictionaryNode resultNode = (DictionaryNode)this.children.get(childIndex);
            if (resultNode.name.equalsIgnoreCase(path))
            {
              if (index <= 0) {
                return (DictionaryNode)this.children.remove(childIndex);
              }
              index--;
            }
          }
        }
      }
      else
      {
        nodeName = path.substring(0, separatorIndex).trim();
        childPath = path.substring(separatorIndex + 1, path.length()).trim();
        if (childPath.length() > 0)
        {
          index = nodeName.indexOf("->");
          if (index < 0)
          {
            DictionaryNode resultNode = getChildIgnoreCase(nodeName);
            if (resultNode != null) {
              return resultNode.removeChildIgnoreCase(childPath);
            }
          }
          else
          {
            String indexString = nodeName.substring(index + "->".length(), nodeName.length());
            nodeName = nodeName.substring(0, index);
            index = Integer.parseInt(indexString);
            for (DictionaryNode resultNode : this.children) {
              if (resultNode.name.equalsIgnoreCase(nodeName))
              {
                if (index <= 0) {
                  return resultNode.removeChildIgnoreCase(childPath);
                }
                index--;
              }
            }
          }
        }
      }
    }
    return null;
  }
  
  public void setStringIgnoreCase(String key, String value)
  {
    if (key != null) {
      getChildIgnoreCase(key, true).value = value;
    }
  }
  
  public List<String> getChildNameList()
  {
    List<String> result = new ArrayList();
    for (DictionaryNode child : getChildList()) {
      result.add(child.name);
    }
    return result;
  }
  
  public List<String> getChildValueList()
  {
    List<String> result = new ArrayList();
    for (DictionaryNode child : getChildList()) {
      result.add(child.value);
    }
    return result;
  }
  
  public List<DictionaryNode> getChildList()
  {
    if (this.type == 1) {
      fillReference();
    }
    if (this.children != null) {
      return new ArrayList(this.children);
    }
    return new ArrayList();
  }
  
  protected void fillReference()
  {
    fillReference(this);
  }
  
  private void fillReference(DictionaryNode firstNode)
  {
    if (this.root == null) {
      return;
    }
    String localValue = this.value;
    this.type = 0;
    this.value = "";
    if ((localValue != null) && (!localValue.isEmpty())) {
      for (String referenceKey : StringUtil.toStringVector(localValue)) {
        if ((referenceKey != null) && (!referenceKey.isEmpty()))
        {
          boolean reverse = false;
          if (referenceKey.startsWith("!"))
          {
            referenceKey = referenceKey.substring(1);
            reverse = true;
          }
          DictionaryNode referenceNode = null;
          int separatorIndex = referenceKey.indexOf('!');
          if (separatorIndex < 0)
          {
            referenceNode = this.root.getChild(referenceKey);
          }
          else
          {
            String fileName = referenceKey.substring(0, separatorIndex).trim();
            if (!UrlUtil.ROOT_PATTERN.matcher(fileName).matches())
            {
              String path = this.root.getRelativeFilePath();
              if (path != null)
              {
                path = path.replace('\\', '/');
                int lastSlashIndex = path.lastIndexOf('/');
                if (lastSlashIndex >= 0)
                {
                  path = path.substring(0, lastSlashIndex + 1);
                  if ((path != null) && (!path.isEmpty())) {
                    fileName = path + fileName;
                  }
                }
              }
            }
            if (separatorIndex + 1 < referenceKey.length()) {
              referenceKey = referenceKey.substring(separatorIndex + 1, referenceKey.length());
            } else {
              referenceKey = null;
            }
            Dictionary dic = Dictionary.getCachedDictionary(fileName, this.root.getCache());
            if (dic == null) {
              logger.log(Level.WARNING, "Resource ''{0}'' is not available, at {1}", new Object[] { fileName, getTrace() });
            } else if ((referenceKey != null) && (!referenceKey.isEmpty())) {
              referenceNode = dic.getChild(referenceKey);
            } else {
              referenceNode = dic;
            }
          }
          if (referenceNode != null)
          {
            if (referenceNode == firstNode)
            {
              logger.log(Level.WARNING, "Reference to ''{0}'' is cycle reference, at {1}", new Object[] { referenceKey, getTrace() });
            }
            else
            {
              merge(referenceNode, reverse, false);
              localValue = referenceNode.getValue(firstNode);
              if (localValue != null) {
                this.value = localValue;
              }
            }
          }
          else {
            logger.log(Level.WARNING, "Reference to ''{0}'' is invalid, at {1}", new Object[] { referenceKey, getTrace() });
          }
        }
      }
    }
  }
  
  public void setValue(String value)
  {
    this.value = value;
  }
  
  public String getValue()
  {
    return getValue(this);
  }
  
  private String getValue(DictionaryNode firstNode)
  {
    if (this.type == 1) {
      fillReference(firstNode);
    }
    if (this.value != null) {
      return this.value;
    }
    return "";
  }
  
  public Dictionary getRoot()
  {
    return this.root;
  }
  
  public String getNodeName()
  {
    return this.name;
  }
  
  public void setNodeName(String nodeName)
  {
    this.name = nodeName;
  }
  
  public Attributes getAttributes()
  {
    return this.attributes;
  }
  
  public void setAttributes(Attributes attributes)
  {
    this.attributes = attributes;
  }
  
  public List<Object> getVector()
  {
    if (this.type == 1) {
      fillReference();
    }
    List<Object> result = new ArrayList();
    if (this.children != null) {
      for (DictionaryNode nd : this.children) {
        if ((nd.children != null) && (nd.children.size() > 0)) {
          result.add(nd.getVector());
        } else {
          result.add(nd.getValue());
        }
      }
    }
    return result;
  }
  
  public void merge(DictionaryNode targetNode, boolean replaceIfExist, boolean cleanup)
  {
    if (targetNode == null) {
      return;
    }
    if (targetNode.children == null)
    {
      if (cleanup) {
        this.children = null;
      }
      return;
    }
    Map<String, IntegerHolder> indexMap = new HashMap();
    List<DictionaryNode> mergedNodes = new ArrayList();
    for (DictionaryNode targetChildNode : targetNode.getChildList())
    {
      IntegerHolder index = (IntegerHolder)indexMap.get(targetChildNode.name);
      DictionaryNode sourceChildNode;
      if (index == null)
      {
        sourceChildNode = getChild(targetChildNode.name);
        index = new IntegerHolder(0);
        indexMap.put(targetChildNode.name, index);
      }
      else
      {
        sourceChildNode = getChild(targetChildNode.name + "->" + 
        
          String.valueOf(index.value));
      }
      index.value += 1;
      if (sourceChildNode == null)
      {
        if (cleanup) {
          mergedNodes.add(targetChildNode);
        } else {
          addChild(targetChildNode);
        }
      }
      else
      {
        if (cleanup) {
          mergedNodes.add(sourceChildNode);
        }
        if (replaceIfExist)
        {
          sourceChildNode.name = targetChildNode.name;
          sourceChildNode.value = targetChildNode.value;
          sourceChildNode.type = targetChildNode.type;
        }
        sourceChildNode.merge(targetChildNode, replaceIfExist, cleanup);
      }
    }
    if (cleanup) {
      if (this.children != null)
      {
        this.children.clear();
        this.children.addAll(mergedNodes);
      }
      else
      {
        this.children = mergedNodes;
      }
    }
  }
  
  public String getComment()
  {
    if (this.root != null) {
      return this.root.getComment(this);
    }
    return null;
  }
  
  public void setComment(String comment)
  {
    if (this.root != null) {
      this.root.setComment(this, comment);
    }
  }
  
  public int getLineNumber()
  {
    if (this.root != null) {
      return this.root.getLineNumber(this);
    }
    return -1;
  }
  
  public Map toMap()
  {
    Map map = new LinkedHashMap();
    for (DictionaryNode child : getChildList()) {
      if (child.type == 2)
      {
        map.put(child.name, child.getVector());
      }
      else
      {
        String localValue = child.getValue();
        if (localValue != null) {
          map.put(child.name, localValue);
        }
      }
    }
    return map;
  }
  
  public void copyDictionaryData(DictionaryNode nd)
  {
    this.type = nd.type;
    this.name = nd.name;
    this.value = nd.value;
    this.children = nd.children;
  }
  
  public DictionaryNode createCopy()
  {
    DictionaryNode nd = new DictionaryNode();
    nd.type = this.type;
    nd.name = this.name;
    nd.value = this.value;
    if (this.children == null)
    {
      nd.children = null;
    }
    else
    {
      nd.children = new ArrayList();
      for (DictionaryNode child : this.children) {
        nd.children.add(child.createCopy());
      }
    }
    return nd;
  }
  
  public List<DictionaryNode> findChild(String name, String value)
  {
    for (DictionaryNode nd : getChildList())
    {
      if (nd.isEquals(name, value))
      {
        List<DictionaryNode> result = new ArrayList();
        result.add(nd);
        return result;
      }
      List<DictionaryNode> localChildren = nd.findChild(name, value);
      if (localChildren != null)
      {
        localChildren.add(nd);
        return localChildren;
      }
    }
    return null;
  }
  
  public List findChildIgnoreNull(String name, String value)
  {
    for (DictionaryNode nd : getChildList())
    {
      if (nd.isEqualsIgnoreNull(name, value))
      {
        List<DictionaryNode> result = new ArrayList();
        result.add(nd);
        return result;
      }
      List<DictionaryNode> localChildren = nd.findChildIgnoreNull(name, value);
      if (localChildren != null)
      {
        localChildren.add(nd);
        return localChildren;
      }
    }
    return null;
  }
  
  public boolean isEquals(String name, String value)
  {
    return ((name == null) && (this.name == null)) || ((name != null) && (name.equals(this.name)) && (((value == null) && (this.value == null)) || ((value != null) && (value.equals(this.value)))));
  }
  
  public boolean isEqualsIgnoreNull(String name, String value)
  {
    return ((name == null) || (name.equals(this.name))) && ((value == null) || (value.equals(this.value)));
  }
  
  public Object clone()
    throws CloneNotSupportedException
  {
    return super.clone();
  }
  
  public void putAll(Map map)
  {
    if (map == null) {
      return;
    }
    Iterator iterator = map.entrySet().iterator();
    while (iterator.hasNext())
    {
      Map.Entry entry = (Map.Entry)iterator.next();
      setChildValue((String)entry.getKey(), StringUtil.nvl(entry.getValue(), ""));
    }
  }
  
  public int size()
  {
    return this.children == null ? 0 : this.children.size();
  }
  
  public boolean isEmpty()
  {
    return (this.children == null) || (this.children.isEmpty());
  }
  
  public boolean containsKey(Object key)
  {
    return getChild((String)key) != null;
  }
  
  public boolean containsValue(Object value)
  {
    return false;
  }
  
  public Object get(Object key)
  {
    return getString((String)key);
  }
  
  public Object put(Object key, Object value)
  {
    setString((String)key, (String)value);
    return value;
  }
  
  public Object remove(Object key)
  {
    return removeChild(key.toString());
  }
  
  public void clear()
  {
    if (this.children != null) {
      this.children.clear();
    }
  }
  
  public Set keySet()
  {
    return new HashSet(getChildNameList());
  }
  
  public Collection values()
  {
    return getChildValueList();
  }
  
  public Set entrySet()
  {
    return null;
  }
  
  public String toString()
  {
    if (this.type == 1) {
      return this.name + "=>" + this.value;
    }
    if (this.type == 3) {
      return this.name + "=:\n:{\n\t" + this.value + "\n:}";
    }
    return this.name + "==" + this.value;
  }
}
