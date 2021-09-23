package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpMethod;
import uk.gov.justice.hmpps.prison.api.model.PersonalCareNeed;
import uk.gov.justice.hmpps.prison.api.model.ReasonableAdjustment;
import uk.gov.justice.hmpps.prison.api.model.ScheduledEvent;
import uk.gov.justice.hmpps.prison.repository.BookingRepository;
import uk.gov.justice.hmpps.prison.repository.InmateRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocationType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CaseStatus;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtEvent;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.DisciplinaryAction;
import uk.gov.justice.hmpps.prison.repository.jpa.model.LegalCaseType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MilitaryBranch;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MilitaryDischarge;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MilitaryRank;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCourtCase;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderMilitaryRecord;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderPropertyContainer;
import uk.gov.justice.hmpps.prison.repository.jpa.model.PropertyContainer;
import uk.gov.justice.hmpps.prison.repository.jpa.model.WarZone;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BookingResourceImplIntTest extends ResourceTest {

    @MockBean
    private InmateRepository inmateRepository;
    @SpyBean
    private BookingRepository bookingRepository;
    @MockBean
    private OffenderBookingRepository offenderBookingRepository;

    @Test
    public void getPersonalCareNeeds() {
        final var bookingId = -1;

        when(inmateRepository.findPersonalCareNeeds(anyLong(), anySet())).thenReturn(List.of(createPersonalCareNeeds()));

        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of(), Map.of());

        final var responseEntity = testRestTemplate.exchange("/api/bookings/-1/personal-care-needs?type=MATSTAT&type=DISAB+RM&type=DISAB+RC", HttpMethod.GET, requestEntity, String.class);

        assertThatJsonFileAndStatus(responseEntity, 200, "personalcareneeds.json");

        verify(inmateRepository).findPersonalCareNeeds(bookingId, Set.of("DISAB", "MATSTAT"));
    }

    @Test
    public void postPersonalCareNeedsForOffenders() {
        when(inmateRepository.findPersonalCareNeeds(anyList(), anySet())).thenReturn(createPersonalCareNeedsForOffenders());

        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", List.of(), List.of("A1234AA", "A1234AB", "A1234AC"));

        final var responseEntity = testRestTemplate.exchange("/api/bookings/offenderNo/personal-care-needs?type=MATSTAT&type=DISAB+RM&type=DISAB+RC", HttpMethod.POST, requestEntity, String.class);

        assertThatJsonFileAndStatus(responseEntity, 200, "personalcareneeds_offenders.json");

        verify(inmateRepository).findPersonalCareNeeds(List.of("A1234AA", "A1234AB", "A1234AC"), Set.of("DISAB", "MATSTAT"));
    }

    @Test
    public void getPersonalCareNeeds_missingProblemType() {
        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of(), Map.of());
        final var responseEntity = testRestTemplate.exchange("/api/bookings/-1/personal-care-needs", HttpMethod.GET, requestEntity, String.class);
        assertThatJsonFileAndStatus(responseEntity, 400, "personalcareneeds_validation.json");
    }

    @Test
    public void postPersonalCareNeedsForOffenders_missingOffenders() {
        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", List.of(), List.of());
        final var responseEntity = testRestTemplate.exchange("/api/bookings/offenderNo/personal-care-needs?type=MATSTAT", HttpMethod.POST, requestEntity, String.class);
        assertThatJsonFileAndStatus(responseEntity, 400, "personalcareneeds_offender_validation.json");
    }

    @Test
    public void postPersonalCareNeedsForOffenders_missingProblemType() {
        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", List.of(), List.of("A1234AA", "A1234AB", "A1234AC"));
        final var responseEntity = testRestTemplate.exchange("/api/bookings/offenderNo/personal-care-needs", HttpMethod.POST, requestEntity, String.class);
        assertThatJsonFileAndStatus(responseEntity, 400, "personalcareneeds_validation.json");
    }

    @Test
    public void postPersonalCareNeedsForOffenders_emptyBody() {
        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of(), Map.of());
        final var responseEntity = testRestTemplate.exchange("/api/bookings/offenderNo/personal-care-needs?type=MATSTAT", HttpMethod.POST, requestEntity, String.class);

        assertThatStatus(responseEntity, 400);
        assertThat(responseEntity.getBody()).contains("Malformed request");
    }

    @Test
    public void offenderAlerts_respondsWithOKWhenOffenderNumberSupplied() {
        final var oneOffendersInRequest = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", List.of("ROLE_VIEW_PRISONER_DATA"), List.of("A1234AA"));

        final var minimumOfOneOffenderRequiredResponse = testRestTemplate.exchange("/api/bookings/offenderNo/alerts", HttpMethod.POST, oneOffendersInRequest, String.class);

        assertThatStatus(minimumOfOneOffenderRequiredResponse, 200);
    }

    @Test
    public void offenderAlerts_respondsWithBadRequestWhenNoOffendersNumbersSupplied() {
        final var noOffendersInRequest = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", List.of("ROLE_VIEW_PRISONER_DATA"), List.of());

        final var minimumOfOneOffenderRequiredResponse = testRestTemplate.exchange("/api/bookings/offenderNo/alerts", HttpMethod.POST, noOffendersInRequest, String.class);

        assertThatStatus(minimumOfOneOffenderRequiredResponse, 400);
        assertThat(minimumOfOneOffenderRequiredResponse.getBody()).contains("A minimum of one offender number is required");
    }

    @Test
    public void offenderAlerts_emptyBody() {
        final var noOffendersInRequest = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", List.of("ROLE_VIEW_PRISONER_DATA"), null);

        final var minimumOfOneOffenderRequiredResponse = testRestTemplate.exchange("/api/bookings/offenderNo/alerts", HttpMethod.POST, noOffendersInRequest, String.class);

        assertThatStatus(minimumOfOneOffenderRequiredResponse, 400);
        assertThat(minimumOfOneOffenderRequiredResponse.getBody()).contains("Malformed request");
    }

    @Test
    public void getReasonableAdjustment() {
        final var bookingId = -1;
        final var treatmentCodes = List.of("WHEELCHR_ACC", "PEEP");
        when(inmateRepository.findReasonableAdjustments(bookingId, treatmentCodes)).thenReturn(
                List.of(
                        new ReasonableAdjustment("WHEELCHR_ACC", "abcd", LocalDate.of(2010, 6, 21), null, "LEI", "Wheelchair accessibility"),
                        new ReasonableAdjustment("PEEP", "efgh", LocalDate.of(2010, 6, 21), null, "LEI", "Some other description"))
        );

        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of(), Map.of());

        final var responseEntity = testRestTemplate.exchange("/api/bookings/-1/reasonable-adjustments?type=WHEELCHR_ACC&type=PEEP", HttpMethod.GET, requestEntity, String.class);

        assertThatJsonFileAndStatus(responseEntity, 200, "reasonableadjustment.json");

        verify(inmateRepository).findReasonableAdjustments(bookingId, treatmentCodes);
    }

    private PersonalCareNeed createPersonalCareNeeds() {
        return PersonalCareNeed.builder().problemType("MATSTAT").problemCode("ACCU9").problemStatus("ON").problemDescription("Preg, acc under 9mths").startDate(LocalDate.of(2010, 6, 21)).build();
    }

    private List<PersonalCareNeed> createPersonalCareNeedsForOffenders() {
        return List.of(
                PersonalCareNeed.builder().problemType("MATSTAT").problemCode("ACCU9").problemStatus("ON")
                        .problemDescription("Preg, acc under 9mths").commentText("P1")
                        .startDate(LocalDate.parse("2010-06-21")).endDate(null).offenderNo("A1234AA").build(),
                PersonalCareNeed.builder().problemType("DISAB").problemCode("RM").problemStatus("ON")
                        .problemDescription("No Disability").commentText("description 1")
                        .startDate(LocalDate.parse("2010-06-21")).endDate(null).offenderNo("A1234AA").build(),
                PersonalCareNeed.builder().problemType("DISAB").problemCode("RC").problemStatus("ON")
                        .problemDescription("No Disability").commentText(null)
                        .startDate(LocalDate.parse("2010-06-22")).endDate(null).offenderNo("A1234AB").build(),
                PersonalCareNeed.builder().problemType("DISAB").problemCode("RC").problemStatus("ON")
                        .problemDescription("No Disability").commentText(null)
                        .startDate(LocalDate.parse("2010-06-22")).endDate(null).offenderNo("A1234AC").build(),
                PersonalCareNeed.builder().problemType("DISAB").problemCode("ND").problemStatus("ON")
                        .problemDescription("No Disability").commentText("description 2")
                        .startDate(LocalDate.parse("2010-06-24")).endDate(null).offenderNo("A1234AD").build());
    }

    @Test
    public void getReasonableAdjustment_missingTreatmentCodes() {
        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of(), Map.of());
        final var responseEntity = testRestTemplate.exchange("/api/bookings/-1/reasonable-adjustments", HttpMethod.GET, requestEntity, String.class);
        assertThatJsonFileAndStatus(responseEntity, 400, "reasonableadjustment_validation.json");
    }

    @Test
    public void getVisitBalances() {
        final var offenderNo = "A1234AA";

        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of(), Map.of());

        final var responseEntity = testRestTemplate.exchange("/api/bookings/offenderNo/" + offenderNo + "/visit/balances", HttpMethod.GET, requestEntity, String.class);

        assertThatJsonFileAndStatus(responseEntity, 200, "visitbalances.json");
    }

    @Test
    public void getVisitBalances_invalidBookingId() {

        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of(), Map.of());

        final var responseEntity = testRestTemplate.exchange("/api/bookings/offenderNo/-3/visit/balances", HttpMethod.GET, requestEntity, String.class);

        assertThatJsonFileAndStatus(responseEntity, 404, "visitbalancesinvalidbookingid.json");
    }

    @Test
    public void getOffenderBalances() {
        final var bookingId = -1;

        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of(), Map.of());

        final var responseEntity = testRestTemplate.exchange("/api/bookings/" + bookingId + "/balances", HttpMethod.GET, requestEntity, String.class);

        assertThatJsonFileAndStatus(responseEntity, 200, "offender_balance.json");
    }

    @Test
    public void getEvents() {
        when(bookingRepository.getBookingActivities(anyLong(), any(), any(), anyString(), any())).thenReturn(
                List.of(createEvent("act", "10:11:12"),
                        createEvent("act", "08:59:50"))
        );
        when(bookingRepository.getBookingVisits(anyLong(), any(), any(), anyString(), any())).thenReturn(
                List.of(createEvent("vis", "09:02:03"))
        );
        when(bookingRepository.getBookingAppointments(anyLong(), any(), any(), anyString(), any())).thenReturn(
                List.of(createEvent("app", null))
        );
        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of(), Map.of());
        final var responseEntity = testRestTemplate.exchange("/api/bookings/-1/events", HttpMethod.GET, requestEntity, String.class);

        assertThatJsonFileAndStatus(responseEntity, 200, "events.json");
    }

    @Test
    public void getMilitaryRecords() {
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
        final var responseEntity = testRestTemplate.exchange("/api/bookings/-1/military-records", HttpMethod.GET, requestEntity, String.class);
        assertThatJsonFileAndStatus(responseEntity, 200, "military_records.json");
    }

    @Test
    public void getCourtCases_returnsMatchingActiveCourtCase() {
        when(offenderBookingRepository.findById(-1L)).thenReturn(Optional.of(OffenderBooking.builder()
                .courtCases(List.of(OffenderCourtCase.builder()
                        .id(-1L)
                        .caseSeq(-1L)
                        .beginDate(LocalDate.EPOCH)
                        .agencyLocation(AgencyLocation.builder()
                                .id("MDI")
                                .active(true)
                                .type(AgencyLocationType.COURT_TYPE)
                                .description("Moorland")
                                .build())
                        .legalCaseType(new LegalCaseType("A", "Adult"))
                        .caseInfoPrefix("CIP")
                        .caseInfoNumber("CIN20177010")
                        .caseStatus(new CaseStatus("A", "Active"))
                        .courtEvents(List.of(CourtEvent.builder()
                                .id(-1L)
                                .eventDate(LocalDate.EPOCH)
                                .startTime(LocalDate.EPOCH.atStartOfDay())
                                .courtLocation(AgencyLocation.builder()
                                        .id("COURT1")
                                        .description("Court 1")
                                        .type(AgencyLocationType.COURT_TYPE)
                                        .courtType(new CourtType("MC", "Mag Court"))
                                        .active(true)
                                        .build())
                                .build()))
                        .build()))
                .build()));

        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of(), Map.of());
        final var responseEntity = testRestTemplate.exchange("/api/bookings/-1/court-cases", HttpMethod.GET, requestEntity, String.class);

        assertThatJsonFileAndStatus(responseEntity, 200, "court_cases.json");
    }

    @Test
    public void getCourtCases_returnsAllMatchingCourtCases() {
        when(offenderBookingRepository.findById(-1L)).thenReturn(Optional.of(OffenderBooking.builder()
                .courtCases(List.of(
                        OffenderCourtCase.builder()
                                .id(-1L)
                                .caseSeq(-1L)
                                .beginDate(LocalDate.EPOCH)
                                .agencyLocation(AgencyLocation.builder()
                                        .id("MDI")
                                        .active(true)
                                        .type(AgencyLocationType.COURT_TYPE)
                                        .description("Moorland")
                                        .build())
                                .legalCaseType(new LegalCaseType("A", "Adult"))
                                .caseInfoPrefix("CIP")
                                .caseInfoNumber("CIN20177010")
                                .caseStatus(new CaseStatus("A", "Active"))
                                .courtEvents(List.of(CourtEvent.builder()
                                        .id(-1L)
                                        .eventDate(LocalDate.EPOCH)
                                        .startTime(LocalDate.EPOCH.atStartOfDay())
                                        .courtLocation(AgencyLocation.builder()
                                                .id("COURT1")
                                                .description("Court 1")
                                                .type(AgencyLocationType.COURT_TYPE)
                                                .courtType(new CourtType("MC", "Mag Court"))
                                                .active(true)
                                                .build())
                                        .build()))
                                .build(),
                        OffenderCourtCase.builder()
                                .id(-2L)
                                .caseSeq(-2L)
                                .beginDate(LocalDate.EPOCH)
                                .agencyLocation(AgencyLocation.builder()
                                        .id("MDI")
                                        .active(true)
                                        .type(AgencyLocationType.COURT_TYPE)
                                        .description("Moorland")
                                        .build())
                                .legalCaseType(new LegalCaseType("A", "Adult"))
                                .caseInfoPrefix("CIP")
                                .caseInfoNumber("CIN20177010")
                                .caseStatus(new CaseStatus("I", "Inactive"))
                                .courtEvents(List.of(CourtEvent.builder()
                                        .id(-1L)
                                        .eventDate(LocalDate.EPOCH)
                                        .startTime(LocalDate.EPOCH.atStartOfDay())
                                        .courtLocation(AgencyLocation.builder()
                                                .id("COURT1")
                                                .description("Court 1")
                                                .type(AgencyLocationType.COURT_TYPE)
                                                .courtType(new CourtType("MC", "Mag Court"))
                                                .active(true)
                                                .build())
                                        .build()))
                                .build()))
                .build()));

        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of(), Map.of());
        final var responseEntity = testRestTemplate.exchange("/api/bookings/-1/court-cases?activeOnly=false", HttpMethod.GET, requestEntity, String.class);

        assertThatJsonFileAndStatus(responseEntity, 200, "court_cases_active_and_inactive.json");
    }

    @Test
    public void getPropertyContainers() {
        final var parentParentLocation = AgencyInternalLocation.builder().locationId(-1L).locationType("WING").agencyId("LEI")
            .currentOccupancy(null).operationalCapacity(13).description("LEI-A").userDescription("Block A").capacity(14)
            .certifiedFlag(true).locationCode("A").active(true).build();

        final var parentLocation = AgencyInternalLocation.builder().locationId(-2L).locationType("LAND").agencyId("LEI").capacity(14)
            .currentOccupancy(null).operationalCapacity(13).description("LEI-A-1").parentLocation(parentParentLocation).userDescription("Landing A/1")
            .certifiedFlag(true).locationCode("1").active(true).build();

        when(offenderBookingRepository.findById(-1L)).thenReturn(Optional.of(
            OffenderBooking.builder()
                .propertyContainers(List.of(
                    OffenderPropertyContainer.builder()
                        .containerId(-1L)
                        .active(true)
                        .internalLocation(AgencyInternalLocation.builder()
                                .locationId(-10L)
                                .active(true)
                                .locationType("CELL")
                                .agencyId("LEI")
                                .description("LEI-A-1-8")
                                .parentLocation(parentLocation)
                                .currentOccupancy(0)
                                .operationalCapacity(1)
                                .userDescription(null)
                                .locationCode("8")
                                .build())
                        .sealMark("TEST10")
                        .containerType(new PropertyContainer("BULK", "Bulk"))
                        .build()))
            .build()));

        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of(), Map.of());
        final var responseEntity = testRestTemplate.exchange("/api/bookings/-1/property", HttpMethod.GET, requestEntity, String.class);

        assertThatJsonFileAndStatus(responseEntity, 200, "offender_property_containers.json");
    }

    @Test
    public void getOffences() {
        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_SYSTEM_USER"), Map.of());
        final var responseEntity = testRestTemplate.exchange("/api/bookings/offenderNo/A1234AA/offenceHistory?convictionsOnly=false", HttpMethod.GET, requestEntity, String.class);

        assertThatJsonFileAndStatus(responseEntity, 200, "offender_offences.json");
    }

    @Test
    public void getOffencesNotAuthorised() {
        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of(), Map.of());
        final var responseEntity = testRestTemplate.exchange("/api/bookings/offenderNo/A1234AA/offenceHistory?convictionsOnly=false", HttpMethod.GET, requestEntity, String.class);

        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(403);

    }

    @Test
    public void getOffenderContacts() {
        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of(), Map.of());
        final var responseEntity = testRestTemplate.exchange("/api/bookings/-1/contacts", HttpMethod.GET, requestEntity, String.class);

        assertThatJsonFileAndStatus(responseEntity, 200, "offender_contacts.json");
    }

    private ScheduledEvent createEvent(final String type, final String time) {
        return ScheduledEvent.builder().bookingId(-1L)
                .startTime(Optional.ofNullable(time).map(t -> "2019-01-02T" + t).map(LocalDateTime::parse).orElse(null))
                .eventType(type + time)
                .eventSubType("some sub " + type)
                .build();
    }
}
