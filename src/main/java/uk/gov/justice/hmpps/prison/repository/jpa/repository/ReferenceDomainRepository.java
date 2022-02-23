package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReferenceDomainRepository extends JpaRepository<uk.gov.justice.hmpps.prison.repository.jpa.model.ReferenceDomain, String> {
}
