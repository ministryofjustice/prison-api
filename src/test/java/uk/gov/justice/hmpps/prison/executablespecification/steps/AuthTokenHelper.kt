package uk.gov.justice.hmpps.prison.executablespecification.steps

import org.springframework.stereotype.Component
import uk.gov.justice.hmpps.test.kotlin.auth.JwtAuthorisationHelper
import java.util.EnumMap

@Component
class AuthTokenHelper(private val jwtAuthenticationHelper: JwtAuthorisationHelper) {
  private val tokens: EnumMap<AuthToken, String> = EnumMap(AuthToken::class.java)
  var token: String? = null

  enum class AuthToken {
    PRISON_API_USER,
    API_TEST_USER,
    RENEGADE_USER,
    NO_CASELOAD_USER,
    NORMAL_USER,
    GLOBAL_SEARCH,
    VIEW_PRISONER_DATA,
    LOCAL_ADMIN,
    ADMIN_TOKEN,
    SUPER_ADMIN,
    INACTIVE_BOOKING_USER,
    SYSTEM_USER_READ_WRITE,
    CATEGORISATION_CREATE,
    CATEGORISATION_APPROVE,
    LAA_USER,
    BULK_APPOINTMENTS_USER,

    // ITAG_USER with ROLE_MAINTAIN_IEP and scope ['read','write]
    MAINTAIN_IEP,
    PAY,
    UPDATE_ALERT,
    COURT_HEARING_MAINTAINER,
    PRISON_MOVE_MAINTAINER,
    CREATE_BOOKING_USER,
    SMOKE_TEST,
    REF_DATA_MAINTAINER,
    REF_DATA_MAINTAINER_NO_WRITE,
    UNAUTHORISED_USER,
    CRD_USER,
    OFFENCE_MAINTAINER,
    RELEASE_DATE_MANUAL_COMPARER,
    UPDATE_OFFENCE_SCHEDULES,
    VIEW_CASE_NOTES,
    KEY_WORKER,
  }

  init {
    tokens[AuthToken.PRISON_API_USER] = prisonApiUser()
    tokens[AuthToken.API_TEST_USER] = apiTestUser()
    tokens[AuthToken.RENEGADE_USER] = renegadeUser()
    tokens[AuthToken.NO_CASELOAD_USER] = noCaseloadUser()
    tokens[AuthToken.GLOBAL_SEARCH] = globalSearchUser()
    tokens[AuthToken.VIEW_PRISONER_DATA] = viewPrisonerDataUser()
    tokens[AuthToken.LOCAL_ADMIN] = localAdmin()
    tokens[AuthToken.ADMIN_TOKEN] = adminToken()
    tokens[AuthToken.SUPER_ADMIN] = superAdmin()
    tokens[AuthToken.INACTIVE_BOOKING_USER] = inactiveBookingUser()
    tokens[AuthToken.SYSTEM_USER_READ_WRITE] = systemUserReadWrite()
    tokens[AuthToken.CATEGORISATION_CREATE] = categorisationCreate()
    tokens[AuthToken.CATEGORISATION_APPROVE] = categorisationApprove()
    tokens[AuthToken.LAA_USER] = laaUser()
    tokens[AuthToken.BULK_APPOINTMENTS_USER] = bulkAppointmentsUser()
    tokens[AuthToken.MAINTAIN_IEP] = maintainIep()
    tokens[AuthToken.NORMAL_USER] = normalUser()
    tokens[AuthToken.PAY] = payUser()
    tokens[AuthToken.UPDATE_ALERT] = updateAlert()
    tokens[AuthToken.COURT_HEARING_MAINTAINER] = courtHearingMaintainer()
    tokens[AuthToken.PRISON_MOVE_MAINTAINER] = prisonMoveMaintiner()
    tokens[AuthToken.CREATE_BOOKING_USER] = createBookingApiUser()
    tokens[AuthToken.SMOKE_TEST] = createSmokeTestUser()
    tokens[AuthToken.REF_DATA_MAINTAINER] = createRefDataMaintainerUser(true)
    tokens[AuthToken.REF_DATA_MAINTAINER_NO_WRITE] = createRefDataMaintainerUser(false)
    tokens[AuthToken.UNAUTHORISED_USER] = createUnauthorisedUser()
    tokens[AuthToken.CRD_USER] = createReleaseDatesCalculatorUser()
    tokens[AuthToken.OFFENCE_MAINTAINER] = someClientUser("ROLE_OFFENCE_MAINTAINER")
    tokens[AuthToken.UPDATE_OFFENCE_SCHEDULES] = someClientUser("ROLE_UPDATE_OFFENCE_SCHEDULES")
    tokens[AuthToken.RELEASE_DATE_MANUAL_COMPARER] = someClientUser("ROLE_RELEASE_DATE_MANUAL_COMPARER")
    tokens[AuthToken.VIEW_CASE_NOTES] = someClientUser("ROLE_VIEW_CASE_NOTES")
  }

  fun setToken(clientId: AuthToken) {
    token = getToken(clientId)
  }

  fun getToken(clientId: AuthToken): String = tokens[clientId] ?: throw RuntimeException("Token for $clientId not found")

  private fun prisonApiUser(): String = jwtAuthenticationHelper.createJwtAccessToken(username = "PRISON_API_USER")

  private fun apiTestUser(): String = jwtAuthenticationHelper.createJwtAccessToken(username = "API_TEST_USER")

  private fun renegadeUser(): String = jwtAuthenticationHelper.createJwtAccessToken(username = "RENEGADE")

  private fun noCaseloadUser(): String = jwtAuthenticationHelper.createJwtAccessToken(
    username = "RO_USER",
    scope = listOf("read", "write"),
    roles = listOf("ROLE_LICENCE_RO"),
  )

  private fun globalSearchUser(): String = jwtAuthenticationHelper.createJwtAccessToken(
    clientId = "deliusnewtech",
    roles = listOf("ROLE_GLOBAL_SEARCH"),
  )

  private fun viewPrisonerDataUser(): String = jwtAuthenticationHelper.createJwtAccessToken(
    clientId = "aclient",
    roles = listOf("ROLE_VIEW_PRISONER_DATA"),
  )

  private fun systemUserReadWrite(): String = jwtAuthenticationHelper.createJwtAccessToken(
    clientId = "PRISON_API_USER",
    scope = listOf("read", "write"),
    roles = listOf("ROLE_SYSTEM_USER"),
  )

  private fun localAdmin(): String = jwtAuthenticationHelper.createJwtAccessToken(
    username = "ITAG_USER_ADM",
    roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES", "ROLE_KW_MIGRATION", "ROLE_OAUTH_ADMIN"),
  )

  private fun adminToken(): String = jwtAuthenticationHelper.createJwtAccessToken(
    username = "ITAG_USER",
    roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN", "ROLE_GLOBAL_SEARCH", "ROLE_OMIC_ADMIN"),
  )

  private fun superAdmin(): String = jwtAuthenticationHelper.createJwtAccessToken(
    clientId = "PRISON_API_USER",
    scope = listOf("read", "write"),
    roles = listOf(
      "ROLE_MAINTAIN_ACCESS_ROLES_ADMIN",
      "ROLE_GLOBAL_SEARCH",
      "ROLE_MAINTAIN_ACCESS_ROLES",
      "ROLE_OMIC_ADMIN",
    ),
  )

  private fun inactiveBookingUser(): String = jwtAuthenticationHelper.createJwtAccessToken(
    username = "RO_USER",
    scope = listOf("read", "write"),
    roles = listOf("ROLE_GLOBAL_SEARCH", "ROLE_INACTIVE_BOOKINGS", "ROLE_LICENCE_RO"),
  )

  private fun categorisationCreate(): String = jwtAuthenticationHelper.createJwtAccessToken(
    username = "ITAG_USER",
    scope = listOf("read", "write"),
    roles = listOf(
      "ROLE_MAINTAIN_ACCESS_ROLES_ADMIN",
      "ROLE_GLOBAL_SEARCH",
      "ROLE_CREATE_CATEGORISATION",
      "ROLE_OMIC_ADMIN",
    ),
  )

  private fun categorisationApprove(): String = jwtAuthenticationHelper.createJwtAccessToken(
    username = "ITAG_USER",
    scope = listOf("read", "write"),
    roles = listOf("ROLE_APPROVE_CATEGORISATION"),
  )

  private fun laaUser(): String = jwtAuthenticationHelper.createJwtAccessToken(
    username = "LAA_USER",
    scope = listOf("read", "write"),
    roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES"),
  )

  private fun bulkAppointmentsUser(): String = jwtAuthenticationHelper.createJwtAccessToken(
    username = "API_TEST_USER",
    scope = listOf("read", "write"),
    roles = listOf("ROLE_BULK_APPOINTMENTS"),
  )

  private fun maintainIep(): String = jwtAuthenticationHelper.createJwtAccessToken(
    username = "ITAG_USER",
    scope = listOf("read", "write"),
    roles = listOf("ROLE_MAINTAIN_IEP"),
  )

  private fun normalUser(): String = jwtAuthenticationHelper.createJwtAccessToken(
    username = "ITAG_USER",
    scope = listOf("read", "write"),
  )

  private fun payUser(): String = jwtAuthenticationHelper.createJwtAccessToken(
    username = "ITAG_USER",
    roles = listOf("ROLE_PAY"),
    scope = listOf("read", "write"),
  )

  private fun updateAlert(): String = jwtAuthenticationHelper.createJwtAccessToken(
    username = "ITAG_USER",
    roles = listOf("ROLE_UPDATE_ALERT"),
    scope = listOf("read", "write"),
  )

  private fun courtHearingMaintainer(): String = jwtAuthenticationHelper.createJwtAccessToken(
    username = "DOES_NOT_EXIST",
    scope = listOf("read", "write"),
    roles = listOf("ROLE_COURT_HEARING_MAINTAINER"),
  )

  private fun prisonMoveMaintiner(): String = jwtAuthenticationHelper.createJwtAccessToken(
    username = "DOES_NOT_EXIST",
    scope = listOf("read", "write"),
    roles = listOf("ROLE_PRISON_MOVE_MAINTAINER"),
  )

  private fun createBookingApiUser(): String = jwtAuthenticationHelper.createJwtAccessToken(
    username = "ITAG_USER",
    scope = listOf("read", "write"),
    roles = listOf(
      "ROLE_BOOKING_CREATE",
      "ROLE_RELEASE_PRISONER",
      "ROLE_TRANSFER_PRISONER",
      "ROLE_INACTIVE_BOOKINGS",
      "ROLE_VIEW_PRISONER_DATA",
    ),
  )

  private fun createSmokeTestUser(): String = jwtAuthenticationHelper.createJwtAccessToken(
    username = "SMOKE_TEST_USER",
    scope = listOf("read", "write"),
    roles = listOf("ROLE_SMOKE_TEST"),
  )

  private fun createUnauthorisedUser(): String = jwtAuthenticationHelper.createJwtAccessToken(
    username = "UNAUTHORISED_USER",
  )

  private fun createRefDataMaintainerUser(allowWriteScope: Boolean): String = jwtAuthenticationHelper.createJwtAccessToken(
    username = "ITAG_USER",
    scope = if (allowWriteScope) listOf("read", "write") else listOf("read"),
    roles = listOf("ROLE_MAINTAIN_REF_DATA"),
  )

  private fun createReleaseDatesCalculatorUser(): String = jwtAuthenticationHelper.createJwtAccessToken(
// use ITAG_USER to avoid the pain of creating a new username in the test DB
    username = "ITAG_USER",
    scope = listOf("read", "write"),
    roles = listOf("ROLE_RELEASE_DATES_CALCULATOR"),
  )

  fun someClientUser(vararg roles: String): String = jwtAuthenticationHelper.createJwtAccessToken(
    username = "Another System",
    scope = listOf("read", "write"),
    roles = listOf(*roles),
  )
}
