package uk.gov.justice.hmpps.prison.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.justice.hmpps.prison.api.model.OffenderDamageObligationModel;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderDamageObligation;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderDamageObligationRepository;
import uk.gov.justice.hmpps.prison.security.VerifyOffenderAccess;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OffenderDamageObligationService {
    private final OffenderDamageObligationRepository repository;
    private final String currency;

    public OffenderDamageObligationService(@Value("${api.currency:GBP}") final String currency, final OffenderDamageObligationRepository repository) {
        this.repository = repository;
        this.currency = currency;
    }

    @VerifyOffenderAccess
    public List<OffenderDamageObligationModel> getDamageObligations(final String offenderNo, final String status) {

        try {
            final var damages = StringUtils.isNotEmpty(status) ?
                    repository.findOffenderDamageObligationByOffender_NomsIdAndStatus(offenderNo, status).stream() :
                    repository.findOffenderDamageObligationByOffender_NomsId(offenderNo).stream();

            return damages
                    .map(this::damageObligationTransformer)
                    .collect(Collectors.toList());

        } catch(EntityNotFoundException e) {
            throw EntityNotFoundException.withMessage(String.format("Offender not found: %s", offenderNo));
        }
    }

    public OffenderDamageObligationModel damageObligationTransformer(final OffenderDamageObligation damageObligation) {
        final var offender = damageObligation.getOffender();

        return OffenderDamageObligationModel.builder()
                .id(damageObligation.getId())
                .offenderNo(offender != null ? offender.getNomsId() : "")
                .currency(currency)
                .amountPaid(damageObligation.getAmountPaid())
                .amountToPay(damageObligation.getAmountToPay())
                .comment(damageObligation.getComment())
                .prisonId(damageObligation.getPrison() != null ? damageObligation.getPrison().getId(): null)
                .referenceNumber(damageObligation.getReferenceNumber())
                .startDateTime(damageObligation.getStartDateTime())
                .endDateTime(damageObligation.getEndDateTime())
                .status(damageObligation.getStatus())
                .build();
    }
}
