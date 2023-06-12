package uk.gov.justice.hmpps.prison.repository.jpa.repository

import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceTerm
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceTerm.PK
import java.time.LocalDate

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
public class SentenceTermRepositoryTest {

  @Autowired
  lateinit var repository: SentenceTermRepository

  @Test
  fun getSingleSentenceTerm() {
    val sentenceTerms = repository.findByOffenderBookingBookingId(-1L)

    assertThat(sentenceTerms).hasSize(1)
    assertThat(sentenceTerms)
      .containsExactly(
        SentenceTerm.builder()
          .id(PK(-1L, 1, 1))
          .years(1)
          .sentenceTermCode("IMP")
          .startDate(LocalDate.of(2017, 3, 25))
          .lifeSentenceFlag("N")
          .build(),
      )
  }

  @Test
  fun getMultipleSentenceTerms() {
    val sentenceTerms = repository.findByOffenderBookingBookingId(-2L)
    assertThat(sentenceTerms).hasSize(4)
  }
}
