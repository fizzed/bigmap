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
 * Abstract database is a set of interfaces to use on-memory hash database, on-memory tree
 * database, hash database, B+ tree database, fixed-length database, and table database with the
 * same API.  Before operations to store or retrieve records, it is necessary to connect the
 * abstract database object to the concrete one.  The method `open' is used to open a concrete
 * database and the method `close' is used to close the database.  To avoid data missing or
 * corruption, it is important to close every database instance when it is no longer in use.  It
 * is forbidden for multible database objects in a process to open the same database at the same
 * time.
 */
public class ADB implements DBM {
  //----------------------------------------------------------------
  // static initializer
  //----------------------------------------------------------------
  static {
    Loader.load();
    init();
  }
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
  //----------------------------------------------------------------
  // constructors and finalizers
  //----------------------------------------------------------------
  /**
   * Create an abstract database object.
   */
  public ADB(){
    initialize();
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
   * Open a database.
   * @param name the name of the database.  If it is "*", the database will be an on-memory hash
   * database.  If it is "+", the database will be an on-memory tree database.  If its suffix is
   * ".tch", the database will be a hash database.  If its suffix is ".tcb", the database will be
   * a B+ tree database.  If its suffix is ".tcf", the database will be a fixed-length database.
   * If its suffix is ".tct", the database will be a table database.  Otherwise, this method
   * fails.  Tuning parameters can trail the name, separated by "#".  Each parameter is composed
   * of the name and the value, separated by "=".  On-memory hash database supports "bnum",
   * "capnum", and "capsiz".  On-memory tree database supports "capnum" and "capsiz".  Hash
   * database supports "mode", "bnum", "apow", "fpow", "opts", "rcnum", and "xmsiz".  B+ tree
   * database supports "mode", "lmemb", "nmemb", "bnum", "apow", "fpow", "opts", "lcnum", "ncnum",
   * and "xmsiz".  Fixed-length database supports "mode", "width", and "limsiz".  Table database
   * supports "mode", "bnum", "apow", "fpow", "opts", "rcnum", "lcnum", "ncnum", "xmsiz", and
   * "idx".
   * @return If successful, it is true, else, it is false.
   * @note The tuning parameter "capnum" specifies the capacity number of records.  "capsiz"
   * specifies the capacity size of using memory.  Records spilled the capacity are removed by
   * the storing order.  "mode" can contain "w" of writer, "r" of reader, "c" of creating, "t" of
   * truncating, "e" of no locking, and "f" of non-blocking lock.  The default mode is relevant
   * to "wc".  "opts" can contains "l" of large option, "d" of Deflate option, "b" of BZIP2
   * option, and "t" of TCBS option.  "idx" specifies the column name of an index and its type
   * separated by ":".  For example, "casket.tch#bnum=1000000#opts=ld" means that the name of the
   * database file is "casket.tch", and the bucket number is 1000000, and the options are large
   * and Deflate.
   */
  public native boolean open(String name);
  /**
   * Close the database.
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
   * Remove a record.
   * @param key the key.
   * @return If successful, it is true, else, it is false.
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
   * Retrieve a record.
   * @param key the key.
   * @return If successful, it is the value of the corresponding record.  `null' is returned if
   * no record corresponds.
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
   * Get the size of the value of a record.
   * @param key the key.
   * @return If successful, it is the size of the value of the corresponding record, else, it
   * is -1.
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
   * @note The iterator is used in order to access the key of every record stored in a database.
   */
  public native boolean iterinit();
  /**
   * Get the next key of the iterator.
   * @return If successful, it is the next key, else, it is `null'.  `null' is returned when no
   * record is to be get out of the iterator.
   * @note It is possible to access every record by iteration of calling this method.  It is
   * allowed to update or remove records whose keys are fetched while the iteration.  However,
   * it is not assured if updating the database is occurred while the iteration.  Besides, the
   * order of this traversal access method is arbitrary, so it is not assured that the order of
   * storing matches the one of the traversal access.
   */
  public native byte[] iternext();
  /**
   * Get the next key of the iterator.
   * The same as `new String(iternext(), "UTF-8")'.
   * @see #iternext()
   */
  public String iternext2(){
    byte[] key = iternext();
    return key != null ? Util.otos(key) : null;
  }
  /**
   * Get forward matching keys.
   * @param prefix the prefix of the corresponding keys.
   * @param max the maximum number of keys to be fetched.  If it is negative, no limit is
   * specified.
   * @return a list object of the keys of the corresponding records.  This method does never fail.
   * It returns an empty list even if no record corresponds.
   * @note This function may be very slow because every key in the database is scanned.
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
   * Optimize the storage.
   * @param params specifies the string of the tuning parameters, which works as with the tuning
   * of parameters the method `open'.  If it is `null', it is not used.
   * @return If successful, it is true, else, it is false.
   */
  public native boolean optimize(String params);
  /**
   * Optimize the storage.
   * The same as `optimize(null)'.
   * @see #optimize(String)
   */
  public boolean optimize(){
    return optimize(null);
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
   * transaction.  All updated regions are kept track of by write ahead logging while the
   * transaction.  If the database is closed during transaction, the transaction is aborted
   * implicitly.
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
   * database file.  "*" stands for on-memory hash database.  "+" stands for on-memory tree
   * database.
   */
  public native String path();
  /**
   * Get the number of records.
   * @return the number of records or 0 if the object does not connect to any database instance.
   */
  public native long rnum();
  /**
   * Get the size of the database.
   * @return the size of the database or 0 if the object does not connect to any database
   * instance.
   */
  public native long size();
  /**
   * Get the size of the database.
   * The same as `size()'.
   * @see #size()
   */
  public long fsiz(){
    return size();
  }
  /**
   * Call a versatile function for miscellaneous operations.
   * @param name the name of the function.
   * @param args a list object of arguments.  If it is `null', no argument is specified.
   * @return If successful, it is an array of the result.  `null' is returned on failure.
   */
  public List misc(String name, List args){
    if(args == null) args = new ArrayList();
    byte[][] ary;
    ary = new byte[args.size()][];
    Iterator it = args.iterator();
    int anum = 0;
    while(it.hasNext()){
      Object arg = it.next();
      ary[anum++] = arg instanceof byte[] ? (byte[])arg : Util.otos(arg).getBytes();
    }
    byte[][] res = miscimpl(name, ary);
    if(res != null){
      List list = new ArrayList();
      for(int i = 0; i < res.length; i++){
        list.add(res[i]);
      }
      return list;
    }
    return null;
  }
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
  /**
   * Call a versatile function for miscellaneous operations.
   */
  private native byte[][] miscimpl(String name, byte[][] args);
}



/* END OF FILE */
