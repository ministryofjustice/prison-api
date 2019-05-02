# Elite2 API server #

### How do I get set up? ###

* If using Intellij, install [Lombok](http://projectlombok.org/download)

* The mobile API Server uses the configuration file in YAML format. There is at least one initial configuration
  packaged in the jar on

    `elite2-api/src/main/resources/application.yaml`

    Let's say you want to create a specific configuration to the DEV profile: just copy a file to your configurations.

    `elite2-api/src/main/configs/application-dev.yml`

* Dependencies
  To minimise dependency management we use Spring Boot because it gives us a broad set of
  frameworks with the right version avoiding library conflicts.
  
* Build command

    `gradlew clean build`
  
* Database configuration
  To connect the application to a database, change the configurations in your configuration profile file, for example in mobile-dev.yml.

### How to run the Application? ###

1) Run `net.syscon.elite.Elite2ApiServer` using spring boot profile `nomis-hsqldb`

2) Using Gradle directly via command-line (optionally setting the spring profile variable to select the in-memory database)

```bash
set JAVA_OPTS=-Dspring.profiles.active=nomis-hsqldb
gradlew bootRun
```

### Environment Variables

Below are the key environment variables that can be set per service:-
```properties
SPRING_PROFILES_ACTIVE=nomis
SPRING_DATASOURCE_URL=<jdbc datasource url>
SPRING_DATASOURCE_PASSWORD=<db password>
```

3) In a Docker container

Build Docker image and run

```bash
./gradlew clean assemble

docker build -t mojdigitalstudio/elite2-api .

docker run -d -p 8888:8080             \
       -h elite2-api                   \
       --name=elite2-api               \
    mojdigitalstudio/elite2-api:latest
```

### Using Docker compose ###
```bash
docker-compose up -d
```

### Running Feature Tests ###

All feature tests can be run within intellij by running `net.syscon.elite.executablespecification.AllFeatureTest`
with environment variable of `api.db.target=nomis`

Tests can be run individually by adding a `@wip` tag to the top of the feature file, or to an individual feature scenario.
Then add the environment variable `cucumber.options=--tags '@wip'` to the executable arguments.

### Project related links ###
* [BitBucket](https://bitbucket.org/cool_syscon_team/mobile)
* [CircleCi](https://circleci.com/bb/cool_syscon_team)
