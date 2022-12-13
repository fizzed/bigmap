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
 * Cursor is a mechanism to access each record of B+ tree database in ascending or descending
 * order.
 */
public class BDBCUR {
  //----------------------------------------------------------------
  // static initializer
  //----------------------------------------------------------------
  static {
    System.loadLibrary("jtokyocabinet");
    init();
  }
  //----------------------------------------------------------------
  // public constants
  //---------------------------------------------------------------
  /** cursor put mode: current */
  public static final int CPCURRENT = 0;
  /** cursor put mode: before */
  public static final int CPBEFORE = 1;
  /** cursor put mode: after */
  public static final int CPAFTER = 2;
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
  /** host database object */
  private BDB bdb = null;
  //----------------------------------------------------------------
  // constructors and finalizers
  //----------------------------------------------------------------
  /**
   * Create a cursor object.
   * @param bdb the B+ tree database object.
   * @note The cursor is available only after initialization with the `first' or the `jump'
   * methods and so on.  Moreover, the position of the cursor will be indefinite when the
   * database is updated after the initialization of the cursor.
   */
  public BDBCUR(BDB bdb){
    initialize(bdb);
    this.bdb = bdb;
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
   * Move the cursor to the first record.
   * @return If successful, it is true, else, it is false.  False is returned if there is no
   * record in the database.
   */
  public native boolean first();
  /**
   * Move the cursor to the last record.
   * @return If successful, it is true, else, it is false.  False is returned if there is no
   * record in the database.
   */
  public native boolean last();
  /**
   * Move the cursor to the front of records corresponding a key.
   * @param key the key.
   * @return If successful, it is true, else, it is false.  False is returned if there is no
   * record corresponding the condition.
   * @note The cursor is set to the first record corresponding the key or the next substitute if
   * completely matching record does not exist.
   */
  public native boolean jump(byte[] key);
  /**
   * Move the cursor to the front of records corresponding a key.
   * The same as `open(key.getBytes())'.
   * @see #jump(byte[])
   */
  public boolean jump(String key){
    return jump(key.getBytes());
  }
  /**
   * Move the cursor to the previous record.
   * @return If successful, it is true, else, it is false.  False is returned if there is no
   * previous record.
   */
  public native boolean prev();
  /**
   * Move the cursor to the next record.
   * @return If successful, it is true, else, it is false.  False is returned if there is no
   * next record.
   */
  public native boolean next();
  /**
   * Insert a record around the cursor.
   * @param value the value.
   * @param cpmode detail adjustment: `BDBCUR.CPCURRENT', which means that the value of the
   * current record is overwritten, `BDBCUR.CPBEFORE', which means that the new record is
   * inserted before the current record, `BDBCUR.CPAFTER', which means that the new record is
   * inserted after the current record.
   * @return If successful, it is true, else, it is false.  False is returned when the cursor is
   * at invalid position.
   * @note After insertion, the cursor is moved to the inserted record.
   */
  public native boolean put(byte[] value, int cpmode);
  /**
   * Insert a record around the cursor.
   * The same as `put(value.getBytes(), cpmode)'.
   * @see #put(byte[], int)
   */
  public boolean put(String value, int cpmode){
    return put(value.getBytes(), cpmode);
  }
  /**
   * Remove the record where the cursor is.
   * @return If successful, it is true, else, it is false.  False is returned when the cursor is
   * at invalid position.
   * @note After deletion, the cursor is moved to the next record if possible.
   */
  public native boolean out();
  /**
   * Get the key of the record where the cursor is.
   * @return If successful, it is the key, else, it is `null'.  'null' is returned when the
   * cursor is at invalid position.
   */
  public native byte[] key();
  /**
   * Get the key of the record where the cursor is.
   * The same as `new String(key(), "UTF-8")'.
   * @see #key()
   */
  public String key2(){
    byte[] tkey = key();
    return tkey != null ? Util.otos(tkey) : null;
  }
  /**
   * Get the value of the record where the cursor is.
   * @return If successful, it is the value, else, it is `null'.  'null' is returned when the
   * cursor is at invalid position.
   */
  public native byte[] val();
  /**
   * Get the value of the record where the cursor is.
   * The same as `new String(value(), "UTF-8")'.
   * @see #val()
   */
  public String val2(){
    byte[] tval = val();
    return tval != null ? Util.otos(tval) : null;
  }
  //----------------------------------------------------------------
  // private methods
  //----------------------------------------------------------------
  /**
   * Initialize the object.
   */
  private native void initialize(BDB bdb);
  /**
   * Release resources.
   */
  private native void destruct();
}



/* END OF FILE */
