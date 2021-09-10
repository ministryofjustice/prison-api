package uk.gov.justice.hmpps.prison.api.resource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.service.CaseLoadService;
import uk.gov.justice.hmpps.prison.service.CaseNoteService;
import uk.gov.justice.hmpps.prison.service.LocationService;
import uk.gov.justice.hmpps.prison.service.UserService;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;


@ExtendWith(MockitoExtension.class)
public class UserResourceTest {
    @Mock
    private AuthenticationFacade authenticationFacade;
    @Mock
    private UserService userService;
    @Mock
    private LocationService locationService;
    @Mock
    private CaseLoadService caseLoadService;
    @Mock
    private CaseNoteService caseNoteService;

    private UserResource userResource;

    @BeforeEach
    public void setUp() {
        userResource = new UserResource(authenticationFacade, locationService, userService,
            caseLoadService, caseNoteService);
    }

    @Test
    public void addAccessRoles_noRolesAdded() {
        userResource.addAccessRoles("bob", Collections.emptyList());
        verifyNoInteractions(userService);
    }

    @Test
    public void addAccessRoles() {
        userResource.addAccessRoles("bob", List.of("Joe", "fred"));
        verify(userService).addAccessRole("bob", "Joe");
        verify(userService).addAccessRole("bob", "fred");
    }
}
