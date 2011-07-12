/*
 * Copyright 2003,2004 The Apache Software Foundation
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
package net.sf.cglib.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Chris Nokleberg
 * @version $Id: CollectionUtils.java,v 1.7 2004/06/24 21:15:21 herbyderby Exp $
 */
public class CollectionUtils {
    private CollectionUtils() { }

    public static <K,V> Map<K, List<V>> bucket(Collection<V> c, Transformer<V, K> t) {
        Map<K, List<V>> buckets = new HashMap<K, List<V>>();
        for (Iterator<V> it = c.iterator(); it.hasNext();) {
            V value = it.next();
            K key = t.transform(value);
            List<V> bucket = buckets.get(key);
            if (bucket == null) {
                buckets.put(key, bucket = new LinkedList<V>());
            }
            bucket.add(value);
        }
        return buckets;
    }

    public static <K, V> void reverse(Map<K, V> source, Map<V, K> target) {
        for (Iterator<K> it = source.keySet().iterator(); it.hasNext();) {
            K key = it.next();
            target.put(source.get(key), key);
        }
    }

    public static <T> Collection<T> filter(Collection<T> c, Predicate<? super T> p) {
        Iterator<T> it = c.iterator();
        while (it.hasNext()) {
            if (!p.evaluate(it.next())) {
                it.remove();
            }
        }
        return c;
    }

    public static <F, T> List<T> transform(Collection<F> c, Transformer<? super F, ? extends T> t) {
        List<T> result = new ArrayList<T>(c.size());
        for (Iterator<F> it = c.iterator(); it.hasNext();) {
            result.add(t.transform(it.next()));
        }
        return result;
    }

    public static <T> Map<T, Integer> getIndexMap(List<T> list) {
        Map<T, Integer> indexes = new HashMap<T, Integer>();
        int index = 0;
        for(T item : list) {
            indexes.put(item, new Integer(index++));
        }
        return indexes;
    }
}

