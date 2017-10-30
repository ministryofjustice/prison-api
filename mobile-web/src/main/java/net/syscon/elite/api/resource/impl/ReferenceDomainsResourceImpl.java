package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.ReferenceCode;
import net.syscon.elite.api.resource.ReferenceDomainResource;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.ReferenceDomainService;
import org.springframework.cache.annotation.Cacheable;

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
	@Cacheable("alertTypes")
	public GetAlertTypesResponse getAlertTypes(boolean includeSubTypes, String query, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
		Page<ReferenceCode> referenceCodes =
				referenceDomainService.getAlertTypes(
						query,
						sortFields,
						sortOrder,
						nvl(pageOffset, 0L),
						nvl(pageLimit, 10L),
						includeSubTypes);

		return GetAlertTypesResponse.respond200WithApplicationJson(referenceCodes);
	}

    @Override
	@Cacheable("alertTypesByType")
	public GetAlertTypeResponse getAlertType(String alertType) {
		ReferenceCode alertTypeByCode = referenceDomainService.getAlertTypeByCode(alertType);

		return GetAlertTypeResponse.respond200WithApplicationJson(alertTypeByCode);
	}

	@Override
	@Cacheable("alertTypesByTypeFiltered")
	public GetAlertTypesByTypeResponse getAlertTypesByType(String alertType, String query, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
		Page<ReferenceCode> alertTypesByParent =
				referenceDomainService.getAlertTypesByParent(
						alertType,
						query,
						sortFields,
						sortOrder,
						nvl(pageOffset, 0L),
						nvl(pageLimit, 10L));

		return GetAlertTypesByTypeResponse.respond200WithApplicationJson(alertTypesByParent);
	}

	@Override
	@Cacheable("alertTypesByTypeAndCode")
	public GetAlertTypeCodeResponse getAlertTypeCode(String alertType, String alertCode) {
		ReferenceCode alertTypeByParentAndCode = referenceDomainService.getAlertTypeByParentAndCode(alertType, alertCode);

		return GetAlertTypeCodeResponse.respond200WithApplicationJson(alertTypeByParentAndCode);
	}

	@Override
	@Cacheable("caseNoteSources")
	public GetCaseNoteSourcesResponse getCaseNoteSources(String query, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
		Page<ReferenceCode> caseNoteSources =
				referenceDomainService.getCaseNoteSources(
						query,
						sortFields,
						sortOrder,
						nvl(pageOffset, 0L),
						nvl(pageLimit, 10L));

		return GetCaseNoteSourcesResponse.respond200WithApplicationJson(caseNoteSources);
	}

	@Override
	@Cacheable("caseNoteSourcesByCode")
	public GetCaseNoteSourceResponse getCaseNoteSource(String sourceCode) {
		ReferenceCode caseNoteSource = referenceDomainService.getCaseNoteSource(sourceCode);

		return GetCaseNoteSourceResponse.respond200WithApplicationJson(caseNoteSource);
	}

	@Override
	@Cacheable("caseNoteTypes")
	public GetCaseNoteTypesResponse getCaseNoteTypes(boolean includeSubTypes, String query, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
		Page<ReferenceCode> caseNoteTypes =
				referenceDomainService.getCaseNoteTypes(
						query,
						sortFields,
						sortOrder,
						nvl(pageOffset, 0L),
						nvl(pageLimit, 10L),
						includeSubTypes);

		return GetCaseNoteTypesResponse.respond200WithApplicationJson(caseNoteTypes);
	}

	@Override
	@Cacheable("caseNoteTypesByCode")
	public GetCaseNoteTypeResponse getCaseNoteType(String typeCode) {
		ReferenceCode caseNoteType = this.referenceDomainService.getCaseNoteType(typeCode);

		return GetCaseNoteTypeResponse.respond200WithApplicationJson(caseNoteType);
	}

	@Override
	@Cacheable("caseNoteTypesByType")
	public GetCaseNoteSubTypesResponse getCaseNoteSubTypes(String typeCode, Long pageOffset, Long pageLimit) {
		Page<ReferenceCode> caseNoteSubTypes =
				referenceDomainService.getCaseNoteSubTypesByParent(
						typeCode,
						nvl(pageOffset, 0L),
						nvl(pageLimit, 10L));

		return GetCaseNoteSubTypesResponse.respond200WithApplicationJson(caseNoteSubTypes);
	}

	@Override
	@Cacheable("caseNoteTypesByTypeSubType")
	public GetCaseNoteSubTypeResponse getCaseNoteSubType(String typeCode, String subTypeCode) {
		ReferenceCode caseNoteSubType = referenceDomainService.getCaseNoteSubType(typeCode, subTypeCode);

		return GetCaseNoteSubTypeResponse.respond200WithApplicationJson(caseNoteSubType);
	}
}
