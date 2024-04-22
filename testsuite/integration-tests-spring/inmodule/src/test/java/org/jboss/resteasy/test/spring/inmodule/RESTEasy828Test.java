package org.jboss.resteasy.test.spring.inmodule;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.resteasy.spi.HttpResponseCodes;
import org.jboss.resteasy.test.spring.inmodule.resource.RESTEasy828Resource;

import org.jboss.resteasy.utils.PortProviderUtil;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.wildfly.testing.tools.deployments.DeploymentDescriptors;

import java.io.FilePermission;
import java.lang.reflect.ReflectPermission;
import java.util.PropertyPermission;
import java.util.logging.LoggingPermission;

@ExtendWith(ArquillianExtension.class)
@RunAsClient
public class RESTEasy828Test {

   static Client client;

   private String generateURL(String path) {
      return PortProviderUtil.generateURL(path, RESTEasy828Test.class.getSimpleName());
   }

   @BeforeEach
   public void init() {
      client = ClientBuilder.newClient();
   }

   @AfterEach
   public void after() {
      client.close();
   }

   @Deployment
   public static Archive<?> deploy() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, RESTEasy828Test.class.getSimpleName() + ".war")
              .addAsWebInfResource(RESTEasy828Test.class.getPackage(), "resteasy828/web.xml", "web.xml");
      archive.addAsWebInfResource(RESTEasy828Test.class.getPackage(),
              "resteasy828/applicationContext.xml", "applicationContext.xml");

      archive.addClass(RESTEasy828Resource.class);

      archive.addAsLibraries(Maven
              .resolver()
              .loadPomFromFile("pom.xml")
              .resolve("org.springframework:spring-webmvc")
              .withTransitivity()
              .asFile());

      archive.addAsLibraries(Maven
              .resolver()
              .loadPomFromFile("pom.xml")
              .resolve("org.jboss.resteasy.spring:resteasy-spring")
              .withTransitivity()
              .asFile());

      archive.addAsLibraries(Maven
              .resolver()
              .loadPomFromFile("pom.xml")
              .resolve("org.jboss.resteasy:resteasy-core")
              .withTransitivity()
              .asFile());

      archive.addAsLibraries(Maven
              .resolver()
              .loadPomFromFile("pom.xml")
              .resolve("org.jboss.resteasy:resteasy-core-spi")
              .withTransitivity()
              .asFile());


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

   @Test
   public void testResteasy828() throws InterruptedException {
      WebTarget target = client.target(generateURL("/resteasy828"));
      Response response = target.request().get();
      Assertions.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
      Assertions.assertNotNull(target.request().get(String.class));
   }
}
