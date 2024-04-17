package uk.gov.justice.hmpps.prison.repository;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceCalculation;

import java.util.Optional;

public interface SentenceCalculationRepository extends JpaRepository<SentenceCalculation, Long> {
    @NotNull
    Optional<SentenceCalculation> findById(@NotNull Long id);
}
