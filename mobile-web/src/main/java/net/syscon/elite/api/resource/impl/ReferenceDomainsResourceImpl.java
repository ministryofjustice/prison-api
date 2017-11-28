package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.ReferenceCode;
import net.syscon.elite.api.resource.ReferenceDomainResource;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.CaseNoteService;
import net.syscon.elite.service.ReferenceDomainService;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Path;
import java.util.List;
import java.util.Optional;

import static net.syscon.util.ResourceUtils.nvl;

@RestResource
@Path("/reference-domains")
public class ReferenceDomainsResourceImpl implements ReferenceDomainResource {
	private final ReferenceDomainService referenceDomainService;
	private final CaseNoteService caseNoteService;

	public ReferenceDomainsResourceImpl(ReferenceDomainService referenceDomainService, CaseNoteService caseNoteService) {
		this.referenceDomainService = referenceDomainService;
        this.caseNoteService = caseNoteService;
    }

	@Override
	public GetAlertTypesResponse getAlertTypes(Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
		Page<ReferenceCode> referenceCodes =
				referenceDomainService.getAlertTypes(
						sortFields,
						sortOrder,
						nvl(pageOffset, 0L),
						nvl(pageLimit, 10L));

		return GetAlertTypesResponse.respond200WithApplicationJson(referenceCodes);
	}

	@Override
	public GetCaseNoteSourcesResponse getCaseNoteSources(Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
		Page<ReferenceCode> caseNoteSources =
				referenceDomainService.getCaseNoteSources(
						sortFields,
						sortOrder,
						nvl(pageOffset, 0L),
						nvl(pageLimit, 10L));

		return GetCaseNoteSourcesResponse.respond200WithApplicationJson(caseNoteSources);
	}

	@Override
	public GetCaseNoteTypesResponse getCaseNoteTypes() {
		List<ReferenceCode> caseNoteTypes = caseNoteService.getUsedCaseNoteTypesWithSubTypes();

		return GetCaseNoteTypesResponse.respond200WithApplicationJson(caseNoteTypes);
	}

	@Override
	public GetReferenceCodesByDomainResponse getReferenceCodesByDomain(String domain, boolean withSubCodes, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
		Page<ReferenceCode> referenceCodes =
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
	public GetReferenceCodeByDomainAndCodeResponse getReferenceCodeByDomainAndCode(String domain, String code, boolean withSubCodes) {
		Optional<ReferenceCode> referenceCode = referenceDomainService
				.getReferenceCodeByDomainAndCode(domain, code, withSubCodes);

		// If no exception thrown in service layer, we know that reference code exists for specified domain and code.
        // However, if sub-codes were requested but reference code does not have any sub-codes, response from service
        // layer will be empty - this is a bad request.
        if (!referenceCode.isPresent()) {
            String message = String.format("Reference code for domain [%s] and code [%s] does not have sub-codes.", domain, code);

            throw new BadRequestException(message);
        }

		return GetReferenceCodeByDomainAndCodeResponse.respond200WithApplicationJson(referenceCode.get());
	}
}
