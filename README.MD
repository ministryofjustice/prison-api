# README #

This README is going to show how to run mobile API server.

### What is this repository for? ###

* This is a initial version to create a standard API using the
  best available technology to expose the business rules
  already done in the Elite product.   
  
* Version 
* 2.0-SNAPSHOT

### How do I get set up? ###

* The mobile API Server uses the configuration file in YAML format. There is at least one initial configuration
  packaged in the jar on

    mobile-web/src/main/resources/mobile.yaml
       
  If you need override any property from the initial configuration file, you have to do it in your own copy. Just copy
  the SAMPLE file available in accordingly with your desired profile. 
    
    mobile-web/src/main/configs/mobile-dev-SAMPLE.yml
  
  Let's say you want create a specific configuration to the DEV profile, so just copy a file to your configurations.
       
    mobile-web/src/main/configs/mobile-dev.yml
    
* Dependencies
  To minimize the dependency management job we choose to use Spring Boot because they give us a broad set of
  frameworks with the right version avoiding library conflicts.
  
* Database configuration
  To connect the application to a database, change the configurations in your configuration profile file, for example in mobile-dev.yml.

###How to run the Application? ###

1) Running the main method of the class `net.syscon.elite.MobileApiServer` in your IDE

2) Using directly Gradle via command-line
```bash
gradle run
```

3) Using `java -jar` with a pakcaged version
```bash
gradle assemble
...
nohup java -cp mobile-web-<VERSION>.jar:/etc/elite-mobile -Dloader.main=net.syscon.elite.MobileApiServer org.springframework.boot.loader.PropertiesLauncher &
```
Where `/etc/elite-mobile` is the external configuration directory. In the project this directory is `mobile-web/src/main/configs`

4) In a Docker container

Build Docker image and run

```bash
docker build -t sysconjusticesystems/mobile-web .
```

Run 
```bash
docker run -d -p 7080:7080 -e SERVER_PORT=7080 -e SPRING_PROFILES_ACTIVE=dev --name=mobile-web sysconjusticesystems/mobile-web 
```

### Using Docker compose ###
```bash
docker-compose up -d
```

### TODO: ###
* Automated Tests
* Code style and metrics
* Continuous integration
* Authentication/Authorization

### Project related links ###
* [SonarQube](http://chronos.syscon.ca:9000/sonar)
* [Artifactory](http://chronos.syscon.ca:8081/artifactory)
* [Jenkins](http://chronos.syscon.ca:7080)