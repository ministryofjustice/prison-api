package uk.gov.justice.hmpps.prison.service;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.api.model.OffenderCell;
import uk.gov.justice.hmpps.prison.api.model.OffenderCellAttribute;
import uk.gov.justice.hmpps.prison.api.model.PrisonContactDetail;
import uk.gov.justice.hmpps.prison.api.model.Telephone;
import uk.gov.justice.hmpps.prison.repository.AgencyRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AddressType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyEstablishmentType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocationProfile;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocationEstablishment;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocationEstablishmentId;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocationType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.City;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Country;
import uk.gov.justice.hmpps.prison.repository.jpa.model.County;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.HousingAttributeReferenceCode;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AddressPhoneRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyAddressRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyInternalLocationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationFilter;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AvailablePrisonIepLevelRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.hmpps.prison.repository.support.StatusFilter.ALL;

@ExtendWith(MockitoExtension.class)
public class AgencyServiceTest {
    private AgencyService service;

    @Mock
    private AuthenticationFacade authenticationFacade;
    @Mock
    private AgencyRepository agencyRepo;
    @Mock
    private AvailablePrisonIepLevelRepository availablePrisonIepLevelRepository;
    @Mock
    private ReferenceDomainService referenceDomainService;
    @Mock
    private AgencyInternalLocationRepository agencyInternalLocationRepository;
    @Mock
    private AgencyLocationRepository agencyLocationRepository;
    @Mock
    private ReferenceCodeRepository<AgencyLocationType> agencyLocationTypeReferenceCodeRepository;
    @Mock
    private ReferenceCodeRepository<CourtType> courtTypeReferenceCodeRepository;
    @Mock
    private AddressPhoneRepository addressPhoneRepository;
    @Mock
    private AgencyAddressRepository agencyAddressRepository;
    @Mock
    private ReferenceCodeRepository<AddressType> addressTypeReferenceCodeRepository;
    @Mock
    private ReferenceCodeRepository<City> cityReferenceCodeRepository;
    @Mock
    private ReferenceCodeRepository<County> countyReferenceCodeRepository;
    @Mock
    private ReferenceCodeRepository<Country> countryReferenceCodeRepository;

    @BeforeEach
    public void setUp() {
        service = new AgencyService(authenticationFacade, agencyRepo, availablePrisonIepLevelRepository, agencyLocationRepository, referenceDomainService, agencyLocationTypeReferenceCodeRepository, courtTypeReferenceCodeRepository, agencyInternalLocationRepository,
        addressPhoneRepository, agencyAddressRepository, addressTypeReferenceCodeRepository, cityReferenceCodeRepository, countyReferenceCodeRepository, countryReferenceCodeRepository);
    }

    @Test
    public void shouldCallGetAgency() {
        when(agencyLocationRepository.findAll(isA(AgencyLocationFilter.class))).thenReturn(List.of(AgencyLocation.builder().id("LEI").build()));
        service.getAgency("LEI", ALL, null, false, false);
        verify(agencyLocationRepository).findAll(isA(AgencyLocationFilter.class));
    }

    @Test
    public void shouldReturnEntityNotFoundForSinglePrisonWithBlankAddress() {
        assertThatThrownBy(() -> service.getPrisonContactDetail("BLANK")).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void shouldReturnEntityNotFoundForEmptyResult() {
        assertThatThrownBy(() -> service.getPrisonContactDetail("NOADDRESS")).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void shouldIdentifyBlankAddress() {
        assertThat(service.removeBlankAddresses(buildPrisonContactDetailsList())).hasSize(2);
        assertThat(service.removeBlankAddresses(buildPrisonContactDetailsListSingleResult())).hasSize(1);
        assertThat(service.removeBlankAddresses(buildPrisonContactDetailsListSingleResultBlankAddress())).isEmpty();
    }

    @Test
    public void shouldCallRepositoryForAgencyLocationsByType() {
        when(agencyInternalLocationRepository.findAgencyInternalLocationsByAgencyIdAndLocationTypeAndActive("SOME AGENCY", "SOME TYPE", true))
                .thenReturn(List.of(AgencyInternalLocation.builder().locationId(1L).build()));

        service.getAgencyLocationsByType("SOME AGENCY", "SOME TYPE");

        verify(agencyInternalLocationRepository).findAgencyInternalLocationsByAgencyIdAndLocationTypeAndActive("SOME AGENCY", "SOME TYPE", true);
    }

    @Test
    public void shouldReturnLocationsForAgencyLocationsByType() {
        when(agencyInternalLocationRepository.findAgencyInternalLocationsByAgencyIdAndLocationTypeAndActive("ANY AGENCY", "ANY TYPE", true))
                .thenReturn(List.of(AgencyInternalLocation.builder().locationId(1L).build()));

        final var locations = service.getAgencyLocationsByType("ANY AGENCY", "ANY TYPE");

        assertThat(locations).extracting("locationId").containsExactly(1L);
    }

    @Test
    public void shouldThrowNotFoundIfNoAgencyLocationsByType() {
        when(agencyInternalLocationRepository.findAgencyInternalLocationsByAgencyIdAndLocationTypeAndActive("ANY AGENCY", "ANY TYPE", true))
                .thenReturn(emptyList());

        assertThatThrownBy(() -> service.getAgencyLocationsByType("ANY AGENCY", "ANY TYPE")).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void shouldReturnAllActiveCellsWithSpaceForAgency() {
        when(agencyInternalLocationRepository.findWithProfilesAgencyInternalLocationsByAgencyIdAndLocationTypeAndActive("LEI", "CELL", true)).thenReturn(List.of(
                AgencyInternalLocation.builder().locationId(-1L).locationType("CELL").operationalCapacity(2).currentOccupancy(1).active(true).profiles(buildAgencyInternalLocationProfiles()).build(),
                AgencyInternalLocation.builder().locationId(-2L).locationType("CELL").operationalCapacity(2).currentOccupancy(1).active(true).profiles(buildAgencyInternalLocationProfiles()).build(),
                AgencyInternalLocation.builder().locationId(-3L).locationType("CELL").operationalCapacity(2).currentOccupancy(2).active(true).profiles(buildAgencyInternalLocationProfiles()).build()
        ));

        final var offenderCells = service.getCellsWithCapacityInAgency("LEI", null);
        assertThat(offenderCells).extracting("id").containsExactly(-1L, -2L);
    }

    @Test
    public void shouldReturnAllActiveCellsWithSpaceForAgencyWithAttribute() {
        when(agencyInternalLocationRepository.findWithProfilesAgencyInternalLocationsByAgencyIdAndLocationTypeAndActive("LEI", "CELL", true)).thenReturn(List.of(
                AgencyInternalLocation.builder().locationId(-1L).locationType("CELL").operationalCapacity(2).currentOccupancy(1).active(true).profiles(buildAgencyInternalLocationProfiles()).build(),
                AgencyInternalLocation.builder().locationId(-2L).locationType("CELL").capacity(2).currentOccupancy(1).active(true).profiles(List.of()).build(),
                AgencyInternalLocation.builder().locationId(-3L).locationType("CELL").operationalCapacity(2).currentOccupancy(2).active(true).profiles(List.of()).build()
        ));

        final var offenderCells = service.getCellsWithCapacityInAgency("LEI", "DO");
        assertThat(offenderCells).extracting("id").containsExactly(-1L);
    }

    @Test
    public void shouldReturnAllActiveCellsWithIgnoringZeroOperationalCapacityForAgencyWithAttribute() {
        when(agencyInternalLocationRepository.findWithProfilesAgencyInternalLocationsByAgencyIdAndLocationTypeAndActive("LEI", "CELL", true)).thenReturn(List.of(
            AgencyInternalLocation.builder().locationId(-1L).locationType("CELL").operationalCapacity(0).capacity(3).currentOccupancy(2).active(true).profiles(buildAgencyInternalLocationProfiles()).build(),
            AgencyInternalLocation.builder().locationId(-2L).locationType("CELL").operationalCapacity(0).capacity(2).currentOccupancy(2).active(true).profiles(emptyList()).build()
        ));

        final var offenderCells = service.getCellsWithCapacityInAgency("LEI", "DO");
        assertThat(offenderCells).extracting("id").containsExactly(-1L);
    }

    @Test
    public void shouldReturnCellWithAttributes() {
        when(agencyInternalLocationRepository.findOneByLocationId(anyLong())).thenReturn(Optional.of(
                AgencyInternalLocation.builder()
                        .locationId(-1L)
                        .locationType("CELL")
                        .description("LEI-1-1-01")
                        .userDescription("LEI-1-1-01")
                        .operationalCapacity(2)
                        .currentOccupancy(1)
                        .active(true)
                        .profiles(buildAgencyInternalLocationProfiles())
                        .build()));

        final var offenderCell = service.getCellAttributes(-1L);
        assertThat(offenderCell).isEqualTo(
                OffenderCell.builder()
                        .attributes(List.of(
                                OffenderCellAttribute.builder()
                                        .code("DO")
                                        .description("Double Occupancy")
                                        .build(),
                                OffenderCellAttribute.builder()
                                        .code("LC")
                                        .description("Listener Cell")
                                        .build()))
                        .id(-1L)
                        .capacity(2)
                        .noOfOccupants(1)
                        .description("LEI-1-1-01")
                        .userDescription("LEI-1-1-01")
                        .build());
    }

    @Test
    public void shouldReturnCellWithAttributes_notFoundLivingUnit() {
        when(agencyInternalLocationRepository.findOneByLocationId(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getCellAttributes(-19999999L)).isInstanceOf(EntityNotFoundException.class).hasMessage("No cell details found for location id -19999999");
    }

    @Test
    public void shouldReturnAllEstablishmentTypesForMoorland() {
        final var agencyLocation = AgencyLocation.builder()
            .id("MDI")
            .description("Moorland")
            .type(AgencyLocationType.PRISON_TYPE)
            .build();
        final var establishmentType = new AgencyEstablishmentType("IM", "Closed Young Offender Institute (Male)");
        when(agencyLocationRepository.findById("MDI")).thenReturn(Optional.of(AgencyLocation.builder()
                .id("MDI")
                .establishmentTypes(List.of(new AgencyLocationEstablishment(new AgencyLocationEstablishmentId(agencyLocation.getId(), establishmentType.getCode()), agencyLocation, establishmentType)))
                .build()));

        final var establishmentTypes = service.getEstablishmentTypes("MDI");

        assertThat(establishmentTypes.getAgencyId()).isEqualTo("MDI");
        assertThat(establishmentTypes.getEstablishmentTypes()).extracting("code").containsExactly("IM");
        assertThat(establishmentTypes.getEstablishmentTypes()).extracting("description").containsExactly("Closed Young Offender Institute (Male)");
    }

    @Test
    public void shouldFailForEstablishmentTypesWhenAgencyNotFound() {
        when(agencyLocationRepository.findById(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getEstablishmentTypes("unknown")).isInstanceOf(EntityNotFoundException.class).hasMessage("Resource with id [unknown] not found.");
    }

    private List<PrisonContactDetail> buildPrisonContactDetailsList() {
        return ImmutableList.of(
                PrisonContactDetail.builder().agencyId("ABC")
                        .premise("ABC prison")
                        .city("Manchester")
                        .phones(ImmutableList.of(Telephone.builder().number("0114 2233444").type("BUS").build(), Telephone.builder().number("0114 6667775").type("BUS").build()))
                        .build(),
                PrisonContactDetail.builder().agencyId("DEF")
                        .premise("ABC prison")
                        .city("Manchester")
                        .phones(ImmutableList.of(Telephone.builder().number("0114 2233444").type("BUS").build()))
                        .build(),
                PrisonContactDetail.builder().agencyId("BLANK")
                        .phones(ImmutableList.of(Telephone.builder().number("0114 2233444").type("BUS").build()))
                        .build(),
                PrisonContactDetail.builder().agencyId("BLANK_WITH_COUNTRY")
                        .country("England")
                        .phones(ImmutableList.of(Telephone.builder().number("0114 2233444").type("BUS").build()))
                        .build()
        );

    }

    private List<PrisonContactDetail> buildPrisonContactDetailsListSingleResult() {
        return ImmutableList.of(
                PrisonContactDetail.builder().agencyId("ABC")
                        .premise("ABC prison")
                        .city("Manchester")
                        .phones(ImmutableList.of(Telephone.builder().number("0114 2233444").type("BUS").build(), Telephone.builder().number("0114 6667775").type("BUS").build()))
                        .build()
        );
    }

    private List<PrisonContactDetail> buildPrisonContactDetailsListSingleResultBlankAddress() {
        return ImmutableList.of(
                PrisonContactDetail.builder().agencyId("BLANK")
                        .country("England")
                        .phones(ImmutableList.of(Telephone.builder().number("0114 2233444").type("BUS").build(), Telephone.builder().number("0114 6667775").type("BUS").build()))
                        .build()
        );
    }

    private List<AgencyInternalLocationProfile> buildAgencyInternalLocationProfiles() {
        return ImmutableList.of(
                AgencyInternalLocationProfile.builder()
                        .locationId(-1L)
                        .profileType("HOU_UNIT_ATT")
                        .housingAttributeReferenceCode(new HousingAttributeReferenceCode("DO", "Double Occupancy"))
                        .build(),
                AgencyInternalLocationProfile.builder()
                        .locationId(-1L)
                        .profileType("HOU_UNIT_ATT")
                        .housingAttributeReferenceCode(new HousingAttributeReferenceCode("LC", "Listener Cell"))
                        .build(),
                AgencyInternalLocationProfile.builder()
                        .locationId(-1L)
                        .profileType("HOU_UNIT_ATT")
                        .housingAttributeReferenceCode(null)
                        .build());
    }

}
