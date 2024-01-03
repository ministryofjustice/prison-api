package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("/api/staff/{staffId}/caseloads")
class StaffCaseloadsResourceTest : ResourceTest() {

  @Test
  fun testCanRetrieveCaseloadForNonExistentStaffMember() {
    webTestClient.get().uri("/api/staff/{staffId}/caseloads", 10)
      .headers(setClientAuthorisation(listOf("ROLE_STAFF_SEARCH")))
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun testCanRetrieveCaseloadForAStaffMember() {
    webTestClient.get()
      .uri("/api/staff/{staffId}/caseloads", -2)
      .headers(setAuthorisation(emptyList()))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .json(
        """[
          {caseLoadId:"BXI",description:"Brixton (HMP)",type:"INST",caseloadFunction:"GENERAL",currentlyActive:false},
          {caseLoadId:"LEI",description:"Leeds (HMP)",type:"INST",caseloadFunction:"GENERAL",currentlyActive:true},
          {caseLoadId:"MDI",description:"Moorland Closed (HMP & YOI)",type:"INST",caseloadFunction:"GENERAL",currentlyActive:false},
          {caseLoadId:"NWEB",description:"Nomis-web Application",type:"APP",caseloadFunction:"GENERAL",currentlyActive:false},
          {caseLoadId:"RNI",description:"Ranby (HMP)",type:"INST",caseloadFunction:"GENERAL",currentlyActive:false},
          {caseLoadId:"SYI",description:"Shrewsbury (HMP)",type:"INST",caseloadFunction:"GENERAL",currentlyActive:false},
          {caseLoadId:"WAI",description:"The Weare (HMP)",type:"INST",caseloadFunction:"GENERAL",currentlyActive:false}]
        """.trimMargin(),
      )
  }

  @Test
  fun testCanRetrieveCaseloadForStaffWithNoCaseloads() {
    webTestClient.get()
      .uri("/api/staff/{staffId}/caseloads", -10)
      .headers(setAuthorisation("EXOFF5", emptyList()))
      .exchange()
      .expectStatus().isNoContent
  }

  @Test
  fun `cannot get another staff members details`() {
    webTestClient.get()
      .uri("/api/staff/{staffId}/caseloads", -2)
      .headers(setAuthorisation("EXOFF5", emptyList()))
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `can get another staff members details with role`() {
    webTestClient.get()
      .uri("/api/staff/{staffId}/caseloads", -2)
      .headers(setAuthorisation("EXOFF5", listOf("ROLE_STAFF_SEARCH")))
      .exchange()
      .expectStatus().isOk
  }
}
