package net.syscon.util;

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

	public static void loadFromClassloader(Properties props, String... fileNames) {
		final ClassLoader cl = Thread.currentThread().getContextClassLoader();
		for (String resource : fileNames) {
			try {
				Properties prop = PropertiesLoaderUtils.loadAllProperties(resource, cl);
				props.putAll(prop);
			} catch (Throwable ex) {
				LOG.debug("ignoring net.wisecoding.util.config file " + resource + " ...");
			}
		}
	}

	public static Properties getPropertiesFromClassloader(String... fileNames) {
		final Properties props = new Properties();
		loadFromClassloader(props, fileNames);
		return props;
	}

	private static void loadAllProperties(PropertySource<?> propertySource, Properties result) {
		if (propertySource instanceof CompositePropertySource) {
			CompositePropertySource compositePropertySource = (CompositePropertySource) propertySource;
			compositePropertySource.getPropertySources().forEach(source -> loadAllProperties(source, result));
		} else if (propertySource instanceof EnumerablePropertySource<?>) {
			EnumerablePropertySource<?> enumerablePropertySource = (EnumerablePropertySource<?>) propertySource;
			for (String propertyName : enumerablePropertySource.getPropertyNames()) {
				result.put(propertyName, enumerablePropertySource.getProperty(propertyName));
			}
		}
	}

	public static Properties toProperties(ConfigurableEnvironment env) {
		final Properties props = new Properties();
		for (PropertySource<?> propertySource : env.getPropertySources()) {
			loadAllProperties(propertySource, props);
		}
		return props;
	}

	public static Properties toProperties(ConfigurableEnvironment env, String... prefixFilters) {
		final Properties result = new Properties();
		for (String prefixFilter : prefixFilters) {
			for (Map.Entry<Object, Object> entry : toProperties(env).entrySet()) {
				if (entry.getKey().toString().startsWith(prefixFilter)) {
					result.put(entry.getKey(), entry.getValue());
				}
			}
		}
		return result;
	}
}