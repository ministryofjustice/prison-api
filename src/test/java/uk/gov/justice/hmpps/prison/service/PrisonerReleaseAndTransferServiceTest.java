package uk.gov.justice.hmpps.prison.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.justice.hmpps.prison.api.model.InmateDetail;
import uk.gov.justice.hmpps.prison.api.model.RequestForCourtTransferIn;
import uk.gov.justice.hmpps.prison.api.model.RequestToReleasePrisoner;
import uk.gov.justice.hmpps.prison.repository.FinanceRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.*;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.*;
import uk.gov.justice.hmpps.prison.repository.storedprocs.CopyProcs.CopyBookData;
import uk.gov.justice.hmpps.prison.repository.storedprocs.OffenderAdminProcs.GenerateNewBookingNo;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.service.transformers.OffenderTransformer;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.time.Clock;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@WithMockUser("ITAG_USER_ADM")
@ContextConfiguration(classes = TestClock.class)
@ActiveProfiles("test")
@Transactional
public class PrisonerReleaseAndTransferServiceTest {

    @Mock
    OffenderBookingRepository offenderBookingRepository;
    @Mock
    OffenderRepository offenderRepository;
    @Mock
    AgencyLocationRepository agencyLocationRepository;
    @Mock
    ExternalMovementRepository externalMovementRepository;
    @Mock
    ReferenceCodeRepository<MovementType> movementTypeRepository;
    @Mock
    ReferenceCodeRepository<AgencyLocationType> agencyLocationTypeRepository;
    @Mock
    ReferenceCodeRepository<MovementReason> movementReasonRepository;
    @Mock
    BedAssignmentHistoriesRepository bedAssignmentHistoriesRepository;
    @Mock
    AgencyInternalLocationRepository agencyInternalLocationRepository;
    @Mock
    MovementTypeAndReasonRespository movementTypeAndReasonRespository;
    @Mock
    OffenderSentenceAdjustmentRepository offenderSentenceAdjustmentRepository;
    @Mock
    OffenderKeyDateAdjustmentRepository offenderKeyDateAdjustmentRepository;
    @Mock
    OffenderCaseNoteRepository caseNoteRepository;
    @Mock
    AuthenticationFacade authenticationFacade;
    @Mock
    OffenderNoPayPeriodRepository offenderNoPayPeriodRepository;
    @Mock
    OffenderPayStatusRepository offenderPayStatusRepository;
    @Mock
    AvailablePrisonIepLevelRepository availablePrisonIepLevelRepository;
    @Mock
    FinanceRepository financeRepository;
    @Mock
    ImprisonmentStatusRepository imprisonmentStatusRepository;
    @Mock
    ReferenceCodeRepository<CaseNoteType> caseNoteTypeReferenceCodeRepository;
    @Mock
    ReferenceCodeRepository<CaseNoteSubType> caseNoteSubTypeReferenceCodeRepository;
    @Mock
    ProfileCodeRepository profileCodeRepository;
    @Mock
    ProfileTypeRepository profileTypeRepository;
    @Mock
    ReferenceCodeRepository<AgencyLocationType> agencyLocationTypeReferenceCodeRepository;
    @Mock
    StaffUserAccountRepository staffUserAccountRepository;
    @Mock
    GenerateNewBookingNo generateNewBookingNo;
    @Mock
    CopyTableRepository copyTableRepository;
    @Mock
    CopyBookData copyBookData;
    @Mock
    OffenderTransformer offenderTransformer;
    @Mock
    OffenderProgramProfileRepository offenderProgramProfileRepository;
    @Mock
    EntityManager entityManager;
    @Mock
    CourtEventRepository courtEventRepository;
    @Mock
    ReferenceCodeRepository<EventStatus> eventStatusRepository;

    @Autowired
    PrisonerReleaseAndTransferService prisonerReleaseAndTransferService;

    private static String OFFENDER_NO = "G6942UN";


    @Test
    public void returnPrisonerFromCourt() {
        RequestForCourtTransferIn requestForCourtTransferIn = new RequestForCourtTransferIn();
        requestForCourtTransferIn.setAgencyId("ABDRCT");
        InmateDetail inmateDetail = prisonerReleaseAndTransferService.courtTransferIn(OFFENDER_NO, requestForCourtTransferIn);
    }


}
