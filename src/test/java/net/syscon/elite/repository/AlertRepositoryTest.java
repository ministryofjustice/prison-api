package net.syscon.elite.repository;

import net.syscon.elite.api.model.AlertSubtype;
import net.syscon.elite.web.config.PersistenceConfigs;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)
public class AlertRepositoryTest {
    @Autowired
    private AlertRepository repository;

    @Test
    public void testGetAlertTypes() {

        final var alertTypes = repository.getAlertTypes();

        assertThat(alertTypes).hasSize(13);

        // Spot checking
        assertThat(alertTypes.get(0).getCode()).isEqualTo("H");
        assertThat(alertTypes.get(0).getDescription()).isEqualTo("Self Harm");
        assertThat(alertTypes.get(0).getListSeq()).isEqualTo(1);
        
        assertThat(alertTypes.get(12).getCode()).isEqualTo("A");
        assertThat(alertTypes.get(12).getDescription()).isEqualTo("Social Care");
        assertThat(alertTypes.get(12).getListSeq()).isEqualTo(12);
    }

    @Test
    public void testGetAlertSubtypesForSelfHarm() {

        final var alertSubtypes = repository.getAlertSubtypes("H");

        assertThat(alertSubtypes).hasSize(3);

        assertThat(alertSubtypes).containsExactly(AlertSubtype.builder()
                                                            .code("HA")
                                                            .description("ACCT Open (HMPS)")
                                                            .listSeq(1)
                                                            .parentCode("H")
                                                            .build(),
                                                  AlertSubtype.builder()
                                                            .code("HC")
                                                            .description("Self Harm - Custody")
                                                            .listSeq(1)
                                                            .parentCode("H")
                                                            .build(),
                                                  AlertSubtype.builder()
                                                            .code("HS")
                                                            .description("Self Harm - Community")
                                                            .listSeq(1)
                                                            .parentCode("H")
                                                            .build());
    }
}
