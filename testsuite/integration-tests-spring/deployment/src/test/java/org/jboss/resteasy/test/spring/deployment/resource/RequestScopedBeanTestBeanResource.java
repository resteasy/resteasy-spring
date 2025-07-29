package org.jboss.resteasy.test.spring.deployment.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.springframework.beans.factory.annotation.Qualifier;

@Path("/")
public class RequestScopedBeanTestBeanResource {
    @GET
    public String test(@Qualifier("testBean") RequestScopedBeanTestBean bean) {
        return bean.configured;
    }
}
