package org.jboss.resteasy.test.spring.deployment;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.test.spring.deployment.resource.JavaConfigBeanConfiguration;
import org.jboss.resteasy.test.spring.deployment.resource.JavaConfigResource;
import org.jboss.resteasy.test.spring.deployment.resource.JavaConfigService;
import org.jboss.resteasy.spi.HttpResponseCodes;
import org.jboss.resteasy.utils.PortProviderUtil;
import org.jboss.resteasy.utils.TestUtilSpring;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.wildfly.testing.tools.deployments.DeploymentDescriptors;

import java.io.FilePermission;
import java.lang.reflect.ReflectPermission;
import java.util.PropertyPermission;
import java.util.logging.LoggingPermission;

/**
 * @tpSubChapter Spring
 * @tpChapter Integration tests - dependencies included in deployment
 * @tpTestCaseDetails This test will verify that the resource invoked by RESTEasy has been
 * initialized by spring when defined using spring's JavaConfig.
 * @tpSince RESTEasy 3.0.16
 */
@ExtendWith(ArquillianExtension.class)
@RunAsClient
public class JavaConfigDependenciesInDeploymentTest {

    private static final String PATH = "/invoke";

    private String generateURL(String path) {
        return PortProviderUtil.generateURL(path, JavaConfigDependenciesInDeploymentTest.class.getSimpleName());
    }

    @Deployment
    private static Archive<?> deploy() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class, JavaConfigDependenciesInDeploymentTest.class.getSimpleName() + ".war")
                .addClass(JavaConfigResource.class)
                .addClass(JavaConfigService.class)
                .addClass(JavaConfigBeanConfiguration.class)
                .addAsWebInfResource(JavaConfigDependenciesInDeploymentTest.class.getPackage(), "javaConfig/web.xml", "web.xml");

        // Permission needed for "arquillian.debug" to run
        // "suppressAccessChecks" required for access to arquillian-core.jar
        // remaining permissions needed to run springframework
        archive.addAsManifestResource(DeploymentDescriptors.createPermissionsXmlAsset(
                new PropertyPermission("arquillian.*", "read"),
                new PropertyPermission("org.springframework.cglib.test.stressHashCodes", "read"),
                new PropertyPermission("cglib.debugLocation", "read"),
                new RuntimePermission("accessClassInPackage.sun.reflect.annotation"),
                new RuntimePermission("getProtectionDomain"),
                new ReflectPermission("suppressAccessChecks"),
                new RuntimePermission("accessDeclaredMembers"),
                new FilePermission("<<ALL FILES>>", "read"),
                new LoggingPermission("control", "")
        ), "permissions.xml");

        TestUtilSpring.addSpringLibraries(archive);
        return archive;
    }

    /**
     * @tpTestDetails This test will verify that the resource invoked by RESTEasy has been
     * initialized by spring when defined using spring's JavaConfig.
     * @tpSince RESTEasy 3.0.16
     */
    @Test
    public void test() throws Exception {
        Client client = ResteasyClientBuilder.newClient();
        WebTarget target = client.target(generateURL(PATH));
        Response response = target.request().get();
        Assertions.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
        Assertions.assertEquals("hello", response.readEntity(String.class), "Unexpected response");
        client.close();
    }
}
