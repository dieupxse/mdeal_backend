package com.ftl.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CollectionUtil
{
  public static List toVector(Object[] array)
  {
    if (array == null) {
      return null;
    }
    List result = new ArrayList(array.length);
    result.addAll(Arrays.asList(array));
    return result;
  }
  
  public static List to2DVector(Object[][] array)
  {
    if (array == null) {
      return null;
    }
    List result = new ArrayList(array.length);
    for (Object[] element : array) {
      result.add(toVector(element));
    }
    return result;
  }
  
  public static List convert1DVectorTo2DVector(List data)
  {
    List result = new ArrayList(data.size());
    for (Object element : data)
    {
      List row = new ArrayList(1);
      row.add(element);
      result.add(row);
    }
    return result;
  }
  
  public static Map createMapFrom2DVector(List<List> data, int keyIndex, int valueIndex)
  {
    LinkedHashMap map = new LinkedHashMap(data.size());
    fillMapDataFrom2DVector(map, data, keyIndex, valueIndex);
    return map;
  }
  
  public static void fillMapDataFrom2DVector(Map map, List<List> data, int keyIndex, int valueIndex)
  {
    for (List row : data) {
      map.put(row.get(keyIndex), row.get(valueIndex));
    }
  }
  
  public static List getColumnDataFrom2DVector(List<List> data, int columnIndex)
  {
    if (data == null) {
      return null;
    }
    List result = new ArrayList(data.size());
    for (List row : data) {
      result.add(row.get(columnIndex));
    }
    return result;
  }
  
  public static List cloneVector(List data)
  {
    if (data == null) {
      return null;
    }
    List result = new ArrayList(data.size());
    for (Object element : data) {
      if ((element instanceof List)) {
        result.add(cloneVector((List)element));
      } else {
        result.add(element);
      }
    }
    return result;
  }
  
  public static boolean isSimilar(List source, List target, int[] keyIndexes)
  {
    if (source == target) {
      return true;
    }
    if (source.size() != target.size()) {
      return false;
    }
    for (int columnIndex : keyIndexes) {
      if (!isSimilarObject(source.get(columnIndex), target.get(columnIndex))) {
        return false;
      }
    }
    return true;
  }
  
  public static boolean isSimilar(List source, List target)
  {
    if (source == target) {
      return true;
    }
    if (source.size() != target.size()) {
      return false;
    }
    for (int index = 0; index < source.size(); index++) {
      if (!isSimilarObject(source.get(index), target.get(index))) {
        return false;
      }
    }
    return true;
  }
  
  private static boolean isSimilarObject(Object source, Object target)
  {
    if ((source == null) && (target == null)) {
      return true;
    }
    if ((source == null) || (target == null) || 
      (source.getClass() != target.getClass()) || (((source instanceof List)) && 
      (!isSimilar((List)source, (List)target))) || 
      (!source.equals(target))) {
      return false;
    }
    return true;
  }
  
  public static List getChangedData(List<List> newData, List<List> oldData)
  {
    List result = new ArrayList();
    if (newData == oldData) {
      return result;
    }
    List<List> tempData = new ArrayList(oldData);
    for (List newRow : newData)
    {
      List similarRow = null;
      for (List oldRow : tempData) {
        if (isSimilar(newRow, oldRow))
        {
          similarRow = oldRow;
          break;
        }
      }
      if (similarRow == null) {
        result.add(newRow);
      } else {
        tempData.remove(similarRow);
      }
    }
    return result;
  }
  
  public static List getChangedData(List<List> newData, List<List> oldData, int[] uniqueIndexes)
  {
    List result = new ArrayList();
    if (newData == oldData) {
      return result;
    }
    List newUniqueValues = new ArrayList(uniqueIndexes.length);
    List oldUniqueValues = new ArrayList(uniqueIndexes.length);
    List<List> tempData = new ArrayList(oldData);
    for (List newRow : newData)
    {
      newUniqueValues.clear();
      for (int keyIndex : uniqueIndexes) {
        newUniqueValues.add(newRow.get(keyIndex));
      }
      if (isEmpty(newUniqueValues))
      {
        result.add(newRow);
      }
      else
      {
        List similarRow = null;
        for (List oldRow : tempData)
        {
          oldUniqueValues.clear();
          for (int keyIndex : uniqueIndexes) {
            oldUniqueValues.add(oldRow.get(keyIndex));
          }
          if (isSimilar(oldUniqueValues, newUniqueValues))
          {
            similarRow = oldRow;
            break;
          }
        }
        if (similarRow == null) {
          result.add(newRow);
        } else {
          tempData.remove(similarRow);
        }
      }
    }
    return result;
  }
  
  public static List getChangedData(List<List> newData, List<List> oldData, int uniqueIndex)
  {
    List result = new ArrayList();
    if (newData == oldData) {
      return result;
    }
    List<List> tempData = new ArrayList(oldData);
    for (List newRow : newData)
    {
      Object newUniqueValue = newRow.get(uniqueIndex);
      if ((newUniqueValue == null) || (newUniqueValue.equals("")))
      {
        result.add(newRow);
      }
      else
      {
        List similarRow = null;
        for (List oldRow : tempData)
        {
          Object oldUniqueValue = oldRow.get(uniqueIndex);
          if (isSimilarObject(oldUniqueValue, newUniqueValue))
          {
            similarRow = oldRow;
            break;
          }
        }
        if (similarRow == null) {
          result.add(newRow);
        } else {
          tempData.remove(similarRow);
        }
      }
    }
    return result;
  }
  
  public static int indexOf(List data, Object value)
  {
    for (int index = 0; index < data.size(); index++) {
      if (isSimilarObject(data.get(index), value)) {
        return index;
      }
    }
    return -1;
  }
  
  public static void sort2DVector(List<List> data, int[] columnIndexes)
  {
    sort2DVector(data, columnIndexes, new boolean[columnIndexes.length]);
  }
  
  public static void sort2DVector(List<List> data, int[] columnIndexes, boolean[] descendingColumns)
  {
    if ((data == null) || (data.size() <= 0)) {
      return;
    }
    Collections.sort(data, new TwoDimensionVectorComparator(columnIndexes, descendingColumns));
  }
  
  public static int binarySearch2DVector(List<List> data, List keyValues, int[] keyIndexes)
  {
    return binarySearch2DVector(data, keyValues, keyIndexes, new boolean[keyIndexes.length]);
  }
  
  public static int binarySearch2DVector(List<List> data, List keyValues, int[] keyIndexes, boolean[] descendingColumns)
  {
    sort2DVector(data, keyIndexes, descendingColumns);
    return binarySearchSorted2DVector(data, keyValues, keyIndexes, descendingColumns);
  }
  
  public static int binarySearchSorted2DVector(List<List> data, List keyValues, int[] keyIndexes)
  {
    return binarySearchSorted2DVector(data, keyValues, keyIndexes, new boolean[keyIndexes.length]);
  }
  
  public static int binarySearchSorted2DVector(List<List> data, List keyValues, int[] keyIndexes, boolean[] descendingColumns)
  {
    return Collections.binarySearch(data, keyValues, new TwoDimensionVectorComparator(keyIndexes, descendingColumns));
  }
  
  public static List merge2DVector(List<List> data, int[] keyIndexes)
  {
    return merge2DVector(data, keyIndexes, new boolean[keyIndexes.length]);
  }
  
  public static List merge2DVector(List<List> data, int[] keyIndexes, boolean[] descendingColumns)
  {
    sort2DVector(data, keyIndexes, descendingColumns);
    return mergeSorted2DVector(data, keyIndexes);
  }
  
  public static List mergeSorted2DVector(List<List> data, int[] keyIndexes)
  {
    return mergeSorted2DVector(data, keyIndexes, 0);
  }
  
  private static List mergeSorted2DVector(List<List> data, int[] keyIndexes, int level)
  {
    if (data == null) {
      return null;
    }
    if (level >= keyIndexes.length) {
      return data;
    }
    int keyIndex = keyIndexes[level];
    List result = new ArrayList();
    List childData = new ArrayList();
    boolean isFirstRow = true;
    Object lastKey = null;
    for (List row : data)
    {
      Object key = row.get(keyIndex);
      if (isFirstRow)
      {
        isFirstRow = false;
        lastKey = key;
      }
      else if (!isSimilarObject(lastKey, key))
      {
        List resultRow = new ArrayList(2);
        resultRow.add(lastKey);
        int nextLevel = level + 1;
        if (nextLevel < keyIndexes.length) {
          resultRow.add(mergeSorted2DVector(childData, keyIndexes, nextLevel));
        } else {
          resultRow.add(childData);
        }
        result.add(resultRow);
        lastKey = key;
        childData = new ArrayList();
      }
      childData.add(row);
    }
    if (!childData.isEmpty())
    {
      Object resultRow = new ArrayList(2);
      ((List)resultRow).add(lastKey);
      int nextLevel = level + 1;
      if (nextLevel < keyIndexes.length) {
        ((List)resultRow).add(mergeSorted2DVector(childData, keyIndexes, nextLevel));
      } else {
        ((List)resultRow).add(childData);
      }
      result.add(resultRow);
    }
    return result;
  }
  
  public static List filter2DVector(List<List> data, List keyValues, int[] keyIndexes)
  {
    return filter2DVector(data, keyValues, keyIndexes, new boolean[keyIndexes.length]);
  }
  
  public static List filter2DVector(List<List> data, List keyValues, int[] keyIndexes, boolean[] descendingColumns)
  {
    sort2DVector(data, keyIndexes, descendingColumns);
    return filterSorted2DVector(data, keyValues, keyIndexes, descendingColumns);
  }
  
  public static List filterSorted2DVector(List<List> data, List keyValues, int[] keyIndexes)
  {
    return filterSorted2DVector(data, keyValues, keyIndexes, new boolean[keyIndexes.length]);
  }
  
  public static List filterSorted2DVector(List<List> data, List keyValues, int[] keyIndexes, boolean[] descendingColumns)
  {
    if (data == null) {
      return null;
    }
    Comparator comparator = new TwoDimensionVectorComparator(keyIndexes, descendingColumns);
    int index = Collections.binarySearch(data, keyValues, comparator);
    if (index < 0) {
      return new ArrayList();
    }
    int lowIndex = index - 1;
    while (lowIndex >= 0)
    {
      if (comparator.compare(data.get(lowIndex), keyValues) != 0) {
        break;
      }
      lowIndex--;
    }
    lowIndex++;
    
    int highIndex = index + 1;
    while (highIndex < data.size())
    {
      if (comparator.compare(data.get(highIndex), keyValues) != 0) {
        break;
      }
      highIndex++;
    }
    return data.subList(lowIndex, highIndex);
  }
  
  public static List filterUnique2DVector(List<List> data, int[] keyIndexes)
  {
    return filterUnique2DVector(data, keyIndexes, new boolean[keyIndexes.length]);
  }
  
  public static List filterUnique2DVector(List<List> data, int[] keyIndexes, boolean[] descendingColumns)
  {
    sort2DVector(data, keyIndexes, descendingColumns);
    return filterUniqueSorted2DVector(data, keyIndexes);
  }
  
  public static List filterUniqueSorted2DVector(List<List> data, int[] keyIndexes)
  {
    return getAllFirstElementFromMergedVector(
      mergeSorted2DVector(data, keyIndexes), keyIndexes.length);
  }
  
  public static List getAllFirstElementFromMergedVector(List<List> data, int levelCount)
  {
    if (data == null) {
      return null;
    }
    List result = new ArrayList();
    if (levelCount > 1) {
      for (List row : data)
      {
        List childData = (List)row.get(1);
        result.addAll(getAllFirstElementFromMergedVector(childData, levelCount - 1));
      }
    } else {
      for (List row : data)
      {
        List childData = (List)row.get(1);
        result.add(childData.get(0));
      }
    }
    return result;
  }
  
  public static boolean isEmpty(List list)
  {
    for (Object element : list) {
      if (element != null) {
        if ((((element instanceof List)) && (!((List)element).isEmpty())) || (!element.equals(""))) {
          return false;
        }
      }
    }
    return true;
  }
  
  private static class TwoDimensionVectorComparator
    implements Comparator, Serializable
  {
    private int[] keyIndexes;
    private boolean[] descendingColumns;
    
    public TwoDimensionVectorComparator(int[] keyIndexes, boolean[] descendingColumns)
    {
      this.keyIndexes = keyIndexes;
      this.descendingColumns = descendingColumns;
    }
    
    public int compare(Object source, Object target)
    {
      List sourceList = (List)source;
      List targetList = (List)target;
      for (int index = 0; index < this.keyIndexes.length; index++)
      {
        int columnIndex = this.keyIndexes[index];
        Comparable sourceKey;
        Comparable targetKey;
        if ((this.descendingColumns != null) && this.descendingColumns[index])
        {
          targetKey = sourceList.size() <= columnIndex ? null : (Comparable)sourceList.get(columnIndex);
          sourceKey = targetList.size() <= columnIndex ? null : (Comparable)targetList.get(columnIndex);
        }
        else
        {
          sourceKey = sourceList.size() <= columnIndex ? null : (Comparable)sourceList.get(columnIndex);
          targetKey = targetList.size() <= columnIndex ? null : (Comparable)targetList.get(columnIndex);
        }
        if ((sourceKey != null) || (targetKey != null))
        {
          if (sourceKey == null) {
            return -1;
          }
          if (targetKey == null) {
            return 1;
          }
          int result = sourceKey.compareTo(targetKey);
          if (result != 0) {
            return result;
          }
        }
      }
      return 0;
    }
  }
}
