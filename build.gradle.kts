plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "6.0.2"
  kotlin("plugin.spring") version "2.0.0"
  kotlin("plugin.jpa") version "2.0.0"
  kotlin("plugin.lombok") version "2.0.0"
}

configurations {
  implementation {
    exclude(module = "commons-logging")
    exclude(module = "log4j")
    exclude(module = "c3p0")
    exclude(module = "tomcat-jdbc")
  }
}

dependencies {
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
  annotationProcessor("org.projectlombok:lombok:1.18.34")
  testAnnotationProcessor("org.projectlombok:lombok:1.18.34")

  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:1.0.3")
  implementation("org.springframework.boot:spring-boot-starter-aop")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("org.springframework.boot:spring-boot-starter-jdbc")
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  developmentOnly("org.springframework.boot:spring-boot-devtools")

  implementation("commons-codec:commons-codec:1.17.1")
  implementation("com.github.jsqlparser:jsqlparser:5.0")
  implementation("org.ehcache:ehcache:3.10.8")
  implementation("com.zaxxer:HikariCP:5.1.0")

  implementation("io.swagger:swagger-annotations:1.6.14")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")

  implementation("org.apache.commons:commons-lang3:3.15.0")
  implementation("commons-io:commons-io:2.16.1")
  implementation("com.google.guava:guava:33.2.0-jre")
  implementation("org.apache.commons:commons-text:1.12.0")
  implementation("com.oracle.database.jdbc:ojdbc10:19.23.0.0")
  implementation("org.hibernate.orm:hibernate-community-dialects")

  compileOnly("org.projectlombok:lombok:1.18.34")

  // we run on oracle in all environments, but allow instance to be started using hsqldb too
  runtimeOnly("org.hsqldb:hsqldb:2.7.3")
  runtimeOnly("org.flywaydb:flyway-database-hsqldb")

  testImplementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter-test:1.0.3")
  testImplementation("org.springframework.boot:spring-boot-test-autoconfigure")
  testImplementation("org.springframework.boot:spring-boot-starter-webflux")
  testImplementation("io.rest-assured:rest-assured:5.5.0")
  testImplementation("io.rest-assured:json-schema-validator:5.5.0")
  testImplementation("io.rest-assured:spring-mock-mvc:5.5.0")
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("com.google.code.gson:gson:2.11.0")
  testImplementation("org.mockito:mockito-inline:5.2.0")
  testImplementation("org.powermock:powermock-api-mockito2:2.0.9")
  testImplementation("org.powermock:powermock-module-junit4:2.0.9")

  testImplementation("com.tngtech.java:junit-dataprovider:1.13.1")
  testImplementation("net.javacrumbs.json-unit:json-unit-assertj:3.4.1")

  testImplementation("net.serenity-bdd:serenity-core:4.1.20")
  testImplementation("net.serenity-bdd:serenity-junit:4.1.20")
  testImplementation("net.serenity-bdd:serenity-spring:4.1.20")
  testImplementation("net.serenity-bdd:serenity-cucumber:4.1.20")
  testImplementation("com.paulhammant:ngwebdriver:1.2")
  testImplementation("org.wiremock:wiremock:3.9.1")
  testImplementation("io.swagger.parser.v3:swagger-parser:2.1.22") {
    exclude(group = "io.swagger.core.v3")
  }
  testImplementation("io.swagger.core.v3:swagger-core-jakarta:2.2.22")
  testImplementation("commons-beanutils:commons-beanutils:1.9.4")
  testImplementation("io.opentelemetry:opentelemetry-sdk-testing:1.40.0")

  testCompileOnly("org.projectlombok:lombok:1.18.34")
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions.jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
    compilerOptions.freeCompilerArgs = listOf("-Xjvm-default=all")
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
