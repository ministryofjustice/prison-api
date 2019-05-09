package net.syscon.elite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "net.syscon")
@EnableScheduling
public class Elite2ApiServer {
    public static void main(final String[] args) {
        SpringApplication.run(Elite2ApiServer.class, args);
    }
}
