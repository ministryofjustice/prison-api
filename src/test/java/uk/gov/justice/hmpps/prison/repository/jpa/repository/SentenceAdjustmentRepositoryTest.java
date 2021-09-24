package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceAdjustment;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")

@AutoConfigureTestDatabase(replace = NONE)
public class SentenceAdjustmentRepositoryTest {

    @Autowired
    private OffenderSentenceAdjustmentRepository repository;

    @Test
    public void findAllForBooking() {
        final var expected = List.of(
                    SentenceAdjustment.builder()
                            .id(-8L)
                            .offenderBooking(OffenderBooking.builder().bookingId(-6L).build())
                            .sentenceAdjustCode("RSR")
                            .active(true)
                            .adjustDays(4)
                            .build(),
                    SentenceAdjustment.builder()
                            .id(-9L)
                        .offenderBooking(OffenderBooking.builder().bookingId(-6L).build())
                            .sentenceAdjustCode("RST")
                            .active(false)
                            .adjustDays(4)
                            .build(),
                    SentenceAdjustment.builder()
                            .id(-10L)
                        .offenderBooking(OffenderBooking.builder().bookingId(-6L).build())
                            .sentenceAdjustCode("RX")
                            .active(true)
                            .adjustDays(4)
                            .build(),
                    SentenceAdjustment.builder()
                            .id(-11L)
                        .offenderBooking(OffenderBooking.builder().bookingId(-6L).build())
                            .sentenceAdjustCode("S240A")
                            .active(false)
                            .adjustDays(4)
                            .build(),
                    SentenceAdjustment.builder()
                            .id(-12L)
                        .offenderBooking(OffenderBooking.builder().bookingId(-6L).build())
                            .sentenceAdjustCode("UR")
                            .active(true)
                            .adjustDays(4)
                            .build(),
                    SentenceAdjustment.builder()
                            .id(-13L)
                        .offenderBooking(OffenderBooking.builder().bookingId(-6L).build())
                            .sentenceAdjustCode("RX")
                            .active(true)
                            .adjustDays(4)
                            .build()
                );

        final var sentenceAdjustments = repository.findAllByOffenderBooking_BookingId(-6L);

        assertThat(sentenceAdjustments).isEqualTo(expected);
    }
}
