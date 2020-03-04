package net.syscon.elite.api.resource.impl;


import net.syscon.elite.api.model.ReferenceCode;
import net.syscon.elite.api.model.ReferenceCodeInfo;
import net.syscon.elite.api.resource.ReferenceDomainResource;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.core.HasWriteScope;
import net.syscon.elite.core.ProxyUser;
import net.syscon.elite.service.CaseNoteService;
import net.syscon.elite.service.ReferenceDomainService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

import static net.syscon.util.ResourceUtils.nvl;

@RestController
@RequestMapping("${api.base.path}/reference-domains")
@Validated
public class ReferenceDomainsResourceImpl implements ReferenceDomainResource {
    private final ReferenceDomainService referenceDomainService;
    private final CaseNoteService caseNoteService;

    public ReferenceDomainsResourceImpl(final ReferenceDomainService referenceDomainService, final CaseNoteService caseNoteService) {
        this.referenceDomainService = referenceDomainService;
        this.caseNoteService = caseNoteService;
    }

    @Override
    public ResponseEntity<List<ReferenceCode>> getAlertTypes(final Long pageOffset, final Long pageLimit, final String sortFields, final Order sortOrder) {
        final var referenceCodes =
                referenceDomainService.getAlertTypes(
                        sortFields,
                        sortOrder,
                        nvl(pageOffset, 0L),
                        nvl(pageLimit, 10L));

        return ResponseEntity.ok()
                .headers(referenceCodes.getPaginationHeaders())
                .body(referenceCodes.getItems());
    }

    @Override
    public ResponseEntity<List<ReferenceCode>> getCaseNoteSources(final Long pageOffset, final Long pageLimit, final String sortFields, final Order sortOrder) {
        final var caseNoteSources =
                referenceDomainService.getCaseNoteSources(
                        sortFields,
                        sortOrder,
                        nvl(pageOffset, 0L),
                        nvl(pageLimit, 10L));

        return ResponseEntity.ok().headers(caseNoteSources.getPaginationHeaders()).body(caseNoteSources.getItems());
    }

    @Override
    public  List<ReferenceCode> getCaseNoteTypes() {
        return caseNoteService.getUsedCaseNoteTypesWithSubTypes();
    }

    @Override
    public ResponseEntity<List<ReferenceCode>> getReferenceCodesByDomain(final String domain, final boolean withSubCodes, final Long pageOffset, final Long pageLimit, final String sortFields, final Order sortOrder) {
        final var referenceCodes =
                referenceDomainService.getReferenceCodesByDomain(
                        domain,
                        withSubCodes,
                        sortFields,
                        sortOrder,
                        nvl(pageOffset, 0L),
                        nvl(pageLimit, 10L));

        return ResponseEntity.ok().headers(referenceCodes.getPaginationHeaders()).body(referenceCodes.getItems());
    }

    @Override
    public ReferenceCode getReferenceCodeByDomainAndCode(final String domain, final String code, final boolean withSubCodes) {
        return referenceDomainService
                .getReferenceCodeByDomainAndCode(domain, code, withSubCodes).orElseThrow( () -> {

                    // If no exception thrown in service layer, we know that reference code exists for specified domain and code.
                    // However, if sub-codes were requested but reference code does not have any sub-codes, response from service
                    // layer will be empty - this is a bad request.

                    final var message = String.format("Reference code for domain [%s] and code [%s] does not have sub-codes.", domain, code);
                    throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, message);
                });

    }

    @Override
    @HasWriteScope
    @PreAuthorize("hasAnyRole('MAINTAIN_REF_DATA','SYSTEM_USER')")
    @ProxyUser
    public ReferenceCode createReferenceCode(final String domain, final String code, final ReferenceCodeInfo referenceData) {
        return referenceDomainService.createReferenceCode(domain, code, referenceData);
    }

    @Override
    @HasWriteScope
    @PreAuthorize("hasAnyRole('MAINTAIN_REF_DATA','SYSTEM_USER')")
    @ProxyUser
    public ReferenceCode updateReferenceCode(final String domain, final String code, final ReferenceCodeInfo referenceData) {
        return referenceDomainService.updateReferenceCode(domain, code, referenceData);
    }

    @Override
    public List<ReferenceCode> getScheduleReasons(final String eventType) {
        return referenceDomainService.getScheduleReasons(eventType);
    }


}
