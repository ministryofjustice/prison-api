package uk.gov.justice.hmpps.prison.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.OffenderEmploymentResponse;
import uk.gov.justice.hmpps.prison.security.VerifyOffenderAccess;

import javax.validation.constraints.NotNull;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OffenderEmploymentService {

    @VerifyOffenderAccess
    public Page<OffenderEmploymentResponse> getOffenderEmployments(@NotNull final String nomisId, final PageRequest pageRequest) {
        return null;
    }
}
