package org.jboss.resteasy.test.spring.inmodule;

import java.io.FilePermission;
import java.lang.reflect.ReflectPermission;
import java.util.Enumeration;
import java.util.PropertyPermission;
import java.util.logging.LoggingPermission;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.logging.Logger;
import org.jboss.resteasy.test.spring.inmodule.resource.ContextRefreshResource;
import org.jboss.resteasy.test.spring.inmodule.resource.ContextRefreshTrigger;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.web.context.WebApplicationContext;
import org.wildfly.testing.tools.deployments.DeploymentDescriptors;

/**
 * @tpSubChapter Spring
 * @tpChapter Integration tests
 * @tpTestCaseDetails Spring context refresh, RESTEASY-632
 * @tpSince RESTEasy 3.0.16
 */
@ExtendWith(ArquillianExtension.class)
public class ContextRefreshTest {

    private static Logger logger = Logger.getLogger(ContextRefreshTest.class);

    @Deployment
    private static Archive<?> deploy() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class, ContextRefreshTest.class.getSimpleName() + ".war")
                .addClass(ContextRefreshResource.class)
                .addClass(ContextRefreshTrigger.class)
                .addClass(ContextRefreshTest.class)
                .addAsWebInfResource(ContextRefreshTest.class.getPackage(), "web.xml", "web.xml")
                .addAsWebInfResource(ContextRefreshTest.class.getPackage(), "contextRefresh/applicationContext.xml",
                        "applicationContext.xml");
        archive.addAsManifestResource(new StringAsset("Dependencies: org.springframework.spring meta-inf\n"), "MANIFEST.MF");

        // Permission needed for "arquillian.debug" to run
        // "suppressAccessChecks" required for access to arquillian-core.jar
        // remaining permissions needed to run springframework
        archive.addAsManifestResource(DeploymentDescriptors.createPermissionsXmlAsset(
                new PropertyPermission("arquillian.*", "read"),
                new ReflectPermission("suppressAccessChecks"),
                new RuntimePermission("accessDeclaredMembers"),
                new FilePermission("<<ALL FILES>>", "read"),
                new LoggingPermission("control", "")), "permissions.xml");

        return archive;
    }

    /**
     * @tpTestDetails Refresh the persistent representation of the spring configuration twice
     * @tpSince RESTEasy 3.0.16
     */
    @Test
    public void testContextRefresh() throws Exception {
        Assertions.assertTrue(ContextRefreshTrigger.isOK());
        Enumeration<?> en = ContextRefreshTrigger.getApplicationContext().getServletContext().getAttributeNames();
        while (en.hasMoreElements()) {
            logger.info(en.nextElement());
        }
        Object o = ContextRefreshTrigger.getApplicationContext().getServletContext()
                .getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        logger.info(o);
        Assertions.assertFalse(o instanceof Exception);
    }
}
