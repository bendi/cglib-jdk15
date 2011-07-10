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
package net.sf.cglib.reflect;

import java.lang.reflect.*;
import net.sf.cglib.core.*;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Type;

/**
 * @author Chris Nokleberg
 * @version $Id: ConstructorDelegate.java,v 1.20 2006/03/05 02:43:19 herbyderby Exp $
 */
abstract public class ConstructorDelegate {
    private static final ConstructorKey KEY_FACTORY = KeyFactory.create(ConstructorKey.class, KeyFactory.CLASS_BY_NAME);

    interface ConstructorKey {
        public Object newInstance(String declaring, String iface);
    }

    protected ConstructorDelegate() {
    }

    public static <I,C> ConstructorDelegate create(Class<C> targetClass, Class<I> iface) {
        Generator<I,C> gen = new Generator<I,C>();
        gen.setTargetClass(targetClass);
        gen.setInterface(iface);
        return gen.create();
    }

    public static class Generator<I,C> extends AbstractClassGenerator<ConstructorDelegate> {
        private static final Source SOURCE = new Source(ConstructorDelegate.class.getName());
        private static final Type CONSTRUCTOR_DELEGATE =
          TypeUtils.parseType("net.sf.cglib.reflect.ConstructorDelegate");

        private Class<I> iface;
        private Class<C> targetClass;

        public Generator() {
            super(SOURCE);
        }

        public void setInterface(Class<I> iface) {
            this.iface = iface;
        }

        public void setTargetClass(Class<C> targetClass) {
            this.targetClass = targetClass;
            setNamePrefix(targetClass.getName());
        }

        @Override
        protected Object createKey(boolean classOnly) {
        	return KEY_FACTORY.newInstance(iface.getName(), targetClass.getName());
        }

        protected ClassLoader getDefaultClassLoader() {
            return targetClass.getClassLoader();
        }

        public void generateClass(ClassVisitor v) {
            setNamePrefix(targetClass.getName());

            final Method newInstance = ReflectUtils.findNewInstance(iface);
            if (!newInstance.getReturnType().isAssignableFrom(targetClass)) {
                throw new IllegalArgumentException("incompatible return type");
            }
            final Constructor<?> constructor;
            try {
                constructor = targetClass.getDeclaredConstructor(newInstance.getParameterTypes());
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("interface does not match any known constructor");
            }

            ClassEmitter ce = new ClassEmitter(v);
            ce.begin_class(Constants.V1_2,
                           Constants.ACC_PUBLIC,
                           getClassName(),
                           CONSTRUCTOR_DELEGATE,
                           new Type[]{ Type.getType(iface) },
                           Constants.SOURCE_FILE);
            Type declaring = Type.getType(constructor.getDeclaringClass());
            EmitUtils.null_constructor(ce);
            CodeEmitter e = ce.begin_method(Constants.ACC_PUBLIC,
                                            ReflectUtils.getSignature(newInstance),
                                            ReflectUtils.getExceptionTypes(newInstance));
            e.new_instance(declaring);
            e.dup();
            e.load_args();
            e.invoke_constructor(declaring, ReflectUtils.getSignature(constructor));
            e.return_value();
            e.end_method();
            ce.end_class();
        }

        @Override
        protected ConstructorDelegate firstInstance(Class<ConstructorDelegate> type) {
            return ReflectUtils.newInstance(type);
        }

    }
}
