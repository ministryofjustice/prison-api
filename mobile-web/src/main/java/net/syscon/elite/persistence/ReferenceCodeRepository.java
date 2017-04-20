package net.syscon.elite.persistence;

import java.util.List;

import net.syscon.elite.web.api.model.ReferenceCode;

public interface ReferenceCodeRepository {
	public List<ReferenceCode> getCnotetypesByCaseLoad(String caseLoad);
	public List<ReferenceCode> getCnoteSubtypesByCaseNoteType(String caseNotetype);
	public List<ReferenceCode> getReferenceCodesForDomain(String domain);
	public ReferenceCode getReferenceCodeDescriptionForCode(String domain, String code);
}
