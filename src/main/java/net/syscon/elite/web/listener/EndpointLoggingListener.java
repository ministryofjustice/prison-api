package net.syscon.elite.web.listener;

import com.fasterxml.classmate.TypeResolver;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class EndpointLoggingListener implements ApplicationEventListener {

	private static final TypeResolver TYPE_RESOLVER = new TypeResolver();

	private final String applicationPath;

	private boolean withOptions = false;
	private boolean withWadl = false;

	public EndpointLoggingListener(final String applicationPath) {
		this.applicationPath = applicationPath;
	}

	@Override
	public void onEvent(final ApplicationEvent event) {
		if (event.getType() == ApplicationEvent.Type.INITIALIZATION_APP_FINISHED) {
            final var resourceModel = event.getResourceModel();
            final var logDetails = new ResourceLogDetails();
			resourceModel.getResources().forEach(resource ->
				logDetails.addEndpointLogLines(getLinesFromResource(resource))
			);
			logDetails.log();
		}
	}

	@Override
	public RequestEventListener onRequest(final RequestEvent requestEvent) {
		return null;
	}

	public EndpointLoggingListener withOptions() {
		this.withOptions = true;
		return this;
	}

	public EndpointLoggingListener withWadl() {
		this.withWadl = true;
		return this;
	}

	private Set<EndpointLogLine> getLinesFromResource(final Resource resource) {
		final Set<EndpointLogLine> logLines = new HashSet<>();
		populate(this.applicationPath, false, resource, logLines);
		return logLines;
	}

	private void populate(final String basePath, final Class<?> klass, final boolean isLocator, final Set<EndpointLogLine> endpointLogLines) {
		populate(basePath, isLocator, Resource.from(klass), endpointLogLines);
	}
	
	private boolean shouldLog(final String basePath, final ResourceMethod method) {
		if (!withOptions && "OPTIONS".equalsIgnoreCase(method.getHttpMethod())) {
			return false;
		}
		return withWadl || !basePath.contains(".wadl");
	}

	private void populate(final String path, final boolean isLocator, final Resource resource, final Set<EndpointLogLine> endpointLogLines) {
        var basePath = path;
		if (!isLocator) {
			basePath = normalizePath(basePath, resource.getPath());
		}

        for (final var method : resource.getResourceMethods()) {
			if (shouldLog(basePath, method)) {
				endpointLogLines.add(new EndpointLogLine(method.getHttpMethod(), basePath, null));
			}
		}

        for (final var childResource : resource.getChildResources()) {
            for (final var method : childResource.getAllMethods()) {
				populateMethodInfo(basePath, endpointLogLines, childResource, method);
			}
		}
	}



	private void populateMethodInfo(final String basePath, final Set<EndpointLogLine> endpointLogLines, final Resource childResource, final ResourceMethod method) {
		if (method.getType() == ResourceMethod.JaxrsType.RESOURCE_METHOD) {
            final var path = normalizePath(basePath, childResource.getPath());
			if (!withOptions &&  "OPTIONS".equalsIgnoreCase(method.getHttpMethod())) {
				return;
			}
			if (!withWadl && path.contains(".wadl")) {
				return;
			}
			endpointLogLines.add(new EndpointLogLine(method.getHttpMethod(), path, null));
		} else if (method.getType() == ResourceMethod.JaxrsType.SUB_RESOURCE_LOCATOR) {
            final var path = normalizePath(basePath, childResource.getPath());
            final var responseType = TYPE_RESOLVER
					.resolve(method.getInvocable().getResponseType());
            final var erasedType = !responseType.getTypeBindings().isEmpty()
					? responseType.getTypeBindings().getBoundType(0).getErasedType()
					: responseType.getErasedType();
			populate(path, erasedType, true, endpointLogLines);
		}
	}

	private static String normalizePath(final String basePath, final String path) {
		if (path == null) {
			return basePath;
		}
		if (basePath.endsWith("/")) {
			return path.startsWith("/") ? basePath + path.substring(1) : basePath + path;
		}
		return path.startsWith("/") ? basePath + path : basePath + "/" + path;
	}

	private static class ResourceLogDetails {
		private static final Logger logger = LoggerFactory.getLogger(ResourceLogDetails.class);
		private static final Comparator<EndpointLogLine> COMPARATOR = Comparator.comparing((final EndpointLogLine e) -> e.path).thenComparing((final EndpointLogLine e) -> e.httpMethod);
		private final Set<EndpointLogLine> logLines = new TreeSet<>(COMPARATOR);
		private void log() {
            final var sb = new StringBuilder("\nAll endpoints for Jersey application\n");
			logLines.forEach(line -> sb.append(line).append("\n"));
			logger.info(sb.toString());
		}
		private void addEndpointLogLines(final Set<EndpointLogLine> logLines) {
			this.logLines.addAll(logLines);
		}
	}

	private static class EndpointLogLine {

		private static final String DEFAULT_FORMAT = "   %-7s %s";
		private final String httpMethod;
		private final String path;
		private final String format;

		private EndpointLogLine(final String httpMethod, final String path, final String format) {
			this.httpMethod = httpMethod;
			this.path = path;
			this.format = format == null ? DEFAULT_FORMAT : format;
		}

		@Override
		public String toString() {
			return String.format(format, httpMethod, path);
		}
	}
}
