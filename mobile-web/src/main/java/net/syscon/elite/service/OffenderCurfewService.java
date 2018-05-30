package net.syscon.elite.service;

import net.syscon.elite.api.model.OffenderSentenceDetail;

import java.util.List;

public interface OffenderCurfewService {

    List<OffenderSentenceDetail> getHomeDetentionCurfewCandidates(String agencyId, String username);
}
