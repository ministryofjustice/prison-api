package net.syscon.prison.executablespecification;

import cucumber.api.CucumberOptions;
import cucumber.deps.com.thoughtworks.xstream.annotations.XStreamConverter;
import cucumber.deps.com.thoughtworks.xstream.annotations.XStreamConverters;
import net.serenitybdd.cucumber.CucumberWithSerenity;
import net.syscon.prison.util.LocalDateConverter;
import net.syscon.prison.util.LocalDateTimeConverter;
import net.syscon.prison.util.LocalTimeConverter;
import org.junit.runner.RunWith;

@RunWith(CucumberWithSerenity.class)
@CucumberOptions(plugin = "pretty", features = "src/test/resources/features", tags = {"~@ignore"})
@XStreamConverters(value = {
        @XStreamConverter(LocalDateConverter.class),
        @XStreamConverter(LocalDateTimeConverter.class),
        @XStreamConverter(LocalTimeConverter.class)
})
public class AllFeatureTest {}
