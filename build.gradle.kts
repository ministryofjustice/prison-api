plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "5.7.0"
  kotlin("plugin.spring") version "1.9.10"
  kotlin("plugin.jpa") version "1.9.10"
  kotlin("plugin.lombok") version "1.9.10"
}

configurations {
  implementation {
    exclude(module = "commons-logging")
    exclude(module = "log4j")
    exclude(module = "c3p0")
    exclude(module = "tomcat-jdbc")
  }
}

dependencyCheck {
  suppressionFiles.add("dependency-check-suppress-h2.xml")
}

// Temporarily kept at 4.3 due to bug in 4.4 parser
val jsqlParserVersion by extra("4.3")

// Temporarily keep at 2.5.1 until can switch to h2 instead (tests break anyway with 2.6.1)
val hsqldbVersion by extra("2.5.1")

// Unable to upgrade to 2.1.16 as attempting to use the parser caused a 500 from /v3/api-docs. It seems there's an incorrect dependency on an OAS3.1 type but I couldn't work out which project is wrong - so hopefully this will get sorted by the maintainers in due course.
val swaggerParserVersion by extra("2.1.15")

ext["rest-assured.version"] = "5.1.1"

// Temporarily keep hibernate at 6.2.5 until https://hibernate.atlassian.net/jira/software/c/projects/HHH/issues/HHH-16926 is fixed in 6.2.7
ext["hibernate.version"] = "6.2.5.Final"

// Temporarily keep serenity at 3.x until can upgrade
val serenityVersion by extra("3.9.8")

dependencies {
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
  annotationProcessor("org.projectlombok:lombok:1.18.30")
  testAnnotationProcessor("org.projectlombok:lombok:1.18.30")

  implementation("org.springframework.boot:spring-boot-starter-aop")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("org.springframework.boot:spring-boot-starter-jdbc")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-cache")
  developmentOnly("org.springframework.boot:spring-boot-devtools")

  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

  implementation("commons-codec:commons-codec:1.16.0")
  implementation("com.github.jsqlparser:jsqlparser:$jsqlParserVersion")
  implementation("org.ehcache:ehcache:3.10.8")
  implementation("com.zaxxer:HikariCP:5.0.1")

  implementation("io.swagger:swagger-annotations:1.6.12")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")

  implementation("org.apache.commons:commons-lang3:3.13.0")
  implementation("commons-io:commons-io:2.14.0")
  implementation("com.google.guava:guava:32.1.3-jre")
  implementation("org.apache.commons:commons-text:1.10.0")
  implementation("com.oracle.database.jdbc:ojdbc10:19.20.0.0")
  implementation("org.hibernate.orm:hibernate-community-dialects")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

  compileOnly("org.projectlombok:lombok:1.18.30")

  runtimeOnly("org.hsqldb:hsqldb:$hsqldbVersion")
  runtimeOnly("org.flywaydb:flyway-core")

  testImplementation("org.springframework.boot:spring-boot-test-autoconfigure")
  testImplementation("org.springframework.boot:spring-boot-starter-webflux")
  testImplementation("io.rest-assured:rest-assured:5.3.2")
  testImplementation("io.rest-assured:json-schema-validator:5.3.2")
  testImplementation("io.rest-assured:spring-mock-mvc:5.3.2")
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("com.google.code.gson:gson:2.10.1")
  testImplementation("org.mockito:mockito-inline:5.2.0")
  testImplementation("org.powermock:powermock-api-mockito2:2.0.9")
  testImplementation("org.powermock:powermock-module-junit4:2.0.9")

  testImplementation("com.tngtech.java:junit-dataprovider:1.13.1")
  testImplementation("net.javacrumbs.json-unit:json-unit-assertj:3.2.2")

  testImplementation("net.serenity-bdd:serenity-core:$serenityVersion")
  testImplementation("net.serenity-bdd:serenity-junit:$serenityVersion")
  testImplementation("net.serenity-bdd:serenity-spring:$serenityVersion")
  testImplementation("net.serenity-bdd:serenity-cucumber:$serenityVersion")
  testImplementation("com.paulhammant:ngwebdriver:1.2")
  testImplementation("org.wiremock:wiremock:3.2.0")
  testImplementation("io.jsonwebtoken:jjwt-impl:0.12.3")
  testImplementation("io.jsonwebtoken:jjwt-jackson:0.12.3")
  testImplementation("io.swagger.parser.v3:swagger-parser:$swaggerParserVersion")
  testImplementation("io.opentelemetry:opentelemetry-sdk-testing:1.30.1")

  testCompileOnly("org.projectlombok:lombok:1.18.30")
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(20))
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
      freeCompilerArgs = listOf("-Xjvm-default=all")
      jvmTarget = "20"
    }
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

  register<Test>("testIntegration") {
    useJUnitPlatform {
      include("**/*IntTest*")
    }
    minHeapSize = "128m"
    maxHeapSize = "2048m"
  }

  register<Test>("testWithSchemaNomis") {
    environment(mapOf("api.db.target" to "nomis", "cucumber.options" to "--tags \"not (@wip or @broken)\""))
    useJUnitPlatform {
      include("**/executablespecification/*")
    }
    minHeapSize = "128m"
    maxHeapSize = "2048m"
  }

  register<Test>("testWithSchemaNomisOracle") {
    environment(
      mapOf(
        "api.db.target" to "nomis",
        "api.db.dialect" to "oracle",
        "cucumber.options" to "--tags \"not (@wip or @broken)\""
      )
    )

    useJUnitPlatform {
      include("**/executablespecification/*")
    }
  }

  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
      jvmTarget = "20"
    }
  }
}

allOpen {
  annotation("uk.gov.justice.hmpps.prison.repository.jpa.helper.EntityOpen")
}
