package org.jboss.resteasy.test.spring.deployment;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;

import org.jboss.resteasy.test.spring.deployment.resource.RequestScopedBeanInnerBean;
import org.jboss.resteasy.test.spring.deployment.resource.RequestScopedBeanInnerBeanImpl;
import org.jboss.resteasy.test.spring.deployment.resource.RequestScopedBeanQualifierInjectorFactoryImpl;
import org.jboss.resteasy.test.spring.deployment.resource.RequestScopedBeanTestBean;
import org.jboss.resteasy.test.spring.deployment.resource.RequestScopedBeanBean;
import org.jboss.resteasy.test.spring.deployment.resource.RequestScopedBeanTestBeanResource;
import org.jboss.resteasy.test.spring.deployment.resource.RequestScopedBeanBeanFactoryBean;
import org.jboss.resteasy.utils.PortProviderUtil;
import org.jboss.resteasy.utils.TestUtilSpring;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
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
 * @tpTestCaseDetails Test Spring request bean and RESTEasy integration
 * @tpSince RESTEasy 3.0.16
 */
@ExtendWith(ArquillianExtension.class)
@RunAsClient
public class RequestScopedBeanDependenciesInDeploymentTest {

    @Deployment
    private static Archive<?> deploy() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class, RequestScopedBeanDependenciesInDeploymentTest.class.getSimpleName() + ".war")
                .addAsWebInfResource(RequestScopedBeanDependenciesInDeploymentTest.class.getPackage(), "web.xml", "web.xml");
        archive.addAsWebInfResource(RequestScopedBeanDependenciesInDeploymentTest.class.getPackage(), "requestScopedBean/spring-request-scope-test-server.xml", "applicationContext.xml");
        archive.addClass(RequestScopedBeanDependenciesInDeploymentTest.class);
        archive.addClass(RequestScopedBeanQualifierInjectorFactoryImpl.class);
        archive.addClass(RequestScopedBeanInnerBean.class);
        archive.addClass(RequestScopedBeanInnerBeanImpl.class);
        archive.addClass(RequestScopedBeanTestBean.class);
        archive.addClass(RequestScopedBeanBean.class);
        archive.addClass(RequestScopedBeanTestBeanResource.class);
        archive.addClass(RequestScopedBeanBeanFactoryBean.class);

        // Permission needed for "arquillian.debug" to run
        // "suppressAccessChecks" required for access to arquillian-core.jar
        // remaining permissions needed to run springframework
        archive.addAsManifestResource(DeploymentDescriptors.createPermissionsXmlAsset(
                new PropertyPermission("org.graalvm.nativeimage.imagecode", "read"),
                new RuntimePermission("getenv.RESTEASY_SERVER_TRACING_THRESHOLD"),
                new RuntimePermission("getenv.resteasy_server_tracing_threshold"),
                new RuntimePermission("getenv.resteasy.server.tracing.threshold"),
                new RuntimePermission("getenv.RESTEASY_SERVER_TRACING_TYPE"),
                new RuntimePermission("getenv.resteasy_server_tracing_type"),
                new RuntimePermission("getenv.resteasy.server.tracing.type"),
                new RuntimePermission("getClassLoader"),
                new PropertyPermission("arquillian.*", "read"),
                new ReflectPermission("suppressAccessChecks"),
                new RuntimePermission("accessDeclaredMembers"),
                new FilePermission("<<ALL FILES>>", "read"),
                new LoggingPermission("control", "")
        ), "permissions.xml");

        TestUtilSpring.addSpringLibraries(archive);
        return archive;
    }

    private String generateURL(String path) {
        return PortProviderUtil.generateURL(path, RequestScopedBeanDependenciesInDeploymentTest.class.getSimpleName());
    }

    /**
     * @tpTestDetails Test request bean defined in xml spring settings
     * @tpSince RESTEasy 3.0.16
     */
    @Test
    public void testBean() throws Exception {
        Client client = ResteasyClientBuilder.newClient();
        WebTarget target = client.target(generateURL("/"));
        Response response = target.request().accept(MediaType.TEXT_PLAIN_TYPE).get();

        String result = response.readEntity(String.class);
        Assertions.assertEquals("configuredValue", result, "Request bean could not be injected");
        client.close();
    }

}
