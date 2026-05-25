package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import uk.gov.justice.hmpps.prison.repository.jpa.model.Gender
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl
import uk.gov.justice.hmpps.test.kotlin.auth.WithMockAuthUser

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(HmppsAuthenticationHolder::class, AuditorAwareImpl::class)
@WithMockAuthUser
class PrisonerRepositoryTest(
  @Autowired private val repository: OffenderRepository,
  @Autowired private val genderRepository: ReferenceCodeRepository<Gender?>,
  @Autowired private val entityManager: TestEntityManager,
) {

  @Test
  fun save_and_retrieval_of_male_offender() {
    val maleOffender = repository.save(
      Offender
        .builder()
        .gender(genderRepository.findById(Gender.MALE).orElseThrow())
        .lastName("BLOGGS")
        .lastNameKey("BLOGGS")
        .nomsId("NOMS_ID")
        .build(),
    )

    entityManager.flush()

    assertThat(repository.findById(maleOffender.id).orElseThrow()).isEqualTo(maleOffender)
  }

  @Test
  fun save_and_retrieval_of_female_offender() {
    val femaleOffender = repository.save(
      Offender
        .builder()
        .gender(genderRepository.findById(Gender.FEMALE).orElseThrow())
        .lastName("BLOGGS")
        .lastNameKey("BLOGGS")
        .nomsId("NOMS_ID")
        .build(),
    )

    entityManager.flush()

    assertThat(repository.findById(femaleOffender.id).orElseThrow()).isEqualTo(femaleOffender)
  }
}
