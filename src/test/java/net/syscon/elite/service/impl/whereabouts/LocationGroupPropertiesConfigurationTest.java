package net.syscon.elite.service.impl.whereabouts;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    public void whereaboutsGroups_AreAllPatternsThatCompile() {
        properties.values().forEach((e) -> {
            final var patterns = ((String) e).split(",");
            Arrays.stream(patterns).map(Pattern::compile).forEach(p -> {
                //noinspection ResultOfMethodCallIgnored
                p.matcher("some input");
            });
        });
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void whereaboutsGroups_NoDuplicateValues() {
        final var values = properties.values();
        // have to split as each line of properties file can contain multiple patterns
        values.stream().flatMap(a -> Arrays.stream(((String) a).split(",")))
                // then collect into a map which checks for duplicates in the merge function
                .collect(Collectors.toMap((v) -> v, (v) -> v, (a, b) -> {
                    assertThat(a).isNotEqualTo(b);
                    return a;
                }));
    }

    @Test
    public void enabledAgencies() {
        assertThat(enabled)
                .isNotNull()
                .contains("BRI");
    }
}
