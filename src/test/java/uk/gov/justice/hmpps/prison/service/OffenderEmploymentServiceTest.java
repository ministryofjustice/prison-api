package uk.gov.justice.hmpps.prison.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import uk.gov.justice.hmpps.prison.repository.jpa.model.EmploymentStatus;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Occupation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEmployment;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEmployment.PK;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEmployment.PayPeriodType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEmployment.ScheduleType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEmploymentAddress;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderEmploymentRepository;
import uk.gov.justice.hmpps.prison.service.transformers.OffenderEmploymentTransformer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OffenderEmploymentServiceTest {

    private final String nomisId = "abc";

    private final PageRequest pageRequest = PageRequest.of(0, 10);

    private final OffenderEmploymentAddress address = OffenderEmploymentAddress.builder()
        .flat("Flat 1")
        .locality("Nether Edge")
        .premise("Brook Hamlets")
        .street("Mayfield Drive")
        .postalCode("B5")
        .startDate(LocalDate.of(2016, 8, 2))
        .addressId(1L)
        .build();

    private final OffenderEmployment offenderEmployment = OffenderEmployment.builder()
        .id(new PK(1L, 2L))
        .startDate(LocalDate.now().minusDays(5))
        .endDate(LocalDate.now())
        .postType(new EmploymentStatus("CAS", "Casual"))
        .employerName("greggs")
        .supervisorName("lorem")
        .position("ipsum")
        .terminationReason("end of program")
        .wage(BigDecimal.valueOf(5000.55))
        .wagePeriod(PayPeriodType.WEEK)
        .occupation(new Occupation("COOK", "Cook"))
        .comment("Good cook")
        .scheduleType(ScheduleType.FTNIGHT)
        .hoursWeek(30)
        .isEmployerAware(true)
        .isEmployerContactable(false)
        .addresses(List.of(address))
        .build();

    @Mock
    private OffenderEmploymentTransformer transformer;

    @Mock
    private OffenderEmploymentRepository repository;


    private OffenderEmploymentService service;

    @BeforeEach
    void setup() {
        service = new OffenderEmploymentService(repository, transformer);
    }


    @Test
    public void getOffenderEmployments() {

        final var employments = List.of(offenderEmployment, offenderEmployment);

        when(repository.findAllByNomisId(nomisId, pageRequest)).thenReturn(new PageImpl<>(employments));

        service.getOffenderEmployments(nomisId, pageRequest);

        verify(transformer, times(2)).convert(offenderEmployment);

    }
}