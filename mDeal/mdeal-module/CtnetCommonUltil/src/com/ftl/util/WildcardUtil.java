package com.ftl.util;

public class WildcardUtil
{
  public static boolean match(String wildcard, String value)
  {
    return match(wildcard, value, true);
  }
  
  public static boolean match(String wildcard, String value, boolean ignoreCase)
  {
    if (ignoreCase)
    {
      value = value.toUpperCase();
      wildcard = wildcard.toUpperCase();
    }
    int wildcardIndex = 0;
    int valueIndex = 0;
    int wildcardLength = wildcard.length();
    int valueLength = value.length();
    while ((valueIndex < valueLength) && (wildcardIndex < wildcardLength)) {
      if ((wildcard.charAt(wildcardIndex) == '?') || (
        (value.charAt(valueIndex) != '*') && (wildcard.charAt(wildcardIndex) == value.charAt(valueIndex))))
      {
        wildcardIndex++;
        valueIndex++;
      }
      else
      {
        if (wildcard.charAt(wildcardIndex) == '*')
        {
          wildcardIndex++;
          if (wildcardIndex >= wildcardLength) {
            return true;
          }
          for (;;)
          {
            if ((valueIndex < valueLength) && (wildcard.charAt(wildcardIndex) != value.charAt(valueIndex)))
            {
              valueIndex++;
            }
            else
            {
              if (valueIndex >= valueLength) {
                return false;
              }
              String newWildcard = wildcard.substring(wildcardIndex, wildcardLength);
              String newValue = value.substring(valueIndex, valueLength);
              if (match(newWildcard, newValue, ignoreCase)) {
                return true;
              }
              valueIndex++;
            }
          }
        }
        return false;
      }
    }
    if ((valueIndex >= valueLength) && ((wildcardIndex >= wildcardLength) || (isOptional(wildcard.substring(wildcardIndex, wildcard.length()))))) {
      return true;
    }
    return false;
  }
  
  public static boolean isOptional(String pattern)
  {
    for (int index = 0; index < pattern.length(); index++) {
      if (pattern.charAt(index) != '*') {
        return false;
      }
    }
    return true;
  }
}
