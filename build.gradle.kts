plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "4.0.4"
  kotlin("plugin.spring") version "1.6.10"
  kotlin("plugin.jpa") version "1.6.10"
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

dependencies {
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
  annotationProcessor("org.projectlombok:lombok:1.18.22")
  testAnnotationProcessor("org.projectlombok:lombok:1.18.22")

  implementation("org.springframework.boot:spring-boot-starter-aop")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("org.springframework.boot:spring-boot-starter-jdbc")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-cache")
  developmentOnly("org.springframework.boot:spring-boot-devtools")

  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

  implementation(platform("com.amazonaws:aws-java-sdk-bom:1.12.163"))

  implementation("javax.annotation:javax.annotation-api:1.3.2")
  implementation("javax.xml.bind:jaxb-api:2.3.1")
  implementation("com.sun.xml.bind:jaxb-impl:3.0.2")
  implementation("com.sun.xml.bind:jaxb-core:3.0.2")
  implementation("javax.activation:activation:1.1.1")

  implementation("commons-codec:commons-codec:1.15")
  implementation("com.github.jsqlparser:jsqlparser:4.3")
  implementation("net.sf.ehcache:ehcache:2.10.9.2")
  implementation("com.zaxxer:HikariCP:5.0.1")

  implementation("org.springdoc:springdoc-openapi-ui:1.6.6")
  implementation("org.springdoc:springdoc-openapi-kotlin:1.6.6")
  implementation("org.springdoc:springdoc-openapi-data-rest:1.6.6")

  implementation("org.apache.commons:commons-lang3:3.12.0")
  implementation("commons-io:commons-io:2.11.0")
  implementation("org.apache.commons:commons-text:1.9")
  implementation("com.oracle.database.jdbc:ojdbc10:19.13.0.0.1")

  compileOnly("org.projectlombok:lombok:1.18.22")

  runtimeOnly("org.hsqldb:hsqldb:2.5.1")
  runtimeOnly("org.flywaydb:flyway-core:8.5.0")

  testImplementation("org.springframework.boot:spring-boot-test-autoconfigure")
  testImplementation("io.rest-assured:rest-assured:4.5.1")
  testImplementation("io.rest-assured:json-schema-validator:4.5.1")
  testImplementation("io.rest-assured:spring-mock-mvc:4.5.1")
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("com.google.code.gson:gson:2.9.0")
  testImplementation("org.powermock:powermock-api-mockito2:2.0.9")
  testImplementation("org.powermock:powermock-module-junit4:2.0.9")

  testImplementation("com.tngtech.java:junit-dataprovider:1.13.1")
  testImplementation("net.javacrumbs.json-unit:json-unit-assertj:2.32.0")

  testImplementation("net.serenity-bdd:serenity-core:3.2.0")
  testImplementation("net.serenity-bdd:serenity-junit:3.2.0")
  testImplementation("net.serenity-bdd:serenity-spring:3.2.0")
  testImplementation("net.serenity-bdd:serenity-cucumber:3.2.0")
  testImplementation("com.paulhammant:ngwebdriver:1.1.6")
  testImplementation("org.slf4j:slf4j-api:1.7.36")
  testImplementation("com.github.tomakehurst:wiremock-standalone:2.27.2")
  testImplementation("io.jsonwebtoken:jjwt:0.9.1")
  testImplementation("org.glassfish:javax.el:3.0.0")
  testImplementation("io.swagger.parser.v3:swagger-parser:2.0.30")

  testCompileOnly("org.projectlombok:lombok:1.18.22")
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
      freeCompilerArgs = listOf("-Xjvm-default=all")
      jvmTarget = "17"
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
      jvmTarget = "17"
    }
  }

  named("test") { dependsOn("testIntegration", "testWithSchemaNomis") }
}
