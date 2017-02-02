package net.syscon.elite.web.config;

import javax.servlet.ServletContextListener;

import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.jboss.resteasy.plugins.server.servlet.ResteasyBootstrap;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import net.syscon.elite.web.api.resource.AgenciesResource;
import net.syscon.elite.web.api.resource.impl.AgenciesResourceImpl;

@Configuration
@EnableWebMvc
@EnableScheduling
@EnableCaching
@EnableAsync
@Import({
		PersistenceConfigs.class,
		//SpringSecurityConfigs.class,
		ServiceConfigs.class})
public class ServletContextConfigs {
	
	@Bean
	public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
		final ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		final MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(mapper);
		return converter;
	}
	
    @Bean
    // RestEasy configuration
    public ServletContextInitializer initializer() {
        return servletContext -> {
            servletContext.setInitParameter("resteasy.scan", "true");
            servletContext.setInitParameter("resteasy.servlet.mapping.prefix", "/services");
        };
    }

    @Bean
    public ServletContextListener restEasyBootstrap() {
            return new ResteasyBootstrap();
    }

    @Bean
    public ServletRegistrationBean restEasyServlet() {
        final ServletRegistrationBean registrationBean = new ServletRegistrationBean();
        registrationBean.setServlet(new HttpServletDispatcher());
        registrationBean.setName("rest-easy-servlet");
        registrationBean.addUrlMappings("/api/*");
        return registrationBean;
    }
	
	
	@Bean
	public AgenciesResource agenciesResource() {
		return new AgenciesResourceImpl();
	}
	

	

}
