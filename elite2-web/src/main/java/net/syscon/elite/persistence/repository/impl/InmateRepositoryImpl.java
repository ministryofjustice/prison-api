package net.syscon.elite.persistence.repository.impl;

import net.syscon.elite.persistence.repository.InmateRepository;
import net.syscon.elite.web.api.model.InmateSummary;
import org.springframework.data.domain.PageRequest;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

public class InmateRepositoryImpl implements InmateRepository {

	private EntityManager entityManager;

	@Inject
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}


	@Override
	public List<InmateSummary> findSumamries(PageRequest pageRequest) {

		//Query query = entityManager.createNativeQuery(sql, InmateSummary.class);





		return null;
	}
}
