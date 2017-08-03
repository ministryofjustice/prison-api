package net.syscon.elite.web.api.resource.impl;

import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.ReferenceDomainService;
import net.syscon.elite.web.api.model.ReferenceCode;
import net.syscon.elite.web.api.model.ReferenceCodes;
import net.syscon.elite.web.api.resource.ReferenceDomainsResource;
import net.syscon.util.MetaDataFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.Path;
import java.util.List;

@RestResource
@Path("/referenceDomains")
public class ReferenceDomainsResourceImpl implements ReferenceDomainsResource {

	@Autowired
	private ReferenceDomainService referenceDomainService;

	@Override
	public GetReferenceDomainsCaseNotesSubTypesByCaseNoteTypeResponse getReferenceDomainsCaseNotesSubTypesByCaseNoteType(final String caseNoteType, final int offset, final int limit) throws Exception {
		List<ReferenceCode> referenceCodeList = this.referenceDomainService.getCaseNoteSubTypesByParent(caseNoteType, offset, limit);
		ReferenceCodes referenceCodes = new ReferenceCodes(referenceCodeList, MetaDataFactory.createMetaData(limit, offset, referenceCodeList));
		return GetReferenceDomainsCaseNotesSubTypesByCaseNoteTypeResponse.withJsonOK(referenceCodes);
	}

	@Override
	public GetReferenceDomainsCaseNoteSourcesResponse getReferenceDomainsCaseNoteSources(String query, String orderBy, Order order,int offset, int limit) throws Exception {
		List<ReferenceCode> referenceCodeList = this.referenceDomainService.getCaseNoteSources(query, orderBy, order, offset, limit);
		ReferenceCodes referenceCodes = new ReferenceCodes(referenceCodeList, MetaDataFactory.createMetaData(limit, offset, referenceCodeList));
		return GetReferenceDomainsCaseNoteSourcesResponse.withJsonOK(referenceCodes);
	}

	@Override
	public GetReferenceDomainsCaseNoteSourcesBySourceCodeResponse getReferenceDomainsCaseNoteSourcesBySourceCode(String sourceCode) throws Exception {
		ReferenceCode referenceCode = this.referenceDomainService.getCaseNoteSource(sourceCode);
		return GetReferenceDomainsCaseNoteSourcesBySourceCodeResponse.withJsonOK(referenceCode);
	}

	@Override
	public GetReferenceDomainsAlertTypesResponse getReferenceDomainsAlertTypes(String query, String orderBy, Order order, int offset, int limit) throws Exception {
		List<ReferenceCode> referenceCodeList = this.referenceDomainService.getAlertTypes(query, orderBy, order, offset, limit);
		ReferenceCodes referenceCodes = new ReferenceCodes(referenceCodeList, MetaDataFactory.createMetaData(limit, offset, referenceCodeList));
		return GetReferenceDomainsAlertTypesResponse.withJsonOK(referenceCodes);
	}

	@Override
	public GetReferenceDomainsAlertTypesByAlertTypeResponse getReferenceDomainsAlertTypesByAlertType(String alertType) throws Exception {
		ReferenceCode referenceCode = this.referenceDomainService.getAlertTypeByCode(alertType);
		return GetReferenceDomainsAlertTypesByAlertTypeResponse.withJsonOK(referenceCode);
	}

	@Override
	public GetReferenceDomainsAlertTypesByAlertTypeCodesResponse getReferenceDomainsAlertTypesByAlertTypeCodes(String alertType, String query, String orderBy, Order order, int offset, int limit) throws Exception {
		List<ReferenceCode> referenceCodeList = this.referenceDomainService.getAlertTypesByParent(alertType, query, orderBy, order, offset, limit);
		ReferenceCodes referenceCodes = new ReferenceCodes(referenceCodeList, MetaDataFactory.createMetaData(limit, offset, referenceCodeList));
		return GetReferenceDomainsAlertTypesByAlertTypeCodesResponse.withJsonOK(referenceCodes);
	}

	@Override
	public GetReferenceDomainsAlertTypesByAlertTypeCodesByAlertCodeResponse getReferenceDomainsAlertTypesByAlertTypeCodesByAlertCode(
			String alertType, String alertCode) throws Exception {
		ReferenceCode referenceCode = this.referenceDomainService.getAlertTypeByParentAndCode(alertType, alertCode);
		return GetReferenceDomainsAlertTypesByAlertTypeCodesByAlertCodeResponse.withJsonOK(referenceCode);
	}

    @Override
	public GetReferenceDomainsCaseNoteTypesResponse getReferenceDomainsCaseNoteTypes(String query, String orderBy, Order order, int offset, int limit) throws Exception {
		List<ReferenceCode> referenceCodeList = this.referenceDomainService.getCaseNoteTypes(query, orderBy, order, offset, limit);
		ReferenceCodes referenceCodes = new ReferenceCodes(referenceCodeList, MetaDataFactory.createMetaData(limit, offset, referenceCodeList));
		return GetReferenceDomainsCaseNoteTypesResponse.withJsonOK(referenceCodes);
	}

	@Override
	public GetReferenceDomainsCaseNoteTypesByTypeCodeResponse getReferenceDomainsCaseNoteTypesByTypeCode(String typeCode) throws Exception {
        ReferenceCode referenceCode = this.referenceDomainService.getCaseNoteType(typeCode);
        return GetReferenceDomainsCaseNoteTypesByTypeCodeResponse.withJsonOK(referenceCode);
	}

	@Override
	public GetReferenceDomainsCaseNoteTypesByTypeCodeSubTypesResponse getReferenceDomainsCaseNoteTypesByTypeCodeSubTypes(String typeCode, String query, String orderBy, Order order, int offset, int limit) throws Exception {
		List<ReferenceCode> referenceCodeList = this.referenceDomainService.getCaseNoteSubTypes(typeCode, query, orderBy, order, offset, limit);
		ReferenceCodes referenceCodes = new ReferenceCodes(referenceCodeList, MetaDataFactory.createMetaData(limit, offset, referenceCodeList));
		return GetReferenceDomainsCaseNoteTypesByTypeCodeSubTypesResponse.withJsonOK(referenceCodes);
	}

	@Override
	public GetReferenceDomainsCaseNoteTypesByTypeCodeSubTypesBySubTypeCodeResponse getReferenceDomainsCaseNoteTypesByTypeCodeSubTypesBySubTypeCode(String typeCode, String subTypeCode) throws Exception {
        ReferenceCode referenceCode = referenceDomainService.getCaseNoteSubType(typeCode, subTypeCode);
        return GetReferenceDomainsCaseNoteTypesByTypeCodeSubTypesBySubTypeCodeResponse.withJsonOK(referenceCode);
	}
}
