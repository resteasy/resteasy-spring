package org.jboss.resteasy.test.spring.inmodule;

import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.lang.reflect.ReflectPermission;
import java.nio.charset.StandardCharsets;
import java.util.logging.LoggingPermission;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.resteasy.plugins.spring.SpringContextLoaderListener;
import org.jboss.resteasy.test.spring.inmodule.resource.TestResource;
import org.jboss.resteasy.utils.PortProviderUtil;
import org.jboss.resteasy.utils.TestUtil;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.ApplicationContext;
import org.wildfly.testing.tools.deployments.DeploymentDescriptors;

/**
 * @tpSubChapter Spring
 * @tpChapter Integration tests
 * @tpTestCaseDetails Tests adding spring as resource root with dependency for server module
 * @tpSince RESTEasy 3.0.16
 */
@ExtendWith(ArquillianExtension.class)
@RunAsClient
public class AddSpringResteasyAsResourceRootInModuleTest {

    private CloseableHttpClient client;

    private String deploymentName;
    private static String deploymentWithoutSpringMvcDispatcherOrListenerSpringInModule = "deploymentWithoutSpringMvcDispatcherOrListenerSpringInModule";
    private static String deploymentWithSpringContextLoaderListenerSpringInModule = "deploymentWithSpringContextLoaderListenerSpringInModule";
    private static String deploymentWithSpringMvcDispatcherSpringInModule = "deploymentWithSpringMvcDispatcherSpringInModule";

    @BeforeEach
    public void before() throws Exception {
        client = HttpClients.createDefault();
    }

    /**
     * @tpTestDetails Add dependency to spring libraries. Spring libraries are available as server module.
     *                Deployment is configured with SpringMVC Dispatcher Servlet.
     * @tpPassCrit The application is successfully deployed, resource is available, and Spring classes are on the path.
     * @tpSince RESTEasy 3.0.16
     */
    @Test
    @OperateOnDeployment("dep3")
    public void testDeploymentWithSpringMvcDispatcherAddsResourceRoot() throws Exception {
        deploymentName = deploymentWithSpringMvcDispatcherSpringInModule;

        assertResponse(deploymentName);
        Assertions.assertTrue(springClassesAreAvailableToDeployment(deploymentName),
                "Spring classes are not available in deployment");
        Assertions.assertTrue(resteasySpringClassesAreAvailableToDeployment(deploymentName),
                "Resteasy Spring classes are not available in deployment");
    }

    @Deployment(name = "dep3")
    private static WebArchive createDeploymentWithSpringMvcDispatcher() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class, deploymentWithSpringMvcDispatcherSpringInModule + ".war")
                .addClass(TestResource.class)
                .addAsWebInfResource(AddSpringResteasyAsResourceRootInModuleTest.class.getPackage(),
                        "mvc-dispatcher-servlet/web.xml", "web.xml")
                .addAsWebInfResource(AddSpringResteasyAsResourceRootInModuleTest.class.getPackage(),
                        "mvc-dispatcher-servlet/mvc-dispatcher-servlet.xml", "mvc-dispatcher-servlet.xml")
                .addAsWebInfResource(AddSpringResteasyAsResourceRootInModuleTest.class.getPackage(),
                        "mvc-dispatcher-servlet/applicationContext.xml", "applicationContext.xml");

        addSpringLibraries(archive);
        archive.as(ZipExporter.class).exportTo(new File("target", deploymentWithSpringMvcDispatcherSpringInModule + ".war"),
                true);
        return archive;
    }

    /**
     * @tpTestDetails Add dependency to spring libraries. Spring libraries are available as server module.
     *                Deployment is configured with Spring context loader listener.
     * @tpPassCrit The application is successfully deployed, resource is available, and Spring classes are on the path.
     * @tpSince RESTEasy 3.0.16
     */
    @Test
    @OperateOnDeployment("dep2")
    public void testDeploymentWithSpringContextLoaderListenerAddsResourceRoot() throws Exception {
        deploymentName = deploymentWithSpringContextLoaderListenerSpringInModule;

        assertResponse(deploymentName);
        Assertions.assertTrue(springClassesAreAvailableToDeployment(deploymentName),
                "Spring classes are not available in deployment");
        Assertions.assertTrue(resteasySpringClassesAreAvailableToDeployment(deploymentName),
                "Resteasy Spring classes are not available in deployment");
    }

    @Deployment(name = "dep2")
    private static Archive<?> createDeploymentWithSpringContextLoaderListener() {
        WebArchive archive = ShrinkWrap
                .create(WebArchive.class, deploymentWithSpringContextLoaderListenerSpringInModule + ".war")
                .addClass(TestResource.class)
                .addAsWebInfResource(AddSpringResteasyAsResourceRootInModuleTest.class.getPackage(), "web.xml", "web.xml")
                .addAsWebInfResource(AddSpringResteasyAsResourceRootInModuleTest.class.getPackage(), "applicationContext.xml",
                        "applicationContext.xml");
        addSpringLibraries(archive);

        archive.addAsManifestResource(DeploymentDescriptors.createPermissionsXmlAsset(
                new ReflectPermission("suppressAccessChecks"),
                new RuntimePermission("accessDeclaredMembers"),
                new RuntimePermission("getClassLoader"),
                new FilePermission("<<ALL FILES>>", "read"),
                new LoggingPermission("control", "")), "permissions.xml");

        archive.as(ZipExporter.class)
                .exportTo(new File("target", deploymentWithSpringContextLoaderListenerSpringInModule + ".war"), true);
        return archive;
    }

    /**
     * @tpTestDetails Add dependency to spring libraries. Spring libraries are available as server module.
     *                Deployment is configured without Spring context loader listener or MVC dispatcher.
     * @tpPassCrit The application is successfully deployed, resource is available, and Spring classes are on the path.
     * @tpSince RESTEasy 3.0.16
     */
    @Test
    @OperateOnDeployment("dep1")
    public void testDeploymentWithoutSpringMvcDispatcherOrListenerDoesNotAddResourceRoot() throws Exception {
        deploymentName = deploymentWithoutSpringMvcDispatcherOrListenerSpringInModule;

        assertResponse(deploymentName);
        Assertions.assertTrue(springClassesAreAvailableToDeployment(deploymentName),
                "Spring classes are not available in deployment");
        Assertions.assertFalse(
                resteasySpringClassesAreAvailableToDeployment(deploymentName),
                "Resteasy Spring classes are available in deployment, which is not expected");
    }

    @Deployment(name = "dep1")
    private static Archive<?> createDeploymentWithoutSpringMvcDispatcherOrListener() {
        WebArchive archive = TestUtil.prepareArchive(deploymentWithoutSpringMvcDispatcherOrListenerSpringInModule);
        archive.addAsWebInfResource(AddSpringResteasyAsResourceRootInModuleTest.class.getPackage(),
                "web-no-mvc-no-listener.xml", "web.xml")
                .addAsWebInfResource(AddSpringResteasyAsResourceRootInModuleTest.class.getPackage(), "applicationContext.xml",
                        "applicationContext.xml");
        addSpringLibraries(archive);
        archive.as(ZipExporter.class)
                .exportTo(new File("target", deploymentWithoutSpringMvcDispatcherOrListenerSpringInModule + ".war"), true);
        return TestUtil.finishContainerPrepare(archive, null, TestResource.class);
    }

    private boolean resteasySpringClassesAreAvailableToDeployment(String deploymentName) throws IOException, HttpException {

        return isClassAvailableToDeployment(deploymentName, SpringContextLoaderListener.class);
    }

    private boolean isClassAvailableToDeployment(String deploymentName, Class<?> clazz) throws IOException,
            HttpException {
        String className = clazz.getName();
        String CONTEXT_URL = PortProviderUtil.generateURL("/", deploymentName);
        HttpGet httpget = new HttpGet(
                CONTEXT_URL + TestResource.LOAD_CLASS_PATH + "?" + TestResource.CLASSNAME_PARAM + "=" + className);
        try {
            String responseString = new String();
            HttpClientContext context = HttpClientContext.create();
            CloseableHttpResponse response = client.execute(httpget, context);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                responseString = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            }
            return (HttpStatus.SC_OK == response.getStatusLine().getStatusCode())
                    && (className.equals(responseString));
        } finally {
            httpget.releaseConnection();
        }
    }

    private boolean springClassesAreAvailableToDeployment(String deploymentName) throws IOException, HttpException {
        return isClassAvailableToDeployment(deploymentName, ApplicationContext.class);
    }

    private void assertResponse(String deploymentName) throws IOException, HttpException {

        String CONTEXT_URL = PortProviderUtil.generateURL("/", deploymentName);

        HttpGet httpget = new HttpGet(CONTEXT_URL + TestResource.TEST_PATH);

        HttpClientContext context = HttpClientContext.create();
        CloseableHttpResponse response = client.execute(httpget, context);
        Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        HttpEntity entity = response.getEntity();
        String responseString = EntityUtils.toString(entity, StandardCharsets.UTF_8);
        Assertions.assertEquals(TestResource.TEST_RESPONSE, responseString, "The server resource didn't send correct response");
        httpget.releaseConnection();
    }

    private static void addSpringLibraries(WebArchive archive) {
        // you need to use the 'meta-inf' attribute to import the contents of meta-inf so spring can find the correct namespace handlers
        if (isDefinedSystemProperty("use-jboss-deployment-structure")) {
            archive.addAsManifestResource("jboss-deployment-structure.xml");
        } else {
            archive.addAsManifestResource(new StringAsset("Dependencies: org.springframework.spring meta-inf\n"),
                    "MANIFEST.MF");
        }
    }

    private static boolean isDefinedSystemProperty(String name) {
        String value = System.getProperty(name);
        return (value != null);
    }

}
