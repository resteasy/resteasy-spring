package org.jboss.resteasy.test.spring.inmodule.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/scanned")
public class SpringBeanProcessorScannedResource {
    @GET
    public String callGet() {
        return "Hello";
    }
}
