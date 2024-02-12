# OCov4J: Object Coverage Metrics for Java
**OCov4J** is a prototype tool that measures test code coverage based on the **"object code coverage or OCov concept"** . *OCov* is a set of new test coverage metrics that specifically address object-oriented programming concepts. It considers issues related to object-oriented features such as inheritance, polymorphism, and dynamic binding, in addition to traditional procedural programming concerns.

**OCov4J** instruments your Java application code by running your tests (with JUnit or other tools) and calculates object coverage metrics for your test suites.

## What is Object Coverage Criteria or OCov?
We call these new criteria **Object Coverage Criteria** or **OCov** for short. 

These coverage metrics generally work similarly to traditional test coverage, with the difference that they also consider these two fact while measuring the coverage of your tests:

 * **OCov** considers **the actual type of the object** under test (it means **which exact type of the object** executes **which part of the code**), and 
 * **OCov** measures the coverage of **the inherited code (non-private code of parent or ancestor classes)**, in addtion to the main class code.

In contrast, the **traditional code coverage** only consider the codes that are explicitly written in the class itself. It also doesn't matter what kind of object executes them!

in summary:
* **Traditional code coverage**: This method only measures the percentage of code that is executed by test cases. It does not consider the types of objects that execute the code of the class or the inherited code of the object from other super classes.
* **Object coverage**: This method measures the percentage of code that is executed by the exact type of object (actual type of object should be the same as the class under test) and also the inherited code of the class should be coveraged by tests to achinve high object coverage.

for more academic information about these new criteria and how are effective refer to this academic paper:
[M. Ghoreshi, and H. Haghighi. "Object coverage criteria for supporting object-oriented testing." Software Quality Journal (2023)](https://link.springer.com/article/10.1007/s11219-023-09643-3).

## Simple examples that make sence!
### Why does the actual type of the object matter?
The traditional code coverage criteria only consider the static space of a class and do not consider runtime objects. This can cause a test suite to achieve high code coverage for a class, while any object of the class type does not execute the code of this class, or only a small part of the class is executed. To clarify this issue, consider the following sample classes in Java, which model two types of stacks.

`Stack` class models a simple stack in Java. Objects from this class set the maximum stack length using the class constructor during instantiation. This class defines two `push` and `pop` methods to add/remove elements to/from the stack.

**Note**: We have **seeded two bugs** by commenting lines `10-11` and `15-16` of class `Stack` that can result in failures at runtime.

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
10        //if(index==size)                        // commented for bug seeding
11        // throw new Exception(“stack is full”); // commented for bug seeding
12        element[index++]=x;
13      }
14      public int pop(){
15        //if(index==0)                            // commented for bug seeding
16        // throw new Exception(“stack is empty”); // commented for bug seeding
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

Hwever, if we run a similar test using an object with **actual type of `Stack`**, we **may encounter a runtime exception** due to our seeded faults. 

For example, the following simple test `Stack_TestSuite` leads to an **`IndexOutOfBoundsException` error** in Java, indicating an attempt to access an invalid index within the element array. 
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

### Why does inherited code matter?
The classic code coverage regard the execution of each class code separately and in isolation, and do not consider the inherited parts of the parent/ancestor classes. Therefore, problems related to how the class under test interacts with the states and behaviors of the inherited classes are excluded from the scope of these criteria. 

To explain this issue more precisely, consider the following example containing two classes List and ClearableList. The former models a simple list backed by an array, and the latter models a simple list with an extra method, named clear, for deleting all elements of the list at once. ClearableList inherits the class List and adds the clear method to clear the list. **We have seeded a bug** into **ClearableList** by commenting the line number 7 of the class ClearableList.
```java
01    class List {
02      int maxSize;
03      Object[] items;
04      int lastIndex;
05      public List(size){
06        this.maxSize=size;
07        items=new Object[size];
08      }
09      public void add(Object item){
10        if(lastIndex==maxSize) 
11         throw new Exception(“List is full”); 
12        items[lastIndex++]=item;
13      }
14      public void remove(int index){
15        if(index<0 || index>=lastIndex) 
16         throw new Exception(“Index out of bounds”); 
17        for(int i=index; i<lastIndex-1; i++)
18          items[i]=items[i+1];
19        this.lastIndex--;
20      }
21      public int getSize(){
22        return lastIndex;
23      }
24    }
```

`ClearableList` class:
```java
01    class ClearableList extends List {
02      public ClearableList(int size){
03        super(size);
04      }
05      public void clear() {
06        items =new Object[maxSize];
07        //lastIndex=0; /* commented for bug seeding */
08      }
09    }
```
Now consider the below test suite `List_TestSuite1` which contains four test cases to validate the implementations of `List` and `ClearableList`:
```java
01    class List_TestSuite1 {
02      @Test
03      public void List_Test1(){
04        List list=new List(10);
05        list.add(“A”); list.add(“B”); list.remove(1);
06        assert list.getSize()==1;
07      }
08      @Test(expected = Exception.class)
09      public void List_Test2(){
10        List list=new List(1);
11        list.add(“A”);
12        list.add(“B”); //the expected exception thrown and test passes
13      }
08      @Test(expected = Exception.class)
09      public void List_Test3(){
10        List list=new List(10);
11        list.add(“A”);
12        list.remove(2); // the expected exception thrown and test passes
13      }
14      @Test
15      public void ClearableList_Test1(){
16        ClearableList list=new ClearableList(10);
17        list.clear();
18        assert list.getSize()==0;
19      }
20    }
```
Based on running Test cases `List_TestSuite1`:
* Tests `List_Test1`, `List_Test2`, and `List_Test3` pass and achieve 100% line coverage for class List.
* The `ClearableList_Test1` test, which is passed too, also provides **100% line coverage** for class **ClearableList** and **cannot reveal our seeded bug**.
* Although `ClearableList_Test1` only tests the method defined in `ClearableList` and does not test the inherited methods (like `add` or `remove`), it results in **100% line coverage**.

Nevertheless, the seeded bug in the `ClearableList` class **can easily be detected** by a simple test that uses methods inherited from the parent class. 

For example, in the following `ClearableList_Test2` is another test for the `ClearableList` class that, in addition to testing the child state space, tests the parent state space by calling the inherited method add. 

Unlike the previous test, `ClearableList_Test2` is failed and **reveals our seeded bug easily**. Using this test, after the execution of the `add` method (line 4), one unit is added to the `index` variable; but when the method `clear` is called, although it resets the parent state variable elements, it does not reset the value of the parent’s state variable index (as mentioned, line 7 of `ClearableList` was commented to create this fault). Hence, the list length in the assertion section of the test becomes equal to one, which causes the test to fail. 

```java
01      @Test
02      public void ClearableList_Test2(){
03        ClearableList list=new ClearableList(10);
04        list.add(“A”);
05        list.clear();
06        assert list.getSize()==0;
07      }
```
Regarding this example, we defining our new coverage criteria by considering the parts of the class state and behavior, which are inherited from parent or ancestor classes.

## OCov4J Usage
To use `OCov4J`, we should first use it as a `java-agent` through running test execution. In this phase, the tool attaches to your program-under-test and instruments your code. 

As an example of how to use `OCov4J`, consider your jar file containing your classes as `your-program.jar` for which you have added a `JUnit` test class called `MY_Test1` in a `my-test.java`. Now, you first attach the `OCov4J` Jar file to the Java process while executing the tests with the following command. This command executes the unit tests on class `ClearableList`:
```
java -cp your-program.jar:junit.jar:<other class-path libraries may be needed for execution of your program> 
     -javaagent:OCov4J.jar 
     org.junit.runner.JUnitCore  MY_Test1  
```
As shown in the above statements, we used the option `–javaagent` to attach the Jar file of `OCov4J` to the JVM process. The command then causes the JUnit core to run the tests specified in the test suite `ClearList_Test1`. After running these tests, `OCov4J` saves the coverage information in some Comma Separated Values (CSV) files in the current directory. These CSV files can be used for later processing in spreadsheet tools. `OCov4J` provides some commands to print coverage level values. For example, by executing the following command, the object line coverage level is shown in the terminal:
```
java -jar ../path/to/OCov4J.jar --line-coverage
```
## OCov4J Command Options
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

