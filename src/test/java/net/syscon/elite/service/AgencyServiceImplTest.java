package net.syscon.elite.service;

import com.google.common.collect.ImmutableList;
import net.syscon.elite.api.model.Agency;
import net.syscon.elite.api.model.Location;
import net.syscon.elite.api.model.PrisonContactDetail;
import net.syscon.elite.api.model.Telephone;
import net.syscon.elite.repository.AgencyRepository;
import net.syscon.elite.repository.jpa.repository.AgencyRepositoryJpa;
import net.syscon.elite.security.AuthenticationFacade;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static net.syscon.elite.repository.support.StatusFilter.ALL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AgencyServiceImplTest {
    private AgencyServiceImpl service;

    @Mock
    private AuthenticationFacade authenticationFacade;
    @Mock
    private AgencyRepository agencyRepo;
    @Mock
    private ReferenceDomainService referenceDomainService;
    @Mock
    private AgencyRepositoryJpa agencyRepositoryJpa;

    @Before
    public void setUp() {
        service = new AgencyServiceImpl(authenticationFacade, agencyRepo, referenceDomainService, agencyRepositoryJpa);
        when(agencyRepo.getPrisonContactDetails(eq(null))).thenReturn(buildPrisonContactDetailsList());
        when(agencyRepo.getPrisonContactDetails(eq("ABC"))).thenReturn(buildPrisonContactDetailsListSingleResult());
        when(agencyRepo.getPrisonContactDetails(eq("BLANK"))).thenReturn(buildPrisonContactDetailsListSingleResultBlankAddress());
        when(agencyRepo.getPrisonContactDetails(eq("NOADDRESS"))).thenReturn(ImmutableList.of());
    }

    @Test
    public void shouldCallGetAgency() {
        when(agencyRepo.findAgency(Mockito.anyString(), any(), isNull())).thenReturn(Optional.of(Agency.builder().build()));
        service.getAgency("LEI", ALL, null);
        verify(agencyRepo).findAgency("LEI", ALL, null);
    }

    @Test
    public void shouldCallCollaboratorsForFullPrisonList() throws Exception {
        service.getPrisonContactDetail();
        verify(agencyRepo, Mockito.times(1)).getPrisonContactDetails(null);
    }

    @Test
    public void shouldCallCollaboratorsForSinglePrison() throws Exception {
        service.getPrisonContactDetail("ABC");
        verify(agencyRepo, Mockito.times(1)).getPrisonContactDetails("ABC");
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldReturnEntityNotFoundForSinglePrisonWithBlankAddress() throws Exception {
        service.getPrisonContactDetail("BLANK");
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldReturnEntityNotFoundForEmptyResult() throws Exception {
        service.getPrisonContactDetail("NOADDRESS");
    }

    @Test()
    public void shouldIdentifyBlankAddress() throws Exception {
        assertThat(service.removeBlankAddresses(buildPrisonContactDetailsList())).hasSize(2);
        assertThat(service.removeBlankAddresses(buildPrisonContactDetailsListSingleResult())).hasSize(1);
        assertThat(service.removeBlankAddresses(buildPrisonContactDetailsListSingleResultBlankAddress())).isEmpty();
    }

    @Test
    public void shouldCallRepositoryForAgencyLocationsByType() {
        when(agencyRepositoryJpa.getAgencyLocationsByType("SOME AGENCY", "SOME TYPE"))
                .thenReturn(List.of(Location.builder().locationId(1L).build()));

        service.getAgencyLocationsByType("SOME AGENCY", "SOME TYPE");

        verify(agencyRepositoryJpa).getAgencyLocationsByType("SOME AGENCY", "SOME TYPE");
    }

    @Test
    public void shouldReturnLocationsForAgencyLocationsByType() {
        when(agencyRepositoryJpa.getAgencyLocationsByType("ANY AGENCY", "ANY TYPE"))
                .thenReturn(List.of(Location.builder().locationId(1L).build()));

        var locations = service.getAgencyLocationsByType("ANY AGENCY", "ANY TYPE");

        assertThat(locations).extracting("locationId").containsExactly(1L);
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldThrowNotFoundIfNoAgencyLocationsByType() {
        when(agencyRepositoryJpa.getAgencyLocationsByType("ANY AGENCY", "ANY TYPE"))
                .thenReturn(emptyList());

        service.getAgencyLocationsByType("ANY AGENCY", "ANY TYPE");
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
