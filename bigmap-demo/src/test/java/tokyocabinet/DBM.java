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
 * Common interface of DBM.
 */
public interface DBM {
  /**
   * Store a record.
   * @param key the key.
   * @param value the value.
   * @return If successful, it is true, else, it is false.
   * @note If a record with the same key exists in the database, it is overwritten.
   */
  public boolean put(byte[] key, byte[] value);
  /**
   * Store a record.
   * The same as `put(key.getBytes(), value.getBytes())'.
   * @see #put(byte[], byte[])
   */
  public boolean put(String key, String value);
  /**
   * Store a new record.
   * @param key the key.
   * @param value the value.
   * @return If successful, it is true, else, it is false.
   * @note If a record with the same key exists in the database, this method has no effect.
   */
  public boolean putkeep(byte[] key, byte[] value);
  /**
   * Store a new record.
   * The same as `putkeep(key.getBytes(), value.getBytes())'.
   * @see #putkeep(byte[], byte[])
   */
  public boolean putkeep(String key, String value);
  /**
   * Remove a record.
   * @param key the key.
   * @return If successful, it is true, else, it is false.
   */
  public boolean out(byte[] key);
  /**
   * Remove a record.
   * The same as `out(key.getBytes())'.
   * @see #out(byte[])
   */
  public boolean out(String key);
  /**
   * Retrieve a record.
   * @param key the key.
   * @return If successful, it is the value of the corresponding record.  `null' is returned if
   * no record corresponds.
   */
  public byte[] get(byte[] key);
  /**
   * Retrieve a record.
   * The same as `new String(get(key.getBytes()), "UTF-8")'.
   * @see #get(byte[])
   */
  public String get(String key);
  /**
   * Initialize the iterator.
   * @return If successful, it is true, else, it is false.
   */
  public boolean iterinit();
  /**
   * Get the next key of the iterator.
   * @return If successful, it is the next key, else, it is `null'.  `null' is returned when no
   * record is to be get out of the iterator.
   */
  public byte[] iternext();
  /**
   * Get the next key of the iterator.
   * The same as `new String(iternext(), "UTF-8")'.
   * @see #iternext()
   */
  public String iternext2();
  /**
   * Get forward matching keys.
   * @param prefix the prefix of the corresponding keys.
   * @param max the maximum number of keys to be fetched.  If it is negative, no limit is
   * specified.
   * @return a list object of the keys of the corresponding records.  This method does never fail.
   * It returns an empty list even if no record corresponds.
   * @note This function may be very slow because every key in the database is scanned.
   */
  public List fwmkeys(byte[] prefix, int max);
  /**
   * Get forward matching keys.
   * The same as `fwmkeys(prefix.getBytes(), max)'.  However, type of each element is `String'.
   * @see #fwmkeys(byte[], int)
   */
  public List fwmkeys(String prefix, int max);
  /**
   * Add an integer to a record
   * @param key the key.
   * @param num the additional value.
   * @return If successful, it is the summation value, else, it is `Integer.MIN_VALUE'.
   * @note If the corresponding record exists, the value is treated as an integer and is added to.
   * If no record corresponds, a new record of the additional value is stored.
   */
  public int addint(byte[] key, int num);
  /**
   * Add an integer to a record
   * The same as `addint(key.getBytes(), num)'.
   * @see #addint(byte[], int)
   */
  public int addint(String key, int num);
  /**
   * Add a real number to a record
   * @param key the key.
   * @param num the additional value.
   * @return If successful, it is the summation value, else, it is `Double.NaN'.
   * @note If the corresponding record exists, the value is treated as a real number and is added
   * to.  If no record corresponds, a new record of the additional value is stored.
   */
  public double adddouble(byte[] key, double num);
  /**
   * Add a real number to a record
   * The same as `adddouble(key.getBytes(), num)'.
   * @see #adddouble(byte[], double)
   */
  public double adddouble(String key, double num);
  /**
   * Get the number of records.
   * @return the number of records or 0 if the object does not connect to any database file.
   */
  public long rnum();
  /**
   * Get the size of the database file.
   * @return the size of the database file or 0 if the object does not connect to any database
   * file.
   */
  public long fsiz();
}



/* END OF FILE */
