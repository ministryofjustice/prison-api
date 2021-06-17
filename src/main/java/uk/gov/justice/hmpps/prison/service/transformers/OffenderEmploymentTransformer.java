package uk.gov.justice.hmpps.prison.service.transformers;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import uk.gov.justice.hmpps.prison.api.model.OffenderEmploymentResponse;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEmployment;
import uk.gov.justice.hmpps.prison.service.AddressTransformer;

@Component
public class OffenderEmploymentTransformer implements Converter<OffenderEmployment, OffenderEmploymentResponse> {
    @Override
    public OffenderEmploymentResponse convert(@NotNull final OffenderEmployment offenderEmployment) {
        return new OffenderEmploymentResponse(
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
            AddressTransformer.translate(offenderEmployment.getAddresses())
        );
    }
}
