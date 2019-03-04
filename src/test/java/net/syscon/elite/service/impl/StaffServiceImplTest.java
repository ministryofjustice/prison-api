package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.StaffDetail;
import net.syscon.elite.repository.StaffRepository;
import net.syscon.elite.repository.UserRepository;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.StaffService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.ws.rs.BadRequestException;
import java.util.ArrayList;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StaffServiceImplTest {

    private static final Long ID_MULTIPLE = 8888L;
    private static final Long ID_SINGLE = 9999L;
    private static final Long ID_NONE = 9999L;
    private static final Long ID_BAD = 123L;
    private List<String> multipleAddresses = Arrays.asList(new String[] {"test1@a.com", "test2@b.com", "test3@c.com"});
    private List<String> singleAddress = Arrays.asList(new String[] {"test1@a.com"});
    private List<String> emptyList = new ArrayList<>();

    @Mock
    public StaffRepository staffRepository;

    @Mock
    public UserRepository userRepository;

    private StaffService staffService;

    @Before
    public void init() {
        staffService = new StaffServiceImpl(staffRepository, userRepository);
    }

    @Test
    public void testMultipleEmails() {
        when(staffRepository.findByStaffId(ID_MULTIPLE)).thenReturn(getValidStaffDetails(ID_MULTIPLE));
        when(staffRepository.findEmailAddressesForStaffId(ID_MULTIPLE)).thenReturn(multipleAddresses);
        List<String> addresses = staffService.getStaffEmailAddresses(ID_MULTIPLE);
        assertThat(addresses.size()).isGreaterThan(1);
        assertThat(addresses.get(0)).isEqualToIgnoringCase(multipleAddresses.get(0));
        verify(staffRepository, times(1)).findByStaffId(ID_MULTIPLE);
        verify(staffRepository, times(1)).findEmailAddressesForStaffId(ID_MULTIPLE);
    }

    @Test
    public void testSingleEmail() {
        when(staffRepository.findByStaffId(ID_SINGLE)).thenReturn(getValidStaffDetails(ID_SINGLE));
        when(staffRepository.findEmailAddressesForStaffId(ID_SINGLE)).thenReturn(singleAddress);
        List<String> addresses = staffService.getStaffEmailAddresses(ID_SINGLE);
        assertThat(addresses.size()).isEqualTo(1);
        assertThat(addresses.get(0)).isEqualToIgnoringCase(singleAddress.get(0));
        verify(staffRepository, times(1)).findByStaffId(ID_SINGLE);
        verify(staffRepository, times(1)).findEmailAddressesForStaffId(ID_SINGLE);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testNoneExist() {
        when(staffRepository.findByStaffId(ID_NONE)).thenReturn(getValidStaffDetails(ID_NONE));
        when(staffRepository.findEmailAddressesForStaffId(ID_NONE)).thenReturn(emptyList).thenThrow(EntityNotFoundException.withId(ID_NONE));
        List<String> addresses = staffService.getStaffEmailAddresses(ID_NONE);
        assertThat(addresses).isEmpty();
        verify(staffRepository, times(1)).findByStaffId(ID_NONE);
        verify(staffRepository, times(1)).findEmailAddressesForStaffId(ID_NONE);
    }

    @Test(expected = BadRequestException.class)
    public void testInvalidId() {
        when(staffRepository.findByStaffId(ID_BAD)).thenThrow(new BadRequestException());
        List<String> addresses = staffService.getStaffEmailAddresses(ID_BAD);
        assertThat(addresses).isNull();
        verify(staffRepository, times(1)).findByStaffId(ID_BAD);
        verify(staffRepository, times(0)).findEmailAddressesForStaffId(ID_BAD);
    }

    private Optional<StaffDetail> getValidStaffDetails(Long staffId) {
        HashMap<String,Object> additionalProperties = new HashMap<String,Object>();
        StaffDetail staffDetail = new StaffDetail(additionalProperties, staffId, "Bob", "Harris", "ACTIVE", 0L);
        return Optional.of(staffDetail);
    }
}
