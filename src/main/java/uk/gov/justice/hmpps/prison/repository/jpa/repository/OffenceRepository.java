package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.HOCode;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offence;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offence.PK;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Statute;

public interface OffenceRepository extends CrudRepository<Offence, PK> {
    Page<Offence> findAll(Pageable pageable);

    Page<Offence> findAllByActive(final boolean active, Pageable pageable);

    Page<Offence> findAllByDescriptionLike(final String description, Pageable pageable);

    Page<Offence> findAllByHoCode(final HOCode hoCode, Pageable pageable);

    Page<Offence> findAllByStatute(final Statute statute, Pageable pageable);

}
