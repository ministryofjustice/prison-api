@file:Suppress("ClassName")

package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl
import java.time.LocalDate

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import(HmppsAuthenticationHolder::class, AuditorAwareImpl::class)
class OffenderRepositoryTest {
  @Autowired
  private lateinit var repository: OffenderRepository

  @Nested
  inner class findRootOffenderByNomsId {
    @Test
    fun findByOffenderNomsIdUnique() {
      val offender = repository.findRootOffenderByNomsId("A1234AL").orElseThrow()
      assertThat(offender).extracting({ it.id }, { it.rootOffender.id }).containsExactly(-1012L, -1012L)
      assertThat(offender.allBookings).hasSize(2)
      val latestBooking = offender.allBookings.minBy { it.bookingSequence }
      assertThat(latestBooking.bookingId).isEqualTo(-12L)
    }
  }

  @Nested
  inner class findRootOffenderByNomsIdForUpdate {
    @Test
    fun `test find root offender with bookings`() {
      // this prisoner has two offender records - ensure we get the right one
      val offender = repository.findRootOffenderByNomsIdForUpdate("A1234AL").orElseThrow()
      assertThat(offender.id).isEqualTo(-1012L)
      assertThat(offender.rootOffenderId).isEqualTo(-1012L)
      assertThat(offender.rootOffender.id).isEqualTo(-1012L)

      assertThat(offender.allBookings).hasSizeGreaterThan(0)
    }

    @Test
    fun `test find root offender with no bookings`() {
      // this prisoner has two offender records but no bookings
      val offender = repository.findRootOffenderByNomsIdForUpdate("A1234DD").orElseThrow()
      assertThat(offender.id).isEqualTo(-1056L)
      assertThat(offender.rootOffenderId).isEqualTo(-1056L)
      assertThat(offender.rootOffender.id).isEqualTo(-1056L)

      assertThat(offender.allBookings).hasSize(0)
    }
  }

  @Nested
  inner class findLinkedToLatestBooking {
    @Test
    fun `retrieves the offender record linked to latest booking`() {
      // this prisoner has two offender records - ensure we get the right one
      val offender = repository.findLinkedToLatestBooking("A1234AL").orElseThrow()

      assertThat(offender.id).isEqualTo(-1012L) // Needs to be the one connected to the booking with booking seq of 1
      assertThat(offender.allBookings).hasSizeGreaterThan(0)
    }

    @Test
    fun `returns empty when there is no booking linked to the offender record`() {
      assertThat(repository.findLinkedToLatestBooking("A9880GH")).isEmpty()
    }
  }

  @Nested
  inner class findLinkedToLatestBookingForUpdate {
    @Test
    fun `retrieves the offender record linked to latest booking`() {
      // this prisoner has two offender records - ensure we get the right one
      val offender = repository.findLinkedToLatestBookingForUpdate("A1234AL").orElseThrow()

      assertThat(offender.id).isEqualTo(-1012L) // Needs to be the one connected to the booking with booking seq of 1
      assertThat(offender.allBookings).hasSizeGreaterThan(0)
    }

    @Test
    fun `returns empty when there is no booking linked to the offender record`() {
      assertThat(repository.findLinkedToLatestBookingForUpdate("A9880GH")).isEmpty()
    }
  }

  @Nested
  inner class findByIdForUpdate {
    @Test
    fun `retrieves the offender record`() {
      val offender = repository.findByIdForUpdate(-1012L).orElseThrow()

      assertThat(offender.id).isEqualTo(-1012L)
      assertThat(offender.allBookings).hasSizeGreaterThan(0)
    }

    @Test
    fun `returns empty when offender id not found`() {
      assertThat(repository.findByIdForUpdate(-999)).isEmpty()
    }
  }

  @Nested
  inner class findByNomsId {
    @Test
    fun `retrieves the aliases`() {
      val aliases = repository.findByNomsId("A1234AL")

      assertThat(aliases).hasSize(2)
      assertThat(aliases.map { it.offenderId }).containsExactly(-1013L, -1012L)
      assertThat(aliases.firstOrNull { it.offenderId == -1012L }?.isWorkingName).isTrue()
      assertThat(aliases.firstOrNull { it.offenderId == -1013L }?.isWorkingName).isFalse()
    }

    @Test
    fun `populates all the alias fields`() {
      with(repository.findByNomsId("A1072AA").first()) {
        assertThat(prisonerNumber).isEqualTo("A1072AA")
        assertThat(offenderId).isEqualTo(-1072)
        assertThat(isWorkingName).isTrue()
        assertThat(firstName).isEqualTo("OLD FIRST NAME")
        assertThat(middleName1).isEqualTo("OLD MIDDLE NAME")
        assertThat(middleName2).isEqualTo("OLD SECOND MIDDLE NAME")
        assertThat(lastName).isEqualTo("OLD LAST NAME")
        assertThat(dateOfBirth).isEqualTo(LocalDate.of(2000, 12, 1))
        assertThat(nameType?.code).isEqualTo("CN")
        assertThat(title?.code).isEqualTo("MR")
        assertThat(sex?.code).isEqualTo("M")
        assertThat(ethnicity?.code).isEqualTo("W1")
      }
    }

    @Test
    fun `returns empty when offender id not found`() {
      assertThat(repository.findByNomsId("???")).isEmpty()
    }
  }

  @Nested
  inner class findAliasById {
    @Test
    fun `retrieves the alias`() {
      val alias = repository.findAliasById(-1013L).orElseThrow()
      val workingNameAlias = repository.findAliasById(-1012L).orElseThrow()

      assertThat(alias.offenderId).isEqualTo(-1013L)
      assertThat(alias.prisonerNumber).isEqualTo("A1234AL")
      assertThat(alias.isWorkingName).isFalse()
      assertThat(workingNameAlias.offenderId).isEqualTo(-1012L)
      assertThat(workingNameAlias.prisonerNumber).isEqualTo("A1234AL")
      assertThat(workingNameAlias.isWorkingName).isTrue()
    }

    @Test
    fun `populates all the alias fields`() {
      with(repository.findAliasById(-1072).orElseThrow()) {
        assertThat(prisonerNumber).isEqualTo("A1072AA")
        assertThat(offenderId).isEqualTo(-1072)
        assertThat(isWorkingName).isTrue()
        assertThat(firstName).isEqualTo("OLD FIRST NAME")
        assertThat(middleName1).isEqualTo("OLD MIDDLE NAME")
        assertThat(middleName2).isEqualTo("OLD SECOND MIDDLE NAME")
        assertThat(lastName).isEqualTo("OLD LAST NAME")
        assertThat(dateOfBirth).isEqualTo(LocalDate.of(2000, 12, 1))
        assertThat(nameType?.code).isEqualTo("CN")
        assertThat(title?.code).isEqualTo("MR")
        assertThat(sex?.code).isEqualTo("M")
        assertThat(ethnicity?.code).isEqualTo("W1")
      }
    }

    @Test
    fun `returns empty when offender id not found`() {
      assertThat(repository.findAliasById(-999L)).isEmpty()
    }
  }
}
