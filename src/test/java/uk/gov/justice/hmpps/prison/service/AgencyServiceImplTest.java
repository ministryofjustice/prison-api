package uk.gov.justice.hmpps.prison.service;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.api.model.AgencyEstablishmentType;
import uk.gov.justice.hmpps.prison.api.model.AgencyEstablishmentTypes;
import uk.gov.justice.hmpps.prison.api.model.OffenderCell;
import uk.gov.justice.hmpps.prison.api.model.OffenderCellAttribute;
import uk.gov.justice.hmpps.prison.api.model.PrisonContactDetail;
import uk.gov.justice.hmpps.prison.api.model.ReferenceCode;
import uk.gov.justice.hmpps.prison.api.model.Telephone;
import uk.gov.justice.hmpps.prison.repository.AgencyRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ActiveFlag;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocationProfile;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocationEstablishment;
import uk.gov.justice.hmpps.prison.repository.jpa.model.HousingAttributeReferenceCode;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyInternalLocationProfileRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyInternalLocationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationFilter;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository;
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
public class AgencyServiceImplTest {
    private AgencyServiceImpl service;

    @Mock
    private AuthenticationFacade authenticationFacade;
    @Mock
    private AgencyRepository agencyRepo;
    @Mock
    private ReferenceDomainService referenceDomainService;
    @Mock
    private AgencyInternalLocationRepository agencyInternalLocationRepository;
    @Mock
    private AgencyLocationRepository agencyLocationRepository;
    @Mock
    private AgencyInternalLocationProfileRepository agencyInternalLocationProfileRepository;

    @BeforeEach
    public void setUp() {
        service = new AgencyServiceImpl(authenticationFacade, agencyRepo, agencyLocationRepository, referenceDomainService, agencyInternalLocationRepository, agencyInternalLocationProfileRepository);
    }

    @Test
    public void shouldCallGetAgency() {
        when(agencyLocationRepository.findAll(isA(AgencyLocationFilter.class))).thenReturn(List.of(AgencyLocation.builder().id("LEI").build()));
        service.getAgency("LEI", ALL, null);
        verify(agencyLocationRepository).findAll(isA(AgencyLocationFilter.class));
    }

    @Test
    public void shouldCallCollaboratorsForFullPrisonList() {
        service.getPrisonContactDetail();
        verify(agencyRepo).getPrisonContactDetails(null);
    }

    @Test
    public void shouldCallCollaboratorsForSinglePrison() {
        when(agencyRepo.getPrisonContactDetails("ABC")).thenReturn(buildPrisonContactDetailsListSingleResult());
        service.getPrisonContactDetail("ABC");
        verify(agencyRepo).getPrisonContactDetails("ABC");
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
        when(agencyInternalLocationRepository.findAgencyInternalLocationsByAgencyIdAndLocationTypeAndActiveFlag("SOME AGENCY", "SOME TYPE", ActiveFlag.Y))
                .thenReturn(List.of(AgencyInternalLocation.builder().locationId(1L).build()));

        service.getAgencyLocationsByType("SOME AGENCY", "SOME TYPE");

        verify(agencyInternalLocationRepository).findAgencyInternalLocationsByAgencyIdAndLocationTypeAndActiveFlag("SOME AGENCY", "SOME TYPE", ActiveFlag.Y);
    }

    @Test
    public void shouldReturnLocationsForAgencyLocationsByType() {
        when(agencyInternalLocationRepository.findAgencyInternalLocationsByAgencyIdAndLocationTypeAndActiveFlag("ANY AGENCY", "ANY TYPE", ActiveFlag.Y))
                .thenReturn(List.of(AgencyInternalLocation.builder().locationId(1L).build()));

        final var locations = service.getAgencyLocationsByType("ANY AGENCY", "ANY TYPE");

        assertThat(locations).extracting("locationId").containsExactly(1L);
    }

    @Test
    public void shouldThrowNotFoundIfNoAgencyLocationsByType() {
        when(agencyInternalLocationRepository.findAgencyInternalLocationsByAgencyIdAndLocationTypeAndActiveFlag("ANY AGENCY", "ANY TYPE", ActiveFlag.Y))
                .thenReturn(emptyList());

        assertThatThrownBy(() -> service.getAgencyLocationsByType("ANY AGENCY", "ANY TYPE")).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void shouldReturnAllActiveCellsWithSpaceForAgency() {
        when(agencyInternalLocationRepository.findAgencyInternalLocationsByAgencyIdAndLocationTypeAndActiveFlag("LEI", "CELL", ActiveFlag.Y)).thenReturn(List.of(
                AgencyInternalLocation.builder().locationId(-1L).locationType("CELL").operationalCapacity(2).currentOccupancy(1).activeFlag(ActiveFlag.Y).build(),
                AgencyInternalLocation.builder().locationId(-2L).locationType("CELL").operationalCapacity(2).currentOccupancy(1).activeFlag(ActiveFlag.Y).build(),
                AgencyInternalLocation.builder().locationId(-3L).locationType("CELL").operationalCapacity(2).currentOccupancy(2).activeFlag(ActiveFlag.Y).build()
        ));
        when(agencyInternalLocationProfileRepository.findAllByLocationId(anyLong())).thenReturn(buildAgencyInternalLocationProfiles());

        final var offenderCells = service.getCellsWithCapacityInAgency("LEI", null);
        assertThat(offenderCells).extracting("id").containsExactly(-1L, -2L);
    }

    @Test
    public void shouldReturnAllActiveCellsWithSpaceForAgencyWithAttribute() {
        when(agencyInternalLocationRepository.findAgencyInternalLocationsByAgencyIdAndLocationTypeAndActiveFlag("LEI", "CELL", ActiveFlag.Y)).thenReturn(List.of(
                AgencyInternalLocation.builder().locationId(-1L).locationType("CELL").operationalCapacity(2).currentOccupancy(1).activeFlag(ActiveFlag.Y).build(),
                AgencyInternalLocation.builder().locationId(-2L).locationType("CELL").capacity(2).currentOccupancy(1).activeFlag(ActiveFlag.Y).build(),
                AgencyInternalLocation.builder().locationId(-3L).locationType("CELL").operationalCapacity(2).currentOccupancy(2).activeFlag(ActiveFlag.Y).build()
        ));

        when(agencyInternalLocationProfileRepository.findAllByLocationId(-1L)).thenReturn(buildAgencyInternalLocationProfiles());
        when(agencyInternalLocationProfileRepository.findAllByLocationId(-2L)).thenReturn(List.of());

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
                        .activeFlag(ActiveFlag.Y)
                        .build()));
        when(agencyInternalLocationProfileRepository.findAllByLocationId(anyLong())).thenReturn(buildAgencyInternalLocationProfiles());

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
        when(agencyLocationRepository.findById("MDI")).thenReturn(Optional.of(AgencyLocation.builder()
                .id("MDI")
                .establishmentTypes(List.of(AgencyLocationEstablishment
                        .builder()
                        .agencyLocId("MDI")
                        .establishmentType("IM")
                        .build()))
                .build()));

        when(referenceDomainService.getReferenceCodeByDomainAndCode("ESTAB_TYPE", "IM", false)).thenReturn(Optional.of(ReferenceCode.builder().code("IM").description("Closed Young Offender Institute (Male)").build()));

        final var establishmentTypes = service.getEstablishmentTypes("MDI");

        assertThat(establishmentTypes).isEqualTo(AgencyEstablishmentTypes.builder().agencyId("MDI").establishmentTypes(List.of(AgencyEstablishmentType.builder().code("IM").description("Closed Young Offender Institute (Male)").build())).build());
    }

    @Test
    public void shouldFailForEstablishmentTypesWhenAgencyNotFound() {
        when(agencyLocationRepository.findById(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getEstablishmentTypes("unknown")).isInstanceOf(EntityNotFoundException.class).hasMessage("Resource with id [unknown] not found.");
    }

    @Test
    public void shouldFailForEstablishmentTypesWhenAgencyEstablishmentNotFound() {
        when(agencyLocationRepository.findById("MDI")).thenReturn(Optional.of(AgencyLocation.builder()
                .id("MDI")
                .establishmentTypes(List.of(AgencyLocationEstablishment
                        .builder()
                        .agencyLocId("MDI")
                        .establishmentType("IM")
                        .build()))
                .build()));

        when(referenceDomainService.getReferenceCodeByDomainAndCode("ESTAB_TYPE", "IM", false)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getEstablishmentTypes("MDI")).isInstanceOf(EntityNotFoundException.class).hasMessage("Establishment type IM for agency MDI not found.");
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
