package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.ReferenceCode;
import net.syscon.elite.api.model.ReferenceCodeInfo;
import net.syscon.elite.api.resource.ReferenceDomainResource;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.CaseNoteService;
import net.syscon.elite.service.ReferenceDomainService;
import org.springframework.validation.annotation.Validated;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Path;

import static net.syscon.util.ResourceUtils.nvl;

@RestResource
@Path("/reference-domains")
@Validated
public class ReferenceDomainsResourceImpl implements ReferenceDomainResource {
	private final ReferenceDomainService referenceDomainService;
	private final CaseNoteService caseNoteService;

    public ReferenceDomainsResourceImpl(final ReferenceDomainService referenceDomainService, final CaseNoteService caseNoteService) {
		this.referenceDomainService = referenceDomainService;
        this.caseNoteService = caseNoteService;
    }

	@Override
    public GetAlertTypesResponse getAlertTypes(final Long pageOffset, final Long pageLimit, final String sortFields, final Order sortOrder) {
        final var referenceCodes =
				referenceDomainService.getAlertTypes(
						sortFields,
						sortOrder,
						nvl(pageOffset, 0L),
						nvl(pageLimit, 10L));

		return GetAlertTypesResponse.respond200WithApplicationJson(referenceCodes);
	}

	@Override
    public GetCaseNoteSourcesResponse getCaseNoteSources(final Long pageOffset, final Long pageLimit, final String sortFields, final Order sortOrder) {
        final var caseNoteSources =
				referenceDomainService.getCaseNoteSources(
						sortFields,
						sortOrder,
						nvl(pageOffset, 0L),
						nvl(pageLimit, 10L));

		return GetCaseNoteSourcesResponse.respond200WithApplicationJson(caseNoteSources);
	}

	@Override
	public GetCaseNoteTypesResponse getCaseNoteTypes() {
        final var caseNoteTypes = caseNoteService.getUsedCaseNoteTypesWithSubTypes();

		return GetCaseNoteTypesResponse.respond200WithApplicationJson(caseNoteTypes);
	}

	@Override
    public GetReferenceCodesByDomainResponse getReferenceCodesByDomain(final String domain, final boolean withSubCodes, final Long pageOffset, final Long pageLimit, final String sortFields, final Order sortOrder) {
        final var referenceCodes =
				referenceDomainService.getReferenceCodesByDomain(
						domain,
						withSubCodes,
						sortFields,
						sortOrder,
						nvl(pageOffset, 0L),
						nvl(pageLimit, 10L));

		return GetReferenceCodesByDomainResponse.respond200WithApplicationJson(referenceCodes);
	}

	@Override
    public GetReferenceCodeByDomainAndCodeResponse getReferenceCodeByDomainAndCode(final String domain, final String code, final boolean withSubCodes) {
        final var referenceCode = referenceDomainService
				.getReferenceCodeByDomainAndCode(domain, code, withSubCodes);

		// If no exception thrown in service layer, we know that reference code exists for specified domain and code.
        // However, if sub-codes were requested but reference code does not have any sub-codes, response from service
        // layer will be empty - this is a bad request.
        if (referenceCode.isEmpty()) {
            final var message = String.format("Reference code for domain [%s] and code [%s] does not have sub-codes.", domain, code);

            throw new BadRequestException(message);
        }

		return GetReferenceCodeByDomainAndCodeResponse.respond200WithApplicationJson(referenceCode.get());
	}

	@Override
	public ReferenceCode createReferenceCode(final String domain,final String code, final ReferenceCodeInfo referenceData) {
		return referenceDomainService.createReferenceCode(domain, code, referenceData);
	}

	@Override
	public ReferenceCode updateReferenceCode(final String domain,final String code, final ReferenceCodeInfo referenceData) {
		return referenceDomainService.updateReferenceCode(domain, code, referenceData);
	}

	@Override
    public GetScheduleReasonsResponse getScheduleReasons(final String eventType) {
        final var result = referenceDomainService.getScheduleReasons(eventType);

        return GetScheduleReasonsResponse.respond200WithApplicationJson(result);
    }


}
