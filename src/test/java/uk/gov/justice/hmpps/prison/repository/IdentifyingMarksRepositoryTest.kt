package uk.gov.justice.hmpps.prison.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.web.config.PersistenceConfigs
import uk.gov.justice.hmpps.test.kotlin.auth.WithMockAuthUser

@ActiveProfiles("test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = [PersistenceConfigs::class])
@WithMockAuthUser
class IdentifyingMarksRepositoryTest {
  @Autowired
  private lateinit var repository: IdentifyingMarksRepository

  @Test
  fun `get identifying marks for latest booking`() {
    val identifyingMarks = repository.findIdentifyingMarksForLatestBooking("A1234AA")

    assertThat(identifyingMarks).hasSize(2)
    val mark1 = identifyingMarks[0]
    assertThat(mark1.markId).isEqualTo(1L)
    assertThat(mark1.prisonerNumber).isEqualTo("A1234AA")
    assertThat(mark1.bookingId).isEqualTo(-1L)
    assertThat(mark1.markType).isEqualTo("TAT")
    assertThat(mark1.bodyPart).isEqualTo("TORSO")
    assertThat(mark1.side).isNull()
    assertThat(mark1.partOrientation).isNull()

    val mark2 = identifyingMarks[1]
    assertThat(mark2.markId).isEqualTo(2L)
    assertThat(mark2.prisonerNumber).isEqualTo("A1234AA")
    assertThat(mark1.bookingId).isEqualTo(-1L)
    assertThat(mark2.markType).isEqualTo("TAT")
    assertThat(mark2.bodyPart).isEqualTo("ARM")
    assertThat(mark2.side).isEqualTo("L")
    assertThat(mark2.partOrientation).isEqualTo("UPP")
  }

  @Test
  fun `get images for identifying mark`() {
    val imageIds = repository.findImageIdsForIdentifyingMark(-1L, 1)
    assertThat(imageIds).hasSize(2)
    assertThat(imageIds.get(0).id).isEqualTo(-101L)
    assertThat(imageIds.get(1).id).isEqualTo(-100L)

    val imageIds2 = repository.findImageIdsForIdentifyingMark(-1L, 2)
    assertThat(imageIds2).hasSize(1)
    assertThat(imageIds2.get(0).id).isEqualTo(-102L)
  }
}
