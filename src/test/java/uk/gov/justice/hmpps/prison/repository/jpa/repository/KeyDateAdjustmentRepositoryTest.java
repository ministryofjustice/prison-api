package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.prison.repository.jpa.model.KeyDateAdjustment;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")

@AutoConfigureTestDatabase(replace = NONE)
public class KeyDateAdjustmentRepositoryTest {

    @Autowired
    private OffenderKeyDateAdjustmentRepository repository;

    @Test
    public void findAllForBooking() {
        final var expected = List.of(
                            KeyDateAdjustment
                                    .builder()
                                    .id(-8L)
                                    .sentenceAdjustCode("ADA")
                                    .active(true)
                                .offenderBooking(OffenderBooking.builder().bookingId(-6L).build())
                                    .adjustDays(4)
                                    .build(),
                            KeyDateAdjustment
                                    .builder()
                                    .id(-9L)
                                    .sentenceAdjustCode("ADA")
                                    .active(false)
                                .offenderBooking(OffenderBooking.builder().bookingId(-6L).build())
                                    .adjustDays(9)
                                    .build(),
                            KeyDateAdjustment
                                    .builder()
                                    .id(-10L)
                                    .sentenceAdjustCode("ADA")
                                    .active(true)
                                .offenderBooking(OffenderBooking.builder().bookingId(-6L).build())
                                    .adjustDays(13)
                                    .build(),
                            KeyDateAdjustment
                                    .builder()
                                    .id(-11L)
                                    .sentenceAdjustCode("UAL")
                                    .active(false)
                                .offenderBooking(OffenderBooking.builder().bookingId(-6L).build())
                                    .adjustDays(1)
                                    .build(),
                            KeyDateAdjustment
                                    .builder()
                                    .id(-12L)
                                    .sentenceAdjustCode("RADA")
                                    .active(true)
                                .offenderBooking(OffenderBooking.builder().bookingId(-6L).build())
                                    .adjustDays(2)
                                    .build(),
                            KeyDateAdjustment
                                    .builder()
                                    .id(-13L)
                                    .sentenceAdjustCode("UAL")
                                    .active(true)
                                .offenderBooking(OffenderBooking.builder().bookingId(-6L).build())
                                    .adjustDays(7)
                                    .build()
                            );

        final var keyDateAdjustments = repository.findAllByOffenderBooking_BookingId(-6L);

        assertThat(keyDateAdjustments).isEqualTo(expected);
    }
}
