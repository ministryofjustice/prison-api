package net.syscon.elite.service.impl;

import com.google.common.collect.ImmutableList;
import net.syscon.elite.api.model.PrisonContactDetails;
import net.syscon.elite.api.model.Telephone;
import net.syscon.elite.repository.AgencyRepository;
import net.syscon.elite.service.EntityNotFoundException;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AgencyServiceImplTest {
    private AgencyServiceImpl service;

    @Mock
    private AgencyRepository agencyRepo;

    @Before
    public void setUp() throws Exception {
        service = new AgencyServiceImpl(agencyRepo);
        when(agencyRepo.getPrisonContactDetails(eq(null))).thenReturn(buildPrisonContactDetailsList());
        when(agencyRepo.getPrisonContactDetails(eq("ABC"))).thenReturn(buildPrisonContactDetailsListSingleResult());
        when(agencyRepo.getPrisonContactDetails(eq("BLANK"))).thenReturn(buildPrisonContactDetailsListSingleResultBlankAddress());
        when(agencyRepo.getPrisonContactDetails(eq("NOADDRESS"))).thenReturn(ImmutableList.of());
    }

    @Test
    public void shouldCallCollaboratorsForFullPrisonList () throws Exception {
        service.getPrisonContactDetails();
        verify(agencyRepo, Mockito.times(1)).getPrisonContactDetails(null);
    }

    @Test
    public void shouldCallCollaboratorsForSinglePrison() throws Exception {
        service.getPrisonContactDetails("ABC");
        verify(agencyRepo, Mockito.times(1)).getPrisonContactDetails("ABC");
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldReturnEntityNotFoundForSinglePrisonWithBlankAddress() throws Exception {
        service.getPrisonContactDetails("BLANK");
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldReturnEntityNotFoundForEmptyResult() throws Exception {
        service.getPrisonContactDetails("NOADDRESS");
    }

    @Test()
    public void shouldIdentifyBlankAddress() throws Exception {
        Assertions.assertThat(service.removeBlankAddresses(buildPrisonContactDetailsList())).hasSize(2);
        Assertions.assertThat(service.removeBlankAddresses(buildPrisonContactDetailsListSingleResult())).hasSize(1);
        Assertions.assertThat(service.removeBlankAddresses(buildPrisonContactDetailsListSingleResultBlankAddress())).isEmpty();
    }


    private List<PrisonContactDetails> buildPrisonContactDetailsList() {
        return ImmutableList.of(
                PrisonContactDetails.builder().agencyId("ABC")
                        .premise("ABC prison")
                        .city("Manchester")
                        .phones(ImmutableList.of(Telephone.builder().number("0114 2233444").type("BUS").build(), Telephone.builder().number("0114 6667775").type("BUS").build()))
                        .build(),
                PrisonContactDetails.builder().agencyId("DEF")
                        .premise("ABC prison")
                        .city("Manchester")
                        .phones(ImmutableList.of(Telephone.builder().number("0114 2233444").type("BUS").build()))
                        .build(),
                PrisonContactDetails.builder().agencyId("BLANK")
                        .phones(ImmutableList.of(Telephone.builder().number("0114 2233444").type("BUS").build()))
                        .build(),
                PrisonContactDetails.builder().agencyId("BLANK_WITH_COUNTRY")
                        .country("England")
                        .phones(ImmutableList.of(Telephone.builder().number("0114 2233444").type("BUS").build()))
                        .build()
        );

    }

    private List<PrisonContactDetails> buildPrisonContactDetailsListSingleResult() {
        return ImmutableList.of(
                PrisonContactDetails.builder().agencyId("ABC")
                        .premise("ABC prison")
                        .city("Manchester")
                        .phones(ImmutableList.of(Telephone.builder().number("0114 2233444").type("BUS").build(), Telephone.builder().number("0114 6667775").type("BUS").build()))
                        .build()
        );
    }

    private List<PrisonContactDetails> buildPrisonContactDetailsListSingleResultBlankAddress() {
        return ImmutableList.of(
                PrisonContactDetails.builder().agencyId("BLANK")
                        .country("England")
                        .phones(ImmutableList.of(Telephone.builder().number("0114 2233444").type("BUS").build(), Telephone.builder().number("0114 6667775").type("BUS").build()))
                        .build()
        );
    }

}