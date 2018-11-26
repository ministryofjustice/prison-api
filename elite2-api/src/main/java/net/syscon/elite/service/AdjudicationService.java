package net.syscon.elite.service;

import net.syscon.elite.api.model.AdjudicationDetail;

import java.time.LocalDate;

public interface AdjudicationService {
    AdjudicationDetail getAdjudications(Long bookingId, LocalDate awardCutoffDate, LocalDate adjudicationCutoffDate);
}
