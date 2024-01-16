package org.jboss.resteasy.springmvc.test.client;

import io.undertow.servlet.api.DeploymentInfo;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.jboss.resteasy.plugins.server.undertow.spring.UndertowJaxrsSpringServer;
import org.jboss.resteasy.test.TestPortProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BasicSpringTest {

   UndertowJaxrsSpringServer server;
   ResteasyClient client;

   @BeforeEach
   public void before() throws Exception {
      server = new UndertowJaxrsSpringServer();

      server.start();

      DeploymentInfo deployment = server.undertowDeployment("classpath:spring-servlet.xml", null);
      deployment.setDeploymentName(BasicSpringTest.class.getName());
      deployment.setContextPath("/");
      deployment.setClassLoader(BasicSpringTest.class.getClassLoader());

      server.deploy(deployment);

      client = new ResteasyClientBuilderImpl().build();
   }

   @AfterEach
   public void after() {
      server.stop();
      client.close();
   }

   @Test
   public void testBasic() throws IOException {

      ResteasyWebTarget target = client.target(TestPortProvider.generateURL("/"));

      BasicResource br = target.proxy(BasicResource.class);
      assertEquals("org/jboss/resteasy/springmvc/test", br.getBasicString());

      assertEquals("something", br.getBasicObject().getSomething());

      assertEquals("Hi, I'm custom!", br.getSpringMvcValue());

      assertEquals(1, br.getSingletonCount().intValue());
      assertEquals(2, br.getSingletonCount().intValue());

      assertEquals(1, br.getPrototypeCount().intValue());
      assertEquals(1, br.getPrototypeCount().intValue());

      assertEquals("text/plain", br.getContentTypeHeader());

      Integer interceptorCount = br
            .getSpringInterceptorCount("afterCompletion");

      assertEquals(Integer.valueOf(8), interceptorCount);
      assertEquals("text/plain", br.getContentTypeHeader());
      assertEquals("springSomething", br.testSpringXml().getSomething());
   }
}
