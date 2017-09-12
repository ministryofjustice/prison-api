package net.syscon.elite2.swagger.codegen.language;

import java.io.File;

import java.util.UUID;

import org.junit.Test;

import net.syscon.elite2.swagger.codegen.CodegenerationException;
import net.syscon.elite2.swagger.codegen.StandaloneCodegenerator;

public class JaxRsInterfacesGeneratorTest {

    @Test
    public void testGenerationFromJson() throws CodegenerationException {
        StandaloneCodegenerator generator = StandaloneCodegenerator.builder().withApiFile(getApiJsonFile())
                                                                   .forLanguage("jaxrsinterfaces")
                                                                   .withApiPackage("de.zalando.swagger.api")
                                                                   .withModelPackage("de.zalando.swagger.model")
                                                                   .writeResultsTo(generateOutputDir()).build();

        generator.generate();
    }

    protected File getApiJsonFile() {
        return new File(getClass().getResource("/petstore.json").getFile());
    }

    protected File generateOutputDir() {
        File userDir = new File(System.getProperty("user.dir"));
        File outputDirectory = new File(userDir, "/target/" + UUID.randomUUID().toString());
        if (!outputDirectory.mkdirs()) {
            System.out.println("NOT_CREATED at " + outputDirectory.getAbsolutePath());
        }

        System.out.println("OUTPUT TO : " + outputDirectory.getAbsolutePath());
        return outputDirectory;
    }
}
