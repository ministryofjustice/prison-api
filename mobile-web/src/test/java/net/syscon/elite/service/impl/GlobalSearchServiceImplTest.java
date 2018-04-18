package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.PrisonerDetail;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.repository.InmateRepository;
import net.syscon.elite.service.GlobalSearchService;
import net.syscon.elite.service.PrisonerDetailSearchCriteria;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.*;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(InmateRepository.class)
public class GlobalSearchServiceImplTest {
    private InmateRepository repository;

    private PrisonerDetailSearchCriteria criteria;

    private GlobalSearchService service;

    private PageRequest pageRequest = new PageRequest();

    @Before
    public void setUp() {
        repository = Mockito.mock(InmateRepository.class);
        service = new GlobalSearchServiceImpl(repository);

        PowerMockito.mockStatic(InmateRepository.class);
    }

    @Test
    public void testFindOffendersBlankCriteria() {
        when(InmateRepository.generateFindOffendersQuery(criteria)).thenReturn("");

        criteria = PrisonerDetailSearchCriteria.builder().build();

        service.findOffenders(criteria, pageRequest);

        Mockito.verify(repository, Mockito.never()).findOffenders(anyString(), any(PageRequest.class));
    }

    @Test
    public void testFindOffendersByOffenderNo() {
        final String TEST_OFFENDER_NO = "AA1234B";
        final String TEST_QUERY = "offenderNo:eq:'AA1234B'";

        criteria = PrisonerDetailSearchCriteria.builder().offenderNo(TEST_OFFENDER_NO).build();

        when(InmateRepository.generateFindOffendersQuery(criteria)).thenReturn(TEST_QUERY);
        Mockito.when(repository.findOffenders(eq(TEST_QUERY), any(PageRequest.class))).thenReturn(pageResponse(0));

        service.findOffenders(criteria, pageRequest);

        Mockito.verify(repository, Mockito.times(1)).findOffenders(eq(TEST_QUERY), any(PageRequest.class));
    }

    @Test
    public void testFindOffendersPrioritisedMatchWithOffenderNoMatch() {
        final String TEST_OFFENDER_NO = "AA1234B";
        final String TEST_PNC_NUMBER = "PNC123456";
        final String TEST_OFFENDER_NO_QUERY = "offenderNo:eq:'AA1234B'";
        final String TEST_PNC_NUMBER_QUERY = "pncNumber:eq:'PNC123456'";

        criteria = PrisonerDetailSearchCriteria.builder()
                .prioritisedMatch(true)
                .offenderNo(TEST_OFFENDER_NO)
                .pncNumber(TEST_PNC_NUMBER)
                .build();

        PrisonerDetailSearchCriteria offNoCriteria = PrisonerDetailSearchCriteria.builder().offenderNo(TEST_OFFENDER_NO).build();

        when(InmateRepository.generateFindOffendersQuery(offNoCriteria)).thenReturn(TEST_OFFENDER_NO_QUERY);

        Mockito.when(repository.findOffenders(eq(TEST_OFFENDER_NO_QUERY), any(PageRequest.class))).thenReturn(pageResponse(1));

        Page<PrisonerDetail> response = service.findOffenders(criteria, pageRequest);

        assertThat(response.getItems()).isNotEmpty();

        Mockito.verify(repository, Mockito.times(1)).findOffenders(eq(TEST_OFFENDER_NO_QUERY), any(PageRequest.class));
        Mockito.verify(repository, Mockito.never()).findOffenders(eq(TEST_PNC_NUMBER_QUERY), any(PageRequest.class));
    }

    @Test
    public void testFindOffendersPrioritisedMatchWithPncNumberMatch() {
        final String TEST_OFFENDER_NO = "AA1234B";
        final String TEST_PNC_NUMBER = "PNC123456";
        final String TEST_OFFENDER_NO_QUERY = "offenderNo:eq:'AA1234B'";
        final String TEST_PNC_NUMBER_QUERY = "pncNumber:eq:'PNC123456'";

        criteria = PrisonerDetailSearchCriteria.builder()
                .prioritisedMatch(true)
                .offenderNo(TEST_OFFENDER_NO)
                .pncNumber(TEST_PNC_NUMBER)
                .build();

        PrisonerDetailSearchCriteria offNoCriteria = PrisonerDetailSearchCriteria.builder().offenderNo(TEST_OFFENDER_NO).build();
        PrisonerDetailSearchCriteria pncNumberCriteria = PrisonerDetailSearchCriteria.builder().pncNumber(TEST_PNC_NUMBER).build();

        when(InmateRepository.generateFindOffendersQuery(offNoCriteria)).thenReturn(TEST_OFFENDER_NO_QUERY);
        when(InmateRepository.generateFindOffendersQuery(pncNumberCriteria)).thenReturn(TEST_PNC_NUMBER_QUERY);

        Mockito.when(repository.findOffenders(eq(TEST_OFFENDER_NO_QUERY), any(PageRequest.class))).thenReturn(pageResponse(0));
        Mockito.when(repository.findOffenders(eq(TEST_PNC_NUMBER_QUERY), any(PageRequest.class))).thenReturn(pageResponse(1));

        Page<PrisonerDetail> response = service.findOffenders(criteria, pageRequest);

        assertThat(response.getItems()).isNotEmpty();

        Mockito.verify(repository, Mockito.times(1)).findOffenders(eq(TEST_OFFENDER_NO_QUERY), any(PageRequest.class));
        Mockito.verify(repository, Mockito.times(1)).findOffenders(eq(TEST_PNC_NUMBER_QUERY), any(PageRequest.class));
    }

    @Test
    public void testFindOffendersPrioritisedMatchWithCroNumberMatch() {
        final String TEST_OFFENDER_NO = "AA1234B";
        final String TEST_PNC_NUMBER = "PNC123456";
        final String TEST_CRO_NUMBER = "CRO987654";
        final String TEST_OFFENDER_NO_QUERY = "offenderNo:eq:'AA1234B'";
        final String TEST_PNC_NUMBER_QUERY = "pncNumber:eq:'PNC123456'";
        final String TEST_CRO_NUMBER_QUERY = "croNumber:eq:'CRO987654'";

        criteria = PrisonerDetailSearchCriteria.builder()
                .prioritisedMatch(true)
                .offenderNo(TEST_OFFENDER_NO)
                .pncNumber(TEST_PNC_NUMBER)
                .croNumber(TEST_CRO_NUMBER)
                .build();

        PrisonerDetailSearchCriteria offNoCriteria = PrisonerDetailSearchCriteria.builder().offenderNo(TEST_OFFENDER_NO).build();
        PrisonerDetailSearchCriteria pncNumberCriteria = PrisonerDetailSearchCriteria.builder().pncNumber(TEST_PNC_NUMBER).build();
        PrisonerDetailSearchCriteria croNumberCriteria = PrisonerDetailSearchCriteria.builder().croNumber(TEST_CRO_NUMBER).build();

        when(InmateRepository.generateFindOffendersQuery(offNoCriteria)).thenReturn(TEST_OFFENDER_NO_QUERY);
        when(InmateRepository.generateFindOffendersQuery(pncNumberCriteria)).thenReturn(TEST_PNC_NUMBER_QUERY);
        when(InmateRepository.generateFindOffendersQuery(croNumberCriteria)).thenReturn(TEST_CRO_NUMBER_QUERY);

        Mockito.when(repository.findOffenders(eq(TEST_OFFENDER_NO_QUERY), any(PageRequest.class))).thenReturn(pageResponse(0));
        Mockito.when(repository.findOffenders(eq(TEST_PNC_NUMBER_QUERY), any(PageRequest.class))).thenReturn(pageResponse(0));
        Mockito.when(repository.findOffenders(eq(TEST_CRO_NUMBER_QUERY), any(PageRequest.class))).thenReturn(pageResponse(1));

        Page<PrisonerDetail> response = service.findOffenders(criteria, pageRequest);

        assertThat(response.getItems()).isNotEmpty();

        Mockito.verify(repository, Mockito.times(1)).findOffenders(eq(TEST_OFFENDER_NO_QUERY), any(PageRequest.class));
        Mockito.verify(repository, Mockito.times(1)).findOffenders(eq(TEST_PNC_NUMBER_QUERY), any(PageRequest.class));
        Mockito.verify(repository, Mockito.times(1)).findOffenders(eq(TEST_CRO_NUMBER_QUERY), any(PageRequest.class));
    }

    @Test
    public void testFindOffendersPrioritisedMatchWithPersonalAttrsMatch() {
        final String TEST_OFFENDER_NO = "AA1234B";
        final String TEST_LAST_NAME = "STEPHENS";
        final String TEST_OFFENDER_NO_QUERY = "offenderNo:eq:'AA1234B'";
        final String TEST_PERSONAL_ATTRS_QUERY = "lastName:eq:'STEPHENS'";

        criteria = PrisonerDetailSearchCriteria.builder()
                .prioritisedMatch(true)
                .offenderNo(TEST_OFFENDER_NO)
                .lastName(TEST_LAST_NAME)
                .build();

        PrisonerDetailSearchCriteria offNoCriteria = PrisonerDetailSearchCriteria.builder().offenderNo(TEST_OFFENDER_NO).build();
        PrisonerDetailSearchCriteria personalAttrsCriteria = PrisonerDetailSearchCriteria.builder().lastName(TEST_LAST_NAME).build();

        when(InmateRepository.generateFindOffendersQuery(offNoCriteria)).thenReturn(TEST_OFFENDER_NO_QUERY);
        when(InmateRepository.generateFindOffendersQuery(personalAttrsCriteria)).thenReturn(TEST_PERSONAL_ATTRS_QUERY);

        Mockito.when(repository.findOffenders(eq(TEST_OFFENDER_NO_QUERY), any(PageRequest.class))).thenReturn(pageResponse(0));
        Mockito.when(repository.findOffenders(eq(TEST_PERSONAL_ATTRS_QUERY), any(PageRequest.class))).thenReturn(pageResponse(3));

        Page<PrisonerDetail> response = service.findOffenders(criteria, pageRequest);

        assertThat(response.getItems()).isNotEmpty();

        Mockito.verify(repository, Mockito.times(1)).findOffenders(eq(TEST_OFFENDER_NO_QUERY), any(PageRequest.class));
        Mockito.verify(repository, Mockito.times(1)).findOffenders(eq(TEST_PERSONAL_ATTRS_QUERY), any(PageRequest.class));
    }

    @Test
    public void testFindOffendersPrioritisedMatchWithDobRangeMatch() {
        final String TEST_OFFENDER_NO = "AA1234B";
        final String TEST_LAST_NAME = "STEPHENS";
        final LocalDate TEST_DOB_FROM = LocalDate.of(1960, 1, 1);
        final LocalDate TEST_DOB_TO = LocalDate.of(1964, 12, 31);
        final String TEST_OFFENDER_NO_QUERY = "offenderNo:eq:'AA1234B'";
        final String TEST_PERSONAL_ATTRS_QUERY = "lastName:eq:'STEPHENS'";
        final String TEST_DOB_RANGE_QUERY = "(and:dateOfBirth:gteq:'1960-01-01':'YYYY-MM-DD',and:dateOfBirth:lteq:'1964-12-31':'YYYY-MM-DD')";

        criteria = PrisonerDetailSearchCriteria.builder()
                .prioritisedMatch(true)
                .offenderNo(TEST_OFFENDER_NO)
                .lastName(TEST_LAST_NAME)
                .dobFrom(TEST_DOB_FROM)
                .dobTo(TEST_DOB_TO)
                .build();

        PrisonerDetailSearchCriteria offNoCriteria = PrisonerDetailSearchCriteria.builder().offenderNo(TEST_OFFENDER_NO).build();
        PrisonerDetailSearchCriteria personalAttrsCriteria = PrisonerDetailSearchCriteria.builder().lastName(TEST_LAST_NAME).build();

        PrisonerDetailSearchCriteria dobRangeCriteria = PrisonerDetailSearchCriteria.builder()
                .dobFrom(TEST_DOB_FROM)
                .dobTo(TEST_DOB_TO)
                .build();

        when(InmateRepository.generateFindOffendersQuery(offNoCriteria)).thenReturn(TEST_OFFENDER_NO_QUERY);
        when(InmateRepository.generateFindOffendersQuery(personalAttrsCriteria)).thenReturn(TEST_PERSONAL_ATTRS_QUERY);
        when(InmateRepository.generateFindOffendersQuery(dobRangeCriteria)).thenReturn(TEST_DOB_RANGE_QUERY);

        Mockito.when(repository.findOffenders(eq(TEST_OFFENDER_NO_QUERY), any(PageRequest.class))).thenReturn(pageResponse(0));
        Mockito.when(repository.findOffenders(eq(TEST_PERSONAL_ATTRS_QUERY), any(PageRequest.class))).thenReturn(pageResponse(0));
        Mockito.when(repository.findOffenders(eq(TEST_DOB_RANGE_QUERY), any(PageRequest.class))).thenReturn(pageResponse(5));

        Page<PrisonerDetail> response = service.findOffenders(criteria, pageRequest);

        assertThat(response.getItems()).isNotEmpty();

        Mockito.verify(repository, Mockito.times(1)).findOffenders(eq(TEST_OFFENDER_NO_QUERY), any(PageRequest.class));
        Mockito.verify(repository, Mockito.times(1)).findOffenders(eq(TEST_PERSONAL_ATTRS_QUERY), any(PageRequest.class));
        Mockito.verify(repository, Mockito.times(1)).findOffenders(eq(TEST_DOB_RANGE_QUERY), any(PageRequest.class));
    }

    private Page<PrisonerDetail> pageResponse(int prisonerCount) {
        List<PrisonerDetail> prisoners = new ArrayList<>();

        for (int i = 1; i <= prisonerCount; i++) {
            prisoners.add(PrisonerDetail.builder().offenderNo(String.format("A%4dAA", i)).build());
        }

        return new Page<>(prisoners, prisonerCount, 0, 10);
    }
}
