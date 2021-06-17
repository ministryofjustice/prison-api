package uk.gov.justice.hmpps.prison.service.transformers;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import uk.gov.justice.hmpps.prison.api.model.OffenderEmploymentResponse;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEmployment;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEmployment.EmploymentPostType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEmployment.PayPeriodType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEmployment.ScheduleType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ReferenceCode;
import uk.gov.justice.hmpps.prison.service.AddressTransformer;

import java.util.Optional;
import java.util.function.Function;

@Component
public class OffenderEmploymentTransformer implements Converter<OffenderEmployment, OffenderEmploymentResponse> {
    @Override
    public OffenderEmploymentResponse convert(@NotNull final OffenderEmployment offenderEmployment) {
        return new OffenderEmploymentResponse(
            offenderEmployment.getId().getBookingId(),
            offenderEmployment.getStartDate(),
            offenderEmployment.getEndDate(),
            getOrNull(offenderEmployment.getPostType(), EmploymentPostType::getDescription),
            offenderEmployment.getEmployerName(),
            offenderEmployment.getSupervisorName(),
            offenderEmployment.getPosition(),
            offenderEmployment.getTerminationReason(),
            offenderEmployment.getWage(),
            getOrNull(offenderEmployment.getWagePeriod(), PayPeriodType::getDescription),
            getOrNull(offenderEmployment.getOccupation(), ReferenceCode::getDescription),
            offenderEmployment.getComment(),
            getOrNull(offenderEmployment.getScheduleType(), ScheduleType::getDescription),
            offenderEmployment.getHoursWeek(),
            offenderEmployment.getIsEmployerAware(),
            offenderEmployment.getIsEmployerContactable(),
            AddressTransformer.translate(offenderEmployment.getAddresses())
        );
    }

    private <T> String getOrNull(T data, Function<T, String> map) {
        return Optional.ofNullable(data).map(map).orElse(null);
    }
}
