package net.syscon.elite.repository.impl;

import com.google.common.collect.ImmutableList;
import net.syscon.elite.api.model.PrisonContactDetail;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.List;

public class AgencyRepositoryImplTest {

    private AgencyRepositoryImpl repo = new AgencyRepositoryImpl();

    @Test
    public void shouldMapTupleResultsToPrisonContactDetailsList() throws Exception {
        final List<PrisonContactDetail> prisonContactDetails = repo.mapResultsToPrisonContactDetailsList(getTestData());
        Assertions.assertThat(prisonContactDetails).hasSize(2);
    }

    @Test
    public void shouldHandleEmptyResultSetForSinglePrison() throws Exception {
        final List<PrisonContactDetail> prisonContactDetails = repo.mapResultsToPrisonContactDetailsList(ImmutableList.of());
        Assertions.assertThat(prisonContactDetails).isEmpty();
    }


    @Test
    public void shouldReturnResultSetWithBlankAddressForSinglePrison() throws Exception {
        final List<PrisonContactDetail> prisonContactDetails = repo.mapResultsToPrisonContactDetailsList(getResultForPrisonWithBlankAddress());
        Assertions.assertThat(prisonContactDetails).hasSize(1);
    }

    /* outer join to phones results in repeated address rows for each phone record */
    private ImmutableList<List<Address>> getTestData() {
        return ImmutableList.of(ImmutableList.of(Address.builder().agencyId("123").city("Sheffield").phoneNo("123456").phoneType("BUS").extNo("123").build(),
                Address.builder().agencyId("123").city("Sheffield").phoneNo("444456").phoneType("BUS2").extNo("1234").build(),
                Address.builder().agencyId("123").city("Sheffield").phoneNo("444999").phoneType("BUS2").extNo("1234").build()),

                ImmutableList.of(Address.builder().agencyId("321").city("Sheffield").phoneNo("123456").phoneType("BUS").extNo("123").build())
        );
    }

    private ImmutableList<List<Address>> getResultForPrisonWithBlankAddress() {
        return ImmutableList.of(ImmutableList.of(Address.builder().agencyId("123").phoneNo("123456").phoneType("BUS").extNo("123").build()));
    }
}