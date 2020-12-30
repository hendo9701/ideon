# IDEon
**An IDE for building language recognizers written with ANTLR**

## Setup
1.  ```git clone https://github.com/hendo9701/ideon.git```
1.  ```cd ideon```
1.  ```./gradlew build```
1.  Add a system variable named "ANTLR_HOME" and set its value to be the path of your current installed
version of antlr. You can get it [here](https://www.antlr.org/download.html). 
    1. Windows: 
        1.  ```set ANTLR_HOME=<PATH TO ANTLR JAR>```
        1.  Type: ```echo %ANTLR_HOME%``` and you should see the path to the antlr jar
    2. Linux:   
        1.  ```ANTLR_HOME=<<PATH TO ANTLR JAR>>```
        1.  Type ```echo $ANTLR_HOME``` and you should see the path to the antlr jar
1.  ```./gradlew run```

## Features
1.  Basic syntax highlighting 
1.  Project tree visualization
1.  Configurable [ANTLR](https://www.antlr.org/) code generation
1.  Compiler and interpreter generation (*Pending*)
1.  Grammar testing via parse tree visualization

## Extensibility
1.  Support for languages other than java can be added via ```css``` and ```regex```
