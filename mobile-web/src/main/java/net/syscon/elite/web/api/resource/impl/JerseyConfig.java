package net.syscon.elite.web.api.resource.impl;

import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;

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

import net.syscon.elite.web.listener.EndpointLoggingListener;

@Component
@ApplicationPath("/api")
public class JerseyConfig extends ResourceConfig {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Inject
	public JerseyConfig(final ConfigurableEnvironment env) {
		final ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
		provider.addIncludeFilter(new AnnotationTypeFilter(Component.class));
		provider.addIncludeFilter(new AnnotationTypeFilter(Service.class));
		final Set<BeanDefinition> resources = provider.findCandidateComponents(AgenciesResourceImpl.class.getPackage().getName());
		final String thisClassName = getClass().getName();
		for (final BeanDefinition beanDef : resources) {
			final String beanClassName = beanDef.getBeanClassName();
			if (!beanClassName.equals(thisClassName)) {
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


