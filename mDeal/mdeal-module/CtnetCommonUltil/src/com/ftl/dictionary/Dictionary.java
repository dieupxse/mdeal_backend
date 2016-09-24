package com.ftl.dictionary;

import com.ftl.util.FLDUtil;
import com.ftl.util.FileUtil;
import com.ftl.util.Global;
import com.ftl.util.IntegerHolder;
import com.ftl.util.Log;
import com.ftl.util.StreamUtil;
import com.ftl.util.StringEscapeUtil;
import com.ftl.util.StringUtil;
import com.ftl.util.UrlUtil;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

public class Dictionary
  extends DictionaryNode
{
  private static final Logger logger = Logger.getLogger("");
  private static int CHECK_INTERVAL = 5000;
  private static final char NEXT_LEVEL_SYMBOL = '\t';
  private static final char SEPARATOR_SYMBOL = '=';
  private static final char REF_SYMBOL = '>';
  private static final char VAL_SYMBOL = '=';
  private static final char VECTOR_SYMBOL = '^';
  private static final char LONG_STRING_SYMBOL = ':';
  private static final String BEGIN_SYMBOL = ":{";
  private static final String END_SYMBOL = ":}";
  private static final String COMMENT_SYMBOL = "#";
  private static final String ROOT_REFERENCE_SYMBOL = "#reference";
  private static final String ROOT_REVERSE_REFERENCE_SYMBOL = "#reverseReference";
  private static final String ROOT_INCLUDE_SYMBOL = "#include";
  public Map commentMap = new HashMap();
  public Map lineNumberMap = new HashMap();
  private File file;
  private String relativePath;
  private long lastFileTime = 0L;
  private long nextCheckTime = 0L;
  private boolean mergeOnUpdate = true;
  private String language = null;
  private String iconPath = null;
  
  public Dictionary()
  {
    this.root = this;
  }
  
  public Dictionary(DictionaryNode nd)
  {
    this();
    copyDictionaryData(nd);
  }
  
  public Dictionary(Map map)
  {
    this();
    putAll(map);
  }
  
  public Dictionary(String fileName)
    throws IOException
  {
    this();
    this.relativePath = fileName;
    load(fileName);
  }
  
  public Dictionary(File file)
    throws IOException
  {
    this();
    this.relativePath = file.getPath();
    load(file);
  }
  
  public Dictionary(InputStream is)
    throws IOException
  {
    this();
    load(is);
  }
  
  public Dictionary(Reader reader)
    throws IOException
  {
    this();
    load(reader);
  }
  
  public void load(String fileName)
    throws IOException
  {
    fileName = fileName.replace('\\', '/');
    File localFile = new File(fileName);
    if (!localFile.exists()) {
      if (fileName.startsWith("/")) {
        localFile = new File(Global.RESOURCE_DIR + fileName.substring(1));
      } else {
        localFile = new File(Global.RESOURCE_DIR + fileName);
      }
    }
    if (localFile.exists())
    {
      load(localFile);
      logger.log(Level.INFO, "Loaded dictionary from file {0}", localFile.getAbsolutePath());
      return;
    }
    if (!fileName.startsWith("/")) {
      fileName = "/" + fileName;
    }
    InputStream is = null;
    try
    {
      is = Dictionary.class.getResourceAsStream(fileName);
      if (is == null) {
        throw new IOException("Resource " + fileName + " not found");
      }
      load(is);
      logger.log(Level.INFO, "Loaded dictionary from resource {0}", fileName);
    }
    finally
    {
      StreamUtil.safeClose(is);
    }
  }
  
  /* Error */
  public void load(File file)
    throws IOException
  {
    // Byte code:
    //   0: aload_0
    //   1: aload_1
    //   2: putfield 47	com/ftl/dictionary/Dictionary:file	Ljava/io/File;
    //   5: aload_0
    //   6: invokestatic 48	java/lang/System:currentTimeMillis	()J
    //   9: getstatic 49	com/ftl/dictionary/Dictionary:CHECK_INTERVAL	I
    //   12: i2l
    //   13: ladd
    //   14: putfield 7	com/ftl/dictionary/Dictionary:nextCheckTime	J
    //   17: aload_0
    //   18: aload_0
    //   19: getfield 47	com/ftl/dictionary/Dictionary:file	Ljava/io/File;
    //   22: invokevirtual 50	java/io/File:lastModified	()J
    //   25: putfield 6	com/ftl/dictionary/Dictionary:lastFileTime	J
    //   28: aconst_null
    //   29: astore_2
    //   30: new 51	java/io/FileInputStream
    //   33: dup
    //   34: aload_1
    //   35: invokespecial 52	java/io/FileInputStream:<init>	(Ljava/io/File;)V
    //   38: astore_2
    //   39: aload_0
    //   40: aload_2
    //   41: invokevirtual 20	com/ftl/dictionary/Dictionary:load	(Ljava/io/InputStream;)V
    //   44: aload_2
    //   45: invokestatic 46	com/ftl/util/StreamUtil:safeClose	(Ljava/io/InputStream;)V
    //   48: goto +10 -> 58
    //   51: astore_3
    //   52: aload_2
    //   53: invokestatic 46	com/ftl/util/StreamUtil:safeClose	(Ljava/io/InputStream;)V
    //   56: aload_3
    //   57: athrow
    //   58: return
    // Line number table:
    //   Java source line #172	-> byte code offset #0
    //   Java source line #173	-> byte code offset #5
    //   Java source line #174	-> byte code offset #17
    //   Java source line #175	-> byte code offset #28
    //   Java source line #178	-> byte code offset #30
    //   Java source line #179	-> byte code offset #39
    //   Java source line #183	-> byte code offset #44
    //   Java source line #184	-> byte code offset #48
    //   Java source line #183	-> byte code offset #51
    //   Java source line #185	-> byte code offset #58
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	59	0	this	Dictionary
    //   0	59	1	file	File
    //   29	24	2	is	java.io.FileInputStream
    //   51	6	3	localObject	Object
    // Exception table:
    //   from	to	target	type
    //   30	44	51	finally
  }
  
  public void load(InputStream is)
    throws IOException
  {
    load(new InputStreamReader(is, Global.ENCODING));
  }
  
  private static final List<Character> fileMarks = new ArrayList();
  
  public void load(Reader reader)
    throws IOException
  {
    this.root = this;
    this.name = null;
    this.value = null;
    this.children = null;
    
    LinkedList<BufferedReader> readerStack = new LinkedList();
    BufferedReader bufferedReader;
    if ((reader instanceof BufferedReader))
    {
      bufferedReader = (BufferedReader)reader;
      readerStack.addLast(bufferedReader);
    }
    else
    {
      bufferedReader = new BufferedReader(reader);
      readerStack.addLast(bufferedReader);
    }
    bufferedReader.mark(1);
    int firstChar = bufferedReader.read();
    if (fileMarks.indexOf(Character.valueOf((char)firstChar)) < 0) {
      bufferedReader.reset();
    }
    LinkedList<DictionaryNode> referenceNodes = new LinkedList();
    DictionaryNode nd = load(readerStack, new IntegerHolder(0), this, new IntegerHolder(0), referenceNodes);
    for (DictionaryNode node : referenceNodes) {
      node.fillReference();
    }
    if (nd != null) {
      addChild(nd);
    }
  }
  
  private DictionaryNode load(LinkedList<BufferedReader> readerStack, IntegerHolder lineIndex, DictionaryNode parentNode, IntegerHolder level, LinkedList<DictionaryNode> referenceNodes)
    throws IOException
  {
    StringBuilder commentBuilder = null;
    String line;
    while ((line = getLine(readerStack, lineIndex)) != null)
    {
      String trimedLine = line.trim();
      if (trimedLine.isEmpty())
      {
        if (commentBuilder == null) {
          commentBuilder = new StringBuilder();
        } else {
          commentBuilder.append("\n");
        }
        commentBuilder.append(line);
      }
      else if ((trimedLine.startsWith("#reference")) || (trimedLine.startsWith("#reverseReference")))
      {
        String localValue;
        if (trimedLine.startsWith("#reverseReference")) {
          localValue = trimedLine.substring("#reverseReference".length()).trim();
        } else {
          localValue = trimedLine.substring("#reference".length()).trim();
        }
        if (localValue.indexOf('!') < 0) {
          localValue = localValue + '!';
        }
        if ((this.value == null) || (this.type != 1)) {
          this.value = "";
        }
        this.type = 1;
        if ((this.value != null) && (!this.value.isEmpty())) {
          this.value += ",";
        }
        if (trimedLine.startsWith("#reverseReference")) {
          this.value += "!";
        }
        this.value += localValue;
        referenceNodes.addFirst(this);
      }
      else if (trimedLine.startsWith("#include"))
      {
        String fileName = trimedLine.substring("#include".length()).trim();
        if (!UrlUtil.ROOT_PATTERN.matcher(fileName).matches())
        {
          String path = getRoot().getRelativeFilePath();
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
        fileName = translateKey(fileName);
        InputStream is = null;
        try
        {
          is = UrlUtil.getResource(fileName).openStream();
          if (is == null) {
            throw new IOException("Resource " + fileName + " not found");
          }
          readerStack.add(new BufferedReader(new InputStreamReader(is, Global.ENCODING)));
        }
        finally
        {
          StreamUtil.safeClose(is);
        }
      }
      else if (trimedLine.startsWith("#"))
      {
        if (commentBuilder == null) {
          commentBuilder = new StringBuilder();
        } else {
          commentBuilder.append("\n");
        }
        commentBuilder.append(line);
      }
      else
      {
        DictionaryNode node = createChild();
        if (commentBuilder != null)
        {
          setComment(node, commentBuilder.toString());
          commentBuilder = null;
        }
        setLineNumber(node, lineIndex.value);
        
        int lineLevel = getLineLevel(line);
        int separatorIndex = trimedLine.indexOf('=');
        if (separatorIndex < 0)
        {
          node.name = trimedLine;
        }
        else
        {
          node.name = trimedLine.substring(0, separatorIndex).trim();
          if (trimedLine.length() > separatorIndex + 1)
          {
            char localType = trimedLine.charAt(separatorIndex + 1);
            if ((localType == '>') || (localType == '^') || (localType == '='))
            {
              node.value = trimedLine.substring(separatorIndex + 2).trim();
              node.value = StringEscapeUtil.unescapeJava(node.value);
              if (localType == '>')
              {
                node.type = 1;
                if (node.value.indexOf('!') >= 0) {
                  referenceNodes.addFirst(node);
                }
              }
              else if (localType == '^')
              {
                node.type = 2;
              }
              else
              {
                node.type = 0;
              }
            }
            else if (localType == ':')
            {
              node.type = 3;
              line = getLine(readerStack, lineIndex);
              if ((line == null) || (!line.trim().equals(":{"))) {
                throw new IOException("Begin symbol (':{') expected, line " + lineIndex.value + ": " + line);
              }
              StringBuilder content = new StringBuilder();
              while ((line = getLine(readerStack, lineIndex)) != null)
              {
                trimedLine = line.trim();
                if (trimedLine.equals(":}")) {
                  break;
                }
                int dataIndex = 0;
                int count = Math.min(lineLevel, line.length() - 1);
                while (dataIndex <= count)
                {
                  if (line.charAt(dataIndex) != '\t') {
                    break;
                  }
                  dataIndex++;
                }
                content.append(line.substring(dataIndex));
                content.append("\n");
              }
              if (line == null) {
                throw new IOException("End symbol (':}') expected, line " + lineIndex.value);
              }
              if (content.length() > 0) {
                node.value = content.substring(0, content.length() - 1);
              } else {
                node.value = "";
              }
            }
            else
            {
              throw new IOException("Type of value not supported, line " + lineIndex.value + ": " + line);
            }
          }
        }
        if (lineLevel == level.value)
        {
          parentNode.addChild(node);
        }
        else if (lineLevel == level.value + 1)
        {
          DictionaryNode lastNode = (DictionaryNode)parentNode.children.get(parentNode.children.size() - 1);
          lastNode.addChild(node);
          IntegerHolder nextLevel = new IntegerHolder(lineLevel);
          node = load(readerStack, lineIndex, lastNode, nextLevel, referenceNodes);
          if (node != null) {
            if (level.value == nextLevel.value)
            {
              parentNode.addChild(node);
            }
            else
            {
              level.value = nextLevel.value;
              return node;
            }
          }
        }
        else
        {
          if (lineLevel > level.value + 1) {
            throw new IOException("Invalid node level, line " + lineIndex.value + ": " + line);
          }
          level.value = lineLevel;
          return node;
        }
      }
    }
    return null;
  }
  
  private int getLineLevel(String line)
  {
    int length = line.length();
    for (int index = 0; index < length; index++) {
      if (line.charAt(index) != '\t') {
        return index;
      }
    }
    return -1;
  }
  
  private String getLine(LinkedList<BufferedReader> readerStack, IntegerHolder lineIndex)
    throws IOException
  {
    if (readerStack.isEmpty()) {
      return null;
    }
    String result = ((BufferedReader)readerStack.getLast()).readLine();
    if (result != null)
    {
      lineIndex.value += 1;
      return result;
    }
    readerStack.removeLast();
    return getLine(readerStack, lineIndex);
  }
  
  private void store(Writer writer, DictionaryNode nd, String tabLevel)
    throws IOException
  {
    StringBuilder nodeContent = new StringBuilder();
    nodeContent.append(tabLevel);
    nodeContent.append(nd.name);
    String nextTabLevel;
    if ((nd.type == 2) || ((nd.value != null) && 
      (nd.value.length() > 0)))
    {
      nodeContent.append('=');
      String localValue = nd.value;
      if (nd.type != 3)
      {
        if (nd.type == 0) {
          if ((nd.value.length() <= 128) || 
            (nd.value.indexOf('\n') < 0)) {}
        }
      }
      else
      {
        nodeContent.append(':');
        nodeContent.append("\n");
        nodeContent.append(tabLevel);
        nodeContent.append(":{");
        nodeContent.append("\n");
        nextTabLevel = tabLevel + "\t";
        int index = 0;
        int lastIndex = index;
        while ((index = localValue.indexOf('\n', lastIndex)) >= 0)
        {
          nodeContent.append(nextTabLevel);
          nodeContent.append(localValue.substring(lastIndex, index + 1));
          lastIndex = index + 1;
        }
        nodeContent.append(nextTabLevel);
        nodeContent.append(localValue.substring(lastIndex, localValue.length()));
        nodeContent.append("\n");
        nodeContent.append(tabLevel);
        nodeContent.append(":}");
        
      }
      if (nd.type == 0)
      {
        nodeContent.append('=');
        localValue = StringEscapeUtil.escapeJavaIgnoreWideCharacter(localValue);
        nodeContent.append(localValue);
      }
      else if (nd.type == 2)
      {
        nodeContent.append('^');
        localValue = StringEscapeUtil.escapeJavaIgnoreWideCharacter(localValue);
        nodeContent.append(localValue);
      }
      else
      {
        nodeContent.append('>');
        localValue = StringEscapeUtil.escapeJavaIgnoreWideCharacter(localValue);
        nodeContent.append(localValue);
      }
    }
    
    String comment = getComment(nd);
    if (comment != null)
    {
      writer.write(comment);
      writer.write("\n");
    }
    writer.write(nodeContent.toString());
    writer.write("\n");
    if (nd.children != null) {
      for (DictionaryNode child : nd.children) {
        store(writer, child, tabLevel + '\t');
      }
    }
  }
  
  public void store()
    throws IOException
  {
    if (this.file == null) {
      throw new IOException("Only Dictionary created with constructor Dictionary(\"String\") can do store() method");
    }
    store(this.file);
  }
  
  public void store(String fileName)
    throws IOException
  {
    store(new File(fileName));
  }
  
  public void store(File file)
    throws IOException
  {
    synchronized (this)
    {
      File tempFile = null;
      for (;;)
      {
        tempFile = new File(file.getPath() + "." + System.currentTimeMillis());
        if (!tempFile.exists()) {
          break;
        }
        try
        {
          wait(10L);
        }
        catch (Exception ex)
        {
          ex.printStackTrace();
        }
      }
      FileOutputStream os = null;
      try
      {
        os = new FileOutputStream(tempFile);
        store(os);
      }
      finally
      {
        StreamUtil.safeClose(os);
      }
      FileUtil.renameFile(tempFile.getPath(), file.getPath(), true);
      ignoreChange();
    }
  }
  
  /* Error */
  public void store(OutputStream os)
    throws IOException
  {
    // Byte code:
    //   0: aconst_null
    //   1: astore_2
    //   2: new 158	java/io/OutputStreamWriter
    //   5: dup
    //   6: aload_1
    //   7: getstatic 54	com/ftl/util/Global:ENCODING	Ljava/lang/String;
    //   10: invokespecial 159	java/io/OutputStreamWriter:<init>	(Ljava/io/OutputStream;Ljava/lang/String;)V
    //   13: astore_2
    //   14: aload_0
    //   15: aload_2
    //   16: invokevirtual 160	com/ftl/dictionary/Dictionary:store	(Ljava/io/Writer;)V
    //   19: aload_2
    //   20: ifnull +21 -> 41
    //   23: aload_2
    //   24: invokevirtual 161	java/io/OutputStreamWriter:close	()V
    //   27: goto +14 -> 41
    //   30: astore_3
    //   31: aload_2
    //   32: ifnull +7 -> 39
    //   35: aload_2
    //   36: invokevirtual 161	java/io/OutputStreamWriter:close	()V
    //   39: aload_3
    //   40: athrow
    //   41: return
    // Line number table:
    //   Java source line #614	-> byte code offset #0
    //   Java source line #617	-> byte code offset #2
    //   Java source line #618	-> byte code offset #14
    //   Java source line #622	-> byte code offset #19
    //   Java source line #623	-> byte code offset #23
    //   Java source line #622	-> byte code offset #30
    //   Java source line #623	-> byte code offset #35
    //   Java source line #625	-> byte code offset #41
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	42	0	this	Dictionary
    //   0	42	1	os	OutputStream
    //   1	35	2	writer	java.io.OutputStreamWriter
    //   30	10	3	localObject	Object
    // Exception table:
    //   from	to	target	type
    //   2	19	30	finally
  }
  
  public void store(Writer writer)
    throws IOException
  {
    String comment = getComment();
    if (comment != null) {
      writer.write(comment);
    }
    if (this.children != null) {
      for (DictionaryNode child : this.children) {
        store(writer, child, "");
      }
    }
  }
  
  public void loadFromFLD(DictionaryNode parentNode, InputStream is)
    throws IOException
  {
    for (;;)
    {
      byte[] nameData = FLDUtil.readFLDData(is);
      if (nameData == null) {
        return;
      }
      byte[] valueData = FLDUtil.readFLDData(is);
      if (valueData == null) {
        return;
      }
      DictionaryNode node = createChild();
      node.name = new String(nameData, Global.ENCODING);
      if (valueData.length > 0) {
        node.value = new String(valueData, Global.ENCODING);
      }
      parentNode.addChild(node);
      
      byte[] data = FLDUtil.readFLDData(is);
      if (data == null) {
        return;
      }
      if (data.length > 0)
      {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        loadFromFLD(node, bis);
      }
    }
  }
  
  /* Error */
  public void loadFromFLD(File file)
    throws IOException
  {
    // Byte code:
    //   0: aconst_null
    //   1: astore_2
    //   2: new 51	java/io/FileInputStream
    //   5: dup
    //   6: aload_1
    //   7: invokespecial 52	java/io/FileInputStream:<init>	(Ljava/io/File;)V
    //   10: astore_2
    //   11: aload_0
    //   12: aload_0
    //   13: aload_2
    //   14: invokevirtual 168	com/ftl/dictionary/Dictionary:loadFromFLD	(Lcom/ftl/dictionary/DictionaryNode;Ljava/io/InputStream;)V
    //   17: aload_2
    //   18: invokestatic 46	com/ftl/util/StreamUtil:safeClose	(Ljava/io/InputStream;)V
    //   21: goto +10 -> 31
    //   24: astore_3
    //   25: aload_2
    //   26: invokestatic 46	com/ftl/util/StreamUtil:safeClose	(Ljava/io/InputStream;)V
    //   29: aload_3
    //   30: athrow
    //   31: return
    // Line number table:
    //   Java source line #684	-> byte code offset #0
    //   Java source line #687	-> byte code offset #2
    //   Java source line #688	-> byte code offset #11
    //   Java source line #692	-> byte code offset #17
    //   Java source line #693	-> byte code offset #21
    //   Java source line #692	-> byte code offset #24
    //   Java source line #694	-> byte code offset #31
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	32	0	this	Dictionary
    //   0	32	1	file	File
    //   1	25	2	is	java.io.FileInputStream
    //   24	6	3	localObject	Object
    // Exception table:
    //   from	to	target	type
    //   2	17	24	finally
  }
  
  public void loadFromFLD(String fileName)
    throws IOException
  {
    loadFromFLD(new File(fileName));
  }
  
  private void storeFLD(OutputStream os, DictionaryNode nd)
    throws IOException
  {
    FLDUtil.writeFLDString(os, nd.name);
    FLDUtil.writeFLDString(os, nd.value);
    if (nd.children != null)
    {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      for (DictionaryNode child : this.children) {
        storeFLD(bos, child);
      }
      FLDUtil.writeFLDData(os, bos.toByteArray());
    }
    else
    {
      os.write(0);
    }
  }
  
  public void storeFLD(OutputStream os)
    throws IOException
  {
    if (this.children != null) {
      for (DictionaryNode child : this.children) {
        storeFLD(os, child);
      }
    }
  }
  
  public void storeFLD(String fileName)
    throws IOException
  {
    storeFLD(new File(fileName));
  }
  
  public void storeFLD(File file)
    throws IOException
  {
    File tempFile = null;
    long time = System.currentTimeMillis();
    for (;;)
    {
      tempFile = new File(file.getPath() + "." + time);
      if (!tempFile.exists()) {
        break;
      }
      time += 1L;
    }
    FileOutputStream os = null;
    try
    {
      os = new FileOutputStream(tempFile);
      storeFLD(os);
    }
    finally
    {
      StreamUtil.safeClose(os);
    }
    FileUtil.renameFile(tempFile.getPath(), file.getPath(), true);
  }
  
  public void ignoreChange()
  {
    if (this.file != null) {
      this.lastFileTime = this.file.lastModified();
    }
  }
  
  public long getLastFileTime()
  {
    return this.lastFileTime;
  }
  
  public boolean isChanged()
  {
    if (this.nextCheckTime < System.currentTimeMillis())
    {
      this.nextCheckTime = (System.currentTimeMillis() + CHECK_INTERVAL);
      if (this.file != null)
      {
        long currentFileTime = getCurrentFileTime();
        if (currentFileTime != this.lastFileTime) {
          return true;
        }
      }
    }
    return false;
  }
  
  public long getNextChangedCheckTime()
  {
    return this.nextCheckTime;
  }
  
  public long getCurrentFileTime()
  {
    if (this.file != null) {
      return this.file.lastModified();
    }
    return 0L;
  }
  
  public void update()
  {
    if (isChanged()) {
      forceUpdate();
    }
  }
  
  public void forceUpdate()
  {
    try
    {
      this.lastFileTime = this.file.lastModified();
      if (this.mergeOnUpdate)
      {
        Dictionary nd = new Dictionary(this.file);
        merge(nd, true, true);
      }
      else
      {
        load(this.file);
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  
  public void setMergeOnUpdate(boolean mergeOnUpdate)
  {
    this.mergeOnUpdate = mergeOnUpdate;
  }
  
  public boolean isMergeOnUpdate()
  {
    return this.mergeOnUpdate;
  }
  
  public void setLanguage(String language)
  {
    this.language = language;
  }
  
  public String getLanguage()
  {
    return this.language;
  }
  
  public void setIconPath(String iconPath)
  {
    this.iconPath = iconPath;
  }
  
  public String getIconPath()
  {
    return this.iconPath;
  }
  
  public void loadFromXML(InputStream is)
    throws Exception
  {
    SAXParserFactory spf = SAXParserFactory.newInstance();
    spf.setValidating(false);
    SAXParser parser = spf.newSAXParser();
    loadFromXML(is, parser);
  }
  
  public void loadFromXML(InputStream is, SAXParser parser)
    throws Exception
  {
    parser.parse(is, createDefaultContentHandler());
  }
  
  public void loadFromXML(InputSource is, SAXParser parser)
    throws Exception
  {
    parser.parse(is, createDefaultContentHandler());
  }
  
  public DefaultHandler createDefaultContentHandler()
  {
    return new DefaultHandler()
    {
      private Stack nameStack = new Stack();
      private Stack valueStack = new Stack();
      public Stack dataStack = new Stack();
      
      public void startElement(String uri, String name, String qName, Attributes atts)
        throws SAXException
      {
        this.valueStack.push(new StringBuffer());
        this.nameStack.push(qName);
        DictionaryNode nd;
        if (this.dataStack.size() == 0) {
          nd = Dictionary.this;
        } else {
          nd = Dictionary.this.createChild();
        }
        nd.name = qName;
        if ((atts != null) && (atts.getLength() > 0)) {
          nd.attributes = new AttributesImpl(atts);
        }
        this.dataStack.push(nd);
      }
      
      public void endElement(String uri, String name, String qName)
        throws SAXException
      {
        if ((this.nameStack.size() == 0) || (!qName.equals(this.nameStack.peek()))) {
          throw new SAXException("Found end mark of " + qName + " without start mark");
        }
        this.nameStack.pop();
        StringBuffer stringValue = (StringBuffer)this.valueStack.pop();
        DictionaryNode nd = (DictionaryNode)this.dataStack.pop();
        nd.value = stringValue.toString().trim();
        if (this.dataStack.size() > 0)
        {
          DictionaryNode parentNode = (DictionaryNode)this.dataStack.peek();
          parentNode.addChild(nd);
        }
      }
      
      public void characters(char[] ch, int start, int length)
      {
        if (this.valueStack.size() > 0)
        {
          StringBuffer stringValue = (StringBuffer)this.valueStack.peek();
          stringValue.append(new String(ch, start, length));
        }
      }
    };
  }
  
  protected DictionaryNode createChild()
  {
    return new DictionaryNode(this);
  }
  
  public String getComment(DictionaryNode nd)
  {
    return (String)this.commentMap.get(nd);
  }
  
  public void setComment(DictionaryNode nd, String comment)
  {
    this.commentMap.put(nd, comment);
  }
  
  public int getLineNumber(DictionaryNode nd)
  {
    Integer i = (Integer)this.lineNumberMap.get(nd);
    if (i == null) {
      return -1;
    }
    return i.intValue();
  }
  
  public void setLineNumber(DictionaryNode nd, int lineNumber)
  {
    this.lineNumberMap.put(nd, Integer.valueOf(lineNumber));
  }
  
  public String toString()
  {
    try
    {
      StringWriter writer = new StringWriter();
      store(writer);
      writer.close();
      return writer.toString();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      return "Error occurred: " + ex.getMessage();
    }
  }
  
  public File getSourceFile()
  {
    return this.file;
  }
  
  public String getSourceFilePath()
  {
    if (this.file == null) {
      return null;
    }
    return this.file.getPath();
  }
  
  public String getRelativeFilePath()
  {
    return this.relativePath;
  }
  
  public static Dictionary getCachedDictionary(String key, final Map cache, boolean createIfNotExist)
  {
    key = new File(key).getPath().replace('\\', '/');
    Dictionary dic = (Dictionary)cache.get(key);
    if (dic == null) {
      try
      {
        dic = new Dictionary(translateKey(key))
        {
          public Map getCache()
          {
            return cache;
          }
        };
        dic.setMergeOnUpdate(false);
        cache.put(key, dic);
      }
      catch (Exception e)
      {
        logger.warning(e.getMessage());
      }
    }
    if ((dic == null) && (createIfNotExist))
    {
      dic = new Dictionary();
      cache.put(key, dic);
    }
    return dic;
  }
  
  private static String translateKey(String key)
  {
    if (config != null)
    {
      String value = config.getString("Translation." + key);
      if ((value != null) && (!value.isEmpty())) {
        return value;
      }
    }
    return key;
  }
  
  public static Dictionary getCachedDictionary(String key, Map cache)
  {
    return getCachedDictionary(key, cache, false);
  }
  
  private Map dictionaryCache = null;
  private static Dictionary config;
  
  public Map getCache()
  {
    if (this.dictionaryCache == null) {
      this.dictionaryCache = new HashMap();
    }
    return this.dictionaryCache;
  }
  
  static
  {
    
    
    String key = StringUtil.replaceAll(Dictionary.class.getName(), ".", "/") + ".dic";
    try
    {
      config = new Dictionary(key);
    }
    catch (Exception ex)
    {
      logger.log(Level.WARNING, "Loading configuration file {0} failed", key);
    }
  }
}
