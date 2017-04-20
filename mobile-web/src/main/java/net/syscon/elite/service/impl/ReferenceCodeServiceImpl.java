package net.syscon.elite.service.impl;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.syscon.elite.persistence.ReferenceCodeRepository;
import net.syscon.elite.service.ReferenceDomainService;
import net.syscon.elite.web.api.model.ReferenceCode;


@Service
@Transactional
public class ReferenceCodeServiceImpl implements ReferenceDomainService {
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private ReferenceCodeRepository referenceCodeRepository;
	@Inject
	public void setReferenceCodeRepository(final ReferenceCodeRepository referenceCodeRepository) {
		this.referenceCodeRepository = referenceCodeRepository;
	}
	@Override
	public List<ReferenceCode> getCnotetypesByCaseLoad(final String caseLoad) {
		return referenceCodeRepository.getCnotetypesByCaseLoad(caseLoad);
	}
	@Override
	public List<ReferenceCode> getCnoteSubtypesByCaseNoteType(final String caseNotetype) {
		return referenceCodeRepository.getCnoteSubtypesByCaseNoteType(caseNotetype);
	}
	@Override
	public List<ReferenceCode> getReferencecodesForDomain(final String domain) {
		// TODO Auto-generated method stub
		return referenceCodeRepository.getReferenceCodesForDomain(domain);
	}
	@Override
	public ReferenceCode getRefrenceCodeDescriptionForCode(final String domain, final String code) {
		// TODO Auto-generated method stub
		return referenceCodeRepository.getReferenceCodeDescriptionForCode(domain, code);
	}

	

}
