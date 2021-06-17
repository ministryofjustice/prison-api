package uk.gov.justice.hmpps.prison.service.transformers;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import uk.gov.justice.hmpps.prison.api.model.OffenderEmploymentResponse;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEmployment;

public class OffenderEmploymentTransformer implements Converter<OffenderEmployment, OffenderEmploymentResponse> {
    @Override
    public OffenderEmploymentResponse convert(@NotNull final OffenderEmployment source) {
        return OffenderEmploymentResponse.builder().build();
    }
}
