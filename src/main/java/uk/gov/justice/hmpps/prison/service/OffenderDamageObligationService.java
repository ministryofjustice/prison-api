package uk.gov.justice.hmpps.prison.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.OffenderDamageObligationModel;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderDamageObligation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderDamageObligation.Status;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderDamageObligationRepository;
import uk.gov.justice.hmpps.prison.security.VerifyOffenderAccess;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OffenderDamageObligationService {
    private final OffenderDamageObligationRepository repository;

    @Value("${api.currency:GBP}")
    private String currency;

    public OffenderDamageObligationService(final OffenderDamageObligationRepository repository) {
        this.repository = repository;
    }

    @VerifyOffenderAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    @Transactional
    public List<OffenderDamageObligationModel> getDamageObligations(final String offenderNo, final Status status) {

        final var statusCode = Optional.ofNullable(status).map(Status::code).orElse(Status.ALL.code());
        final var damages = StringUtils.isNotEmpty(statusCode) ?
                repository.findOffenderDamageObligationByOffender_NomsIdAndStatus(offenderNo, status.code()) :
                repository.findOffenderDamageObligationByOffender_NomsId(offenderNo);

        return damages
                .stream()
                .map(this::damageObligationTransformer)
                .toList();
    }

    public OffenderDamageObligationModel damageObligationTransformer(final OffenderDamageObligation damageObligation) {
        return OffenderDamageObligationModel.builder()
                .id(damageObligation.getId())
                .offenderNo(damageObligation.getOffender().getNomsId())
                .currency(currency)
                .amountPaid(damageObligation.getAmountPaid())
                .amountToPay(damageObligation.getAmountToPay())
                .comment(damageObligation.getComment())
                .prisonId(damageObligation.getPrison() != null ? damageObligation.getPrison().getId() : null)
                .referenceNumber(damageObligation.getReferenceNumber())
                .startDateTime(damageObligation.getStartDateTime())
                .endDateTime(damageObligation.getEndDateTime())
                .status(damageObligation.getStatus())
                .build();
    }
}
