package uk.gov.justice.hmpps.prison.helper.builder

import jakarta.persistence.EntityManager
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.repository.jpa.model.Gang
import uk.gov.justice.hmpps.prison.repository.jpa.model.NonAssociationReason
import uk.gov.justice.hmpps.prison.repository.jpa.repository.GangRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository

const val NEW_GANG_CODE_1 = "NEW_GANG_1"
const val NEW_GANG_CODE_2 = "NEW_GANG_2"
const val NEW_GANG_CODE_3 = "NEW_GANG_3"

@Component
@Transactional
class GangBuilder(
  private val gangRepository: GangRepository,
  private val nonAssociationReasonRepository: ReferenceCodeRepository<NonAssociationReason>,
  private val offenderBookingRepository: OffenderBookingRepository,
  private val entityManager: EntityManager,
) {

  fun initGangs() {
    val firstGang = Gang(
      code = NEW_GANG_CODE_1,
      name = "The First Gang",
    )
    val secondGang = Gang(
      code = NEW_GANG_CODE_2,
      name = "The Second Gang",
    )
    val thirdGang = Gang(
      code = NEW_GANG_CODE_3,
      name = "The Third Gang",
    )
    gangRepository.save(firstGang)
    gangRepository.save(secondGang)
    gangRepository.save(thirdGang)

    firstGang.addMember(
      booking = offenderBookingRepository.findByOffenderNomsIdAndActive("A1234AA", true).orElseThrow(),
      commentText = "gang 1 - member 1 added",
    )
    firstGang.addMember(
      booking = offenderBookingRepository.findByOffenderNomsIdAndActive("A1234AB", true).orElseThrow(),
      commentText = "gang 1 - member 2 added",
    )
    firstGang.addMember(
      booking = offenderBookingRepository.findByOffenderNomsIdAndActive("A1234AC", true).orElseThrow(),
      commentText = "gang 1 - member 3 added",
    )
    secondGang.addMember(
      booking = offenderBookingRepository.findByOffenderNomsIdAndActive("A1234AD", true).orElseThrow(),
      commentText = "gang 2 - member 1 added",
    )
    thirdGang.addMember(
      booking = offenderBookingRepository.findByOffenderNomsIdAndActive("A1234AE", true).orElseThrow(),
      commentText = "gang 3 - member 1 added",
    )
    thirdGang.addMember(
      booking = offenderBookingRepository.findByOffenderNomsIdAndActive("A1234AF", true).orElseThrow(),
      commentText = "gang 3 - member 2 added",
    )

    val bullying = nonAssociationReasonRepository.findById(NonAssociationReason.pk("BUL")).orElseThrow()
    val rival = nonAssociationReasonRepository.findById(NonAssociationReason.pk("RIV")).orElseThrow()

    firstGang.addNonAssociation(secondGang, bullying)
    thirdGang.addNonAssociation(secondGang, rival)

    gangRepository.saveAndFlush(firstGang)
    gangRepository.saveAndFlush(secondGang)
    gangRepository.saveAndFlush(thirdGang)

    entityManager.clear()
  }

  fun teardown() {
    gangRepository.deleteAll()
  }
}
