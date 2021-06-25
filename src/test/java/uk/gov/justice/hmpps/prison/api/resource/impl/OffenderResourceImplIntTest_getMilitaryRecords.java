package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.repository.jpa.model.DisciplinaryAction;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MilitaryBranch;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MilitaryDischarge;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MilitaryRank;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderMilitaryRecord;
import uk.gov.justice.hmpps.prison.repository.jpa.model.WarZone;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class OffenderResourceImplIntTest_getMilitaryRecords extends ResourceTest {
    @MockBean
    private OffenderBookingRepository offenderBookingRepository;

    @Test
    public void shouldReturnMilitaryRecords() {
        when(offenderBookingRepository.findById(anyLong())).thenReturn(Optional.of(OffenderBooking.builder()
            .militaryRecords(List.of(
                OffenderMilitaryRecord.builder()
                    .startDate(LocalDate.parse("2000-01-01"))
                    .endDate(LocalDate.parse("2020-10-17"))
                    .militaryDischarge(new MilitaryDischarge("DIS", "Dishonourable"))
                    .warZone(new WarZone("AFG", "Afghanistan"))
                    .militaryBranch(new MilitaryBranch("ARM", "Army"))
                    .description("left")
                    .unitNumber("auno")
                    .enlistmentLocation("Somewhere")
                    .militaryRank(new MilitaryRank("LCPL_RMA", "Lance Corporal  (Royal Marines)"))
                    .serviceNumber("asno")
                    .disciplinaryAction(new DisciplinaryAction("CM", "Court Martial"))
                    .dischargeLocation("Sheffield")
                    .build(),
                OffenderMilitaryRecord.builder()
                    .startDate(LocalDate.parse("2001-01-01"))
                    .militaryBranch(new MilitaryBranch("NAV", "Navy"))
                    .description("second record")
                    .build()))
            .build()));

        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of(), Map.of());

        final var responseEntity = testRestTemplate.exchange(
            "/api/offenders/A1234AA/military-records", HttpMethod.GET, requestEntity, String.class);

        assertThatJsonFileAndStatus(responseEntity, 200, "military_records.json");
    }

    @Test
    public void shouldReturn404WhenOffenderNotFound() {
        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of(), Map.of());

        final var response = testRestTemplate.exchange(
            "/api/offenders/A1554AN/military-records", HttpMethod.GET, requestEntity, ErrorResponse.class);

        assertThat(response.getBody()).isEqualTo(
            ErrorResponse.builder()
                .status(404)
                .userMessage("Resource with id [A1554AN] not found.")
                .developerMessage("Resource with id [A1554AN] not found.")
                .build());
    }

    @Test
    public void shouldReturn404WhenNoPrivileges() {
        // run with user that doesn't have access to the caseload
        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER_ADM", List.of(), Map.of());

        final var response = testRestTemplate.exchange(
            "/api/offenders/A1234AA/military-records", HttpMethod.GET, requestEntity, ErrorResponse.class);

        assertThat(response.getBody()).isEqualTo(
            ErrorResponse.builder()
                .status(404)
                .userMessage("Resource with id [A1234AA] not found.")
                .developerMessage("Resource with id [A1234AA] not found.")
                .build());

        verifyNoInteractions(offenderBookingRepository);
    }
}
