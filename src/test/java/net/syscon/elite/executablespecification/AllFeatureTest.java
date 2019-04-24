package net.syscon.elite.executablespecification;

import cucumber.api.CucumberOptions;
import cucumber.deps.com.thoughtworks.xstream.annotations.XStreamConverter;
import cucumber.deps.com.thoughtworks.xstream.annotations.XStreamConverters;
import net.serenitybdd.cucumber.CucumberWithSerenity;
import net.syscon.elite.util.LocalDateConverter;
import net.syscon.elite.util.LocalTimeConverter;
import org.junit.runner.RunWith;

@RunWith(CucumberWithSerenity.class)
@CucumberOptions(plugin = "pretty", features = "src/test/resources/features", tags = {"~@ignore"})
@XStreamConverters(value = {
        @XStreamConverter(LocalDateConverter.class),
        @XStreamConverter(LocalTimeConverter.class)
})
public class AllFeatureTest {
}
