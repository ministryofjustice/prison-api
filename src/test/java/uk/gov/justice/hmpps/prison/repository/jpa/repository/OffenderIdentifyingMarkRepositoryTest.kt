package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OffenderIdentifyingMarkRepositoryTest {
  @Autowired
  private lateinit var repository: OffenderIdentifyingMarkRepository

  @Test
  fun `find all marks for a prisoner's latest booking`() {
    val marks = repository.findAllMarksForLatestBooking("A1234AA")

    assertThat(marks).hasSize(3)
    val mark1 = marks[0]
    assertThat(mark1.bookingId).isEqualTo(-1L)
    assertThat(mark1.sequenceId).isEqualTo(1)
    assertThat(mark1.offenderBooking.offender.nomsId).isEqualTo("A1234AA")
    assertThat(mark1.markType).isEqualTo("TAT")
    assertThat(mark1.bodyPart).isEqualTo("TORSO")
    assertThat(mark1.side).isNull()
    assertThat(mark1.partOrientation).isNull()
    assertThat(mark1.images.map { it.id }).containsExactlyInAnyOrder(-100L, -101L)

    val mark2 = marks[1]
    assertThat(mark2.bookingId).isEqualTo(-1L)
    assertThat(mark2.sequenceId).isEqualTo(2)
    assertThat(mark2.offenderBooking.offender.nomsId).isEqualTo("A1234AA")
    assertThat(mark2.markType).isEqualTo("TAT")
    assertThat(mark2.bodyPart).isEqualTo("ARM")
    assertThat(mark2.side).isEqualTo("L")
    assertThat(mark2.partOrientation).isEqualTo("UPP")
    assertThat(mark2.images.map { it.id }).containsExactlyInAnyOrder(-102L, -103L)

    val mark3 = marks[2]
    assertThat(mark3.bookingId).isEqualTo(-1L)
    assertThat(mark3.sequenceId).isEqualTo(3)
    assertThat(mark3.offenderBooking.offender.nomsId).isEqualTo("A1234AA")
    assertThat(mark3.markType).isEqualTo("TAT")
    assertThat(mark3.bodyPart).isEqualTo("LEG")
    assertThat(mark3.side).isEqualTo("R")
    assertThat(mark3.partOrientation).isEqualTo("UPP")
    assertThat(mark3.images).isEmpty()
  }

  @Test
  fun `find specific mark for a prisoner's latest booking`() {
    val mark = repository.getMarkForLatestBookingByOffenderNumberAndSequenceId("A1234AA", 2)!!

    assertThat(mark.bookingId).isEqualTo(-1L)
    assertThat(mark.sequenceId).isEqualTo(2)
    assertThat(mark.offenderBooking.offender.nomsId).isEqualTo("A1234AA")
    assertThat(mark.markType).isEqualTo("TAT")
    assertThat(mark.bodyPart).isEqualTo("ARM")
    assertThat(mark.side).isEqualTo("L")
    assertThat(mark.partOrientation).isEqualTo("UPP")
    assertThat(mark.images.map { it.id }).containsExactlyInAnyOrder(-102L, -103L)
  }
}
