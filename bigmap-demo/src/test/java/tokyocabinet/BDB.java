/*************************************************************************************************
 * Java binding of Tokyo Cabinet
 *                                                               Copyright (C) 2006-2010 FAL Labs
 * This file is part of Tokyo Cabinet.
 * Tokyo Cabinet is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation; either
 * version 2.1 of the License or any later version.  Tokyo Cabinet is distributed in the hope
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public
 * License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with Tokyo
 * Cabinet; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA.
 *************************************************************************************************/


package tokyocabinet;

import java.util.*;
import java.io.*;
import java.net.*;



/**
 * B+ tree database is a file containing a B+ tree and is handled with the B+ tree database API.
 * Before operations to store or retrieve records, it is necessary to open a database file and
 * connect the B+ tree database object to it.  To avoid data missing or corruption, it is
 * important to close every database file when it is no longer in use.  It is forbidden for
 * multible database objects in a process to open the same database at the same time.
 */
public class BDB implements DBM {
  //----------------------------------------------------------------
  // static initializer
  //----------------------------------------------------------------
  static {
    Loader.load();
    init();
  }
  //----------------------------------------------------------------
  // public constants
  //---------------------------------------------------------------
  /** error code: success */
  public static final int ESUCCESS = 0;
  /** error code: threading error */
  public static final int ETHREAD = 1;
  /** error code: invalid operation */
  public static final int EINVALID = 2;
  /** error code: file not found */
  public static final int ENOFILE = 3;
  /** error code: no permission */
  public static final int ENOPERM = 4;
  /** error code: invalid meta data */
  public static final int EMETA = 5;
  /** error code: invalid record header */
  public static final int ERHEAD = 6;
  /** error code: open error */
  public static final int EOPEN = 7;
  /** error code: close error */
  public static final int ECLOSE = 8;
  /** error code: trunc error */
  public static final int ETRUNC = 9;
  /** error code: sync error */
  public static final int ESYNC = 10;
  /** error code: stat error */
  public static final int ESTAT = 11;
  /** error code: seek error */
  public static final int ESEEK = 12;
  /** error code: read error */
  public static final int EREAD = 13;
  /** error code: write error */
  public static final int EWRITE = 14;
  /** error code: mmap error */
  public static final int EMMAP = 15;
  /** error code: lock error */
  public static final int ELOCK = 16;
  /** error code: unlink error */
  public static final int EUNLINK = 17;
  /** error code: rename error */
  public static final int ERENAME = 18;
  /** error code: mkdir error */
  public static final int EMKDIR = 19;
  /** error code: rmdir error */
  public static final int ERMDIR = 20;
  /** error code: existing record */
  public static final int EKEEP = 21;
  /** error code: no record found */
  public static final int ENOREC = 22;
  /** error code: miscellaneous error */
  public static final int EMISC = 9999;
  /** comparison function: by lexical order */
  public static final int CMPLEXICAL = 0;
  /** comparison function: as decimal strings of real numbers */
  public static final int CMPDECIMAL = 1;
  /** comparison function: as 32-bit integers in the native byte order */
  public static final int CMPINT32 = 2;
  /** comparison function: as 64-bit integers in the native byte order */
  public static final int CMPINT64 = 3;
  /** tuning option: use 64-bit bucket array */
  public static final int TLARGE  =  1 << 0;
  /** tuning option: compress each record with Deflate */
  public static final int TDEFLATE = 1 << 1;
  /** tuning option: compress each record with BZIP2 */
  public static final int TBZIP = 1 << 2;
  /** tuning option: compress each record with TCBS */
  public static final int TTCBS = 1 << 3;
  /** open mode: open as a reader */
  public static final int OREADER = 1 << 0;
  /** open mode: open as a writer */
  public static final int OWRITER = 1 << 1;
  /** open mode: writer creating */
  public static final int OCREAT = 1 << 2;
  /** open mode: writer truncating */
  public static final int OTRUNC = 1 << 3;
  /** open mode: open without locking */
  public static final int ONOLCK = 1 << 4;
  /** open mode: lock without blocking */
  public static final int OLCKNB = 1 << 5;
  /** open mode: synchronize every transaction */
  public static final int OTSYNC = 1 << 6;
  //----------------------------------------------------------------
  // public static methods
  //----------------------------------------------------------------
  /**
   * Get the message string corresponding to an error code.
   * @param ecode the error code.
   * @return the message string of the error code.
   */
  public static native String errmsg(int ecode);
  //----------------------------------------------------------------
  // private static methods
  //----------------------------------------------------------------
  /**
   * Initialize the class.
   */
  private static native void init();
  //----------------------------------------------------------------
  // private fields
  //----------------------------------------------------------------
  /** pointer to the native object */
  private long ptr = 0;
  /** embedded cursor for iterator */
  private BDBCUR cur = null;
  //----------------------------------------------------------------
  // constructors and finalizers
  //----------------------------------------------------------------
  /**
   * Create a B+ tree database object.
   */
  public BDB(){
    initialize();
    cur = new BDBCUR(this);
  }
  /**
   * Release resources.
   */
  protected void finalize(){
    destruct();
  }
  //----------------------------------------------------------------
  // public methods
  //----------------------------------------------------------------
  /**
   * Get the last happened error code.
   * @return the last happened error code.
   */
  public native int ecode();
  /**
   * Get the message string corresponding to the last happened error code.
   * @return the message string of the error code.
   */
  public String errmsg(){
    return errmsg(ecode());
  }
  /**
   * Set the custom comparator object.
   * @param cmp the custom comparator object.
   * @return If successful, it is true, else, it is false.
   * @note The default comparison function compares keys of two records by lexical order.  The
   * comparison function should be set before the database is opened.  Moreover, User-defined
   * comparators should be set every time the database is being opened.
   */
  public native boolean setcomparator(BDBCMP cmp);
  /**
   * Set the built-in comparison function.
   * @param cmp the constant of a built-in comparison function.
   * @return If successful, it is true, else, it is false.
   * @note The default comparison function compares keys of two records by lexical order.  The
   * constants `BDB.CMPLEXICAL' (dafault), `BDB.CMPDECIMAL', `BDB.CMPINT32', and `BDB.CMPINT64'
   * are supported.  The comparison function should be set before the database is opened.
   */
  public native boolean setcmpfunc(int cmp);
  /**
   * Set the tuning parameters.
   * @param lmemb the number of members in each leaf page.  If it is not more than 0, the default
   * value is specified.  The default value is 128.
   * @param nmemb the number of members in each non-leaf page.  If it is not more than 0, the
   * default value is specified.  The default value is 256.
   * @param bnum the number of elements of the bucket array.  If it is not more than 0, the
   * default value is specified.  The default value is 32749.  Suggested size of the bucket array
   * is about from 1 to 4 times of the number of all pages to be stored.
   * @param apow the size of record alignment by power of 2.  If it is negative, the default
   * value is specified.  The default value is 8 standing for 2^8=256.
   * @param fpow the maximum number of elements of the free block pool by power of 2.  If it is
   * negative, the default value is specified.  The default value is 10 standing for 2^10=1024.
   * @param opts options by bitwise-or: `BDB.TLARGE' specifies that the size of the database can
   * be larger than 2GB by using 64-bit bucket array, `BDB.TDEFLATE' specifies that each record
   * is compressed with Deflate encoding, `BDB.TBZIP' specifies that each record is compressed
   * with BZIP2 encoding, `BDB.TTCBS' specifies that each record is compressed with TCBS
   * encoding.
   * @return If successful, it is true, else, it is false.
   * @note The tuning parameters of the database should be set before the database is opened.
   */
  public native boolean tune(int lmemb, int nmemb, long bnum, int apow, int fpow, int opts);
  /**
   * Set the caching parameters.
   * @param lcnum the maximum number of leaf nodes to be cached.  If it is not more than 0, the
   * default value is specified.  The default value is 1024.
   * @param ncnum the maximum number of non-leaf nodes to be cached.  If it is not more than 0,
   * the default value is specified.  The default value is 512.
   * @return If successful, it is true, else, it is false.
   * @note The tuning parameters of the database should be set before the database is opened.
   */
  public native boolean setcache(int lcnum, int ncnum);
  /**
   * Set the size of the extra mapped memory.
   * @param xmsiz the size of the extra mapped memory.  If it is not more than 0, the extra
   * mapped memory is disabled.  It is disabled by default.
   * @return If successful, it is true, else, it is false.
   * @note The mapping parameters should be set before the database is opened.
   */
  public native boolean setxmsiz(long xmsiz);
  /**
   * Set the unit step number of auto defragmentation.
   * @param dfunit the unit step number.  If it is not more than 0, the auto defragmentation is
   * disabled.  It is disabled by default.
   * @return If successful, it is true, else, it is false.
   * @note The defragmentation parameters should be set before the database is opened.
   */
  public native boolean setdfunit(int dfunit);
  /**
   * Open a database file.
   * @param path the path of the database file.
   * @param omode the connection mode: `BDB.OWRITER' as a writer, `BDB.OREADER' as a reader.  If
   * the mode is `BDB.OWRITER', the following may be added by bitwise-or: `BDB.OCREAT', which
   * means it creates a new database if not exist, `BDB.OTRUNC', which means it creates a new
   * database regardless if one exists, `BDB.OTSYNC', which means every transaction synchronizes
   * updated contents with the device.  Both of `BDB.OREADER' and `BDB.OWRITER' can be added to
   * by bitwise-or: `BDB.ONOLCK', which means it opens the database file without file locking, or
   * `BDB.OLCKNB', which means locking is performed without blocking.
   * @return If successful, it is true, else, it is false.
   */
  public native boolean open(String path, int omode);
  /**
   * Open a database file.
   * The same as `open(name, BDB.OREADER)'.
   * @see #open(String, int)
   */
  public boolean open(String name){
    return open(name, OREADER);
  }
  /**
   * Close the database file.
   * @return If successful, it is true, else, it is false.
   * @note Update of a database is assured to be written when the database is closed.  If a
   * writer opens a database but does not close it appropriately, the database will be broken.
   */
  public native boolean close();
  /**
   * Store a record.
   * @param key the key.
   * @param value the value.
   * @return If successful, it is true, else, it is false.
   * @note If a record with the same key exists in the database, it is overwritten.
   */
  public native boolean put(byte[] key, byte[] value);
  /**
   * Store a record.
   * The same as `put(key.getBytes(), value.getBytes())'.
   * @see #put(byte[], byte[])
   */
  public boolean put(String key, String value){
    return put(key.getBytes(), value.getBytes());
  }
  /**
   * Store a new record.
   * @param key the key.
   * @param value the value.
   * @return If successful, it is true, else, it is false.
   * @note If a record with the same key exists in the database, this method has no effect.
   */
  public native boolean putkeep(byte[] key, byte[] value);
  /**
   * Store a new record.
   * The same as `putkeep(key.getBytes(), value.getBytes())'.
   * @see #putkeep(byte[], byte[])
   */
  public boolean putkeep(String key, String value){
    return putkeep(key.getBytes(), value.getBytes());
  }
  /**
   * Concatenate a value at the end of the existing record.
   * @param key the key.
   * @param value the value.
   * @return If successful, it is true, else, it is false.
   * @note If there is no corresponding record, a new record is created.
   */
  public native boolean putcat(byte[] key, byte[] value);
  /**
   * Concatenate a value at the end of the existing record.
   * The same as `putcat(key.getBytes(), value.getBytes())'.
   * @see #putcat(byte[], byte[])
   */
  public boolean putcat(String key, String value){
    return putcat(key.getBytes(), value.getBytes());
  }
  /**
   * Store a record with allowing duplication of keys.
   * @param key the key.
   * @param value the value.
   * @return If successful, it is true, else, it is false.
   * @note If a record with the same key exists in the database, the new record is placed after
   * the existing one.
   */
  public native boolean putdup(byte[] key, byte[] value);
  /**
   * Store a record with allowing duplication of keys.
   * The same as `putdup(key.getBytes(), value.getBytes())'.
   * @see #putdup(byte[], byte[])
   */
  public boolean putdup(String key, String value){
    return putdup(key.getBytes(), value.getBytes());
  }
  /**
   * Store records with allowing duplication of keys.
   * @param key the key.
   * @param values a list of the values.  Type of each element should be `byte[]'.
   * @return If successful, it is true, else, it is false.
   * @note If a record with the same key exists in the database, the new records are placed
   * after the existing one.
   */
  public native boolean putlist(byte[] key, List values);
  /**
   * Store records with allowing duplication of keys.
   * The same as `putlist(key.getBytes(), value)'.  Type of each element should be `String'.
   * @see #putlist(byte[], List)
   */
  public boolean putlist(String key, List values){
    List avalues = new ArrayList();
    Iterator it = values.iterator();
    while(it.hasNext()){
      String value = (String)it.next();
      avalues.add(value.getBytes());
    }
    return putlist(key.getBytes(), avalues);
  }
  /**
   * Remove a record.
   * @param key the key.
   * @return If successful, it is true, else, it is false.
   * @note If the key of duplicated records is specified, the first one is selected.
   */
  public native boolean out(byte[] key);
  /**
   * Remove a record.
   * The same as `out(key.getBytes())'.
   * @see #out(byte[])
   */
  public boolean out(String key){
    return out(key.getBytes());
  }
  /**
   * Remove records.
   * @param key the key.
   * @return If successful, it is true, else, it is false.
   * @note If the key of duplicated records is specified, all of them are removed.
   */
  public native boolean outlist(byte[] key);
  /**
   * Remove records.
   * The same as `outlist(key.getBytes())'.
   * @see #outlist(byte[])
   */
  public boolean outlist(String key){
    return outlist(key.getBytes());
  }
  /**
   * Retrieve a record.
   * @param key the key.
   * @return If successful, it is the value of the corresponding record.  `null' is returned if
   * no record corresponds.
   * @note If the key of duplicated records is specified, the first one is selected.
   */
  public native byte[] get(byte[] key);
  /**
   * Retrieve a record.
   * The same as `new String(get(key.getBytes()), "UTF-8")'.
   * @see #get(byte[])
   */
  public String get(String key){
    byte[] value = get(key.getBytes());
    return value != null ? Util.otos(value) : null;
  }
  /**
   * Retrieve records.
   * @param key the key.
   * @return If successful, it is a list of the values of the corresponding records.  `null' is
   * returned if no record corresponds.  Type of each element is `byte[]'.
   */
  public native List getlist(byte[] key);
  /**
   * Retrieve records.
   * The same as `getlist(key.getBytes())'.  However, type of each element is `String'.
   * @see #get(byte[])
   */
  public List getlist(String key){
    List values = getlist(key.getBytes());
    if(values != null){
      List svalues = new ArrayList();
      Iterator it = values.iterator();
      while(it.hasNext()){
        byte[] value = (byte[])it.next();
        svalues.add(Util.otos(value));
      }
      return svalues;
    }
    return null;
  }
  /**
   * Get the number of records corresponding a key.
   * @param key the key.
   * @return If successful, it is the number of the corresponding records, else, it is 0.
   */
  public native int vnum(byte[] key);
  /**
   * Get the number of records corresponding a key.
   * The same as `vnum(key.getBytes())'.
   * @see #vnum(byte[])
   */
  public int vnum(String key){
    return vnum(key.getBytes());
  }
  /**
   * Get the size of the value of a record.
   * @param key the key.
   * @return If successful, it is the size of the value of the corresponding record, else, it
   * is -1.
   * @note If the key of duplicated records is specified, the first one is selected.
   */
  public native int vsiz(byte[] key);
  /**
   * Get the size of the value of a record.
   * The same as `vsiz(key.getBytes())'.
   * @see #vsiz(byte[])
   */
  public int vsiz(String key){
    return vsiz(key.getBytes());
  }
  /**
   * Initialize the iterator.
   * @return If successful, it is true, else, it is false.
   * @note The iterator is implemented with the cursor embedded in the database.
   */
  public boolean iterinit(){
    return cur.first();
  }
  /**
   * Get the next key of the iterator.
   * @return If successful, it is the next key, else, it is `null'.  `null' is returned when no
   * record is to be get out of the iterator.
   */
  public byte[] iternext(){
    byte[] key = cur.key();
    cur.next();
    return key;
  }
  /**
   * Get the next key of the iterator.
   * The same as `new String(iternext(), "UTF-8")'.
   * @see #iternext()
   */
  public String iternext2(){
    String key = cur.key2();
    cur.next();
    return key;
  }
  /**
   * Get keys of ranged records.
   * @param bkey the key of the beginning border.  If it is `null', the first record is specified.
   * @param binc whether the beginning border is inclusive or not.
   * @param ekey the key of the ending border.  If it is `null', the last record is specified.
   * @param einc whether the ending border is inclusive or not.
   * @param max the maximum number of keys to be fetched.  If it is negative, no limit is
   * specified.
   * @return a list object of the keys of the corresponding records.  This method does never fail.
   * It returns an empty list even if no record corresponds.
   */
  public native List range(byte[] bkey, boolean binc, byte[] ekey, boolean einc, int max);
  /**
   * Get keys of ranged records.
   * The same as `range(bkey.getBytes(), binc, ekey.getBytes(), einc, max)'.  However, type of
   * each element is `String'.
   * @see #range(byte[], boolean, byte[], boolean, int)
   */
  public List range(String bkey, boolean binc, String ekey, boolean einc, int max){
    List keys = range(bkey.getBytes(), binc, ekey.getBytes(), einc, max);
    List skeys = new ArrayList();
    Iterator it = keys.iterator();
    while(it.hasNext()){
      byte[] key = (byte[])it.next();
      skeys.add(Util.otos(key));
    }
    return skeys;
  }
  /**
   * Get forward matching keys.
   * @param prefix the prefix of the corresponding keys.
   * @param max the maximum number of keys to be fetched.  If it is negative, no limit is
   * specified.
   * @return a list object of the keys of the corresponding records.  This method does never fail.
   * It returns an empty list even if no record corresponds.
   */
  public native List fwmkeys(byte[] prefix, int max);
  /**
   * Get forward matching keys.
   * The same as `fwmkeys(prefix.getBytes(), max)'.  However, type of each element is `String'.
   * @see #fwmkeys(byte[], int)
   */
  public List fwmkeys(String prefix, int max){
    List keys = fwmkeys(prefix.getBytes(), max);
    List skeys = new ArrayList();
    Iterator it = keys.iterator();
    while(it.hasNext()){
      byte[] key = (byte[])it.next();
      skeys.add(Util.otos(key));
    }
    return skeys;
  }
  /**
   * Add an integer to a record.
   * @param key the key.
   * @param num the additional value.
   * @return If successful, it is the summation value, else, it is `Integer.MIN_VALUE'.
   * @note If the corresponding record exists, the value is treated as an integer and is added to.
   * If no record corresponds, a new record of the additional value is stored.
   */
  public native int addint(byte[] key, int num);
  /**
   * Add an integer to a record.
   * The same as `addint(key.getBytes(), num)'.
   * @see #addint(byte[], int)
   */
  public int addint(String key, int num){
    return addint(key.getBytes(), num);
  }
  /**
   * Add a real number to a record.
   * @param key the key.
   * @param num the additional value.
   * @return If successful, it is the summation value, else, it is `Double.NaN'.
   * @note If the corresponding record exists, the value is treated as a real number and is added
   * to.  If no record corresponds, a new record of the additional value is stored.
   */
  public native double adddouble(byte[] key, double num);
  /**
   * Add a real number to a record.
   * The same as `adddouble(key.getBytes(), num)'.
   * @see #adddouble(byte[], double)
   */
  public double adddouble(String key, double num){
    return adddouble(key.getBytes(), num);
  }
  /**
   * Synchronize updated contents with the file and the device.
   * @return If successful, it is true, else, it is false.
   * @note This method is useful when another process connects the same database file.
   */
  public native boolean sync();
  /**
   * Optimize the database file.
   * @param lmemb the number of members in each leaf page.  If it is not more than 0, the current
   * setting is not changed.
   * @param nmemb the number of members in each non-leaf page.  If it is not more than 0, the
   * current setting is not changed.
   * @param bnum the number of elements of the bucket array.  If it is not more than 0, the
   * default value is specified.  The default value is two times of the number of pages.
   * @param apow the size of record alignment by power of 2.  If it is negative, the current
   * setting is not changed.
   * @param fpow the maximum number of elements of the free block pool by power of 2.  If it is
   * negative, the current setting is not changed.
   * @param opts options by bitwise-or: `BDB.TLARGE' specifies that the size of the database can
   * be larger than 2GB by using 64-bit bucket array, `BDB.TDEFLATE' specifies that each record
   * is compressed with Deflate encoding, `BDB.TBZIP' specifies that each record is compressed
   * with BZIP2 encoding, `BDB.TTCBS' specifies that each record is compressed with TCBS
   * encoding.  If it is 0xff, the current setting is not changed.
   * @return If successful, it is true, else, it is false.
   * @note This method is useful to reduce the size of the database file with data fragmentation
   * by successive updating.
   */
  public native boolean optimize(int lmemb, int nmemb, long bnum, int apow, int fpow, int opts);
  /**
   * Optimize the database file.
   * The same as `optimize(-1, -1, -1, -1, -1, 0xff)'.
   * @see #optimize(int, int, long, int, int, int)
   */
  public boolean optimize(){
    return optimize(-1, -1, -1, -1, -1, 0xff);
  }
  /**
   * Remove all records.
   * @return If successful, it is true, else, it is false.
   */
  public native boolean vanish();
  /**
   * Copy the database file.
   * @param path the path of the destination file.  If it begins with `@', the trailing substring
   * is executed as a command line.
   * @return If successful, it is true, else, it is false.  False is returned if the executed
   * command returns non-zero code.
   * @note The database file is assured to be kept synchronized and not modified while the
   * copying or executing operation is in progress.  So, this method is useful to create a backup
   * file of the database file.
   */
  public native boolean copy(String path);
  /**
   * Begin the transaction.
   * @return If successful, it is true, else, it is false.
   * @note The database is locked by the thread while the transaction so that only one
   * transaction can be activated with a database object at the same time.  Thus, the
   * serializable isolation level is assumed if every database operation is performed in the
   * transaction.  Because all pages are cached on memory while the transaction, the amount of
   * referred records is limited by the memory capacity.  If the database is closed during
   * transaction, the transaction is aborted implicitly.
   */
  public native boolean tranbegin();
  /**
   * Commit the transaction.
   * @return If successful, it is true, else, it is false.
   * @note Update in the transaction is fixed when it is committed successfully.
   */
  public native boolean trancommit();
  /**
   * Abort the transaction.
   * @return If successful, it is true, else, it is false.
   * @note Update in the transaction is discarded when it is aborted.  The state of the database
   * is rollbacked to before transaction.
   */
  public native boolean tranabort();
  /**
   * Get the path of the database file.
   * @return the path of the database file or `null' if the object does not connect to any
   * database file.
   */
  public native String path();
  /**
   * Get the number of records.
   * @return the number of records or 0 if the object does not connect to any database file.
   */
  public native long rnum();
  /**
   * Get the size of the database file.
   * @return the size of the database file or 0 if the object does not connect to any database
   * file.
   */
  public native long fsiz();
  //----------------------------------------------------------------
  // private methods
  //----------------------------------------------------------------
  /**
   * Initialize the object.
   */
  private native void initialize();
  /**
   * Release resources.
   */
  private native void destruct();
}



/* END OF FILE */
