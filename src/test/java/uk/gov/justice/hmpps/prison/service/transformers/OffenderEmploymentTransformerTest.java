package uk.gov.justice.hmpps.prison.service.transformers;

import org.junit.Test;
import uk.gov.justice.hmpps.prison.repository.jpa.model.EmploymentStatus;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Occupation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEmployment;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEmployment.PK;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEmployment.PayPeriodType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEmployment.ScheduleType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEmploymentAddress;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class OffenderEmploymentTransformerTest {

    private final OffenderEmploymentTransformer transformer = new OffenderEmploymentTransformer();

    @Test
    public void convert() {
        final var address = OffenderEmploymentAddress.builder()
            .addressId(1L)
            .build();

        final var offenderEmployment = OffenderEmployment
            .builder()
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

        final var actual = transformer.convert(offenderEmployment);

        assertThat(actual.getBookingId()).isEqualTo(offenderEmployment.getId().getBookingId());
        assertThat(actual.getStartDate()).isEqualTo(offenderEmployment.getStartDate());
        assertThat(actual.getEndDate()).isEqualTo(offenderEmployment.getEndDate());
        assertThat(actual.getPostType()).isEqualTo(offenderEmployment.getPostType().getDescription());
        assertThat(actual.getEmployerName()).isEqualTo(offenderEmployment.getEmployerName());
        assertThat(actual.getSupervisorName()).isEqualTo(offenderEmployment.getSupervisorName());
        assertThat(actual.getPosition()).isEqualTo(offenderEmployment.getPosition());
        assertThat(actual.getTerminationReason()).isEqualTo(offenderEmployment.getTerminationReason());
        assertThat(actual.getWage()).isEqualTo(offenderEmployment.getWage());
        assertThat(actual.getWagePeriod()).isEqualTo(offenderEmployment.getWagePeriod().getDescription());
        assertThat(actual.getOccupation()).isEqualTo(offenderEmployment.getOccupation().getDescription());
        assertThat(actual.getComment()).isEqualTo(offenderEmployment.getComment());
        assertThat(actual.getSchedule()).isEqualTo(offenderEmployment.getScheduleType().getDescription());
        assertThat(actual.getHoursWeek()).isEqualTo(offenderEmployment.getHoursWeek());
        assertThat(actual.getIsEmployerAware()).isEqualTo(offenderEmployment.getIsEmployerAware());
        assertThat(actual.getIsEmployerContactable()).isEqualTo(offenderEmployment.getIsEmployerContactable());

        assertThat(actual.getAddresses()).hasSize(1);
        assertThat(actual.getAddresses().get(0).getAddressId()).isEqualTo(address.getAddressId());
    }

    @Test
    public void convertWithMissingNestedFields() {

        final var offenderEmployment = OffenderEmployment
            .builder()
            .id(new PK(1L, 2L))
            .startDate(LocalDate.now().minusDays(5))
            .endDate(LocalDate.now())
            .employerName("greggs")
            .supervisorName("lorem")
            .position("ipsum")
            .terminationReason("end of program")
            .wage(BigDecimal.valueOf(5000.55))
            .comment("Good cook")
            .hoursWeek(30)
            .isEmployerAware(true)
            .isEmployerContactable(false)
            .build();

        final var actual = transformer.convert(offenderEmployment);

        assertThat(actual.getBookingId()).isEqualTo(offenderEmployment.getId().getBookingId());
        assertThat(actual.getStartDate()).isEqualTo(offenderEmployment.getStartDate());
        assertThat(actual.getEndDate()).isEqualTo(offenderEmployment.getEndDate());
        assertThat(actual.getPostType()).isNull();
        assertThat(actual.getEmployerName()).isEqualTo(offenderEmployment.getEmployerName());
        assertThat(actual.getSupervisorName()).isEqualTo(offenderEmployment.getSupervisorName());
        assertThat(actual.getPosition()).isEqualTo(offenderEmployment.getPosition());
        assertThat(actual.getTerminationReason()).isEqualTo(offenderEmployment.getTerminationReason());
        assertThat(actual.getWage()).isEqualTo(offenderEmployment.getWage());
        assertThat(actual.getWagePeriod()).isNull();
        assertThat(actual.getOccupation()).isNull();
        assertThat(actual.getComment()).isEqualTo(offenderEmployment.getComment());
        assertThat(actual.getSchedule()).isNull();
        assertThat(actual.getHoursWeek()).isEqualTo(offenderEmployment.getHoursWeek());
        assertThat(actual.getIsEmployerAware()).isEqualTo(offenderEmployment.getIsEmployerAware());
        assertThat(actual.getIsEmployerContactable()).isEqualTo(offenderEmployment.getIsEmployerContactable());

        assertThat(actual.getAddresses()).hasSize(0);
    }
}