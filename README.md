# OCov4J: Object Coverage Metrics for Java
A tool for measuring test code coverage based on "object code coverage" concept which is more consistent with object-oriented related issues than classic code coverage. `OCov4J` instruments your java application code through running your tests, and calculates object code coverage level for your test suites.

The approach is depicted in this paper:
[M. Ghoreshi, and H. Haghighi. "Object coverage criteria for supporting object-oriented testing." Software Quality Journal (2023)](https://link.springer.com/article/10.1007/s11219-023-09643-3).
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

