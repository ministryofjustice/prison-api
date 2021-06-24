package uk.gov.justice.hmpps.prison.service.transformers;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import uk.gov.justice.hmpps.prison.api.model.Employment;
import uk.gov.justice.hmpps.prison.repository.jpa.model.EmploymentSchedule;
import uk.gov.justice.hmpps.prison.repository.jpa.model.EmploymentStatus;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEmployment;
import uk.gov.justice.hmpps.prison.repository.jpa.model.PayPeriod;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ReferenceCode;
import uk.gov.justice.hmpps.prison.service.AddressTransformer;

import static uk.gov.justice.hmpps.prison.util.OptionalUtil.getOrNull;

@Component
public class OffenderEmploymentTransformer implements Converter<OffenderEmployment, Employment> {
    @Override
    public Employment convert(@NotNull final OffenderEmployment offenderEmployment) {
        return Employment.builder()
            .bookingId(offenderEmployment.getId().getBookingId())
            .startDate(offenderEmployment.getStartDate())
            .endDate(offenderEmployment.getEndDate())
            .postType(getOrNull(offenderEmployment.getPostType(), EmploymentStatus::getDescription))
            .employerName(offenderEmployment.getEmployerName())
            .supervisorName(offenderEmployment.getSupervisorName())
            .position(offenderEmployment.getPosition())
            .terminationReason(offenderEmployment.getTerminationReason())
            .wage(offenderEmployment.getWage())
            .wagePeriod(getOrNull(offenderEmployment.getWagePeriod(), PayPeriod::getDescription))
            .occupation(getOrNull(offenderEmployment.getOccupation(), ReferenceCode::getDescription))
            .comment(offenderEmployment.getComment())
            .schedule(getOrNull(offenderEmployment.getScheduleType(), EmploymentSchedule::getDescription))
            .hoursWeek(offenderEmployment.getHoursWeek())
            .isEmployerAware(offenderEmployment.getIsEmployerAware())
            .isEmployerContactable(offenderEmployment.getIsEmployerContactable())
            .addresses(AddressTransformer.translate(offenderEmployment.getAddresses()))
            .build();
    }
}
