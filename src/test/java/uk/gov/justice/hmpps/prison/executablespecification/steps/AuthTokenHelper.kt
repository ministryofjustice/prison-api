package uk.gov.justice.hmpps.prison.executablespecification.steps

import org.springframework.stereotype.Component
import uk.gov.justice.hmpps.prison.util.JwtAuthenticationHelper
import uk.gov.justice.hmpps.prison.util.JwtParameters.Companion.builder
import java.time.Duration
import java.util.EnumMap

@Component
class AuthTokenHelper(private val jwtAuthenticationHelper: JwtAuthenticationHelper) {
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

  private fun prisonApiUser(): String {
    return jwtAuthenticationHelper.createJwt(
      builder()
        .username("PRISON_API_USER")
        .scope(listOf("read"))
        .roles(emptyList())
        .expiryTime(Duration.ofDays((365 * 10).toLong()))
        .build(),
    )
  }

  private fun apiTestUser(): String {
    return jwtAuthenticationHelper.createJwt(
      builder()
        .username("API_TEST_USER")
        .scope(listOf("read"))
        .roles(emptyList())
        .expiryTime(Duration.ofDays((365 * 10).toLong()))
        .build(),
    )
  }

  private fun renegadeUser(): String {
    return jwtAuthenticationHelper.createJwt(
      builder()
        .username("RENEGADE")
        .scope(listOf("read"))
        .roles(emptyList())
        .expiryTime(Duration.ofDays((365 * 10).toLong()))
        .build(),
    )
  }

  private fun noCaseloadUser(): String {
    return jwtAuthenticationHelper.createJwt(
      builder()
        .username("RO_USER")
        .scope(listOf("read", "write"))
        .roles(listOf("ROLE_LICENCE_RO"))
        .expiryTime(Duration.ofDays((365 * 10).toLong()))
        .build(),
    )
  }

  private fun globalSearchUser(): String {
    return jwtAuthenticationHelper.createJwt(
      builder()
        .clientId("deliusnewtech")
        .internalUser(false)
        .scope(listOf("read"))
        .roles(listOf("ROLE_GLOBAL_SEARCH"))
        .expiryTime(Duration.ofDays((365 * 10).toLong()))
        .build(),
    )
  }

  private fun viewPrisonerDataUser(): String {
    return jwtAuthenticationHelper.createJwt(
      builder()
        .clientId("aclient")
        .internalUser(false)
        .scope(listOf("read"))
        .roles(listOf("ROLE_VIEW_PRISONER_DATA"))
        .expiryTime(Duration.ofDays((365 * 10).toLong()))
        .build(),
    )
  }

  private fun systemUserReadWrite(): String {
    return jwtAuthenticationHelper.createJwt(
      builder()
        .clientId("PRISON_API_USER")
        .internalUser(false)
        .scope(listOf("read", "write"))
        .roles(listOf("ROLE_SYSTEM_USER"))
        .expiryTime(Duration.ofDays((365 * 10).toLong()))
        .build(),
    )
  }

  private fun localAdmin(): String {
    return jwtAuthenticationHelper.createJwt(
      builder()
        .username("ITAG_USER_ADM")
        .scope(listOf("read"))
        .roles(listOf("ROLE_MAINTAIN_ACCESS_ROLES", "ROLE_KW_MIGRATION", "ROLE_OAUTH_ADMIN"))
        .expiryTime(Duration.ofDays((365 * 10).toLong()))
        .build(),
    )
  }

  private fun adminToken(): String {
    return jwtAuthenticationHelper.createJwt(
      builder()
        .username("ITAG_USER")
        .scope(listOf("read"))
        .roles(listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN", "ROLE_GLOBAL_SEARCH", "ROLE_OMIC_ADMIN"))
        .expiryTime(Duration.ofDays((365 * 10).toLong()))
        .build(),
    )
  }

  private fun superAdmin(): String {
    return jwtAuthenticationHelper.createJwt(
      builder()
        .clientId("PRISON_API_USER")
        .scope(listOf("read", "write"))
        .roles(
          listOf(
            "ROLE_MAINTAIN_ACCESS_ROLES_ADMIN",
            "ROLE_GLOBAL_SEARCH",
            "ROLE_MAINTAIN_ACCESS_ROLES",
            "ROLE_OMIC_ADMIN",
          ),
        )
        .expiryTime(Duration.ofDays((365 * 10).toLong()))
        .build(),
    )
  }

  private fun inactiveBookingUser(): String {
    return jwtAuthenticationHelper.createJwt(
      builder()
        .username("RO_USER")
        .scope(listOf("read", "write"))
        .roles(listOf("ROLE_GLOBAL_SEARCH", "ROLE_INACTIVE_BOOKINGS", "ROLE_LICENCE_RO"))
        .expiryTime(Duration.ofDays((365 * 10).toLong()))
        .build(),
    )
  }

  private fun categorisationCreate(): String {
    return jwtAuthenticationHelper.createJwt(
      builder()
        .username("ITAG_USER")
        .scope(listOf("read", "write"))
        .roles(
          listOf(
            "ROLE_MAINTAIN_ACCESS_ROLES_ADMIN",
            "ROLE_GLOBAL_SEARCH",
            "ROLE_CREATE_CATEGORISATION",
            "ROLE_OMIC_ADMIN",
          ),
        )
        .expiryTime(Duration.ofDays((365 * 10).toLong()))
        .build(),
    )
  }

  private fun categorisationApprove(): String {
    return jwtAuthenticationHelper.createJwt(
      builder()
        .username("ITAG_USER")
        .scope(listOf("read", "write"))
        .roles(listOf("ROLE_APPROVE_CATEGORISATION"))
        .expiryTime(Duration.ofDays((365 * 10).toLong()))
        .build(),
    )
  }

  private fun laaUser(): String {
    return jwtAuthenticationHelper.createJwt(
      builder()
        .username("LAA_USER")
        .scope(listOf("read", "write"))
        .roles(listOf("ROLE_MAINTAIN_ACCESS_ROLES"))
        .expiryTime(Duration.ofDays((365 * 10).toLong()))
        .build(),
    )
  }

  private fun bulkAppointmentsUser(): String {
    return jwtAuthenticationHelper.createJwt(
      builder()
        .username("API_TEST_USER")
        .scope(listOf("read", "write"))
        .roles(listOf("ROLE_BULK_APPOINTMENTS"))
        .expiryTime(Duration.ofDays((365 * 10).toLong()))
        .build(),
    )
  }

  private fun maintainIep(): String {
    return jwtAuthenticationHelper.createJwt(
      builder()
        .username("ITAG_USER")
        .scope(listOf("read", "write"))
        .roles(listOf("ROLE_MAINTAIN_IEP"))
        .expiryTime(Duration.ofDays((365 * 10).toLong()))
        .build(),
    )
  }

  private fun normalUser(): String {
    return jwtAuthenticationHelper.createJwt(
      builder()
        .username("ITAG_USER")
        .scope(listOf("read", "write"))
        .expiryTime(Duration.ofDays((365 * 10).toLong()))
        .build(),
    )
  }

  private fun payUser(): String {
    return jwtAuthenticationHelper.createJwt(
      builder()
        .username("ITAG_USER")
        .roles(listOf("ROLE_PAY"))
        .scope(listOf("read", "write"))
        .expiryTime(Duration.ofDays(1))
        .build(),
    )
  }

  private fun updateAlert(): String {
    return jwtAuthenticationHelper.createJwt(
      builder()
        .username("ITAG_USER")
        .roles(listOf("ROLE_UPDATE_ALERT"))
        .scope(listOf("read", "write"))
        .expiryTime(Duration.ofDays(1))
        .build(),
    )
  }

  private fun courtHearingMaintainer(): String {
    return jwtAuthenticationHelper.createJwt(
      builder()
        .username("DOES_NOT_EXIST")
        .scope(listOf("read", "write"))
        .roles(listOf("ROLE_COURT_HEARING_MAINTAINER"))
        .expiryTime(Duration.ofDays((365 * 10).toLong()))
        .build(),
    )
  }

  private fun prisonMoveMaintiner(): String {
    return jwtAuthenticationHelper.createJwt(
      builder()
        .username("DOES_NOT_EXIST")
        .scope(listOf("read", "write"))
        .roles(listOf("ROLE_PRISON_MOVE_MAINTAINER"))
        .expiryTime(Duration.ofDays((365 * 10).toLong()))
        .build(),
    )
  }

  private fun createBookingApiUser(): String {
    return jwtAuthenticationHelper.createJwt(
      builder()
        .username("ITAG_USER")
        .scope(listOf("read", "write"))
        .roles(
          listOf(
            "ROLE_BOOKING_CREATE",
            "ROLE_RELEASE_PRISONER",
            "ROLE_TRANSFER_PRISONER",
            "ROLE_INACTIVE_BOOKINGS",
            "ROLE_VIEW_PRISONER_DATA",
          ),
        )
        .expiryTime(Duration.ofDays((365 * 10).toLong()))
        .build(),
    )
  }

  private fun createSmokeTestUser(): String {
    return jwtAuthenticationHelper.createJwt(
      builder()
        .username("SMOKE_TEST_USER")
        .scope(listOf("read", "write"))
        .roles(listOf("ROLE_SMOKE_TEST"))
        .expiryTime(Duration.ofDays((365 * 10).toLong()))
        .build(),
    )
  }

  private fun createUnauthorisedUser(): String {
    return jwtAuthenticationHelper.createJwt(
      builder()
        .username("UNAUTHORISED_USER")
        .expiryTime(Duration.ofDays((365 * 10).toLong()))
        .build(),
    )
  }

  private fun createRefDataMaintainerUser(allowWriteScope: Boolean): String {
    return jwtAuthenticationHelper.createJwt(
      builder()
        .username("ITAG_USER")
        .scope(if (allowWriteScope) listOf("read", "write") else listOf("read"))
        .roles(listOf("ROLE_MAINTAIN_REF_DATA"))
        .expiryTime(Duration.ofDays((365 * 10).toLong()))
        .build(),
    )
  }

  private fun createReleaseDatesCalculatorUser(): String {
    return jwtAuthenticationHelper.createJwt(
      builder()
        .username("ITAG_USER") // use ITAG_USER to avoid the pain of creating a new username in the test DB
        .scope(listOf("read", "write"))
        .roles(listOf("ROLE_RELEASE_DATES_CALCULATOR"))
        .expiryTime(Duration.ofDays((365 * 10).toLong()))
        .build(),
    )
  }

  fun someClientUser(vararg roles: String): String {
    return jwtAuthenticationHelper.createJwt(
      builder()
        .username("Another System")
        .scope(listOf("read", "write"))
        .roles(listOf(*roles))
        .expiryTime(Duration.ofDays((365 * 10).toLong()))
        .build(),
    )
  }
}
