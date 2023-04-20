/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2022 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.resteasy.utils;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * This processor appends, assuming it does not already exist, a {@code jboss-deployment-structure.xml} file to
 * deployments. This deployment descriptor excludes CDI modules that cause issues as Spring does not support CDI.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class SpringApplicationArchiveProcessor implements ApplicationArchiveProcessor {
    private static final String EXCLUDED_MODULES =
            // This module adds CDI support for RESTEasy and should not be used for Spring deployments
            "            <module name=\"org.jboss.resteasy.resteasy-cdi\"/>\n";
    private static final String INCLUDED_MODULES =
            // Spring requires VFS and WFCORE-6250 removed. For now we need to add it until WFLY-17921 is fixed.
            "            <module name=\"org.jboss.vfs\"/>\n";
    private static final String SUBSYSTEMS =
            // This subsystem includes a resources which uses CDI and therefore cannot be used by Spring
            "            <subsystem name=\"microprofile-opentracing-smallrye\"/>\n" +
                    // May as well not have Weld process deployments
                    "            <subsystem name=\"weld\"/>\n" +
                    "            <subsystem name=\"jsf\"/>\n";

    private static final String FULL =
            "<?xml version=\"1.0\"?>\n" +
                    "<jboss-deployment-structure xmlns=\"urn:jboss:deployment-structure:1.2\"\n" +
                    "                            xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                    "    <deployment>\n" +
                    "        <exclude-subsystems>\n" +
                    SUBSYSTEMS +
                    "        </exclude-subsystems>\n" +
                    "        <exclusions>\n" +
                    EXCLUDED_MODULES +
                    "        </exclusions>\n" +
                    "        <dependencies>\n" +
                    INCLUDED_MODULES +
                    "        </dependencies>\n" +
                    "    </deployment>\n" +
                    "</jboss-deployment-structure>";

    @Override
    public void process(final Archive<?> applicationArchive, final TestClass testClass) {
        final ArchivePath path;
        if (applicationArchive instanceof WebArchive) {
            path = ArchivePaths.create("/WEB-INF/jboss-deployment-structure.xml");
        } else {
            path = ArchivePaths.create("/META-INF/jboss-deployment-structure.xml");
        }
        if (!applicationArchive.contains(path)) {
            applicationArchive.add(new StringAsset(FULL), path);
        }
    }
}
