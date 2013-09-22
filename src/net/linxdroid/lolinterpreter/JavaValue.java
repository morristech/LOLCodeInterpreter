/*
 * Java LOLCODE - LOLCODE parser and interpreter (http://lolcode.com/)
 * Copyright (C) 2007-2011  Brett Kail (bkail@iastate.edu)
 * http://bkail.public.iastate.edu/lolcode/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.linxdroid.lolinterpreter;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

class JavaValue extends AbstractTypedScalarValue {
	private static final String TYPE = "JAVA";
	private static final ReferenceQueue<JavaClass> javaClassReferenceQueue = new ReferenceQueue<JavaClass>();
	private static final Map<Class, JavaClassReference> javaClassReferences = new HashMap<Class, JavaClassReference>();

	protected Object object;
	protected JavaClass javaClass;

	private synchronized static JavaClass createJavaClass(Class klass) {
		Reference ref;
		while ((ref = javaClassReferenceQueue.poll()) != null) {
			javaClassReferences.remove(((JavaClassReference)ref).getKey());
		}

		JavaClassReference javaClassRef = javaClassReferences.get(klass);

		JavaClass javaClass;
		if (javaClassRef == null) {
			javaClass = null;
		} else {
			javaClass = javaClassRef.get();
		}

		if (javaClass == null) {
			javaClass = new JavaClass(klass);
			javaClassReferences.put(klass, new JavaClassReference(javaClass, klass));
		}

		return javaClass;
	}

	public static Value create(Class klass) {
		return new JavaValue(klass, createJavaClass(klass));
	}

	private JavaValue(Object object, JavaClass javaClass) {
		this.object = object;
		this.javaClass = javaClass;
	}

	@Override
	public String toString() {
		return getClass().getName() + '@' + Integer.toHexString(hashCode()) + '[' + object + ']';
	}

	public String getType() {
		return TYPE;
	}

	public boolean getBoolean() {
		throw new LOLCodeException(LOLCodeException.BAD_JAVA_USE);
	}

	public int getInt() {
		throw new LOLCodeException(LOLCodeException.BAD_JAVA_USE);
	}

	public float getFloat() {
		throw new LOLCodeException(LOLCodeException.BAD_JAVA_USE);
	}

	public String getString() {
		return object.toString();
	}

	@Override
	public void setSlot(Value index, Value value) {
		if (!index.isYarn()) {
			throw new LOLCodeException(LOLCodeException.BAD_SLOT);
		}

		javaClass.get(index.getString()).set(object, value);
	}

	@Override
	public Value getSlot(Value index) {
		if (!index.isYarn()) {
			throw new LOLCodeException(LOLCodeException.BAD_SLOT);
		}

		return javaClass.get(index.getString()).get(object);

	}

	private static Value toValue(Object o) {
		if (o == null) {
			return NoobValue.INSTANCE;
		}

		if (o instanceof Boolean) {
			return TroofValue.getInstance(((Boolean)o).booleanValue());
		}

		if (o instanceof String) {
			return new YarnValue((String)o);
		}

		if (o instanceof Long || o instanceof Double) {
			return new YarnValue(o.toString());
		}

		if (o instanceof Float) {
			return new NumbarValue(((Float)o).floatValue());
		}

		if (o instanceof Number) {
			return new NumbrValue(((Number)o).intValue());
		}

		Class klass;
		if (o instanceof Class) {
			klass = (Class)o;
		} else {
			klass = o.getClass();
			if (klass.isArray()) {
				return new JavaArrayValue(o, createJavaClass(klass));
			}
		}

		return new JavaValue(o, createJavaClass(klass));
	}

	private static Object fromValue(Value value, Class c) {
		if (value == NoobValue.INSTANCE) {
			if (c.isPrimitive()) {
				throw new LOLCodeException(LOLCodeException.BAD_JAVA_TYPE, "NOOB not convertable to " + c);
			}

			return null;
		}

		if (value instanceof JavaValue) {
			Object object = ((JavaValue)value).object;
			if (!c.isInstance(object)) {
				throw new LOLCodeException(LOLCodeException.BAD_JAVA_TYPE, object.getClass() + " not instanceof " + c.getClass());
			}

			return object;
		}

		if (c == Boolean.TYPE || c == Boolean.class) {
			return value.getBoolean() ? Boolean.TRUE : Boolean.FALSE;
		}

		if (c == Byte.TYPE || c == Byte.class) {
			return Byte.valueOf((byte)value.getInt());
		}

		if (c == Short.TYPE || c == Short.class) {
			return Short.valueOf((short)value.getInt());
		}

		if (c == Character.TYPE || c == Character.class) {
			return Character.valueOf((char)value.getInt());
		}

		if (c == Integer.TYPE || c == Integer.class) {
			return Integer.valueOf(value.getInt());
		}

		if (c == Float.TYPE || c == Float.class) {
			return Float.valueOf(value.getFloat());
		}

		if (c == Long.TYPE || c == Long.class) {
			return Long.valueOf(value.getInt());
		}

		if (c == Double.TYPE || c == Double.class) {
			return Double.valueOf(value.getFloat());
		}

		if (c == String.class) {
			return value.getString();
		}

		if (c == Object.class) {
			if (value instanceof TroofValue) {
				return Boolean.valueOf(value.getBoolean());
			}

			if (value instanceof NumbrValue) {
				return Integer.valueOf(value.getInt());
			}

			if (value instanceof NumbarValue) {
				return Float.valueOf(value.getFloat());
			}

			if (value instanceof YarnValue) {
				return value.getString();
			}
		}

		throw new LOLCodeException(LOLCodeException.BAD_JAVA_TYPE, value.getType() + " not convertable to " + c);
	}

	private static class JavaClassReference extends SoftReference<JavaClass> {
		private Class klass;

		public JavaClassReference(JavaClass javaClass, Class klass) {
			super(javaClass, javaClassReferenceQueue);
			this.klass = klass;
		}

		public Class getKey() {
			return klass;
		}
	}

	private static class JavaClass extends HashMap<String, Slot> {
		private static final long serialVersionUID = 0;

		private static Comparator<Callable> CALLABLE_COMPARATOR = new Comparator<Callable>() {
			public int compare(Callable a, Callable b) {
				int compare = a.getName().compareTo(b.getName());
				if (compare == 0) {
					compare = a.getParameterTypes().length - b.getParameterTypes().length;
				}
				return compare;
			}
		};

		private static Comparator<Field> FIELD_COMPARATOR = new Comparator<Field>() {
			public int compare(Field a, Field b) {
				return a.getName().compareTo(b.getName());
			}
		};

		private Class klass;

		public JavaClass(Class klass) {
			this.klass = klass;
			addMethodSlots(klass);
			addFieldSlots(klass);

			if (klass.isArray()) {
				put("length", LengthSlot.INSTANCE);
				put("new", NewArraySlot.INSTANCE);
			} else if ((klass.getModifiers() & Modifier.ABSTRACT) == 0) {
				addConstructorSlot(klass);
			}
		}

		private void addMethodSlots(Class klass) {
			Method[] methods = klass.getMethods();

			Callable[] callables = new Callable[methods.length];
			for (int i = 0; i < methods.length; i++) {
				callables[i] = new MethodCallable(methods[i]);
			}

			Arrays.sort(callables, CALLABLE_COMPARATOR);

			for (int i = 0, end; i < callables.length; i = end) {
				String name = callables[i].getName();

				for (end = i + 1; end < callables.length && callables[end].getName().equals(name); end++) { }

				put(name, new CallableSlot(callables, i, end));
			}
		}

		private void addFieldSlots(Class klass) {
			Field[] fields = klass.getFields();
			Arrays.sort(fields, FIELD_COMPARATOR);

			for (Field field : fields) {
				put(field.getName(), new FieldSlot(field));
			}
		}

		private void addConstructorSlot(Class klass) {
			Constructor[] constructors = klass.getConstructors();

			Callable[] callables = new Callable[constructors.length];
			for (int i = 0; i < constructors.length; i++) {
				callables[i] = new ConstructorCallable(constructors[i]);
			}

			Arrays.sort(callables, CALLABLE_COMPARATOR);

			put("new", new CallableSlot(callables, 0, callables.length));
		}

		public Class getJavaClass() {
			return klass;
		}
	}

	private static interface Slot {
		void set(Object o, Value value);
		Value get(Object o);
	}

	private static class FieldSlot implements Slot {
		private Field field;

		public FieldSlot(Field field) {
			this.field = field;
		}

		private boolean isStatic() {
			return (field.getModifiers() & Modifier.STATIC) != 0;
		}

		public void set(Object o, Value value) {
			if (!isStatic() && o instanceof Class && o != Class.class) {
				throw new LOLCodeException(LOLCodeException.BAD_SLOT);
			}

			try {
				field.set(o, fromValue(value, field.getType()));
			} catch (IllegalAccessException ex) {
				throw new IllegalStateException(ex);
			}
		}

		public Value get(Object o) {
			if (!isStatic() && o instanceof Class && o != Class.class) {
				throw new LOLCodeException(LOLCodeException.BAD_SLOT);
			}

			try {
				return toValue(field.get(o));
			} catch (IllegalAccessException ex) {
				throw new IllegalStateException(ex);
			}
		}
	}

	private static abstract class AbstractCallableSlot extends AbstractTypedScalarValue implements Slot {
		public String getType() {
			throw new UnsupportedOperationException();
		}

		public void set(Object o, Value value) {
			throw new LOLCodeException(LOLCodeException.BAD_BUKKIT_TYPE, TYPE);
		}

		public Value get(Object o) {
			return this;
		}

		public boolean getBoolean() {
			throw new UnsupportedOperationException();
		}

		public int getInt() {
			throw new UnsupportedOperationException();
		}

		public float getFloat() {
			throw new UnsupportedOperationException();
		}

		public String getString() {
			throw new UnsupportedOperationException();
		}
	}

	private static class CallableSlot extends AbstractCallableSlot {
		private static final Object[] EMPTY_ARRAY = new Object[0];

		private Callable[] callables;
		int begin;
		int end;

		public CallableSlot(Callable[] callables, int begin, int end) {
			this.callables = callables;
			this.begin = begin;
			this.end = end;
		}

		public Value call(Value target) {
			return call(target, Collections.<Value>emptyList(), EMPTY_ARRAY);
		}

		public Value call(Value target, List<Value> arguments) {
			return call(target, arguments, new Object[arguments.size()]);
		}

		public Value call(Value target, List<Value> arguments, Object[] objects) {
			JavaValue javaValue = (JavaValue)target;
			Object o = javaValue.object;
			boolean isClass = o instanceof Class && o != Class.class;
			LOLCodeException typeError = null;
			boolean found = false;

			for (int i = begin; i < end; i++) {
				Callable callable = callables[i];
				Class[] paramTypes = callable.getParameterTypes();

				if (callable.isStatic() || !isClass) {
					found = true;

					if (paramTypes.length == objects.length) {
						try {
							for (int j = 0; j < paramTypes.length; j++) {
								objects[j] = fromValue(arguments.get(j), paramTypes[j]);
							}
						} catch (LOLCodeException ex) {
							if (typeError == null) {
								typeError = ex;
							}
							continue;
						}

						try {
							return callable.call(javaValue.javaClass, o, objects);
						} catch (IllegalAccessException ex) {
							throw new IllegalStateException(ex);
						} catch (InstantiationException ex) {
							throw new IllegalStateException(ex);
						} catch (InvocationTargetException ex) {
							throw new LOLCodeException(ex.getCause().getClass().getName(), ex);
						}
					}
				}
			}

			if (typeError != null) {
				throw new LOLCodeException(LOLCodeException.BAD_JAVA_TYPE, typeError);
			}

			throw new LOLCodeException(found ? LOLCodeException.BAD_ARGUMENT_COUNT : LOLCodeException.BAD_SLOT);
		}
	}

	private interface Callable {
		public boolean isStatic();
		public String getName();
		public Class[] getParameterTypes();
		public Value call(JavaClass javaClass, Object target, Object[] args) throws InstantiationException, IllegalAccessException, InvocationTargetException;
	}

	private static class MethodCallable implements Callable {
		private Method method;
		private Class[] paramTypes;

		public MethodCallable(Method method) {
			this.method = method;
			this.paramTypes = method.getParameterTypes();
		}

		@Override
		public String toString() {
			return super.toString() + '[' + method + ']';
		}

		public boolean isStatic() {
			return (method.getModifiers() & Modifier.STATIC) != 0;
		}

		public String getName() {
			return method.getName();
		}

		public Class[] getParameterTypes() {
			return paramTypes;
		}

		public Value call(JavaClass javaClass, Object target, Object[] args) throws IllegalAccessException, InvocationTargetException {
			return toValue(method.invoke(target, args));
		}
	}

	private static class ConstructorCallable implements Callable {
		private Constructor constructor;
		private Class[] paramTypes;

		public ConstructorCallable(Constructor constructor) {
			this.constructor = constructor;
			paramTypes = constructor.getParameterTypes();
		}

		@Override
		public String toString() {
			return super.toString() + '[' + constructor + ']';
		}

		public boolean isStatic() {
			return true;
		}

		public String getName() {
			return constructor.getName();
		}

		public Class[] getParameterTypes() {
			return paramTypes;
		}

		public Value call(JavaClass javaClass, Object target, Object[] args) throws InstantiationException, IllegalAccessException, InvocationTargetException {
			return new JavaValue(constructor.newInstance(args), javaClass);
		}
	}

	private static class NewArraySlot extends AbstractCallableSlot {
		public static final Slot INSTANCE = new NewArraySlot();

		private NewArraySlot() { }

		public Value call(Value target) {
			throw new LOLCodeException(LOLCodeException.BAD_ARGUMENT_COUNT);
		}

		public Value call(Value target, List<Value> arguments) {
			if (arguments.size() != 1) {
				throw new LOLCodeException(LOLCodeException.BAD_ARGUMENT_COUNT);
			}

			int size;
			try {
				size = arguments.get(0).getInt();
			} catch (LOLCodeException ex) {
				throw new LOLCodeException(LOLCodeException.BAD_JAVA_TYPE);
			}

			JavaClass javaClass = ((JavaValue)target).javaClass;
			Object result = Array.newInstance(javaClass.getJavaClass().getComponentType(), size);

			return new JavaArrayValue(result, javaClass);
		}
	}

	private static class LengthSlot implements Slot {
		public static final Slot INSTANCE = new LengthSlot();

		private LengthSlot() { }

		public void set(Object o, Value v) {
			throw new LOLCodeException(LOLCodeException.BAD_SLOT);
		}

		public Value get(Object o) {
			if (o instanceof Class) {
				throw new LOLCodeException(LOLCodeException.BAD_SLOT);
			}

			return new NumbrValue(Array.getLength(o));
		}
	}

	private static class JavaArrayValue extends JavaValue {
		public JavaArrayValue(Object o, JavaClass javaClass) {
			super(o, javaClass);
		}

		@Override
		public void setSlot(Value index, Value value) {
			if (!index.isNumeric()) {
				super.setSlot(index, value);
			} else {
				try {
					Array.set(object, index.getInt(), fromValue(value, javaClass.getJavaClass().getComponentType()));
				} catch (ArrayIndexOutOfBoundsException ex) {
					throw new LOLCodeException(LOLCodeException.BAD_SLOT, ex);
				}
			}
		}

		@Override
		public Value getSlot(Value index) {
			if (!index.isNumeric()) {
				return super.getSlot(index);
			}

			try {
				return toValue(Array.get(object, index.getInt()));
			} catch (ArrayIndexOutOfBoundsException ex) {
				throw new LOLCodeException(LOLCodeException.BAD_SLOT, ex);
			}
		}
	}
}
