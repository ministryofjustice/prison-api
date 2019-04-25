package net.syscon.elite.repository;

import net.syscon.elite.api.model.Adjudication;
import net.syscon.elite.api.model.AdjudicationOffence;
import net.syscon.elite.api.model.Award;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.service.AdjudicationSearchCriteria;

import java.util.List;

public interface AdjudicationsRepository {

    List<Award> findAwards(long bookingId);

    List<AdjudicationOffence> findAdjudicationOffences(String offenderNumber);

    Page<Adjudication> findAdjudications(AdjudicationSearchCriteria criteria);
}
