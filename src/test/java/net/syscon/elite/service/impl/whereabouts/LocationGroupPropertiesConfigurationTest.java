package net.syscon.elite.service.impl.whereabouts;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Properties;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {LocationGroupPropertiesConfiguration.class})
public class LocationGroupPropertiesConfigurationTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    @Qualifier("whereaboutsGroups")
    private Properties properties;

    @Autowired
    @Qualifier("whereaboutsEnabled")
    Set<String> enabled;

    @Test
    public void checkContext() {
        assertThat(context).isNotNull();
    }

    @Test
    public void groupsPropertiesWiredInUsingQualifier() {
        assertThat(properties)
                .isNotEmpty()
                .containsKeys("MDI_Houseblock 1", "WCI_Segregation");
    }

    @Test
    public void enabledAgencies() {
        assertThat(enabled)
                .isNotNull()
                .contains("BRI");
    }
}
