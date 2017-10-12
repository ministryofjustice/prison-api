# README #

This README is going to show how to run mobile API server.

### What is this repository for? ###

* This is a initial version to create a standard API using the
  best available technology to expose the business rules
  already done in the Elite product.

### How do I get set up? ###

* If using Eclipse, install the buildship Gradle plugin, and [Lombok](http://projectlombok.org/download)

* The mobile API Server uses the configuration file in YAML format. There is at least one initial configuration
  packaged in the jar on

    `mobile-web/src/main/resources/mobile.yaml`

    Let's say you want to create a specific configuration to the DEV profile: just copy a file to your configurations.

    `mobile-web/src/main/configs/mobile-dev.yml`

* Dependencies
  To minimise dependency management we use Spring Boot because it gives us a broad set of
  frameworks with the right version avoiding library conflicts.
  
* Build command

    `gradlew clean build -x integTest`
  
* Database configuration
  To connect the application to a database, change the configurations in your configuration profile file, for example in mobile-dev.yml.

### How to run the Application? ###

1) Running the main method of the class `net.syscon.elite.MobileApiServer` in your IDE
Note that this does not yet work in Eclipse due to a gradle bug (see [here](https://github.com/gradle/gradle/pull/3016))

2) Using Gradle directly via command-line (optionally setting the spring profile variable to select the in-memory database)

```bash
set JAVA_OPTS=-Dspring.profiles.active=nomis,nomis-hsqldb,noproxy,dev
gradlew bootRun
```

3) Using `java -jar` with a packaged version
```bash
gradle assemble

nohup java -cp mobile-web-<VERSION>.jar:/etc/elite-mobile -Dloader.main=net.syscon.elite.MobileApiServer org.springframework.boot.loader.PropertiesLauncher &
```
Where `/etc/elite-mobile` is the external configuration directory. In the project this directory is `mobile-web/src/main/configs`

If you need to build against Oracle 11 use:-
```bash
./gradlew clean assemble -x integTest
```

4) In a Docker container

Build Docker image and run

```bash
./gradlew clean assemble
docker build -t sysconjusticesystems/elite2-api .
docker run -d -p 8888:8080 -e SERVER_PORT=8080 -e SPRING_PROFILES_ACTIVE=dev,nomis -e SPRING_DATASOURCE_URL=jdbc:oracle:thin:@lonlx01:1521:pvb_dev --name=elite2-api sysconjusticesystems/elite2-api 
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
* [BitBucket](https://bitbucket.org/cool_syscon_team/mobile)
* [CircleCi](https://circleci.com/bb/cool_syscon_team)
