package net.syscon.elite.web.api.resource.impl;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import net.syscon.elite.service.ReferenceDomainService;
import net.syscon.elite.web.api.model.ReferenceCode;
import net.syscon.elite.web.api.model.ReferenceCodes;
import net.syscon.elite.web.api.resource.ReferenceDomainsResource;
import net.syscon.util.MetaDataFactory;

@Component
public class ReferenceDomainsResourceImpl implements ReferenceDomainsResource {


	ReferenceDomainService refrenceCodeService;

	@Inject
	public void setRefrenceCodeService(final ReferenceDomainService refrenceCodeService) {
		this.refrenceCodeService = refrenceCodeService;
	}

	@Override
	public GetReferenceDomainsCaseNotesTypesByCaseLoadResponse getReferenceDomainsCaseNotesTypesByCaseLoad(final String caseLoad, final int offset, final int limit) throws Exception {
		List<ReferenceCode> refrenceCodeList = this.refrenceCodeService.getCnotetypesByCaseLoad(caseLoad, offset, limit);
		ReferenceCodes referenceCodes = new ReferenceCodes(refrenceCodeList, MetaDataFactory.createMetaData(limit, offset, refrenceCodeList));
		return GetReferenceDomainsCaseNotesTypesByCaseLoadResponse.withJsonOK(referenceCodes);
	}

	@Override
	public GetReferenceDomainsCaseNotesSubTypesByCaseNoteTypeResponse getReferenceDomainsCaseNotesSubTypesByCaseNoteType(final String caseNoteType, final int offset, final int limit) throws Exception {
		List<ReferenceCode> refrenceCodeList = this.refrenceCodeService.getCnoteSubtypesByCaseNoteType(caseNoteType, offset, limit);
		ReferenceCodes referenceCodes = new ReferenceCodes(refrenceCodeList, MetaDataFactory.createMetaData(limit, offset, refrenceCodeList));
		return GetReferenceDomainsCaseNotesSubTypesByCaseNoteTypeResponse.withJsonOK(referenceCodes);
	}

	@Override
	public GetReferenceDomainsAlertTypesResponse getReferenceDomainsAlertTypes(String query, String orderBy, Order order, int offset, int limit) throws Exception {
		List<ReferenceCode> refrenceCodeList = this.refrenceCodeService.getAlertTypes(query, orderBy, order, offset, limit);
		ReferenceCodes referenceCodes = new ReferenceCodes(refrenceCodeList, MetaDataFactory.createMetaData(limit, offset, refrenceCodeList));
		return GetReferenceDomainsAlertTypesResponse.withJsonOK(referenceCodes);
	}

	@Override
	public GetReferenceDomainsAlertTypesByAlertTypeResponse getReferenceDomainsAlertTypesByAlertType(String alertType)
			throws Exception {
		ReferenceCode referenceCode = this.refrenceCodeService.getAlertTypesByAlertType(alertType);
		return GetReferenceDomainsAlertTypesByAlertTypeResponse.withJsonOK(referenceCode);
	}

	@Override
	public GetReferenceDomainsAlertTypesByAlertTypeCodesResponse getReferenceDomainsAlertTypesByAlertTypeCodes(String alertType, String query, String orderBy, Order order, int offset, int limit) throws Exception {
		List<ReferenceCode> refrenceCodeList = this.refrenceCodeService.getAlertTypesByAlertTypeCode(alertType, query, orderBy, order, offset, limit);
		ReferenceCodes referenceCodes = new ReferenceCodes(refrenceCodeList, MetaDataFactory.createMetaData(limit, offset, refrenceCodeList));
		return GetReferenceDomainsAlertTypesByAlertTypeCodesResponse.withJsonOK(referenceCodes);
	}

	@Override
	public GetReferenceDomainsAlertTypesByAlertTypeCodesByAlertCodeResponse getReferenceDomainsAlertTypesByAlertTypeCodesByAlertCode(
			String alertType, String alertCode) throws Exception {
		ReferenceCode referenceCode = this.refrenceCodeService.getAlertTypeCodesByAlertCode(alertType, alertCode);
		return GetReferenceDomainsAlertTypesByAlertTypeCodesByAlertCodeResponse.withJsonOK(referenceCode);
	}
}
