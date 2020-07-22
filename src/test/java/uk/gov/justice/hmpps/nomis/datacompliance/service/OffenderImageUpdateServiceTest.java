package uk.gov.justice.hmpps.nomis.datacompliance.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderWithImage;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.repository.OffenderImageUpdateRepository;
import uk.gov.justice.hmpps.prison.api.model.OffenderNumber;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OffenderImageUpdateServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.now();
    private static final String OFFENDER_NUMBER = "A1234AA";

    @Mock
    private OffenderImageUpdateRepository repository;

    private OffenderImageUpdateService service;

    @BeforeEach
    void setUp() {
        service = new OffenderImageUpdateService(repository);
    }

    @Test
    void getOffendersWithImages() {

        when(repository.getOffendersWithImagesCapturedBetween(NOW, NOW.plusSeconds(1), Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of(new OffenderWithImage(OFFENDER_NUMBER))));

        final var offenders = service.getOffendersWithImagesCapturedBetween(NOW, NOW.plusSeconds(1), Pageable.unpaged());

        assertThat(offenders).isEqualTo(new PageImpl<>(List.of(new OffenderNumber(OFFENDER_NUMBER))));
    }

    @Test
    void getOffendersWithImagesReturnsEmpty() {

        when(repository.getOffendersWithImagesCapturedBetween(NOW, NOW.plusSeconds(1), Pageable.unpaged()))
                .thenReturn(Page.empty());

        assertThat(service.getOffendersWithImagesCapturedBetween(NOW, NOW.plusSeconds(1), Pageable.unpaged()))
                .isEmpty();
    }
}
