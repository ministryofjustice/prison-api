plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "4.4.2"
  kotlin("plugin.spring") version "1.7.10"
  kotlin("plugin.jpa") version "1.7.10"
  kotlin("plugin.lombok") version "1.7.10"
}

configurations {
  implementation {
    exclude(module = "commons-logging")
    exclude(module = "log4j")
    exclude(module = "c3p0")
    exclude(module = "tomcat-jdbc")
  }
}

// Temporarily kept at 4.3 due to bug in 4.4 parser
val jsqlParserVersion by extra("4.3")

// Temporarily keep at 2.5.1 until can switch to h2 instead (tests break anyway with 2.6.1)
val hsqldbVersion by extra("2.5.1")

ext["rest-assured.version"] = "5.1.1"

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
  implementation("org.springdoc:springdoc-openapi-ui:1.6.11")
  implementation("org.springdoc:springdoc-openapi-kotlin:1.6.11")
  implementation("org.springdoc:springdoc-openapi-data-rest:1.6.11")

  implementation("org.apache.commons:commons-lang3:3.12.0")
  implementation("commons-io:commons-io:2.11.0")
  implementation("org.apache.commons:commons-text:1.9")
  implementation("com.oracle.database.jdbc:ojdbc10:19.15.0.0.1")

  compileOnly("org.projectlombok:lombok:1.18.24")

  runtimeOnly("org.hsqldb:hsqldb:$hsqldbVersion")
  runtimeOnly("org.flywaydb:flyway-core")

  testImplementation("org.springframework.boot:spring-boot-test-autoconfigure")
  testImplementation("org.springframework.boot:spring-boot-starter-webflux")
  testImplementation("io.rest-assured:rest-assured:5.1.1")
  testImplementation("io.rest-assured:json-schema-validator:5.1.1")
  testImplementation("io.rest-assured:spring-mock-mvc:5.1.1")
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("com.google.code.gson:gson:2.9.1")
  testImplementation("org.powermock:powermock-api-mockito2:2.0.9")
  testImplementation("org.powermock:powermock-module-junit4:2.0.9")

  testImplementation("com.tngtech.java:junit-dataprovider:1.13.1")
  testImplementation("net.javacrumbs.json-unit:json-unit-assertj:2.35.0")

  testImplementation("net.serenity-bdd:serenity-core:3.3.2")
  testImplementation("net.serenity-bdd:serenity-junit:3.3.2")
  testImplementation("net.serenity-bdd:serenity-spring:3.3.2")
  testImplementation("net.serenity-bdd:serenity-cucumber:3.3.2")
  testImplementation("com.paulhammant:ngwebdriver:1.2")
  testImplementation("com.github.tomakehurst:wiremock-standalone:2.27.2")
  testImplementation("io.jsonwebtoken:jjwt:0.9.1")
  testImplementation("org.glassfish:javax.el:3.0.0")
  testImplementation("io.swagger.parser.v3:swagger-parser:2.1.2")

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

  // Since Gradle 7.5 `--add-opens` is not automatically added to test workers. This broke a test in `InfoIntTest` because ehcache v2 uses reflection to retrieve the heap size.
  // As we add `--add-opens` to the JVM args in `run.sh` it seems safe to also add these to the test workers.
  // For more info. on the Gradle change see https://docs.gradle.org/7.5/userguide/upgrading_version_7.html#removes_implicit_add_opens_for_test_workers
  withType<Test> {
    jvmArgs(listOf("--add-opens=java.base/java.lang=ALL-UNNAMED", "--add-opens=java.base/java.util=ALL-UNNAMED"))
  }
}
