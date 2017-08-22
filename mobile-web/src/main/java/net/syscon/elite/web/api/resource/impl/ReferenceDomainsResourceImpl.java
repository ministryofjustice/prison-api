package net.syscon.elite.web.api.resource.impl;

import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.ReferenceDomainService;
import net.syscon.elite.web.api.model.ReferenceCode;
import net.syscon.elite.web.api.model.ReferenceCodes;
import net.syscon.elite.web.api.resource.ReferenceDomainsResource;
import net.syscon.util.MetaDataFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;

import javax.ws.rs.Path;
import java.util.List;

@RestResource
@Path("/referenceDomains")
public class ReferenceDomainsResourceImpl implements ReferenceDomainsResource {

	@Autowired
	private ReferenceDomainService referenceDomainService;

	@Override
	@Cacheable("caseNoteTypesByType")
	public GetReferenceDomainsCaseNotesSubTypesByCaseNoteTypeResponse getReferenceDomainsCaseNotesSubTypesByCaseNoteType(final String caseNoteType, final int offset, final int limit) throws Exception {
		List<ReferenceCode> referenceCodeList = this.referenceDomainService.getCaseNoteSubTypesByParent(caseNoteType, offset, limit);
		ReferenceCodes referenceCodes = new ReferenceCodes(referenceCodeList, MetaDataFactory.createMetaData(limit, offset, referenceCodeList));
		return GetReferenceDomainsCaseNotesSubTypesByCaseNoteTypeResponse.withJsonOK(referenceCodes);
	}

	@Override
	@Cacheable("caseNoteSources")
	public GetReferenceDomainsCaseNoteSourcesResponse getReferenceDomainsCaseNoteSources(String query, String orderBy, Order order,int offset, int limit) throws Exception {
		List<ReferenceCode> referenceCodeList = this.referenceDomainService.getCaseNoteSources(query, orderBy, order, offset, limit);
		ReferenceCodes referenceCodes = new ReferenceCodes(referenceCodeList, MetaDataFactory.createMetaData(limit, offset, referenceCodeList));
		return GetReferenceDomainsCaseNoteSourcesResponse.withJsonOK(referenceCodes);
	}

	@Override
	@Cacheable("caseNoteSourcesByCode")
	public GetReferenceDomainsCaseNoteSourcesBySourceCodeResponse getReferenceDomainsCaseNoteSourcesBySourceCode(String sourceCode) throws Exception {
		ReferenceCode referenceCode = this.referenceDomainService.getCaseNoteSource(sourceCode);
		return GetReferenceDomainsCaseNoteSourcesBySourceCodeResponse.withJsonOK(referenceCode);
	}

	@Override
	@Cacheable("alertTypes")
	public GetReferenceDomainsAlertTypesResponse getReferenceDomainsAlertTypes(String query, String orderBy, Order order, int offset, int limit) throws Exception {
		List<ReferenceCode> referenceCodeList = this.referenceDomainService.getAlertTypes(query, orderBy, order, offset, limit);
		ReferenceCodes referenceCodes = new ReferenceCodes(referenceCodeList, MetaDataFactory.createMetaData(limit, offset, referenceCodeList));
		return GetReferenceDomainsAlertTypesResponse.withJsonOK(referenceCodes);
	}

	@Override
	@Cacheable("alertTypesByType")
	public GetReferenceDomainsAlertTypesByAlertTypeResponse getReferenceDomainsAlertTypesByAlertType(String alertType) throws Exception {
		ReferenceCode referenceCode = this.referenceDomainService.getAlertTypeByCode(alertType);
		return GetReferenceDomainsAlertTypesByAlertTypeResponse.withJsonOK(referenceCode);
	}

	@Override
	@Cacheable("alertTypesByTypeFiltered")
	public GetReferenceDomainsAlertTypesByAlertTypeCodesResponse getReferenceDomainsAlertTypesByAlertTypeCodes(String alertType, String query, String orderBy, Order order, int offset, int limit) throws Exception {
		List<ReferenceCode> referenceCodeList = this.referenceDomainService.getAlertTypesByParent(alertType, query, orderBy, order, offset, limit);
		ReferenceCodes referenceCodes = new ReferenceCodes(referenceCodeList, MetaDataFactory.createMetaData(limit, offset, referenceCodeList));
		return GetReferenceDomainsAlertTypesByAlertTypeCodesResponse.withJsonOK(referenceCodes);
	}

	@Override
	@Cacheable("alertTypesByTypeAndCode")
	public GetReferenceDomainsAlertTypesByAlertTypeCodesByAlertCodeResponse getReferenceDomainsAlertTypesByAlertTypeCodesByAlertCode(
			String alertType, String alertCode) throws Exception {
		ReferenceCode referenceCode = this.referenceDomainService.getAlertTypeByParentAndCode(alertType, alertCode);
		return GetReferenceDomainsAlertTypesByAlertTypeCodesByAlertCodeResponse.withJsonOK(referenceCode);
	}

    @Override
	@Cacheable("caseNoteTypes")
	public GetReferenceDomainsCaseNoteTypesResponse getReferenceDomainsCaseNoteTypes(String query, String orderBy, Order order, int offset, int limit) throws Exception {
		List<ReferenceCode> referenceCodeList = this.referenceDomainService.getCaseNoteTypes(query, orderBy, order, offset, limit);
		ReferenceCodes referenceCodes = new ReferenceCodes(referenceCodeList, MetaDataFactory.createMetaData(limit, offset, referenceCodeList));
		return GetReferenceDomainsCaseNoteTypesResponse.withJsonOK(referenceCodes);
	}

	@Override
	@Cacheable("caseNoteTypesByCode")
	public GetReferenceDomainsCaseNoteTypesByTypeCodeResponse getReferenceDomainsCaseNoteTypesByTypeCode(String typeCode) throws Exception {
        ReferenceCode referenceCode = this.referenceDomainService.getCaseNoteType(typeCode);
        return GetReferenceDomainsCaseNoteTypesByTypeCodeResponse.withJsonOK(referenceCode);
	}

	@Override
	@Cacheable("caseNoteTypesByCodeFiltered")
	public GetReferenceDomainsCaseNoteTypesByTypeCodeSubTypesResponse getReferenceDomainsCaseNoteTypesByTypeCodeSubTypes(String typeCode, String query, String orderBy, Order order, int offset, int limit) throws Exception {
		List<ReferenceCode> referenceCodeList = this.referenceDomainService.getCaseNoteSubTypes(typeCode, query, orderBy, order, offset, limit);
		ReferenceCodes referenceCodes = new ReferenceCodes(referenceCodeList, MetaDataFactory.createMetaData(limit, offset, referenceCodeList));
		return GetReferenceDomainsCaseNoteTypesByTypeCodeSubTypesResponse.withJsonOK(referenceCodes);
	}

	@Override
	@Cacheable("caseNoteTypesByTypeSubType")
	public GetReferenceDomainsCaseNoteTypesByTypeCodeSubTypesBySubTypeCodeResponse getReferenceDomainsCaseNoteTypesByTypeCodeSubTypesBySubTypeCode(String typeCode, String subTypeCode) throws Exception {
        ReferenceCode referenceCode = referenceDomainService.getCaseNoteSubType(typeCode, subTypeCode);
        return GetReferenceDomainsCaseNoteTypesByTypeCodeSubTypesBySubTypeCodeResponse.withJsonOK(referenceCode);
	}
}
