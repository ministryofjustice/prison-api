package net.syscon.elite.service.impl;

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
import java.util.Arrays;
import java.util.List;

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
    public void testGetStaffEmailMultipleExist() {
        when(staffRepository.findEmailAddressesForStaffId(ID_MULTIPLE)).thenReturn(multipleAddresses);
        List<String> addreses = staffService.getStaffEmailAddresses(ID_MULTIPLE);
        // Assertions for list content - multiple entries
        verify(staffRepository, times(1)).findEmailAddressesForStaffId(ID_MULTIPLE);
    }

    @Test
    public void testGetStaffEmailSingleExists() {
        when(staffRepository.findEmailAddressesForStaffId(ID_SINGLE)).thenReturn(singleAddress);
        List<String> addreses = staffService.getStaffEmailAddresses(ID_SINGLE);
        // Assertions for list content - one entry
        verify(staffRepository, times(1)).findEmailAddressesForStaffId(ID_SINGLE);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testGetStaffEmailsNoneExist() {
        when(staffRepository.findEmailAddressesForStaffId(ID_NONE)).thenThrow(new EntityNotFoundException(""));
        List<String> addreses = staffService.getStaffEmailAddresses(ID_NONE);
        verify(staffRepository, times(1)).findEmailAddressesForStaffId(ID_NONE);
    }

    @Test(expected = BadRequestException.class)
    public void testGetStaffEmailInvalidId() {
        when(staffRepository.findEmailAddressesForStaffId(ID_BAD)).thenReturn(emptyList);
        List<String> addreses = staffService.getStaffEmailAddresses(ID_NONE);
        verify(staffRepository, times(1)).findEmailAddressesForStaffId(ID_NONE);
    }
}
