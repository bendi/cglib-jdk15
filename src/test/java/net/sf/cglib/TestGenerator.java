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
package net.sf.cglib;

import net.sf.cglib.core.AbstractClassGenerator;
import net.sf.cglib.core.ReflectUtils;

abstract public class TestGenerator extends AbstractClassGenerator<Integer> {
    private static int counter;

    public TestGenerator(Source source) {
        super(source);
    }

    @Override
    protected ClassLoader getDefaultClassLoader() {
        return null;
    }

    @Override
    protected Integer firstInstance(Class<Integer> type) throws Exception {
        return ReflectUtils.newInstance(type);
    }

    public Integer create() {
        return create(new Integer(counter++));
    }
}
