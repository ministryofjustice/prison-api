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
import java.util.List;

@Component
public class ReferenceDomainsResourceImpl implements ReferenceDomainsResource {

	private final ReferenceDomainService referenceDomainService;

	@Inject
	public ReferenceDomainsResourceImpl(final ReferenceDomainService referenceDomainService) {
		this.referenceDomainService = referenceDomainService;
	}

	private HttpStatus createHttpStatus(int httpStatusCode, String message) {
		return new HttpStatus("" + httpStatusCode, "" + httpStatusCode, message, message, "");
	}


	@Override
	public GetReferenceDomainsCaseNotesTypesByCaseLoadResponse getReferenceDomainsCaseNotesTypesByCaseLoad(final String caseLoad, final int offset, final int limit) throws Exception {
		List<ReferenceCode> referenceCodeList = this.referenceDomainService.getCaseNoteTypesByCaseLoad(caseLoad, offset, limit);
		ReferenceCodes referenceCodes = new ReferenceCodes(referenceCodeList, MetaDataFactory.createMetaData(limit, offset, referenceCodeList));
		return GetReferenceDomainsCaseNotesTypesByCaseLoadResponse.withJsonOK(referenceCodes);
	}

	@Override
	public GetReferenceDomainsCaseNotesSubTypesByCaseNoteTypeResponse getReferenceDomainsCaseNotesSubTypesByCaseNoteType(final String caseNoteType, final int offset, final int limit) throws Exception {
		// TODO: After IG change the endpoint we must switch the method getCnoteSubtypesByCaseNoteType => getCaseNoteSubTypesByParent
		// List<ReferenceCode> referenceCodeList = this.referenceDomainService.getCaseNoteSubTypesByParent(caseNoteType, offset, limit);
		List<ReferenceCode> referenceCodeList = this.referenceDomainService.getCnoteSubtypesByCaseNoteType(caseNoteType, offset, limit);
		ReferenceCodes referenceCodes = new ReferenceCodes(referenceCodeList, MetaDataFactory.createMetaData(limit, offset, referenceCodeList));
		return GetReferenceDomainsCaseNotesSubTypesByCaseNoteTypeResponse.withJsonOK(referenceCodes);
	}

	@Override
	public GetReferenceDomainsCaseNoteSourcesResponse getReferenceDomainsCaseNoteSources(String query, String orderBy, Order order,int offset, int limit) throws Exception {
		List<ReferenceCode> referenceCodeList = this.referenceDomainService.getCaseNoteSources(query, orderBy, order, offset, limit);
		ReferenceCodes referenceCodes = new ReferenceCodes(referenceCodeList, MetaDataFactory.createMetaData(limit, offset, referenceCodeList));
		return GetReferenceDomainsCaseNoteSourcesResponse.withJsonOK(referenceCodes);
	}

	@Override
	public GetReferenceDomainsCaseNoteSourcesBySourceCodeResponse getReferenceDomainsCaseNoteSourcesBySourceCode(String sourceCode) throws Exception {
		try {
			ReferenceCode referenceCode = this.referenceDomainService.getCaseNoteSource(sourceCode);
			return GetReferenceDomainsCaseNoteSourcesBySourceCodeResponse.withJsonOK(referenceCode);
		} catch (DataAccessException ex) {
			return GetReferenceDomainsCaseNoteSourcesBySourceCodeResponse.withJsonNotFound(createHttpStatus(404, String.format("Case Note Source with code: \"%s\" was not found", sourceCode)));
		}
	}

	@Override
	public GetReferenceDomainsAlertTypesResponse getReferenceDomainsAlertTypes(String query, String orderBy, Order order, int offset, int limit) throws Exception {
		List<ReferenceCode> referenceCodeList = this.referenceDomainService.getAlertTypes(query, orderBy, order, offset, limit);
		ReferenceCodes referenceCodes = new ReferenceCodes(referenceCodeList, MetaDataFactory.createMetaData(limit, offset, referenceCodeList));
		return GetReferenceDomainsAlertTypesResponse.withJsonOK(referenceCodes);
	}

	@Override
	public GetReferenceDomainsAlertTypesByAlertTypeResponse getReferenceDomainsAlertTypesByAlertType(String alertType) throws Exception {
		ReferenceCode referenceCode = this.referenceDomainService.getAlertTypeByCode(alertType);
		return GetReferenceDomainsAlertTypesByAlertTypeResponse.withJsonOK(referenceCode);
	}

	@Override
	public GetReferenceDomainsAlertTypesByAlertTypeCodesResponse getReferenceDomainsAlertTypesByAlertTypeCodes(String alertType, String query, String orderBy, Order order, int offset, int limit) throws Exception {
		List<ReferenceCode> referenceCodeList = this.referenceDomainService.getAlertTypesByParent(alertType, query, orderBy, order, offset, limit);
		ReferenceCodes referenceCodes = new ReferenceCodes(referenceCodeList, MetaDataFactory.createMetaData(limit, offset, referenceCodeList));
		return GetReferenceDomainsAlertTypesByAlertTypeCodesResponse.withJsonOK(referenceCodes);
	}

	@Override
	public GetReferenceDomainsAlertTypesByAlertTypeCodesByAlertCodeResponse getReferenceDomainsAlertTypesByAlertTypeCodesByAlertCode(
			String alertType, String alertCode) throws Exception {
		ReferenceCode referenceCode = this.referenceDomainService.getAlertTypeByParentAndCode(alertType, alertCode);
		return GetReferenceDomainsAlertTypesByAlertTypeCodesByAlertCodeResponse.withJsonOK(referenceCode);
	}


	@Override
	public GetReferenceDomainsCaseNoteTypesResponse getReferenceDomainsCaseNoteTypes(String query, String orderBy, Order order, int offset, int limit) throws Exception {
		List<ReferenceCode> referenceCodeList = this.referenceDomainService.getCaseNoteTypes(query, orderBy, order, offset, limit);
		ReferenceCodes referenceCodes = new ReferenceCodes(referenceCodeList, MetaDataFactory.createMetaData(limit, offset, referenceCodeList));
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
	public GetReferenceDomainsCaseNoteTypesByTypeCodeSubTypesResponse getReferenceDomainsCaseNoteTypesByTypeCodeSubTypes(String typeCode, String query, String orderBy, Order order, int offset, int limit) throws Exception {
		List<ReferenceCode> referenceCodeList = this.referenceDomainService.getCaseNoteSubTypes(typeCode, query, orderBy, order, offset, limit);
		ReferenceCodes referenceCodes = new ReferenceCodes(referenceCodeList, MetaDataFactory.createMetaData(limit, offset, referenceCodeList));
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
