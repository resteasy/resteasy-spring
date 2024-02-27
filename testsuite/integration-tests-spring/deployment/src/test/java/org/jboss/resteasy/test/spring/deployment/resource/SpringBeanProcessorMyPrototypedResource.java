package org.jboss.resteasy.test.spring.deployment.resource;

import org.jboss.logging.Logger;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import org.junit.jupiter.api.Assertions;

@Path("/prototyped")
public class SpringBeanProcessorMyPrototypedResource {
    private SpringBeanProcessorCustomer springBeanProcessorCustomer;

    private int counter = 0;
    private static Logger logger = Logger.getLogger(SpringBeanProcessorMyPrototypedResource.class);

    public SpringBeanProcessorMyPrototypedResource() {
        logger.info("here");
    }

    @GET
    @Path("{id}")
    @Produces("text/plain")
    public String callGet(@PathParam("id") String id) {
        Assertions.assertEquals("1", id, "Got unexpected value");
        return springBeanProcessorCustomer.getName() + (counter++);
    }

    public SpringBeanProcessorCustomer getSpringBeanProcessorCustomer() {
        return springBeanProcessorCustomer;
    }

    public void setSpringBeanProcessorCustomer(SpringBeanProcessorCustomer springBeanProcessorCustomer) {
        this.springBeanProcessorCustomer = springBeanProcessorCustomer;
    }

}
