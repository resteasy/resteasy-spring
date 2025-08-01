package org.jboss.resteasy.test.spring.inmodule.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;

import org.springframework.beans.factory.annotation.Autowired;

@Path("/")
public class SpringBeanProcessorMyResource {

    @Autowired
    SpringBeanProcessorCustomerService springBeanProcessorCustomerService;

    private SpringBeanProcessorCustomer springBeanProcessorCustomer;

    @GET
    @Produces("foo/bar")
    public SpringBeanProcessorCustomer callGet() {
        return springBeanProcessorCustomer;
    }

    @Path("customer-name")
    @GET
    @Produces("foo/bar")
    public SpringBeanProcessorCustomer getCustomer(@QueryParam("name") String name) {
        return springBeanProcessorCustomerService.convert(name);
    }

    @Path("customer-object")
    @GET
    @Produces("text/String")
    public String getName(@QueryParam("customer") SpringBeanProcessorCustomer springBeanProcessorCustomer) {
        return springBeanProcessorCustomerService.convert(springBeanProcessorCustomer);
    }

    public void setSpringBeanProcessorCustomer(SpringBeanProcessorCustomer springBeanProcessorCustomer) {
        this.springBeanProcessorCustomer = springBeanProcessorCustomer;
    }
}
