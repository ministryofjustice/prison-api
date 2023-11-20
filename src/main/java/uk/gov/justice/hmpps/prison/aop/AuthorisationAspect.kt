package uk.gov.justice.hmpps.prison.aop

import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.aspectj.lang.annotation.Pointcut
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade
import uk.gov.justice.hmpps.prison.security.VerifyAgencyAccess
import uk.gov.justice.hmpps.prison.security.VerifyBookingAccess
import uk.gov.justice.hmpps.prison.security.VerifyOffenderAccess
import uk.gov.justice.hmpps.prison.service.AgencyService
import uk.gov.justice.hmpps.prison.service.BookingService
import uk.gov.justice.hmpps.prison.service.support.AgencyRequest

@Aspect
@Component
class AuthorisationAspect(private val bookingService: BookingService, private val agencyService: AgencyService) {
  @Pointcut("@annotation(uk.gov.justice.hmpps.prison.security.VerifyOffenderAccess) && execution(* *(String,..)) && args(offenderNo,..)")
  fun verifyOffenderAccessPointcut(offenderNo: String) {
    // no code needed - pointcut definition
  }

  @Pointcut("@annotation(uk.gov.justice.hmpps.prison.security.VerifyBookingAccess) && execution(* *(Long,..)) && args(bookingId,..)")
  fun verifyBookingAccessPointcut(bookingId: Long) {
    // no code needed - pointcut definition
  }

  @Pointcut("@annotation(uk.gov.justice.hmpps.prison.security.VerifyAgencyAccess) && execution(* *(String,..)) && args(agencyId,..)")
  fun verifyAgencyAccessPointcut(agencyId: String) {
    // no code needed - pointcut definition
  }

  @Pointcut("@annotation(uk.gov.justice.hmpps.prison.security.VerifyAgencyAccess) && args(uk.gov.justice.hmpps.prison.service.support.AgencyRequest,..) && args(request,..)")
  fun verifyAgencyRequestAccessPointcut(request: AgencyRequest) {
    // no code needed - pointcut definition
  }

  @Before(value = "verifyBookingAccessPointcut(bookingId)", argNames = "jp,bookingId")
  fun verifyBookingAccess(jp: JoinPoint, bookingId: Long) {
    log.debug("Verifying booking access for booking [{}]", bookingId)
    val signature = jp.signature as MethodSignature
    val method = signature.method
    val annotation = method.getAnnotation(VerifyBookingAccess::class.java)
    val overrideRoles = annotation.overrideRoles
    if (AuthenticationFacade.hasRoles(*overrideRoles)) {
      bookingService.checkBookingExists(bookingId)
    } else {
      bookingService.verifyBookingAccess(bookingId, *overrideRoles)
    }
  }

  @Before(value = "verifyOffenderAccessPointcut(offenderNo)", argNames = "jp,offenderNo")
  fun verifyOffenderAccess(jp: JoinPoint, offenderNo: String) {
    log.debug("Verifying offender access for offender No [{}]", offenderNo)
    val signature = jp.signature as MethodSignature
    val method = signature.method
    val annotation = method.getAnnotation(VerifyOffenderAccess::class.java)
    val overrideRoles = annotation.overrideRoles
    bookingService.getOffenderIdentifiers(offenderNo, *overrideRoles)
  }

  @Before(value = "verifyAgencyAccessPointcut(agencyId)", argNames = "jp,agencyId")
  fun verifyAgencyAccess(jp: JoinPoint, agencyId: String) {
    log.debug("Verifying agency access for agency [{}]", agencyId)
    if (AuthenticationFacade.hasRoles(*getOverrideRoles(jp))) {
      agencyService.checkAgencyExists(agencyId)
    } else {
      agencyService.verifyAgencyAccess(agencyId)
    }
  }

  @Before(value = "verifyAgencyRequestAccessPointcut(request)", argNames = "jp,request")
  fun verifyAgencyRequestAccess(jp: JoinPoint, request: AgencyRequest) {
    log.debug("Verifying agency access for agency [{}]", request.agencyId)
    if (AuthenticationFacade.hasRoles(*getOverrideRoles(jp))) {
      agencyService.checkAgencyExists(request.agencyId)
    } else {
      agencyService.verifyAgencyAccess(request.agencyId)
    }
  }

  private fun getOverrideRoles(jp: JoinPoint): Array<String> {
    val signature = jp.signature as MethodSignature
    val method = signature.method
    val annotation = method.getAnnotation(VerifyAgencyAccess::class.java)
    return annotation.overrideRoles
  }

  private companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
