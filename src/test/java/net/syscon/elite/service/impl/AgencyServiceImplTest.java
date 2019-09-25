package net.syscon.elite.service.impl;

import com.google.common.collect.ImmutableList;
import net.syscon.elite.api.model.Agency;
import net.syscon.elite.api.model.PrisonContactDetail;
import net.syscon.elite.api.model.Telephone;
import net.syscon.elite.repository.AgencyRepository;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.ReferenceDomainService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Optional;

import static net.syscon.elite.repository.support.StatusFilter.ALL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

    @Before
    public void setUp() {
        service = new AgencyServiceImpl(authenticationFacade, agencyRepo, referenceDomainService);
        when(agencyRepo.getPrisonContactDetails(eq(null))).thenReturn(buildPrisonContactDetailsList());
        when(agencyRepo.getPrisonContactDetails(eq("ABC"))).thenReturn(buildPrisonContactDetailsListSingleResult());
        when(agencyRepo.getPrisonContactDetails(eq("BLANK"))).thenReturn(buildPrisonContactDetailsListSingleResultBlankAddress());
        when(agencyRepo.getPrisonContactDetails(eq("NOADDRESS"))).thenReturn(ImmutableList.of());
    }

    @Test
    public void shouldCallGetAgency() {
        when(agencyRepo.findAgency(Mockito.anyString(), any())).thenReturn(Optional.of(Agency.builder().build()));
        service.getAgency("LEI", ALL);
        verify(agencyRepo).findAgency("LEI", ALL);
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
