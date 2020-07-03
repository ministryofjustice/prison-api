package uk.gov.justice.hmpps.prison.repository;

import uk.gov.justice.hmpps.prison.api.model.Agency;
import uk.gov.justice.hmpps.prison.api.model.adjudications.Adjudication;
import uk.gov.justice.hmpps.prison.api.model.adjudications.AdjudicationDetail;
import uk.gov.justice.hmpps.prison.api.model.adjudications.AdjudicationOffence;
import uk.gov.justice.hmpps.prison.api.model.adjudications.Award;
import uk.gov.justice.hmpps.prison.api.support.Page;
import uk.gov.justice.hmpps.prison.service.AdjudicationSearchCriteria;

import java.util.List;
import java.util.Optional;

public interface AdjudicationsRepository {

    List<Award> findAwards(long bookingId);

    List<AdjudicationOffence> findAdjudicationOffences(String offenderNumber);

    List<Agency> findAdjudicationAgencies(String offenderNumber);

    Page<Adjudication> findAdjudications(AdjudicationSearchCriteria criteria);

    Optional<AdjudicationDetail> findAdjudicationDetails(String offenderNumber, long adjudicationNumber);
}
