jwordsearch
===========

Haven't written java for years, so I figured I'd brush up and do a quick, for-fun project which reads a word puzzle from a text file, and looks for a given word in all directions within it

To run; 

Build using maven:
------------------
```
git clone https://github.com/eljrax/jwordsearch.git  
cd jwordsearch/jwordsearch/  
mvn install  
#On a mac, you may need to specify the java version to use, as it defaults to 1.6  
JAVA_HOME=$(/usr/libexec/java_home -v 1.7) mvn install  
java -jar ~/.m2/repository/org/eljrax/jwordsearch/1.0-SNAPSHOT/jwordsearch-1.0-SNAPSHOT-jar-with-dependencies.jar  
```
Alternatively - avoiding maven:
-------------------------------
```
git clone https://github.com/eljrax/jwordsearch.git
cd jwordsearch/jwordsearch/
javac src/main/java/org/eljrax/jwordsearch/App.java
java -classpath src/main/java/ org/eljrax/jwordsearch/App
```
