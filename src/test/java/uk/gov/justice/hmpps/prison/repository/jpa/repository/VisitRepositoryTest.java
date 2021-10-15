package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.prison.repository.jpa.model.SearchLevel;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Visit;
import uk.gov.justice.hmpps.prison.repository.jpa.model.VisitStatus;
import uk.gov.justice.hmpps.prison.repository.jpa.model.VisitType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.VisitVisitor;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@Import({AuthenticationFacade.class, AuditorAwareImpl.class})
@WithMockUser
public class VisitRepositoryTest {

    @Autowired
    private VisitRepository repository;

    @Autowired
    private OffenderBookingRepository offenderBookingRepository;

    @Autowired
    private VisitOrderRepository visitOrderRepository;

    @Autowired
    private VisitVisitorRepository visitVisitorRepository;

    @Autowired
    private ReferenceCodeRepository<VisitType> visitTypeRepository;

    @Autowired
    private ReferenceCodeRepository<VisitStatus> visitStatusRepository;

    @Autowired
    private ReferenceCodeRepository<SearchLevel> searchRepository;


    @Autowired
    private AgencyLocationRepository agencyRepository;

    @Autowired
    private AgencyInternalLocationRepository agencyInternalRepository;

    @Autowired
    private AgencyVisitSlotRepository agencyVisitSlotRepository;


    @Autowired
    private PersonRepository personRepository;


    @Autowired
    private TestEntityManager entityManager;


    @Test
    void saveVisit() {
        final var offenderBooking = offenderBookingRepository.findById(-10L).orElseThrow();
        final var visit = Visit.builder()
                .offenderBooking(offenderBooking)
                .visitType(visitTypeRepository.findById(VisitType.pk("SCON")).orElseThrow())
                .visitStatus(visitStatusRepository.findById(VisitStatus.pk("SCH")).orElseThrow())
                .commentText("comment text")
                .visitDate(LocalDate.of(2009, 12, 21))
                .startTime(LocalDateTime.of(2009, 12, 21,13,15))
                .endTime(LocalDateTime.of(2009, 12, 21,14,15))
                .location(agencyRepository.findById("LEI").orElseThrow())
                .searchLevel(searchRepository.findById(SearchLevel.pk("FULL")).orElseThrow())
                .agencyInternalLocation(agencyInternalRepository.findById(-3L).orElseThrow())
                .visitorConcernText("visitor concerns")
                .visitOrder(visitOrderRepository.findById(-2L).orElseThrow())
                .agencyVisitSlot(agencyVisitSlotRepository.findById(-1L).orElseThrow())
                .build();
        repository.save(visit);

        entityManager.flush();

        final var persistedVisitList = repository.findByOffenderBooking(offenderBooking);
        assertThat(persistedVisitList).isNotEmpty();
        final var persistedVisit = persistedVisitList.get(0);

        visitVisitorRepository.save(VisitVisitor.builder()
                .offenderBooking(offenderBooking)
                .visitId(persistedVisitList.get(0).getId())
                .groupLeader(true).assistedVisit(true)
                .person(personRepository.findById(-1L).orElseThrow()).build());

        entityManager.flush();
        entityManager.refresh(persistedVisit);

        assertThat(persistedVisit.getVisitDate()).isEqualTo(LocalDate.of(2009, 12, 21));
        assertThat(persistedVisit.getStartTime()).isEqualTo(LocalDateTime.of(2009, 12, 21,13,15));
        assertThat(persistedVisit.getEndTime()).isEqualTo(LocalDateTime.of(2009, 12, 21,14,15));
        assertThat(persistedVisit.getId()).isNotNull();
        assertThat(persistedVisit.getSearchLevel().getDescription()).isEqualTo("Full Search");
        assertThat(persistedVisit.getVisitStatus().getDescription()).isEqualTo("Scheduled");
        assertThat(persistedVisit.getVisitorConcernText()).isEqualTo("visitor concerns");


        final var visitOrder = persistedVisit.getVisitOrder();
        assertThat(visitOrder).isNotNull();
        assertThat(visitOrder.getVisitOrderType().getDescription()).isEqualTo("Visiting Order");
        assertThat(visitOrder.getIssueDate()).isEqualTo(LocalDate.of(2001,1,1));
        assertThat(visitOrder.getStatus().getDescription()).isEqualTo("Active");
        assertThat(visitOrder.getCommentText()).isEqualTo("Some VO Comment Text");
        assertThat(visitOrder.getOffenderBooking().getBookingId()).isEqualTo(-10);

        final var agencyVisitSlot = persistedVisit.getAgencyVisitSlot();
        assertThat(agencyVisitSlot.getMaxAdults()).isEqualTo(18017);
        assertThat(agencyVisitSlot.getMaxGroups()).isEqualTo(30);
        assertThat(agencyVisitSlot.getWeekDay()).isEqualTo("SUN");
        assertThat(agencyVisitSlot.getLocation().getId()).isEqualTo("LEI");
        assertThat(agencyVisitSlot.getAgencyInternalLocation().getDescription()).isEqualTo("LEI-A-1-1");

        final var visitOrderVisitors = visitOrder.getVisitors();
        assertThat(visitOrderVisitors.size()).isEqualTo(1);

        final var visitOrderVisitorPerson = visitOrderVisitors.get(0).getPerson();
        assertThat(visitOrderVisitorPerson).isNotNull();
        assertThat(visitOrderVisitorPerson.getId()).isEqualTo(-1);

        final var visitVisitors = persistedVisit.getVisitors();
        assertThat(visitVisitors.size()).isEqualTo(1);
        final var visitVisitor = visitVisitors.get(0);
        assertThat(visitVisitor.isGroupLeader()).isTrue();
        assertThat(visitVisitor.isAssistedVisit()).isTrue();

        final var visitVisitorPerson = visitVisitors.get(0).getPerson();
        assertThat(visitVisitorPerson.getId()).isEqualTo(-1);

    }

}


