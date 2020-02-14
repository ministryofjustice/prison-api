package net.syscon.elite.service;

import net.syscon.elite.api.model.OffenderAddress;
import net.syscon.elite.repository.OffenderAddressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class OffenderAddressServiceImplTest {

    @Mock
    private OffenderAddressRepository offenderAddressRepository;

    private OffenderAddressService offenderAddressService;

    @BeforeEach
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
