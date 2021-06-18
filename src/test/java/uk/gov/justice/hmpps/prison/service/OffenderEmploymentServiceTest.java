package uk.gov.justice.hmpps.prison.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Occupation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEmployment;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEmployment.EmploymentPostType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEmployment.PK;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEmployment.PayPeriodType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEmployment.ScheduleType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEmploymentAddress;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderEmploymentRepository;
import uk.gov.justice.hmpps.prison.service.transformers.OffenderEmploymentTransformer;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OffenderEmploymentServiceTest {

    @Mock
    private OffenderEmploymentRepository repository;

    private final OffenderEmploymentTransformer transformer = new OffenderEmploymentTransformer();

    private OffenderEmploymentService service;


    @BeforeEach
    void setup() {
        service = new OffenderEmploymentService(repository, transformer);
    }


    @Test
    public void getOffenderEmployments() {
        var nomisId = "abc";
        var pageRequest = PageRequest.of(0, 10);
        var offenderEmployments = generateMockData();

        when(repository.findAllByNomisId(nomisId, pageRequest)).thenReturn(offenderEmployments);

        var actual = service.getOffenderEmployments(nomisId, pageRequest).getContent();
        var expected = offenderEmployments.getContent().stream().map(transformer::convert).collect(Collectors.toList());

        assertThat(actual).isEqualTo(expected);
    }

    private Page<OffenderEmployment> generateMockData() {

        var address = new OffenderEmploymentAddress();
        address.setFlat("Flat 1");
        address.setLocality("Nether Edge");
        address.setPremise("Brook Hamlets");
        address.setStreet("Mayfield Drive");
        address.setPostalCode("B5");
        address.setStartDate(LocalDate.of(2016, 8, 2));
        address.setAddressId(1L);

        List<OffenderEmploymentAddress> addresses = List.of(address);

        var results = LongStream
            .rangeClosed(1, 2)
            .mapToObj(it -> new OffenderEmployment(
                new PK(it, 2L),
                LocalDate.now().minusDays(5),
                LocalDate.now(),
                EmploymentPostType.CAS,
                "greggs",
                "lorem",
                "ipsum",
                "end of program",
                5000.55,
                PayPeriodType.WEEK,
                new Occupation("COOK", "Cook"),
                "Good cook",
                ScheduleType.FTNIGHT,
                30,
                true,
                false,
                addresses
            ))
            .collect(Collectors.toList());


        return new PageImpl(results);
    }
}