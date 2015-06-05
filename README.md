# cofi

A pet compiler project. Originally started out of curiosity and for educational purposes. *cofi* stands for *compiler first*. As the name implies, the language is/was of secondary concern. Primary goal designing and developing the compiler itself.

## How to build/run

To build

1. `mvn install` [java-commons](https://github.com/benschulz/java-commons),
2. `mvn install` [jswizzle](https://github.com/benschulz/jswizzle),
3. `mvn compile` this project

and to finally run, call

4. `mvn -f cofic-testing/pom.xml exec:java -Dexec.mainClass="de.benshu.cofi.cofic.Testing" -Dexec.cleanupDaemonThreads=false`.

## Status

The compiler is in its alpha stage. It compiles the current "language runtime" and an interpreter runs the produced "binary" (a normalized and fully type-annotated version of the source code).

### Task List

The following things are next on the (long) to-do list. They are ordered by significance for some definition of significance.

1. **Modules.** Implement basic module support so as to allow `helloworld.cofi` to be separated into its own module.

2. **Higher kinded types.** The type system API and implementation are largely higher-kind ready. The biggest issue will likely be local type inference.

3. **Graceful error handling.** Most compile time errors lead to an exception being thrown. One exception is, for instance, provinding an illegal supertype (e.g. `FixList<E> extends List` is ill-kinded). This sort of graceful error handling should be extended to cover all compile time errors.

4. …


