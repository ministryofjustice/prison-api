package net.syscon.elite.service;

import net.syscon.elite.api.model.Adjudication;
import net.syscon.elite.api.model.AdjudicationDetail;
import net.syscon.elite.api.model.AdjudicationOffence;
import net.syscon.elite.api.support.Page;

import java.time.LocalDate;
import java.util.List;

public interface AdjudicationService {

    Page<Adjudication> findAdjudications(AdjudicationSearchCriteria criteria);

    List<AdjudicationOffence> findAdjudicationsOffences(String offenderNo);

    AdjudicationDetail getAdjudications(Long bookingId, LocalDate awardCutoffDate, LocalDate adjudicationCutoffDate);
}
