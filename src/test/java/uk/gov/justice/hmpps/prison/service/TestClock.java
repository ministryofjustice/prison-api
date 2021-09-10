package uk.gov.justice.hmpps.prison.service;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.time.Clock;
import java.time.LocalDateTime;

@TestConfiguration
public class TestClock {
    @Bean
    public Clock clock() {
        return Clock.fixed(
            LocalDateTime.of(2020, 1, 2, 3, 4, 5).atZone(java.time.ZoneId.systemDefault()).toInstant(),
            java.time.ZoneId.systemDefault());
    }
}
