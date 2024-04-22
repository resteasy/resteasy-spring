package org.jboss.resteasy.test.spring.inmodule;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.ClientURI;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.test.spring.inmodule.resource.Contact;
import org.jboss.resteasy.test.spring.inmodule.resource.ContactService;
import org.jboss.resteasy.test.spring.inmodule.resource.Contacts;
import org.jboss.resteasy.test.spring.inmodule.resource.ContactsResource;
import org.jboss.resteasy.util.HttpHeaderNames;
import org.jboss.resteasy.utils.PortProviderUtil;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.wildfly.testing.tools.deployments.DeploymentDescriptors;

import java.io.FilePermission;
import java.lang.reflect.ReflectPermission;
import java.util.logging.LoggingPermission;

/**
 * @tpSubChapter Spring
 * @tpChapter Integration tests
 * @tpSince RESTEasy 3.0.16
 */
@ExtendWith(ArquillianExtension.class)
@RunAsClient
public class ContactsTest {

    private static Logger logger = Logger.getLogger(ContactsTest.class);
    private static ContactProxy proxy;
    private static ResteasyClient client;

    @Path(ContactsResource.CONTACTS_URL)
    public interface ContactProxy {
        @Path("data")
        @POST
        @Consumes(MediaType.APPLICATION_XML)
        Response createContact(Contact contact);

        @GET
        @Produces(MediaType.APPLICATION_XML)
        Contact getContact(@ClientURI String uri);

        @GET
        String getString(@ClientURI String uri);
    }

    @Deployment
    private static Archive<?> deploy() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class, ContactsTest.class.getSimpleName() + ".war")
                .addClass(ContactsResource.class)
                .addClass(ContactService.class)
                .addClass(Contacts.class)
                .addClass(Contact.class)
                .addClass(ContactsTest.class)
                .addAsWebInfResource(ContactsTest.class.getPackage(), "contacts/web.xml", "web.xml")
                .addAsWebInfResource(ContactsTest.class.getPackage(), "contacts/springmvc-servlet.xml", "springmvc-servlet.xml");
        archive.addAsManifestResource(new StringAsset("Dependencies: org.springframework.spring meta-inf\n"), "MANIFEST.MF");

        archive.addAsManifestResource(DeploymentDescriptors.createPermissionsXmlAsset(
                new ReflectPermission("suppressAccessChecks"),
                new RuntimePermission("accessDeclaredMembers"),
                new RuntimePermission("getClassLoader"),
                new FilePermission("<<ALL FILES>>", "read"),
                new LoggingPermission("control", "")
        ), "permissions.xml");

        return archive;
    }

    private String generateURL(String path) {
        return PortProviderUtil.generateURL(path, ContactsTest.class.getSimpleName());
    }

    /**
     * @tpTestDetails Test is using component-scan and annotation-config spring features. This features are unusable if
     * running with spring dependency 3.2.8.RELEASE and earlier. Only 3.2.9.RELEASE and spring 4 are supported.
     * @tpSince RESTEasy 3.0.16
     */
    @Test
    public void testData() {
        client = (ResteasyClient) ClientBuilder.newClient();
        proxy = client.target(generateURL("")).proxy(ContactProxy.class);
        Response response = proxy.createContact(new Contact("Solomon", "Duskis"));
        Assertions.assertEquals(201, response.getStatus());
        String duskisUri = (String) response.getMetadata().getFirst(HttpHeaderNames.LOCATION);
        logger.info(duskisUri);
        Assertions.assertTrue(duskisUri.endsWith(ContactsResource.CONTACTS_URL + "/data/Duskis"), "Unexpected response from the server");
        response.close();
        Assertions.assertEquals("Solomon", proxy.getContact(duskisUri).getFirstName(), "Unexpected response from the server");
        response = proxy.createContact(new Contact("Bill", "Burkie"));
        response.close();
        logger.info(proxy.getString(generateURL(ContactsResource.CONTACTS_URL + "/data")));
    }
}
