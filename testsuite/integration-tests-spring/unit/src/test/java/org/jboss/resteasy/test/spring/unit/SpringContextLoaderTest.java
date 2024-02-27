package org.jboss.resteasy.test.spring.unit;

import org.jboss.resteasy.core.ResteasyDeploymentImpl;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.StaticWebApplicationContext;
import jakarta.servlet.ServletContext;


/**
 * @tpSubChapter Spring
 * @tpChapter Unit test
 * @tpTestCaseDetails Tests that SpringContextLoader does proper validations and adds an application listener
 * @tpSince RESTEasy 3.0.16
 */
public class SpringContextLoaderTest {

    private SpringContextLoaderSubclass contextLoader;

    @BeforeEach
    public void setupEditor() {
        contextLoader = new SpringContextLoaderSubclass();
    }

    /**
     * @tpTestDetails Tests that ResteasyDeployment is required for customizeContext() of SpringContextLoader
     */
    @Test
    public void testThatDeploymentIsRequired() {
        Assertions.assertThrows(RuntimeException.class, () -> contextLoader.customizeContext(mockServletContext(null), mockWebApplicationContext()));

    }

    /**
     * @tpTestDetails Tests that only one application listener is added for customizeContext() call of SpringContextLoader
     * @tpSince RESTEasy 3.0.16
     */
    @Test
    public void testThatWeAddedAnApplicationListener() {
        StaticWebApplicationContext context = mockWebApplicationContext();
        int numListeners = context.getApplicationListeners().size();
        contextLoader.customizeContext(mockServletContext(someDeployment()), context);
        int numListenersNow = context.getApplicationListeners().size();
        Assertions.assertEquals(
                numListeners + 1, numListenersNow, "Expected to add exactly one new listener; in fact added " + (numListenersNow - numListeners));
    }

    private StaticWebApplicationContext mockWebApplicationContext() {
        return new StaticWebApplicationContext();
    }

    private ServletContext mockServletContext(ResteasyDeployment deployment) {
        MockServletContext context = new MockServletContext();

        if (deployment != null) {
            context.setAttribute(ResteasyDeployment.class.getName(), deployment);
        }

        return context;
    }

    private ResteasyDeployment someDeployment() {
        return new ResteasyDeploymentImpl();
    }
}
