package net.syscon.elite.service;

import net.syscon.elite.api.model.AdjudicationDetail;

import java.time.LocalDate;

public interface AdjudicationService {

    AdjudicationDetail getAdjudications(final long bookingId, LocalDate awardCutoffDate, LocalDate adjudicationCutoffDate);
}
