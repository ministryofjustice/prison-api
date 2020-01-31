package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.resource.OffenderAssessmentResource;
import net.syscon.elite.api.support.CategoryInformationType;
import net.syscon.elite.service.InmateService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.ws.rs.BadRequestException;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class OffenderResourceImplTest {
    @Mock
    private InmateService inmateService;
    private OffenderAssessmentResource offenderAssessmentResource;

    @Before
    public void setUp() {
        offenderAssessmentResource = new OffenderAssessmentResourceImpl(inmateService);
    }

    @Test(expected = BadRequestException.class)
    public void getOffenderCategorisationsInvalidType() {
        offenderAssessmentResource.getOffenderCategorisations("LEI", "INVALID_CAT_TYPE", null);
    }

    @Test()
    public void getOffenderCategorisations() {
        offenderAssessmentResource.getOffenderCategorisations("LEI", CategoryInformationType.CATEGORISED.name(), null);
        verify(inmateService).getCategory("LEI", CategoryInformationType.CATEGORISED, null);
    }
}
