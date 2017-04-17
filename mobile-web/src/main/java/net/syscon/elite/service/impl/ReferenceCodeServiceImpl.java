package net.syscon.elite.service.impl;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.syscon.elite.persistence.ReferenceCodeRepository;
import net.syscon.elite.service.ReferenceCodeService;
import net.syscon.elite.web.api.model.Referencecode;

@Service
@Transactional
public class ReferenceCodeServiceImpl implements ReferenceCodeService {
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private ReferenceCodeRepository referenceCodeRepository;
	@Inject
	public void setReferenceCodeRepository(ReferenceCodeRepository referenceCodeRepository) {
		this.referenceCodeRepository = referenceCodeRepository;
	}
	@Override
	public List<Referencecode> getCnotetypesByCaseLoad(String caseLoad) {
		return referenceCodeRepository.getCnotetypesByCaseLoad(caseLoad);
	}
	@Override
	public List<Referencecode> getCnoteSubtypesByCaseNoteType(String caseNotetype) {
		return referenceCodeRepository.getCnoteSubtypesByCaseNoteType(caseNotetype);
	}
	@Override
	public List<Referencecode> getReferencecodesForDomain(String domain) {
		// TODO Auto-generated method stub
		return referenceCodeRepository.getReferencecodesForDomain(domain);
	}
	@Override
	public Referencecode getRefrenceCodeDescriptionForCode(String domain, String code) {
		// TODO Auto-generated method stub
		return referenceCodeRepository.getReferencecodeDescriptionForCode(domain, code);
	}

	

}
