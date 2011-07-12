/*
 * Copyright 2003 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.cglib.beans;

import java.util.*;

public /* need it for class loading  */ class FixedKeySet extends AbstractSet<String> {
    private Set<String> set;
    private int size;

    public FixedKeySet(String[] keys) {
        size = keys.length;
        set = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(keys)));
    }

    @Override
    public Iterator<String> iterator() {
        return set.iterator();
    }

    @Override
    public int size() {
        return size;
    }
}
