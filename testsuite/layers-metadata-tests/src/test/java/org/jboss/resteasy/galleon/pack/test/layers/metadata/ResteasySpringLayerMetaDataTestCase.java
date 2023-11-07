package org.jboss.resteasy.galleon.pack.test.layers.metadata;

import org.jboss.resteasy.utils.TestUtilSpring;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

import java.nio.file.Path;

public class ResteasySpringLayerMetaDataTestCase extends AbstractLayerMetaDataTestCase {
    private static Path web;
    private static Path web2;

    public static void createArchiveForResteasySpring(String webXmlPath) {
        WebArchive war = createWebArchive("web.xml", ResteasySpringLayerMetaDataTestCase.class.getResource(webXmlPath));
        ZipExporter exporter = war.as(ZipExporter.class);
        web = ARCHIVES_PATH.resolve("web.war");
        exporter.exportTo(web.toFile());
    }

    public static void createArchiveForResteasySpringWeb(String webXmlPath) {
        WebArchive war = createWebArchive("web.xml", ResteasySpringLayerMetaDataTestCase.class.getResource(webXmlPath));
        war.addClass(TestController.class);
        TestUtilSpring.addSpringLibraries(war);
        ZipExporter exporter = war.as(ZipExporter.class);
        web2 = ARCHIVES_PATH.resolve("web2.war");
        exporter.exportTo(web2.toFile());
    }

    @Test
    public void testResteasySpring() throws Exception {
        createArchiveForResteasySpring("/web.xml");
        checkLayersForArchive(web, "resteasy-spring");
    }

    @Test
    public void createArchiveForResteasySpringWeb() throws Exception {
        createArchiveForResteasySpringWeb("/web2.xml");
        checkLayersForArchive(web2, "resteasy-spring-web");
    }
}
