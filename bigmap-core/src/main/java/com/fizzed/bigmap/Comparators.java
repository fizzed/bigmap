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

import java.util.Comparator;

public class Comparators {
 
    static public <T> Comparator<T> autoComparator(Class<T> type) {
        if (Comparable.class.isAssignableFrom(type)) {
            return (T o1, T o2) -> {
                Comparable c1 = (Comparable)o1;
                Comparable c2 = (Comparable)o2;
                if (c1 == null && c2 != null) {
                    return 1;
                }
                else if (c2 == null && c1 != null) {
                    return -1;
                }
                else {
                    return c1.compareTo(c2);
                }
            };
        }
        return null;
//        throw new IllegalArgumentException("Only classes that implement Comparable are valid. "
//            + "Either supply a custom comparator or implement Comparable on your class!");
    }
    
}