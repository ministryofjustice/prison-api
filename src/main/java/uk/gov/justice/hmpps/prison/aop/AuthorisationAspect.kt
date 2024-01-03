package uk.gov.justice.hmpps.prison.aop

import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.aspectj.lang.annotation.Pointcut
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.LoggerFactory
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Component
import uk.gov.justice.hmpps.prison.repository.jpa.model.StaffUserAccount
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StaffUserAccountRepository
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade
import uk.gov.justice.hmpps.prison.security.VerifyAgencyAccess
import uk.gov.justice.hmpps.prison.security.VerifyBookingAccess
import uk.gov.justice.hmpps.prison.security.VerifyOffenderAccess
import uk.gov.justice.hmpps.prison.security.VerifyStaffAccess
import uk.gov.justice.hmpps.prison.service.AgencyService
import uk.gov.justice.hmpps.prison.service.BookingService
import uk.gov.justice.hmpps.prison.service.support.AgencyRequest

@Aspect
@Component
class AuthorisationAspect(
  private val bookingService: BookingService,
  private val agencyService: AgencyService,
  private val authenticationFacade: AuthenticationFacade,
  private val staffUserAccountRepository: StaffUserAccountRepository,
) {
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

  @Pointcut("@annotation(uk.gov.justice.hmpps.prison.security.VerifyStaffAccess) && execution(* *(Long,..)) && args(staffId,..)")
  fun verifyStaffAccessPointcut(staffId: Long) {
    // no code needed - pointcut definition
  }

  @Before(value = "verifyBookingAccessPointcut(bookingId)", argNames = "jp,bookingId")
  fun verifyBookingAccess(jp: JoinPoint, bookingId: Long) {
    log.debug("Verifying booking access for booking [{}]", bookingId)
    val signature = jp.signature as MethodSignature
    val method = signature.method
    val annotation = method.getAnnotation(VerifyBookingAccess::class.java)
    val overrideRoles = annotation.overrideRoles
    val accessDeniedError = annotation.accessDeniedError
    if (AuthenticationFacade.hasRoles(*overrideRoles)) {
      bookingService.checkBookingExists(bookingId)
    } else {
      bookingService.verifyBookingAccess(bookingId, accessDeniedError, *overrideRoles)
    }
  }

  @Before(value = "verifyOffenderAccessPointcut(offenderNo)", argNames = "jp,offenderNo")
  fun verifyOffenderAccess(jp: JoinPoint, offenderNo: String) {
    log.debug("Verifying offender access for offender No [{}]", offenderNo)
    val signature = jp.signature as MethodSignature
    val method = signature.method
    val annotation = method.getAnnotation(VerifyOffenderAccess::class.java)
    val overrideRoles = annotation.overrideRoles
    val accessDeniedError = annotation.accessDeniedError
    bookingService.getOffenderIdentifiers(offenderNo, accessDeniedError, *overrideRoles)
  }

  @Before(value = "verifyAgencyAccessPointcut(agencyId)", argNames = "jp,agencyId")
  fun verifyAgencyAccess(jp: JoinPoint, agencyId: String) {
    log.debug("Verifying agency access for agency [{}]", agencyId)
    val signature = jp.signature as MethodSignature
    val method = signature.method
    val annotation = method.getAnnotation(VerifyAgencyAccess::class.java)
    val overrideRoles = annotation.overrideRoles
    val accessDeniedError = annotation.accessDeniedError
    if (AuthenticationFacade.hasRoles(*overrideRoles)) {
      agencyService.checkAgencyExists(agencyId)
    } else {
      agencyService.verifyAgencyAccess(agencyId, accessDeniedError)
    }
  }

  @Before(value = "verifyAgencyRequestAccessPointcut(request)", argNames = "jp,request")
  fun verifyAgencyRequestAccess(jp: JoinPoint, request: AgencyRequest) {
    verifyAgencyAccess(jp, request.agencyId)
  }

  @Before(value = "verifyStaffAccessPointcut(staffId)", argNames = "jp,staffId")
  fun verifyStaffAccess(jp: JoinPoint, staffId: Long) {
    log.debug("Verifying staffId access for staffId [{}]", staffId)
    val signature = jp.signature as MethodSignature
    val method = signature.method
    val annotation = method.getAnnotation(VerifyStaffAccess::class.java)
    val overrideRoles = annotation.overrideRoles
    if (!AuthenticationFacade.hasRoles(*overrideRoles)) {
      val currentUsername: String = authenticationFacade.getCurrentUsername()
        ?: throw AccessDeniedException("No current username for staffId=$staffId")
      staffUserAccountRepository.findByUsername(currentUsername)
        .ifPresentOrElse(
          { staffUserAccount: StaffUserAccount ->
            val userStaffId = staffUserAccount.staff.staffId
            if (userStaffId != staffId) {
              throw AccessDeniedException("staff=$userStaffId accessing details of other staff=$staffId")
            }
          },
          {
            throw AccessDeniedException("Cannot find staff id for username=$currentUsername")
          },
        )
    }
  }

  private companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
