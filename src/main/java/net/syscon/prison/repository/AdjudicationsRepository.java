package net.syscon.prison.repository;

import net.syscon.prison.api.model.Agency;
import net.syscon.prison.api.model.adjudications.Adjudication;
import net.syscon.prison.api.model.adjudications.AdjudicationDetail;
import net.syscon.prison.api.model.adjudications.AdjudicationOffence;
import net.syscon.prison.api.model.adjudications.Award;
import net.syscon.prison.api.support.Page;
import net.syscon.prison.service.AdjudicationSearchCriteria;

import java.util.List;
import java.util.Optional;

public interface AdjudicationsRepository {

    List<Award> findAwards(long bookingId);

    List<AdjudicationOffence> findAdjudicationOffences(String offenderNumber);

    List<Agency> findAdjudicationAgencies(String offenderNumber);

    Page<Adjudication> findAdjudications(AdjudicationSearchCriteria criteria);

    Optional<AdjudicationDetail> findAdjudicationDetails(String offenderNumber, long adjudicationNumber);
}
