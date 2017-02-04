# README #

This README is going to show how to run Elite2 API server.

### What is this repository for? ###

* This is a initial version to create a standard API using the
  best available technology to expose the business rules
  already done in the Elite product.   
  
* Version 
* 2.0-SNAPSHOT

### How do I get set up? ###

* The Elite2 API Server uses the configuration file in YAML format. There is at least one initial configuration in

    elite2-web/src/main/resources/elite2.yaml
       
  If you need override any property from the initial configuration file, you have to do it in your own copy. Just copy
  the SAMPLE file available in accordingly with your desired profile. 
    
    elite2-web/src/main/configs/elite2-dev-SAMPLE.yml
  
  Let's say you want create a specific configuration to the DEV profile, so just copy a file to your configurations.
       
    elite2-web/src/main/configs/elite2-dev.yml
    
* Dependencies
  To minimize the dependency management job we choose to use Spring Boot because they give us a broad set of
  frameworks with the right version avoiding library conflicts.
  
* Database configuration
  To connect the application to a database, change the configurations in your configuration profile file, for example in elite2-dev.yml.
  
* How to run the Application
  You can use the Gradle to run the application or you can just run the main method of the class 
  net.syscon.elite.Elite2Application in your IDE or you can also package in a jar file and run it using
  java -jar elite2.jar
  
* How to package the Application
  To create a JAR of the application just run gradle to package it:
    
    gradle package
    

* Deployment instructions


### Contribution guidelines ###

* Writing tests
* Code review
* Other guidelines

### Who do I talk to? ###

* Repo owner or admin




