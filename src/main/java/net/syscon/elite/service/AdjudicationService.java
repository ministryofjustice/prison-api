package net.syscon.elite.service;

import net.syscon.elite.api.model.Adjudication;
import net.syscon.elite.api.model.AdjudicationDetail;
import net.syscon.elite.api.support.Page;

import java.time.LocalDate;

public interface AdjudicationService {

    Page<Adjudication> findAdjudications(AdjudicationSearchCriteria criteria);

    AdjudicationDetail getAdjudications(Long bookingId, LocalDate awardCutoffDate, LocalDate adjudicationCutoffDate);
}
