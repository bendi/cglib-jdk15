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

import java.beans.PropertyDescriptor;
import java.util.*;
import net.sf.cglib.core.*;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Type;

/**
 * @author Juozas Baliuka, Chris Nokleberg
 */
public class BeanGenerator<T> extends AbstractClassGenerator<T>
{
    private static final Source SOURCE = new Source(BeanGenerator.class.getName());
    private static final BeanGeneratorKey KEY_FACTORY = KeyFactory.create(BeanGeneratorKey.class);

    interface BeanGeneratorKey {
        public Object newInstance(String superclass, Map<String, Type> props);
    }

    private Class<T> superclass;
    private Map<String, Type> props = new HashMap<String, Type>();

    public BeanGenerator() {
        super(SOURCE);
    }

    /**
     * Set the class which the generated class will extend. The class
     * must not be declared as final, and must have a non-private
     * no-argument constructor.
     * @param superclass class to extend, or null to extend Object
     */
    public void setSuperclass(Class<T> superclass) {
        if (superclass != null && superclass.equals(Object.class)) {
            superclass = null;
        }
        this.superclass = superclass;
    }

    public void addProperty(String name, Class<?> type) {
        if (props.containsKey(name)) {
            throw new IllegalArgumentException("Duplicate property name \"" + name + "\"");
        }
        props.put(name, Type.getType(type));
    }

    @Override
    protected ClassLoader getDefaultClassLoader() {
        if (superclass != null) {
            return superclass.getClassLoader();
        } else {
            return null;
        }
    }

    @Override
    protected Object createKey(boolean classOnly) {
        if (superclass != null) {
            setNamePrefix(superclass.getName());
        }
        String superName = (superclass != null) ? superclass.getName() : "java.lang.Object";
        return KEY_FACTORY.newInstance(superName, props);
    }

    public void generateClass(ClassVisitor v) throws Exception {
        int size = props.size();
        String[] names = (String[])props.keySet().toArray(new String[size]);
        Type[] types = new Type[size];
        for (int i = 0; i < size; i++) {
            types[i] = (Type)props.get(names[i]);
        }
        ClassEmitter ce = new ClassEmitter(v);
        ce.begin_class(Constants.V1_2,
                       Constants.ACC_PUBLIC,
                       getClassName(),
                       superclass != null ? Type.getType(superclass) : Constants.TYPE_OBJECT,
                       null,
                       null);
        EmitUtils.null_constructor(ce);
        EmitUtils.add_properties(ce, names, types);
        ce.end_class();
    }

    @Override
    protected T firstInstance(Class<T> type) {
        return ReflectUtils.newInstance(type);
    }

    public static <T> void addProperties(BeanGenerator<T> gen, Map<String, Class<?>> props) {
        for (Iterator<String> it = props.keySet().iterator(); it.hasNext();) {
            String name = (String)it.next();
            gen.addProperty(name, props.get(name));
        }
    }

    public static <T> void addProperties(BeanGenerator<T> gen, Class<?> type) {
        addProperties(gen, ReflectUtils.getBeanProperties(type));
    }

    public static <T> void addProperties(BeanGenerator<T> gen, PropertyDescriptor[] descriptors) {
        for (int i = 0; i < descriptors.length; i++) {
            gen.addProperty(descriptors[i].getName(), descriptors[i].getPropertyType());
        }
    }
}
