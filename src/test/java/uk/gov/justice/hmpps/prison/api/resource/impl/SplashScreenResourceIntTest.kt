package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import uk.gov.justice.hmpps.prison.api.model.BlockAccessType
import uk.gov.justice.hmpps.prison.api.model.RequestSplashConditionUpdate
import uk.gov.justice.hmpps.prison.api.model.RequestSplashScreenCreateOrUpdate
import uk.gov.justice.hmpps.prison.repository.jpa.model.SplashCondition
import uk.gov.justice.hmpps.prison.repository.jpa.model.SplashScreen
import uk.gov.justice.hmpps.prison.repository.jpa.model.SplashScreenFunction
import uk.gov.justice.hmpps.prison.repository.jpa.repository.SplashConditionRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.SplashScreenFunctionRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.SplashScreenRepository

class SplashScreenResourceIntTest : ResourceTest() {

  @Autowired
  private lateinit var splashScreenRepository: SplashScreenRepository

  @Autowired
  private lateinit var splashScreenFunctionRepository: SplashScreenFunctionRepository

  @Autowired
  private lateinit var splashConditionRepository: SplashConditionRepository

  private lateinit var aFunction: SplashScreenFunction
  private lateinit var splashScreen1: SplashScreen
  private lateinit var splashScreen2: SplashScreen

  @BeforeEach
  fun `set up`() {
    // Create test functions
    aFunction = splashScreenFunctionRepository.save(
      SplashScreenFunction(
        functionName = "AFUNCTION",
        description = "Some PL/SQL function",
      ),
    )

    // Create test splash screens
    splashScreen1 = splashScreenRepository.save(
      SplashScreen(
        moduleName = "OIDCHOLO",
        function = aFunction,
        warningText = "This service is currently under maintenance.",
        blockedText = "Access to this service is currently restricted.",
        blockAccessType = BlockAccessType.NO,
      ),
    )

    splashScreen2 = splashScreenRepository.save(
      SplashScreen(
        moduleName = "ASCREEN",
        function = null,
        warningText = "This service is currently under maintenance.",
        blockedText = null,
        blockAccessType = BlockAccessType.NO,
      ),
    )

    // Create test conditions
    splashConditionRepository.save(
      SplashCondition(
        splashScreen = splashScreen1,
        conditionType = "CASELOAD",
        conditionValue = "MDI",
        blockAccess = false,
      ),
    )

    splashConditionRepository.save(
      SplashCondition(
        splashScreen = splashScreen1,
        conditionType = "CASELOAD",
        conditionValue = "LEI",
        blockAccess = true,
      ),
    )

    splashConditionRepository.save(
      SplashCondition(
        splashScreen = splashScreen2,
        conditionType = "USER_ROLE",
        conditionValue = "ADJUDICATIONS_REVIEWER",
        blockAccess = false,
      ),
    )
  }

  @AfterEach
  fun `tear down`() {
    splashConditionRepository.deleteAll()
    splashScreenRepository.deleteAll()
    splashScreenFunctionRepository.deleteAll()
  }

  @Nested
  inner class GetAllSplashScreens {
    @Test
    fun `should return unauthorised without an auth token`() {
      webTestClient.get()
        .uri("/api/splash-screen")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return forbidden without a valid role`() {
      webTestClient.get()
        .uri("/api/splash-screen")
        .headers(setAuthorisation(listOf("ROLE_INVALID")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return all splash screens`() {
      webTestClient.get()
        .uri("/api/splash-screen")
        .headers(setAuthorisation(listOf("PRISON_API__SPLASH_SCREEN__RO")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$[*].moduleName").value<List<String>> {
          assertThat(it).containsExactlyInAnyOrder("OIDCHOLO", "ASCREEN")
        }
    }
  }

  @Nested
  inner class GetSplashScreenByModuleName {
    @Test
    fun `should return unauthorised without an auth token`() {
      webTestClient.get()
        .uri("/api/splash-screen/OIDCHOLO")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return forbidden without a valid role`() {
      webTestClient.get()
        .uri("/api/splash-screen/OIDCHOLO")
        .headers(setAuthorisation(listOf("ROLE_INVALID")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return not found if module name does not exist`() {
      webTestClient.get()
        .uri("/api/splash-screen/INVALID")
        .headers(setAuthorisation(listOf("PRISON_API__SPLASH_SCREEN__RO")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").value<String> {
          assertThat(it).contains("Splash screen not found for module: INVALID")
        }
    }

    @Test
    fun `should return splash screen with function and conditions`() {
      webTestClient.get()
        .uri("/api/splash-screen/OIDCHOLO")
        .headers(setAuthorisation(listOf("PRISON_API__SPLASH_SCREEN__RO")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("moduleName").isEqualTo("OIDCHOLO")
        .jsonPath("functionName").isEqualTo("AFUNCTION")
        .jsonPath("function.description").isEqualTo("Some PL/SQL function")
        .jsonPath("warningText").isEqualTo("This service is currently under maintenance.")
        .jsonPath("blockedText").isEqualTo("Access to this service is currently restricted.")
        .jsonPath("blockAccessType").isEqualTo(BlockAccessType.NO)
        .jsonPath("conditions[*].conditionType").value<List<String>> {
          assertThat(it).containsExactlyInAnyOrder("CASELOAD", "CASELOAD")
        }
        .jsonPath("conditions[*].conditionValue").value<List<String>> {
          assertThat(it).containsExactlyInAnyOrder("MDI", "LEI")
        }
    }

    @Test
    fun `should return splash screen without function`() {
      webTestClient.get()
        .uri("/api/splash-screen/ASCREEN")
        .headers(setAuthorisation(listOf("PRISON_API__SPLASH_SCREEN__RO")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("moduleName").isEqualTo("ASCREEN")
        .jsonPath("functionName").doesNotExist()
        .jsonPath("function").doesNotExist()
        .jsonPath("warningText").isEqualTo("This service is currently under maintenance.")
        .jsonPath("blockedText").doesNotExist()
        .jsonPath("blockAccessType").isEqualTo(BlockAccessType.NO)
        .jsonPath("conditions[*].conditionType").value<List<String>> {
          assertThat(it).containsExactly("USER_ROLE")
        }
        .jsonPath("conditions[*].conditionValue").value<List<String>> {
          assertThat(it).containsExactly("ADJUDICATIONS_REVIEWER")
        }
    }
  }

  @Nested
  inner class GetSplashScreensByCondition {
    @Test
    fun `should return unauthorised without an auth token`() {
      webTestClient.get()
        .uri("/api/splash-screen/condition?conditionType=CASELOAD&conditionValue=MDI")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return forbidden without a valid role`() {
      webTestClient.get()
        .uri("/api/splash-screen/condition?conditionType=CASELOAD&conditionValue=MDI")
        .headers(setAuthorisation(listOf("ROLE_INVALID")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return splash screens with matching condition`() {
      webTestClient.get()
        .uri("/api/splash-screen/condition/CASELOAD/MDI")
        .headers(setAuthorisation(listOf("PRISON_API__SPLASH_SCREEN__RO")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$[*].moduleName").value<List<String>> {
          assertThat(it).containsExactly("OIDCHOLO")
        }
    }

    @Test
    fun `should return empty list if no splash screens match condition`() {
      webTestClient.get()
        .uri("/api/splash-screen/condition/CASELOAD/BXI")
        .headers(setAuthorisation(listOf("PRISON_API__SPLASH_SCREEN__RO")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$").isArray
        .jsonPath("$").isEmpty
    }
  }

  @Nested
  inner class CreateSplashScreen {
    @Test
    fun `should return unauthorised without an auth token`() {
      webTestClient.post()
        .uri("/api/splash-screen/NEW_MODULE")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
          RequestSplashScreenCreateOrUpdate(
            functionName = "AFUNCTION",
            warningText = "Warning",
            blockAccessType = BlockAccessType.NO,
          ),
        )
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return forbidden without a valid role`() {
      webTestClient.post()
        .uri("/api/splash-screen/NEW_MODULE")
        .headers(setAuthorisation(listOf("ROLE_INVALID")))
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
          RequestSplashScreenCreateOrUpdate(
            functionName = "AFUNCTION",
            warningText = "Warning",
            blockAccessType = BlockAccessType.NO,
          ),
        )
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return not found if function does not exist`() {
      webTestClient.post()
        .uri("/api/splash-screen/NEW_MODULE")
        .headers(setAuthorisation(listOf("PRISON_API__SPLASH_SCREEN__RW")))
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
          RequestSplashScreenCreateOrUpdate(
            functionName = "INVALID_FUNCTION",
            warningText = "Warning",
            blockAccessType = BlockAccessType.NO,
          ),
        )
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").value<String> {
          assertThat(it).contains("Function not found: INVALID_FUNCTION")
        }
    }

    @Test
    fun `should return conflict if module name already exists`() {
      webTestClient.post()
        .uri("/api/splash-screen/OIDCHOLO")
        .headers(setAuthorisation(listOf("PRISON_API__SPLASH_SCREEN__RW")))
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
          RequestSplashScreenCreateOrUpdate(
            functionName = "AFUNCTION",
            warningText = "Warning",
            blockAccessType = BlockAccessType.NO,
          ),
        )
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isEqualTo(409)
        .expectBody().jsonPath("userMessage").value<String> {
          assertThat(it).contains("Splash screen with module name OIDCHOLO already exists")
        }
    }

    @Test
    fun `should create splash screen`() {
      webTestClient.post()
        .uri("/api/splash-screen/NEW_MODULE")
        .headers(setAuthorisation(listOf("PRISON_API__SPLASH_SCREEN__RW")))
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
          RequestSplashScreenCreateOrUpdate(
            functionName = "AFUNCTION",
            warningText = "Warning",
            blockedText = "Blocked",
            blockAccessType = BlockAccessType.NO,
            conditions = listOf(
              RequestSplashConditionUpdate(
                conditionType = "CASELOAD",
                conditionValue = "BXI",
                blockAccess = false,
              ),
            ),
          ),
        )
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isCreated
        .expectBody()
        .jsonPath("moduleName").isEqualTo("NEW_MODULE")
        .jsonPath("functionName").isEqualTo("AFUNCTION")
        .jsonPath("warningText").isEqualTo("Warning")
        .jsonPath("blockedText").isEqualTo("Blocked")
        .jsonPath("blockAccessType").isEqualTo(BlockAccessType.NO)
        .jsonPath("conditions[0].conditionType").isEqualTo("CASELOAD")
        .jsonPath("conditions[0].conditionValue").isEqualTo("BXI")
        .jsonPath("conditions[0].blockAccess").isEqualTo("false")

      // Verify splash screen was created in the database
      val splashScreen = splashScreenRepository.findByModuleName("NEW_MODULE")
      assertThat(splashScreen).isNotNull
      assertThat(splashScreen!!.function).isEqualTo(aFunction)
      assertThat(splashScreen.warningText).isEqualTo("Warning")
      assertThat(splashScreen.blockedText).isEqualTo("Blocked")
      assertThat(splashScreen.blockAccessType).isEqualTo(BlockAccessType.NO)

      // Verify condition was created in the database
      val conditions = splashConditionRepository.findBySplashScreen(splashScreen)
      assertThat(conditions).hasSize(1)
      assertThat(conditions[0].conditionType).isEqualTo("CASELOAD")
      assertThat(conditions[0].conditionValue).isEqualTo("BXI")
      assertThat(conditions[0].blockAccess).isFalse()
    }
  }

  @Nested
  inner class UpdateSplashScreen {
    @Test
    fun `should return unauthorised without an auth token`() {
      webTestClient.put()
        .uri("/api/splash-screen/OIDCHOLO")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
          RequestSplashScreenCreateOrUpdate(
            warningText = "Updated warning",
            blockAccessType = BlockAccessType.YES,
          ),
        )
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return forbidden without a valid role`() {
      webTestClient.put()
        .uri("/api/splash-screen/OIDCHOLO")
        .headers(setAuthorisation(listOf("ROLE_INVALID")))
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
          RequestSplashScreenCreateOrUpdate(
            warningText = "Updated warning",
            blockAccessType = BlockAccessType.YES,
          ),
        )
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return not found if module name does not exist`() {
      webTestClient.put()
        .uri("/api/splash-screen/INVALID")
        .headers(setAuthorisation(listOf("PRISON_API__SPLASH_SCREEN__RW")))
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
          RequestSplashScreenCreateOrUpdate(
            warningText = "Updated warning",
            blockAccessType = BlockAccessType.YES,
          ),
        )
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").value<String> {
          assertThat(it).contains("Splash screen not found for module: INVALID")
        }
    }

    @Test
    fun `should update splash screen`() {
      webTestClient.put()
        .uri("/api/splash-screen/OIDCHOLO")
        .headers(setAuthorisation(listOf("PRISON_API__SPLASH_SCREEN__RW")))
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
          RequestSplashScreenCreateOrUpdate(
            warningText = "Updated warning",
            blockedText = "Updated blocked text",
            blockAccessType = BlockAccessType.YES,
          ),
        )
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("moduleName").isEqualTo("OIDCHOLO")
        .jsonPath("warningText").isEqualTo("Updated warning")
        .jsonPath("blockedText").isEqualTo("Updated blocked text")
        .jsonPath("blockAccessType").isEqualTo(BlockAccessType.YES)

      // Verify splash screen was updated in the database
      val splashScreen = splashScreenRepository.findByModuleName("OIDCHOLO")
      assertThat(splashScreen).isNotNull
      assertThat(splashScreen!!.warningText).isEqualTo("Updated warning")
      assertThat(splashScreen.blockedText).isEqualTo("Updated blocked text")
      assertThat(splashScreen.blockAccessType).isEqualTo(BlockAccessType.YES)
    }
  }

  @Nested
  inner class DeleteSplashScreen {
    @Test
    fun `should return unauthorised without an auth token`() {
      webTestClient.delete()
        .uri("/api/splash-screen/OIDCHOLO")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return forbidden without a valid role`() {
      webTestClient.delete()
        .uri("/api/splash-screen/OIDCHOLO")
        .headers(setAuthorisation(listOf("ROLE_INVALID")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return not found if module name does not exist`() {
      webTestClient.delete()
        .uri("/api/splash-screen/INVALID")
        .headers(setAuthorisation(listOf("PRISON_API__SPLASH_SCREEN__RW")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").value<String> {
          assertThat(it).contains("Splash screen not found for module: INVALID")
        }
    }

    @Test
    fun `should delete splash screen and its conditions`() {
      webTestClient.delete()
        .uri("/api/splash-screen/OIDCHOLO")
        .headers(setAuthorisation(listOf("PRISON_API__SPLASH_SCREEN__RW")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNoContent

      // Verify splash screen was deleted from the database
      val splashScreen = splashScreenRepository.findByModuleName("OIDCHOLO")
      assertThat(splashScreen).isNull()

      // Verify conditions were deleted from the database
      val conditions = splashConditionRepository.findAll()
      assertThat(conditions.filter { it.splashScreen.moduleName == "OIDCHOLO" }).isEmpty()
    }
  }

  @Nested
  inner class AddCondition {
    @Test
    fun `should return unauthorised without an auth token`() {
      webTestClient.post()
        .uri("/api/splash-screen/OIDCHOLO/condition")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
          RequestSplashConditionUpdate(
            conditionType = "USER_ROLE",
            conditionValue = "VISIT_SCHEDULER",
            blockAccess = false,
          ),
        )
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return forbidden without a valid role`() {
      webTestClient.post()
        .uri("/api/splash-screen/OIDCHOLO/condition")
        .headers(setAuthorisation(listOf("ROLE_INVALID")))
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
          RequestSplashConditionUpdate(
            conditionType = "USER_ROLE",
            conditionValue = "VISIT_SCHEDULER",
            blockAccess = false,
          ),
        )
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return not found if module name does not exist`() {
      webTestClient.post()
        .uri("/api/splash-screen/INVALID/condition")
        .headers(setAuthorisation(listOf("PRISON_API__SPLASH_SCREEN__RW")))
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
          RequestSplashConditionUpdate(
            conditionType = "USER_ROLE",
            conditionValue = "VISIT_SCHEDULER",
            blockAccess = false,
          ),
        )
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").value<String> {
          assertThat(it).contains("Splash screen not found for module: INVALID")
        }
    }

    @Test
    fun `should return conflict if condition already exists`() {
      webTestClient.post()
        .uri("/api/splash-screen/OIDCHOLO/condition")
        .headers(setAuthorisation(listOf("PRISON_API__SPLASH_SCREEN__RW")))
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
          RequestSplashConditionUpdate(
            conditionType = "CASELOAD",
            conditionValue = "MDI",
            blockAccess = false,
          ),
        )
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isEqualTo(409)
        .expectBody().jsonPath("userMessage").value<String> {
          assertThat(it).contains("Condition already exists for this splash screen")
        }
    }

    @Test
    fun `should add condition to splash screen`() {
      webTestClient.post()
        .uri("/api/splash-screen/OIDCHOLO/condition")
        .headers(setAuthorisation(listOf("PRISON_API__SPLASH_SCREEN__RW")))
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
          RequestSplashConditionUpdate(
            conditionType = "USER_ROLE",
            conditionValue = "VISIT_SCHEDULER",
            blockAccess = false,
          ),
        )
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("moduleName").isEqualTo("OIDCHOLO")
        .jsonPath("conditions[*].conditionType").value<List<String>> {
          assertThat(it).contains("USER_ROLE")
        }
        .jsonPath("conditions[*].conditionValue").value<List<String>> {
          assertThat(it).contains("VISIT_SCHEDULER")
        }

      // Verify condition was added to the database
      val splashScreen = splashScreenRepository.findByModuleName("OIDCHOLO")
      val conditions = splashConditionRepository.findBySplashScreen(splashScreen!!)
      assertThat(conditions.filter { it.conditionType == "USER_ROLE" && it.conditionValue == "VISIT_SCHEDULER" }).hasSize(1)
    }
  }

  @Nested
  inner class UpdateCondition {
    @Test
    fun `should return unauthorised without an auth token`() {
      webTestClient.put()
        .uri("/api/splash-screen/OIDCHOLO/condition/CASELOAD/MDI/true")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return forbidden without a valid role`() {
      webTestClient.put()
        .uri("/api/splash-screen/OIDCHOLO/condition/CASELOAD/MDI/true")
        .headers(setAuthorisation(listOf("ROLE_INVALID")))
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return not found if module name does not exist`() {
      webTestClient.put()
        .uri("/api/splash-screen/INVALID/condition/CASELOAD/MDI/true")
        .headers(setAuthorisation(listOf("PRISON_API__SPLASH_SCREEN__RW")))
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").value<String> {
          assertThat(it).contains("Splash screen not found for module: INVALID")
        }
    }

    @Test
    fun `should return not found if condition does not exist`() {
      webTestClient.put()
        .uri("/api/splash-screen/OIDCHOLO/condition/INVALID_TYPE/INVALID_VALUE/true")
        .headers(setAuthorisation(listOf("PRISON_API__SPLASH_SCREEN__RW")))
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").value<String> {
          assertThat(it).contains("Condition not found")
        }
    }

    @Test
    fun `should update condition`() {
      webTestClient.put()
        .uri("/api/splash-screen/OIDCHOLO/condition/CASELOAD/MDI/true")
        .headers(setAuthorisation(listOf("PRISON_API__SPLASH_SCREEN__RW")))
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("moduleName").isEqualTo("OIDCHOLO")
        .jsonPath("conditions[?(@.conditionType=='CASELOAD' && @.conditionValue=='MDI')].blockAccess").isEqualTo(true)

      // Verify condition was updated in the database
      val splashScreen = splashScreenRepository.findByModuleName("OIDCHOLO")
      val condition = splashConditionRepository.findBySplashScreenAndConditionTypeAndConditionValue(
        splashScreen!!,
        "CASELOAD",
        "MDI",
      )
      assertThat(condition).isNotNull
      assertThat(condition!!.blockAccess).isTrue()
    }
  }

  @Nested
  inner class RemoveCondition {
    @Test
    fun `should return unauthorised without an auth token`() {
      webTestClient.delete()
        .uri("/api/splash-screen/OIDCHOLO/condition/CASELOAD/MDI")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return forbidden without a valid role`() {
      webTestClient.delete()
        .uri("/api/splash-screen/OIDCHOLO/condition/CASELOAD/MDI")
        .headers(setAuthorisation(listOf("ROLE_INVALID")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return not found if module name does not exist`() {
      webTestClient.delete()
        .uri("/api/splash-screen/INVALID/condition/CASELOAD/MDI")
        .headers(setAuthorisation(listOf("PRISON_API__SPLASH_SCREEN__RW")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").value<String> {
          assertThat(it).contains("Splash screen not found for module: INVALID")
        }
    }

    @Test
    fun `should return not found if condition does not exist`() {
      webTestClient.delete()
        .uri("/api/splash-screen/OIDCHOLO/condition/INVALID_TYPE/INVALID_VALUE")
        .headers(setAuthorisation(listOf("PRISON_API__SPLASH_SCREEN__RW")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").value<String> {
          assertThat(it).contains("Condition not found")
        }
    }

    @Test
    fun `should remove condition`() {
      webTestClient.delete()
        .uri("/api/splash-screen/OIDCHOLO/condition/CASELOAD/MDI")
        .headers(setAuthorisation(listOf("PRISON_API__SPLASH_SCREEN__RW")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("moduleName").isEqualTo("OIDCHOLO")
        .jsonPath("conditions[*].conditionValue").value<List<String>> {
          assertThat(it).doesNotContain("MDI")
        }

      // Verify condition was removed from the database
      val splashScreen = splashScreenRepository.findByModuleName("OIDCHOLO")
      val condition = splashConditionRepository.findBySplashScreenAndConditionTypeAndConditionValue(
        splashScreen!!,
        "CASELOAD",
        "MDI",
      )
      assertThat(condition).isNull()
    }
  }

  @Nested
  inner class GetConditionsByType {
    @Test
    fun `should return unauthorised without an auth token`() {
      webTestClient.get()
        .uri("/api/splash-screen/OIDCHOLO/condition/CASELOAD")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return forbidden without a valid role`() {
      webTestClient.get()
        .uri("/api/splash-screen/OIDCHOLO/condition/CASELOAD")
        .headers(setAuthorisation(listOf("ROLE_INVALID")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return not found if module name does not exist`() {
      webTestClient.get()
        .uri("/api/splash-screen/INVALID/condition/CASELOAD")
        .headers(setAuthorisation(listOf("PRISON_API__SPLASH_SCREEN__RO")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").value<String> {
          assertThat(it).contains("Splash screen not found for module: INVALID")
        }
    }

    @Test
    fun `should return conditions of specified type`() {
      webTestClient.get()
        .uri("/api/splash-screen/OIDCHOLO/condition/CASELOAD")
        .headers(setAuthorisation(listOf("PRISON_API__SPLASH_SCREEN__RO")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$[*].conditionType").value<List<String>> {
          assertThat(it).containsOnly("CASELOAD")
        }
        .jsonPath("$[*].conditionValue").value<List<String>> {
          assertThat(it).containsExactlyInAnyOrder("MDI", "LEI")
        }
    }

    @Test
    fun `should return empty list if no conditions of specified type`() {
      webTestClient.get()
        .uri("/api/splash-screen/OIDCHOLO/condition/INVALID_TYPE")
        .headers(setAuthorisation(listOf("PRISON_API__SPLASH_SCREEN__RO")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$").isArray
        .jsonPath("$").isEmpty
    }
  }
}
