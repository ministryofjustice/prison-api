package net.syscon.elite.service;

import java.util.List;

import net.syscon.elite.web.api.model.ReferenceCode;



public interface ReferenceDomainService {
	
	public List<ReferenceCode> getCnotetypesByCaseLoad(String caseLoad);
	public List<ReferenceCode> getCnoteSubtypesByCaseNoteType(String caseNoteType);
	public List<ReferenceCode> getAlertTypes(int offset, int limit);
	public ReferenceCode getAlertTypesByAlertType(String alertType);
	public List<ReferenceCode> getAlertTypesByAlertTypeCode(String alertType, int offset, int limit);
	public ReferenceCode getAlertTypeCodesByAlertCode(String alertType, String alertCode);
	
	
	
	public List<ReferenceCode> getReferencecodesForDomain(String domain);
	public ReferenceCode getRefrenceCodeDescriptionForCode(String domain, String code);
	
	
	
	
}
