package org.jboss.resteasy.test.spring.inmodule.resource;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import org.springframework.stereotype.Component;

@Path("refresh")
@Component
public class ContextRefreshResource {
    @Path("locator/{id}")
    @Produces("text/plain")
    public String locator() {
        return "locator";
    }
}
