package org.jboss.resteasy.test.spring.inmodule;

import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.logging.Logger;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.spi.HttpResponseCodes;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.resteasy.test.spring.inmodule.resource.SpringWebappContextResource;
import org.jboss.resteasy.utils.PortProviderUtil;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import org.wildfly.testing.tools.deployments.DeploymentDescriptors;

import java.io.FilePermission;
import java.lang.reflect.ReflectPermission;
import java.util.HashMap;
import java.util.Map;
import java.util.PropertyPermission;
import java.util.logging.LoggingPermission;

/**
 * @tpSubChapter Spring
 * @tpChapter Integration tests
 * @tpTestCaseDetails Test basic header and uri info context injection with spring dependencies on the classpath
 * @tpSince RESTEasy 3.0.16
 */
@ExtendWith(ArquillianExtension.class)
@RunAsClient
public class SpringWebappContextTest {

    private static Logger logger = Logger.getLogger(SpringWebappContextTest.class);
    private static final String BASE_URL = PortProviderUtil.generateBaseUrl(SpringWebappContextTest.class.getSimpleName());
    private static final String PATH = "/echo";
    private static final String EXPECTED_URI = BASE_URL + PATH + "/uri";
    private static final String EXPECTED_HEADERS = BASE_URL + PATH + "/headers" + ":text/plain";

    @Deployment
    private static Archive<?> deploy() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class, SpringWebappContextTest.class.getSimpleName() + ".war")
                .addClass(SpringWebappContextResource.class)
                .addAsWebInfResource(SpringWebappContextTest.class.getPackage(), "web.xml", "web.xml")
                .addAsWebInfResource(SpringWebappContextTest.class.getPackage(), "springWebAppContext/applicationContext.xml", "applicationContext.xml");
        archive.addAsManifestResource(new StringAsset("Dependencies: org.springframework.spring meta-inf\n"), "MANIFEST.MF");

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

    private String generateURL(String path) {
        return PortProviderUtil.generateURL(path, SpringWebappContextTest.class.getSimpleName());
    }

    /**
     * @tpTestDetails Get uri info from @Context injection
     * @tpSince RESTEasy 3.0.16
     */
    @Test
    public void testGetUri() throws Exception {
        doTestGet(PATH + "/uri", EXPECTED_URI, null);
    }

    /**
     * @tpTestDetails Get headers from @Context injection
     * @tpSince RESTEasy 3.0.16
     */
    @Test
    public void testGetHeaders() throws Exception {
        doTestGet(PATH + "/headers", EXPECTED_HEADERS, null);
    }

    /**
     * @tpTestDetails Test that the parameters given to the first request doesn't stick for the second request
     * @tpSince RESTEasy 3.0.16
     */
    @Test
    public void testParamsDontStick() throws Exception {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("param", "0");
        doTestGet(PATH + "/uri", EXPECTED_URI + "?param=0", parameters);
        parameters.put("param", "1");
        doTestGet(PATH + "/uri", EXPECTED_URI + "?param=1", parameters);
    }

    /**
     * @tpTestDetails Ensure concurrent invocations see different injected values
     * @tpSince RESTEasy 3.0.16
     */
    @Test
    public void testConcurrent() throws Exception {
        Thread uri = new Thread(new Runnable() {
            public void run() {
                for (int i = 0; i < 10; i++) {
                    try {
                        doTestGet(PATH + "/uri", EXPECTED_URI, null);
                    } catch (Exception e) {
                        Assertions.fail(e.toString());
                    }
                }
            }
        });
        Thread headers = new Thread(new Runnable() {
            public void run() {
                for (int i = 0; i < 10; i++) {
                    try {
                        doTestGet(PATH + "/headers", EXPECTED_HEADERS, null);
                    } catch (Exception e) {
                        Assertions.fail(e.toString());
                    }
                }
            }
        });
        uri.start();
        headers.start();
        uri.join();
        headers.join();
    }

    private void doTestGet(String context, String expectedReponsePattern, Map<String, String> parameters) throws Exception {

        Client client = ResteasyClientBuilder.newClient();
        WebTarget target = client.target(generateURL(context));

        WebTarget newTarget = null;
        if (parameters != null) {
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                logger.info("Entry parameters: " + entry.getKey() + ", " + entry.getValue());
                newTarget = target.queryParam(entry.getKey(), entry.getValue());
            }
        } else {
            newTarget = target;
        }

        verify(newTarget.request().accept("text/plain").get(), HttpResponseCodes.SC_OK, expectedReponsePattern);
        client.close();
    }

    private void verify(Response response, int expectedStatus, String expectedResponsePattern) throws Exception {
        Assertions.assertEquals(expectedStatus, response.getStatus(), "Unexpected response code");

        if (expectedResponsePattern != null) {
            String entity = response.readEntity(String.class);
            Assertions.assertTrue(entity.indexOf(expectedResponsePattern) != -1, "Unexpected response: " + entity + ", no match for: "
                    + expectedResponsePattern);
        }
    }
}
