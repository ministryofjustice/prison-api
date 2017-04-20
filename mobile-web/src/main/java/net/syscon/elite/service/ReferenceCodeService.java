package net.syscon.elite.service;

import java.util.List;

import net.syscon.elite.web.api.model.Referencecode;

public interface ReferenceCodeService {
	
	public List<Referencecode> getCnotetypesByCaseLoad(String caseLoad);
	public List<Referencecode> getCnoteSubtypesByCaseNoteType(String caseNotetype);
	public List<Referencecode> getReferencecodesForDomain(String domain);
	public Referencecode getRefrenceCodeDescriptionForCode(String domain, String code);
	
}
