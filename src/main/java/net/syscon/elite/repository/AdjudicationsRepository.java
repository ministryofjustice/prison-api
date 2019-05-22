package net.syscon.elite.repository;

import net.syscon.elite.api.model.adjudications.Adjudication;
import net.syscon.elite.api.model.adjudications.AdjudicationDetail;
import net.syscon.elite.api.model.adjudications.AdjudicationOffence;
import net.syscon.elite.api.model.Agency;
import net.syscon.elite.api.model.adjudications.Award;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.service.AdjudicationSearchCriteria;

import java.util.List;
import java.util.Optional;

public interface AdjudicationsRepository {

    List<Award> findAwards(long bookingId);

    List<AdjudicationOffence> findAdjudicationOffences(String offenderNumber);

    List<Agency> findAdjudicationAgencies(String offenderNumber);

    Page<Adjudication> findAdjudications(AdjudicationSearchCriteria criteria);

    Optional<AdjudicationDetail> findAdjudicationDetails(String offenderNumber, long adjudicationNumber);
}
