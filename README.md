# OCov4J: Object Coverage for Java
A tool for measuring test code coverage based on "object code coverage" concept which is more consistent with object-oriented related issues than classic code coverage. `OCov4J` instruments your java application code through running your tests, and calculates object code coverage level for your test suites.
## How to use
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

