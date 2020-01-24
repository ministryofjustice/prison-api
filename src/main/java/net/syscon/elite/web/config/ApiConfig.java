package net.syscon.elite.web.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;

@Configuration
@Slf4j
public class ApiConfig implements WebMvcRegistrations {

    private final String apiBasePath;

    public ApiConfig(@Value("${api.base.path:api}") final String apiBasePath) {
        this.apiBasePath = apiBasePath;
    }

    public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
        return new RequestMappingHandlerMapping() {
            @Override
            protected void registerHandlerMethod(final Object handler, final Method method, RequestMappingInfo mapping) {
                final var beanType = method.getDeclaringClass();
                if (AnnotationUtils.findAnnotation(beanType, RestController.class) != null) {
                    final var apiPattern = new PatternsRequestCondition(apiBasePath)
                            .combine(mapping.getPatternsCondition());

                    mapping = new RequestMappingInfo(mapping.getName(), apiPattern,
                            mapping.getMethodsCondition(), mapping.getParamsCondition(),
                            mapping.getHeadersCondition(), mapping.getConsumesCondition(),
                            mapping.getProducesCondition(), mapping.getCustomCondition());
                }

                super.registerHandlerMethod(handler, method, mapping);
            }
        };
    }

}
