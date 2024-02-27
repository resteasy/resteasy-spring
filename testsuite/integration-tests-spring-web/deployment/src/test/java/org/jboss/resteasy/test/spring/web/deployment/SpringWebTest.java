package org.jboss.resteasy.test.spring.web.deployment;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.resteasy.spi.HttpResponseCodes;
import org.jboss.resteasy.test.spring.web.deployment.resource.Greeting;
import org.jboss.resteasy.test.spring.web.deployment.resource.GreetingControllerWithNoRequestMapping;
import org.jboss.resteasy.test.spring.web.deployment.resource.ResponseEntityController;
import org.jboss.resteasy.test.spring.web.deployment.resource.ResponseStatusController;
import org.jboss.resteasy.test.spring.web.deployment.resource.SomeClass;
import org.jboss.resteasy.test.spring.web.deployment.resource.TestController;
import org.jboss.resteasy.utils.PortProviderUtil;
import org.jboss.resteasy.utils.TestUtil;
import org.jboss.resteasy.utils.TestUtilSpring;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;

@ExtendWith(ArquillianExtension.class)
@RunAsClient
public class SpringWebTest {

    static Client client;
    private static final String DEPLOYMENT_NAME = "springdep";

    @BeforeEach
    public void before() throws Exception {
        client = ClientBuilder.newClient();
    }

    @AfterEach
    public void after() throws Exception {
        client.close();
    }

    @Test
    public void verifyGetWithQueryParam() {
        WebTarget target = client.target(getBaseURL() + TestController.CONTROLLER_PATH + "/hello?name=people");
        Response response = target.request().get();
        Assertions.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
        String str = response.readEntity(String.class);
        Assertions.assertEquals("hello people", str, "Unexpected response content from the server");
    }

    @Test
    public void verifyGetToMethodWithoutForwardSlash() {
        WebTarget target = client.target(getBaseURL() + TestController.CONTROLLER_PATH + "/yolo");
        Response response = target.request().get();
        Assertions.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
        String str = response.readEntity(String.class);
        Assertions.assertEquals("yolo", str, "Unexpected response content from the server");
    }

    @Test
    public void verifyGetUsingDefaultValue() {
        WebTarget target = client.target(getBaseURL() + TestController.CONTROLLER_PATH + "/hello2");
        Response response = target.request().get();
        Assertions.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
        String str = response.readEntity(String.class);
        Assertions.assertEquals("hello world", str, "Unexpected response content from the server");
    }

    @Test
    public void verifyGetUsingNonEnglishChars() {
        WebTarget target = client.target(getBaseURL() + TestController.CONTROLLER_PATH + "/hello3?name=Γιώργος");
        Response response = target.request().get();
        Assertions.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
        String str = response.readEntity(String.class);
        Assertions.assertEquals("hello Γιώργος", str, "Unexpected response content from the server");
    }

    @Test
    public void verifyPathWithWildcard() {
        WebTarget target = client.target(getBaseURL() + TestController.CONTROLLER_PATH + "/wildcard/whatever/world");
        Response response = target.request().get();
        Assertions.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
        String str = response.readEntity(String.class);
        Assertions.assertEquals("world", str, "Unexpected response content from the server");
    }

    @Test
    public void verifyPathWithMultipleWildcards() {
        WebTarget target = client.target(getBaseURL() + TestController.CONTROLLER_PATH + "/wildcard2/something/folks/somethingelse");
        Response response = target.request().get();
        Assertions.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
        String str = response.readEntity(String.class);
        Assertions.assertEquals("folks", str, "Unexpected response content from the server");
    }

    @Test
    public void verifyPathWithAntStyleWildCard() {
        WebTarget target = client.target(getBaseURL() + TestController.CONTROLLER_PATH + "/antwildcard/whatever/we/want");
        Response response = target.request().get();
        Assertions.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
        String str = response.readEntity(String.class);
        Assertions.assertEquals("ant", str, "Unexpected response content from the server");
    }

    @Test
    public void verifyPathWithCharacterWildCard() {
        for (char c : new char[]{'t', 'r'}) {
            WebTarget target = client.target(getBaseURL() + TestController.CONTROLLER_PATH + String.format("/ca%cs", c));
            Response response = target.request().get();
            Assertions.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
            String str = response.readEntity(String.class);
            Assertions.assertEquals("single", str, "Unexpected response content from the server");
        }
    }

    @Test
    public void verifyPathWithMultipleCharacterWildCards() {
        for (String path : new String[]{"/cars/shop/info", "/cart/show/info"}) {
            WebTarget target = client.target(getBaseURL() + TestController.CONTROLLER_PATH + path);
            Response response = target.request().get();
            Assertions.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
            String str = response.readEntity(String.class);
            Assertions.assertEquals("multiple", str, "Unexpected response content from the server");
        }
    }

    @Test
    public void verifyPathVariableTypeConversion() {
        WebTarget target = client.target(getBaseURL() + TestController.CONTROLLER_PATH + "/int/9");
        Response response = target.request().get();
        Assertions.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
        String str = response.readEntity(String.class);
        Assertions.assertEquals("10", str, "Unexpected response content from the server");
    }


    @Test
    public void verifyJsonGetWithPathParamAndGettingMapping() {
        WebTarget target = client.target(getBaseURL() + TestController.CONTROLLER_PATH + "/" + "json/dummy");
        Response response = target.request().get();
        Assertions.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
        String str = response.readEntity(String.class);
        Assertions.assertTrue(str.contains("dummy"), "Unexpected response content from the server");
    }

    @Test
    public void verifyJsonOnRequestMappingGetWithPathParamAndRequestMapping() {
        WebTarget target = client.target(getBaseURL() + TestController.CONTROLLER_PATH + "/" + "json2/dummy");
        Response response = target.request().get();
        Assertions.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
        String str = response.readEntity(String.class);
        Assertions.assertTrue(str.contains("dummy"), "Unexpected response content from the server");
    }

    @Test
    public void verifyJsonPostWithPostMapping() {
        WebTarget target = client.target(getBaseURL() + TestController.CONTROLLER_PATH + "/" + "json");
        Response response = target.request().post(Entity.entity("{\"message\": \"hi\"}", MediaType.APPLICATION_JSON));
        Assertions.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
        String str = response.readEntity(String.class);
        Assertions.assertTrue(str.contains("hi"), "Unexpected response content from the server");
    }

    @Test
    public void verifyJsonPostWithRequestMapping() {
        WebTarget target = client.target(getBaseURL() + TestController.CONTROLLER_PATH + "/" + "json2");
        Response response = target.request().post(Entity.entity("{\"message\": \"hi\"}", MediaType.APPLICATION_JSON));
        Assertions.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
        String str = response.readEntity(String.class);
        Assertions.assertTrue(str.contains("hi"), "Unexpected response content from the server");
    }

    @Test
    public void verifyMultipleInputAndJsonResponse() {
        WebTarget target = client.target(getBaseURL() + TestController.CONTROLLER_PATH + "/" + "json3?suffix=!");
        Response response = target.request().put(Entity.entity("{\"message\": \"hi\"}", MediaType.APPLICATION_JSON));
        Assertions.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
        String str = response.readEntity(String.class);
        Assertions.assertTrue(str.contains("hi!"), "Unexpected response content from the server");
    }

    @Test
    public void verifyHttpServletRequestParameterInjection() {
        WebTarget target = client.target(getBaseURL() + TestController.CONTROLLER_PATH + "/" + "servletRequest");
        Response response = target.request().get();
        Assertions.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
        String str = response.readEntity(String.class);
        Assertions.assertTrue(str.contains("localhost") || str.contains("127.0.0.1"), "Unexpected response content from the server");
    }

    @Test
    public void verifyEmptyContentResponseEntity() {
        WebTarget target = client.target(getBaseURL() + ResponseEntityController.CONTROLLER_PATH + "/noContent");
        Response response = target.request().get();
        Assertions.assertEquals(HttpResponseCodes.SC_NO_CONTENT, response.getStatus());
    }

    @Test
    public void verifyStringContentResponseEntity() {
        WebTarget target = client.target(getBaseURL() + ResponseEntityController.CONTROLLER_PATH + "/string");
        Response response = target.request().get();
        Assertions.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
        String str = response.readEntity(String.class);
        Assertions.assertEquals("hello world", str, "Unexpected response content from the server");
    }

    @Test
    public void verifyJsonContentResponseEntity() {
        WebTarget target = client.target(getBaseURL() + ResponseEntityController.CONTROLLER_PATH + "/" + "json");
        Response response = target.request().get();
        Assertions.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
        SomeClass someClass = response.readEntity(SomeClass.class);
        Assertions.assertEquals(someClass.getMessage(), "dummy", "Unexpected response content from the server");
        Assertions.assertTrue(response.getHeaderString("custom-header").contains("somevalue"), "Incorrect headers response");
    }

    @Test
    public void verifyJsonContentResponseEntityWithoutType() {
        WebTarget target = client.target(getBaseURL() + ResponseEntityController.CONTROLLER_PATH + "/" + "json2");
        Response response = target.request().get();
        Assertions.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
        SomeClass someClass = response.readEntity(SomeClass.class);
        Assertions.assertEquals(someClass.getMessage(), "dummy", "Unexpected response content from the server");
    }

    @Test
    public void verifyEmptyContentResponseStatus() {
        WebTarget target = client.target(getBaseURL() + ResponseStatusController.CONTROLLER_PATH + "/noContent");
        Response response = target.request().get();
        Assertions.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
    }

    @Test
    public void verifyStringResponseStatus() {
        WebTarget target = client.target(getBaseURL() + ResponseStatusController.CONTROLLER_PATH + "/string");
        Response response = target.request().get();
        Assertions.assertEquals(HttpResponseCodes.SC_ACCEPTED, response.getStatus());
        String str = response.readEntity(String.class);
        Assertions.assertEquals("accepted", str, "Unexpected response content from the server");
    }

    @Test
    public void verifyControllerWithoutRequestMapping() {
        WebTarget target = client.target(getBaseURL() + "hello");
        Response response = target.request().get();
        Assertions.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
        String str = response.readEntity(String.class);
        Assertions.assertEquals("hello world", str, "Unexpected response content from the server");
    }


    private String getBaseURL() {
        return PortProviderUtil.generateURL("/", DEPLOYMENT_NAME);
    }

    @Deployment
    public static Archive<?> createDeployment() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class, DEPLOYMENT_NAME + ".war");
        archive.addAsWebInfResource(SpringWebTest.class.getPackage(), "web.xml", "web.xml");


        TestUtilSpring.addSpringLibraries(archive, "org.jboss.resteasy.spring:resteasy-spring-web:" + TestUtilSpring.getResteasySpringVersion());
        archive.as(ZipExporter.class).exportTo(new File("target", DEPLOYMENT_NAME + ".war"), true);
        return TestUtil.finishContainerPrepare(archive, null,
                SomeClass.class, Greeting.class, TestController.class, ResponseEntityController.class, ResponseStatusController.class, GreetingControllerWithNoRequestMapping.class);
    }

}
