package net.syscon.elite.api.support;

import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping(value = "/auth/.well-known", produces = MediaType.APPLICATION_JSON_VALUE)
@Profile("dev")
public class StubJwkServer {

    @GetMapping("/jwks.json")
    public ResponseEntity<String> getJwkSet() {
        return new ResponseEntity<>(jwks(), OK);
    }

    private String jwks() {
        return "{\n" +
                "  \"keys\": [\n" +
                "    {\n" +
                "      \"kty\": \"RSA\",\n" +
                "      \"e\": \"AQAB\",\n" +
                "      \"use\": \"sig\",\n" +
                "      \"kid\": \"dps-client-key\",\n" +
                "      \"alg\": \"RS256\",\n" +
                "      \"n\": \"sOPAtsQADdbRu_EH6LP5BM1_mF40VDBn12hJSXPPd5WYK0HLY20VM7AxxR9mnYCF6So1Wt7fGNqUx_WyemBpIJNrs_7Dzwg3uwiQuNh4zKR-EGxWbLwi3yw7lXPUzxUyC5xt88e_7vO-lz1oCnizjh4mxNAms6ZYF7qfnhJE9WvWPwLLkojkZu1JdusLaVowN7GTGNpME8dzeJkam0gp4oxHQGhMN87K6jqX3cEwO6Dvhemg8whs96nzQl8n2LFvAK2up9Prr9Gi2LFgTt7KqXA06kC4Kgw2IR1eFgzcBlTOEwmzjre65HoNaJBr9uNZzV5sILPMczzhQj_fMhz3_Q\"\n" +
                "    }" +
                "  ]\n" +
                "}\n";
    }


}
