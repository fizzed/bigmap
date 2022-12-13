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
 * Custom comparator is an interface which has a method to compare two keys of B+ tree records.
 */
public interface BDBCMP {
  //----------------------------------------------------------------
  // public methods
  //----------------------------------------------------------------
  /**
   * Compare two keys of B+ tree records.
   * @param a one key.
   * @param b the other key.
   * @return It is positive if the former is big, negative if the latter is big, 0 if both are
   * equivalent.
   */
  public int compare(byte[] a, byte[] b);
}



/* END OF FILE */
