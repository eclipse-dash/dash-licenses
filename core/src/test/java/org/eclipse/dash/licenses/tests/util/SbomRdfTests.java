/*************************************************************************
 * Copyright (c) 2026 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.tests.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.Collection;

import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.cli.CycloneDXSbomReader;
import org.eclipse.dash.licenses.cli.IDependencyListReader;
import org.eclipse.dash.licenses.cli.SpdxSbomReader;
import org.junit.jupiter.api.Test;

class SbomFileReaderRdfTest {

    // The reader chain Main uses: try CycloneDX, then SPDX.
    private static IDependencyListReader readerFor(File file) {
        IDependencyListReader reader = CycloneDXSbomReader.forFile(file);
        if (reader == null) {
            reader = SpdxSbomReader.forFile(file);
        }
        return reader;
    }

    @Test
    void testParseSpdxRdfXml() throws Exception {
        File file = new File(getClass().getClassLoader().getResource("test.spdx.rdf").getFile());
        IDependencyListReader reader = readerFor(file);

        Collection<IContentId> ids = reader.getContentIds();

        assertNotNull(ids);
        assertFalse(ids.isEmpty(), "Expected at least one content ID");

        IContentId id = ids.iterator().next();
        assertEquals("maven", id.getType());
        assertEquals("org.apache.commons", id.getNamespace());
        assertEquals("commons-lang3", id.getName());
        assertEquals("3.12.0", id.getVersion());
    }

    @Test
    void testParseSpdxRdfXml_skipsNonPurl() throws Exception {
        File file = new File(getClass().getClassLoader().getResource("test.spdx.rdf").getFile());
        IDependencyListReader reader = readerFor(file);

        Collection<IContentId> ids = reader.getContentIds();

        assertEquals(1, ids.size());
    }
}
