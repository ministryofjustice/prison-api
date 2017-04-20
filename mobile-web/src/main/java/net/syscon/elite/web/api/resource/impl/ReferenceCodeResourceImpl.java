package net.syscon.elite.web.api.resource.impl;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import net.syscon.elite.service.ReferenceCodeService;
import net.syscon.elite.web.api.model.Referencecode;
import net.syscon.elite.web.api.resource.ReferencecodesResource;

@Component
public class ReferenceCodeResourceImpl implements ReferencecodesResource {
	
	ReferenceCodeService refrenceCodeService;
	@Inject
	public void setRefrenceCodeService(ReferenceCodeService refrenceCodeService) {
		this.refrenceCodeService = refrenceCodeService;
	}

	@Override
	public GetReferencecodesCnotetypesByCaseLoadResponse getReferencecodesCnotetypesByCaseLoad(String caseLoad)
			throws Exception {
		List<Referencecode> refrenceCodeList = this.refrenceCodeService.getCnotetypesByCaseLoad(caseLoad);
		return GetReferencecodesCnotetypesByCaseLoadResponse.withJsonOK(refrenceCodeList);
	}

	@Override
	public GetReferencecodesCnotesubtypesByCaseNoteTypeResponse getReferencecodesCnotesubtypesByCaseNoteType(
			String caseNoteType) throws Exception {
		List<Referencecode> refrenceCodeList = this.refrenceCodeService.getCnoteSubtypesByCaseNoteType(caseNoteType);
		return GetReferencecodesCnotesubtypesByCaseNoteTypeResponse.withJsonOK(refrenceCodeList);
	}

	@Override
	public GetReferencecodesByDomainResponse getReferencecodesByDomain(String domain) throws Exception {
		List<Referencecode> refrenceCodeList = this.refrenceCodeService.getReferencecodesForDomain(domain);
		return GetReferencecodesByDomainResponse.withJsonOK(refrenceCodeList);
	}

	@Override
	public GetReferencecodesByDomainByCodeResponse getReferencecodesByDomainByCode(String domain, String code)
			throws Exception {
		Referencecode referencecode = this.refrenceCodeService.getRefrenceCodeDescriptionForCode(domain, code);
		return GetReferencecodesByDomainByCodeResponse.withJsonOK(referencecode);
	}

}
