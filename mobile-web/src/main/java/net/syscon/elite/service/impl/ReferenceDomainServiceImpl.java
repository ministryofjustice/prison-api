package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.ReferenceCode;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.repository.ReferenceCodeRepository;
import net.syscon.elite.service.ReferenceDomainService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class ReferenceDomainServiceImpl implements ReferenceDomainService {
	private final ReferenceCodeRepository referenceCodeRepository;

	public ReferenceDomainServiceImpl(ReferenceCodeRepository referenceCodeRepository) {
		this.referenceCodeRepository = referenceCodeRepository;
	}

	private String getDefaultOrderBy(String orderBy) {
		return StringUtils.defaultIfBlank(orderBy, "code");
	}

	private Order getDefaultOrder(Order order) {
		return Objects.isNull(order) ? Order.ASC : order;
	}

	@Override
    @Cacheable("alertTypes")
	public Page<ReferenceCode> getAlertTypes(String orderBy, Order order, long offset, long limit) {
		return referenceCodeRepository.getReferenceCodesByDomain("ALERT", true, getDefaultOrderBy(orderBy), getDefaultOrder(order), offset, limit);
	}

	@Override
    @Cacheable("caseNoteSources")
	public Page<ReferenceCode> getCaseNoteSources(String orderBy, Order order, long offset, long limit) {
		return referenceCodeRepository.getReferenceCodesByDomain("NOTE_SOURCE", false, getDefaultOrderBy(orderBy), getDefaultOrder(order), offset, limit);
	}

	@Override
	@Cacheable("caseNoteTypes")
	public Page<ReferenceCode> getCaseNoteTypes(String orderBy, Order order, long offset, long limit) {
		return referenceCodeRepository.getReferenceCodesByDomain("TASK_TYPE", true, getDefaultOrderBy(orderBy), getDefaultOrder(order), offset, limit);
	}
}
