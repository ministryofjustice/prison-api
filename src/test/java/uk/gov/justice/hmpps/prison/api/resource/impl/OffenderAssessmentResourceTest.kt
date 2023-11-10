package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.mockito.kotlin.mock
import org.springframework.web.client.HttpClientErrorException
import uk.gov.justice.hmpps.prison.api.resource.OffenderAssessmentResource
import uk.gov.justice.hmpps.prison.api.support.CategoryInformationType.CATEGORISED
import uk.gov.justice.hmpps.prison.service.InmateService

class OffenderAssessmentResourceTest {
  private val inmateService = mock<InmateService>()
  private val offenderAssessmentResource = OffenderAssessmentResource(inmateService, null)

  @Test
  fun offenderCategorisationsInvalidType() {
    assertThatThrownBy { offenderAssessmentResource.getOffenderCategorisations("LEI", "INVALID_CAT_TYPE", null) }
      .isInstanceOf(HttpClientErrorException::class.java)
  }

  @Test
  fun offenderCategorisations() {
    offenderAssessmentResource.getOffenderCategorisations("LEI", CATEGORISED.name, null)
    verify(inmateService).getCategory("LEI", CATEGORISED, null)
  }
}
