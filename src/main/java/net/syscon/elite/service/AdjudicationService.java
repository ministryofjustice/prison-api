package net.syscon.elite.service;

import net.syscon.elite.api.model.adjudications.Adjudication;
import net.syscon.elite.api.model.adjudications.AdjudicationDetail;
import net.syscon.elite.api.model.adjudications.AdjudicationSummary;
import net.syscon.elite.api.model.adjudications.AdjudicationOffence;
import net.syscon.elite.api.model.Agency;
import net.syscon.elite.api.support.Page;

import java.time.LocalDate;
import java.util.List;

public interface AdjudicationService {

    Page<Adjudication> findAdjudications(AdjudicationSearchCriteria criteria);

    List<AdjudicationOffence> findAdjudicationsOffences(String offenderNo);

    List<Agency> findAdjudicationAgencies(String offenderNo);

    AdjudicationSummary getAdjudicationSummary(Long bookingId, LocalDate awardCutoffDate, LocalDate adjudicationCutoffDate);

    AdjudicationDetail findAdjudication(String offenderNo, long adjudicationNo);
}
