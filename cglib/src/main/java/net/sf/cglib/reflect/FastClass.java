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
package net.sf.cglib.reflect;

import net.sf.cglib.core.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Type;

abstract public class FastClass<T>
{
    private Class<T> type;

    protected FastClass() {
        throw new Error("Using the FastClass empty constructor--please report to the cglib-devel mailing list");
    }

    protected FastClass(Class<T> type) {
        this.type = type;
    }

    public static <T> FastClass<T> create(Class<T> type) {

        return create(type.getClassLoader(),type);

    }
    public static <T> FastClass<T> create(ClassLoader loader, Class<T> type) {
        Generator<T> gen = new Generator<T>();
        gen.setType(type);
        gen.setClassLoader(loader);
        return gen.create();
    }

    public static class Generator<T> extends AbstractClassGenerator<FastClass<T>>
    {
        private static final Source SOURCE = new Source(FastClass.class.getName());
        private Class<T> type;

        public Generator() {
            super(SOURCE);
        }

        public void setType(Class<T> type) {
            this.type = type;
        }

        @Override
        protected Object createKey(boolean classOnly) {
            setNamePrefix(type.getName());
        	return type.getName();
        }

        @Override
        protected ClassLoader getDefaultClassLoader() {
            return type.getClassLoader();
        }

        public void generateClass(ClassVisitor v) throws Exception {
            new FastClassEmitter(v, getClassName(), type);
        }

        @Override
        protected FastClass<T> firstInstance(Class<FastClass<T>> type) {
            return (FastClass<T>) ReflectUtils.newInstance(type,
                                            new Class[]{ Class.class },
                                            new Object[]{ this.type });
        }

    }

    public Object invoke(String name, Class<?>[] parameterTypes, Object obj, Object[] args) throws InvocationTargetException {
        return invoke(getIndex(name, parameterTypes), obj, args);
    }

    public T newInstance() throws InvocationTargetException {
        return newInstance(getIndex(Constants.EMPTY_CLASS_ARRAY), null);
    }

    public T newInstance(Class<?>[] parameterTypes, Object[] args) throws InvocationTargetException {
        return newInstance(getIndex(parameterTypes), args);
    }

    public FastMethod<T> getMethod(Method method) {
        return new FastMethod<T>(this, method);
    }

    public FastConstructor<T> getConstructor(Constructor<T> constructor) {
        return new FastConstructor<T>(this, constructor);
    }

    public FastMethod<T> getMethod(String name, Class<?>[] parameterTypes) {
        try {
            return getMethod(type.getMethod(name, parameterTypes));
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        }
    }

    public FastConstructor<T> getConstructor(Class<?>[] parameterTypes) {
        try {
            return getConstructor(type.getConstructor(parameterTypes));
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        }
    }

    public String getName() {
        return type.getName();
    }

    public Class<T> getJavaClass() {
        return type;
    }

    @Override
    public String toString() {
        return type.toString();
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof FastClass)) {
            return false;
        }
        return type.equals(((FastClass<?>)o).type);
    }

    /**
     * Return the index of the matching method. The index may be used
     * later to invoke the method with less overhead. If more than one
     * method matches (i.e. they differ by return type only), one is
     * chosen arbitrarily.
     * @see #invoke(int, Object, Object[])
     * @param name the method name
     * @param parameterTypes the parameter array
     * @return the index, or <code>-1</code> if none is found.
     */
    abstract public int getIndex(String name, Class<?>[] parameterTypes);

    /**
     * Return the index of the matching constructor. The index may be used
     * later to create a new instance with less overhead.
     * @see #newInstance(int, Object[])
     * @param parameterTypes the parameter array
     * @return the constructor index, or <code>-1</code> if none is found.
     */
    abstract public int getIndex(Class<?>[] parameterTypes);

    /**
     * Invoke the method with the specified index.
     * @see #getIndex(String, Class[])
     * @param index the method index
     * @param obj the object the underlying method is invoked from
     * @param args the arguments used for the method call
     * @throws java.lang.reflect.InvocationTargetException if the underlying method throws an exception
     */
    abstract public Object invoke(int index, Object obj, Object[] args) throws InvocationTargetException;

    /**
     * Create a new instance using the specified constructor index and arguments.
     * @see #getIndex(Class[])
     * @param index the constructor index
     * @param args the arguments passed to the constructor
     * @throws java.lang.reflect.InvocationTargetException if the constructor throws an exception
     */
    abstract public T newInstance(int index, Object[] args) throws InvocationTargetException;

    abstract public int getIndex(Signature sig);

    /**
     * Returns the maximum method index for this class.
     */
    abstract public int getMaxIndex();

    protected static String getSignatureWithoutReturnType(String name, Class<?>[] parameterTypes) {
        StringBuffer sb = new StringBuffer();
        sb.append(name);
        sb.append('(');
        for (int i = 0; i < parameterTypes.length; i++) {
            sb.append(Type.getDescriptor(parameterTypes[i]));
        }
        sb.append(')');
        return sb.toString();
    }
}
