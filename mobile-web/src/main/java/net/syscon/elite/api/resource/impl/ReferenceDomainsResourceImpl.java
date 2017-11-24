package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.ReferenceCode;
import net.syscon.elite.api.resource.ReferenceDomainResource;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.ReferenceDomainService;

import javax.ws.rs.Path;

import static net.syscon.util.ResourceUtils.nvl;

@RestResource
@Path("/reference-domains")
public class ReferenceDomainsResourceImpl implements ReferenceDomainResource {
	private final ReferenceDomainService referenceDomainService;

	public ReferenceDomainsResourceImpl(ReferenceDomainService referenceDomainService) {
		this.referenceDomainService = referenceDomainService;
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
	public GetCaseNoteTypesResponse getCaseNoteTypes(Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
		Page<ReferenceCode> caseNoteTypes =
				referenceDomainService.getCaseNoteTypes(
						sortFields,
						sortOrder,
						nvl(pageOffset, 0L),
						nvl(pageLimit, 10L));

		return GetCaseNoteTypesResponse.respond200WithApplicationJson(caseNoteTypes);
	}
}
