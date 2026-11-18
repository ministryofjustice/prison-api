package uk.gov.justice.hmpps.prison.api.resource.impl

import jakarta.persistence.EntityManagerFactory
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.hibernate.SessionFactory
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.hmpps.prison.repository.jpa.model.WorkflowScreen.PK
import uk.gov.justice.hmpps.prison.repository.jpa.repository.WorkflowScreenRepository

@Sql("/sql/workflow_configuration_init.sql")
@Sql(scripts = ["/sql/workflow_configuration_clean.sql"], executionPhase = AFTER_TEST_METHOD)
class WorkflowConfigurationResourceTest : ResourceTest() {
  @Autowired
  private lateinit var screenRepository: WorkflowScreenRepository

  @Autowired
  private lateinit var entityManagerFactory: EntityManagerFactory

  @ParameterizedTest
  @ValueSource(strings = ["workflow-folders", "workflow-screens"])
  fun `requires authentication`(route: String) {
    webTestClient.get().uri("/api/$route")
      .exchange()
      .expectStatus().isUnauthorized
      .expectBody().isEmpty
  }

  @ParameterizedTest
  @ValueSource(strings = ["workflow-folders", "workflow-screens"])
  fun `requires the global read role`(route: String) {
    webTestClient.get().uri("/api/$route")
      .headers(setClientAuthorisation(listOf()))
      .exchange()
      .expectStatus().isForbidden
      .expectBody()
      .jsonPath("status").isEqualTo(403)
      .jsonPath("userMessage").isEqualTo("Access Denied")
  }

  @ParameterizedTest
  @ValueSource(strings = ["workflow-folders", "workflow-screens"])
  fun `requires read scope even with the global role`(route: String) {
    val token = jwtAuthenticationHelper.createJwtAccessToken(
      roles = listOf("VIEW_PRISONER_DATA"),
      scope = listOf("write"),
    )
    webTestClient.get().uri("/api/$route")
      .headers { it.setBearerAuth(token) }
      .exchange()
      .expectStatus().isForbidden
  }

  @ParameterizedTest
  @ValueSource(strings = ["workflow-folders", "workflow-screens"])
  fun `returns all rows with standard page metadata`(route: String) {
    getPage(route)
      .jsonPath("content.length()").isEqualTo(3)
      .jsonPath("number").isEqualTo(0)
      .jsonPath("size").isEqualTo(500)
      .jsonPath("numberOfElements").isEqualTo(3)
      .jsonPath("totalElements").isEqualTo(3)
      .jsonPath("totalPages").isEqualTo(1)
      .jsonPath("last").isEqualTo(true)
    if (route == "workflow-folders") {
      getPage(route).jsonPath("content[2].workflowCode").isEqualTo("WFX_C")
    }
  }

  @ParameterizedTest
  @CsvSource("workflow-folders,1", "workflow-screens,0")
  fun `returns every source field without trimming strings or truncating timestamps`(route: String, index: Int) {
    val expected = mapOf(
      "workflowCode" to if (route == "workflow-folders") "WFX_B" else "WFX_A",
      "description" to " workflow export ",
      "workflowSeq" to 0,
      "caseloadType" to "INST",
      "createDatetime" to "2020-01-01T10:11:12.123456",
      "createUserId" to "WF_CREATE",
      "modifyDatetime" to "2020-02-02T11:12:13.234567",
      "modifyUserId" to "WF_MODIFY",
      "auditTimestamp" to "2020-03-03T12:13:14.345678",
      "auditUserId" to "WF_AUDIT",
      "auditModuleName" to "WFX_MODULE",
      "auditClientUserId" to "WF_CLIENT",
      "auditClientIpAddress" to "192.0.2.1",
      "auditClientWorkstationName" to "WF_WORKSTATION",
      "auditAdditionalInfo" to " export metadata ",
    ) + if (route == "workflow-screens") mapOf("moduleName" to "WFX_MOD_A") else emptyMap()

    getPage(route).jsonPath("content[$index]").isEqualTo(expected)
  }

  @ParameterizedTest
  @CsvSource("workflow-folders,0", "workflow-screens,1")
  fun `returns nullable fields explicitly as null`(route: String, index: Int) {
    val body = getPage(route).returnResult().responseBody!!
    val row = jsonMapper.readTree(body)["content"][index]
    val nullableFields = listOf(
      "description", "workflowSeq", "caseloadType", "modifyDatetime", "modifyUserId",
      "auditTimestamp", "auditUserId", "auditModuleName", "auditClientUserId",
      "auditClientIpAddress", "auditClientWorkstationName", "auditAdditionalInfo",
    )
    nullableFields.forEach { field ->
      assertThat(row.has(field)).describedAs("$field is present").isTrue()
      assertThat(row[field].isNull).describedAs("$field is null").isTrue()
    }
    assertThat(row.size()).isEqualTo(if (route == "workflow-folders") 15 else 16)
    assertThat(row["createDatetime"].stringValue()).isEqualTo("2020-01-01T10:11:12.123456")
    assertThat(row["createUserId"].stringValue()).isEqualTo("WF_CREATE")
  }

  @ParameterizedTest
  @ValueSource(strings = ["workflow-folders", "workflow-screens"])
  fun `small pages return every composite key once in fixed order`(route: String) {
    val actual = mutableListOf<String>()
    for (page in 0..1) {
      val body = getPage(route, "?page=$page&size=2&sort=workflowCode,desc")
        .jsonPath("number").isEqualTo(page)
        .jsonPath("size").isEqualTo(2)
        .jsonPath("totalElements").isEqualTo(3)
        .jsonPath("totalPages").isEqualTo(2)
        .jsonPath("last").isEqualTo(page == 1)
        .returnResult().responseBody!!
      val content = jsonMapper.readTree(body)["content"]
      for (index in 0 until content.size()) {
        val row = content[index]
        actual += if (route == "workflow-folders") {
          row["workflowCode"].stringValue()
        } else {
          "${row["workflowCode"].stringValue()}/${row["moduleName"].stringValue()}"
        }
      }
    }
    val expected = if (route == "workflow-folders") {
      listOf("WFX_A", "WFX_B", "WFX_C")
    } else {
      listOf("WFX_A/WFX_MOD_A", "WFX_A/WFX_MOD_Z", "WFX_B/WFX_MOD_A")
    }
    assertThat(actual).containsExactlyElementsOf(expected)
  }

  @ParameterizedTest
  @ValueSource(strings = ["workflow-folders", "workflow-screens"])
  fun `exactly full final page reports completion`(route: String) {
    getPage(route, "?size=3")
      .jsonPath("content.length()").isEqualTo(3)
      .jsonPath("numberOfElements").isEqualTo(3)
      .jsonPath("last").isEqualTo(true)
  }

  @ParameterizedTest
  @ValueSource(strings = ["workflow-folders", "workflow-screens"])
  fun `page beyond the end is empty`(route: String) {
    getPage(route, "?page=2&size=2")
      .jsonPath("content").isEmpty
      .jsonPath("number").isEqualTo(2)
      .jsonPath("totalElements").isEqualTo(3)
      .jsonPath("last").isEqualTo(true)
  }

  @ParameterizedTest
  @ValueSource(strings = ["workflow-folders", "workflow-screens"])
  fun `empty table returns a completed empty page`(route: String) {
    dataLoader.jdbcTemplate.update("DELETE FROM WORKFLOW_SCREENS WHERE WORKFLOW_CODE IN ('WFX_A', 'WFX_B', 'WFX_C')")
    if (route == "workflow-folders") {
      dataLoader.jdbcTemplate.update("DELETE FROM WORKFLOW_FOLDERS WHERE WORKFLOW_CODE IN ('WFX_A', 'WFX_B', 'WFX_C')")
    }
    getPage(route)
      .jsonPath("content").isEmpty
      .jsonPath("totalElements").isEqualTo(0)
      .jsonPath("last").isEqualTo(true)
  }

  @ParameterizedTest
  @CsvSource(
    "workflow-folders,page=-1", "workflow-screens,page=-1",
    "workflow-folders,size=0", "workflow-screens,size=0",
    "workflow-folders,size=-1", "workflow-screens,size=-1",
    "workflow-folders,page=invalid", "workflow-screens,page=invalid",
    "workflow-folders,size=invalid", "workflow-screens,size=invalid",
    "workflow-folders,page=2147483648", "workflow-screens,page=2147483648",
    "workflow-folders,size=2147483648", "workflow-screens,size=2147483648",
    "workflow-folders,page=2147483647&size=1", "workflow-screens,page=2147483647&size=1",
    "workflow-folders,page=2147483646&size=2", "workflow-screens,page=2147483646&size=2",
  )
  fun `rejects invalid pagination with the standard error shape`(route: String, query: String) {
    webTestClient.get().uri("/api/$route?$query")
      .headers(setClientAuthorisation(listOf("VIEW_PRISONER_DATA")))
      .exchange()
      .expectStatus().isBadRequest
      .expectBody()
      .jsonPath("status").isEqualTo(400)
      .jsonPath("userMessage").isNotEmpty
  }

  @ParameterizedTest
  @ValueSource(strings = ["workflow-folders", "workflow-screens"])
  fun `caps page size at the configured maximum`(route: String) {
    getPage(route, "?size=10000")
      .jsonPath("size").isEqualTo(5000)
      .jsonPath("content.length()").isEqualTo(3)
  }

  @ParameterizedTest
  @ValueSource(strings = ["workflow-folders", "workflow-screens"])
  fun `paged projections do not load entities or issue queries per row`(route: String) {
    val statistics = entityManagerFactory.unwrap(SessionFactory::class.java).statistics
    val previouslyEnabled = statistics.isStatisticsEnabled
    try {
      statistics.isStatisticsEnabled = true
      statistics.clear()
      getPage(route, "?size=2").jsonPath("content.length()").isEqualTo(2)
      assertThat(statistics.prepareStatementCount).isEqualTo(2)
      assertThat(statistics.entityLoadCount).isZero()
    } finally {
      statistics.isStatisticsEnabled = previouslyEnabled
    }
  }

  @Test
  fun `screen entity identity includes both workflow code and module name`() {
    val keys = listOf(PK("WFX_A", "WFX_MOD_A"), PK("WFX_A", "WFX_MOD_Z"), PK("WFX_B", "WFX_MOD_A"))
    val screens = screenRepository.findAllById(keys)
    assertThat(screens.map { it.id }).containsExactlyInAnyOrderElementsOf(keys)
    assertThat(screens.toSet()).hasSize(3)
  }

  @ParameterizedTest
  @CsvSource("WFX_MISSING,WFX_MOD_A", "WFX_A,WFX_MISSING")
  fun `local schema enforces both screen foreign keys`(workflowCode: String, moduleName: String) {
    assertThatThrownBy {
      dataLoader.jdbcTemplate.update(
        "INSERT INTO WORKFLOW_SCREENS (WORKFLOW_CODE, MODULE_NAME) VALUES (?, ?)",
        workflowCode,
        moduleName,
      )
    }.isInstanceOf(DataIntegrityViolationException::class.java)
  }

  private fun getPage(route: String, query: String = ""): WebTestClient.BodyContentSpec = webTestClient.get()
    .uri("/api/$route$query")
    .headers(setClientAuthorisation(listOf("VIEW_PRISONER_DATA")))
    .exchange()
    .expectStatus().isOk
    .expectBody()
}
