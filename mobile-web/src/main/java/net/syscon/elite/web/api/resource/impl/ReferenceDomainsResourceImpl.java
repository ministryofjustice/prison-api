package net.syscon.elite.web.api.resource.impl;

import net.syscon.elite.service.ReferenceDomainService;
import net.syscon.elite.web.api.model.HttpStatus;
import net.syscon.elite.web.api.model.ReferenceCode;
import net.syscon.elite.web.api.model.ReferenceCodes;
import net.syscon.elite.web.api.resource.ReferenceDomainsResource;
import net.syscon.util.MetaDataFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import java.util.List;

@Component
public class ReferenceDomainsResourceImpl implements ReferenceDomainsResource {

	private final ReferenceDomainService referenceDomainService;

	@Inject
	public ReferenceDomainsResourceImpl(final ReferenceDomainService referenceDomainService) {
		this.referenceDomainService = referenceDomainService;
	}

	@Override
	public GetReferenceDomainsCaseNotesTypesByCaseLoadResponse getReferenceDomainsCaseNotesTypesByCaseLoad(final String caseLoad, final int offset, final int limit) throws Exception {
		List<ReferenceCode> refrenceCodeList = this.referenceDomainService.getCaseNoteTypesByCaseLoad(caseLoad, offset, limit);
		ReferenceCodes referenceCodes = new ReferenceCodes(refrenceCodeList, MetaDataFactory.createMetaData(limit, offset, refrenceCodeList));
		return GetReferenceDomainsCaseNotesTypesByCaseLoadResponse.withJsonOK(referenceCodes);
	}

	@Override
	public GetReferenceDomainsCaseNotesSubTypesByCaseNoteTypeResponse getReferenceDomainsCaseNotesSubTypesByCaseNoteType(final String caseNoteType, final int offset, final int limit) throws Exception {
		List<ReferenceCode> refrenceCodeList = this.referenceDomainService.getCaseNoteSubTypesByCaseNoteType(caseNoteType, offset, limit);
		ReferenceCodes referenceCodes = new ReferenceCodes(refrenceCodeList, MetaDataFactory.createMetaData(limit, offset, refrenceCodeList));
		return GetReferenceDomainsCaseNotesSubTypesByCaseNoteTypeResponse.withJsonOK(referenceCodes);
	}

	@Override
	public GetReferenceDomainsAlertTypesResponse getReferenceDomainsAlertTypes(String query, String orderBy, Order order, int offset, int limit) throws Exception {
		List<ReferenceCode> refrenceCodeList = this.referenceDomainService.getAlertTypes(query, orderBy, order, offset, limit);
		ReferenceCodes referenceCodes = new ReferenceCodes(refrenceCodeList, MetaDataFactory.createMetaData(limit, offset, refrenceCodeList));
		return GetReferenceDomainsAlertTypesResponse.withJsonOK(referenceCodes);
	}

	@Override
	public GetReferenceDomainsAlertTypesByAlertTypeResponse getReferenceDomainsAlertTypesByAlertType(String alertType) throws Exception {
		ReferenceCode referenceCode = this.referenceDomainService.getAlertTypesByAlertType(alertType);
		return GetReferenceDomainsAlertTypesByAlertTypeResponse.withJsonOK(referenceCode);
	}

	@Override
	public GetReferenceDomainsAlertTypesByAlertTypeCodesResponse getReferenceDomainsAlertTypesByAlertTypeCodes(String alertType, String query, String orderBy, Order order, int offset, int limit) throws Exception {
		List<ReferenceCode> refrenceCodeList = this.referenceDomainService.getAlertTypesByAlertTypeCode(alertType, query, orderBy, order, offset, limit);
		ReferenceCodes referenceCodes = new ReferenceCodes(refrenceCodeList, MetaDataFactory.createMetaData(limit, offset, refrenceCodeList));
		return GetReferenceDomainsAlertTypesByAlertTypeCodesResponse.withJsonOK(referenceCodes);
	}

	@Override
	public GetReferenceDomainsAlertTypesByAlertTypeCodesByAlertCodeResponse getReferenceDomainsAlertTypesByAlertTypeCodesByAlertCode(
			String alertType, String alertCode) throws Exception {
		ReferenceCode referenceCode = this.referenceDomainService.getAlertTypeCodesByAlertCode(alertType, alertCode);
		return GetReferenceDomainsAlertTypesByAlertTypeCodesByAlertCodeResponse.withJsonOK(referenceCode);
	}


	@Override
	public GetReferenceDomainsCaseNoteSourcesResponse getReferenceDomainsCaseNoteSources(@DefaultValue("0") int offset, @DefaultValue("10") int limit) throws Exception {
		List<ReferenceCode> refrenceCodeList = this.referenceDomainService.getReferenceCodesByDomain("NOTE_SOURCE", offset, limit);
		ReferenceCodes referenceCodes = new ReferenceCodes(refrenceCodeList, MetaDataFactory.createMetaData(limit, offset, refrenceCodeList));
		return GetReferenceDomainsCaseNoteSourcesResponse.withJsonOK(referenceCodes);
	}

	private HttpStatus createHttpStatus(int httpStatusCode, String message) {
		return new HttpStatus("" + httpStatusCode, "" + httpStatusCode, message, message, "");
	}


	@Override
	public GetReferenceDomainsCaseNoteSourcesBySourceCodeResponse getReferenceDomainsCaseNoteSourcesBySourceCode(String sourceCode) throws Exception {
		try {
			ReferenceCode referenceCode = this.referenceDomainService.getReferenceCodeByDomainAndCode("NOTE_SOURCE", sourceCode);
			return GetReferenceDomainsCaseNoteSourcesBySourceCodeResponse.withJsonOK(referenceCode);
		} catch (DataAccessException ex) {
			return GetReferenceDomainsCaseNoteSourcesBySourceCodeResponse.withJsonNotFound(createHttpStatus(404, String.format("Case Note Source with code: \"%s\" was not found", sourceCode)));
		}
	}

	@Override
	public GetReferenceDomainsCaseNoteTypesResponse getReferenceDomainsCaseNoteTypes(String query, String orderBy, @DefaultValue("asc") Order order, @DefaultValue("0") int offset, @DefaultValue("10") int limit) throws Exception {
		List<ReferenceCode> refrenceCodeList = this.referenceDomainService.getCaseNoteTypes(query, orderBy, order, offset, limit);
		ReferenceCodes referenceCodes = new ReferenceCodes(refrenceCodeList, MetaDataFactory.createMetaData(limit, offset, refrenceCodeList));
		return GetReferenceDomainsCaseNoteTypesResponse.withJsonOK(referenceCodes);
	}

	@Override
	public GetReferenceDomainsCaseNoteTypesByTypeCodeResponse getReferenceDomainsCaseNoteTypesByTypeCode(String typeCode) throws Exception {
		try {
			ReferenceCode referenceCode = this.referenceDomainService.getCaseNoteType(typeCode);
			return GetReferenceDomainsCaseNoteTypesByTypeCodeResponse.withJsonOK(referenceCode);
		} catch (DataAccessException ex) {
			return GetReferenceDomainsCaseNoteTypesByTypeCodeResponse.withJsonNotFound(createHttpStatus(404, String.format("Case Note Type with code: \"%s\" was not found", typeCode)));
		}
	}

	@Override
	public GetReferenceDomainsCaseNoteTypesByTypeCodeSubTypesResponse getReferenceDomainsCaseNoteTypesByTypeCodeSubTypes(String typeCode, String query, String orderBy, @DefaultValue("asc") Order order, @DefaultValue("0") int offset, @DefaultValue("10") int limit) throws Exception {
		List<ReferenceCode> refrenceCodeList = this.referenceDomainService.getCaseNoteSubTypes(typeCode, query, orderBy, order, offset, limit);
		ReferenceCodes referenceCodes = new ReferenceCodes(refrenceCodeList, MetaDataFactory.createMetaData(limit, offset, refrenceCodeList));
		return GetReferenceDomainsCaseNoteTypesByTypeCodeSubTypesResponse.withJsonOK(referenceCodes);
	}

	@Override
	public GetReferenceDomainsCaseNoteTypesByTypeCodeSubTypesBySubTypeCodeResponse getReferenceDomainsCaseNoteTypesByTypeCodeSubTypesBySubTypeCode(String typeCode, String subTypeCode) throws Exception {
		try {
			ReferenceCode referenceCode = referenceDomainService.getCaseNoteSubType(typeCode, subTypeCode);
			return GetReferenceDomainsCaseNoteTypesByTypeCodeSubTypesBySubTypeCodeResponse.withJsonOK(referenceCode);
		} catch (DataAccessException ex) {
			return GetReferenceDomainsCaseNoteTypesByTypeCodeSubTypesBySubTypeCodeResponse.withJsonNotFound(createHttpStatus(404, String.format("Case Note SubType with code: \"%s\" was not found", typeCode)));
		}
	}

}
