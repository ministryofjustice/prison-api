package net.syscon.elite.web.config;

import com.nimbusds.jose.JOSEException;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

public class JwkClientTest {

    private RestTemplate restTemplate = mock(RestTemplate.class);
    private final String URL = "some_url";

    public JwkClientTest() throws ParseException {
    }

    @Test
    public void getJwkSets_test() throws ParseException, JOSEException {
        when(restTemplate.exchange(URL, GET, null, String.class)).thenReturn(jwkSetResponse());

        JwkClient jwkClient = new JwkClient(URL);

        assertThat(jwkClient.getPublicKeyForKeyId("dps-client-key")).isNotNull();
    }

    private ResponseEntity<String> jwkSetResponse() {
        return new ResponseEntity<>("{\n" +
                "  \"keys\": [\n" +
                "    {\n" +
                "      \"kty\": \"RSA\",\n" +
                "      \"e\": \"AQAB\",\n" +
                "      \"use\": \"sig\",\n" +
                "      \"kid\": \"dps-client-key\",\n" +
                "      \"alg\": \"RS256\",\n" +
                "      \"n\": \"sOPAtsQADdbRu_EH6LP5BM1_mF40VDBn12hJSXPPd5WYK0HLY20VM7AxxR9mnYCF6So1Wt7fGNqUx_WyemBpIJNrs_7Dzwg3uwiQuNh4zKR-EGxWbLwi3yw7lXPUzxUyC5xt88e_7vO-lz1oCnizjh4mxNAms6ZYF7qfnhJE9WvWPwLLkojkZu1JdusLaVowN7GTGNpME8dzeJkam0gp4oxHQGhMN87K6jqX3cEwO6Dvhemg8whs96nzQl8n2LFvAK2up9Prr9Gi2LFgTt7KqXA06kC4Kgw2IR1eFgzcBlTOEwmzjre65HoNaJBr9uNZzV5sILPMczzhQj_fMhz3_Q\"\n" +
                "    }\n" +
                "  ]\n" +
                "}\n",
                OK);
    }
}
