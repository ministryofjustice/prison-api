package net.syscon.elite.web.config;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;



@Order(1)
@Configuration
public class WebAppInitializer implements WebApplicationInitializer  {

	private void setActiveProfile(ConfigurableEnvironment env) {
		String activeProfile = System.getProperty("spring.profiles.active");
		if (activeProfile == null) {
			activeProfile = "dev";
		}
		env.setActiveProfiles(activeProfile);
	}
	
	
	@Override
	public void onStartup(ServletContext container) throws ServletException {

	      // Create the 'root' Spring application context
	      final AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
	      rootContext.register(ApplicationContextConfigs.class, SpringSecurityConfigs.class);
	      setActiveProfile(rootContext.getEnvironment());

	      // Manage the lifecycle of the root application context
	      container.addListener(new ContextLoaderListener(rootContext));

	      // Create the dispatcher servlet's Spring application context
	      final AnnotationConfigWebApplicationContext dispatcherContext = new AnnotationConfigWebApplicationContext();
	      setActiveProfile(dispatcherContext.getEnvironment());
	      dispatcherContext.register(ServletContextConfigs.class);

	      // Register and map the dispatcher servlet
	      final ServletRegistration.Dynamic dispatcher = container.addServlet("dispatcher", new DispatcherServlet(dispatcherContext));
	      dispatcher.setLoadOnStartup(1);
	      dispatcher.addMapping("/");


	}

	

}