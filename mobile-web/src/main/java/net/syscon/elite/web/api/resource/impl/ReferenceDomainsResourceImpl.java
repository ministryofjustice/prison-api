package net.syscon.elite.web.api.resource.impl;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import net.syscon.elite.service.ReferenceDomainService;
import net.syscon.elite.web.api.model.ReferenceCode;
import net.syscon.elite.web.api.resource.ReferenceDomainsResource;

@Component
public class ReferenceDomainsResourceImpl implements ReferenceDomainsResource {


	ReferenceDomainService refrenceCodeService;

	@Inject
	public void setRefrenceCodeService(final ReferenceDomainService refrenceCodeService) {
		this.refrenceCodeService = refrenceCodeService;
	}

	@Override
	public GetReferenceDomainsCaseNotesTypesByCaseLoadResponse getReferenceDomainsCaseNotesTypesByCaseLoad(final String caseLoad, final int offset, final int limit) throws Exception {
		List<ReferenceCode> refrenceCodeList = this.refrenceCodeService.getCnotetypesByCaseLoad(caseLoad);
		return GetReferenceDomainsCaseNotesTypesByCaseLoadResponse.withJsonOK(refrenceCodeList);
	}

	@Override
	public GetReferenceDomainsCaseNotesSubTypesByCaseNoteTypeResponse getReferenceDomainsCaseNotesSubTypesByCaseNoteType(final String caseNoteType, final int offset, final int limit) throws Exception {
		List<ReferenceCode> refrenceCodeList = this.refrenceCodeService.getCnoteSubtypesByCaseNoteType(caseNoteType);
		return GetReferenceDomainsCaseNotesSubTypesByCaseNoteTypeResponse.withJsonOK(refrenceCodeList);
	}

	@Override
	public GetReferenceDomainsAlertTypesResponse getReferenceDomainsAlertTypes(int offset, int limit) throws Exception {
		List<ReferenceCode> refrenceCodeList = this.refrenceCodeService.getAlertTypes(offset, limit);
		return GetReferenceDomainsAlertTypesResponse.withJsonOK(refrenceCodeList);
	}

	@Override
	public GetReferenceDomainsAlertTypesByAlertTypeResponse getReferenceDomainsAlertTypesByAlertType(String alertType)
			throws Exception {
		ReferenceCode referenceCode = this.refrenceCodeService.getAlertTypesByAlertType(alertType);
		return GetReferenceDomainsAlertTypesByAlertTypeResponse.withJsonOK(referenceCode);
	}

	@Override
	public GetReferenceDomainsAlertTypesByAlertTypeCodesResponse getReferenceDomainsAlertTypesByAlertTypeCodes(
			String alertType, int offset, int limit) throws Exception {
		List<ReferenceCode> refrenceCodeList = this.refrenceCodeService.getAlertTypesByAlertTypeCode(alertType, offset, limit);
		return GetReferenceDomainsAlertTypesByAlertTypeCodesResponse.withJsonOK(refrenceCodeList);
	}

	@Override
	public GetReferenceDomainsAlertTypesByAlertTypeCodesByAlertCodeResponse getReferenceDomainsAlertTypesByAlertTypeCodesByAlertCode(
			String alertType, String alertCode) throws Exception {
		ReferenceCode referenceCode = this.refrenceCodeService.getAlertTypeCodesByAlertCode(alertType, alertCode);
		return GetReferenceDomainsAlertTypesByAlertTypeCodesByAlertCodeResponse.withJsonOK(referenceCode);
	}




}
