package net.syscon.elite.service;

import com.google.common.collect.ImmutableList;
import net.syscon.elite.api.model.PrisonContactDetail;
import net.syscon.elite.api.model.Telephone;
import net.syscon.elite.repository.AgencyRepository;
import net.syscon.elite.repository.jpa.model.ActiveFlag;
import net.syscon.elite.repository.jpa.model.AgencyInternalLocation;
import net.syscon.elite.repository.jpa.model.AgencyLocation;
import net.syscon.elite.repository.jpa.model.HousingAttributeReferenceCode;
import net.syscon.elite.repository.jpa.model.HousingUnitTypeReferenceCode;
import net.syscon.elite.repository.jpa.model.LivingUnit;
import net.syscon.elite.repository.jpa.model.LivingUnitProfile;
import net.syscon.elite.repository.jpa.repository.AgencyInternalLocationRepository;
import net.syscon.elite.repository.jpa.repository.AgencyLocationFilter;
import net.syscon.elite.repository.jpa.repository.AgencyLocationRepository;
import net.syscon.elite.repository.jpa.repository.LivingUnitProfileRepository;
import net.syscon.elite.repository.jpa.repository.LivingUnitRepository;
import net.syscon.elite.security.AuthenticationFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static java.util.Collections.emptyList;
import static net.syscon.elite.repository.support.StatusFilter.ALL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private LivingUnitRepository livingUnitRepository;
    @Mock
    private LivingUnitProfileRepository livingUnitProfileRepository;

    @BeforeEach
    public void setUp() {
        service = new AgencyServiceImpl(authenticationFacade, agencyRepo, agencyLocationRepository, referenceDomainService, agencyInternalLocationRepository, livingUnitRepository, livingUnitProfileRepository);
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
        when(livingUnitRepository.findAllByAgencyLocationId(anyString())).thenReturn(buildLivingUnits());
        when(livingUnitProfileRepository.findAllByLivingUnitIdAndAgencyLocationIdAndDescription(anyLong(), anyString(), anyString())).thenReturn(buildLivingUnitProfiles());

        final var offenderCells = service.getCellsWithCapacityInAgency("LEI", null);
        assertThat(offenderCells).extracting("id").containsExactly(-1L, -2L);
    }

    @Test
    public void shouldReturnAllActiveCellsWithSpaceForAgencyWithAttribute() {
        when(livingUnitRepository.findAllByAgencyLocationId(anyString())).thenReturn(buildLivingUnits());
        when(livingUnitProfileRepository.findAllByLivingUnitIdAndAgencyLocationIdAndDescription(anyLong(), anyString(), anyString())).thenReturn(buildLivingUnitProfiles());

        final var offenderCells = service.getCellsWithCapacityInAgency("LEI", "DO");
        assertThat(offenderCells).extracting("id").containsExactly(-1L);
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

    private List<LivingUnit> buildLivingUnits() {
        return ImmutableList.of(
                LivingUnit.builder()
                        .livingUnitId(-1L)
                        .agencyLocationId("LEI")
                        .description("LEI-1-1-01")
                        .livingUnitType("CELL")
                        .livingUnitCode("01")
                        .level1Code("1")
                        .level2Code("1")
                        .level3Code("01")
                        .level4Code(null)
                        .userDescription("LEI-1-1-01")
                        .housingUnitTypeReferenceCode(new HousingUnitTypeReferenceCode("NA", "Normal Accommodation"))
                        .activeFlag("Y")
                        .capacity(3)
                        .operationalCapacity(2)
                        .noOfOccupants(1)
                        .certifiedFlag("Y")
                        .deactivateDate(null)
                        .reactivateDate(null)
                        .deactiveReasonReferenceCode(null)
                        .comment("Just a cell")
                        .build(),
                LivingUnit.builder()
                        .livingUnitId(-2L)
                        .agencyLocationId("LEI")
                        .description("LEI-1-1-02")
                        .livingUnitType("CELL")
                        .livingUnitCode("02")
                        .level1Code("1")
                        .level2Code("1")
                        .level3Code("02")
                        .level4Code(null)
                        .userDescription("LEI-1-1-02")
                        .housingUnitTypeReferenceCode(new HousingUnitTypeReferenceCode("NA", "Normal Accommodation"))
                        .activeFlag("Y")
                        .capacity(3)
                        .operationalCapacity(2)
                        .noOfOccupants(1)
                        .certifiedFlag("Y")
                        .deactivateDate(null)
                        .reactivateDate(null)
                        .deactiveReasonReferenceCode(null)
                        .comment("Just a cell")
                        .build(),
                LivingUnit.builder()
                        .livingUnitId(-3L)
                        .agencyLocationId("LEI")
                        .description("LEI-1-1-03")
                        .livingUnitType("CELL")
                        .livingUnitCode("03")
                        .level1Code("1")
                        .level2Code("1")
                        .level3Code("03")
                        .level4Code(null)
                        .userDescription("LEI-1-1-03")
                        .housingUnitTypeReferenceCode(new HousingUnitTypeReferenceCode("SPLC", "Specialist Cell"))
                        .activeFlag("Y")
                        .capacity(3)
                        .operationalCapacity(2)
                        .noOfOccupants(2)
                        .certifiedFlag("Y")
                        .deactivateDate(null)
                        .reactivateDate(null)
                        .deactiveReasonReferenceCode(null)
                        .comment("Full cell")
                        .build(),
                LivingUnit.builder()
                        .livingUnitId(-4L)
                        .agencyLocationId("LEI")
                        .description("LEI-1-1-04")
                        .livingUnitType("CELL")
                        .livingUnitCode("04")
                        .level1Code("1")
                        .level2Code("1")
                        .level3Code("04")
                        .level4Code(null)
                        .userDescription("LEI-1-1-04")
                        .housingUnitTypeReferenceCode(new HousingUnitTypeReferenceCode("SPLC", "Specialist Cell"))
                        .activeFlag("N")
                        .capacity(3)
                        .operationalCapacity(2)
                        .noOfOccupants(1)
                        .certifiedFlag("Y")
                        .deactivateDate(null)
                        .reactivateDate(null)
                        .deactiveReasonReferenceCode(null)
                        .comment("Full cell")
                        .build());
    }

    private List<LivingUnitProfile> buildLivingUnitProfiles() {
        return ImmutableList.of(
                LivingUnitProfile.builder()
                        .livingUnitId(-1L)
                        .agencyLocationId("LEI")
                        .description("LEI-1-1-01")
                        .profileId(-1L)
                        .housingAttributeReferenceCode(new HousingAttributeReferenceCode("DO", "Double Occupancy"))
                        .build(),
                LivingUnitProfile.builder()
                        .livingUnitId(-1L)
                        .agencyLocationId("LEI")
                        .description("LEI-1-1-01")
                        .profileId(-2L)
                        .housingAttributeReferenceCode(new HousingAttributeReferenceCode("LC", "Listener Cell"))
                        .build());
    }

}
