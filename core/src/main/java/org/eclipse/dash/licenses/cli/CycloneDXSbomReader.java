/*************************************************************************
 * Copyright (c) 2026 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.cli;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.cyclonedx.exception.ParseException;
import org.cyclonedx.model.Bom;
import org.cyclonedx.parsers.JsonParser;
import org.cyclonedx.parsers.XmlParser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.PackageUrlIdParser;

/**
 * Reads CycloneDX SBOMs (JSON, XML, YAML) and extracts the package URLs of every
 * component as {@link IContentId}s.
 *
 * Created via the {@link #forFile(File)} factory. It returns a reader if the file
 * is a CycloneDX SBOM it can handle, or {@code null} otherwise (including SPDX
 * files), so callers can fall through and try the SPDX reader next.
 *
 * For JSON/XML the file is parsed once, up front, into a {@link Bom} that is kept
 * and exposed via {@link #getSbom()} so the writer can reuse it. YAML has no
 * CycloneDX Bom parser, so it is walked as a tree instead and {@link #getSbom()}
 * returns {@code null}.
 */
public class CycloneDXSbomReader implements IDependencyListReader {

    // Exactly one of these is set: sbom for JSON/XML, yamlFile for YAML.
    private final Bom sbom;
    private final File yamlFile;

    private static final PackageUrlIdParser PURL_PARSER = new PackageUrlIdParser();
    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());

    private CycloneDXSbomReader(Bom sbom) {
        this.sbom = sbom;
        this.yamlFile = null;
    }

    private CycloneDXSbomReader(File yamlFile) {
        this.sbom = null;
        this.yamlFile = yamlFile;
    }

    public static CycloneDXSbomReader forFile(File file) {
        String name = file.getName().toLowerCase();

        if (name.endsWith(".json")) {
            try {
                Bom bom = new JsonParser().parse(file);
                return isValidBom(bom) ? new CycloneDXSbomReader(bom) : null;
            } catch (ParseException e) {
                return null;
            }
        }

        // plain .xml is CycloneDX; ".rdf.xml" is SPDX RDF and is not ours
        if (name.endsWith(".xml") && !name.endsWith(".rdf.xml")) {
            try {
                Bom bom = new XmlParser().parse(file);
                return isValidBom(bom) ? new CycloneDXSbomReader(bom) : null;
            } catch (ParseException e) {
                return null;
            }
        }

        if (name.endsWith(".yaml") || name.endsWith(".yml")) {
            return isValidYamlBom(file) ? new CycloneDXSbomReader(file) : null;
        }

        return null;
    }

    private static boolean isValidBom(Bom bom) {
        return bom != null
                && bom.getSpecVersion() != null
                && !bom.getSpecVersion().isBlank();
    }

    private static boolean isValidYamlBom(File file) {
        try {
            JsonNode root = YAML_MAPPER.readTree(file);
            return "CycloneDX".equalsIgnoreCase(root.path("bomFormat").asText())
                    && !root.path("specVersion").asText().isBlank();
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public Collection<IContentId> getContentIds() {
        // YAML: walk the tree (no Bom available)
        if (sbom == null) {
            return fromYamlTree(yamlFile);
        }

        // JSON/XML: read from the parsed Bom
        List<IContentId> results = new ArrayList<>();
        if (sbom.getMetadata() != null && sbom.getMetadata().getComponent() != null) {
            // The metadata component often has no purl, so guard against null.
            addPurl(results, sbom.getMetadata().getComponent().getPurl());
        }
        if (sbom.getComponents() != null) {
            for (var component : sbom.getComponents()) {
                addPurl(results, component.getPurl());
            }
        }
        return results;
    }

    private List<IContentId> fromYamlTree(File file) {
        List<IContentId> results = new ArrayList<>();
        try {
            JsonNode root = YAML_MAPPER.readTree(file);
            for (JsonNode component : root.path("components")) {
                addPurl(results, component.path("purl").asText(null));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return results;
    }

    private void addPurl(List<IContentId> results, String purl) {
        if (purl == null) {
            return;
        }
        IContentId id = PURL_PARSER.parseId(purl);
        if (id != null) {
            results.add(id);
        }
    }

    /**
     * @return the parsed CycloneDX Bom for JSON/XML input, or {@code null} for YAML
     *         (which has no Bom). Lets the writer reuse the already-parsed document.
     */
    public Bom getSbom() {
        return sbom;
    }
}
