plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "8.3.4"
  kotlin("plugin.spring") version "2.2.0"
  kotlin("plugin.jpa") version "2.2.0"
  kotlin("plugin.lombok") version "2.2.0"
}

configurations {
  implementation {
    exclude(module = "commons-logging")
    exclude(module = "log4j")
    exclude(module = "c3p0")
    exclude(module = "tomcat-jdbc")
  }
}

ext["hibernate.version"] = "6.5.3.Final"

dependencies {
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
  annotationProcessor("org.projectlombok:lombok:1.18.38")
  testAnnotationProcessor("org.projectlombok:lombok:1.18.38")

  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:1.5.0")
  implementation("org.springframework.boot:spring-boot-starter-aop")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("org.springframework.boot:spring-boot-starter-jdbc")
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  developmentOnly("org.springframework.boot:spring-boot-devtools")

  implementation("commons-codec:commons-codec:1.19.0")
  // Had to leave jsqlparser at 5.2 because in 5.3 it fails to parse "Between blah AND blah"
  implementation("com.github.jsqlparser:jsqlparser:5.2")
  implementation("org.ehcache:ehcache:3.10.8")
  runtimeOnly("com.zaxxer:HikariCP")

  implementation("io.swagger:swagger-annotations:1.6.16")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9")

  implementation("org.apache.commons:commons-lang3:3.18.0")
  implementation("commons-io:commons-io:2.20.0")
  implementation("com.google.guava:guava:33.4.8-jre")
  implementation("org.apache.commons:commons-text:1.14.0")
  implementation("com.oracle.database.jdbc:ojdbc10:19.27.0.0")
  implementation("org.hibernate.orm:hibernate-community-dialects")

  compileOnly("org.projectlombok:lombok:1.18.38")

  // we run on oracle in all environments, but allow instance to be started using hsqldb too
  runtimeOnly("org.hsqldb:hsqldb:2.7.4")
  runtimeOnly("org.flywaydb:flyway-database-hsqldb")

  testImplementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter-test:1.4.11")
  testImplementation("org.springframework.boot:spring-boot-test-autoconfigure")
  testImplementation("org.springframework.boot:spring-boot-starter-webflux")
  testImplementation("io.rest-assured:rest-assured:5.5.5")
  testImplementation("io.rest-assured:json-schema-validator:5.5.5")
  testImplementation("io.rest-assured:spring-mock-mvc:5.5.5")
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("com.google.code.gson:gson:2.13.1")
  testImplementation("org.mockito:mockito-inline:5.2.0")
  testImplementation("org.powermock:powermock-api-mockito2:2.0.9")
  testImplementation("org.powermock:powermock-module-junit4:2.0.9")

  testImplementation("com.tngtech.java:junit-dataprovider:1.13.1")
  testImplementation("net.javacrumbs.json-unit:json-unit-assertj:4.1.1")

  testImplementation("net.serenity-bdd:serenity-core:4.2.34")
  testImplementation("net.serenity-bdd:serenity-junit:4.2.34")
  testImplementation("net.serenity-bdd:serenity-spring:4.2.34")
  testImplementation("net.serenity-bdd:serenity-cucumber:4.2.34")
  testImplementation("com.paulhammant:ngwebdriver:1.2")
  testImplementation("org.wiremock:wiremock:3.13.1")
  testImplementation("io.swagger.parser.v3:swagger-parser:2.1.31") {
    exclude(group = "io.swagger.core.v3")
  }
  testImplementation("io.swagger.core.v3:swagger-core-jakarta:2.2.31")
  testImplementation("commons-beanutils:commons-beanutils:1.11.0")
  testImplementation("io.opentelemetry:opentelemetry-sdk-testing:1.50.0")

  testCompileOnly("org.projectlombok:lombok:1.18.38")
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

kotlin {
  jvmToolchain(21)
  kotlinDaemonJvmArgs = listOf("-Xmx1g", "-Xms256m", "-XX:+UseParallelGC")
  compilerOptions {
    freeCompilerArgs.addAll("-Xjvm-default=all", "-Xwhen-guards", "-Xannotation-default-target=param-property")
  }
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions.jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
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
    environment(mapOf("api.db.target" to "nomis", "cucumber.filter.tags" to "not(@wip or @broken)"))
    useJUnitPlatform {
      include("**/executablespecification/*")
    }
    minHeapSize = "128m"
    maxHeapSize = "2048m"
  }

  register<Test>("testWithSchemaNomisOracle") {
    environment(mapOf("api.db.target" to "nomis", "api.db.dialect" to "oracle", "cucumber.filter.tags" to "not(@wip or @broken)"))
    useJUnitPlatform {
      include("**/executablespecification/*")
    }
  }
}

allOpen {
  annotation("uk.gov.justice.hmpps.prison.repository.jpa.helper.EntityOpen")
}
