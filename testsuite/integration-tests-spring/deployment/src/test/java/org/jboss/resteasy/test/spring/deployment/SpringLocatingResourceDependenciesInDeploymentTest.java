package org.jboss.resteasy.test.spring.deployment;

import java.io.FilePermission;
import java.lang.reflect.ReflectPermission;
import java.util.PropertyPermission;
import java.util.logging.LoggingPermission;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.resteasy.spi.HttpResponseCodes;
import org.jboss.resteasy.test.spring.deployment.resource.SpringLocatingLocatingResource;
import org.jboss.resteasy.test.spring.deployment.resource.SpringLocatingSimpleResource;
import org.jboss.resteasy.utils.PortProviderUtil;
import org.jboss.resteasy.utils.TestUtilSpring;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.wildfly.testing.tools.deployments.DeploymentDescriptors;

/**
 * @tpSubChapter Spring
 * @tpChapter Integration tests - dependencies included in deployment
 * @tpSince RESTEasy 3.0.16
 */
@ExtendWith(ArquillianExtension.class)
@RunAsClient
public class SpringLocatingResourceDependenciesInDeploymentTest {

    static Client client;

    @Deployment
    private static Archive<?> deploy() {
        WebArchive archive = ShrinkWrap
                .create(WebArchive.class, SpringLocatingResourceDependenciesInDeploymentTest.class.getSimpleName() + ".war")
                .addAsWebInfResource(SpringLocatingResourceDependenciesInDeploymentTest.class.getPackage(), "web.xml",
                        "web.xml");
        archive.addAsWebInfResource(SpringLocatingResourceDependenciesInDeploymentTest.class.getPackage(),
                "springLocatingResource/applicationContext.xml", "applicationContext.xml");
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
                new LoggingPermission("control", "")), "permissions.xml");

        TestUtilSpring.addSpringLibraries(archive);
        return archive;
    }

    @BeforeEach
    public void init() {
        client = ClientBuilder.newClient();
    }

    @AfterEach
    public void after() throws Exception {
        client.close();
    }

    private String generateURL(String path) {
        return PortProviderUtil.generateURL(path, SpringLocatingResourceDependenciesInDeploymentTest.class.getSimpleName());
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
            Assertions.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
            Assertions.assertEquals("basic", response.readEntity(String.class), "Unexpected response from the server");
        }
        {
            WebTarget target = client.target(generateURL("/basic"));
            Response response = target.request().put(Entity.text("basic"));
            Assertions.assertEquals(HttpResponseCodes.SC_NO_CONTENT, response.getStatus());

        }
        {
            WebTarget target = client.target(generateURL("/queryParam"));
            Response response = target.queryParam("param", "hello world").request().get();
            Assertions.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
            Assertions.assertEquals("hello world", response.readEntity(String.class), "Unexpected response from the server");
        }
        {
            WebTarget target = client.target(generateURL("/uriParam/1234"));
            Response response = target.request().get();
            Assertions.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
            Assertions.assertEquals("1234", response.readEntity(String.class));
        }

    }

    /**
     * @tpTestDetails Test resource bean defined in xml spring settings, resource calls another resource also
     *                defined as resource bean
     * @tpSince RESTEasy 3.0.16
     */
    @Test
    public void testLocatingResource() throws Exception {

        {
            WebTarget target = client.target(generateURL("/locating/basic"));
            Response response = target.request().get();
            Assertions.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
            Assertions.assertEquals("basic", response.readEntity(String.class), "Unexpected response from the server");
        }
        {
            WebTarget target = client.target(generateURL("/locating/basic"));
            Response response = target.request().put(Entity.text("basic"));
            Assertions.assertEquals(HttpResponseCodes.SC_NO_CONTENT, response.getStatus());
        }
        {
            WebTarget target = client.target(generateURL("/locating/queryParam"));
            Response response = target.queryParam("param", "hello world").request().get();
            Assertions.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
            Assertions.assertEquals("hello world", response.readEntity(String.class), "Unexpected response from the server");
        }
        {
            WebTarget target = client.target(generateURL("/locating/uriParam/1234"));
            Response response = target.request().get();
            Assertions.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
            Assertions.assertEquals("1234", response.readEntity(String.class));
        }
    }
}
