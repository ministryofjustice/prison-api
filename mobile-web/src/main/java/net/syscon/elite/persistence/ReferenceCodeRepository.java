package net.syscon.elite.persistence;

import java.util.List;

import net.syscon.elite.web.api.model.Referencecode;

public interface ReferenceCodeRepository {
	public List<Referencecode> getCnotetypesByCaseLoad(String caseLoad);
	public List<Referencecode> getCnoteSubtypesByCaseNoteType(String caseNotetype);
	public List<Referencecode> getReferencecodesForDomain(String domain);
	public Referencecode getReferencecodeDescriptionForCode(String domain, String code); 
}
