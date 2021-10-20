package uk.gov.justice.hmpps.prison.repository.jpa.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class GlobalVisitorRestrictionTest {
    @Test
    void isActive_startDate_today() {
        assertThat(GlobalVisitorRestriction.builder().startDate(LocalDate.now()).build().isActive()).isTrue();
    }

    @Test
    void isActive_startDate_tomorrow() {
        assertThat(GlobalVisitorRestriction.builder().startDate(LocalDate.now().plusDays(1)).build().isActive()).isFalse();
    }

    @Test
    void isActive_with_expiryDate() {
        assertThat(GlobalVisitorRestriction.builder().startDate(LocalDate.now().minusDays(20)).expiryDate(LocalDate.now()).build().isActive()).isTrue();
    }

    @Test
    void isActive_hasExpired() {
        assertThat(GlobalVisitorRestriction.builder().startDate(LocalDate.now().minusDays(20)).expiryDate(LocalDate.now().minusDays(1)).build().isActive()).isFalse();
    }
}
