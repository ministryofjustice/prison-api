package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
public class OffenderEmploymentRepositoryTest {

    @Autowired
    private OffenderEmploymentRepository repository;

    @Test
    public void findAllByNomisId() {
        var employments1 = repository.findAllByNomisId("G8346GA", Pageable.unpaged());
        var employments2 = repository.findAllByNomisId("G2823GV", Pageable.unpaged());

        assertThat(employments1).hasSize(3);
        assertThat(employments1.stream().filter(em-> em.getAddresses().size() == 2).count()).isEqualTo(1);
        assertThat(employments1.stream().filter(em-> em.getAddresses().size() == 0).count()).isEqualTo(2);
        assertThat(employments2).hasSize(2);
        assertThat(employments1.stream().filter(em-> em.getAddresses().size() == 0).count()).isEqualTo(2);
    }

    @Test
    public void pagedFindAllByNomisId() {
        var size = 2;
        var employmentsPage1 = repository.findAllByNomisId("G8346GA", PageRequest.of(0, size));
        var employmentsPage2 = repository.findAllByNomisId("G8346GA", PageRequest.of(1, size));

        assertThat(employmentsPage1.getTotalElements()).isEqualTo(3);
        assertThat(employmentsPage2).hasSize(1);
        assertThat(employmentsPage2.getTotalElements()).isEqualTo(3);
        assertThat(employmentsPage2).hasSize(1);
    }

}