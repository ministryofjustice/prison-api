package net.syscon.elite.repository.jpa.repository

import net.syscon.elite.repository.jpa.model.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.transaction.TestTransaction
import java.time.LocalDate

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OffenderBookingRepositoryTest {
  @Autowired
  private lateinit var repository: OffenderBookingRepository

  @Test
  fun `retrieve military records`() {
    val booking = repository.findById(-1L).orElseThrow()
    assertThat(booking.militaryRecords).containsExactly(
        OffenderMilitaryRecord(
            startDate = LocalDate.parse("2000-01-01"), endDate = LocalDate.parse("2020-10-17"),
            militaryDischarge = MilitaryDischarge("DIS", "Dishonourable"),
            militaryBranch = MilitaryBranch("ARM", "Army"),
            warZone = WarZone("AFG", "Afghanistan"),
            description = "left", unitNumber = "auno", enlistmentLocation = "Somewhere",
            militaryRank = MilitaryRank("LCPL_RMA", "Lance Corporal  (Royal Marines)"),
            serviceNumber = "asno",
            disciplinaryAction = DisciplinaryAction("CM", "Court Martial"),
            dischargeLocation = "Sheffield"),
        OffenderMilitaryRecord(
            startDate = LocalDate.parse("2001-01-01"),
            militaryBranch = MilitaryBranch("NAV", "Navy"),
            description = "second record"))
  }

  @Test
  fun `save military records`() {
    val booking = repository.findById(-2L).orElseThrow()
    val militaryRecords = booking.militaryRecords
    assertThat(militaryRecords).hasSize(0)
    militaryRecords?.add(
        OffenderMilitaryRecord(startDate = LocalDate.parse("2000-01-01"),
            militaryBranch = MilitaryBranch("ARM", "Army"),
            dischargeLocation = "Somewhere",
            disciplinaryAction = DisciplinaryAction("CM", "Court Martial")))
    repository.save(booking)
    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    val persistedBooking = repository.findById(-2L)
    assertThat(persistedBooking.get().militaryRecords).containsExactly(
        OffenderMilitaryRecord(
            startDate = LocalDate.parse("2000-01-01"),
            militaryBranch = MilitaryBranch("ARM", "Army"),
            dischargeLocation = "Somewhere",
            disciplinaryAction = DisciplinaryAction("CM", "Court Martial")))
  }
}
