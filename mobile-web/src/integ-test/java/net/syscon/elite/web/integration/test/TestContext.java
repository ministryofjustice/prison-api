package net.syscon.elite.web.integration.test;

import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class TestContext {

  @Bean
  public EmbeddedServletContainerFactory servletContainer() {
	JettyEmbeddedServletContainerFactory factory = new JettyEmbeddedServletContainerFactory();
    factory.setPort(TestApiConfig.PORT);
    factory.setSessionTimeout(10, TimeUnit.MINUTES);
    return factory;
  }

}
