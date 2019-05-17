package net.syscon.elite.service;

import net.syscon.elite.api.model.Adjudication;
import net.syscon.elite.api.model.AdjudicationSummary;
import net.syscon.elite.api.model.AdjudicationOffence;
import net.syscon.elite.api.model.Agency;
import net.syscon.elite.api.support.Page;

import java.time.LocalDate;
import java.util.List;

public interface AdjudicationService {

    Page<Adjudication> findAdjudications(AdjudicationSearchCriteria criteria);

    List<AdjudicationOffence> findAdjudicationsOffences(String offenderNo);

    List<Agency> findAdjudicationAgencies(String offenderNo);

    AdjudicationSummary getAdjudicationSummary(Long bookingId, LocalDate awardCutoffDate, LocalDate adjudicationCutoffDate);
}
