package com.ftl.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import static java.lang.Float.NaN;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class KLV
{
  private static short KEY_MAP_ENTRY = 0;
  private static short KEY_MAP_ENTRY_KEY = 0;
  private static final short KEY_MAP_ENTRY_BYTE_ARRAY = 0;
  private static final short KEY_MAP_ENTRY_STRING = 1;
  private static final short KEY_MAP_ENTRY_BYTE = 2;
  private static final short KEY_MAP_ENTRY_CHAR = 3;
  private static final short KEY_MAP_ENTRY_DOUBLE = 4;
  private static final short KEY_MAP_ENTRY_FLOAT = 5;
  private static final short KEY_MAP_ENTRY_INTEGER = 6;
  private static final short KEY_MAP_ENTRY_LONG = 7;
  private static final short KEY_MAP_ENTRY_SHORT = 8;
  
  public static enum LengthEncoding
  {
    OneByte(1),  TwoBytes(2),  FourBytes(4),  BER(5);
    
    private int value;
    
    private LengthEncoding(int value)
    {
      this.value = value;
    }
    
    public int value()
    {
      return this.value;
    }
    
    public static LengthEncoding valueOf(int value)
    {
      switch (value)
      {
      case 1: 
        return OneByte;
      case 2: 
        return TwoBytes;
      case 4: 
        return FourBytes;
      case 0: 
        return BER;
      }
      return null;
    }
  }
  
  public static enum KeyLength
  {
    OneByte(1),  TwoBytes(2),  FourBytes(4),  SixteenBytes(16);
    
    private int value;
    
    private KeyLength(int value)
    {
      this.value = value;
    }
    
    public int value()
    {
      return this.value;
    }
    
    public static KeyLength valueOf(int value)
    {
      switch (value)
      {
      case 1: 
        return OneByte;
      case 2: 
        return TwoBytes;
      case 4: 
        return FourBytes;
      case 16: 
        return SixteenBytes;
      }
      return null;
    }
  }
  
  public static final KeyLength DEFAULT_KEY_LENGTH = KeyLength.TwoBytes;
  public static final LengthEncoding DEFAULT_LENGTH_ENCODING = LengthEncoding.BER;
  public static final String DEFAULT_CHARSET_NAME = "UTF-8";
  private KeyLength keyLength;
  private byte[] keyIfLong;
  private int keyIfShort;
  private LengthEncoding lengthEncoding;
  private byte[] value;
  private int offsetAfterInstantiation;
  
  public KLV()
  {
    this.keyLength = DEFAULT_KEY_LENGTH;
    this.lengthEncoding = DEFAULT_LENGTH_ENCODING;
    this.value = new byte[0];
  }
  
  public KLV(short key, byte value)
  {
    this.keyLength = DEFAULT_KEY_LENGTH;
    this.lengthEncoding = DEFAULT_LENGTH_ENCODING;
    this.value = new byte[0];
    setKey(key);
    setValue(value);
  }
  
  public KLV(short key, short value)
  {
    this.keyLength = DEFAULT_KEY_LENGTH;
    this.lengthEncoding = DEFAULT_LENGTH_ENCODING;
    this.value = new byte[0];
    setKey(key);
    setValue(value);
  }
  
  public KLV(short key, int value)
  {
    this.keyLength = DEFAULT_KEY_LENGTH;
    this.lengthEncoding = DEFAULT_LENGTH_ENCODING;
    this.value = new byte[0];
    setKey(key);
    setValue(value);
  }
  
  public KLV(short key, long value)
  {
    this.keyLength = DEFAULT_KEY_LENGTH;
    this.lengthEncoding = DEFAULT_LENGTH_ENCODING;
    this.value = new byte[0];
    setKey(key);
    setValue(value);
  }
  
  public KLV(short key, String value)
  {
    this.keyLength = DEFAULT_KEY_LENGTH;
    this.lengthEncoding = DEFAULT_LENGTH_ENCODING;
    this.value = new byte[0];
    setKey(key);
    setValue(value);
  }
  
  public KLV(short key, byte[] value)
  {
    this.keyLength = DEFAULT_KEY_LENGTH;
    this.lengthEncoding = DEFAULT_LENGTH_ENCODING;
    this.value = new byte[0];
    setKey(key);
    setValue(value);
  }
  
  public KLV(byte[] theBytes, KeyLength keyLength, LengthEncoding lengthEncoding)
  {
    this(theBytes, 0, keyLength, lengthEncoding);
  }
  
  public KLV(byte[] theBytes, int offset, KeyLength keyLength, LengthEncoding lengthEncoding)
  {
    if (theBytes == null) {
      throw new NullPointerException("KLV byte array must not be null.");
    }
    if (keyLength == null) {
      throw new NullPointerException("Key length must not be null.");
    }
    if (lengthEncoding == null) {
      throw new NullPointerException("Length encoding must not be null.");
    }
    if ((offset < 0) || (offset >= theBytes.length)) {
      throw new ArrayIndexOutOfBoundsException(String.format("Offset %d is out of range (byte array length: %d).", new Object[] {
      
        Integer.valueOf(offset), Integer.valueOf(theBytes.length) }));
    }
    setKey(theBytes, offset, keyLength);
    
    int valueOffset = setLength(theBytes, offset + keyLength.value(), lengthEncoding);
    int remaining = theBytes.length - valueOffset;
    if (remaining < this.value.length) {
      throw new ArrayIndexOutOfBoundsException(String.format("Not enough bytes left in array (%d) for the declared length (%d).", new Object[] {
      
        Integer.valueOf(remaining), Integer.valueOf(this.value.length) }));
    }
    System.arraycopy(theBytes, valueOffset, this.value, 0, this.value.length);
    
    this.offsetAfterInstantiation = (valueOffset + this.value.length);
  }
  
  public KLV(int shortKey, KeyLength keyLength, LengthEncoding lengthFieldEncoding, byte[] value)
  {
    this(shortKey, keyLength, lengthFieldEncoding, value, 0, value.length);
  }
  
  public KLV(int shortKey, KeyLength keyLength, LengthEncoding lengthEncoding, byte[] value, int offset, int length)
  {
    if (keyLength == null) {
      throw new NullPointerException("Key length must not be null.");
    }
    if (lengthEncoding == null) {
      throw new NullPointerException("Length encoding must not be null.");
    }
    if (value != null)
    {
      if (offset < 0) {
        throw new ArrayIndexOutOfBoundsException("Offset must not be negative: " + offset);
      }
      if ((value.length > 0) && (offset >= value.length)) {
        throw new ArrayIndexOutOfBoundsException(String.format("Offset %d is out of range (byte array length: %d).", new Object[] {
        
          Integer.valueOf(offset), Integer.valueOf(value.length) }));
      }
      if (length - offset < value.length) {
        throw new ArrayIndexOutOfBoundsException(String.format("Not enough bytes in array (%d) for declared length (%d).", new Object[] {
        
          Integer.valueOf(value.length), Integer.valueOf(length) }));
      }
    }
    this.keyLength = keyLength;
    this.keyIfShort = shortKey;
    
    this.lengthEncoding = lengthEncoding;
    if (value == null) {
      this.value = new byte[0];
    } else {
      switch (lengthEncoding)
      {
      case OneByte: 
        if (length > 255) {
          throw new IllegalArgumentException(String.format("%s encoding cannot support a %d-byte value.", new Object[] { lengthEncoding, 
          
            Integer.valueOf(length) }));
        }
        this.value = new byte[length];
        System.arraycopy(value, offset, this.value, 0, length);
        break;
      case TwoBytes: 
        if (length > 65535) {
          throw new IllegalArgumentException(String.format("%s encoding cannot support a %d-byte value.", new Object[] { lengthEncoding, 
          
            Integer.valueOf(length) }));
        }
        this.value = new byte[length];
        System.arraycopy(value, offset, this.value, 0, length);
        break;
      case FourBytes: 
      case BER: 
        this.value = new byte[length];
        System.arraycopy(value, offset, this.value, 0, length);
        break;
      default: 
        
        break;
      }
    }
  }
  
  public KLV(byte[] key, LengthEncoding lengthEncoding, byte[] value, int offset, int length)
  {
    if (key == null) {
      throw new NullPointerException("Key must not be null.");
    }
    if (lengthEncoding == null) {
      throw new NullPointerException("Length encoding must not be null.");
    }
    if ((key.length != 1) && (key.length != 2) && (key.length != 4) && (key.length != 16)) {
      throw new IllegalArgumentException("Key length must be 1, 2, 4, or 16 bytes, not " + key.length);
    }
    if (value != null)
    {
      if ((offset < 0) || (offset >= value.length)) {
        throw new ArrayIndexOutOfBoundsException(String.format("Offset %d is out of range (byte array length: %d).", new Object[] {
        
          Integer.valueOf(offset), Integer.valueOf(value.length) }));
      }
      if (offset + length >= value.length) {
        throw new ArrayIndexOutOfBoundsException(String.format("Not enough bytes in array for declared length of %d.", new Object[] {
        
          Integer.valueOf(length) }));
      }
    }
    setKey(key, 0, KeyLength.valueOf(key.length));
    
    this.lengthEncoding = lengthEncoding;
    if (value == null) {
      this.value = new byte[0];
    } else {
      switch (lengthEncoding)
      {
      case OneByte: 
        if (length > 255) {
          throw new IllegalArgumentException(String.format("%s encoding cannot support a %d-byte value.", new Object[] { lengthEncoding, 
          
            Integer.valueOf(length) }));
        }
        this.value = new byte[length];
        System.arraycopy(value, offset, this.value, 0, length);
        break;
      case TwoBytes: 
        if (length > 65535) {
          throw new IllegalArgumentException(String.format("%s encoding cannot support a %d-byte value.", new Object[] { lengthEncoding, 
          
            Integer.valueOf(length) }));
        }
        this.value = new byte[length];
        System.arraycopy(value, offset, this.value, 0, length);
        break;
      case FourBytes: 
      case BER: 
        this.value = new byte[length];
        System.arraycopy(value, offset, this.value, 0, length);
        break;
      default: 
       
        break;
      }
    }
  }
  
  public byte[] toBytes()
  {
    byte[] key = getFullKey();
    byte[] lengthField = makeLengthField(this.lengthEncoding, this.value.length);
    byte[] bytes = new byte[key.length + lengthField.length + this.value.length];
    
    System.arraycopy(key, 0, bytes, 0, key.length);
    System.arraycopy(lengthField, 0, bytes, key.length, lengthField.length);
    System.arraycopy(this.value, 0, bytes, key.length + lengthField.length, this.value.length);
    
    return bytes;
  }
  
  public List<KLV> getSubKLVList()
  {
    return getSubKLVList(this.keyLength, this.lengthEncoding);
  }
  
  public List<KLV> getSubKLVList(KeyLength keyLength, LengthEncoding lengthEncoding)
  {
    return bytesToList(this.value, 0, this.value.length, keyLength, lengthEncoding);
  }
  
  public Map<Short, KLV> getSubKLVMap()
  {
    return getSubKLVMap(this.keyLength, this.lengthEncoding);
  }
  
  public Map<Short, KLV> getSubKLVMap(KeyLength keyLength, LengthEncoding lengthEncoding)
  {
    return bytesToMap(this.value, 0, this.value.length, keyLength, lengthEncoding);
  }
  
  public KeyLength getKeyLength()
  {
    return this.keyLength;
  }
  
  public int getShortKey()
  {
    switch (this.keyLength)
    {
    case OneByte: 
      return this.keyIfShort & 0xFF;
    case TwoBytes: 
      return this.keyIfShort & 0xFFFF;
    case FourBytes: 
      return this.keyIfShort;
    case SixteenBytes: 
      assert (this.keyIfLong != null);
      assert (16 == this.keyIfLong.length) : this.keyIfLong.length;
      int key = 0;
      for (int i = 0; i < 4; i++) {
        key |= (this.keyIfLong[(13 + i)] & 0xFF) << (4 - i) * 8;
      }
      return key;
    }

    return 0;
  }
  
  public byte[] getFullKey()
  {
    int length = this.keyLength.value();
    byte[] key = new byte[length];
    switch (this.keyLength)
    {
    case OneByte: 
      key[0] = ((byte)this.keyIfShort);
      break;
    case TwoBytes: 
      key[0] = ((byte)(this.keyIfShort >> 8));
      key[1] = ((byte)this.keyIfShort);
      break;
    case FourBytes: 
      key[0] = ((byte)(this.keyIfShort >> 24));
      key[1] = ((byte)(this.keyIfShort >> 16));
      key[2] = ((byte)(this.keyIfShort >> 8));
      key[3] = ((byte)this.keyIfShort);
      break;
    case SixteenBytes: 
      assert (this.keyIfLong != null);
      assert (16 == this.keyIfLong.length) : this.keyIfLong.length;
      System.arraycopy(this.keyIfLong, 0, key, 0, 16);
      break;
    default: 

      break;
    }
    return key;
  }
  
  public LengthEncoding getLengthEncoding()
  {
    return this.lengthEncoding;
  }
  
  public int getLength()
  {
    return this.value.length;
  }
  
  public byte[] getValue()
  {
    return this.value;
  }
  
  public int getValueAs8bitSignedInt()
  {
    byte[] bytes = getValue();
    byte value = 0;
    if (bytes.length > 0) {
      value = bytes[0];
    }
    return value;
  }
  
  public int getValueAs8bitUnsignedInt()
  {
    byte[] bytes = getValue();
    int value = 0;
    if (bytes.length > 0) {
      value = bytes[0] & 0xFF;
    }
    return value;
  }
  
  public int getValueAs16bitSignedInt()
  {
    byte[] bytes = getValue();
    short value = 0;
    int length = bytes.length;
    int shortLen = length < 2 ? length : 2;
    for (int i = 0; i < shortLen; i++) {
      value = (short)(value | (bytes[i] & 0xFF) << shortLen * 8 - i * 8 - 8);
    }
    return value;
  }
  
  public int getValueAs16bitUnsignedInt()
  {
    byte[] bytes = getValue();
    int value = 0;
    int length = bytes.length;
    int shortLen = length < 2 ? length : 2;
    for (int i = 0; i < shortLen; i++) {
      value |= (bytes[i] & 0xFF) << shortLen * 8 - i * 8 - 8;
    }
    return value;
  }
  
  public int getValueAs32bitInt()
  {
    byte[] bytes = getValue();
    int value = 0;
    int length = bytes.length;
    int shortLen = length < 4 ? length : 4;
    for (int i = 0; i < shortLen; i++) {
      value |= (bytes[i] & 0xFF) << shortLen * 8 - i * 8 - 8;
    }
    return value;
  }
  
  public long getValueAs64bitLong()
  {
    byte[] bytes = getValue();
    long value = 0L;
    int length = bytes.length;
    int shortLen = length < 8 ? length : 8;
    for (int i = 0; i < shortLen; i++) {
      value |= (bytes[i] & 0xFF) << shortLen * 8 - i * 8 - 8;
    }
    return value;
  }
  
  public float getValueAsFloat()
  {
    return getValue().length < 4 ? NaN : Float.intBitsToFloat(getValueAs32bitInt());
  }
  
  public double getValueAsDouble()
  {
    return getValue().length < 8 ? NaN : Double.longBitsToDouble(getValueAs64bitLong());
  }
  
  public String getValueAsString()
  {
    try
    {
      return getValueAsString("UTF-8");
    }
    catch (UnsupportedEncodingException exc) {}
    return new String(getValue());
  }
  
  public String getValueAsString(String charsetName)
    throws UnsupportedEncodingException
  {
    return new String(getValue(), charsetName);
  }
  
  public KLV setKeyLength(KeyLength keyLength)
  {
    if (this.keyLength == keyLength) {
      return this;
    }
    if (keyLength == KeyLength.SixteenBytes)
    {
      this.keyIfShort = 0;
      this.keyIfLong = new byte[16];
    }
    else if (this.keyLength == KeyLength.SixteenBytes)
    {
      this.keyIfShort = 0;
      this.keyIfLong = null;
    }
    this.keyLength = keyLength;
    return this;
  }
  
  public KLV setKey(byte[] key)
  {
    if (key == null) {
      throw new NullPointerException("Key must not be null.");
    }
    switch (key.length)
    {
    case 1: 
    case 2: 
    case 4: 
    case 16: 
      return setKey(key, 0, KeyLength.valueOf(key.length));
    }
    throw new IllegalArgumentException("Invalid key size: " + key.length);
  }
  
  public KLV setKey(byte[] inTheseBytes, int offset, KeyLength keyLength)
  {
    if (inTheseBytes == null) {
      throw new NullPointerException("Byte array must not be null.");
    }
    if (keyLength == null) {
      throw new NullPointerException("Key length must not be null.");
    }
    if ((offset < 0) || (offset >= inTheseBytes.length)) {
      throw new ArrayIndexOutOfBoundsException(String.format("Offset %d is out of range (byte array length: %d).", new Object[] {
      
        Integer.valueOf(offset), Integer.valueOf(inTheseBytes.length) }));
    }
    if (inTheseBytes.length - offset < keyLength.value()) {
      throw new ArrayIndexOutOfBoundsException(String.format("Not enough bytes for %d-byte key.", new Object[] {
        Integer.valueOf(keyLength.value()) }));
    }
    this.keyLength = keyLength;
    switch (keyLength)
    {
    case OneByte: 
      this.keyIfShort = (inTheseBytes[offset] & 0xFF);
      this.keyIfLong = null;
      break;
    case TwoBytes: 
      this.keyIfShort = ((inTheseBytes[offset] & 0xFF) << 8);
      this.keyIfShort |= inTheseBytes[(offset + 1)] & 0xFF;
      this.keyIfLong = null;
      break;
    case FourBytes: 
      this.keyIfShort = ((inTheseBytes[offset] & 0xFF) << 24);
      this.keyIfShort |= (inTheseBytes[(offset + 1)] & 0xFF) << 16;
      this.keyIfShort |= (inTheseBytes[(offset + 2)] & 0xFF) << 8;
      this.keyIfShort |= inTheseBytes[(offset + 3)] & 0xFF;
      this.keyIfLong = null;
      break;
    case SixteenBytes: 
      this.keyIfLong = new byte[16];
      System.arraycopy(inTheseBytes, offset, this.keyIfLong, 0, 16);
      this.keyIfShort = 0;
      break;
    default: 
      throw new IllegalArgumentException("Unknown key length: " + keyLength);
    }
    return this;
  }
  
  public KLV setKey(int shortKey)
  {
    return setKey(shortKey, this.keyLength);
  }
  
  public KLV setKey(int shortKey, KeyLength keyLength)
  {
    switch (keyLength)
    {
    case OneByte: 
    case TwoBytes: 
    case FourBytes: 
      this.keyIfShort = shortKey;
      this.keyIfLong = null;
      this.keyLength = keyLength;
      break;
    case SixteenBytes: 
      this.keyLength = keyLength;
      break;
    default: 
      
      break;
    }
    return this;
  }
  
  public KLV setLengthEncoding(LengthEncoding lengthEncoding)
  {
    switch (lengthEncoding)
    {
    case OneByte: 
      if (this.value.length > 511)
      {
        byte[] bytes = new byte['?'];
        System.arraycopy(this.value, 0, bytes, 0, bytes.length);
        this.value = bytes;
      }
      this.lengthEncoding = lengthEncoding;
      break;
    case TwoBytes: 
      if (this.value.length > 131071)
      {
        byte[] bytes = new byte[131071];
        System.arraycopy(this.value, 0, bytes, 0, bytes.length);
        this.value = bytes;
      }
      this.lengthEncoding = lengthEncoding;
      break;
    case FourBytes: 
    case BER: 
      this.lengthEncoding = lengthEncoding;
      break;
    default: 

      break;
    }
    return this;
  }
  
  public int setLength(byte[] inTheseBytes, int offset, LengthEncoding lengthEncoding)
  {
    if (inTheseBytes == null) {
      throw new NullPointerException("Byte array must not be null.");
    }
    if (lengthEncoding == null) {
      throw new NullPointerException("Length encoding must not be null.");
    }
    if ((offset < 0) || (offset >= inTheseBytes.length)) {
      throw new ArrayIndexOutOfBoundsException(String.format("Offset %d is out of range (byte array length: %d).", new Object[] {
      
        Integer.valueOf(offset), Integer.valueOf(inTheseBytes.length) }));
    }
    int length = 0;
    int valueOffset = 0;
    switch (lengthEncoding)
    {
    case OneByte: 
      if (inTheseBytes.length - offset < 1) {
        throw new ArrayIndexOutOfBoundsException(String.format("Not enough bytes for %s length encoding.", new Object[] { lengthEncoding }));
      }
      length = inTheseBytes[offset] & 0xFF;
      setLength(length, lengthEncoding);
      valueOffset = offset + 1;
      break;
    case TwoBytes: 
      if (inTheseBytes.length - offset < 2) {
        throw new ArrayIndexOutOfBoundsException(String.format("Not enough bytes for %s length encoding.", new Object[] { lengthEncoding }));
      }
      length = (inTheseBytes[offset] & 0xFF) << 8;
      length |= inTheseBytes[(offset + 1)] & 0xFF;
      setLength(length, lengthEncoding);
      valueOffset = offset + 2;
      break;
    case FourBytes: 
      if (inTheseBytes.length - offset < 4) {
        throw new ArrayIndexOutOfBoundsException(String.format("Not enough bytes for %s length encoding.", new Object[] { lengthEncoding }));
      }
      length = (inTheseBytes[offset] & 0xFF) << 24;
      length |= (inTheseBytes[(offset + 1)] & 0xFF) << 16;
      length |= (inTheseBytes[(offset + 2)] & 0xFF) << 8;
      length |= inTheseBytes[(offset + 3)] & 0xFF;
      setLength(length, lengthEncoding);
      valueOffset = offset + 4;
      break;
    case BER: 
      if (inTheseBytes.length - offset < 1) {
        throw new ArrayIndexOutOfBoundsException(String.format("Not enough bytes for %s length encoding.", new Object[] { lengthEncoding }));
      }
      int ber = inTheseBytes[offset] & 0xFF;
      if ((ber & 0x80) == 0)
      {
        setLength(ber, lengthEncoding);
        valueOffset = offset + 1;
      }
      else
      {
        int following = ber & 0x7F;
        if (inTheseBytes.length - offset < following + 1) {
          throw new ArrayIndexOutOfBoundsException(String.format("Not enough bytes for %s length encoding.", new Object[] { lengthEncoding }));
        }
        for (int i = 0; i < following; i++) {
          length |= (inTheseBytes[(offset + 1 + i)] & 0xFF) << (following - 1 - i) * 8;
        }
        setLength(length, lengthEncoding);
        valueOffset = offset + 1 + following;
      }
      break;
    default: 

      break;
    }
    this.lengthEncoding = lengthEncoding;
    return valueOffset;
  }
  
  public KLV setLength(int length)
  {
    return setLength(length, this.lengthEncoding);
  }
  
  public KLV setLength(int length, LengthEncoding lengthEncoding)
  {
    if (length < 0) {
      throw new IllegalArgumentException("Length must not be negative: " + length);
    }
    switch (lengthEncoding)
    {
    case OneByte: 
      if (length > 255) {
        throw new IllegalArgumentException(String.format("%s encoding cannot support a %d-byte value.", new Object[] { lengthEncoding, 
        
          Integer.valueOf(length) }));
      }
      break;
    case TwoBytes: 
      if (length > 65535) {
        throw new IllegalArgumentException(String.format("%s encoding cannot support a %d-byte value.", new Object[] { lengthEncoding, 
        
          Integer.valueOf(length) }));
      }
      break;
    case FourBytes: 
    case BER: 
      break;
    default: 

      break;
    }
    byte[] bytes = new byte[length];
    if (this.value != null) {
      System.arraycopy(this.value, 0, bytes, 0, Math.min(length, this.value.length));
    }
    this.value = bytes;
    
    return this;
  }
  
  public KLV setValue(byte[] newValue)
  {
    return setValue(newValue, 0, newValue.length);
  }
  
  public KLV setValue(byte newValue)
  {
    return setValue(new byte[] { newValue });
  }
  
  public KLV setValue(short newValue)
  {
    return setValue(new byte[] { (byte)(newValue >> 8), (byte)newValue });
  }
  
  public KLV setValue(int newValue)
  {
    return setValue(new byte[] { (byte)(newValue >> 24), (byte)(newValue >> 16), (byte)(newValue >> 8), (byte)newValue });
  }
  
  public KLV setValue(long newValue)
  {
    return setValue(new byte[] { (byte)(int)(newValue >> 56), (byte)(int)(newValue >> 48), (byte)(int)(newValue >> 40), (byte)(int)(newValue >> 32), (byte)(int)(newValue >> 24), (byte)(int)(newValue >> 16), (byte)(int)(newValue >> 8), (byte)(int)newValue });
  }
  
  public KLV setValue(String newValue)
  {
    if (newValue == null) {
      return setValue(new byte[0]);
    }
    try
    {
      return setValue(newValue.getBytes("UTF-8"));
    }
    catch (UnsupportedEncodingException exc) {}
    return setValue(newValue.getBytes());
  }
  
  public KLV setValue(byte[] newValue, int offset, int length)
  {
    if (newValue == null) {
      throw new NullPointerException("Byte array must not be null.");
    }
    if (offset < 0) {
      throw new ArrayIndexOutOfBoundsException("Offset must not be negative: " + offset);
    }
    if ((this.value.length > 0) && (offset >= this.value.length)) {
      throw new ArrayIndexOutOfBoundsException(String.format("Offset %d is out of range (byte array length: %d).", new Object[] {
      
        Integer.valueOf(offset), Integer.valueOf(this.value.length) }));
    }
    if (newValue.length - offset < length) {
      throw new IllegalArgumentException(String.format("Number of bytes (%d) and offset (%d) not sufficient for declared length (%d).", new Object[] {
      
        Integer.valueOf(newValue.length), Integer.valueOf(offset), Integer.valueOf(length) }));
    }
    switch (this.lengthEncoding)
    {
    case OneByte: 
      if (length > 255) {
        throw new IllegalArgumentException(String.format("%s encoding cannot support a %d-byte value.", new Object[] { this.lengthEncoding, 
        
          Integer.valueOf(length) }));
      }
      break;
    case TwoBytes: 
      if (length > 65535) {
        throw new IllegalArgumentException(String.format("%s encoding cannot support a %d-byte value.", new Object[] { this.lengthEncoding, 
        
          Integer.valueOf(length) }));
      }
      break;
    case FourBytes: 
    case BER: 
      break;
    default: 

      break;
    }
    byte[] bytes = new byte[length];
    System.arraycopy(newValue, offset, bytes, 0, length);
    this.value = bytes;
    
    return this;
  }
  
  public KLV addSubKLV(int key, byte subValue)
  {
    return addSubKLV(key, new byte[] { subValue });
  }
  
  public KLV addSubKLV(int key, short subValue)
  {
    return addSubKLV(key, new byte[] { (byte)(subValue >> 8), (byte)subValue });
  }
  
  public KLV addSubKLV(int key, int subValue)
  {
    return addSubKLV(key, new byte[] { (byte)(subValue >> 24), (byte)(subValue >> 16), (byte)(subValue >> 8), (byte)subValue });
  }
  
  public KLV addSubKLV(int key, long subValue)
  {
    return addSubKLV(key, new byte[] { (byte)(int)(subValue >> 56), (byte)(int)(subValue >> 48), (byte)(int)(subValue >> 40), (byte)(int)(subValue >> 32), (byte)(int)(subValue >> 24), (byte)(int)(subValue >> 16), (byte)(int)(subValue >> 8), (byte)(int)subValue });
  }
  
  public KLV addSubKLV(int key, String subValue)
  {
    if (subValue == null) {
      return addSubKLV(key, new byte[0]);
    }
    try
    {
      return addSubKLV(key, subValue.getBytes("UTF-8"));
    }
    catch (UnsupportedEncodingException exc) {}
    return addSubKLV(key, subValue.getBytes());
  }
  
  public KLV addSubKLV(int key, byte[] subValue)
  {
    return addSubKLV(key, this.keyLength, this.lengthEncoding, subValue);
  }
  
  public KLV addSubKLV(int subKey, KeyLength subKeyLength, LengthEncoding subLengthEncoding, byte[] subValue)
  {
    return addSubKLV(new KLV(subKey, subKeyLength, subLengthEncoding, subValue, 0, subValue.length));
  }
  
  public KLV addSubKLV(KLV sub)
  {
    return addPayload(sub.toBytes());
  }
  
  public KLV addPayload(byte[] extraBytes)
  {
    addPayload(extraBytes, 0, extraBytes.length);
    return this;
  }
  
  public KLV addPayload(byte[] bytes, int offset, int length)
  {
    if (bytes == null) {
      throw new NullPointerException("Byte array must not be null.");
    }
    if ((offset < 0) || (offset >= bytes.length)) {
      throw new ArrayIndexOutOfBoundsException(String.format("Offset %d is out of range (byte array length: %d).", new Object[] {
      
        Integer.valueOf(offset), Integer.valueOf(bytes.length) }));
    }
    if (bytes.length - offset < length) {
      throw new IllegalArgumentException(String.format("Number of bytes (%d) and offset (%d) not sufficient for declared length (%d).", new Object[] {
      
        Integer.valueOf(bytes.length), Integer.valueOf(offset), Integer.valueOf(length) }));
    }
    int newLength = this.value.length + length;
    switch (this.lengthEncoding)
    {
    case OneByte: 
      if (newLength > 255) {
        throw new IllegalArgumentException(String.format("%s encoding cannot support a %d-byte value.", new Object[] { this.lengthEncoding, 
        
          Integer.valueOf(newLength) }));
      }
      break;
    case TwoBytes: 
      if (newLength > 65535) {
        throw new IllegalArgumentException(String.format("%s encoding cannot support a %d-byte value.", new Object[] { this.lengthEncoding, 
        
          Integer.valueOf(newLength) }));
      }
      break;
    case FourBytes: 
    case BER: 
      break;
    default: 

      break;
    }
    byte[] newValue = new byte[newLength];
    System.arraycopy(this.value, 0, newValue, 0, this.value.length);
    System.arraycopy(bytes, offset, newValue, this.value.length, length);
    this.value = newValue;
    
    return this;
  }
  
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append('[');
    
    sb.append("Key=");
    if (this.keyLength.value() <= 4)
    {
      sb.append(getShortKey());
    }
    else
    {
      sb.append('[');
      byte[] longKey = getFullKey();
      for (byte b : longKey) {
        sb.append(Long.toHexString(b & 0xFF)).append(' ');
      }
      sb.append(']');
    }
    sb.append(", Length=");
    sb.append(getLength());
    
    sb.append(", Value=[");
    byte[] value = getValue();
    for (byte b : value) {
      sb.append(Long.toHexString(b & 0xFF)).append(' ');
    }
    sb.append(']');
    
    sb.append(']');
    return sb.toString();
  }
  
  public static List<KLV> bytesToList(byte[] bytes, int offset, int length, KeyLength keyLength, LengthEncoding lengthEncoding)
  {
    if (lengthEncoding == null) {
      throw new NullPointerException("Length encoding must not be null.");
    }
    LinkedList<KLV> list = new LinkedList();
    int currentPos = offset;
    for (;;)
    {
      if (currentPos < offset + length) {
        try
        {
          KLV klv = new KLV(bytes, currentPos, keyLength, lengthEncoding);
          currentPos = klv.offsetAfterInstantiation;
          list.add(klv);
        }
        catch (Exception exc)
        {
          System.err.println("Stopped parsing with exception: " + exc.getMessage());
          exc.printStackTrace();
        }
      }
       return list;
    }
   
  }
  
  public static Map<Short, KLV> bytesToMap(byte[] bytes, int offset, int length, KeyLength keyLength, LengthEncoding lengthEncoding)
  {
    Map<Short, KLV> map = new HashMap();
    for (KLV klv : bytesToList(bytes, offset, length, keyLength, lengthEncoding)) {
      map.put(Short.valueOf((short)klv.getShortKey()), klv);
    }
    return map;
  }
  
  protected static byte[] makeLengthField(LengthEncoding lengthEncoding, int payloadLength)
  {
    byte[] bytes = null;
    switch (lengthEncoding)
    {
    case OneByte: 
      if (payloadLength > 255) {
        throw new IllegalArgumentException(String.format("Too much data (%d bytes) for one-byte length field encoding.", new Object[] {Integer.valueOf(payloadLength) }));
      }
      bytes = new byte[] { (byte)payloadLength };
      
      break;
    case TwoBytes: 
      if (payloadLength > 65535) {
        throw new IllegalArgumentException(String.format("Too much data (%d bytes) for two-byte length field encoding.", new Object[] {Integer.valueOf(payloadLength) }));
      }
      bytes = new byte[] { (byte)(payloadLength >> 8), (byte)payloadLength };
      
      break;
    case FourBytes: 
      bytes = new byte[] { (byte)(payloadLength >> 24), (byte)(payloadLength >> 16), (byte)(payloadLength >> 8), (byte)payloadLength };
      
      break;
    case BER: 
      if (payloadLength <= 127) {
        bytes = new byte[] { (byte)payloadLength };
      } else if (payloadLength <= 255) {
        bytes = new byte[] { -127, (byte)payloadLength };
      } else if (payloadLength <= 65535) {
        bytes = new byte[] { -126, (byte)(payloadLength >> 8), (byte)payloadLength };
      } else {
        bytes = new byte[] { -124, (byte)(payloadLength >> 24), (byte)(payloadLength >> 16), (byte)(payloadLength >> 8), (byte)payloadLength };
      }
      break;
    default: 
      throw new IllegalStateException("Unknown length field encoding flag: " + lengthEncoding);
    }
    return bytes;
  }
  

  public void store(OutputStream os)
    throws Exception
  {
    os.write(toBytes());
  }
  
  private static Object getMapEntryValue(KLV klv)
    throws Exception
  {
    int type = klv.getShortKey();
    if (type == 6) {
      return Integer.valueOf(klv.getValueAs32bitInt());
    }
    if (type == 7) {
      return Long.valueOf(klv.getValueAs64bitLong());
    }
    if (type == 4) {
      return Double.valueOf(klv.getValueAsDouble());
    }
    if (type == 5) {
      return Float.valueOf(klv.getValueAsFloat());
    }
    if (type == 8) {
      return Integer.valueOf(klv.getValueAs16bitSignedInt());
    }
    if (type == 2) {
      return Integer.valueOf(klv.getValueAs8bitSignedInt());
    }
    if (type == 0) {
      return klv.getValue();
    }
    if (type == 1) {
      return klv.getValueAsString();
    }
    return null;
  }
  
  public Map<String, Object> getValueAsMap()
    throws Exception
  {
    Map<String, Object> map = new HashMap();
    List<KLV> list = getSubKLVList();
    for (KLV l : list)
    {
      List<KLV> listEntry = l.getSubKLVList();
      map.put(((KLV)listEntry.get(0)).getValueAsString(), getMapEntryValue((KLV)listEntry.get(1)));
    }
    return map;
  }
}
