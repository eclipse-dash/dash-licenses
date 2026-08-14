/*************************************************************************
 * Copyright (c) 2026 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.eclipse.dash.licenses.cli.CycloneDXSbomReader;
import org.eclipse.dash.licenses.cli.IDependencyListReader;
import org.eclipse.dash.licenses.cli.SpdxSbomReader;
import org.junit.jupiter.api.Test;

class SbomFileReaderTests {

    private static final String AFS_CYCLONEDX_JSON = "/afs-1.0.0-cyclonedx.json";
    private static final String CACHE_PARENT_CYCLONEDX_XML = "/cache-parent-1.1.0-cyclonedx.xml";
    private static final String SLF4J_SPDX_RDF = "/slf4j-test.spdx.rdf";
    private static final String SLF4J_SPDX_JSON = "/slf4j-test.spdx.json";
    private static final String SLF4J_SPDX_TAG_VALUE = "/slf4j-test.spdx";
    private static final String SLF4J_CYCLONEDX_YAML = "/slf4j-test-cyclonedx.yaml";
    private static final String SLF4J_SPDX_YAML = "/slf4j-test.spdx.yaml";

    // The reader chain Main uses: try CycloneDX, then SPDX.
    private static IDependencyListReader readerFor(File file) {
        IDependencyListReader reader = CycloneDXSbomReader.forFile(file);
        if (reader == null) {
            reader = SpdxSbomReader.forFile(file);
        }
        return reader;
    }

    @Test
    void testJsonFormat() throws Exception {
        var input = new File(this.getClass().getResource(AFS_CYCLONEDX_JSON).toURI());
        IDependencyListReader reader = readerFor(input);
        var expected = Arrays.asList(new String[] {
                "maven/mavencentral/org.eclipse.serializer/afs/1.0.0",
                "maven/mavencentral/org.eclipse.serializer/base/1.0.0",
                "maven/mavencentral/org.slf4j/slf4j-api/1.7.32"
        });
        var found = reader.getContentIds().stream().map(each -> each.toString()).collect(Collectors.toList());
        assertEquals(expected, found);
    }

    @Test
    void testXmlFormat() throws Exception {
        var input = new File(this.getClass().getResource(CACHE_PARENT_CYCLONEDX_XML).toURI());
        IDependencyListReader reader = readerFor(input);
        var expected = Arrays.asList(new String[] {
                "maven/mavencentral/org.eclipse.store/cache-parent/1.1.0"
        });
        var found = reader.getContentIds().stream().map(each -> each.toString()).collect(Collectors.toList());
        assertEquals(expected, found);
    }

    @Test
    void testRdfFormat() throws Exception {
        var input = new File(this.getClass().getResource(SLF4J_SPDX_RDF).toURI());
        IDependencyListReader reader = readerFor(input);
        var expected = Arrays.asList(new String[] {
                "maven/mavencentral/org.slf4j/slf4j-api/1.7.32"
        });
        var found = reader.getContentIds().stream().map(each -> each.toString()).collect(Collectors.toList());
        assertEquals(expected, found);
    }
    @Test
    void testSpdxJsonFormat() throws Exception {
        var input = new File(this.getClass().getResource(SLF4J_SPDX_JSON).toURI());
        IDependencyListReader reader = readerFor(input);
        var expected = Arrays.asList(new String[] {
                "maven/mavencentral/org.slf4j/slf4j-api/1.7.32"
        });
        var found = reader.getContentIds().stream().map(each -> each.toString()).collect(Collectors.toList());
        assertEquals(expected, found);
    }
    @Test
    void testSpdxTagValueFormat() throws Exception {
        var input = new File(this.getClass().getResource(SLF4J_SPDX_TAG_VALUE).toURI());
        IDependencyListReader reader = readerFor(input);
        var expected = Arrays.asList(new String[] {
                "maven/mavencentral/org.slf4j/slf4j-api/1.7.32"
        });
        var found = reader.getContentIds().stream().map(each -> each.toString()).collect(Collectors.toList());
        assertEquals(expected, found);
    }
    @Test
    void testCycloneDxYamlFormat() throws Exception {
        var input = new File(this.getClass().getResource(SLF4J_CYCLONEDX_YAML).toURI());
        IDependencyListReader reader = readerFor(input);
        var expected = Arrays.asList(new String[] {
                "maven/mavencentral/org.slf4j/slf4j-api/1.7.32"
        });
        var found = reader.getContentIds().stream().map(each -> each.toString()).collect(Collectors.toList());
        assertEquals(expected, found);
    }
    @Test
    void testSpdxYamlFormat() throws Exception {
        var input = new File(this.getClass().getResource(SLF4J_SPDX_YAML).toURI());
        IDependencyListReader reader = readerFor(input);
        var expected = Arrays.asList(new String[] {
                "maven/mavencentral/org.slf4j/slf4j-api/1.7.32"
        });
        var found = reader.getContentIds().stream().map(each -> each.toString()).collect(Collectors.toList());
        assertEquals(expected, found);
    }

}
