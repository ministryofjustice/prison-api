package net.syscon.elite.persistence.repository;


import net.syscon.elite.web.api.model.InmateSummary;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface InmateRepository {

	List<InmateSummary> findSumamries(PageRequest pageRequest);


}
