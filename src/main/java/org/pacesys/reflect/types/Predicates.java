package org.pacesys.reflect.types;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Common Predicates used for locating members by Field, Annotations, Public Access
 * 
 * @author Jeremy Unruh
 * @revision Vishal Zanzrukia 
 */
public class Predicates {

	/**
	 * Finds members who are decorated with the specified Annotation(s)
	 *
	 * @param <T> Member Type
	 * @param annotations the annotations to look for
	 * @return Predicate
	 */
	@SafeVarargs
	public static <T extends AccessibleObject> Predicate<T> findByAnnotations(Class<? extends Annotation>... annotations) {
		return new AnnotatedPredicate<T>(annotations);
	}

	/**
	 * Finds members who are decorated with the specified Annotation(s)
	 *
	 * @param <T> Member Type
	 * @param annotations the annotations to look for
	 * @return Predicate
	 */
	public static <T extends AccessibleObject> Predicate<T> findByAnnotations(Set<Class<? extends Annotation>> annotations) {
		return new AnnotatedPredicate<T>(annotations);
	}
	
	/**
	 * Finds members who are Publicly Accessible
	 *
	 * @param <T> Member Type
	 * @return Predicate
	 */
	public static <T extends Member> Predicate<T> publicAccess() {
		return new PublicAccessPredicate<T>();
	}
	
	/**
	 * Find fields by Name
	 *
	 * @param search the field name to search for
	 * @return Predicate
	 */
	public static Predicate<Field> fieldName(String search) {
		return new MemberByNamePredicate<Field>(search);
	}
	
	/**
	 * Find Methods by Name
	 *
	 * @param search the method name to search for
	 * @return Predicate
	 */
	public static Predicate<Method> methodName(String search) {
		return new MemberByNamePredicate<Method>(search);
	}

	static class MemberByNamePredicate<T extends Member> implements Predicate<T> {

		String search;

		MemberByNamePredicate(String search) {  this.search = search; }

		@Override
		public boolean apply(T input) {
			return input.getName().equalsIgnoreCase(search);
		}

	}

	static class PublicAccessPredicate<T extends Member> implements Predicate<T> {
		@Override
		public boolean apply(T input) {
			return Modifier.isPublic(input.getModifiers());
		}
	}


	static class AnnotatedPredicate<T extends AccessibleObject> implements Predicate<T> {
		
		Set<Class<? extends Annotation>> annotations;
		
		@SafeVarargs
		AnnotatedPredicate(Class<? extends Annotation>... annotations) {
			this.annotations = new HashSet<Class<? extends Annotation>>(Arrays.asList(annotations));
		}

		AnnotatedPredicate(Set<Class<? extends Annotation>> annotations) {
			this.annotations = annotations;
		}

		@Override
		public boolean apply(T input) {
			for (Annotation a : input.getAnnotations()) {
				if (annotations.contains(a.annotationType()))
					return true;
			}
			return false;
		}
	}
}
