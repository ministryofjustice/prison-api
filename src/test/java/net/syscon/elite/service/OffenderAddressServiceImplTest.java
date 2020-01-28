package net.syscon.elite.service;

import net.syscon.elite.api.model.OffenderAddress;
import net.syscon.elite.repository.OffenderAddressRepository;
import net.syscon.elite.service.BookingService;
import net.syscon.elite.service.OffenderAddressService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class OffenderAddressServiceImplTest {

    @Mock
    private OffenderAddressRepository offenderAddressRepository;

    private OffenderAddressService offenderAddressService;

    @Before
    public void setUp() {
        offenderAddressService = new OffenderAddressService(offenderAddressRepository);
    }


    @Test
    public void canRetrieveAddresses() {

        String offenderNo = "off-1";
        OffenderAddress address = OffenderAddress.builder().primary(true).noFixedAddress(true).build();

        when(offenderAddressRepository.getAddresses(offenderNo)).thenReturn(List.of(address));

        List<OffenderAddress> results = offenderAddressService.getAddressesByOffenderNo(offenderNo);

        assertThat(results).containsExactly(address);
    }
}
