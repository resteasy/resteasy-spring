package org.jboss.resteasy.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jboss.resteasy.utils.maven.MavenUtil;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * Base util class for RESTEasy spring related testing.
 */
public class TestUtilSpring {

    private static final String defaultSpringVersion = "5.3.7";

    /**
     * Get spring version
     *
     * @return Spring version.
     */
    private static String getSpringVersion() {
        return System.getProperty("version.org.springframework", defaultSpringVersion);
    }

    /**
     * Get Spring dependencies for specified spring version
     *
     * @param springVersion
     * @return Spring libraries
     */
    private static File[] resolveSpringDependencies(String springVersion, String... additionalDeps) {
        MavenUtil mavenUtil;
        mavenUtil = MavenUtil.create(true);
        List<File> runtimeDependencies = new ArrayList<>();

        try {
            runtimeDependencies.add(mavenUtil.createMavenGavFile("org.springframework:spring-core:" + springVersion));
            runtimeDependencies.add(mavenUtil.createMavenGavFile("org.springframework:spring-web:" + springVersion));
            runtimeDependencies.add(mavenUtil.createMavenGavFile("org.springframework:spring-webmvc:" + springVersion));
            runtimeDependencies.add(mavenUtil.createMavenGavFile("org.springframework:spring-context:" + springVersion));
            runtimeDependencies.add(mavenUtil.createMavenGavFile("org.springframework:spring-expression:" + springVersion));
            runtimeDependencies.add(mavenUtil.createMavenGavFile("org.springframework:spring-beans:" + springVersion));
            runtimeDependencies.add(mavenUtil.createMavenGavFile("org.springframework:spring-aop:" + springVersion));
            if (includeResteasySpring()) {
                runtimeDependencies.add(mavenUtil.createMavenGavFile("org.jboss.resteasy.spring:resteasy-spring:" + getResteasySpringVersion()));
            }
            for (String dep : additionalDeps) {
                runtimeDependencies.add(mavenUtil.createMavenGavFile(dep));
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to get artifacts from maven via Aether library", e);
        }

        File[] dependencies = runtimeDependencies.toArray(new File[]{});
        return dependencies;
    }

    /**
     * Adds Spring libraries and its dependencies into webarchove
     *
     * @param archive
     */
    public static void addSpringLibraries(WebArchive archive) {
        archive.addAsLibraries(resolveSpringDependencies(getSpringVersion()));
    }

    /**
     * Adds Spring libraries and its dependencies into webarchove
     *
     * @param archive
     */
    public static void addSpringLibraries(final WebArchive archive, final String... additionalDeps) {
        archive.addAsLibraries(resolveSpringDependencies(getSpringVersion(), additionalDeps));
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
