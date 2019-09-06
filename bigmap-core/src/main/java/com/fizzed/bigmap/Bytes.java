/*
 * Copyright 2019 Fizzed, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fizzed.bigmap;

public class Bytes {
 
    static public boolean startsWith(
            byte[] bytes,
            byte[] prefix) {
        
        return startsWith(bytes, prefix, null);
    }
    
    /**
     * Verifies if the bytes start with the provided prefix. Optionally, a
     * wildcard byte can be provided, where if the prefix contains that byte
     * that position will be considered a match.
     * @param bytes
     * @param prefix
     * @param wildcardMarker
     * @return 
     */
    static public boolean startsWith(
            byte[] bytes,
            byte[] prefix,
            Byte wildcardMarker) {
        
        if (bytes == null && prefix == null) {
            return true;
        }
        if (bytes == null || prefix == null) {
            return false;
        }
        if (prefix.length > bytes.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (bytes[i] != prefix[i]
                    && (wildcardMarker == null || prefix[i] != wildcardMarker)) {
                return false;
            }
        }
        return true;
    }
    
    static public boolean endsWith(
            byte[] bytes,
            byte[] suffix) {
        
        if (bytes == null && suffix == null) {
            return true;
        }
        if (bytes == null || suffix == null) {
            return false;
        }
        if (suffix.length > bytes.length) {
            return false;
        }
        int offset = bytes.length - suffix.length;
        for (int i = 0; i < suffix.length; i++) {
            if (bytes[offset+i] != suffix[i]) {
                return false;
            }
        }
        return true;
    }
    
    static public boolean endsWith(
            byte[] bytes,
            byte[] suffix,
            Byte wildcardMarker) {
        
        if (bytes == null && suffix == null) {
            return true;
        }
        if (bytes == null || suffix == null) {
            return false;
        }
        if (suffix.length > bytes.length) {
            return false;
        }
        int offset = bytes.length - suffix.length;
        for (int i = 0; i < suffix.length; i++) {
            if (bytes[offset+i] != suffix[i]
                    && (wildcardMarker == null || suffix[i] != wildcardMarker)) {
                return false;
            }
        }
        return true;
    }
    
}