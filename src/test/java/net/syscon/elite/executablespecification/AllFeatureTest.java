package net.syscon.elite.executablespecification;

import cucumber.api.CucumberOptions;
import cucumber.deps.com.thoughtworks.xstream.annotations.XStreamConverter;
import cucumber.deps.com.thoughtworks.xstream.annotations.XStreamConverters;
import net.serenitybdd.cucumber.CucumberWithSerenity;
import net.syscon.elite.api.resource.OauthMockServer;
import net.syscon.elite.util.LocalDateConverter;
import net.syscon.elite.util.LocalDateTimeConverter;
import net.syscon.elite.util.LocalTimeConverter;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

@RunWith(CucumberWithSerenity.class)
@CucumberOptions(plugin = "pretty", features = "src/test/resources/features", tags = {"~@ignore"})
@XStreamConverters(value = {
        @XStreamConverter(LocalDateConverter.class),
        @XStreamConverter(LocalDateTimeConverter.class),
        @XStreamConverter(LocalTimeConverter.class)
})
public class AllFeatureTest {

    private static OauthMockServer oauthMockServer;

    @BeforeClass
    public static void setUp() {
        oauthMockServer = new OauthMockServer(8080);
        oauthMockServer.start();
        oauthMockServer.stubJwkServer();
    }

    @AfterClass
    public static void tearDown() {
        oauthMockServer.stop();
    }


}
