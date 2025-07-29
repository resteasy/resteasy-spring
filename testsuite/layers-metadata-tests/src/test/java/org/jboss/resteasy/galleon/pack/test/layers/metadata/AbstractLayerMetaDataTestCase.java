package org.jboss.resteasy.galleon.pack.test.layers.metadata;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.wildfly.glow.Arguments;
import org.wildfly.glow.GlowMessageWriter;
import org.wildfly.glow.GlowSession;
import org.wildfly.glow.ScanResults;
import org.wildfly.glow.maven.MavenResolver;

public class AbstractLayerMetaDataTestCase {

    private static String URL_PROPERTY = "wildfly-glow-galleon-feature-packs-url";
    static Path ARCHIVES_PATH = Paths.get("target/glow-archives");

    @BeforeAll
    public static void prepareArchivesDirectory() throws Exception {
        Path glowXmlPath = Path.of("target/test-classes/glow");
        System.setProperty(URL_PROPERTY, glowXmlPath.toUri().toString());
        if (Files.exists(ARCHIVES_PATH)) {
            Files.walkFileTree(ARCHIVES_PATH, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        Files.createDirectories(ARCHIVES_PATH);
    }

    protected static WebArchive createWebArchive(String xmlName, URL xmlContent) {
        WebArchive war = ShrinkWrap.create(WebArchive.class);
        war.addAsWebInfResource(xmlContent, xmlName);
        return war;
    }

    protected Set<String> checkLayersForArchive(Path archivePath, String expectedLayer) throws Exception {
        Arguments arguments = Arguments.scanBuilder().setBinaries(Collections.singletonList(archivePath)).build();
        ScanResults scanResults = GlowSession.scan(MavenResolver.newMavenResolver(), arguments, GlowMessageWriter.DEFAULT);
        Set<String> foundLayers = scanResults.getDiscoveredLayers().stream().map(l -> l.getName()).collect(Collectors.toSet());
        Assertions.assertTrue(foundLayers.contains(expectedLayer));

        return foundLayers;
    }
}
