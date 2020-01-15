package net.syscon.elite.repository;

import java.util.Set;

public interface OffenderDeletionRepository {

    Set<String> deleteOffender(String offenderNo);
}
