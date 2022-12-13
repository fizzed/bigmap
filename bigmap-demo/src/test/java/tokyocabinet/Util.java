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
 * Set of utility methods.
 */
public class Util {
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
  // public static methods
  //----------------------------------------------------------------
  /**
   * Get the string containing the version information.
   * @return the string containing the version information.
   */
  public static native String version();
  /**
   * Convert a string to integer.
   * @param str a decimal string.
   * @return the result integer.
   */
  public static int atoi(String str){
    int len = str.length();
    int num = 0;
    int sign = 1;
    for(int i = 0; i < len; i++){
      char c = str.charAt(i);
      if(c <= ' ') continue;
      if(c == '-'){
        sign = -1;
      } else if(c >= '0' && c <= '9'){
        num = num * 10 + c - '0';
      } else {
        break;
      }
    }
    return num * sign;
  }
  /**
   * Convert a string to integer.
   * @param str a decimal string.
   * @return the result integer.
   */
  public static long atol(String str){
    int len = str.length();
    long num = 0;
    int sign = 1;
    for(int i = 0; i < len; i++){
      char c = str.charAt(i);
      if(c <= ' ') continue;
      if(c == '-'){
        sign = -1;
      } else if(c >= '0' && c <= '9'){
        num = num * 10 + c - '0';
      } else {
        break;
      }
    }
    return num * sign;
  }
  /**
   * Convert a integer to string.
   * @param num a number.
   * @param cols the number of columns.  The result string may be longer than it.
   * @param padding a padding character to fulfil columns with.
   * @return the result string.
   */
  public static String itoa(long num, int cols, char padding){
    StringBuffer sb = new StringBuffer(cols);
    boolean minus = false;
    if(num < 0){
      num *= -1;
      cols--;
      minus = true;
    }
    int i = 0;
    while(num > 0){
      sb.insert(0, num % 10);
      num /= 10;
      i++;
    }
    while(i < cols){
      sb.insert(0, padding);
      i++;
    }
    if(minus) sb.insert(0, '-');
    return sb.toString();
  }
  /**
   * Convert an object to string.
   * @param obj an object.
   * @return the result string.
   */
  public static String otos(Object obj){
    try {
      if(obj == null) return "";
      if(obj instanceof byte[]) return new String((byte[])obj, "UTF-8");
      return obj.toString();
    } catch(Exception e){
      return "";
    }
  }
  /**
   * Get the current time.
   * @return the current time of seconds from the epoch.
   */
  public static double time(){
    return System.currentTimeMillis() / 1000.0;
  }
  /**
   * Serialize an object.
   * @param obj a serializable object.
   * @return a byte array of the serialized object or null if an error occurs.
   */
  public static byte[] serialize(Object obj){
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos = null;
    try {
      oos = new ObjectOutputStream(baos);
      oos.writeObject(obj);
      oos.flush();
      baos.flush();
      return baos.toByteArray();
    } catch(IOException e){
      return null;
    } finally {
      try {
        if(oos != null) oos.close();
      } catch(IOException e){}
    }
  }
  /**
   * Redintegrate a serialized object.
   * @param serial a byte array of the serialized object.
   * @return the original object or null if an error occurs.
   */
  public static Object deserialize(byte[] serial){
    ByteArrayInputStream bais = new ByteArrayInputStream(serial);
    ObjectInputStream ois = null;
    try {
      ois = new ObjectInputStream(bais);
      return ois.readObject();
    } catch(IOException e){
      return null;
    } catch(ClassNotFoundException e){
      return null;
    } finally {
      try {
        if(ois != null) ois.close();
      } catch(IOException e){}
    }
  }
  /**
   * Serialize an integer.
   * @param num an integer.
   * @return a byte array of the serialized integer.
   */
  public static native byte[] packint(int num);
  /**
   * Redintegrate a serialized integer.
   * @param serial a byte array of the serialized integer.
   * @return the original integer.
   */
  public static native int unpackint(byte[] serial);
  /**
   * Serialize a real number.
   * @param num a real number.
   * @return a byte array of the serialized real number.
   */
  public static native byte[] packdouble(double num);
  /**
   * Redintegrate a serialized real number.
   * @param serial a byte array of the serialized real number.
   * @return the original real number.
   */
  public static native double unpackdouble(byte[] serial);
  /**
   * Execute a shell command using the native function `system' defined in POSIX and ANSI C.
   * @param cmd a command line.
   * @return the return value of the function.  It depends on the native system.
   */
  public static native int system(String cmd);
  /**
   * Change current working directory using the native function `chdir' defined in POSIX.
   * @param path the path of a directory.
   * @return 0 on success, or -1 on failure.
   */
  public static native int chdir(String path);
  /**
   * Get current working directory using the native function `getcwd' defined in POSIX.
   * @return the path of the current working directory or null on failure.
   */
  public static native String getcwd();
  /**
   * Get process identification using the native function `getpid' defined in POSIX.
   * @return the process ID of the current process.
   */
  public static native int getpid();
  /**
   * Set an environment variable using the native function `putenv' defined in POSIX and ANSI C.
   * @param name the name of an environment variable.
   * @param value value of an environment variable.
   * @return 0 on success, or -1 on failure.
   */
  public static synchronized native int putenv(String name, String value);
  /**
   * Get an environment variable using the native function `getenv' defined in POSIX and ANSI C.
   * @param name the name of an environment variable.
   * @return the value of the variable, or null if it does not exist.
   */
  public static synchronized native String getenv(String name);
  //----------------------------------------------------------------
  // package static methods
  //----------------------------------------------------------------
  /**
   * Convert a map into a string array.
   */
  static byte[][] maptostrary(Map map){
    byte[][] ary = new byte[map.size()*2][];
    Iterator it = map.entrySet().iterator();
    int anum = 0;
    while(it.hasNext()){
      Map.Entry ent = (Map.Entry)it.next();
      Object key = ent.getKey();
      ary[anum++] = key instanceof byte[] ? (byte[])key : Util.otos(key).getBytes();
      Object val = ent.getValue();
      ary[anum++] = val instanceof byte[] ? (byte[])val : Util.otos(val).getBytes();
    }
    return ary;
  }
  //----------------------------------------------------------------
  // constructors and finalizers
  //----------------------------------------------------------------
  /**
   * Dummy constructor.
   */
  private Util() throws NoSuchMethodException {
    throw new NoSuchMethodException();
  }
}



/* END OF FILE */
