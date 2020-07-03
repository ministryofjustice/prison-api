package net.syscon.prison.repository;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import net.syscon.prison.api.support.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
public class OffenderBookingSearchRequest {

    private Set<String> caseloads;
    private String offenderNo;
    private String searchTerm1;
    private String searchTerm2;
    private String locationPrefix;
    private List<String> alerts;
    private String convictedStatus;
    private LocalDate fromDob;
    private LocalDate toDob;
    private PageRequest pageRequest;
}
