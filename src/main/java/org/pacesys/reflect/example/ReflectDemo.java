package org.pacesys.reflect.example;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import org.pacesys.reflect.Reflect;
import org.pacesys.reflect.Reflect.MethodType;

/**
 * @author Vishal.Zanzrukia
 *
 */
public class ReflectDemo {
	
	/**
	 * return all the methods
	 * 
	 * @param clazz
	 * @return
	 */
	public static List<Method> getAllMethods(Class<?> clazz){
		return Reflect.on(clazz).methods().all();
	}
	
	/**
	 * return only public methods
	 * 
	 * @param clazz
	 * @return
	 */
	public static List<Method> getOnlyPublicMethods(Class<?> clazz){
		return Reflect.on(clazz).methods().publicOnly();
	}
	
	/**
	 * return only static methods
	 * 
	 * @param clazz
	 * @return
	 */
	public static List<Method> getOnlyPublicStaticMethods(Class<?> clazz){
		return Reflect.on(clazz).methods(MethodType.STATIC).publicOnly();
	}
	
	/**
	 * return only non static methods
	 * 
	 * @param clazz
	 * @return
	 */
	public static List<Method> getOnlyPublicInstanceMethods(Class<?> clazz){
		return Reflect.on(clazz).methods(MethodType.INSTANCE).publicOnly();
	}
	
	/**
	 * return only non static methods
	 * 
	 * @param clazz
	 * @return
	 */
	public static List<Field> getFileds(Class<?> clazz){
		return Reflect.on(clazz).fields().all();
	}
	
	
	public static void main(String[] args) {
		List<Method> methods = ReflectDemo.getOnlyPublicStaticMethods(TestSubClass.class);
		System.out.println("-----------public static methods only---------");
		for(Method m : methods){
			System.out.println(m.getName());
		}
		System.out.println("\n\n\n");
		System.out.println("-----------public static methods only---------");
		
		
		List<Field> fields = getFileds(TestSubClass.class);
		for(Field f : fields){
			System.out.println(f);
		}
	}
	
}


class TestSubperClass{
	
	public int aa;
	
	public static int aab;
	
	public static void publicStaticMethod(){
		
	}
	
	static void privateStaticMethod(){
		
	}
	
	void instaceMethod(){
		
	}
	
	void extraMethod(){
		
	}
}


class TestSubClass extends TestSubperClass{
	
	public int aa;
	
	public static void publicStaticMethod(){
		
	}
	
	void instaceMethod(){
		
	}
	
}