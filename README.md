# OCov4J: Object Coverage Metrics for Java
**OCov4J** is a prototype tool that measures test code coverage based on the **object code coverage or OCov** concept. *OCov* is a set of new test coverage metrics that specifically address object-oriented programming concepts. It considers issues related to object-oriented features such as inheritance, polymorphism, and dynamic binding, in addition to traditional procedural programming concerns.

**OCov4J** instruments your Java application code by running your tests (with JUnit or other tools) and calculates object coverage metrics for your test suites.

## What is Object Coverage Criteria or OCov?
We call these new criteria **Object Coverage Criteria** or **OCov** for short. 

These coverage metrics generally work similarly to traditional test coverage, with the difference that they also consider these two fact while measuring the coverage of your tests:

 * **OCov** considers **the actual type of the object** under test (it means **which exact type of the object** executes **which part of the code**), and 
 * **OCov** measures the coverage of **the inherited code (non-private code of parent or ancestor classes)**, in addtion to the main class code.

In fact, in the **traditional code coverage** method, only consider the codes that are explicitly written in the class itself. It also doesn't matter what kind of object executes them!

in summary:
* **Traditional code coverage**: This method only measures the percentage of code that is executed by test cases. It does not consider the types of objects that execute the code or the inherited code of the object from super classes.
* **Object coverage**: This method measures the percentage of code that is executed by the exact type of object (actual type of object should be the same as the class under test) and also the inherited code of the class should be coveraged by tests to achinve high object coverage.

for more academic information about these new criteria and how are effective refer to this academic paper:
[M. Ghoreshi, and H. Haghighi. "Object coverage criteria for supporting object-oriented testing." Software Quality Journal (2023)](https://link.springer.com/article/10.1007/s11219-023-09643-3).

## Simple examples that make sence!
### Why does the actual type of the object matter?
The traditional code coverage criteria only consider the static space of a class and do not consider runtime objects. This can cause a test suite to achieve high code coverage for a class, while any object of the class type does not execute the code of this class, or only a small part of the class is executed. To clarify this issue, consider the following sample classes in Java, which model two types of stacks.


`Stack` class models a simple stack in Java. Objects from this class set the maximum stack length using the class constructor during instantiation. This class defines two `push` and `pop` methods to add/remove elements to/from the stack.
**Note: We have seeded two faults by commenting lines `10-11` and `15-16` of class `Stack` that can result in failures at runtime.**

```java
01    class Stack {
02      int[] element;
03      int size;
04      int index;
05      public Stack(int size){ 
06        this.size=size;
07        element=new int[size];
08      }
09      public void push(int x){ 
10        //if(index==size) 
11        // throw new Exception(“stack is full”); /* commented for bug seeding */
12        element[index++]=x;
13      }
14      public int pop(){
15        //if(index==0)
16        // throw new Exception(“stack is empty”); /* commented for bug seeding */
17        return element[--index];
18    }
```

`CircularStack` inherites `Stack` to model a simple LIFO fixed length buffer. When the element array of the buffer is full, new elements are placed at the beginning of this array; and when the index of the current value of the buffer is zero, it jumps to the end of the element array:
```java
01    class CircularStack extends Stack {
02      public CircularStack(int size){ 
03        super(size); 
04      }
05      @Overide
06      public void push(int x){
07        if(index==size)
08          index=0;
09        super.push(x);
10      }
11      @Overide
12      public void pop(){
13        if(index==0)
14          index=size;
15        return super.pop();
16      }
17    }
```
Now, consider the below JUnit `CircularStack_TestSuite` which only contains one test case for `CircularStack`:
```java
01    public class CircularStack_TestSuite {
02      @Test
03      public void CircularStack_Test1(){
04        CircularStack cs=new CircularStack(2);
05        cs.push(1); cs.push(2); cs.push(3);
06        assert cs.pop()==3;
07      }
08    }
```
 * The above test passes and reveals no bug in the stack implementation!
 *  This test case results in **100% code coverage** for **class `Stack`**!
 *  This means that although **class Stack** has not been tested by **actual Stack objects** at all, its test coverage level is 100%!!!

Hwever, if we run a similar test using an object instantiating the `Stack` class, we may encounter a runtime exception due to our seeded faults. 

For example, the following simple test `Stack_TestSuite` leads to an `IndexOutOfBoundsException` error in Java, indicating an attempt to access an invalid index within the element array. 
```java
01    public class Stack_TestSuite {
02      @Test
03      public void Stack_Test1(){
04        Stack s=new Stack(2);
05        s.push(1); s.push(2);
06        s.push(3); // it throws an IndexOutOfBoundsException
07        assert s.pop()==3;
08      }
09    }
```
As shown in this example, the traditional coverage metric incorrectly assumes coverage of a class while it has not directly been tested. This condition can mislead programmers and cause them not to write separate unit tests for the `Stack` class. In the **OCov Approach**, we consider the type of the executor object in order to address this issue.

## Uses example
To use `OCov4J`, we should first use it as an `java-agent` thorogh running test execution. In this phase, the tool atache to your program-under-test and instruments your code. As an example of how to use `OCov4J`, consider your jar file contining your classes as  `your-program.jar` for which you have add a `JUnit` test calss called `MY_Test1 ` in a `my-test.java`. Now, you first attach the `OCov4J` Jar file to the Java process while executing the tests with the following command. This command executes the unit tests on class ClearableList:
```
java -cp your-program.jar:junit.jar:<other class-path libraries may be needed for execution of your program> 
     -javaagent:OCov4J.jar 
     org.junit.runner.JUnitCore  MY_Test1  
```
As shown in the above statements, We used option –javaagent, to attach the Jar file of `OCov4J` to the JVM process. The command then causes the JUnit core to run the tests specified in test suite ClearList_Test1. After running these tests, `OCov4J` saves the coverage information in some Comma Separated Values (CSV) files in the current directory. These CSV files can be used for later processing in spreadsheets tools. `OCov4J` provides some commands to view coverage level values. For example, by executing the following command, the object line coverage level is shown on terminal:
```
java -jar ../path/to/OCov4J.jar --line-coverage
```
## Command and Parameters
After running your tests, for measuring the object coverage level you should use the `OCov4J` jar command as follwo:
``` 
   java -jar ocov4j.jar 
                       [-dp] 
                       [-a=<cover items data file>] 
                       [-c=<covered items data file>] 
                       [--poly-config-file=<config file for calculate poly coverage>]  
                       [Tests...]   Only calculate coverage for these classes
```
Options are:
```
  -a, --all-file=<cover items data file>
                   The address of data file contains all cover items information
                   
  -c, --cover-file=<covered items data file>
                   The address of data file contains all covered items information
                 
  -d, --details    Show details of class names and numbers related to coverage calculation
  
  -p, --poly       calculate poly coverage from a config file in the below option
  
  --poly-config-file=<config file for calculate poly coverage>
                   address of config file for calculate poly coverage
```

