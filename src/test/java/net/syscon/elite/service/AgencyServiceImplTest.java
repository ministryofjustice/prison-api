package net.syscon.elite.service;

import com.google.common.collect.ImmutableList;
import net.syscon.elite.api.model.PrisonContactDetail;
import net.syscon.elite.api.model.Telephone;
import net.syscon.elite.repository.AgencyRepository;
import net.syscon.elite.repository.jpa.model.ActiveFlag;
import net.syscon.elite.repository.jpa.model.AgencyInternalLocation;
import net.syscon.elite.repository.jpa.model.AgencyLocation;
import net.syscon.elite.repository.jpa.repository.AgencyInternalLocationRepository;
import net.syscon.elite.repository.jpa.repository.AgencyLocationFilter;
import net.syscon.elite.repository.jpa.repository.AgencyLocationRepository;
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

    @BeforeEach
    public void setUp() {
        service = new AgencyServiceImpl(authenticationFacade, agencyRepo, agencyLocationRepository, referenceDomainService, agencyInternalLocationRepository);
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
}
