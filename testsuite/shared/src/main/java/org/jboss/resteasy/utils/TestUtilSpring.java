package org.jboss.resteasy.utils;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;

/**
 * Base util class for RESTEasy spring related testing.
 */
public class TestUtilSpring {

    /**
     * Get Spring dependencies for specified spring version
     *
     * @return Spring libraries
     */
    private static File[] resolveSpringDependencies(String... additionalDeps) {
        final PomEquippedResolveStage resolver = Maven.resolver().loadPomFromFile("pom.xml");
        Set<File> runtimeDependencies = new HashSet<>();

        try {
            final File[] springDeps = resolver.resolve(
                    "org.springframework:spring-core",
                    "org.springframework:spring-web",
                    "org.springframework:spring-webmvc",
                    "org.springframework:spring-context",
                    "org.springframework:spring-expression",
                    "org.springframework:spring-beans",
                    "org.springframework:spring-aop").withTransitivity()
                    .asFile();
            runtimeDependencies.addAll(Arrays.asList(springDeps));
            if (includeResteasySpring()) {
                runtimeDependencies.add(resolver.resolve("org.jboss.resteasy.spring:resteasy-spring").withoutTransitivity()
                        .asSingleFile());
            }
            if (additionalDeps != null && additionalDeps.length > 0) {
                final File[] additional = resolver.resolve(additionalDeps)
                        .withTransitivity()
                        .asFile();
                runtimeDependencies.addAll(Arrays.asList(additional));
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to get artifacts from maven via Aether library", e);
        }

        return runtimeDependencies.toArray(new File[0]);
    }

    /**
     * Adds Spring libraries and its dependencies into webarchove
     *
     * @param archive
     */
    public static void addSpringLibraries(WebArchive archive) {
        archive.addAsLibraries(resolveSpringDependencies());
    }

    /**
     * Adds Spring libraries and its dependencies into webarchove
     *
     * @param archive
     */
    public static void addSpringLibraries(final WebArchive archive, final String... additionalDeps) {
        archive.addAsLibraries(resolveSpringDependencies(additionalDeps));
    }

    /**
     * Returns the version of this project to test with via the property {@code version.test.org.jboss.resteasy.spring}.
     * Note an empty or {@code null} value will result in an error.
     *
     * @return the version of this project to test with
     */
    public static String getResteasySpringVersion() {
        return System.getProperty("version.test.org.jboss.resteasy.spring");
    }

    /**
     * Indicates whether the RESTEasy Spring dependencies should be included in the deployment or as a module.
     * <p>
     * This value is controlled by the system property {@code include.resteasy.spring}. A value of {@code true} or an
     * empty string will enable this feature. All other values are treated as {@code false}.
     * </p>
     *
     * @return {@code true} if the dependencies should be included
     */
    public static boolean includeResteasySpring() {
        final String value = System.getProperty("include.resteasy.spring");
        if (value == null) {
            return false;
        }
        return "".equals(value.trim()) || Boolean.parseBoolean(value);
    }

}
