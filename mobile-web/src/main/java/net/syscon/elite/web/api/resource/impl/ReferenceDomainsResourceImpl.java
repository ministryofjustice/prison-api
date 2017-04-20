package net.syscon.elite.web.api.resource.impl;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import net.syscon.elite.service.ReferenceDomainService;
import net.syscon.elite.web.api.resource.ReferenceDomainsResource;

@Component
public class ReferenceDomainsResourceImpl implements ReferenceDomainsResource {


	ReferenceDomainService refrenceCodeService;

	@Inject
	public void setRefrenceCodeService(final ReferenceDomainService refrenceCodeService) {
		this.refrenceCodeService = refrenceCodeService;
	}

	@Override
	public GetReferenceDomainsDomainsResponse getReferenceDomainsDomains(final String query, final int offset, final int limit) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GetReferenceDomainsDomainsByDomainIdResponse getReferenceDomainsDomainsByDomainId(final String domainId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GetReferenceDomainsDomainsByDomainIdCodesResponse getReferenceDomainsDomainsByDomainIdCodes(final String domainId, final String query, final int offset, final int limit) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GetReferenceDomainsDomainsByDomainIdCodesByCodeIdResponse getReferenceDomainsDomainsByDomainIdCodesByCodeId(final String domainId, final String codeId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GetReferenceDomainsCasenotesTypesByCaseLoadResponse getReferenceDomainsCasenotesTypesByCaseLoad(final String caseLoad, final int offset, final int limit) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GetReferenceDomainsCasenotesSubtypesByCaseNoteTypeResponse getReferenceDomainsCasenotesSubtypesByCaseNoteType(final String caseNoteType, final int offset, final int limit) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}


}
