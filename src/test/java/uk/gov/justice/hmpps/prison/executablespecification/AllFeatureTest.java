package uk.gov.justice.hmpps.prison.executablespecification;

import cucumber.api.CucumberOptions;
import cucumber.deps.com.thoughtworks.xstream.annotations.XStreamConverter;
import cucumber.deps.com.thoughtworks.xstream.annotations.XStreamConverters;
import net.serenitybdd.cucumber.CucumberWithSerenity;
import org.junit.runner.RunWith;
import uk.gov.justice.hmpps.prison.util.LocalDateConverter;
import uk.gov.justice.hmpps.prison.util.LocalDateTimeConverter;
import uk.gov.justice.hmpps.prison.util.LocalTimeConverter;

@RunWith(CucumberWithSerenity.class)
@CucumberOptions(plugin = "pretty", features = "src/test/resources/features", tags = {"~@ignore"})
@XStreamConverters(value = {
        @XStreamConverter(LocalDateConverter.class),
        @XStreamConverter(LocalDateTimeConverter.class),
        @XStreamConverter(LocalTimeConverter.class)
})
public class AllFeatureTest {}
