package org.jboss.resteasy.galleon.pack.test.layers.metadata;

import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Path;

public class ResteasySpringLayerMetaDataTestCase extends AbstractLayerMetaDataTestCase {
    private static Path web;

    @BeforeClass
    public static void createArchives() {
        web = createWebArchive("web.war", "web.xml", ResteasySpringLayerMetaDataTestCase.class.getResource("/web.xml"));
    }

    @Test
    public void testWeb() throws Exception {
        checkLayersForArchive(web, "resteasy-spring");
    }
}
