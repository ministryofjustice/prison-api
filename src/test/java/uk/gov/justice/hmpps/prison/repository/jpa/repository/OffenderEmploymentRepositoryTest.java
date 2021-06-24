package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.prison.repository.jpa.model.City;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Country;
import uk.gov.justice.hmpps.prison.repository.jpa.model.County;
import uk.gov.justice.hmpps.prison.repository.jpa.model.EmploymentSchedule;
import uk.gov.justice.hmpps.prison.repository.jpa.model.EmploymentStatus;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Occupation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEmployment;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEmployment.PK;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEmploymentAddress;
import uk.gov.justice.hmpps.prison.repository.jpa.model.PayPeriod;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
public class OffenderEmploymentRepositoryTest {

    private final String offenderNumber = "G8346GA";
    private final int pageSize = 2;

    @Autowired
    private OffenderEmploymentRepository repository;

    private final OffenderEmploymentAddress address1 = OffenderEmploymentAddress.builder()
        .addressId(2349704L)
        .noFixedAddressFlag("N")
        .primaryFlag("N")
        .county(new County("STAFFS", "Staffordshire"))
        .city(new City("25764", "Stoke-On-Trent"))
        .country(new Country("ENG", "England"))
        .build();


    private final OffenderEmploymentAddress address2 = OffenderEmploymentAddress.builder()
        .addressId(2349705L)
        .noFixedAddressFlag("N")
        .primaryFlag("N")
        .country(new Country("ENG", "England"))
        .build();


    private final OffenderEmployment employment1 = OffenderEmployment
        .builder()
        .id(new PK(584215L, 1L))
        .startDate(LocalDate.of(2010, 4, 26))
        .postType(new EmploymentStatus("FT", "Full Time"))
        .employerName("HGuvZZoLHGuvZZo")
        .supervisorName("MarMar")
        .occupation(new Occupation("5807", "Driver"))
        .scheduleType(new EmploymentSchedule("INHAND", "In Hand"))
        .wagePeriod(new PayPeriod("WEEK", "Weekly"))
        .isEmployerAware(false)
        .isEmployerContactable(false)
        .addresses(List.of(address1, address2))
        .build();

    private final OffenderEmployment employment2 = OffenderEmployment
        .builder()
        .id(new PK(584215L, 2L))
        .startDate(LocalDate.of(2012, 3, 10))
        .postType(new EmploymentStatus("NDEAL", "New Deal"))
        .isEmployerAware(false)
        .isEmployerContactable(false)
        .build();

    private final OffenderEmployment employment3 = OffenderEmployment
        .builder()
        .id(new PK(584215L, 3L))
        .startDate(LocalDate.of(2012, 7, 11))
        .postType(new EmploymentStatus("SEMP", "Self Employed"))
        .occupation(new Occupation("4803", "Debt Collector"))
        .isEmployerAware(false)
        .isEmployerContactable(false)
        .build();


    @Test
    public void testExpectedNumberOfEmploymentsAreReturnedWithEmploymentAddresses() {

        final var employments = repository.findAllByNomisId(offenderNumber, Pageable.unpaged());

        assertThat(employments).hasSize(3);
        assertThat(employments.stream().filter(em -> em.getAddresses().size() == 2).count()).isEqualTo(1);
        assertThat(employments.stream().filter(em -> em.getAddresses().size() == 2).count()).isEqualTo(1);

        assertThat(employment1).isEqualTo(employments.getContent().get(0));
        assertThat(employment2).isEqualTo(employments.getContent().get(1));
        assertThat(employment3).isEqualTo(employments.getContent().get(2));


    }

    @Test
    public void pagedFindAllByNomisIdPage1() {
        final var employmentsPage1 = repository.findAllByNomisId(offenderNumber, PageRequest.of(0, pageSize));

        assertThat(employmentsPage1.getTotalElements()).isEqualTo(3);
        assertThat(employmentsPage1).hasSize(2);
        assertThat(employment1).isEqualTo(employmentsPage1.getContent().get(0));
        assertThat(employment2).isEqualTo(employmentsPage1.getContent().get(1));
    }

    @Test
    public void pagedFindAllByNomisIdPage2() {
        final var employmentsPage2 = repository.findAllByNomisId(offenderNumber, PageRequest.of(1, pageSize));

        assertThat(employmentsPage2.getTotalElements()).isEqualTo(3);
        assertThat(employmentsPage2).hasSize(1);
        assertThat(employment3).isEqualTo(employmentsPage2.getContent().get(0));
    }

}