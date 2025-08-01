package org.jboss.resteasy.test.spring.deployment.resource;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;

import org.springframework.beans.factory.annotation.Autowired;

@Provider
public class SpringBeanProcessorCustomerParamConverterProvider implements ParamConverterProvider {

    // this isn't a complex service, but it provides a test to confirm that
    // RESTEasy doesn't muck up the @Autowired annotation handling in the Spring
    // life-cycle
    @Autowired
    SpringBeanProcessorCustomerService service;

    @SuppressWarnings("unchecked")
    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
        if (!SpringBeanProcessorCustomerParamConverter.class.equals(rawType))
            return null;
        return (ParamConverter<T>) new SpringBeanProcessorCustomerParamConverter();
    }
}
