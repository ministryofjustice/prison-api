package uk.gov.justice.hmpps.prison.service.reference

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.prison.api.model.HOCodeDto
import uk.gov.justice.hmpps.prison.api.model.OffenceDto
import uk.gov.justice.hmpps.prison.api.model.StatuteDto
import uk.gov.justice.hmpps.prison.repository.jpa.model.HOCode
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offence
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offence.PK
import uk.gov.justice.hmpps.prison.repository.jpa.model.Statute
import uk.gov.justice.hmpps.prison.repository.jpa.repository.HOCodeRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenceRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StatuteRepository
import uk.gov.justice.hmpps.prison.service.EntityAlreadyExistsException
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException
import java.time.LocalDate
import java.util.Optional

internal class OffenceServiceTest {
  private val offenceRepository: OffenceRepository = mock()
  private val hoCodeRepository: HOCodeRepository = mock()
  private val statuteRepository: StatuteRepository = mock()
  private val service = OffenceService(
    offenceRepository,
    hoCodeRepository,
    statuteRepository,
  )

  @Nested
  @DisplayName("HO Codes related tests")
  inner class HOCodesTest {
    @Test
    internal fun `Will create a new HO Code`() {
      service.createHomeOfficeCode(
        HOCodeDto.builder()
          .code("2XX")
          .description("2XX Description")
          .build()
      )

      verify(hoCodeRepository, times(1)).save(
        HOCode.builder()
          .code("2XX")
          .description("2XX Description")
          .build()
      )
    }

    @Test
    internal fun `Will fail when trying to create a HO Code if HO Code already exists`() {
      val hoCode = HOCode.builder().code("2XX").build()
      whenever(hoCodeRepository.findById("2XX")).thenReturn(Optional.of(hoCode))

      assertThrows<EntityAlreadyExistsException> {
        service.createHomeOfficeCode(HOCodeDto.builder().code("2XX").build())
      }
    }
  }

  @Nested
  @DisplayName("Statutes related tests")
  inner class StatutesTest {
    @Test
    internal fun `Will create a new Statute`() {
      service.createStatute(
        StatuteDto.builder()
          .code("2XX")
          .description("2XX Description")
          .build()
      )

      verify(statuteRepository, times(1)).save(
        Statute.builder()
          .code("2XX")
          .description("2XX Description")
          .build()
      )
    }

    @Test
    internal fun `Will fail when trying to create a Statute if the code already exists`() {
      val statute = Statute.builder().code("2XX").build()
      whenever(statuteRepository.findById("2XX")).thenReturn(Optional.of(statute))

      assertThrows<EntityAlreadyExistsException> {
        service.createStatute(StatuteDto.builder().code("2XX").build())
      }
    }
  }

  @Nested
  @DisplayName("Create Offence related tests")
  inner class CreateOffenceTest {
    private val statute: Statute = Statute.builder()
      .code("2XX")
      .description("2XX Description")
      .build()

    @Test
    internal fun `Will create a new Offence`() {
      whenever(statuteRepository.findById("STA1")).thenReturn(Optional.of(statute))

      service.createOffence(
        OffenceDto.builder()
          .code("2XX")
          .statuteCode(StatuteDto.builder().code("STA1").build())
          .description("2XX Description")
          .build()
      )

      verify(offenceRepository, times(1)).save(
        Offence.builder()
          .code("2XX")
          .description("2XX Description")
          .statute(statute)
          .build()
      )
    }

    @Test
    internal fun `Will fail when trying to create a Offence if the code already exists`() {
      val offence = Offence.builder().code("2XX").build()
      whenever(offenceRepository.findById(PK("2XX", statute.code))).thenReturn(Optional.of(offence))
      whenever(statuteRepository.findById("STA1")).thenReturn(Optional.of(statute))

      assertThrows<EntityAlreadyExistsException> {
        service.createOffence(
          OffenceDto.builder()
            .code("2XX")
            .statuteCode(StatuteDto.builder().code("STA1").build())
            .build()
        )
      }
    }

    @Test
    internal fun `Will fail when trying to create a Offence if the associated statute does not exist`() {
      whenever(offenceRepository.findById(PK("2XX", statute.code))).thenReturn(Optional.empty())
      whenever(statuteRepository.findById("STA1")).thenReturn(Optional.empty())

      assertThrows<EntityNotFoundException> {
        service.createOffence(
          OffenceDto.builder()
            .code("2XX")
            .statuteCode(StatuteDto.builder().code("STA1").build())
            .build()
        )
      }
    }

    @Test
    internal fun `Will fail when trying to create a Offence if the associated hoCode does not exist`() {
      whenever(offenceRepository.findById(PK("2XX", statute.code))).thenReturn(Optional.empty())
      whenever(statuteRepository.findById("STA1")).thenReturn(Optional.of(statute))
      whenever(hoCodeRepository.findById("HO1")).thenReturn(Optional.empty())

      assertThrows<EntityNotFoundException> {
        service.createOffence(
          OffenceDto.builder()
            .code("2XX")
            .hoCode(HOCodeDto.builder().code("HO1").build())
            .statuteCode(StatuteDto.builder().code("STA1").build())
            .build()
        )
      }
    }
  }

  @Nested
  @DisplayName("Update Offence related tests")
  inner class UodateOffenceTest {
    private val statute: Statute = Statute.builder()
      .code("2XX")
      .description("2XX Description")
      .build()

    @Test
    internal fun `Will update an offence`() {
      val offence = Offence.builder().code("2XX").build()
      whenever(offenceRepository.findById(PK("2XX", "STA1"))).thenReturn(Optional.of(offence))
      val expiryDate = LocalDate.of(2020, 1, 1)

      service.updateOffence(
        OffenceDto.builder()
          .code("2XX")
          .statuteCode(StatuteDto.builder().code("STA1").build())
          .description("2XX Update")
          .severityRanking("99")
          .listSequence(99)
          .activeFlag("N")
          .expiryDate(expiryDate)
          .build()
      )

      verify(offenceRepository, times(1)).save(
        Offence.builder()
          .code("2XX")
          .description("2XX Update")
          .severityRanking("99")
          .listSequence(99)
          .active(false)
          .expiryDate(expiryDate)
          .build()
      )
    }

    @Test
    internal fun `Will fail when trying to update a Offence if the offence does not exist`() {
      whenever(offenceRepository.findById(PK("2XX", "STA1"))).thenReturn(Optional.empty())

      assertThrows<EntityNotFoundException> {
        service.updateOffence(
          OffenceDto.builder()
            .code("2XX")
            .statuteCode(StatuteDto.builder().code("STA1").build())
            .build()
        )
      }
    }

    @Test
    internal fun `Will fail when trying to update a Offence if the associated hoCode does not exist`() {
      val offence = Offence.builder().code("2XX").build()
      whenever(offenceRepository.findById(PK("2XX", "STA1"))).thenReturn(Optional.of(offence))
      whenever(statuteRepository.findById("STA1")).thenReturn(Optional.of(statute))
      whenever(hoCodeRepository.findById("HO1")).thenReturn(Optional.empty())

      assertThrows<EntityNotFoundException> {
        service.updateOffence(
          OffenceDto.builder()
            .code("2XX")
            .hoCode(HOCodeDto.builder().code("HO1").build())
            .statuteCode(StatuteDto.builder().code("STA1").build())
            .build()
        )
      }
    }
  }
}
