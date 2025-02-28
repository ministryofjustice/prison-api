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
class OffenderDistinguishingMarkRepositoryTest {
  @Autowired
  private lateinit var repository: OffenderIdentifyingMarkRepository

  @Test
  fun `find all marks for a prisoner's latest booking`() {
    val marks = repository.findAllMarksForLatestBooking("A1069AA")

    assertThat(marks).hasSize(2)
    val mark = marks[0]
    assertThat(mark.bookingId).isEqualTo(-105L)
    assertThat(mark.sequenceId).isEqualTo(1)
    assertThat(mark.offenderBooking.offender.nomsId).isEqualTo("A1069AA")
    assertThat(mark.markType).isEqualTo("TAT")
    assertThat(mark.bodyPart).isEqualTo("ARM")
    assertThat(mark.side).isEqualTo("L")
    assertThat(mark.partOrientation).isEqualTo("UPP")
    assertThat(mark.images.map { it.id }).containsExactlyInAnyOrder(-100L, -101L)

    val mark2 = marks[1]
    assertThat(mark2.bookingId).isEqualTo(-105L)
    assertThat(mark2.sequenceId).isEqualTo(2)
    assertThat(mark2.offenderBooking.offender.nomsId).isEqualTo("A1069AA")
    assertThat(mark2.markType).isEqualTo("TAT")
    assertThat(mark2.bodyPart).isEqualTo("LEG")
    assertThat(mark2.side).isEqualTo("R")
    assertThat(mark2.partOrientation).isEqualTo("UPP")
    assertThat(mark2.images.map { it.id }).containsExactlyInAnyOrder(-102L, -103L)
  }

  @Test
  fun `find specific mark for a prisoner's latest booking`() {
    val mark = repository.getMarkForLatestBookingByOffenderNumberAndSequenceId("A1069AA", 2)!!

    assertThat(mark.bookingId).isEqualTo(-105L)
    assertThat(mark.sequenceId).isEqualTo(2)
    assertThat(mark.offenderBooking.offender.nomsId).isEqualTo("A1069AA")
    assertThat(mark.markType).isEqualTo("TAT")
    assertThat(mark.bodyPart).isEqualTo("LEG")
    assertThat(mark.side).isEqualTo("R")
    assertThat(mark.partOrientation).isEqualTo("UPP")
    assertThat(mark.images.map { it.id }).containsExactlyInAnyOrder(-102L, -103L)
  }
}
