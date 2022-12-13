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
 * Query is a mechanism to search for and retrieve records corresponding conditions from table
 * database.
 */
public class TDBQRY {
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
  /** query condition: string is equal to */
  public static final int QCSTREQ = 0;
  /** query condition: string is included in */
  public static final int QCSTRINC = 1;
  /** query condition: string begins with */
  public static final int QCSTRBW = 2;
  /** query condition: string ends with */
  public static final int QCSTREW = 3;
  /** query condition: string includes all tokens in */
  public static final int QCSTRAND = 4;
  /** query condition: string includes at least one token in */
  public static final int QCSTROR = 5;
  /** query condition: string is equal to at least one token in */
  public static final int QCSTROREQ = 6;
  /** query condition: string matches regular expressions of */
  public static final int QCSTRRX = 7;
  /** query condition: number is equal to */
  public static final int QCNUMEQ = 8;
  /** query condition: number is greater than */
  public static final int QCNUMGT = 9;
  /** query condition: number is greater than or equal to */
  public static final int QCNUMGE = 10;
  /** query condition: number is less than */
  public static final int QCNUMLT = 11;
  /** query condition: number is less than or equal to */
  public static final int QCNUMLE = 12;
  /** query condition: number is between two tokens of */
  public static final int QCNUMBT = 13;
  /** query condition: number is equal to at least one token in */
  public static final int QCNUMOREQ = 14;
  /** query condition: full-text search with the phrase of */
  public static final int QCFTSPH = 15;
  /** query condition: full-text search with all tokens in */
  public static final int QCFTSAND = 16;
  /** query condition: full-text search with at least one token in */
  public static final int QCFTSOR = 17;
  /** query condition: full-text search with the compound expression of */
  public static final int QCFTSEX = 18;
  /** query condition: negation flag */
  public static final int QCNEGATE = 1 << 24;
  /** query condition: no index flag */
  public static final int QCNOIDX = 1 << 25;
  /** order type: string ascending */
  public static final int QOSTRASC = 0;
  /** order type: string descending */
  public static final int QOSTRDESC = 1;
  /** order type: number ascending */
  public static final int QONUMASC = 2;
  /** order type: number descending */
  public static final int QONUMDESC = 3;
  /** set operation type: union */
  public static final int MSUNION = 0;
  /** set operation type: intersection */
  public static final int MSISECT = 1;
  /** set operation type: difference */
  public static final int MSDIFF = 2;
  /** KWIC option: mark up by tabs */
  public static final int KWMUTAB = 1 << 0;
  /** KWIC option: mark up by control characters */
  public static final int KWMUCTRL = 1 << 1;
  /** KWIC option: mark up by square brackets */
  public static final int KWMUBRCT = 1 << 2;
  /** KWIC option: do not overlap */
  public static final int KWNOOVER = 1 << 24;
  /** KWIC option: pick up the lead string */
  public static final int KWPULEAD = 1 << 25;
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
  private TDB tdb = null;
  //----------------------------------------------------------------
  // constructors and finalizers
  //----------------------------------------------------------------
  /**
   * Create a query object.
   * @param tdb the table database object.
   */
  public TDBQRY(TDB tdb){
    initialize(tdb);
    this.tdb = tdb;
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
   * Add a narrowing condition.
   * @param name the name of a column.  An empty string means the primary key.
   * @param op an operation type: `TDBQRY.QCSTREQ' for string which is equal to the expression,
   * `TDBQRY.QCSTRINC' for string which is included in the expression, `TDBQRY.QCSTRBW' for
   * string which begins with the expression, `TDBQRY.QCSTREW' for string which ends with the
   * expression, `TDBQRY.QCSTRAND' for string which includes all tokens in the expression,
   * `TDBQRY.QCSTROR' for string which includes at least one token in the expression,
   * `TDBQRY.QCSTROREQ' for string which is equal to at least one token in the expression,
   * `TDBQRY.QCSTRRX' for string which matches regular expressions of the expression,
   * `TDBQRY.QCNUMEQ' for number which is equal to the expression, `TDBQRY.QCNUMGT' for number
   * which is greater than the expression, `TDBQRY.QCNUMGE' for number which is greater than or
   * equal to the expression, `TDBQRY.QCNUMLT' for number which is less than the expression,
   * `TDBQRY.QCNUMLE' for number which is less than or equal to the expression, `TDBQRY.QCNUMBT'
   * for number which is between two tokens of the expression, `TDBQRY.QCNUMOREQ' for number
   * which is equal to at least one token in the expression, `TDBQRY.QCFTSPH' for full-text
   * search with the phrase of the expression, `TDBQRY.QCFTSAND' for full-text search with all
   * tokens in the expression, `TDBQRY.QCFTSOR' for full-text search with at least one token in
   * the expression, `TDBQRY.QCFTSEX' for full-text search with the compound expression.  All
   * operations can be flagged by bitwise-or: `TDBQRY.QCNEGATE' for negation, `TDBQRY.QCNOIDX'
   * for using no index.
   * @param expr an operand exression.
   */
  public native void addcond(String name, int op, String expr);
  /**
   * Set the order of the result.
   * @param name the name of a column.  An empty string means the primary key.
   * @param type the order type: `TDBQRY.QOSTRASC' for string ascending, `TDBQRY.QOSTRDESC' for
   * string descending, `TDBQRY.QONUMASC' for number ascending, `TDBQRY.QONUMDESC' for number
   * descending.
   */
  public native void setorder(String name, int type);
  /**
   * Set the maximum number of records of the result.
   * @param max the maximum number of records of the result.  If it is negative, no limit is
   * specified.
   * @param skip the maximum number of records of the result.  If it is not more than 0, no
   * record is skipped.
   */
  public native void setlimit(int max, int skip);
  /**
   * Execute the search.
   * @return a list object of the primary keys of the corresponding records.  This method does
   * never fail.  It returns an empty array even if no record corresponds.
   */
  public native List search();
  /**
   * Remove each corresponding record.
   * @return If successful, the return value is true, else, it is false.
   */
  public native boolean searchout();
  /**
   * Process each corresponding record.
   * @param qp specifies the query processor object.
   * @return If successful, the return value is true, else, it is false.
   */
  public native boolean proc(TDBQRYPROC qp);
  /**
   * Get the hint string.
   * @return the hint string.
   */
  public native String hint();
  /**
   * Retrieve records with multiple query objects and get the set of the result.
   * @param others an array of the query objects except for the self object.
   * @param type a set operation type: `TDBQRY.MSUNION' for the union set, `TDBQRY.MSISECT' for
   * the intersection set, `TDBQRY.MSDIFF' for the difference set.  If it is not defined,
   * `TDBQRY.MSUNION' is specified.
   * @return a list object of the primary keys of the corresponding records.  This method does
   * never fail.  It returns an empty array even if no record corresponds.
   * @note If the first query object has the order setting, the result array is sorted by the
   * order.
   */
  public native List metasearch(TDBQRY[] others, int type);
  /**
   * Generate keyword-in-context strings.
   * @param cols a hash containing columns.
   * @param name the name of a column.  If it is not defined, the first column of the query is
   * specified.
   * @param width the width of strings picked up around each keyword.  If it is negative, the
   * whole text is picked up.
   * @param opts options by bitwise-or: `TDBQRY.KWMUTAB' specifies that each keyword is marked up
   * between two tab characters, `TDBQRY.KWMUCTRL' specifies that each keyword is marked up by
   * the STX (0x02) code and the ETX (0x03) code, `TDBQRY.KWMUBRCT' specifies that each keyword
   * is marked up by the two square brackets, `TDBQRY.KWNOOVER' specifies that each context does
   * not overlap, `TDBQRY.KWPULEAD' specifies that the lead string is picked up forcibly.
   * @return an array of strings around keywords.
   */
  public String[] kwic(Map cols, String name, int width, int opts){
    return kwicimpl(Util.maptostrary(cols), name, width, opts);
  }
  //----------------------------------------------------------------
  // private methods
  //----------------------------------------------------------------
  /**
   * Initialize the object.
   */
  private native void initialize(TDB tdb);
  /**
   * Release resources.
   */
  private native void destruct();
  /**
   * Generate a keyword-in-context string.
   */
  private native String[] kwicimpl(byte[][] cols, String name, int width, int opts);
}



/* END OF FILE */
