package net.syscon.elite.v2.api.resource.impl;

import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.ReferenceDomainService;
import net.syscon.elite.v2.api.model.ReferenceCode;
import net.syscon.elite.v2.api.resource.ReferenceDomainResource;
import net.syscon.elite.v2.api.support.Order;
import net.syscon.util.MetaDataFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;

import javax.ws.rs.Path;
import java.util.List;

import static net.syscon.util.ResourceUtils.nvl;

@RestResource
@Path("/reference-domains")
public class ReferenceDomainsResourceImpl implements ReferenceDomainResource {

	@Autowired
	private ReferenceDomainService referenceDomainService;

	@Override
	@Cacheable("alertTypes")
	public GetAlertTypesResponse getAlertTypes(boolean includeSubTypes, String query, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
        final long offset = nvl(pageOffset, 0L);
        final long limit = nvl(pageLimit, 10L);
		List<ReferenceCode> referenceCodes = referenceDomainService.getAlertTypes(query, sortFields, sortOrder, offset, limit, includeSubTypes);

        if (includeSubTypes) {
            return GetAlertTypesResponse.respond200WithApplicationJson(referenceCodes, (long)referenceCodes.size(),  0L, (long)referenceCodes.size());
        } else {
            return GetAlertTypesResponse.respond200WithApplicationJson(referenceCodes, MetaDataFactory.getTotalRecords(referenceCodes),  offset, limit);
        }
	}

    @Override
	@Cacheable("alertTypesByType")
	public GetAlertTypeResponse getAlertType(String alertType) {
		final ReferenceCode alertTypeByCode = referenceDomainService.getAlertTypeByCode(alertType);
		return GetAlertTypeResponse.respond200WithApplicationJson(alertTypeByCode);
	}

	@Override
	@Cacheable("alertTypesByTypeFiltered")
	public GetAlertTypesByTypeResponse getAlertTypesByType(String alertType, String query, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
		final List<ReferenceCode> alertTypesByParent = referenceDomainService.getAlertTypesByParent(alertType, query, sortFields, sortOrder, nvl(pageOffset, 0L), nvl(pageLimit, 10L));
		return GetAlertTypesByTypeResponse.respond200WithApplicationJson(alertTypesByParent, MetaDataFactory.getTotalRecords(alertTypesByParent), nvl(pageOffset, 0L), nvl(pageLimit, 10L));
	}

	@Override
	@Cacheable("alertTypesByTypeAndCode")
	public GetAlertTypeCodeResponse getAlertTypeCode(String alertType, String alertCode) {
		final ReferenceCode alertTypeByParentAndCode = referenceDomainService.getAlertTypeByParentAndCode(alertType, alertCode);
		return GetAlertTypeCodeResponse.respond200WithApplicationJson(alertTypeByParentAndCode);
	}

	@Override
	@Cacheable("caseNoteSources")
	public GetCaseNoteSourcesResponse getCaseNoteSources(String query, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
		final List<ReferenceCode> caseNoteSources = referenceDomainService.getCaseNoteSources(query, sortFields, sortOrder, nvl(pageOffset, 0L), nvl(pageLimit, 10L));
		return GetCaseNoteSourcesResponse.respond200WithApplicationJson(caseNoteSources, MetaDataFactory.getTotalRecords(caseNoteSources), nvl(pageOffset, 0L), nvl(pageLimit, 10L));
	}

	@Override
	@Cacheable("caseNoteSourcesByCode")
	public GetCaseNoteSourceResponse getCaseNoteSource(String sourceCode) {
		final ReferenceCode caseNoteSource = referenceDomainService.getCaseNoteSource(sourceCode);
		return GetCaseNoteSourceResponse.respond200WithApplicationJson(caseNoteSource);
	}

	@Override
	@Cacheable("caseNoteTypes")
	public GetCaseNoteTypesResponse getCaseNoteTypes(boolean includeSubTypes, String query, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
		final long offset = nvl(pageOffset, 0L);
		final long limit = nvl(pageLimit, 10L);
		final List<ReferenceCode> caseNoteTypes = referenceDomainService.getCaseNoteTypes(query, sortFields, sortOrder, offset, limit, includeSubTypes);

		if (includeSubTypes) {
			return GetCaseNoteTypesResponse.respond200WithApplicationJson(caseNoteTypes, (long) caseNoteTypes.size(), 0L, (long)caseNoteTypes.size());
		} else {
			return GetCaseNoteTypesResponse.respond200WithApplicationJson(caseNoteTypes, MetaDataFactory.getTotalRecords(caseNoteTypes), offset, limit);
		}
	}

	@Override
	@Cacheable("caseNoteTypesByCode")
	public GetCaseNoteTypeResponse getCaseNoteType(String typeCode) {
		final ReferenceCode caseNoteType = this.referenceDomainService.getCaseNoteType(typeCode);
		return GetCaseNoteTypeResponse.respond200WithApplicationJson(caseNoteType);
	}

	@Override
	@Cacheable("caseNoteTypesByType")
	public GetCaseNoteSubTypesResponse getCaseNoteSubTypes(String typeCode, Long pageOffset, Long pageLimit) {
		final List<ReferenceCode> caseNoteSubTypes = referenceDomainService.getCaseNoteSubTypesByParent(typeCode, nvl(pageOffset, 0L), nvl(pageLimit, 10L));
		return GetCaseNoteSubTypesResponse.respond200WithApplicationJson(caseNoteSubTypes, MetaDataFactory.getTotalRecords(caseNoteSubTypes), nvl(pageOffset, 0L), nvl(pageLimit, 10L));
	}

	@Override
	@Cacheable("caseNoteTypesByTypeSubType")
	public GetCaseNoteSubTypeResponse getCaseNoteSubType(String typeCode, String subTypeCode) {
		final ReferenceCode caseNoteSubType = referenceDomainService.getCaseNoteSubType(typeCode, subTypeCode);
		return GetCaseNoteSubTypeResponse.respond200WithApplicationJson(caseNoteSubType);
	}
}
