package net.syscon.elite.web.api.resource.impl;

import net.syscon.elite.web.listener.EndpointLoggingListener;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spring.scope.RequestContextFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;
import java.util.Set;

@Component
@ApplicationPath("/api")
public class JerseyConfig extends ResourceConfig {


	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Inject
	public JerseyConfig(ConfigurableEnvironment env) {
		final ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
		provider.addIncludeFilter(new AnnotationTypeFilter(Component.class));
		provider.addIncludeFilter(new AnnotationTypeFilter(Service.class));
		final Set<BeanDefinition> resources = provider.findCandidateComponents(AgenciesResourceImpl.class.getPackage().getName());
		for (final BeanDefinition beanDef : resources) {
			if (!beanDef.getBeanClassName().equals(this.getClass().getName())) {
				try {
					final Class<?> clazz = Class.forName(beanDef.getBeanClassName());
					register(clazz);
				} catch (final Exception ex) {
					log.warn(ex.getMessage(), ex);
				}
			}
		}

		final String contextPath = env.getProperty("server.contextPath");
		register(new EndpointLoggingListener(contextPath));
		register(RequestContextFilter.class);
		register(LoggingFeature.class);
	}

}


