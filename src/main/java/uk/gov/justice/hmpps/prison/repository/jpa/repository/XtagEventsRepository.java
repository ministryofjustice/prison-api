package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.service.filters.OffenderEventsFilter;
import uk.gov.justice.hmpps.prison.service.xtag.XtagEventNonJpa;

import java.util.List;

@Repository
public interface XtagEventsRepository {
    List<XtagEventNonJpa> findAll(OffenderEventsFilter oeFilter);
    List<XtagEventNonJpa> findTest(OffenderEventsFilter oeFilter, boolean useEnq);
}
