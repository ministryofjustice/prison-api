package net.syscon.elite.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import net.syscon.elite.api.support.PageRequest;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class InmateSearchCriteria {
    private final String username;
    private final String query;
    private final List<Long> bookingIds;
    private final List<String> offenderNos;
    private final boolean iepLevel;
    private final PageRequest pageRequest;
}
