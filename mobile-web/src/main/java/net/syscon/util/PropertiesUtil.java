package net.syscon.util;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.PropertiesLoaderUtils;


public class PropertiesUtil {

	public static final Logger LOG = LoggerFactory.getLogger(PropertiesUtil.class);
	
	private PropertiesUtil() {}

	@SuppressWarnings("squid:S1166")
	public static void loadFromClassloader(final Properties props, final String... fileNames) {
		final ClassLoader cl = Thread.currentThread().getContextClassLoader();
		for (final String resource : fileNames) {
			try {
				final Properties prop = PropertiesLoaderUtils.loadAllProperties(resource, cl);
				props.putAll(prop);
			} catch (IOException | RuntimeException ex) {
				LOG.debug("ignoring properties file " + resource + " ...");
			}
		}
	}

	public static Properties getPropertiesFromClassloader(final String... fileNames) {
		final Properties props = new Properties();
		loadFromClassloader(props, fileNames);
		return props;
	}

	private static void loadAllProperties(final PropertySource<?> propertySource, final Properties result) {
		if (propertySource instanceof CompositePropertySource) {
			final CompositePropertySource compositePropertySource = (CompositePropertySource) propertySource;
			compositePropertySource.getPropertySources().forEach(source -> loadAllProperties(source, result));
		} else if (propertySource instanceof EnumerablePropertySource<?>) {
			final EnumerablePropertySource<?> enumerablePropertySource = (EnumerablePropertySource<?>) propertySource;
			for (final String propertyName : enumerablePropertySource.getPropertyNames()) {
				result.put(propertyName, enumerablePropertySource.getProperty(propertyName));
			}
		}
	}

	public static Properties toProperties(final ConfigurableEnvironment env) {
		final Properties props = new Properties();
		for (final PropertySource<?> propertySource : env.getPropertySources()) {
			loadAllProperties(propertySource, props);
		}
		return props;
	}

	public static Properties toProperties(final ConfigurableEnvironment env, final String... prefixFilters) {
		final Properties result = new Properties();
		for (final String prefixFilter : prefixFilters) {
			for (final Map.Entry<Object, Object> entry : toProperties(env).entrySet()) {
				if (entry.getKey().toString().startsWith(prefixFilter)) {
					result.put(entry.getKey(), entry.getValue());
				}
			}
		}
		return result;
	}
}