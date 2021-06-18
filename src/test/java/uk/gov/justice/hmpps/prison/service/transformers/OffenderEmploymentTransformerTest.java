package uk.gov.justice.hmpps.prison.service.transformers;

import org.junit.Test;
import uk.gov.justice.hmpps.prison.api.model.OffenderEmploymentResponse;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Occupation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEmployment;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEmployment.EmploymentPostType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEmployment.PK;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEmployment.PayPeriodType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEmployment.ScheduleType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEmploymentAddress;
import uk.gov.justice.hmpps.prison.service.AddressTransformer;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class OffenderEmploymentTransformerTest {

    private final OffenderEmploymentTransformer transformer = new OffenderEmploymentTransformer();

    @Test
    public void convert() {
        List<OffenderEmploymentAddress> addresses = Stream.iterate(1L, it -> it + 1).map(it -> {
                var address = new OffenderEmploymentAddress();
                address.setFlat("Flat " + it);
                address.setLocality("Nether Edge");
                address.setPremise("Brook Hamlets");
                address.setStreet("Mayfield Drive");
                address.setPostalCode("B5");
                address.setStartDate(LocalDate.of(2016, 8, 2));
                address.setAddressId(it);
                return address;
            }
        ).limit(5).collect(Collectors.toList());

        var offenderEmployment = new OffenderEmployment(
            new PK(1L, 2L),
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
        );

        var actual = transformer.convert(offenderEmployment);

        var expected = new OffenderEmploymentResponse(
            offenderEmployment.getId().getBookingId(),
            offenderEmployment.getStartDate(),
            offenderEmployment.getEndDate(),
            offenderEmployment.getPostType().getDescription(),
            offenderEmployment.getEmployerName(),
            offenderEmployment.getSupervisorName(),
            offenderEmployment.getPosition(),
            offenderEmployment.getTerminationReason(),
            offenderEmployment.getWage(),
            offenderEmployment.getWagePeriod().getDescription(),
            offenderEmployment.getOccupation().getDescription(),
            offenderEmployment.getComment(),
            offenderEmployment.getScheduleType().getDescription(),
            offenderEmployment.getHoursWeek(),
            offenderEmployment.getIsEmployerAware(),
            offenderEmployment.getIsEmployerContactable(),
            AddressTransformer.translate(addresses)
        );


        assertThat(actual).isEqualTo(expected);
    }
}