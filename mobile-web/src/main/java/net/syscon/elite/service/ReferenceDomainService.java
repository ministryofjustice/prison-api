package net.syscon.elite.service;

import java.util.List;

import net.syscon.elite.web.api.model.ReferenceCode;



public interface ReferenceDomainService {
	
	public List<ReferenceCode> getCnotetypesByCaseLoad(String caseLoad);
	public List<ReferenceCode> getCnoteSubtypesByCaseNoteType(String caseNotetype);
	public List<ReferenceCode> getReferencecodesForDomain(String domain);
	public ReferenceCode getRefrenceCodeDescriptionForCode(String domain, String code);
	
}
