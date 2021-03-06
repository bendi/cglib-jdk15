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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sf.cglib.core.AbstractClassGenerator;
import net.sf.cglib.core.ClassEmitter;
import net.sf.cglib.core.CodeEmitter;
import net.sf.cglib.core.Constants;
import net.sf.cglib.core.EmitUtils;
import net.sf.cglib.core.Local;
import net.sf.cglib.core.MethodInfo;
import net.sf.cglib.core.ProcessArrayCallback;
import net.sf.cglib.core.ReflectUtils;
import net.sf.cglib.core.Signature;
import net.sf.cglib.core.TypeUtils;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Type;

abstract public class MulticastDelegate<T> implements Cloneable {
    protected Object[] targets = {};

    protected MulticastDelegate() {
    }

    @SuppressWarnings("unchecked")
	public List<T> getTargets() {
        return new ArrayList<T>(Arrays.asList((T[])targets));
    }

    abstract public MulticastDelegate<T> add(T target);

    protected MulticastDelegate<T> addHelper(T target) {
        MulticastDelegate<T> copy = newInstance();
        copy.targets = new Object[targets.length + 1];
        System.arraycopy(targets, 0, copy.targets, 0, targets.length);
        copy.targets[targets.length] = target;
        return copy;
    }

    public MulticastDelegate<T> remove(T target) {
        for (int i = targets.length - 1; i >= 0; i--) {
            if (targets[i].equals(target)) {
                MulticastDelegate<T> copy = newInstance();
                copy.targets = new Object[targets.length - 1];
                System.arraycopy(targets, 0, copy.targets, 0, i);
                System.arraycopy(targets, i + 1, copy.targets, i, targets.length - i - 1);
                return copy;
            }
        }
        return this;
    }

    abstract public MulticastDelegate<T> newInstance();

    @SuppressWarnings("unchecked")
	public static <T> MulticastDelegate<T> create(Class<T> iface) {
        Generator<T> gen = new Generator<T>();
        gen.setInterface(iface);
        return (MulticastDelegate<T>) gen.create();
    }

    public static class Generator<T> extends AbstractClassGenerator<MulticastDelegate<?>> {
        private static final Source SOURCE = new Source(MulticastDelegate.class.getName());
        private static final Type MULTICAST_DELEGATE =
          TypeUtils.parseType("net.sf.cglib.reflect.MulticastDelegate");
        private static final Signature NEW_INSTANCE =
          new Signature("newInstance", MULTICAST_DELEGATE, new Type[0]);
        private static final Signature ADD_DELEGATE =
          new Signature("add", MULTICAST_DELEGATE, new Type[]{ Constants.TYPE_OBJECT });
        private static final Signature ADD_HELPER =
          new Signature("addHelper", MULTICAST_DELEGATE, new Type[]{ Constants.TYPE_OBJECT });

        private Class<T> iface;

        public Generator() {
            super(SOURCE);
        }

        @Override
        protected ClassLoader getDefaultClassLoader() {
            return iface.getClassLoader();
        }

        public void setInterface(Class<T> iface) {
            this.iface = iface;
        }

        @Override
        protected Object createKey(boolean classOnly) {
            setNamePrefix(MulticastDelegate.class.getName());
            return iface.getName();
        }

        public void generateClass(ClassVisitor cv) {
            final MethodInfo method = ReflectUtils.getMethodInfo(ReflectUtils.findInterfaceMethod(iface));

            ClassEmitter ce = new ClassEmitter(cv);
            ce.begin_class(Constants.V1_2,
                           Constants.ACC_PUBLIC,
                           getClassName(),
                           MULTICAST_DELEGATE,
                           new Type[]{ Type.getType(iface) },
                           Constants.SOURCE_FILE);
            EmitUtils.null_constructor(ce);

            // generate proxied method
            emitProxy(ce, method);

            // newInstance
            CodeEmitter e = ce.begin_method(Constants.ACC_PUBLIC, NEW_INSTANCE, null);
            e.new_instance_this();
            e.dup();
            e.invoke_constructor_this();
            e.return_value();
            e.end_method();

            // add
            e = ce.begin_method(Constants.ACC_PUBLIC, ADD_DELEGATE, null);
            e.load_this();
            e.load_arg(0);
            e.checkcast(Type.getType(iface));
            e.invoke_virtual_this(ADD_HELPER);
            e.return_value();
            e.end_method();

            ce.end_class();
        }

        private void emitProxy(ClassEmitter ce, final MethodInfo method) {
            final CodeEmitter e = EmitUtils.begin_method(ce, method, Constants.ACC_PUBLIC);
            Type returnType = method.getSignature().getReturnType();
            final boolean returns = returnType != Type.VOID_TYPE;
            Local result = null;
            if (returns) {
                result = e.make_local(returnType);
                e.zero_or_null(returnType);
                e.store_local(result);
            }
            e.load_this();
            e.super_getfield("targets", Constants.TYPE_OBJECT_ARRAY);
            final Local result2 = result;
            EmitUtils.process_array(e, Constants.TYPE_OBJECT_ARRAY, new ProcessArrayCallback() {
                public void processElement(Type type) {
                    e.checkcast(Type.getType(iface));
                    e.load_args();
                    e.invoke(method);
                    if (returns) {
                        e.store_local(result2);
                    }
                }
            });
            if (returns) {
                e.load_local(result);
            }
            e.return_value();
            e.end_method();
        }

        @SuppressWarnings("unchecked")
		@Override
        protected MulticastDelegate<T> firstInstance(Class<MulticastDelegate<?>> type) {
            // make a new instance in case first object is used with a long list of targets
            return (MulticastDelegate<T>) ReflectUtils.newInstance(type).newInstance();
        }
    }
}
