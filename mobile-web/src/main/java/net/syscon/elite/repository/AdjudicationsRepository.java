package net.syscon.elite.repository;

import net.syscon.elite.api.model.Award;

import java.util.List;

public interface AdjudicationsRepository {

    List<Award> findAwards(long bookingId);

    int getAdjudicationCount(long bookingId);
}
