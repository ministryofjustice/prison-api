package uk.gov.justice.hmpps.prison.service;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder;
import uk.gov.justice.hmpps.prison.api.model.OffenderCell;
import uk.gov.justice.hmpps.prison.api.model.OffenderCellAttribute;
import uk.gov.justice.hmpps.prison.api.model.PrisonContactDetail;
import uk.gov.justice.hmpps.prison.api.model.Telephone;
import uk.gov.justice.hmpps.prison.repository.AgencyRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocationProfile;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocationType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.HousingAttributeReferenceCode;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyInternalLocationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationFilter;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.hmpps.prison.repository.support.StatusFilter.ALL;

@ExtendWith(MockitoExtension.class)
public class AgencyServiceTest {
    private AgencyService service;

    @Mock
    private HmppsAuthenticationHolder hmppsAuthenticationHolder;
    @Mock
    private AgencyRepository agencyRepo;
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

    @BeforeEach
    public void setUp() {
        service = new AgencyService(hmppsAuthenticationHolder, agencyRepo, agencyLocationRepository, referenceDomainService, agencyLocationTypeReferenceCodeRepository, courtTypeReferenceCodeRepository, agencyInternalLocationRepository);
    }

    @Test
    public void shouldCallGetAgency() {
        when(agencyLocationRepository.findAll(isA(AgencyLocationFilter.class))).thenReturn(List.of(AgencyLocation.builder().id("LEI").build()));
        service.getAgency("LEI", ALL, null, false, false, false);
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
    public void shouldReturnAllActiveReceptionsWithSpaceForAgency() {
        when(agencyInternalLocationRepository.findWithProfilesAgencyInternalLocationsByAgencyIdAndLocationCodeAndActive("LEI", "RECP", true)).thenReturn(List.of(
            AgencyInternalLocation.builder().locationId(-1L).description("LEI-RECP").locationCode("RECP").operationalCapacity(2).currentOccupancy(1).active(true).profiles(buildAgencyInternalLocationProfiles()).build(),
            AgencyInternalLocation.builder().locationId(-2L).description("LEI-RECEP").locationCode("AREA").operationalCapacity(2).currentOccupancy(1).active(true).profiles(buildAgencyInternalLocationProfiles()).build()
        ));

        final var receptions = service.getReceptionsWithCapacityInAgency("LEI", null);
        assertThat(receptions).extracting("id").containsExactly(-1L);
    }

    @Test
    public void shouldReturnAllActiveReceptionsWithSpaceForAgencyWithAttribute() {
        when(agencyInternalLocationRepository.findWithProfilesAgencyInternalLocationsByAgencyIdAndLocationCodeAndActive("LEI", "RECP", true)).thenReturn(List.of(
            AgencyInternalLocation.builder().locationId(-1L).description("LEI-RECP").locationCode("RECP").operationalCapacity(2).currentOccupancy(1).active(true).profiles(buildAgencyInternalLocationProfiles()).build(),
            AgencyInternalLocation.builder().locationId(-2L).description("LEI-RECP").locationCode("RECP").capacity(2).currentOccupancy(1).active(true).profiles(List.of()).build(),
            AgencyInternalLocation.builder().locationId(-3L).description("LEI-RECP").locationCode("RECP").operationalCapacity(2).currentOccupancy(2).active(true).profiles(List.of()).build()
        ));

        final var offenderCells = service.getReceptionsWithCapacityInAgency("LEI", "DO");
        assertThat(offenderCells).extracting("id").containsExactly(-1L);
    }

    @Test
    public void shouldReturnAllActiveReceptionsWithIgnoringZeroOperationalCapacityForAgencyWithAttribute() {
        when(agencyInternalLocationRepository.findWithProfilesAgencyInternalLocationsByAgencyIdAndLocationCodeAndActive("LEI", "RECP", true)).thenReturn(List.of(
            AgencyInternalLocation.builder().locationId(-1L).description("LEI-RECP").locationCode("RECP").operationalCapacity(0).capacity(3).currentOccupancy(2).active(true).profiles(buildAgencyInternalLocationProfiles()).build(),
            AgencyInternalLocation.builder().locationId(-2L).description("LEI-RECP").locationCode("RECP").operationalCapacity(0).capacity(2).currentOccupancy(2).active(true).profiles(emptyList()).build()
        ));

        final var offenderCells = service.getReceptionsWithCapacityInAgency("LEI", "DO");
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
