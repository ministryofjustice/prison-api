package uk.gov.justice.hmpps.prison.api.resource

import com.fasterxml.jackson.annotation.JsonIgnore
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.type.filter.AnnotationTypeFilter
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.hmpps.prison.core.ProgrammaticAuthorisation
import uk.gov.justice.hmpps.prison.core.ReferenceData
import uk.gov.justice.hmpps.prison.security.VerifyAgencyAccess
import uk.gov.justice.hmpps.prison.security.VerifyBookingAccess
import uk.gov.justice.hmpps.prison.security.VerifyOffenderAccess
import uk.gov.justice.hmpps.prison.security.VerifyStaffAccess
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Method

class ControllerTest {
  private data class EndpointInfo(
    val method: String,
    @JsonIgnore
    val hasEndpointLevelProtection: Boolean,
  )

  private data class ControllerInfo(
    val controller: String,
    val hasControllerLevelProtection: Boolean,
    val endpoints: List<EndpointInfo>,
  )

  @Test
  fun `Ensure all endpoints have a role annotation`() {
    val controllers = getControllers()
    val failedEndpoints = ArrayList<String>()

    for (controller in controllers) {
      if (!controller.hasControllerLevelProtection) {
        for (endpoint in controller.endpoints) {
          if (!endpoint.hasEndpointLevelProtection) {
            val method = endpoint.method.replace(Regex("^public [^ ]+ "), "")
            println(method)
            failedEndpoints += method
          }
        }
      }
    }
    // Print out any endpoints that don't have a role annotation
    assertThat(failedEndpoints).isEmpty()
  }

  private fun getControllers() = ClassPathScanningCandidateComponentProvider(false)
    .also { it.addIncludeFilter(AnnotationTypeFilter(RestController::class.java)) }
    .findCandidateComponents("uk.gov.justice")
    .map { Class.forName(it.beanClassName) }
    .map { ControllerInfo(it.toString(), it.isProtectedByAnnotation(), it.getEndpoints()) }

  private fun Class<*>.getEndpoints() = this.methods
    .filter { it.isEndpoint() }
    .map { EndpointInfo(it.toString(), it.isProtectedByAnnotation()) }

  private fun Method.isEndpoint() = this.annotations.any {
    it.annotationClass.qualifiedName!!.startsWith("org.springframework.web.bind.annotation")
  }

  private fun AnnotatedElement.isProtectedByAnnotation(): Boolean {
    if (getAnnotation(VerifyOffenderAccess::class.java) != null ||
      getAnnotation(VerifyBookingAccess::class.java) != null ||
      getAnnotation(VerifyAgencyAccess::class.java) != null ||
      getAnnotation(VerifyStaffAccess::class.java) != null ||
      getAnnotation(ReferenceData::class.java) != null ||
      getAnnotation(ProgrammaticAuthorisation::class.java) != null
    ) {
      return true
    }
    val annotation = getAnnotation(PreAuthorize::class.java) ?: return false
    return annotation.value.contains("hasAnyRole") || annotation.value.contains("hasRole")
  }
}
