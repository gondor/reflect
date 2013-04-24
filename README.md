# Reflect 1.0.0
====

*Fluent Reflection - Get rid of the messy boiler plate!

## Setup

Maven Dependency Setup

Current Release - 1.0.0
```xml
<dependency>
	<groupId>org.pacesys</groupId>
	<artifactId>reflect</artifactId>
	<version>1.0.0</version>
</dependency>
```

Current Snapshot (Development) - 1.0.1-SNAPSHOT
```xml
<dependency>
	<groupId>org.pacesys</groupId>
	<artifactId>reflect</artifactId>
	<version>1.0.1-SNAPSHOT</version>
</dependency>
```
## Field Recipes

**Finding All Fields**

This will return all fields from all super classes (public and private)
```java
List<Field> fields = Reflect.on(someClass).fields().all();
````

**Finding All Fields based on an Annotation**

This will find all fields that have been annotated with the specified annotation in the class hierarchy. 
```java
List<Field> fields = Reflect.on(someClass).fields().annotatedWith(annotation);
````

**Single Field**

Will find a single field based on the specified name
```java
Field f = Reflect.on(someClass).fields().named(fieldName);
````

**Matching based on a Predicate**

A predicate will allow you to control what is matched and what should be discarded during inspection.  Each field found in the hierarchy will be passed to the predicate to return either a true (matched) or false to discard.  The matched list will be returned
```java
Predicate<Field> predicate = new Predicate<Field>() {
	public boolean apply(Field field) {
	  if (field.isEnumConstant() || someOtherEval)
	  	return true;
	  return false;
	}
};

List<Field> fields = Reflect.on(someClass).fields().match(predicate);
````
**Public Only Fields**

Finds only public based fields within the class hierarchy
```java
List<Field> fields = Reflect.on(someClass).fields().publicOnly();
````
## Method Recipes

The method finder has two modes in the builder chain.  Each mode provides the same chained options.

```java
// Finds static and instance based methods
Reflect.on(someClass).methods().chain //...

// Finds ONLY static methods
Reflect.on(someClass).methods(true).chain //...
````
**Finding All Methods**

This will return all methods from all super classes (public and private)
```java
List<Method> methods = Reflect.on(someClass).methods().all();
````
**Finding Accessors and Mutators**

The following call will find all Java Bean based Accessor/Mutator methods for the full class hierarchy
```java
// Map of Name to Method
Map<String,Method> accessors = Reflect.on(someClass).methods().accessors();

// Map of Name to Method
Map<String,Method> mutators = Reflect.on(someClass).methods().mutators();
````
**Finding Methods based on an Annotation**

This will find all methods that have been annotated with the specified annotation in the current class only
```java
List<Method> methods = Reflect.on(someClass).methods().annotatedWith(annotation);
````
This will find all methods that have been annotated with the specified annotation in the class hierarchy (current class and superclasses)
```java
List<Method> methods = Reflect.on(someClass).methods().annotatedWithRecursive(annotation);
````
**Matching Methods based on a Predicate**

A predicate will allow you to control what is matched and what should be discarded during inspection.  Each method found will be passed to the predicate to return either a true (matched) or false to discard.  The matched list will be returned
```java
Predicate<Method> predicate = new Predicate<Method>() {
	public boolean apply(Method method) {
	  if (method equals something)
	  	return true;
	  return false;
	}
};

List<Method> methods = Reflect.on(someClass).methods().match(predicate);
````
**Public Only Methods**

Finds only public based methods within the class
```java
List<Method> methods = Reflect.on(someClass).methods().publicOnly();
````
**Calling a Method**

The following shows how to easily call a method and get back Generic type T as a result. 
```java
// No Arguments - returns T (in example SomeResult)
SomeResult something = Reflect.on(method).against(new Object()).call(); 

// With Arguments - returns T (in example SomeResult)
SomeResult something = Reflect.on(method).against(new Object()).call(myVarArgs);
````
## Keeping Reflect for many calls

If you have many lookups such as finding Fields, Methods and calling methods it's more efficient to hold onto Reflect while using it against the current class.

```java
Reflect r = Reflect.on(someClass);
Map<String,Method> mutators = r.methods().mutators();

if (mutators.containsKey("name"))
  Reflect.on(mutators.get("name")).against(someClassInstance).call("Jeff");

Field f = r.fields().named("myField");
// etc
````

# License

Reflect is licensed under MIT.  See License.txt for license.  Copyright 2013, Jeremy Unruh
