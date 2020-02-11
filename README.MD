# Elite2 API server #


[![CircleCI](https://circleci.com/gh/ministryofjustice/elite2-api/tree/master.svg?style=svg)](https://circleci.com/gh/ministryofjustice/elite2-api)
[![Known Vulnerabilities](https://snyk.io/test/github/ministryofjustice/elite2-api/badge.svg)](https://snyk.io/test/github/ministryofjustice/elite2-api)

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

*Windows*
```bash
set JAVA_OPTS=-Dspring.profiles.active=nomis-hsqldb
gradlew bootRun
```

*Mac*
```bash
./gradlew bootRun --args='--spring.profiles.active=nomis-hsqldb'
```

### Coding Standards
There are a few different styles in this project due to historical reasons and the accumulation of technical debt.

The preferred patterns going forward are:
- Use Spring Rest Controllers for APIs
- Use thin controllers and apply business logic in an associated service 
- Use SpringJPA for database access
- Use TestRestTemplate for integration testing (e.g. extend ResourceTest, do not use Cucumber)
- Use ControllerAdvice for error handling
- Do not create interfaces with single implementations

There are some recent examples around PrisonStatusController and OffenderImageRepository and AgencyResourceImplIntTest.
  
#### Health

- `/ping`: will respond `pong` to all requests.  This should be used by dependent systems to check connectivity to elite 2,
rather than calling the `/health` endpoint.
- `/health`: provides information about the application health and its dependencies.  This should only be used
by elite 2 health monitoring (e.g. pager duty) and not other systems who wish to find out the state of elite 2.
- `/info`: provides information about the version of deployed application.

### Querying a local HSQLDB Database 

The feature tests and repository integration tests create and populate a local in-memory HSQLDB called `nomis-db`.
This is by definition transient but it can be very useful to persist this database temporarily in order to 
execute test queries, examine the data and check your repository queries.

To do this:

* Alter the file `application-nomis-hsqldb.yml` to use a file-based url.
  In this example I created a directory ~/dbs under my home directory first.
  
`spring:
   datasource:
     url: jdbc:hsqldb:file:~/dbs/nomis-db;sql.syntax_ora=true;get_column_name=false;shutdown=false;sql.nulls_first=false;sql.nulls_order=false
     username: sa
     password: changeme
`

* Add a temporary password to the spring datasource (by default its blank) which 
  then does not allow a client connection via the IntelliJ database tools.

* Run one of the repository integration tests in IntelliJ to create and populate the DB
  and let it finish and exit. As its now file-based the database remains intact.

* Within IntelliJ, select the `Database` tab and add a datasource with the following properties:

`DriverType: HSQLDB`
`Connection type: URL only`
`URL: jdbc:hsqldb:file:/<your home dir>/dbs/nomis-db;sql.syntax_ora=true;get_column_name=false;shutdown=false;sql.nulls_first=false;sql.nulls_order=false;hsqldb.lock_file=false`
`User: sa`
`Password: changme`

* Connect, open an SQL editor or browse the tables with the test data present.

* Important: Close the connection and remove the database files before attempting
  to re-run any integration or feature tests.
  
  `$ cd ~/dbs`
  `$ rm -rf nomis-db*`

* Don't commit these changes!  

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

All feature tests can be run from `net.syscon.elite.executablespecification.AllFeatureTest`

Tests can be run individually by adding a `@wip` tag to the top of the feature file, or to an individual feature scenario, and
then adding the environment variable `cucumber.options=--tags '@wip'` to the executable arguments in 'Edit Confgurations'.

To run all feature tests in IntelliJ, avoiding known @wip or @broken tests, add the following env vars into the 'Edit Configurations' tab:-

| Env var        | Value                         |
| -------------- | ------------------------------|
|api.db.target   | nomis                         | 
|cucumber.options| --tags 'not(@wip or @broken)' |

### Running localstack
```bash
TMPDIR=/private$TMPDIR docker-compose -f docker-compose-data-compliance.yml up localstack
```

#### Creating the Topic and Queue
Simpliest way is running the following script
```bash
./setup-queue.bash
```

Or you can run the scripts individually as shown below.

#### Creating a topic and queue on localstack

```bash
aws --endpoint-url=http://localhost:4575 sns create-topic --name offender_events
```

Results in:
```json
{
    "TopicArn": "arn:aws:sns:eu-west-2:000000000000:offender_events"
}

```

#### Creating a queue
```bash
aws --endpoint-url=http://localhost:4576 sqs create-queue --queue-name elite2_api_queue
```

Results in:
```json
{
   "QueueUrl": "http://localhost:4576/queue/elite2_api_queue"
}
```

#### Creating a subscription
```bash
aws --endpoint-url=http://localhost:4575 sns subscribe \
    --topic-arn arn:aws:sns:eu-west-2:000000000000:offender_events \
    --protocol sqs \
    --notification-endpoint http://localhost:4576/queue/elite2_api_queue \
    --attributes '{"FilterPolicy":"{\"eventType\":[\"DATA_COMPLIANCE_DELETE-OFFENDER\"]}"}'
```

Results in:
```json
{
    "SubscriptionArn": "arn:aws:sns:eu-west-2:000000000000:offender_events:074545bd-393c-4a43-ad62-95b1809534f0"
}
```

#### Publish to the queue
(Warning, if configured, this will prompt Elite2 to delete the Offender provided)
```bash
aws --endpoint-url=http://localhost:4575 sns publish \
    --topic-arn arn:aws:sns:eu-west-2:000000000000:offender_events \
    --message '{"offenderIdDisplay":"SOME_OFFENDER_ID_DISPLAY"}' \ 
    --message-attributes "eventType={StringValue=DATA_COMPLIANCE_DELETE-OFFENDER,DataType=String}"
```

#### Read off the queue
```bash
aws --endpoint-url=http://localhost:4576 sqs receive-message \
    --queue-url http://localhost:4576/queue/elite2_api_queue
```

### Project related links ###
* [GitHub](https://github.com/ministryofjustice/elite2-api)
* [Old location - BitBucket](https://bitbucket.org/cool_syscon_team/mobile)
* [CircleCi](https://circleci.com/bb/cool_syscon_team)
