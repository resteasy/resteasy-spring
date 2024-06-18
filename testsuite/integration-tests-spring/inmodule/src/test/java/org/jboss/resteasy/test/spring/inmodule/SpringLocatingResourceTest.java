package org.jboss.resteasy.test.spring.inmodule;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.resteasy.test.spring.inmodule.resource.SpringLocatingLocatingResource;
import org.jboss.resteasy.test.spring.inmodule.resource.SpringLocatingSimpleResource;
import org.jboss.resteasy.spi.HttpResponseCodes;
import org.jboss.resteasy.utils.PortProviderUtil;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import org.wildfly.testing.tools.deployments.DeploymentDescriptors;

import java.io.FilePermission;
import java.lang.reflect.ReflectPermission;
import java.util.PropertyPermission;
import java.util.logging.LoggingPermission;

/**
 * @tpSubChapter Spring
 * @tpChapter Integration tests
 * @tpSince RESTEasy 3.0.16
 */
@RunWith(Arquillian.class)
@RunAsClient
public class SpringLocatingResourceTest {

   static Client client;

   @Deployment
   private static Archive<?> deploy() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, SpringLocatingResourceTest.class.getSimpleName() + ".war")
            .addAsWebInfResource(SpringLocatingResourceTest.class.getPackage(), "springLocatingResource/web.xml", "web.xml");
      archive.addAsWebInfResource(SpringLocatingResourceTest.class.getPackage(),
            "springLocatingResource/applicationContext.xml", "applicationContext.xml");
      archive.addAsManifestResource(new StringAsset("Dependencies: org.springframework.spring meta-inf\n"), "MANIFEST.MF");
      archive.addClass(SpringLocatingLocatingResource.class);
      archive.addClass(SpringLocatingSimpleResource.class);

      // Permission needed for "arquillian.debug" to run
      // "suppressAccessChecks" required for access to arquillian-core.jar
      // remaining permissions needed to run springframework
      archive.addAsManifestResource(DeploymentDescriptors.createPermissionsXmlAsset(
         new PropertyPermission("arquillian.*", "read"),
         new ReflectPermission("suppressAccessChecks"),
         new RuntimePermission("accessDeclaredMembers"),
         new FilePermission("<<ALL FILES>>", "read"),
         new LoggingPermission("control", "")
      ), "permissions.xml");

      return archive;
   }

   @Before
   public void init() {
      client = ClientBuilder.newClient();
   }

   @After
   public void after() throws Exception {
      client.close();
   }

   private String generateURL(String path) {
      return PortProviderUtil.generateURL(path, SpringLocatingResourceTest.class.getSimpleName());
   }

   /**
    * @tpTestDetails Test resource bean defined in xml spring settings
    * @tpSince RESTEasy 3.0.16
    */
   @Test
   public void testNoDefaultsResource() throws Exception {

      {
         WebTarget target = client.target(generateURL("/basic"));
         Response response = target.request().get();
         Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
         Assert.assertEquals("Unexpected response from the server", "basic", response.readEntity(String.class));
      }
      {
         WebTarget target = client.target(generateURL("/basic"));
         Response response = target.request().put(Entity.text("basic"));
         Assert.assertEquals(HttpResponseCodes.SC_NO_CONTENT, response.getStatus());

      }
      {
         WebTarget target = client.target(generateURL("/queryParam"));
         Response response = target.queryParam("param", "hello world").request().get();
         Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
         Assert.assertEquals("Unexpected response from the server", "hello world", response.readEntity(String.class));
      }
      {
         WebTarget target = client.target(generateURL("/uriParam/1234"));
         Response response = target.request().get();
         Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
         Assert.assertEquals("1234", response.readEntity(String.class));
      }

   }

   /**
    * @tpTestDetails Test resource bean defined in xml spring settings, resource calls another resource also
    * defined as resource bean
    * @tpSince RESTEasy 3.0.16
    */
   @Test
   public void testLocatingResource() throws Exception {

      {
         WebTarget target = client.target(generateURL("/locating/basic"));
         Response response = target.request().get();
         Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
         Assert.assertEquals("Unexpected response from the server", "basic", response.readEntity(String.class));
      }
      {
         WebTarget target = client.target(generateURL("/locating/basic"));
         Response response = target.request().put(Entity.text("basic"));
         Assert.assertEquals(HttpResponseCodes.SC_NO_CONTENT, response.getStatus());
      }
      {
         WebTarget target = client.target(generateURL("/locating/queryParam"));
         Response response = target.queryParam("param", "hello world").request().get();
         Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
         Assert.assertEquals("Unexpected response from the server", "hello world", response.readEntity(String.class));
      }
      {
         WebTarget target = client.target(generateURL("/locating/uriParam/1234"));
         Response response = target.request().get();
         Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
         Assert.assertEquals("1234", response.readEntity(String.class));
      }
   }
}
