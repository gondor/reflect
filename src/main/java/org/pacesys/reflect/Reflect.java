package org.pacesys.reflect;

import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.ReflectionException;

import org.pacesys.reflect.types.Predicate;
import org.pacesys.reflect.types.Predicates;
import org.pacesys.reflect.types.Transient;

/**
 * Provides Fluent Reflection functionality to aid in common boiler plate reflection tasks.
 * 
 * @author Jeremy Unruh
 */
public final class Reflect {

	private static final String PREFIX_SET = "set";
	private static final String PREFIX_GET = "get";
	private static final String PREFIX_IS = "is";
	
	public enum AccessorMutatorType { ACCESSOR, MUTATOR, NA }
	
	final Class<?> type;
	private Reflect(Class<?> type) {
		this.type = type;
	}

	/**
	 * Sets up Reflection against the given Class
	 * @param type the class to reflect on
	 * @return Reflect
	 */
	public static Reflect on(Class<?> type) {
		return new Reflect(type);
	}
	
	/**
	 * Reflects against a Method.  This call will filter out
	 * class level reflection options and only return valid Method based calls
	 * @param m the Method to reflect on
	 * @return ReflectMethodInvoker
	 */
	public static ReflectMethodInvoker on(Method m) {
		 return new ReflectMethodInvoker(m);
	}
	
	/**
	 * Reflection options against Class fields
	 * @return FieldFinder
	 */
	public FieldFinder fields() {
		return new FieldFinder();
	}
	
	/**
	 * Reflection options against methods
	 * @param isStaticOnly true if we should only deal with static declared methods
	 * @return MethodFinder
	 */
	public MethodFinder methods(boolean isStaticOnly) {
		return new MethodFinder(isStaticOnly);
	}
	
	/**
	 * Reflection options against Methods
	 * @return MethodFinder
	 */
	public MethodFinder methods() {
		return new MethodFinder(Boolean.FALSE);
	}

	public static class ReflectMethodInvoker {
		Method m;
		Object instance;
		ReflectMethodInvoker(Method m) { this.m = m; }
		
		/**
		 * Will invoke the call against an instance.  If this method is not called
		 * then invocations will be against static declared methods only
		 * @param instance the instance to invoke against
		 * @return ReflectMethodInvoker
		 */
		public ReflectMethodInvoker against(Object instance) {
			this.instance = instance;
			return this;
		}
		
		/**
		 * Calls the current Method with No or optional arguments (only if the method accepts arguments). 
		 * @param args the arguments needed for this method call or null if the method takes no arguments
		 * @return ReflectMethodInvoker
		 * @throws ReflectionException
		 */
		@SuppressWarnings("unchecked")
		public <T> T call(Object... args) throws ReflectionException {
			 try {
					return (T) m.invoke(instance, args);
				} catch (Exception e) {
					throw new ReflectionException(e);
				}
		}
	}
	
	public class FieldFinder extends MemberFinder<Field> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public List<Field> match(Predicate<Field> predicate) {
			return Reflect.fieldsFor(type, predicate);
		}
		
		/**
		 * Finds the first field that matches the specified field name (case-insensitive)
		 * @param fieldName the field to find by name
		 * @return the Field or null
		 */
		public Field named(String fieldName) {
			List<Field> fields =  Reflect.fieldsFor(type, Predicates.fieldName(fieldName));
			if (fields != null && !fields.isEmpty())
				return fields.get(0);
			
			return null;
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public List<Field> annotatedWith(Class<? extends Annotation>... annotation) {
			return Reflect.fieldsFor(type, Predicates.<Field>findByAnnotations(annotation));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public List<Field> annotatedWith(Set<Class<? extends Annotation>> annotations) {
			return Reflect.fieldsFor(type, Predicates.<Field>findByAnnotations(annotations));
		}
	}
	
	public class MethodFinder extends MemberFinder<Method> {
		boolean isStaticOnly;
		MethodFinder(boolean isStaticOnly) { this.isStaticOnly = isStaticOnly; }
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public List<Method> match(Predicate<Method> predicate) {
			return Reflect.methodsFor(type, isStaticOnly, predicate);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public List<Method> annotatedWith(Class<? extends Annotation>... annotation) {
			return Reflect.methodsFor(type, isStaticOnly, Predicates.<Method>findByAnnotations(annotation));
		}
		
		public List<Method> annotatedWithRecursive(Class<? extends Annotation>... annotation) {
			return Reflect.methodsForRecursive(type, isStaticOnly, Predicates.<Method>findByAnnotations(annotation));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public List<Method> annotatedWith(Set<Class<? extends Annotation>> annotations) {
			return Reflect.methodsFor(type, isStaticOnly, Predicates.<Method>findByAnnotations(annotations));
		}
		
		/**
		 * Finds all Accessors (includes accessors from Super Classes)
		 * @return Map of Accessor Name to Method
		 */
		public Map<String,Method> accessors() {
			Map<String,Method> results =  new LinkedHashMap<String, Method>();
			Reflect.acessorsFor(type, results);
			return results;
		}

		/**
		 * Finds all Mutators (includes mutators from Super Classes)
		 * @return Map of Mutator Name to Method
		 */
		public Map<String,Method> mutators() {
			Map<String,Method> results =  new LinkedHashMap<String, Method>();
			Reflect.mutatorsFor(type, results);
			return results;
		}
	}
	
	abstract class MemberFinder<T extends AccessibleObject> {
		/**
		 * Will find the given members using the specified predicate (includes super classes)
		 * @param predicate the predicate used for filtering out the matches
		 * @return List of Members or empty
		 */
		public abstract List<T> match(Predicate<T> predicate);
		
		/**
		 * Finds all members marked with the specified Annotation (includes super classes)
		 * @param annotation the annotation used to find members
		 * @return List of Members or empty
		 */
		public abstract List<T> annotatedWith(Class<? extends Annotation>... annotation);
		
		/**
		 * Finds all members marked with the specified Annotation(s) (includes super classes)
		 * @param annotations the annotations used to find members
		 * @return List of Members or empty
		 */
		public abstract List<T> annotatedWith(Set<Class<? extends Annotation>> annotations);
		
		/**
		 * Finds all members without any matching/filtering (includes super classes)
		 * @return List of Members
		 */
		public List<T> all() { return match(null);}
		
		/**
		 * Finds all Public declared members (includes super classes)
		 * @return List of Members
		 */
		public List<T> publicOnly() { return match(Predicates.<T>publicAccess()); }
	}
	
	/**
	 * Finds methods in the given Class and will call the optional predicate for inclusion
	 * @param type the Class to find static methods for
	 * @param isStaticOnly if true only static matching methods are returned
	 * @param predicate the Predicate to filter results (optional)
	 * @return List of Methods which passed or Empty
	 */
	private static List<Method> methodsFor(Class<?> type, boolean isStaticOnly, Predicate<Method> predicate) {
		List<Method> methods = new ArrayList<Method>();
		for (Method m : type.getDeclaredMethods()) {
			if (!isStaticOnly || Modifier.isStatic(m.getModifiers()))
			{
				if (predicate == null || predicate.apply(m))
				{
					if (!m.isAccessible())
						m.setAccessible(true);
					methods.add(m);
				}
			}
		}
		return methods;
	}
	
	/**
	 * Finds methods in the given Class and will call the optional predicate for inclusion
	 * @param type the Class to find static methods for
	 * @param isStaticOnly if true only static matching methods are returned
	 * @param predicate the Predicate to filter results (optional)
	 * @return List of Methods which passed or Empty
	 */
	private static List<Method> methodsForRecursive(Class<?> type, boolean isStaticOnly, Predicate<Method> predicate) {
		List<Method> methods = new ArrayList<Method>();
		Class<?> t = type;
    while (t != null && t != Object.class) 
    {
			for (Method m : t.getDeclaredMethods()) {
				if (!isStaticOnly || Modifier.isStatic(m.getModifiers()))
				{
					if (predicate == null || predicate.apply(m))
					{
						if (!m.isAccessible())
							m.setAccessible(true);
						methods.add(m);
					}
				}
			}
			t = t.getSuperclass();
    }
		return methods;
	}
	
	 public static AccessorMutatorType isMethodMutatorOrAccessor(Method m) {
	  	if (m == null) return AccessorMutatorType.NA;
	  	
	  	if (m.getName().startsWith(PREFIX_GET) || m.getName().startsWith(PREFIX_IS))
	  		return AccessorMutatorType.ACCESSOR;
	  	else if (m.getName().startsWith(PREFIX_SET))
	  		return AccessorMutatorType.MUTATOR;
	  	
	  	return AccessorMutatorType.NA;
	 }
	 
	 /**
	  * Formats the method name for a method if it is a Mutator or Accessor into its potential field name.  For example:
	  * isTest becomes test, setName becomes name.  fetchName returns fetchName since it isn't the standard specification.
	  * @param m the method name to format
	  * @return the Method name formatted if the spec was matched or the original name
	  */
	 public static String formatMutatorAccessor(Method m) {
		 if (m == null) return null;
		 
		 if (m.getName().startsWith(PREFIX_GET) || m.getName().startsWith(PREFIX_SET)) {
			 return Introspector.decapitalize(m.getName().substring(3));
		 }
		 else if (m.getName().startsWith(PREFIX_IS)) 
			 return Introspector.decapitalize(m.getName().substring(2));
		 
		 return m.getName();
	 }
	 
	/**
	 * Finds fields from the given class and all super classes
	 *
	 * @param type the top level class to find fields for
	 * @param predicate optional predicate to filter the fields wanted
	 * @return Linked Map containing Field Name to Field
	 */
	private static List<Field> fieldsFor(Class<?> type, Predicate<Field> predicate) {
		List<Field> fields = new ArrayList<Field>();
		Class<?> t = type;
    while (t != null && t != Object.class) 
    {
    	  for (Field f : t.getDeclaredFields()) {
    	  	if (predicate == null || predicate.apply(f))
    	  	{
	    	  	if (!f.isAccessible())
	    	  		f.setAccessible(true);
	    	  	fields.add(f);
    	  	}
    	  }
        t = t.getSuperclass();
    }
    return fields;
	}
	
	/**
   * Builds accessors for type hierarchy up to Object.class
   */
  private static void acessorsFor(Class<?> type, Map<String, Method> accessors) {
    Class<?> superClass = type.getSuperclass();
    if (superClass != Object.class)
      acessorsFor(superClass, accessors);
    for (Method method : type.getDeclaredMethods()) {
      if (method.getParameterTypes().length == 0 && !method.isAnnotationPresent(Transient.class)) {
        String methodName = method.getName();
        if (methodName.startsWith(PREFIX_GET))
          accessors.put(Introspector.decapitalize(methodName.substring(3)), method);
        else if (methodName.startsWith(PREFIX_IS))
          accessors.put(Introspector.decapitalize(methodName.substring(2)), method);
      }
    }
  }

  /**
   * Builds mutators for type hierarchy up to Object.class
   */
  private static void mutatorsFor(Class<?> type, Map<String, Method> mutators) {
    Class<?> superClass = type.getSuperclass();
    if (superClass != Object.class)
      mutatorsFor(superClass, mutators);
    for (Method method : type.getDeclaredMethods()) {
      if (method.getParameterTypes().length == 1) {
        String methodName = method.getName();
        if (methodName.startsWith(PREFIX_SET))
          mutators.put(Introspector.decapitalize(methodName.substring(3)), method);
      }
    }
  }
}
