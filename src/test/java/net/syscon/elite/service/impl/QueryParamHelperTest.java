package net.syscon.elite.service.impl;

import org.assertj.core.data.MapEntry;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class QueryParamHelperTest {
    @Test
    public void splitTypes_empty() {
        final var map = QueryParamHelper.splitTypes(List.of());
        assertThat(map).isEmpty();
    }

    @Test
    public void splitTypes() {
        final var map = QueryParamHelper.splitTypes(List.of("BOB+JOE", "BOB+FRED", "HARRY", "JOHN+SMITH"));
        assertThat(map).containsKeys("BOB", "HARRY", "JOHN").containsExactly(
                MapEntry.entry("BOB", List.of("JOE", "FRED")),
                MapEntry.entry("HARRY", List.of()),
                MapEntry.entry("JOHN", List.of("SMITH")));
    }
}
