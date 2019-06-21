package net.syscon.elite.api.resource.impl;

import net.syscon.elite.util.JwtAuthenticationHelper;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@ActiveProfiles("nomis-hsqldb")
@SpringBootTest(webEnvironment = RANDOM_PORT)
//@TestPropertySource({ "/application-test.properties" })
public abstract class ResourceTest {

    @Autowired
    protected JwtAuthenticationHelper jwtAuthenticationHelper;

    @Autowired
    protected TestRestTemplate testRestTemplate;
}
