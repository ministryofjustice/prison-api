package uk.gov.justice.hmpps.prison.service

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.transaction.TestTransaction
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.repository.jpa.model.Gang
import uk.gov.justice.hmpps.prison.repository.jpa.model.GangNonAssociation
import uk.gov.justice.hmpps.prison.repository.jpa.model.NonAssociationReason
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.repository.GangMemberRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.GangNonAssociationRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.GangRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository

private const val NEW_GANG_CODE_1 = "NEW_GANG_1"
private const val NEW_GANG_CODE_2 = "NEW_GANG_2"
private const val NEW_GANG_CODE_3 = "NEW_GANG_3"

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@WithAnonymousUser
class GangServiceImplIntTest {

  @Autowired
  lateinit var gangService: GangService

  @Autowired
  lateinit var gangRepository: GangRepository

  @Autowired
  lateinit var gangNonAssociationRepository: GangNonAssociationRepository

  @Autowired
  lateinit var repository: GangMemberRepository

  @Autowired
  lateinit var nonAssociationReasonRepository: ReferenceCodeRepository<NonAssociationReason>

  lateinit var primaryGang: Gang
  lateinit var secondaryGang: Gang
  lateinit var gang3: Gang

  @AfterEach
  fun teardown() {
    TestTransaction.start()
    gangRepository.delete(primaryGang)
    gangRepository.delete(secondaryGang)
    gangRepository.delete(gang3)
    TestTransaction.flagForCommit()
    TestTransaction.end()
  }

  @BeforeEach
  fun setup() {
    primaryGang = Gang(
      code = NEW_GANG_CODE_1,
      name = "The First Gang",
    )

    primaryGang.addMember(
      booking = OffenderBooking.builder().bookingId(-2).build(),
      commentText = "gang 1 - member 1 added",
    )
    primaryGang.addMember(
      booking = OffenderBooking.builder().bookingId(-3).build(),
      commentText = "gang 1 - member 2 added",
    )
    primaryGang.addMember(
      booking = OffenderBooking.builder().bookingId(-4).build(),
      commentText = "gang 1 - member 3 added",
    )

    gangRepository.save(primaryGang)

    secondaryGang = Gang(
      code = NEW_GANG_CODE_2,
      name = "The Second Gang",
    )
    secondaryGang.addMember(
      booking = OffenderBooking.builder().bookingId(-5).build(),
      commentText = "gang 2 - member 1 added",
    )
    gangRepository.save(secondaryGang)

    gang3 = Gang(
      code = NEW_GANG_CODE_3,
      name = "The Third Gang",
    )
    gang3.addMember(
      booking = OffenderBooking.builder().bookingId(-6).build(),
      commentText = "gang 3 - member 1 added",
    )
    gang3.addMember(
      booking = OffenderBooking.builder().bookingId(-7).build(),
      commentText = "gang 3 - member 2 added",
    )
    gangRepository.save(gang3)

    gangNonAssociationRepository.save(
      GangNonAssociation(
        primaryGang = primaryGang,
        secondaryGang = secondaryGang,
        nonAssociationReason = nonAssociationReasonRepository.findById(NonAssociationReason.pk("BUL"))
          .orElseThrow(),
      ),
    )

    gangNonAssociationRepository.save(
      GangNonAssociation(
        primaryGang = secondaryGang,
        secondaryGang = gang3,
        nonAssociationReason = nonAssociationReasonRepository.findById(NonAssociationReason.pk("RIV"))
          .orElseThrow(),
      ),
    )

    TestTransaction.flagForCommit()
    TestTransaction.end()
  }

  @Test
  fun getNonAssociatesInGangs() {
    val gangNas = gangService.getNonAssociatesInGangs("A1234AE")
    Assertions.assertThat(gangNas.gangNonAssociations).hasSize(2)
  }
}
