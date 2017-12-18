package net.syscon.elite.executablespecification;

import cucumber.api.CucumberOptions;
import net.serenitybdd.cucumber.CucumberWithSerenity;
import org.junit.runner.RunWith;

@RunWith(CucumberWithSerenity.class)
@CucumberOptions(format = "pretty", features = "src/test/resources/features", tags = {"~@ignore"})
public class AllFeatureTest {
}
