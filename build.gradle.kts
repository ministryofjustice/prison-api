plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "4.3.1"
  kotlin("plugin.spring") version "1.7.0"
  kotlin("plugin.jpa") version "1.7.0"
  kotlin("plugin.lombok") version "1.7.0"
}

configurations {
  implementation {
    exclude(module = "commons-logging")
    exclude(module = "log4j")
    exclude(module = "c3p0")
    exclude(module = "tomcat-jdbc")
  }
}

// spring boot configuration specifies the version of selenium so need to override for serenity to work properly
ext["selenium.version"] = "4.1.1"

// SDI-261: Pin versions to prevent issue with useLatestVersions

// Problem with hibernate-core 5.6.7.Final which was causing issue with 'startingWith'
// https://github.com/spring-projects/spring-data-jpa/issues/2472
// https://hibernate.atlassian.net/browse/HHH-15142
// Temporarily revert to 5.6.5.Final until fixed
val hibernateCoreVersion by extra("5.6.5.Final")

// Temporarily kept at 4.3 due to bug in 4.4 parser
val jsqlParserVersion by extra("4.3")

// Temporarily keep at 2.5.1 until can switch to h2 instead (tests break anyway with 2.6.1)
val hsqldbVersion by extra("2.5.1")

// groovy errors with latest version
val restAssuredVersion by extra("4.5.1")

// Temporarily keep at 3.2.2 as seems to bring in groovy incompatibilites by upgrading to 3.2.4
val serenityVersion by extra("3.2.2")

dependencies {
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
  annotationProcessor("org.projectlombok:lombok:1.18.24")
  testAnnotationProcessor("org.projectlombok:lombok:1.18.24")

  implementation("org.springframework.boot:spring-boot-starter-aop")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("org.springframework.boot:spring-boot-starter-jdbc")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-cache")
  developmentOnly("org.springframework.boot:spring-boot-devtools")

  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

  implementation("org.hibernate:hibernate-core:$hibernateCoreVersion")

  implementation("javax.annotation:javax.annotation-api:1.3.2")
  implementation("javax.xml.bind:jaxb-api:2.3.1")
  implementation("com.sun.xml.bind:jaxb-impl:4.0.0")
  implementation("com.sun.xml.bind:jaxb-core:4.0.0")
  implementation("javax.activation:activation:1.1.1")

  implementation("commons-codec:commons-codec:1.15")
  implementation("com.github.jsqlparser:jsqlparser:$jsqlParserVersion")
  implementation("net.sf.ehcache:ehcache:2.10.9.2")
  implementation("com.zaxxer:HikariCP:5.0.1")

  implementation("io.swagger:swagger-annotations:1.6.6")
  implementation("org.springdoc:springdoc-openapi-ui:1.6.9")
  implementation("org.springdoc:springdoc-openapi-kotlin:1.6.9")
  implementation("org.springdoc:springdoc-openapi-data-rest:1.6.9")

  implementation("org.apache.commons:commons-lang3:3.12.0")
  implementation("commons-io:commons-io:2.11.0")
  implementation("org.apache.commons:commons-text:1.9")
  implementation("com.oracle.database.jdbc:ojdbc10:19.15.0.0.1")

  compileOnly("org.projectlombok:lombok:1.18.24")

  runtimeOnly("org.hsqldb:hsqldb:$hsqldbVersion")
  runtimeOnly("org.flywaydb:flyway-core:8.5.13")

  testImplementation("org.springframework.boot:spring-boot-test-autoconfigure")
  testImplementation("org.springframework.boot:spring-boot-starter-webflux")
  testImplementation("io.rest-assured:rest-assured:$restAssuredVersion")
  testImplementation("io.rest-assured:json-schema-validator:$restAssuredVersion")
  testImplementation("io.rest-assured:spring-mock-mvc:$restAssuredVersion")
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("com.google.code.gson:gson:2.9.0")
  testImplementation("org.powermock:powermock-api-mockito2:2.0.9")
  testImplementation("org.powermock:powermock-module-junit4:2.0.9")

  testImplementation("com.tngtech.java:junit-dataprovider:1.13.1")
  testImplementation("net.javacrumbs.json-unit:json-unit-assertj:2.35.0")

  testImplementation("net.serenity-bdd:serenity-core:$serenityVersion")
  testImplementation("net.serenity-bdd:serenity-junit:$serenityVersion")
  testImplementation("net.serenity-bdd:serenity-spring:$serenityVersion")
  testImplementation("net.serenity-bdd:serenity-cucumber:$serenityVersion")
  testImplementation("com.paulhammant:ngwebdriver:1.1.6")
  testImplementation("org.slf4j:slf4j-api:1.7.36")
  testImplementation("com.github.tomakehurst:wiremock-standalone:2.27.2")
  testImplementation("io.jsonwebtoken:jjwt:0.9.1")
  testImplementation("org.glassfish:javax.el:3.0.0")
  testImplementation("io.swagger.parser.v3:swagger-parser:2.1.1")

  testCompileOnly("org.projectlombok:lombok:1.18.24")
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(18))
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
      freeCompilerArgs = listOf("-Xjvm-default=all")
      jvmTarget = "18"
    }
  }

  // Exclude Serenity BDD integration and IntTest tests from "test" task so they can be controlled independently
  test {
    useJUnitPlatform {
      exclude("**/executablespecification/*")
      exclude("**/*IntTest*")
    }
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
      jvmTarget = "18"
    }
  }
}
