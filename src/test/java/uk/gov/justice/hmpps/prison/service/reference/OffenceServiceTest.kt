package uk.gov.justice.hmpps.prison.service.reference

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import uk.gov.justice.hmpps.prison.api.model.HOCodeDto
import uk.gov.justice.hmpps.prison.api.model.OffenceActivationDto
import uk.gov.justice.hmpps.prison.api.model.OffenceDto
import uk.gov.justice.hmpps.prison.api.model.OffenceToScheduleMappingDto
import uk.gov.justice.hmpps.prison.api.model.Schedule.SCHEDULE_15
import uk.gov.justice.hmpps.prison.api.model.StatuteDto
import uk.gov.justice.hmpps.prison.repository.jpa.model.HOCode
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offence
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offence.PK
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenceIndicator
import uk.gov.justice.hmpps.prison.repository.jpa.model.Statute
import uk.gov.justice.hmpps.prison.repository.jpa.repository.HOCodeRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenceIndicatorRepository
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
  private val offenceIndicatorRepository: OffenceIndicatorRepository = mock()
  private val service = OffenceService(
    offenceRepository,
    hoCodeRepository,
    statuteRepository,
    offenceIndicatorRepository,
  )

  @Nested
  @DisplayName("HO Codes related tests")
  inner class HOCodesTest {
    @Test
    internal fun `Will create a new HO Code and skip any HO Codes that already exist`() {
      val existingHoCodeDto = HOCodeDto.builder().code("1XX").build()
      val existingHoCode = HOCode.builder().code("1XX").build()
      val newHoCodeDto = HOCodeDto.builder().code("2XX").description("2XX Description").build()
      val newHoCode = HOCode.builder().code("2XX").description("2XX Description").build()

      whenever(hoCodeRepository.findById("1XX")).thenReturn(Optional.of(existingHoCode))
      service.createHomeOfficeCodes(listOf(existingHoCodeDto, newHoCodeDto))

      verify(hoCodeRepository, times(1)).findById("2XX")
      verify(hoCodeRepository, times(1)).findById("1XX")
      verify(hoCodeRepository, times(1)).save(newHoCode)
      verifyNoMoreInteractions(hoCodeRepository)
    }
  }

  @Nested
  @DisplayName("Statutes related tests")
  inner class StatutesTest {
    @Test
    internal fun `Will create a new Statute and skip any Statutes that already exist`() {
      val existingStatuteDto = StatuteDto.builder().code("1XX").build()
      val existingStatute = Statute.builder().code("1XX").build()
      val newStatuteDto = StatuteDto.builder().code("2XX").description("2XX Description").build()
      val newStatute = Statute.builder().code("2XX").description("2XX Description").build()

      whenever(statuteRepository.findById("1XX")).thenReturn(Optional.of(existingStatute))
      service.createStatutes(listOf(existingStatuteDto, newStatuteDto))

      verify(statuteRepository, times(1)).findById("2XX")
      verify(statuteRepository, times(1)).findById("1XX")
      verify(statuteRepository, times(1)).save(newStatute)
      verifyNoMoreInteractions(statuteRepository)
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

      service.createOffences(
        listOf(
          OffenceDto.builder()
            .code("2XX")
            .statuteCode(StatuteDto.builder().code("STA1").build())
            .description("2XX Description")
            .build(),
        ),
      )

      verify(offenceRepository, times(1)).save(
        Offence.builder()
          .code("2XX")
          .description("2XX Description")
          .statute(statute)
          .build(),
      )
    }

    @Test
    internal fun `Will fail when trying to create a Offence if the code already exists`() {
      val offence = Offence.builder().code("2XX").build()
      whenever(offenceRepository.findById(PK("2XX", statute.code))).thenReturn(Optional.of(offence))
      whenever(statuteRepository.findById("STA1")).thenReturn(Optional.of(statute))

      assertThrows<EntityAlreadyExistsException> {
        service.createOffences(
          listOf(
            OffenceDto.builder()
              .code("2XX")
              .statuteCode(StatuteDto.builder().code("STA1").build())
              .build(),
          ),
        )
      }
    }

    @Test
    internal fun `Will fail when trying to create a Offence if the associated statute does not exist`() {
      whenever(offenceRepository.findById(PK("2XX", statute.code))).thenReturn(Optional.empty())
      whenever(statuteRepository.findById("STA1")).thenReturn(Optional.empty())

      assertThrows<EntityNotFoundException> {
        service.createOffences(
          listOf(
            OffenceDto.builder()
              .code("2XX")
              .statuteCode(StatuteDto.builder().code("STA1").build())
              .build(),
          ),
        )
      }
    }

    @Test
    internal fun `Will fail when trying to create a Offence if the associated hoCode does not exist`() {
      whenever(offenceRepository.findById(PK("2XX", statute.code))).thenReturn(Optional.empty())
      whenever(statuteRepository.findById("STA1")).thenReturn(Optional.of(statute))
      whenever(hoCodeRepository.findById("HO1")).thenReturn(Optional.empty())

      assertThrows<EntityNotFoundException> {
        service.createOffences(
          listOf(
            OffenceDto.builder()
              .code("2XX")
              .hoCode(HOCodeDto.builder().code("HO1").build())
              .statuteCode(StatuteDto.builder().code("STA1").build())
              .build(),
          ),
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

      service.updateOffences(
        listOf(
          OffenceDto.builder()
            .code("2XX")
            .statuteCode(StatuteDto.builder().code("STA1").build())
            .description("2XX Update")
            .severityRanking("99")
            .listSequence(99)
            .activeFlag("N")
            .expiryDate(expiryDate)
            .build(),
        ),
      )

      verify(offenceRepository, times(1)).save(
        Offence.builder()
          .code("2XX")
          .description("2XX Update")
          .severityRanking("99")
          .listSequence(99)
          .active(false)
          .expiryDate(expiryDate)
          .build(),
      )
    }

    @Test
    internal fun `Will fail when trying to update a Offence if the offence does not exist`() {
      whenever(offenceRepository.findById(PK("2XX", "STA1"))).thenReturn(Optional.empty())

      assertThrows<EntityNotFoundException> {
        service.updateOffences(
          listOf(
            OffenceDto.builder()
              .code("2XX")
              .statuteCode(StatuteDto.builder().code("STA1").build())
              .build(),
          ),
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
        service.updateOffences(
          listOf(
            OffenceDto.builder()
              .code("2XX")
              .hoCode(HOCodeDto.builder().code("HO1").build())
              .statuteCode(StatuteDto.builder().code("STA1").build())
              .build(),
          ),
        )
      }
    }
  }

  @Nested
  @DisplayName("Get offences related tests")
  inner class GetOffencesTest {
    @Test
    internal fun `Fetch offences where the code starts  with the passed string`() {
      val pageable: Pageable = PageRequest.of(0, 20)
      val statute = Statute("STA1", "STA1-desc", "UK", true)
      val offence = Offence("ACODE", statute, null, "A-Desc", null, true, null, null, null)
      val offences = listOf(offence)
      whenever(offenceRepository.findAllByCodeStartsWithIgnoreCase("A", pageable)).thenReturn(PageImpl(offences))

      val offencesReturned = service.getOffencesThatStartWith("A", pageable)

      assertThat(offencesReturned).containsOnly(
        OffenceDto.builder()
          .code("ACODE")
          .description("A-Desc")
          .activeFlag("Y")
          .statuteCode(
            StatuteDto.builder()
              .code("STA1")
              .description("STA1-desc")
              .legislatingBodyCode("UK")
              .activeFlag("Y")
              .build(),
          )
          .build(),
      )
    }
  }

  @Nested
  @DisplayName("Link and unlink offences from schedules tests")
  inner class LinkAndUnlinkOffencesTest {
    private val murderOffence = Offence.builder()
      .code("COML025")
      .description("Murder")
      .build()

    @Test
    internal fun `Link offences to schedules`() {
      val mappingDto1 = OffenceToScheduleMappingDto("COML025", SCHEDULE_15)
      val mappingDto2 = OffenceToScheduleMappingDto("COML026", SCHEDULE_15)
      val mappingDtos = listOf(mappingDto1, mappingDto2)

      val pks = mappingDtos.map { PK(it.offenceCode, it.statuteCode) }.toSet()
      whenever(offenceRepository.findAllById(pks)).thenReturn(listOf(murderOffence))
      whenever(offenceIndicatorRepository.existsByIndicatorCodeAndOffence_Code(SCHEDULE_15.code, "COML025")).thenReturn(
        false,
      )

      service.linkOffencesToSchedules(mappingDtos)

      verify(offenceIndicatorRepository, times(1)).existsByIndicatorCodeAndOffence_Code(SCHEDULE_15.code, "COML025")
      verify(offenceIndicatorRepository, times(1)).saveAll(
        listOf(
          OffenceIndicator.builder()
            .offence(murderOffence)
            .indicatorCode(SCHEDULE_15.code)
            .build(),
        ),
      )
      verifyNoMoreInteractions(offenceIndicatorRepository)
    }

    @Test
    internal fun `Attempting to link offences to schedules where the offence is already linked will be ignored (no duplicate records should be created)`() {
      val mappingDto1 = OffenceToScheduleMappingDto("COML025", SCHEDULE_15)
      val mappingDto2 = OffenceToScheduleMappingDto("COML026", SCHEDULE_15)
      val mappingDtos = listOf(mappingDto1, mappingDto2)
      val manslaughterOffence = Offence.builder()
        .code("COML026")
        .description("Manslaughter")
        .build()

      val pks = mappingDtos.map { PK(it.offenceCode, it.statuteCode) }.toSet()
      whenever(offenceRepository.findAllById(pks)).thenReturn(listOf(murderOffence, manslaughterOffence))
      whenever(offenceIndicatorRepository.existsByIndicatorCodeAndOffence_Code(SCHEDULE_15.code, "COML025")).thenReturn(
        true,
      )
      whenever(offenceIndicatorRepository.existsByIndicatorCodeAndOffence_Code(SCHEDULE_15.code, "COML026")).thenReturn(
        false,
      )

      service.linkOffencesToSchedules(mappingDtos)

      verify(offenceIndicatorRepository, times(1)).existsByIndicatorCodeAndOffence_Code(SCHEDULE_15.code, "COML025")
      verify(offenceIndicatorRepository, times(1)).existsByIndicatorCodeAndOffence_Code(SCHEDULE_15.code, "COML026")
      verify(offenceIndicatorRepository, times(1)).saveAll(
        listOf(
          OffenceIndicator.builder()
            .offence(manslaughterOffence)
            .indicatorCode(SCHEDULE_15.code)
            .build(),
        ),
      )
      verifyNoMoreInteractions(offenceIndicatorRepository)
    }

    @Test
    internal fun `Unlink offences from schedules`() {
      val mappingDto = OffenceToScheduleMappingDto("COML025", SCHEDULE_15)
      service.unlinkOffencesFromSchedules(listOf(mappingDto))

      verify(offenceIndicatorRepository, times(1)).deleteByIndicatorCodeAndOffence_Code(
        mappingDto.schedule.code,
        mappingDto.offenceCode,
      )
    }
  }

  @Nested
  @DisplayName("Activate / deactivate offences test")
  inner class ActivateOrDeactivateOffencesTest {
    private val murderOffence = Offence.builder()
      .code("COML025")
      .description("Murder")
      .build()

    @Test
    internal fun `Activate an offence in NOMIS`() {
      val mappingDto = OffenceActivationDto(offenceCode = "COML025", statuteCode = "COML", activationFlag = true)
      whenever(offenceRepository.findById(PK("COML025", "COML"))).thenReturn(Optional.of(murderOffence))

      service.updateOffenceActiveFlag(mappingDto)

      verify(offenceRepository).save(
        Offence.builder()
          .code("COML025")
          .description("Murder")
          .active(true)
          .expiryDate(null)
          .build(),
      )
    }

    @Test
    internal fun `Deactivate an offence in NOMIS`() {
      val mappingDto = OffenceActivationDto(offenceCode = "COML025", statuteCode = "COML", activationFlag = false)
      whenever(offenceRepository.findById(PK("COML025", "COML"))).thenReturn(Optional.of(murderOffence))

      service.updateOffenceActiveFlag(mappingDto)

      verify(offenceRepository).save(
        Offence.builder()
          .code("COML025")
          .description("Murder")
          .active(false)
          .expiryDate(LocalDate.now())
          .build(),
      )
    }
  }
}
