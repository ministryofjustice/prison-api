package uk.gov.justice.hmpps.prison.repository.jpa.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class SentenceCalculationTest {

    @Test
    void datesShouldBeOverriddenIfTheOverrideIsPresentAndTheDateIsDifferent() {
        SentenceCalculation calc = SentenceCalculation.builder()
            .hdcedCalculatedDate(LocalDate.of(2025, 1, 1))
            .hdcedOverridedDate(LocalDate.of(2025, 1, 2))
            .crdCalculatedDate(LocalDate.of(2025, 2, 1))
            .crdOverridedDate(LocalDate.of(2025, 2, 2))
            .ledCalculatedDate(LocalDate.of(2025, 3, 1))
            .ledOverridedDate(LocalDate.of(2025, 3, 2))
            .sedCalculatedDate(LocalDate.of(2025, 4, 1))
            .sedOverridedDate(LocalDate.of(2025, 4, 2))
            .npdCalculatedDate(LocalDate.of(2025, 5, 1))
            .npdOverridedDate(LocalDate.of(2025, 5, 2))
            .ardCalculatedDate(LocalDate.of(2025, 6, 1))
            .ardOverridedDate(LocalDate.of(2025, 6, 2))
            .tusedCalculatedDate(LocalDate.of(2025, 7, 1))
            .tusedOverridedDate(LocalDate.of(2025, 7, 2))
            .pedCalculatedDate(LocalDate.of(2025, 8, 1))
            .pedOverridedDate(LocalDate.of(2025, 8, 2))
            .build();
        
        assertThat(calc.isHomeDetentionCurfewEligibilityDateOverridden()).describedAs("isHomeDetentionCurfewEligibilityDateOverridden").isTrue();
        assertThat(calc.isConditionalReleaseDateOverridden()).describedAs("isConditionalReleaseDateOverridden").isTrue();
        assertThat(calc.isLicenceExpiryDateOverridden()).describedAs("isLicenceExpiryDateOverridden").isTrue();
        assertThat(calc.isSentenceExpiryDateOverridden()).describedAs("isSentenceExpiryDateOverridden").isTrue();
        assertThat(calc.isNonParoleDateOverridden()).describedAs("isNonParoleDateOverridden").isTrue();
        assertThat(calc.isAutomaticReleaseDateOverridden()).describedAs("isAutomaticReleaseDateOverridden").isTrue();
        assertThat(calc.isTopupSupervisionExpiryDateOverridden()).describedAs("isTopupSupervisionExpiryDateOverridden").isTrue();
        assertThat(calc.isParoleEligibilityDateOverridden()).describedAs("isParoleEligibilityDateOverridden").isTrue();
    }

    @Test
    void datesShouldNotBeOverriddenIfTheOverrideIsTheSameDate() {
        SentenceCalculation calc = SentenceCalculation.builder()
            .hdcedCalculatedDate(LocalDate.of(2025, 1, 1))
            .hdcedOverridedDate(LocalDate.of(2025, 1, 1))
            .crdCalculatedDate(LocalDate.of(2025, 2, 1))
            .crdOverridedDate(LocalDate.of(2025, 2, 1))
            .ledCalculatedDate(LocalDate.of(2025, 3, 1))
            .ledOverridedDate(LocalDate.of(2025, 3, 1))
            .sedCalculatedDate(LocalDate.of(2025, 4, 1))
            .sedOverridedDate(LocalDate.of(2025, 4, 1))
            .npdCalculatedDate(LocalDate.of(2025, 5, 1))
            .npdOverridedDate(LocalDate.of(2025, 5, 1))
            .ardCalculatedDate(LocalDate.of(2025, 6, 1))
            .ardOverridedDate(LocalDate.of(2025, 6, 1))
            .tusedCalculatedDate(LocalDate.of(2025, 7, 1))
            .tusedOverridedDate(LocalDate.of(2025, 7, 1))
            .pedCalculatedDate(LocalDate.of(2025, 8, 1))
            .pedOverridedDate(LocalDate.of(2025, 8, 1))
            .build();

        assertThat(calc.isHomeDetentionCurfewEligibilityDateOverridden()).describedAs("isHomeDetentionCurfewEligibilityDateOverridden").isFalse();
        assertThat(calc.isConditionalReleaseDateOverridden()).describedAs("isConditionalReleaseDateOverridden").isFalse();
        assertThat(calc.isLicenceExpiryDateOverridden()).describedAs("isLicenceExpiryDateOverridden").isFalse();
        assertThat(calc.isSentenceExpiryDateOverridden()).describedAs("isSentenceExpiryDateOverridden").isFalse();
        assertThat(calc.isNonParoleDateOverridden()).describedAs("isNonParoleDateOverridden").isFalse();
        assertThat(calc.isAutomaticReleaseDateOverridden()).describedAs("isAutomaticReleaseDateOverridden").isFalse();
        assertThat(calc.isTopupSupervisionExpiryDateOverridden()).describedAs("isTopupSupervisionExpiryDateOverridden").isFalse();
        assertThat(calc.isParoleEligibilityDateOverridden()).describedAs("isParoleEligibilityDateOverridden").isFalse();
    }

    @Test
    void datesShouldNotBeOverriddenIfThereIsNoOverrideDate() {
        SentenceCalculation calc = SentenceCalculation.builder()
            .hdcedCalculatedDate(LocalDate.of(2025, 1, 1))
            .crdCalculatedDate(LocalDate.of(2025, 2, 1))
            .ledCalculatedDate(LocalDate.of(2025, 3, 1))
            .sedCalculatedDate(LocalDate.of(2025, 4, 1))
            .npdCalculatedDate(LocalDate.of(2025, 5, 1))
            .ardCalculatedDate(LocalDate.of(2025, 6, 1))
            .tusedCalculatedDate(LocalDate.of(2025, 7, 1))
            .pedCalculatedDate(LocalDate.of(2025, 8, 1))
            .build();

        assertThat(calc.isHomeDetentionCurfewEligibilityDateOverridden()).describedAs("isHomeDetentionCurfewEligibilityDateOverridden").isFalse();
        assertThat(calc.isConditionalReleaseDateOverridden()).describedAs("isConditionalReleaseDateOverridden").isFalse();
        assertThat(calc.isLicenceExpiryDateOverridden()).describedAs("isLicenceExpiryDateOverridden").isFalse();
        assertThat(calc.isSentenceExpiryDateOverridden()).describedAs("isSentenceExpiryDateOverridden").isFalse();
        assertThat(calc.isNonParoleDateOverridden()).describedAs("isNonParoleDateOverridden").isFalse();
        assertThat(calc.isAutomaticReleaseDateOverridden()).describedAs("isAutomaticReleaseDateOverridden").isFalse();
        assertThat(calc.isTopupSupervisionExpiryDateOverridden()).describedAs("isTopupSupervisionExpiryDateOverridden").isFalse();
        assertThat(calc.isParoleEligibilityDateOverridden()).describedAs("isParoleEligibilityDateOverridden").isFalse();
    }
}