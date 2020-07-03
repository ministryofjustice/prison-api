package net.syscon.prison.api.resource.impl;

import net.syscon.prison.api.resource.OffenderAssessmentResource;
import net.syscon.prison.api.support.CategoryInformationType;
import net.syscon.prison.service.InmateService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.client.HttpClientErrorException;

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

    @Test(expected = HttpClientErrorException.class)
    public void getOffenderCategorisationsInvalidType() {
        offenderAssessmentResource.getOffenderCategorisations("LEI", "INVALID_CAT_TYPE", null);
    }

    @Test()
    public void getOffenderCategorisations() {
        offenderAssessmentResource.getOffenderCategorisations("LEI", CategoryInformationType.CATEGORISED.name(), null);
        verify(inmateService).getCategory("LEI", CategoryInformationType.CATEGORISED, null);
    }
}
