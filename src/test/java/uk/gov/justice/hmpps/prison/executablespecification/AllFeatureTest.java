package uk.gov.justice.hmpps.prison.executablespecification;

import io.cucumber.junit.CucumberOptions;
import net.serenitybdd.cucumber.CucumberWithSerenity;
import org.junit.runner.RunWith;

@RunWith(CucumberWithSerenity.class)
@CucumberOptions(plugin = "pretty", features = "src/test/resources/features/booking_sentence_details.feature", tags = "not @broken")
public class AllFeatureTest {}
