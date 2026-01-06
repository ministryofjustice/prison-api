plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "10.0.0"
  kotlin("plugin.spring") version "2.3.0"
  kotlin("plugin.jpa") version "2.3.0"
  kotlin("plugin.lombok") version "2.3.0"
}

configurations {
  implementation {
    exclude(module = "log4j")
    exclude(module = "c3p0")
    exclude(module = "tomcat-jdbc")
  }
}

dependencies {
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
  annotationProcessor("org.projectlombok:lombok:1.18.42")
  testAnnotationProcessor("org.projectlombok:lombok:1.18.42")

  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:2.0.0")
  implementation("org.springframework.boot:spring-boot-starter-aspectj")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("org.springframework.boot:spring-boot-starter-jdbc")
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-flyway")
  implementation("org.springframework.boot:spring-boot-jackson2")

  implementation("commons-codec:commons-codec:1.20.0")
  // Had to leave jsqlparser at 5.2 because in 5.3 it fails to parse "Between blah AND blah"
  val jsqlParserVersion = "5.2"
  implementation("com.github.jsqlparser:jsqlparser:$jsqlParserVersion")
  implementation("org.ehcache:ehcache:3.11.1")
  runtimeOnly("com.zaxxer:HikariCP")

  implementation("io.swagger:swagger-annotations:1.6.16")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.1")

  implementation("org.apache.commons:commons-lang3:3.20.0")
  implementation("commons-io:commons-io:2.21.0")
  implementation("com.google.guava:guava:33.5.0-jre")
  implementation("org.apache.commons:commons-text:1.15.0")
  // Had to leave oracle at 21.20.0.0 because in 23 fails to compile stored procedures
  val oracleVersion = "21.20.0.0"
  implementation("com.oracle.database.jdbc:ojdbc11:$oracleVersion")
  implementation("org.hibernate.orm:hibernate-community-dialects")

  compileOnly("org.projectlombok:lombok:1.18.42")

  // we run on oracle in all environments, but allow instance to be started using hsqldb too
  runtimeOnly("org.hsqldb:hsqldb:2.7.4")
  runtimeOnly("org.flywaydb:flyway-database-hsqldb")

  testImplementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter-test:2.0.0")
  testImplementation("org.springframework.boot:spring-boot-test-autoconfigure")
  testImplementation("org.springframework.boot:spring-boot-starter-webflux-test")
  testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
  testImplementation("org.springframework.boot:spring-boot-starter-data-jdbc-test")
  testImplementation("org.springframework.boot:spring-boot-starter-jackson-test")
  testImplementation("org.springframework.boot:spring-boot-starter-gson-test")
  testImplementation("org.springframework.boot:spring-boot-starter-cache-test")
  testImplementation("org.springframework.boot:spring-boot-resttestclient")
  testImplementation("org.springframework.boot:spring-boot-restclient")
  testImplementation("io.rest-assured:rest-assured:5.5.6")
  testImplementation("io.rest-assured:json-schema-validator:5.5.6")
  testImplementation("io.rest-assured:spring-mock-mvc:5.5.6")
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("com.google.code.gson:gson:2.13.2")
  testImplementation("org.mockito:mockito-inline:5.2.0")
  testImplementation("org.powermock:powermock-api-mockito2:2.0.9")
  testImplementation("org.powermock:powermock-module-junit4:2.0.9")

  testImplementation("com.tngtech.java:junit-dataprovider:1.13.1")
  testImplementation("net.javacrumbs.json-unit:json-unit-assertj:5.1.0")

  testImplementation("net.serenity-bdd:serenity-core:4.2.34")
  testImplementation("net.serenity-bdd:serenity-junit:4.2.34")
  testImplementation("net.serenity-bdd:serenity-spring:4.2.34")
  testImplementation("net.serenity-bdd:serenity-cucumber:4.2.34")
  testImplementation("com.paulhammant:ngwebdriver:1.2")
  testImplementation("org.wiremock:wiremock:3.13.2")
  testImplementation("io.swagger.parser.v3:swagger-parser:2.1.37") {
    exclude(group = "io.swagger.core.v3")
  }
  testImplementation("io.swagger.core.v3:swagger-core-jakarta:2.2.41")
  testImplementation("commons-beanutils:commons-beanutils:1.11.0")
  testImplementation("io.opentelemetry:opentelemetry-sdk-testing:1.53.0")

  testCompileOnly("org.projectlombok:lombok:1.18.42")
}

kotlin {
  jvmToolchain(25)
  kotlinDaemonJvmArgs = listOf("-Xmx1g", "-Xms256m", "-XX:+UseParallelGC")
  compilerOptions {
    freeCompilerArgs.addAll("-Xwhen-guards", "-Xannotation-default-target=param-property")
  }
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions.jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25
  }

  // Exclude Serenity BDD integration and IntTest tests from "test" task so they can be controlled independently
  test {
    useJUnitPlatform {
      exclude("**/executablespecification/*")
      exclude("**/*IntTest*")
    }
    minHeapSize = "128m"
    maxHeapSize = "2048m"
  }

  val test by testing.suites.existing(JvmTestSuite::class)
  register<Test>("testIntegration") {
    testClassesDirs = files(test.map { it.sources.output.classesDirs })
    classpath = files(test.map { it.sources.runtimeClasspath })
    useJUnitPlatform {
      include("**/*IntTest*")
    }
    minHeapSize = "128m"
    maxHeapSize = "2048m"
  }

  register<Test>("testWithSchemaNomis") {
    testClassesDirs = files(test.map { it.sources.output.classesDirs })
    classpath = files(test.map { it.sources.runtimeClasspath })
    environment(mapOf("api.db.target" to "nomis", "cucumber.filter.tags" to "not(@wip or @broken)"))
    useJUnitPlatform {
      include("**/executablespecification/*")
    }
    minHeapSize = "128m"
    maxHeapSize = "2048m"
  }

  register<Test>("testWithSchemaNomisOracle") {
    testClassesDirs = files(test.map { it.sources.output.classesDirs })
    classpath = files(test.map { it.sources.runtimeClasspath })
    environment(mapOf("api.db.target" to "nomis", "api.db.dialect" to "oracle", "cucumber.filter.tags" to "not(@wip or @broken)"))
    useJUnitPlatform {
      include("**/executablespecification/*")
    }
  }
}

allOpen {
  annotation("uk.gov.justice.hmpps.prison.repository.jpa.helper.EntityOpen")
}
