# Prison API server #


[![CircleCI](https://circleci.com/gh/ministryofjustice/prison-api/tree/main.svg?style=svg)](https://circleci.com/gh/ministryofjustice/prison-api)
[![API docs](https://img.shields.io/badge/API_docs-view-85EA2D.svg?logo=swagger)](https://api-dev.prison.service.justice.gov.uk/swagger-ui/)

### How do I get set up? ###

* If using Intellij, install [Lombok](http://projectlombok.org/download)

* The mobile API Server uses the configuration file in YAML format. There is at least one initial configuration
  packaged in the jar on

    `prison-api/src/main/resources/application.yaml`

    Let's say you want to create a specific configuration to the DEV profile: just copy a file to your configurations.

    `prison-api/src/main/configs/application-dev.yml`

* Dependencies
  To minimise dependency management we use Spring Boot because it gives us a broad set of
  frameworks with the right version avoiding library conflicts.
  
* Build command

    `gradlew clean build`
  
* Database configuration
  To connect the application to a database, change the configurations in your configuration profile file, for example in mobile-dev.yml.

### How to run the Application? ###

1) Run `uk.gov.justice.hmpps.prison.PrisonApiServer` using spring boot profile `nomis-hsqldb`

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

In order to get the `/info` endpoint to work you will need to add in
```
-add-opens java.base/java.lang=ALL-UNNAMED
-add-opens java.base/java.util=ALL-UNNAMED
```
to your run configuration.  This is because the current version of ehcache needs to calculate the size of the objects in the cache, which the latest version of openjdk disallows.

#### Authenticating with JWT when running locally

When running locally with the profile `dev` JWTs are verified against resources/local-public-key.pem.  When deployed to t3, t2, preprod or prod, JWTs are verified against the public key hosted by the auth server at https://<auth-server>/auth/.well-known/jwks.json.

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

- `/health/ping`: will respond `{"status":"UP"}` to all requests.  This should be used by dependent systems to check connectivity to prison api,
rather than calling the `/health` endpoint.
- `/health`: provides information about the application health and its dependencies.  This should only be used
by prison-api health monitoring (e.g. pager duty) and not other systems who wish to find out the state of prison api
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

docker build -t quay.io/hmpps/prison-api .

docker run -d -p 8888:8080             \
       -h prison-api                   \
       --name=prison-api               \
    quay.io/hmpps/prison-api:latest
```

### Using Docker compose ###
```bash
docker-compose up -d
```

### Running Feature Tests ###

All feature tests can be run from `uk.gov.justice.hmpps.prison.executablespecification.AllFeatureTest`

Tests can be run individually by adding a `@wip` tag to the top of the feature file, or to an individual feature scenario, and
then adding the environment variable `cucumber.options=--tags '@wip'` to the executable arguments in 'Edit Confgurations'.

To run all feature tests in IntelliJ, avoiding known @wip or @broken tests, add the following env vars into the 'Edit Configurations' tab:-

| Env var        | Value                         |
| -------------- | ------------------------------|
|api.db.target   | nomis                         | 
|cucumber.options| --tags 'not(@wip or @broken)' |

### Running with Data Compliance Queues

Prison API is currently being used to retrieve data from the NOMIS database for assessing
an offender's eligibility of deletion under data compliance law.

A suite of services can be started with docker-compose that will also attach Prison API to
a couple of queues used to communicate deletions and deletion referrals.

To do this, simply run:

```bash
TMPDIR=/private$TMPDIR docker-compose -f docker-compose-data-compliance.yml up
```

#### To publish to the data compliance request queue:
(Warning, if configured, this will prompt Prison-api to delete the Offender provided)

The `referralId` should match an existing referral in the `OFFENDER_DELETION_REFERRAL` table
in the Data Compliance database.

```bash

aws --endpoint-url=http://localhost:4576 sqs send-message \
    --queue-url http://localstack:4576/queue/data_compliance_request_queue \
    --message-body '{"offenderIdDisplay":"A1234AA","referralId":123}' \
    --message-attributes "eventType={StringValue=DATA_COMPLIANCE_OFFENDER-DELETION-GRANTED,DataType=String}"
```

#### To read off the data compliance response queue: 
```bash
aws --endpoint-url=http://localhost:4576 sqs receive-message \
    --queue-url http://localhost:4576/queue/data_compliance_response_queue
```

### Authorize with Swagger UI ###
* Without correct authorisation calling endpoints will return http 401 - Unauthorized
* Obtain a correct JWT Token from 'Auth' service
* Go to http://<`host:port`>/swagger-ui/
* Click on top right button 'Authorize'
* In the 'Authorize' dialog enter 
    * `Bearer <Your Token>`
    * Like so `Bearer eyJhbGciOiJ...OsgGjHBuA`
 * Then click 'Authorize' and close dialog
 * The endpoint calls should pass security and allowed through now.
 * When the token expires logout of the 'Authorize' and enter a fresh token.



